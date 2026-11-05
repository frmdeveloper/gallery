package id.frmdeveloper.gallery

import android.content.Context
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource

/**
 * Factory streaming bersama buat pemutar musik (dipakai LayananMusik):
 * item Drive berskema "gd://<id>" ditukar pas dibuka jadi URL unduh Drive
 * + header Authorization dgn token SEGAR (diambil per-buka, jadi antrean
 * panjang tidak putus pas token kedaluwarsa di tengah). Item lokal
 * (content://) diteruskan apa adanya.
 */
fun buatFactoryStreaming(context: Context): DataSource.Factory {
    return ResolvingDataSource.Factory(DefaultDataSource.Factory(context)) { spec ->
        val u = spec.uri
        if (u.scheme == "gd") {
            // Token milik akun pemilik lagu: email tertanam di URI
            // (gd://<id>?a=<email>); cadangan lewat registrasi id berkas
            val email = u.getQueryParameter("a")
            val token = if (!email.isNullOrBlank()) {
                DriveApi.accessTokenUntukEmail(email)
            } else {
                DriveApi.accessTokenUntukFile(u.host ?: "")
            }
            spec.buildUpon()
                .setUri("https://www.googleapis.com/drive/v3/files/${u.host}?alt=media")
                .setHttpRequestHeaders(
                    if (token != null) mapOf("Authorization" to "Bearer $token") else emptyMap()
                )
                .build()
        } else {
            spec
        }
    }
}
