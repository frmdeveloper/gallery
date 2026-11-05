package id.frmdeveloper.gallery

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant

/**
 * Google Drive lewat REST API v3 RESMI + kredensial OAuth sendiri
 * (lihat DriveAuth). Menggantikan jalur SAF yang provider Drive-nya
 * setengah-setengah (folder tidak muncul / enumerasi kosong).
 *
 * Semua fungsi di sini BLOCKING — panggil dari Dispatchers.IO.
 * Token akses di-cache; refresh otomatis dari refresh token hasil login.
 */
object DriveApi {

    private const val API = "https://www.googleapis.com/drive/v3"
    private const val UPLOAD_API = "https://www.googleapis.com/upload/drive/v3"

    // Cache token PER AKUN (email -> (token, waktu kedaluwarsa ms))
    private val tokenByEmail = java.util.concurrent.ConcurrentHashMap<String, Pair<String, Long>>()
    private val tokenLock = Any()

    // Registrasi pemilik berkas (id file -> email akun): jalur yg cuma
    // tahu id (interceptor Coil, video, streaming) bisa milih token akun
    // yg benar dari sini.
    private val petaPemilik = java.util.concurrent.ConcurrentHashMap<String, String>()

    @Volatile private var appCtx: Context? = null

    /** Daftarkan context (dipanggil dari MainActivity & ViewModel). */
    fun init(context: Context) {
        appCtx = context.applicationContext
    }

    /** Semua akun yg login di HP ini (multi-akun). */
    fun daftarAkun(): List<AkunDrive> =
        appCtx?.let { DriveStore.loadDaftarAkun(it) } ?: emptyList()

    fun akunUtama(): AkunDrive? = daftarAkun().firstOrNull()

    fun akunUntukEmail(email: String): AkunDrive? =
        daftarAkun().firstOrNull { it.email == email }

    /** Buang cache token (satu akun bila email diisi, semua bila null). */
    fun resetToken(email: String? = null) {
        synchronized(tokenLock) {
            if (email == null) tokenByEmail.clear() else tokenByEmail.remove(email)
        }
    }

    /** Token dari cache saja (tanpa jaringan) — aman dari main thread. */
    fun cachedToken(akun: AkunDrive? = null): String? {
        val a = akun ?: akunUtama() ?: return null
        val entri = tokenByEmail[a.email] ?: return null
        return entri.first.takeIf { System.currentTimeMillis() < entri.second - 30_000 }
    }

    /**
     * Token valid milik [akun] (bawaan: akun pertama); refresh blocking
     * bila perlu. Refresh DIKUNCI biar request barengan tidak nembak
     * endpoint token rame-rame (thundering herd).
     */
    fun accessTokenBlocking(akun: AkunDrive? = null, force: Boolean = false): String? {
        val a = akun ?: akunUtama() ?: return null
        if (!force) cachedToken(a)?.let { return it }
        return synchronized(tokenLock) {
            if (!force) cachedToken(a)?.let { return it }
            refreshTokenLocked(a)
        }
    }

