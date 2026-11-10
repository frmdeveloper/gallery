package id.frmdeveloper.gallery

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Point
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.CloudMediaProvider
import android.provider.CloudMediaProviderContract
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Penyedia media cloud buat Pemilih Foto sistem Android: album + media
 * Google Drive (dari akun yg login di Galeri FRM) muncul di photo picker
 * aplikasi lain, berdampingan dgn media lokal — pola yg sama kayak
 * Google Foto. Pengguna memilih penyedia aktif di setelan pemilih
 * ("Aplikasi media cloud"). Semua baca-saja, datanya dari DriveApi.scan
 * (di-cache 5 menit biar pemilih gak nunggu scan ulang tiap buka).
 */
class PenyediaCloud : CloudMediaProvider() {

    companion object {
        private const val TAG = "PenyediaCloud"
        private const val API = "https://www.googleapis.com/drive/v3"
        private const val TTL_CACHE = 5 * 60 * 1000L
        private val KOLOM_MEDIA = arrayOf(
            CloudMediaProviderContract.MediaColumns.ID,
            CloudMediaProviderContract.MediaColumns.DATE_TAKEN_MILLIS,
            CloudMediaProviderContract.MediaColumns.MIME_TYPE,
            CloudMediaProviderContract.MediaColumns.SIZE_BYTES,
            CloudMediaProviderContract.MediaColumns.WIDTH,
            CloudMediaProviderContract.MediaColumns.HEIGHT,
            CloudMediaProviderContract.MediaColumns.DURATION_MILLIS,
            CloudMediaProviderContract.MediaColumns.ORIENTATION,
            CloudMediaProviderContract.MediaColumns.STANDARD_MIME_TYPE_EXTENSION,
            CloudMediaProviderContract.MediaColumns.IS_FAVORITE,
            CloudMediaProviderContract.MediaColumns.SYNC_GENERATION,
        )
        private val KOLOM_ALBUM = arrayOf(
            CloudMediaProviderContract.AlbumColumns.ID,
            CloudMediaProviderContract.AlbumColumns.DISPLAY_NAME,
            CloudMediaProviderContract.AlbumColumns.DATE_TAKEN_MILLIS,
            CloudMediaProviderContract.AlbumColumns.MEDIA_COUNT,
            CloudMediaProviderContract.AlbumColumns.MEDIA_COVER_ID,
        )
    }

    /** Hasil scan gabungan semua akun + kapan diambil. */
    private data class DataCloud(
        val items: List<MediaItem>,
        val albums: List<Pair<String, Album>>, // idAlbumPicker -> album
        val diambilPada: Long,
    )

    @Volatile private var cache: DataCloud? = null
    private val kunciCache = Any()

    override fun onCreate(): Boolean {
        DriveApi.init(context!!)
        return true
    }

    // ---------------- data ----------------

    private fun data(): DataCloud {
        val sekarang = System.currentTimeMillis()
        cache?.let { if (sekarang - it.diambilPada < TTL_CACHE) return it }
        synchronized(kunciCache) {
            val s2 = System.currentTimeMillis()
            cache?.let { if (s2 - it.diambilPada < TTL_CACHE) return it }
            val semuaItem = mutableListOf<MediaItem>()
            val semuaAlbum = mutableListOf<Pair<String, Album>>()
            for (akun in DriveApi.daftarAkun()) {
                val scan = runCatching { DriveApi.scan(akun) }.getOrNull() ?: continue
                semuaItem += scan.allItems
                for (album in scan.albums) {
                    val idPicker = if (album.id.endsWith(":all")) {
                        "${akun.email}|ALL"
                    } else {
                        "${akun.email}|F|${album.name}"
                    }
                    semuaAlbum += idPicker to album
                }
            }
            val gabung = DataCloud(
                items = semuaItem.sortedByDescending { it.dateMillis },
                albums = semuaAlbum,
                diambilPada = s2,
            )
            cache = gabung
            return gabung
        }
    }

    private fun idMedia(item: MediaItem): String = "${item.akunDrive}|${item.driveId}"

