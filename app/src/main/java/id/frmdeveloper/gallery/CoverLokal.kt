package id.frmdeveloper.gallery

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.Fetcher
import coil.fetch.FetchResult
import coil.fetch.DrawableResult
import coil.request.Options
import coil.size.Dimension
import java.io.File
import java.security.MessageDigest

/**
 * Versi kecil cover (JPEG maks 512px) buat ditanam sebagai artworkData di
 * metadata sesi: data artwork lewat IPC ke sistem, jadi WAJIB kecil —
 * byte gambar asli bisa ber-MB. Cadangan terakhir: albumart MediaStore.
 */
fun ambilKecil(ctx: Context, lagu: AudioItem, maks: Int = 512): ByteArray? {
    var mentah = CoverLokal.ambil(ctx, lagu)
    if (mentah == null && lagu.artUri != Uri.EMPTY) {
        mentah = runCatching {
            ctx.contentResolver.openInputStream(lagu.artUri)?.use { it.readBytes() }
        }.getOrNull()
    }
    val bmp = mentah?.let { dekodeGambar(it, maks) } ?: return null
    val arus = java.io.ByteArrayOutputStream()
    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, arus)
    return arus.toByteArray()
}

/** Model permintaan gambar cover: satu lagu (bukan URI album MediaStore). */
data class CoverLagu(val lagu: AudioItem)

/**
 * Ambil gambar cover LANGSUNG dari tag berkas lagu itu sendiri
 * (jaudiotagger), BUKAN dari albumart MediaStore. MediaStore itu per-ALBUM
 * dan ekstraknya tidak bisa diandalkan buat lagu yg cover-nya baru ditulis
 * sendiri — makanya cover hasil suntingan tidak kunjung tampil. Hasil
 * ekstrak disimpan di cache berkas berkunci path + waktu-ubah berkas:
 * berkas berubah (cover diganti) -> kunci berubah -> ekstrak ulang.
 */
object CoverLokal {
    fun ambil(ctx: Context, lagu: AudioItem): ByteArray? {
        val sumber = File(lagu.path)
        if (!sumber.isFile) return null
        val dir = File(ctx.cacheDir, "coverlagu").apply { mkdirs() }
        val kunci = MessageDigest.getInstance("SHA-256")
            .digest(lagu.path.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(24)
        val berkas = File(dir, "$kunci-${sumber.lastModified()}.jpg")
        if (berkas.isFile && berkas.length() > 0) {
            return runCatching { berkas.readBytes() }.getOrNull()
        }
        // Jalur utama: MediaMetadataRetriever lewat URI MediaStore —
        // pengekstrak yg sama dgn yg dipakai CX File Explorer & sistem
        // (terbukti bisa membaca cover berkas ini), tanpa isu izin path.
        val dariRetriever = runCatching {
            val r = android.media.MediaMetadataRetriever()
            try {
                r.setDataSource(ctx, lagu.uri)
                r.embeddedPicture
            } finally {
                r.release()
            }
        }.getOrNull()
        // Cadangan: jaudiotagger langsung dari path berkas
        val mentah = dariRetriever ?: runCatching {
            val af = org.jaudiotagger.audio.AudioFileIO.read(sumber)
            af.tag?.firstArtwork?.binaryData
        }.getOrNull() ?: return null
        // Buang varian lama berkas yg sama, simpan ekstrak baru
        dir.listFiles()
            ?.filter { it.name.startsWith("$kunci-") && it.name != berkas.name }
            ?.forEach { runCatching { it.delete() } }
        runCatching { berkas.writeBytes(mentah) }
        return mentah
    }
}

/**
 * Fetcher Coil buat [CoverLagu]: utamakan gambar tertanam di berkas lagu
 * (per berkas, selalu segar); kalau tidak ada, jatuh ke albumart
 * MediaStore. Hasilnya DrawableResult berisi bitmap yg didekode +
 * dikecilkan sendiri (maks ~1200px) — Coil 2.7 tidak menyediakan
 * BitmapResult dan konstruktor ImageSource-nya protected, jadi jalur ini
 * yg aman. Keduanya gagal -> error, pemanggil menampilkan gradasi.
 */
class FetcherCoverLagu(
    private val lagu: AudioItem,
    private val appContext: Context,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val target = (options.size.width as? Dimension.Pixels)?.px
            ?.coerceIn(64, 1200) ?: 1024
        if (lagu.uri.scheme == "content" && lagu.path.isNotBlank()) {
            val bmp = CoverLokal.ambil(appContext, lagu)
                ?.let { dekodeGambar(it, target) }
            if (bmp != null) {
                return DrawableResult(
                    drawable = BitmapDrawable(appContext.resources, bmp),
                    isSampled = true,
                    dataSource = DataSource.DISK,
                )
            }
        }
        if (lagu.artUri != Uri.EMPTY) {
            val bytes = runCatching {
                appContext.contentResolver.openInputStream(lagu.artUri)
                    ?.use { it.readBytes() }
            }.getOrNull()
            val bmp = bytes?.let { dekodeGambar(it, target) }
            if (bmp != null) {
                return DrawableResult(
                    drawable = BitmapDrawable(appContext.resources, bmp),
                    isSampled = true,
                    dataSource = DataSource.DISK,
                )
            }
        }
        error("lagu ini tidak punya cover")
    }

    class Factory(private val appContext: Context) : Fetcher.Factory<CoverLagu> {
        override fun create(
            data: CoverLagu,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = FetcherCoverLagu(data.lagu, appContext, options)
    }
}

/** Dekode byte gambar jadi bitmap, diskalakan kasar ke sekitar [target] px. */
private fun dekodeGambar(bytes: ByteArray, target: Int): android.graphics.Bitmap? {
    val batas = android.graphics.BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, batas)
    if (batas.outWidth <= 0 || batas.outHeight <= 0) return null
    var contoh = 1
    while (maxOf(batas.outWidth, batas.outHeight) / contoh > target * 2) contoh *= 2
    val opsi = android.graphics.BitmapFactory.Options().apply { inSampleSize = contoh }
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opsi)
}
