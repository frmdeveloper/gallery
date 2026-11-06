package id.frmdeveloper.gallery

import android.net.Uri
import android.util.Base64
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Login Google Drive ke AKUN LAIN (bukan akun gd.js) lewat OAuth loopback:
 * aplikasi buka server HTTP kecil di 127.0.0.1 (port acak), browser dibuka ke
 * halaman login Google (client OAuth gd.js, tipe "desktop" — client tipe ini
 * MENERIMA redirect loopback, sudah diverifikasi: URL auth -> 302 ke sign-in).
 * User login pakai akun Google mana pun -> Google redirect balik ke
 * 127.0.0.1 -> kode ditukar (PKCE + client_secret tertanam) jadi refresh
 * token akun tsb, disimpan lokal di prefs aplikasi.
 *
 * Jalur device-code DITOLAK Google (401 invalid_client) karena device flow
 * cuma buat client tipe "TVs and Limited Input" — client gd.js tipe desktop.
 *
 * Semua fungsi BLOCKING — panggil dari Dispatchers.IO.
 */
object DriveLogin {

    class Sesi internal constructor(
        val urlAuth: String,
        internal val state: String,
        internal val verifier: String,
        internal val server: ServerSocket,
    ) {
        val redirectUri: String = "http://127.0.0.1:${server.localPort}/"
        @Volatile var dibatalkan: Boolean = false
        fun batalkan() {
            dibatalkan = true
            runCatching { server.close() }
        }
    }

    private fun acakUrlSafe(nBytes: Int): String {
        val b = ByteArray(nBytes)
        SecureRandom().nextBytes(b)
        return Base64.encodeToString(b, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun tantanganPkce(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /** Bind server lokal + susun URL login Google. */
    fun mulai(): Sesi {
        val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val state = acakUrlSafe(16)
        val verifier = acakUrlSafe(48)
        val redirect = "http://127.0.0.1:${server.localPort}/"
        val url = "https://accounts.google.com/o/oauth2/v2/auth" +
            "?client_id=${URLEncoder.encode(DriveAuth.clientId, "UTF-8")}" +
            "&redirect_uri=${URLEncoder.encode(redirect, "UTF-8")}" +
            "&response_type=code" +
            "&scope=${URLEncoder.encode("https://www.googleapis.com/auth/drive openid email", "UTF-8")}" +
            "&access_type=offline&prompt=select_account%20consent" +
            "&state=$state" +
            "&code_challenge=${tantanganPkce(verifier)}&code_challenge_method=S256"
        return Sesi(url, state, verifier, server)
    }

    /**
     * Tunggu SATU redirect dari browser (timeout 5 menit).
     * Balik authorization code; lempar error bila gagal/timeout/dibatalkan.
     */
    fun tungguKode(sesi: Sesi): String {
        sesi.server.soTimeout = 300_000
        sesi.server.accept().use { socket ->
            socket.soTimeout = 15_000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
            val baris = reader.readLine() ?: error("Redirect kosong")
            // buang sisa header
            while (true) {
                val h = reader.readLine()
                if (h.isNullOrEmpty()) break
            }
            val target = baris.split(" ").getOrNull(1) ?: error("Request redirect tidak dikenal")
            val params = Uri.parse("http://x$target")
            val errorParam = params.getQueryParameter("error")
            val kode = params.getQueryParameter("code")
            val stateBalik = params.getQueryParameter("state")
            val sukses = errorParam == null && kode != null && stateBalik == sesi.state
            val html = if (sukses) {
                "<html><body style='font-family:sans-serif;text-align:center;margin-top:60px'>" +
                    "<h2>Login berhasil</h2><p>Kembali ke aplikasi Galeri FRM.</p></body></html>"
            } else {
                "<html><body style='font-family:sans-serif;text-align:center;margin-top:60px'>" +
                    "<h2>Login gagal</h2><p>Kembali ke aplikasi Galeri FRM dan coba lagi.</p></body></html>"
            }
            val bytes = html.toByteArray(Charsets.UTF_8)
            val out = socket.getOutputStream()
            out.write(
                ("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: ${bytes.size}\r\nConnection: close\r\n\r\n").toByteArray(Charsets.UTF_8)
            )
            out.write(bytes)
            out.flush()
            if (errorParam != null) error("Google menolak login: $errorParam")
            if (stateBalik != sesi.state) error("State OAuth tidak cocok")
            return kode ?: error("Kode login tidak ada")
        }
    }

    /** Tukar code -> (refreshToken, email akun). */
    fun tukarKode(sesi: Sesi, code: String): Pair<String, String> {
        val body = listOf(
            "client_id" to DriveAuth.clientId,
            "client_secret" to DriveAuth.clientSecret,
            "code" to code,
            "code_verifier" to sesi.verifier,
            "grant_type" to "authorization_code",
            "redirect_uri" to sesi.redirectUri,
        ).joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, "UTF-8")}" }
        val conn = (URL("https://oauth2.googleapis.com/token").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        if (conn.responseCode !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
            error("Tukar kode HTTP ${conn.responseCode}: ${err.take(160)}")
        }
        val json = JSONObject(conn.inputStream.bufferedReader().readText())
        val refresh = json.optString("refresh_token")
        if (refresh.isBlank()) error("Google tidak memberi refresh token (coba login ulang)")
        val access = json.optString("access_token")
        var email = ""
        if (access.isNotBlank()) {
            runCatching {
                val ui = (URL("https://openidconnect.googleapis.com/v1/userinfo").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("Authorization", "Bearer $access")
                }
                if (ui.responseCode in 200..299) {
                    email = JSONObject(ui.inputStream.bufferedReader().readText()).optString("email")
                }
            }
        }
        return refresh to email
    }
}