    private fun pecahIdMedia(mediaId: String): Pair<String, String>? {
        val i = mediaId.indexOf('|')
        if (i <= 0) return null
        return mediaId.substring(0, i) to mediaId.substring(i + 1)
    }

    private fun cariItem(mediaId: String): MediaItem? =
        data().items.firstOrNull { idMedia(it) == mediaId }

    private fun idKoleksi(): String {
        val emails = DriveApi.daftarAkun().joinToString(",") { it.email }
        return "galeri-frm-drive-" + emails.hashCode().toUInt().toString(16)
    }

    private fun generasiSync(d: DataCloud): Long = d.items.firstOrNull()?.dateMillis ?: 0L

    // ---------------- kontrak CloudMediaProvider ----------------

    override fun onGetMediaCollectionInfo(extras: Bundle): Bundle {
        val d = data()
        val hasil = Bundle()
        hasil.putString(
            CloudMediaProviderContract.MediaCollectionInfo.MEDIA_COLLECTION_ID,
            idKoleksi(),
        )
        hasil.putLong(
            CloudMediaProviderContract.MediaCollectionInfo.LAST_MEDIA_SYNC_GENERATION,
            generasiSync(d),
        )
        DriveApi.akunUtama()?.email?.let {
            hasil.putString(CloudMediaProviderContract.MediaCollectionInfo.ACCOUNT_NAME, it)
        }
        runCatching {
            val intent = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pi = android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
            hasil.putParcelable(
                CloudMediaProviderContract.MediaCollectionInfo.ACCOUNT_CONFIGURATION_INTENT, pi,
            )
        }
        return hasil
    }

    override fun onQueryMedia(extras: Bundle): Cursor {
        val d = data()
        val albumId = extras.getString(CloudMediaProviderContract.EXTRA_ALBUM_ID)
        var daftar: List<MediaItem> = if (albumId.isNullOrBlank()) {
            d.items
        } else {
            val album = d.albums.firstOrNull { it.first == albumId }?.second
            album?.items ?: emptyList()
        }
        // paginasi: token = offset
        val ukuranHalaman = extras.getInt(CloudMediaProviderContract.EXTRA_PAGE_SIZE, 200)
            .coerceIn(1, 1000)
        val offset = extras.getString(CloudMediaProviderContract.EXTRA_PAGE_TOKEN)
            ?.toIntOrNull() ?: 0
        val total = daftar.size
        daftar = daftar.drop(offset).take(ukuranHalaman)

        val cursor = MatrixCursor(KOLOM_MEDIA)
        for (item in daftar) {
            val extKhusus = when {
                item.mime == "image/gif" ->
                    CloudMediaProviderContract.MediaColumns.STANDARD_MIME_TYPE_EXTENSION_GIF
                else ->
                    CloudMediaProviderContract.MediaColumns.STANDARD_MIME_TYPE_EXTENSION_NONE
            }
            cursor.addRow(
                arrayOf(
                    idMedia(item),
                    item.dateMillis,
                    item.mime,
                    item.size,
                    item.width,
                    item.height,
                    if (item.isVideo) item.durationMs else 0L,
                    0,
                    extKhusus,
                    0,
                    item.dateMillis,
                ),
            )
        }
        val sisa = total - (offset + daftar.size)
        val resp = Bundle()
        resp.putString(
            CloudMediaProviderContract.EXTRA_MEDIA_COLLECTION_ID, idKoleksi(),
        )
        if (sisa > 0) {
            resp.putString(
                CloudMediaProviderContract.EXTRA_PAGE_TOKEN,
                (offset + daftar.size).toString(),
            )
        }
        cursor.setExtras(resp)
        return cursor
    }

    override fun onQueryAlbums(extras: Bundle): Cursor {
        val d = data()
        val cursor = MatrixCursor(KOLOM_ALBUM)
        for ((idPicker, album) in d.albums) {
            val teratas = album.items.firstOrNull()
            val nama = if (idPicker.endsWith("|ALL")) {
                "Google Drive • ${album.akun}"
            } else {
                album.name
            }
            cursor.addRow(
                arrayOf(
                    idPicker,
                    nama,
                    teratas?.dateMillis ?: 0L,
                    album.items.size,
                    teratas?.let { idMedia(it) } ?: "",
                ),
            )
        }
        val resp = Bundle()
        resp.putString(CloudMediaProviderContract.EXTRA_MEDIA_COLLECTION_ID, idKoleksi())
        cursor.setExtras(resp)
        return cursor
    }

