package id.frmdeveloper.gallery

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AlbumKind { LOCAL, DRIVE, FAVORITES }

data class MediaItem(
    val key: String,            // uri string, unik
    val uri: Uri,
    val name: String,
    val mime: String,
    val size: Long,
    val dateMillis: Long,
    val isVideo: Boolean,
    val durationMs: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val bucketId: String = "",
    val bucketName: String = "",
    val relativePath: String = "",
    /** Path absolut berkas (MediaStore DATA); kosong buat item Drive. */
    val path: String = "",
    val isDrive: Boolean = false,
    /** Email akun Drive pemilik item ini (multi-akun); kosong buat lokal. */
    val akunDrive: String = "",
    val driveId: String = "",     // khusus item Drive API: id file di Drive
    val thumbUrl: String = "",    // khusus item Drive API: thumbnailLink (grid)
)

data class Album(
    val id: String,
    val name: String,
    val kind: AlbumKind,
    val items: List<MediaItem>,
    /** Lencana sumber penyimpanan buat pojok kartu album. */
    val ikon: IkonSumber = IkonSumber.HP,
    /** Email akun pemilik (khusus album Drive multi-akun). */
    val akun: String = "",
) {
    val count: Int get() = items.size
    val cover: MediaItem? get() = items.firstOrNull()
}

val LocaleID: Locale = Locale("id", "ID")

/** Sumber penyimpanan sebuah album — ditampilkan sbg ikon di pojok kartu. */
enum class IkonSumber { DRIVE, HP, SD, USB, FAVORIT }

/**
 * Klasifikasikan folder lokal: HP (penyimpanan utama) / kartu SD / USB.
 * Dicocokkan ke volume beneran dari StorageManager (bukan nebak nama):
 * volume utama/emulated = HP; volume lepas-pasang berdeskripsi "USB" =
 * flashdisk; sisanya kartu SD.
 */
fun ikonSumberLokal(context: Context, path: String): IkonSumber {
    if (path.isBlank()) return IkonSumber.HP
    val sm = context.getSystemService(Context.STORAGE_SERVICE)
        as? android.os.storage.StorageManager ?: return IkonSumber.HP
    val vol = runCatching {
        sm.storageVolumes.firstOrNull { v ->
            val dir = v.directory?.absolutePath ?: return@firstOrNull false
            path == dir || path.startsWith("$dir/")
        }
    }.getOrNull()
    if (vol == null) {
        return if (path.startsWith("/storage/emulated")) IkonSumber.HP else IkonSumber.SD
    }
    return when {
        vol.isPrimary || vol.isEmulated -> IkonSumber.HP
        "usb" in (runCatching { vol.getDescription(context) }.getOrNull()
            ?.lowercase() ?: "") -> IkonSumber.USB
        else -> IkonSumber.SD
    }
}

/** Salinan album dengan lencana sumbernya terisi sesuai kind + isi. */
fun Album.denganIkon(context: Context): Album = copy(
    ikon = when (kind) {
        AlbumKind.DRIVE -> IkonSumber.DRIVE
        AlbumKind.FAVORITES -> IkonSumber.FAVORIT
        AlbumKind.LOCAL -> ikonSumberLokal(context, items.firstOrNull()?.path ?: "")
    },
)

// Formatter java.time: THREAD-SAFE + jauh lebih cepat dari SimpleDateFormat.
// (Versi lama bikin SimpleDateFormat BARU per item pas grouping ribuan item
// di main thread — salah satu sumber lag v1.3.)
private val FMT_LENGKAP = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", LocaleID)
private val FMT_BULAN = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", LocaleID)
private val FMT_TAHUN = java.time.format.DateTimeFormatter.ofPattern("yyyy", LocaleID)
private val FMT_SINGKAT = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, HH.mm", LocaleID)
private val ZONE: java.time.ZoneId = java.time.ZoneId.systemDefault()

private fun keTanggal(millis: Long): java.time.LocalDateTime =
    java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), ZONE)

fun formatTanggalLengkap(millis: Long): String = FMT_LENGKAP.format(keTanggal(millis))

fun formatBulan(millis: Long): String = FMT_BULAN.format(keTanggal(millis))

fun formatTahun(millis: Long): String = FMT_TAHUN.format(keTanggal(millis))

fun formatTanggalSingkat(millis: Long): String = FMT_SINGKAT.format(keTanggal(millis))

fun formatUkuran(bytes: Long): String {
    if (bytes <= 0) return "-"
    val units = arrayOf("B", "KB", "MB", "GB")
    var v = bytes.toDouble()
    var i = 0
    while (v >= 1024 && i < units.lastIndex) { v /= 1024; i++ }
    return DecimalFormat("#,##0.#").format(v).replace(',', 'X').replace('.', ',').replace('X', '.') + " " + units[i]
}

fun formatDurasi(ms: Long): String {
    if (ms <= 0) return ""
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}

