package id.frmdeveloper.gallery

/**
 * Identitas aplikasi OAuth buat LOGIN MANUAL Google Drive (DriveLogin).
 *
 * Client id + secret TIDAK ditanam di repo ini — dibaca dari BuildConfig,
 * yang diisi Gradle dari `local.properties` (lihat `local.properties.example`).
 * Caranya: buat OAuth client sendiri di Google Cloud Console, lalu isi
 * drive.clientId + drive.clientSecret di local.properties.
 *
 * Setiap pengguna login pakai akun Google-nya sendiri; refresh token hasil
 * login disimpan lokal di HP masing-masing (DriveStore), bukan di dalam APK.
 */
object DriveAuth {
    val clientId: String get() = BuildConfig.DRIVE_CLIENT_ID
    val clientSecret: String get() = BuildConfig.DRIVE_CLIENT_SECRET
    val webClientId: String get() = BuildConfig.DRIVE_WEB_CLIENT_ID
    val webClientSecret: String get() = BuildConfig.DRIVE_WEB_CLIENT_SECRET

    /** True kalau kredensial OAuth sudah diisi di local.properties. */
    val isConfigured: Boolean get() = clientId.isNotEmpty() && clientSecret.isNotEmpty()
}
