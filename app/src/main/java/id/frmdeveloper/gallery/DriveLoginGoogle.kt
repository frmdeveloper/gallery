package id.frmdeveloper.gallery

import android.app.PendingIntent
import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Login Google Drive lewat DIALOG Google bawaan (Identity Authorization
 * API di Play services) — tanpa browser & tanpa server loopback 127.0.0.1:
 * user memilih akun di lembar Google, memberi izin Drive, aplikasi menerima
 * server auth code (offline access) yang ditukar jadi refresh token, persis
 * hasil akhir jalur browser.
 *
 * Syarat di Google Cloud project yang sama:
 *  - OAuth client ANDROID: package id.frmdeveloper.gallery + SHA-1 sertif
 *    penanda tangan aplikasi (kalau belum terdaftar, authorize() melempar
 *    ApiException DEVELOPER_ERROR).
 *  - OAuth client WEB: DriveAuth.webClientId/webClientSecret — client id-nya
 *    jadi serverClientId buat requestOfflineAccess, secret-nya buat tukar
 *    kode di endpoint token.
 *
 * Gagal di jalur ini (Play services tidak ada / client belum terdaftar /
 * user membatalkan) -> pemanggil jatuh ke login browser (DriveLogin).
 */
object DriveLoginGoogle {

    /** authorize() butuh UI Google tampil (pilih akun / persetujuan). */
    class PerluResolusi(val pendingIntent: PendingIntent) :
        Exception("Perlu pilihan akun/izin dari user")

    private fun permintaan(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(
                    Scope("https://www.googleapis.com/auth/drive"),
                    Scope("https://www.googleapis.com/auth/userinfo.email"),
                )
            )
            .requestOfflineAccess(DriveAuth.webClientId, true)
            .build()

    /**
     * Minta otorisasi ke Google. BLOCKING (Tasks.await) — panggil dari IO.
     * Bila Google perlu menampilkan UI, lempar [PerluResolusi]; setelah UI
     * selesai & user menyetujui, panggil fungsi ini sekali lagi buat
     * mengambil hasilnya (server auth code).
     */
    fun otorisasi(ctx: Context): AuthorizationResult {
        val hasil = try {
            Tasks.await(Identity.getAuthorizationClient(ctx).authorize(permintaan()))
        } catch (e: java.util.concurrent.ExecutionException) {
            throw (e.cause ?: e)
        }
        val pi = hasil.pendingIntent
        if (hasil.hasResolution() && pi != null) throw PerluResolusi(pi)
        return hasil
    }

    /** Tukar server auth code -> (refreshToken, email akun). Blocking, dari IO. */
    fun tukarKodeServer(code: String): Pair<String, String> {
        val body = listOf(
            "client_id" to DriveAuth.webClientId,
            "client_secret" to DriveAuth.webClientSecret,
            "code" to code,
            "grant_type" to "authorization_code",
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