/** Satu file musik dari MediaStore Audio. Cover art via albumart URI. */
data class AudioItem(
    val key: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artUri: Uri,
    val dateMillis: Long,
    /** Nama folder tempat file ini tinggal (buat grup ala Album). */
    val folder: String = "",
    /** Path berkas lokal (MediaStore DATA); kosong buat lagu Drive. Dipakai
     * nulis cover ke tag berkas (fitur ganti cover). */
    val path: String = "",
    /** Email akun Drive pemilik lagu ini (multi-akun); kosong buat lokal. */
    val akunDrive: String = "",
)

/** Grup audio ala Album: "Semua audio" + satu per folder. */
data class AudioFolder(
    val id: String,
    val name: String,
    val items: List<AudioItem>,
    /** "lokal" = file di HP, "drive" = file di Google Drive. */
    val sumber: String = "lokal",
) {
    val count: Int get() = items.size
    val cover: AudioItem? get() = items.firstOrNull()
}

fun kunciHari(millis: Long): String {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
}

object MediaRepo {

    fun loadMedia(context: Context): List<MediaItem> {
        val resolver: ContentResolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.DURATION,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            @Suppress("DEPRECATION") MediaStore.Files.FileColumns.DATA,
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?,?)"
        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )
        val result = mutableListOf<MediaItem>()
        resolver.query(collection, projection, selection, args, null)?.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val iName = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val iMime = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val iSize = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val iTaken = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val iAdded = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val iBucket = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val iBucketName = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val iW = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val iH = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val iDur = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
            val iPath = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
            val iType = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            @Suppress("DEPRECATION")
            val iData = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            while (c.moveToNext()) {
                val id = c.getLong(iId)
                val taken = c.getLong(iTaken)
                val added = c.getLong(iAdded) * 1000L
                val date = if (taken > 0) taken else added
                val uri = ContentUris.withAppendedId(collection, id)
                val mime = c.getString(iMime) ?: ""
                result += MediaItem(
                    key = uri.toString(),
                    uri = uri,
                    name = c.getString(iName) ?: "(tanpa nama)",
                    mime = mime,
                    size = c.getLong(iSize),
                    dateMillis = date,
                    isVideo = c.getInt(iType) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    durationMs = c.getLong(iDur),
                    width = c.getInt(iW),
                    height = c.getInt(iH),
                    bucketId = c.getString(iBucket) ?: "",
                    bucketName = c.getString(iBucketName) ?: "Lainnya",
                    relativePath = c.getString(iPath) ?: "",
                    path = c.getString(iData) ?: "",
                )
            }
        }
        return result.sortedByDescending { it.dateMillis }
    }

    /**
     * Isi SAMPAH lokal: item bertanda trashed TIDAK muncul di query biasa —
     * harus minta eksplisit via QUERY_ARG_MATCH_TRASHED = MATCH_ONLY.
     */
    fun loadSampah(context: Context): List<MediaItem> {
        val resolver: ContentResolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.DURATION,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
        )
        val queryArgs = android.os.Bundle().apply {
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
            putString(
                ContentResolver.QUERY_ARG_SQL_SELECTION,
                "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?,?)",
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                ),
            )
            putString(
                ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC",
            )
        }
        val result = mutableListOf<MediaItem>()
        runCatching {
            resolver.query(collection, projection, queryArgs, null)?.use { c ->
                val iId = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val iName = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val iMime = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val iSize = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val iTaken = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
                val iAdded = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val iBucket = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
                val iBucketName = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                val iW = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
                val iH = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
                val iDur = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
                val iPath = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                val iType = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    val taken = c.getLong(iTaken)
                    val added = c.getLong(iAdded) * 1000L
                    val date = if (taken > 0) taken else added
                    val uri = ContentUris.withAppendedId(collection, id)
                    val mime = c.getString(iMime) ?: ""
                    result += MediaItem(
                        key = uri.toString(),
                        uri = uri,
                        name = c.getString(iName) ?: "(tanpa nama)",
                        mime = mime,
                        size = c.getLong(iSize),
                        dateMillis = date,
                        isVideo = c.getInt(iType) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                        durationMs = c.getLong(iDur),
                        width = c.getInt(iW),
                        height = c.getInt(iH),
                        bucketId = c.getString(iBucket) ?: "",
                        bucketName = c.getString(iBucketName) ?: "Lainnya",
                        relativePath = c.getString(iPath) ?: "",
                    )
                }
            }
        }
        return result
    }

    /**
     * Scan AUDIO (tab Audio — SENGAJA dipisah dari scan foto/video, audio
     * tidak pernah masuk Album/Gambar). Thumbnail = cover art album dari
     * MediaStore (content://media/external/audio/albumart/<albumId>) yang
     * isinya gambar tertanam di file musiknya.
     */
    fun loadAudio(context: Context): List<AudioItem> {
        val resolver: ContentResolver = context.contentResolver
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            @Suppress("DEPRECATION") MediaStore.Audio.Media.DATA,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val result = mutableListOf<AudioItem>()
        runCatching {
            resolver.query(
                collection, projection, selection, null,
                "${MediaStore.Audio.Media.TITLE} ASC",
            )?.use { c ->
                val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val iTitle = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val iArtist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val iAlbumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val iAdded = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                @Suppress("DEPRECATION")
                val iData = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val art = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        c.getLong(iAlbumId),
                    )
                    val pathMentah = c.getString(iData) ?: ""
                    val namaFolder = pathMentah.substringBeforeLast('/')
                        .substringAfterLast('/')
                        .ifBlank { "Penyimpanan" }
                    result += AudioItem(
                        key = uri.toString(),
                        uri = uri,
                        title = c.getString(iTitle)?.takeIf { it.isNotBlank() } ?: "(tanpa judul)",
                        artist = c.getString(iArtist)?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Artis tidak dikenal",
                        album = c.getString(iAlbum) ?: "",
                        durationMs = c.getLong(iDur),
                        artUri = art,
                        dateMillis = c.getLong(iAdded) * 1000L,
                        folder = namaFolder,
                        path = pathMentah,
                    )
                }
            }
        }
        return result
    }

    fun loadAlbums(media: List<MediaItem>): List<Album> {
        return media.groupBy { it.bucketId }
            .map { (bucketId, items) ->
                Album(
                    id = "local:$bucketId",
                    name = items.firstOrNull()?.bucketName ?: "Album",
                    kind = AlbumKind.LOCAL,
                    items = items,
                )
            }
            .sortedByDescending { it.items.firstOrNull()?.dateMillis ?: 0 }
    }
}