    override fun onQueryDeletedMedia(extras: Bundle): Cursor =
        MatrixCursor(arrayOf(CloudMediaProviderContract.MediaColumns.ID))

    @Throws(FileNotFoundException::class)
    override fun onOpenMedia(
        mediaId: String,
        extras: Bundle?,
        signal: CancellationSignal?,
    ): ParcelFileDescriptor {
        val (email, fileId) = pecahIdMedia(mediaId)
            ?: throw FileNotFoundException("id media tidak dikenal: $mediaId")
        val akun = DriveApi.akunUntukEmail(email)
            ?: throw FileNotFoundException("akun tidak login: $email")
        val token = DriveApi.accessTokenBlocking(akun)
            ?: throw FileNotFoundException("token Drive tidak tersedia")
        val url = "$API/files/${URLEncoder.encode(fileId, "UTF-8")}?alt=media"
        val conn = bukaKoneksi(url, token)
        val input = try {
            conn.inputStream
        } catch (e: Exception) {
            throw FileNotFoundException("unduh media gagal: ${e.message}")
        }
        val pipa = ParcelFileDescriptor.createPipe()
        val baca = pipa[0]
        val tulis = pipa[1]
        val thread = Thread {
            try {
                FileOutputStream(tulis.fileDescriptor).use { out ->
                    input.use { it.copyTo(out) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "stream media putus: ${e.message}")
            } finally {
                runCatching { conn.disconnect() }
            }
        }
        thread.isDaemon = true
        thread.start()
        return baca
    }

    @Throws(FileNotFoundException::class)
    override fun onOpenPreview(
        mediaId: String,
        size: Point,
        extras: Bundle?,
        signal: CancellationSignal?,
    ): AssetFileDescriptor {
        val item = cariItem(mediaId)
            ?: throw FileNotFoundException("media tidak ketemu: $mediaId")
        val ctx = context!!
        val berkas = File(ctx.cacheDir, "cloudpreview/${item.driveId}.img")
        berkas.parentFile?.mkdirs()
        // thumbnail Drive (=s512 dari scan) — unduh sekali, pakai ulang
        if (!berkas.exists() || berkas.length() == 0L) {
            val ok = runCatching {
                val token = DriveApi.akunUntukEmail(item.akunDrive)
                    ?.let { DriveApi.accessTokenBlocking(it) }
                if (item.thumbUrl.isNotBlank()) {
                    unduhKe(item.thumbUrl, token, berkas)
                } else {
                    // gak ada thumbnail (jarang): pakai berkas aslinya
                    val t2 = token ?: throw FileNotFoundException("token kosong")
                    unduhKe(
                        "$API/files/${URLEncoder.encode(item.driveId, "UTF-8")}?alt=media",
                        t2, berkas,
                    )
                }
            }.isSuccess
            if (!ok || !berkas.exists() || berkas.length() == 0L) {
                throw FileNotFoundException("thumbnail gagal diunduh: $mediaId")
            }
        }
        val pfd = ParcelFileDescriptor.open(berkas, ParcelFileDescriptor.MODE_READ_ONLY)
        return AssetFileDescriptor(pfd, 0, berkas.length())
    }

    // ---------------- util jaringan ----------------

    private fun bukaKoneksi(url: String, token: String?): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 20_000
        conn.readTimeout = 60_000
        conn.instanceFollowRedirects = true
        if (!token.isNullOrBlank()) conn.setRequestProperty("Authorization", "Bearer $token")
        return conn
    }

    private fun unduhKe(url: String, token: String?, tujuan: File) {
        val conn = bukaKoneksi(url, token)
        try {
            conn.inputStream.use { input ->
                FileOutputStream(tujuan).use { out -> input.copyTo(out) }
            }
        } finally {
            conn.disconnect()
        }
    }

}
