package id.frmdeveloper.gallery

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Layanan pemutar musik: ExoPlayer tinggal DI SINI (bukan di ViewModel),
 * jadi musik tetap jalan pas aplikasi ditinggal/dibersihkan dari Recents,
 * lengkap dengan NOTIFIKASI "sedang diputar" + kontrol layar kunci
 * (disediakan MediaSessionService + notifikasi bawaan media3).
 * UI aplikasi mengendalikan lewat MediaController (lihat GalleryViewModel).
 *
 * Tambahan: POPUP heads-up singkat tiap ganti lagu pas aplikasi lagi di
 * LATAR (notifikasi media bawaan memang tidak pernah muncul sbg popup —
 * itu desain Android, Spotify pun sama). Popup-nya dari kanal kepentingan
 * TINGGI terpisah, hilang sendiri setelah 5 detik.
 */
class LayananMusik : MediaSessionService() {

    private var sesi: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        DriveApi.init(this)
        buatKanalPopup()
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(buatFactoryStreaming(this)))
            .build()
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Popup cuma pas lagu beneran muter & aplikasi lagi di latar.
                // SYARATNYA playWhenReady, BUKAN isPlaying: pas transisi,
                // lagu baru hampir selalu masih buffering -> isPlaying
                // false sesaat -> popup dulu nyaris tidak pernah lolos.
                if (mediaItem != null && player.playWhenReady && !aplikasiDiDepan()) {
                    tampilkanPopup(mediaItem)
                }
            }
        })
        // Aktivitas sesi: notifikasi media diketuk -> aplikasi kebuka.
        // Tanpa ini notifikasi media3 tidak punya content intent bawaan
        // yg bisa diandalkan, ketukan pengguna tidak ngapa-ngapain.
        val niatBuka = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        sesi = MediaSession.Builder(this, player)
            .setSessionActivity(niatBuka)
            .build()
        // Ikon kecil notifikasi media = not balok kita sendiri; bawaan
        // media3 itu ikon play generik (beda sendiri dari aplikasi musik)
        val penyediaNotif = androidx.media3.session.DefaultMediaNotificationProvider(this)
        penyediaNotif.setSmallIcon(R.drawable.ic_notif_musik)
        setMediaNotificationProvider(penyediaNotif)
    }

    override fun onGetSession(controller: MediaSession.ControllerInfo): MediaSession? = sesi

    private fun buatKanalPopup() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(
                NotificationChannel(
                    KANAL_POPUP,
                    "Popup lagu diputar",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Popup singkat pas lagu berganti di latar belakang"
                }
            )
        }
    }

    private fun aplikasiDiDepan(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pid = android.os.Process.myPid()
        return am.runningAppProcesses?.any {
            it.pid == pid &&
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        } == true
    }

    private fun tampilkanPopup(item: MediaItem) {
        val md = item.mediaMetadata
        val judul = md.title?.toString()?.takeIf { it.isNotBlank() } ?: "Sedang diputar"
        val artis = md.artist?.toString()?.takeIf { it.isNotBlank() }
        val buka = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        // Cover lagu (byte yg dicap ViewModel ke metadata) jadi ikon
        // besar popup — tampil seperti kartu media aplikasi musik lain
        val bmpSampul = md.artworkData?.let { data ->
            runCatching {
                android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
            }.getOrNull()
        }
        val pembangun = NotificationCompat.Builder(this, KANAL_POPUP)
            .setSmallIcon(R.drawable.ic_notif_musik)
            .setContentTitle(judul)
            .setContentText(artis ?: "Galeri FRM")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        if (bmpSampul != null) pembangun.setLargeIcon(bmpSampul)
        val notif = pembangun
            .setContentIntent(buka)
            .setAutoCancel(true)
            .setTimeoutAfter(5_000)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
        runCatching {
            getSystemService(NotificationManager::class.java)?.notify(ID_POPUP, notif)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = sesi?.player
        // Aplikasi disapu dari Recents: berhenti HANYA kalau tidak lagi muter.
        // Kalau masih muter -> layanan (dan notifikasinya) tetap hidup.
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0 ||
            p.playbackState == Player.STATE_ENDED
        ) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        sesi?.player?.release()
        sesi?.release()
        sesi = null
        super.onDestroy()
    }

    companion object {
        private const val KANAL_POPUP = "popup_putar"
        private const val ID_POPUP = 7731
    }
}