object FavoritesStore {
    private const val PREF = "galeri_drive_prefs"
    private const val KEY_FAV = "favorites"

    fun load(context: Context): Set<String> =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getStringSet(KEY_FAV, emptySet())?.toSet() ?: emptySet()

    fun save(context: Context, favs: Set<String>) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_FAV, favs).apply()
    }
}

/**
 * Simpanan keadaan UI (perintah user 2026-11-05): tab terakhir yang dibuka
 * + ukuran thumbnail (level zoom grid Gambar & detail album, kolom +
 * mode daftar Album & folder Audio) — semua dipulihkan pas aplikasi
 * dibuka lagi.
 */
object UiStore {
    private const val PREF = "galeri_drive_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun loadTab(context: Context): String =
        prefs(context).getString("ui_tab", "gambar") ?: "gambar"

    fun saveTab(context: Context, tab: String) {
        prefs(context).edit().putString("ui_tab", tab).apply()
    }

    fun loadInt(context: Context, kunci: String, bawaan: Int): Int =
        prefs(context).getInt(kunci, bawaan)

    fun saveInt(context: Context, kunci: String, nilai: Int) {
        prefs(context).edit().putInt(kunci, nilai).apply()
    }

    fun loadString(context: Context, kunci: String, bawaan: String): String? =
        prefs(context).getString(kunci, bawaan)

    fun saveString(context: Context, kunci: String, nilai: String) {
        prefs(context).edit().putString(kunci, nilai).apply()
    }

    fun loadBool(context: Context, kunci: String, bawaan: Boolean): Boolean =
        prefs(context).getBoolean(kunci, bawaan)

    fun saveBool(context: Context, kunci: String, nilai: Boolean) {
        prefs(context).edit().putBoolean(kunci, nilai).apply()
    }
}


/**
 * Posisi terakhir pemutaran VIDEO per item — video lanjut dari terakhir
 * ditonton, bukan dari nol (perintah user). JSON kecil {key: posisiMs}
 * di SharedPreferences, dibatasi 200 entri.
 */
object PosisiVideoStore {
    private const val KUNCI = "posisi_video_json"

    fun baca(context: Context, key: String): Long {
        val mentah = UiStore.loadString(context, KUNCI, "") ?: return 0L
        return runCatching { org.json.JSONObject(mentah).optLong(key, 0L) }.getOrDefault(0L)
    }

    fun simpan(context: Context, key: String, posisiMs: Long) {
        if (posisiMs <= 0) return
        val mentah = UiStore.loadString(context, KUNCI, "") ?: ""
        val obj = runCatching { org.json.JSONObject(mentah) }.getOrNull() ?: org.json.JSONObject()
        obj.put(key, posisiMs)
        val keys = obj.keys().asSequence().toList()
        if (keys.size > 200) keys.take(keys.size - 200).forEach { obj.remove(it) }
        UiStore.saveString(context, KUNCI, obj.toString())
    }

    fun hapus(context: Context, key: String) {
        val mentah = UiStore.loadString(context, KUNCI, "") ?: return
        val obj = runCatching { org.json.JSONObject(mentah) }.getOrNull() ?: return
        if (obj.has(key)) {
            obj.remove(key)
            UiStore.saveString(context, KUNCI, obj.toString())
        }
    }
}

/** "7,1 MB" / "812 KB" buat dialog Rincian. */
fun formatUkuranBerkas(b: Long): String = when {
    b >= 1_000_000L -> String.format(java.util.Locale("id", "ID"), "%.1f MB", b / 1_000_000.0)
    b >= 1_000L -> String.format(java.util.Locale("id", "ID"), "%.1f KB", b / 1_000.0)
    else -> "$b B"
}