    private fun refreshTokenLocked(akun: AkunDrive): String? {
        return runCatching {
            val body = listOf(
                "client_id" to DriveAuth.clientId,
                "client_secret" to DriveAuth.clientSecret,
                "refresh_token" to akun.refreshToken,
                "grant_type" to "refresh_token",
            ).joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, "UTF-8")}" }
            val conn = (URL("https://oauth2.googleapis.com/token").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 20_000
                readTimeout = 20_000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) error("token HTTP ${conn.responseCode}")
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val token = json.getString("access_token")
            val ttl = json.optLong("expires_in", 3600)
            tokenByEmail[akun.email] = token to (System.currentTimeMillis() + ttl * 1000)
            token
        }.getOrNull()
    }

    private fun catatPemilik(idFile: String, email: String) {
        if (idFile.isNotBlank() && email.isNotBlank()) petaPemilik[idFile] = email
    }

    /** Token akun pemilik berkas ini (dari registrasi pindaian). */
    fun accessTokenUntukFile(idFile: String): String? {
        val akun = petaPemilik[idFile]?.let { akunUntukEmail(it) } ?: akunUtama()
        return akun?.let { accessTokenBlocking(it) }
    }

    fun cachedTokenUntukFile(idFile: String): String? {
        val akun = petaPemilik[idFile]?.let { akunUntukEmail(it) } ?: akunUtama()
        return akun?.let { cachedToken(it) }
    }

    fun accessTokenUntukEmail(email: String): String? =
        akunUntukEmail(email)?.let { accessTokenBlocking(it) } ?: accessTokenBlocking()

    private fun request(
        method: String,
        url: String,
        token: String,
        jsonBody: String? = null,
    ): Pair<Int, String> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 30_000
            readTimeout = 60_000
            setRequestProperty("Authorization", "Bearer $token")
            if (jsonBody != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
        }
        if (jsonBody != null) {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(jsonBody) }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.readText() ?: ""
        return code to text
    }

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    private fun parseWaktu(iso: String?): Long {
        if (iso.isNullOrBlank()) return 0
        return runCatching { Instant.parse(iso).toEpochMilli() }.getOrDefault(0)
    }

    private fun fileKeItem(f: JSONObject, folderNames: Map<String, String>, akun: AkunDrive): MediaItem {
        val id = f.getString("id")
        val mime = f.optString("mimeType")
        val isVideo = mime.startsWith("video/")
        val meta = if (isVideo) f.optJSONObject("videoMediaMetadata") else f.optJSONObject("imageMediaMetadata")
        val parents = f.optJSONArray("parents")
        val parentId = if (parents != null && parents.length() > 0) parents.optString(0) else ""
        val folderName = folderNames[parentId] ?: "Google Drive"
        var thumb = f.optString("thumbnailLink")
        // thumbnailLink bawaan kecil (=s220); naikkan ke s512 buat grid
        if (thumb.isNotBlank()) thumb = thumb.replace(Regex("=s\\d+$"), "=s512")
        catatPemilik(id, akun.email)
        return MediaItem(
            key = "driveapi:$id",
            akunDrive = akun.email,
            uri = Uri.parse("$API/files/$id?alt=media"),
            name = f.optString("name", "(tanpa nama)"),
            mime = mime,
            size = f.optString("size").toLongOrNull() ?: 0,
            dateMillis = parseWaktu(f.optString("modifiedTime").ifBlank { f.optString("createdTime") }),
            isVideo = isVideo,
            durationMs = meta?.optLong("durationMillis", 0) ?: 0,
            width = meta?.optInt("width", 0) ?: 0,
            height = meta?.optInt("height", 0) ?: 0,
            bucketId = "driveapi:$parentId",
            bucketName = folderName,
            relativePath = "",
            isDrive = true,
            driveId = id,
            thumbUrl = thumb,
        )
    }

    /**
     * Scan penuh: semua folder jadi album (yg ada medianya), plus album
     * "Seluruh Drive". Media di-query SEKALI lalu dikelompokkan per parent.
     */
    fun scan(akun: AkunDrive): DriveScan {
        val token = accessTokenBlocking(akun) ?: error("Gagal mengambil token Drive (offline / token mati)")

        // 1) daftar folder (buat nama album)
        val folderNames = mutableMapOf<String, String>()
        var pageToken: String? = null
        do {
            val q = enc("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
            val url = "$API/files?q=$q&fields=files(id,name),nextPageToken&pageSize=1000&orderBy=name" +
                (pageToken?.let { "&pageToken=${enc(it)}" } ?: "")
            val (code, text) = request("GET", url, token)
            if (code !in 200..299) error("list folder HTTP $code")
            val json = JSONObject(text)
            val files = json.optJSONArray("files") ?: JSONArray()
            for (i in 0 until files.length()) {
                val f = files.getJSONObject(i)
                folderNames[f.getString("id")] = f.optString("name", "Folder")
            }
            pageToken = json.optString("nextPageToken").ifBlank { null }
        } while (pageToken != null)

        // 2) semua media (cap 5 halaman x 1000)
        val items = mutableListOf<MediaItem>()
        pageToken = null
        var halaman = 0
        val fields = "files(id,name,mimeType,size,createdTime,modifiedTime,thumbnailLink,parents," +
            "imageMediaMetadata(width,height),videoMediaMetadata(width,height,durationMillis)),nextPageToken"
        do {
            val q = enc("(mimeType contains 'image/' or mimeType contains 'video/') and trashed = false")
            val url = "$API/files?q=$q&fields=${enc(fields)}&pageSize=1000&orderBy=modifiedTime desc" +
                (pageToken?.let { "&pageToken=${enc(it)}" } ?: "")
            val (code, text) = request("GET", url, token)
            if (code !in 200..299) error("list media HTTP $code")
            val json = JSONObject(text)
            val files = json.optJSONArray("files") ?: JSONArray()
            for (i in 0 until files.length()) {
                items += fileKeItem(files.getJSONObject(i), folderNames, akun)
            }
            pageToken = json.optString("nextPageToken").ifBlank { null }
            halaman++
        } while (pageToken != null && halaman < 5)

        val urut = items.sortedByDescending { it.dateMillis }
        val albums = mutableListOf<Album>()
        if (urut.isNotEmpty()) {
            albums += Album(
                id = "driveapi:${akun.email}:all", name = "Seluruh Drive",
                kind = AlbumKind.DRIVE, items = urut, akun = akun.email,
            )
        }
        urut.groupBy { it.bucketName }
            .entries.sortedBy { it.key }
            .forEach { (nama, grup) ->
                albums += Album(
                    id = "driveapi:${akun.email}:folder:$nama", name = nama,
                    kind = AlbumKind.DRIVE, items = grup, akun = akun.email,
                )
            }
        return DriveScan(albums = albums, allItems = urut)
    }

    /**
     * Daftar file AUDIO di Drive (buat tab Audio). URI-nya skema "gd://<id>"
     * — pas diputar, player menukar skema itu jadi URL unduh + header
     * Authorization (lihat GalleryViewModel.playerLagu). Durasi tidak
     * disediakan Drive API untuk audio -> 0 (player yang melaporkan durasi
     * asli pas memutar).
     */
    fun daftarAudio(akun: AkunDrive): List<AudioItem> {
        val token = accessTokenBlocking(akun) ?: error("Gagal mengambil token Drive (offline / token mati)")
        val folderNames = mutableMapOf<String, String>()
        var pageToken: String? = null
        do {
            val q = enc("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
            val url = "$API/files?q=$q&fields=files(id,name),nextPageToken&pageSize=1000&orderBy=name" +
                (pageToken?.let { "&pageToken=${enc(it)}" } ?: "")
            val (code, text) = request("GET", url, token)
            if (code !in 200..299) error("list folder HTTP $code")
            val json = JSONObject(text)
            val files = json.optJSONArray("files") ?: JSONArray()
            for (i in 0 until files.length()) {
                val f = files.getJSONObject(i)
                folderNames[f.getString("id")] = f.optString("name", "Folder")
            }
            pageToken = json.optString("nextPageToken").ifBlank { null }
        } while (pageToken != null)

        val hasil = mutableListOf<AudioItem>()
        pageToken = null
        var halaman = 0
        val fields = "files(id,name,mimeType,createdTime,modifiedTime,parents),nextPageToken"
        do {
            val q = enc("mimeType contains 'audio/' and trashed = false")
            val url = "$API/files?q=$q&fields=${enc(fields)}&pageSize=1000&orderBy=name" +
                (pageToken?.let { "&pageToken=${enc(it)}" } ?: "")
            val (code, text) = request("GET", url, token)
            if (code !in 200..299) error("list audio HTTP $code")
            val json = JSONObject(text)
            val files = json.optJSONArray("files") ?: JSONArray()
            for (i in 0 until files.length()) {
                val f = files.getJSONObject(i)
                val id = f.getString("id")
                val parents = f.optJSONArray("parents")
                val parentId = if (parents != null && parents.length() > 0) parents.optString(0) else ""
                val folderName = folderNames[parentId] ?: "Google Drive"
                val namaMentah = f.optString("name", "(tanpa nama)")
                catatPemilik(id, akun.email)
                hasil += AudioItem(
                    key = "driveaudio:$id",
                    uri = Uri.parse("gd://$id?a=${enc(akun.email)}"),
                    akunDrive = akun.email,
                    title = namaMentah.substringBeforeLast('.', namaMentah),
                    artist = "Google Drive",
                    album = folderName,
                    durationMs = 0,
                    artUri = Uri.EMPTY,
                    dateMillis = parseWaktu(f.optString("modifiedTime").ifBlank { f.optString("createdTime") }),
                    folder = folderName,
                )
            }
            pageToken = json.optString("nextPageToken").ifBlank { null }
            halaman++
        } while (pageToken != null && halaman < 5)
        return hasil
    }

    /** Upload resumable satu item lokal ke root Drive. True bila 200/201. */
    fun upload(context: Context, item: MediaItem, akun: AkunDrive): Boolean {
        val token = accessTokenBlocking(akun) ?: return false
        return runCatching {
            // buka sesi upload resumable
            val meta = JSONObject()
                .put("name", item.name)
                .put("mimeType", item.mime.ifEmpty { "application/octet-stream" })
                .toString()
            val conn = (URL("$UPLOAD_API/files?uploadType=resumable").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 30_000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("X-Upload-Content-Type", item.mime)
                if (item.size > 0) setRequestProperty("X-Upload-Content-Length", item.size.toString())
            }
            conn.outputStream.use { it.write(meta.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) error("sesi upload HTTP ${conn.responseCode}")
            val session = conn.getHeaderField("Location") ?: error("tidak ada URL sesi upload")

            // kirim isi file (streaming, chunked tetap dgn panjang pasti bila tahu size)
            val put = (URL(session).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 600_000
                if (item.size > 0) {
                    setFixedLengthStreamingMode(item.size)
                    setRequestProperty("Content-Length", item.size.toString())
                } else {
                    setChunkedStreamingMode(256 * 1024)
                }
                setRequestProperty("Content-Type", item.mime.ifEmpty { "application/octet-stream" })
            }
            val input = context.contentResolver.openInputStream(item.uri) ?: error("input lokal null")
            input.use { ins -> put.outputStream.use { outs -> ins.copyTo(outs, 256 * 1024) } }
            put.responseCode in 200..299
        }.getOrDefault(false)
    }

    /** Pindahkan file Drive ke sampah (trashed=true). */
    fun trash(driveId: String, akun: AkunDrive? = null): Boolean {
        val token = (akun?.let { accessTokenBlocking(it) } ?: accessTokenUntukFile(driveId)) ?: return false
        val (code, _) = runCatching {
            request("PATCH", "$API/files/$driveId", token, "{\"trashed\":true}")
        }.getOrDefault(0 to "")
        return code in 200..299
    }

    /** Daftar isi SAMPAH Drive (trashed=true), yg terakhir diubah duluan. */
    fun daftarSampah(akun: AkunDrive): List<MediaItem> {
        val token = accessTokenBlocking(akun) ?: return emptyList()
        val fields = "files(id,name,mimeType,size,createdTime,modifiedTime,thumbnailLink," +
            "imageMediaMetadata(width,height),videoMediaMetadata(width,height,durationMillis),parents)"
        val url = "$API/files?q=${enc("trashed=true")}&fields=${enc(fields)}" +
            "&pageSize=200&orderBy=${enc("modifiedTime desc")}"
        val (code, text) = runCatching { request("GET", url, token) }.getOrDefault(0 to "")
        if (code !in 200..299) return emptyList()
        val arr = runCatching { JSONObject(text).optJSONArray("files") }.getOrNull() ?: return emptyList()
        val hasil = mutableListOf<MediaItem>()
        for (i in 0 until arr.length()) {
            val f = arr.optJSONObject(i) ?: continue
            runCatching { fileKeItem(f, emptyMap(), akun) }.getOrNull()?.let { hasil += it }
        }
        return hasil
    }

    /** Pulihkan file Drive dari sampah (trashed=false). */
    fun pulihkan(driveId: String, akun: AkunDrive? = null): Boolean {
        val token = (akun?.let { accessTokenBlocking(it) } ?: accessTokenUntukFile(driveId)) ?: return false
        val (code, _) = runCatching {
            request("PATCH", "$API/files/$driveId", token, "{\"trashed\":false}")
        }.getOrDefault(0 to "")
        return code in 200..299
    }

    /** Hapus file Drive PERMANEN (tidak bisa dipulihkan). */
    fun hapusPermanen(driveId: String, akun: AkunDrive? = null): Boolean {
        val token = (akun?.let { accessTokenBlocking(it) } ?: accessTokenUntukFile(driveId)) ?: return false
        val (code, _) = runCatching {
            request("DELETE", "$API/files/$driveId", token)
        }.getOrDefault(0 to "")
        return code in 200..299
    }
}
