package id.frmdeveloper.gallery

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.google.android.gms.auth.api.identity.AuthorizationResult

class GalleryViewModel(app: Application) : AndroidViewModel(app) {

    var media: List<MediaItem> by mutableStateOf(emptyList())
        private set
    var localAlbums: List<Album> by mutableStateOf(emptyList())
        private set
    var driveAlbums: List<Album> by mutableStateOf(emptyList())
        private set
    var driveItems: List<MediaItem> by mutableStateOf(emptyList())
        private set
    var favorites: Set<String> by mutableStateOf(FavoritesStore.load(app))
        private set
    var driveTreeUri: Uri? by mutableStateOf(DriveStore.loadTreeUri(app))
        private set
    var driveApiOn: Boolean by mutableStateOf(false)
        private set
    var driveError: String? by mutableStateOf(null)
        private set
    /** Semua akun Drive yg login (multi-akun); kosong = belum ada login. */
    var daftarAkunDrive: List<AkunDrive> by mutableStateOf(DriveStore.loadDaftarAkun(app))
        private set
    /** Email akun pertama (buat tampilan ringkas); "" bila belum login. */
    var accountEmail: String by mutableStateOf("")
        private set
    var pakaiAkunLain: Boolean by mutableStateOf(false)
        private set

    /** Samakan state turunan tiap daftar akun berubah. */
    private fun sinkronAkun() {
        driveApiOn = daftarAkunDrive.isNotEmpty()
        accountEmail = daftarAkunDrive.firstOrNull()?.email ?: ""
        pakaiAkunLain = daftarAkunDrive.isNotEmpty()
        DriveStore.saveApiEnabled(getApplication(), driveApiOn)
    }

    /** Akun pemilik sebuah item Drive (dari cap akunDrive-nya). */
    private fun akunDari(item: MediaItem): AkunDrive? =
        daftarAkunDrive.firstOrNull { it.email == item.akunDrive }
    var loginMenunggu: Boolean by mutableStateOf(false)
        private set
    private var sesiLogin: DriveLogin.Sesi? = null
    /** Jembatan ke Activity buat menampilkan dialog izin Google (GMS). */
    var peluncurResolusiDrive: ((android.app.PendingIntent) -> Unit)? = null
    private var lanjutResolusi: ((Boolean) -> Unit)? = null
    private var dataResolusi: android.content.Intent? = null

    fun selesaikanResolusiDrive(ok: Boolean, data: android.content.Intent? = null) {
        dataResolusi = data
        val l = lanjutResolusi
        lanjutResolusi = null
        l?.invoke(ok)
    }

    init {
        DriveApi.init(app)
        // Daftar akun dimuat DriveStore (termasuk migrasi login tunggal
        // lama jadi akun pertama); state turunan disamakan dari situ.
        sinkronAkun()
        hubungkanKontroler()
        // Polling posisi lagu buat seekbar pemutar (murah: 2x per detik,
        // cuma baca field player, tidak ada IO).
        viewModelScope.launch {
            while (true) {
                kontroler?.let { p ->
                    posisiLagu = p.currentPosition.coerceAtLeast(0)
                    val d = p.duration
                    durasiLagu = if (d > 0) d else (laguDimainkan?.durationMs ?: 0L)
                }
                delay(500)
            }
        }
    }
    var driveName: String by mutableStateOf("")
        private set
    var loading: Boolean by mutableStateOf(false)
        private set
    var driveLoading: Boolean by mutableStateOf(false)
        private set
    var uploading: Boolean by mutableStateOf(false)
        private set
    var message: String? by mutableStateOf(null)

    fun consumeMessage() { message = null }

    /** Tampilkan pesan snackbar dari lapisan UI (mis. jalur hapus). */
    fun beriTahu(teks: String) {
        message = teks
    }

    fun reloadLocal() {
        viewModelScope.launch {
            loading = true
            val ctx = getApplication<Application>()
            val items = withContext(Dispatchers.IO) { MediaRepo.loadMedia(ctx) }
            media = items
            localAlbums = withContext(Dispatchers.IO) { MediaRepo.loadAlbums(items) }
            loading = false
        }
    }

    fun reloadDrive() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            if (driveApiOn) {
                driveLoading = true
                driveError = null
                // Pindai SEMUA akun, hasilnya digabung (error satu akun
                // tidak menggagalkan akun lain)
                val semuaAlbum = mutableListOf<Album>()
                val semuaItem = mutableListOf<MediaItem>()
                val galat = mutableListOf<String>()
                for (akun in daftarAkunDrive) {
                    val hasil = withContext(Dispatchers.IO) {
                        runCatching { DriveApi.scan(akun) }
                    }
                    hasil.onSuccess { scan ->
                        semuaAlbum += scan.albums
                        semuaItem += scan.allItems
                    }.onFailure { e ->
                        galat += "${akun.email}: ${e.message ?: "gagal"}"
                    }
                }
                driveAlbums = semuaAlbum
                driveItems = semuaItem.sortedByDescending { it.dateMillis }
                driveName = if (daftarAkunDrive.size > 1) {
                    "Google Drive (${daftarAkunDrive.size} akun)"
                } else {
                    "Google Drive (API)"
                }
                if (galat.isNotEmpty() && semuaItem.isEmpty()) {
                    driveError = galat.first()
                } else if (galat.isNotEmpty()) {
                    driveError = "Sebagian akun gagal dimuat: ${galat.first()}"
                }
                if (semuaItem.isNotEmpty() || galat.isEmpty()) reloadAudio()
                driveLoading = false
            } else {
                val tree = driveTreeUri ?: return@launch
                driveLoading = true
                driveName = withContext(Dispatchers.IO) { DriveRepo.displayName(ctx, tree) }
                val scan = withContext(Dispatchers.IO) { runCatching { DriveRepo.scan(ctx, tree) }.getOrNull() }
                if (scan != null) {
                    driveAlbums = scan.albums
                    driveItems = scan.allItems
                }
                driveLoading = false
            }
        }
    }

    /**
     * Login Google Drive (tidak ada akun tertanam sejak v1.15 — setiap
     * pengguna login pakai akun Google-nya sendiri). Jalur utama: DIALOG
     * Google bawaan (DriveLoginGoogle, tanpa browser). Jalur dialog gagal
     * (Play services tak ada / client Android belum terdaftar di Cloud
     * Console / dibatalkan) -> jatuh ke login browser loopback (DriveLogin).
     */
    fun loginAkunLain() {
        if (loginMenunggu) return
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            loginMenunggu = true
            message = "Menyiapkan login Google…"
            // Dialog Google selalu memakai akun yg sudah nempel di HP —
            // buat NAMBAH akun lain dia malah login ke akun yg sama, jadi
            // tambah akun langsung lewat browser (bisa pilih/ketik akun).
            val hasilDialog = if (daftarAkunDrive.isEmpty()) {
                runCatching { prosesLoginDialog(ctx) }
            } else {
                null
            }
            val hasil = if (hasilDialog != null && hasilDialog.isSuccess) {
                hasilDialog
            } else {
                message = if (hasilDialog != null) {
                    "Login dialog gagal (${ringkasSebab(hasilDialog.exceptionOrNull())}) — membuka login lewat browser…"
                } else {
                    "Menambah akun lewat browser — pilih akun yg mau ditambahkan…"
                }
                runCatching { prosesLoginBrowser(ctx) }
            }
            sesiLogin = null
            loginMenunggu = false
            hasil.onSuccess { akunBaru ->
                // Login = MENAMBAH akun (multi-akun); email yg sama berarti
                // token akun itu diperbarui, bukan akun dobel
                daftarAkunDrive = DriveStore.tambahAkun(ctx, akunBaru)
                DriveApi.resetToken(akunBaru.email)
                sinkronAkun()
                message = if (akunBaru.email.isNotBlank()) "Login berhasil: ${akunBaru.email}" else "Login akun lain berhasil"
                reloadDrive()
            }.onFailure { e ->
                message = "Login gagal: ${e.message}"
            }
        }
    }

    /** Dialog Google: otorisasi -> tampilkan UI Google SETIAP kali diminta
     * (bisa berurutan: pilih akun lalu persetujuan izin) -> tukar kode. */
    private suspend fun prosesLoginDialog(ctx: android.content.Context): AkunDrive {
        var hasilAwal: AuthorizationResult? = null
        var putaran = 0
        while (hasilAwal == null) {
            val coba = withContext(Dispatchers.IO) {
                runCatching { DriveLoginGoogle.otorisasi(ctx) }
            }
            hasilAwal = coba.getOrElse { e ->
                if (e is DriveLoginGoogle.PerluResolusi && putaran < 4) {
                    putaran++
                    if (!tungguResolusi(e.pendingIntent)) error("Login dibatalkan")
                    // Hasil otorisasi biasanya IKUT di intent hasil resolusi;
                    // authorize() ulang malah memicu lembar Google dari awal
                    // lagi (kejadian v1.51: muter-muter). Ambil dari intent
                    // dulu; authorize ulang cuma bila intent tak membawa hasil.
                    val data = dataResolusi
                    dataResolusi = null
                    val dariIntent = data?.let { DriveLoginGoogle.hasilDariIntent(ctx, it) }
                    if (dariIntent != null && !dariIntent.hasResolution()) dariIntent else null
                } else {
                    throw e
                }
            }
        }
        val kode = hasilAwal.serverAuthCode ?: error("Google tidak memberi kode login")
        // Token dari client WEB — penanda "web" wajib benar, refresh token
        // cuma bisa ditebus client penerbitnya.
        return withContext(Dispatchers.IO) { DriveLoginGoogle.tukarKodeServer(kode) }
            .let { (refresh, email) -> AkunDrive(email, refresh, "web") }
    }

    /** Tahan coroutine sampai Activity melaporkan hasil dialog izin. */
    private suspend fun tungguResolusi(pi: android.app.PendingIntent): Boolean {
        val peluncur = peluncurResolusiDrive ?: error("Peluncur dialog tidak siap")
        return suspendCancellableCoroutine { cont ->
            lanjutResolusi = { ok -> if (cont.isActive) cont.resume(ok) }
            cont.invokeOnCancellation { lanjutResolusi = null }
            peluncur(pi)
        }
    }

    /** Login cadangan: browser + server loopback 127.0.0.1 (DriveLogin). */
    private suspend fun prosesLoginBrowser(ctx: android.content.Context): AkunDrive =
        withContext(Dispatchers.IO) {
            val sesi = DriveLogin.mulai()
            sesiLogin = sesi
            withContext(Dispatchers.Main) {
                runCatching {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse(sesi.urlAuth),
                    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intent)
                }
            }
            val kode = DriveLogin.tungguKode(sesi)
            DriveLogin.tukarKode(sesi, kode)
                .let { (refresh, email) -> AkunDrive(email, refresh, "desktop") }
        }

    fun batalLogin() {
        selesaikanResolusiDrive(false)
        sesiLogin?.batalkan()
        sesiLogin = null
    }

    /** Keluarkan SATU akun Drive; akun lain tetap login & isinya tampil. */
    fun keluarAkunDrive(email: String) {
        val ctx = getApplication<Application>()
        daftarAkunDrive = DriveStore.hapusAkun(ctx, email)
        DriveApi.resetToken(email)
        sinkronAkun()
        if (daftarAkunDrive.isEmpty()) {
            driveAlbums = emptyList()
            driveItems = emptyList()
            driveName = ""
            driveError = null
        } else {
            // Buang isi akun yg keluar dari tampilan, lalu segarkan sisanya
            driveAlbums = driveAlbums.filter { it.akun != email }
            driveItems = driveItems.filter { it.akunDrive != email }
        }
        message = "Akun $email dikeluarkan"
        reloadDrive()
        reloadAudio()
    }

    /** Hapus item Drive API (masuk sampah Drive), lalu segarkan. */
    fun deleteDriveItems(items: List<MediaItem>, onDone: () -> Unit = {}) {
        val targets = items.filter { it.driveId.isNotBlank() }
        if (targets.isEmpty()) {
            onDone()
            return
        }
        viewModelScope.launch {
            var ok = 0
            withContext(Dispatchers.IO) {
                for (item in targets) if (DriveApi.trash(item.driveId, akunDari(item))) ok++
            }
            message = "Dihapus dari Drive: $ok dari ${targets.size} item"
            reloadDrive()
            onDone()
        }
    }

    // ---- SAMPAH (lokal + Drive) ----
    var sampahLokal: List<MediaItem> by mutableStateOf(emptyList())
        private set
    var sampahDrive: List<MediaItem> by mutableStateOf(emptyList())
        private set
    var sampahLoading: Boolean by mutableStateOf(false)
        private set

    fun muatSampah() {
        viewModelScope.launch {
            sampahLoading = true
            sampahLokal = withContext(Dispatchers.IO) { MediaRepo.loadSampah(getApplication()) }
            sampahDrive = if (driveApiOn) {
                withContext(Dispatchers.IO) {
                    daftarAkunDrive.flatMap { akun ->
                        runCatching { DriveApi.daftarSampah(akun) }.getOrDefault(emptyList())
                    }
                }
            } else {
                emptyList()
            }
            sampahLoading = false
        }
    }

    fun pulihkanDriveItems(items: List<MediaItem>) {
        val targets = items.filter { it.driveId.isNotBlank() }
        if (targets.isEmpty()) return
        viewModelScope.launch {
            var ok = 0
            withContext(Dispatchers.IO) {
                for (item in targets) if (DriveApi.pulihkan(item.driveId, akunDari(item))) ok++
            }
            message = "Dipulihkan dari sampah Drive: $ok dari ${targets.size} item"
            reloadDrive()
            muatSampah()
        }
    }

    fun hapusPermanenDriveItems(items: List<MediaItem>) {
        val targets = items.filter { it.driveId.isNotBlank() }
        if (targets.isEmpty()) return
        viewModelScope.launch {
            var ok = 0
            withContext(Dispatchers.IO) {
                for (item in targets) if (DriveApi.hapusPermanen(item.driveId, akunDari(item))) ok++
            }
            message = "Dihapus permanen dari Drive: $ok dari ${targets.size} item"
            muatSampah()
        }
    }

    // ---- MUSIK (tab Audio) ----
    var laguDaftar: List<AudioItem> by mutableStateOf(emptyList())
        private set
    var laguDimainkan: AudioItem? by mutableStateOf(null)
        private set
    var muterLagu: Boolean by mutableStateOf(false)
        private set
    var posisiLagu: Long by mutableLongStateOf(0L)
        private set
    var durasiLagu: Long by mutableLongStateOf(0L)
        private set
    var acakLagu: Boolean by mutableStateOf(false)
        private set
    /** 0 = mati, 1 = ulang semua, 2 = ulang satu */
    var ulangLagu: Int by mutableIntStateOf(0)
        private set
    private var antreanLagu: List<AudioItem> = emptyList()
    // Player ASLI tinggal di LayananMusik (MediaSessionService) biar musik
    // jalan di latar + ada notifikasi; di sini cuma MediaController-nya.
    private var kontrolerFuture: ListenableFuture<MediaController>? = null
    private var kontroler: MediaController? = null
    private var putarTertunda: Triple<AudioItem, List<AudioItem>, Long>? = null

    var audioFolders: List<AudioFolder> by mutableStateOf(emptyList())
        private set

    /** True selama reloadAudio jalan; tab Audio nampilin loading, bukan
     * tulisan "tidak ada audio", selama muatan pertama belum kelar. */
    var audioSedangDimuat by mutableStateOf(false)
        private set

    fun reloadAudio() {
        audioSedangDimuat = true
        viewModelScope.launch {
          try {
            val lokal = withContext(Dispatchers.IO) { MediaRepo.loadAudio(getApplication()) }
            // Audio Drive ikut sinkron bila lagi login (gagal = diam, lokal tetap tampil)
            val drive = if (driveApiOn) {
                withContext(Dispatchers.IO) {
                    daftarAkunDrive.flatMap { akun ->
                        runCatching { DriveApi.daftarAudio(akun) }.getOrDefault(emptyList())
                    }
                }
            } else {
                emptyList()
            }
            laguDaftar = lokal + drive
            // Susun ala galeri: "Semua audio" (gabungan) paling atas, lalu
            // folder lokal A-Z, lalu folder Drive A-Z
            audioFolders = buildList {
                if (laguDaftar.isNotEmpty()) {
                    add(AudioFolder(id = "semua", name = "Semua audio", items = laguDaftar))
                }
                lokal.groupBy { it.folder }
                    .entries
                    .sortedBy { it.key.lowercase() }
                    .forEach { (nama, items) ->
                        add(AudioFolder(id = "folder:$nama", name = nama, items = items))
                    }
                drive.groupBy { it.folder }
                    .entries
                    .sortedBy { it.key.lowercase() }
                    .forEach { (nama, items) ->
                        add(
                            AudioFolder(
                                id = "drivefolder:$nama",
                                name = nama,
                                items = items,
                                sumber = "drive",
                            )
                        )
                    }
            }
          } finally {
            audioSedangDimuat = false
          }
        }
    }

    private val listenerLagu = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            muterLagu = isPlaying
        }
        override fun onMediaItemTransition(mediaItem: ExoMediaItem?, reason: Int) {
            val k = kontroler ?: return
            antreanLagu.getOrNull(k.currentMediaItemIndex)?.let { laguDimainkan = it }
        }
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) muterLagu = false
        }
        override fun onShuffleModeEnabledChanged(enabled: Boolean) {
            acakLagu = enabled
        }
        override fun onRepeatModeChanged(mode: Int) {
            ulangLagu = when (mode) {
                Player.REPEAT_MODE_ALL -> 1
                Player.REPEAT_MODE_ONE -> 2
                else -> 0
            }
        }
    }

    /** Sambungkan MediaController ke LayananMusik (player latar + notifikasi). */
    private fun hubungkanKontroler() {
        val app = getApplication<Application>()
        val token = SessionToken(app, ComponentName(app, LayananMusik::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        kontrolerFuture = future
        future.addListener({
            runCatching {
                val k = future.get()
                kontroler = k
                k.addListener(listenerLagu)
                acakLagu = k.shuffleModeEnabled
                muterLagu = k.isPlaying
                if (k.mediaItemCount > 0) {
                    antreanLagu.getOrNull(k.currentMediaItemIndex)?.let { laguDimainkan = it }
                }
                putarTertunda?.let { (item, antrean, pos) -> putarLagu(item, antrean, pos) }
                putarTertunda = null
            }
        }, ContextCompat.getMainExecutor(app))
    }

    fun putarLagu(item: AudioItem, antrean: List<AudioItem>, posisiAwalMs: Long = 0L) {
        antreanLagu = antrean.ifEmpty { listOf(item) }
        val idx = antreanLagu.indexOfFirst { it.key == item.key }.coerceAtLeast(0)
        laguDimainkan = antreanLagu[idx]
        val p = kontroler
        if (p == null) {
            // Controller belum tersambung (baru buka aplikasi) — tunda bentar
            putarTertunda = Triple(item, antreanLagu, posisiAwalMs)
            return
        }
        p.setMediaItems(antreanLagu.map { a ->
            ExoMediaItem.Builder()
                .setUri(a.uri)
                .setMediaId(a.key)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(a.title)
                        .setArtist(a.artist)
                        .setAlbumTitle(a.album)
                        .setArtworkUri(if (a.artUri != Uri.EMPTY) a.artUri else null)
                        .build()
                )
                .build()
        })
        p.seekTo(idx, posisiAwalMs)
        p.prepare()
        p.play()
        capArtworkAntrean(p, antreanLagu, idx)
    }

    /**
     * Tanam artwork BYTE (hasil ekstrak langsung dari berkas lagu) ke
     * metadata tiap item antrean — artworkUri album MediaStore sering
     * KOSONG isinya buat album yg gambarnya tidak pernah diekstrak
     * MediaStore, makanya kartu media/notifikasi sistem tampil polos.
     * Mulai dari lagu yg sedang diputar biar cover-nya muncul duluan;
     * berhenti diam-diam kalau antrean sudah diganti pengguna.
     */
    private fun capArtworkAntrean(
        p: MediaController,
        antreanSnapshot: List<AudioItem>,
        idxAwal: Int,
    ) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val urutan = (idxAwal until antreanSnapshot.size) + (0 until idxAwal)
            for (i in urutan) {
                val a = antreanSnapshot[i]
                if (a.uri.scheme != "content") continue
                val bytes = withContext(Dispatchers.IO) {
                    ambilKecil(ctx, a)
                } ?: continue
                if (p.mediaItemCount != antreanSnapshot.size) break
                val cur = runCatching { p.getMediaItemAt(i) }.getOrNull() ?: continue
                if (cur.mediaId != a.key) continue
                val mdBaru = cur.mediaMetadata.buildUpon()
                    .setArtworkData(bytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    .build()
                runCatching {
                    p.replaceMediaItem(i, cur.buildUpon().setMediaMetadata(mdBaru).build())
                }
            }
        }
    }

    // ---- Ganti cover lagu (ditulis ke tag berkasnya, mis. ID3/APIC MP3) ----
    /** Lagu yg lagi diminta ganti cover; AppRoot yg ngurus izin + pemilih foto. */
    var laguGantiCover by mutableStateOf<AudioItem?>(null)
        private set

    /** Dipanggil dari cover pemutar. Lagu Drive ditolak (berkasnya di awan). */
    fun mulaiGantiCover(lagu: AudioItem) {
        if (lagu.uri.scheme != "content" || lagu.path.isBlank()) {
            beriTahu("Cover cuma bisa diganti buat lagu lokal di HP")
            return
        }
        laguGantiCover = lagu
    }

    fun batalGantiCover() {
        laguGantiCover = null
    }

    /** Foto terpilih yg lagi nunggu DIPOTONG PERSEGI sebelum ditulis. */
    var fotoPotong by mutableStateOf<Pair<AudioItem, Uri>?>(null)
        private set

    /**
     * Naik tiap cover selesai ditulis + dipindai ulang MediaStore: kunci
     * cache gambar cover ikut berubah, tampilan maksa muat ulang cover baru.
     */
    var versiCover by mutableIntStateOf(0)
        private set

    fun mulaiPotong(lagu: AudioItem, uriFoto: Uri) {
        fotoPotong = lagu to uriFoto
    }

    fun batalPotong() {
        fotoPotong = null
        laguGantiCover = null
    }

    /**
     * Tulis [uriFoto] sbg cover ke dalam tag berkas lagu. Strategi anti-gagal
     * scoped storage: berkas DISALIN dulu ke cache aplikasi, disunting di
     * sana (folder lagu asli tidak bisa ditulisi langsung), lalu ditulis
     * BALIK lewat URI-nya (izin tulis nempel di URI). Pola jaudiotagger-nya
     * sendiri sudah dites di JVM (tulis di tempat, tanpa berkas sementara),
     * dan setImageFromData SENGAJA tidak dipakai (jalur ImageHandler-nya
     * rawan di Android; ukuran gambar tidak disimpan di frame APIC kok).
     */
    fun tulisCoverLagu(lagu: AudioItem, jpeg: ByteArray) {
        laguGantiCover = null
        fotoPotong = null
        val app = getApplication<Application>()
        viewModelScope.launch {
            var pesanGagal: String? = null
            val sukses = withContext(Dispatchers.IO) {
                runCatching {

                    // Salin ke cache (ekstensi ASLI dipertahankan biar
                    // jaudiotagger memilih parser yg benar), sunting di sana
                    val ext = lagu.path.substringAfterLast('.', "mp3")
                    val salinan = java.io.File(app.cacheDir, "sunting-cover.$ext")
                    app.contentResolver.openInputStream(lagu.uri).use { masuk ->
                        requireNotNull(masuk) { "berkas lagu tidak terbaca" }
                        salinan.outputStream().use { keluar -> masuk.copyTo(keluar) }
                    }
                    val af = org.jaudiotagger.audio.AudioFileIO.read(salinan)
                    val tag = af.tagOrCreateAndSetDefault
                    tag.deleteField(org.jaudiotagger.tag.FieldKey.COVER_ART)
                    val karya = org.jaudiotagger.tag.images.ArtworkFactory.getNew()
                    karya.binaryData = jpeg
                    karya.mimeType = "image/jpeg"
                    karya.pictureType = 3 // 3 = sampul depan (front cover)
                    tag.setField(karya)
                    af.commit()

                    // Tulis balik lewat URI (bukan path mentah)
                    app.contentResolver.openOutputStream(lagu.uri, "w").use { keluar ->
                        requireNotNull(keluar) { "saluran tulis URI tertutup" }
                        salinan.inputStream().use { masuk -> masuk.copyTo(keluar) }
                    }
                    salinan.delete()
                    true
                }.onFailure { e ->
                    pesanGagal = (e.message ?: e.javaClass.simpleName).take(120)
                }.isSuccess
            }
            if (sukses) {
                // Pindai ulang MediaStore dan TUNGGU sampai beneran selesai,
                // baru bersihkan cache + naikkan versi cover. Versi lama
                // keburu-buru: gambar dimuat ulang pas MediaStore belum
                // kelar mindai, jadi yg tampil tetap sampul lama/gradasi.
                android.media.MediaScannerConnection.scanFile(
                    app, arrayOf(lagu.path), null,
                ) { _, _ ->
                    viewModelScope.launch {
                        runCatching {
                            val ml = coil.Coil.imageLoader(app)
                            ml.memoryCache?.clear()
                            ml.diskCache?.clear()
                        }
                        versiCover++
                        reloadAudio()
                        beriTahu("Cover diganti & tersimpan di berkas lagunya")
                    }
                }
            } else {
                beriTahu("Gagal menulis cover: ${pesanGagal ?: "tidak diketahui"}")
            }
        }
    }

    /** Tombol silang mini-player: hentikan putaran + bersihkan player. */
    fun tutupPemutar() {
        kontroler?.let { p ->
            p.stop()
            p.clearMediaItems()
        }
        antreanLagu = emptyList()
        laguDimainkan = null
        muterLagu = false
        posisiLagu = 0
        durasiLagu = 0
    }

    // === Serah-terima mode musik video <-> musik latar ===
    // Pas user keluar aplikasi di mode musik video, putaran video dioper
    // ke layanan musik latar (antrean = SEMUA video yg lagi kebuka) biar
    // jadi musik latar beneran: ada notif media + prev/next jalan.
    /** Video yg lagi dioper ke latar; null = nggak ada operan. */
    var videoDiLatar: MediaItem? by mutableStateOf(null)
        private set

    fun serahkanVideoKeLatar(videos: List<MediaItem>, mulai: MediaItem, posisiMs: Long) {
        if (videos.isEmpty() || videoDiLatar != null) return
        val antrean = videos.map { v ->
            AudioItem(
                key = v.key, uri = v.uri, title = v.name, artist = "Video",
                album = "", durationMs = v.durationMs, artUri = Uri.EMPTY,
                dateMillis = v.dateMillis, akunDrive = v.akunDrive,
            )
        }
        val target = antrean.firstOrNull { it.key == mulai.key } ?: antrean.first()
        videoDiLatar = mulai
        putarLagu(target, antrean, posisiMs)
    }

    /** Ambil alih dari latar: balikin (kunci video yg lagi jalan, posisi)
     * lalu matikan layanan. null kalau nggak ada operan. */
    fun ambilAlihDariLatar(): Pair<String, Long>? {
        val v = videoDiLatar ?: return null
        val hasil = (laguDimainkan?.key ?: v.key) to posisiLagu
        tutupPemutar()
        videoDiLatar = null
        return hasil
    }

    fun toggleLagu() {
        val p = kontroler ?: return
        if (p.isPlaying) {
            p.pause()
        } else {
            if (p.playbackState == Player.STATE_ENDED) p.seekTo(0)
            p.play()
        }
    }

    fun laguBerikut() {
        kontroler?.seekToNextMediaItem()
    }

    fun laguSebelum() {
        val p = kontroler ?: return
        if (p.currentPosition > 3_000) p.seekTo(0) else p.seekToPreviousMediaItem()
    }

    fun cariLaguKe(ms: Long) {
        kontroler?.seekTo(ms)
    }

    fun toggleAcakLagu() {
        val p = kontroler ?: return
        acakLagu = !acakLagu
        p.shuffleModeEnabled = acakLagu
    }

    fun siklusUlangLagu() {
        val p = kontroler ?: return
        ulangLagu = (ulangLagu + 1) % 3
        p.repeatMode = when (ulangLagu) {
            1 -> Player.REPEAT_MODE_ALL
            2 -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    override fun onCleared() {
        kontrolerFuture?.let { MediaController.releaseFuture(it) }
        kontrolerFuture = null
        kontroler = null
        super.onCleared()
    }

    fun connectDrive(treeUri: Uri) {
        val ctx = getApplication<Application>()
        DriveRepo.takePermission(ctx, treeUri)
        DriveStore.saveTreeUri(ctx, treeUri)
        driveTreeUri = treeUri
        message = "Google Drive terhubung"
        reloadDrive()
    }

    fun disconnectDrive() {
        val ctx = getApplication<Application>()
        driveTreeUri?.let { DriveRepo.releasePermission(ctx, it) }
        DriveStore.saveTreeUri(ctx, null)
        driveTreeUri = null
        driveAlbums = emptyList()
        driveItems = emptyList()
        driveName = ""
        message = "Google Drive diputuskan"
    }

    fun toggleFavorite(item: MediaItem) {
        val ctx = getApplication<Application>()
        favorites = if (item.key in favorites) favorites - item.key else favorites + item.key
        FavoritesStore.save(ctx, favorites)
    }

    /** Bintang favorit buat lagu (pemutar gaya iOS) — set favorit yg sama. */
    fun toggleFavoritLagu(lagu: AudioItem) {
        val ctx = getApplication<Application>()
        favorites = if (lagu.key in favorites) favorites - lagu.key else favorites + lagu.key
        FavoritesStore.save(ctx, favorites)
    }

    fun isFavorite(item: MediaItem) = item.key in favorites

    fun favoritesAlbum(): Album {
        val all = (media + driveItems).filter { it.key in favorites }
        return Album(id = "favorites", name = "Favorit", kind = AlbumKind.FAVORITES, items = all)
    }

    fun uploadToDrive(items: List<MediaItem>) {
        val locals = items.filter { !it.isDrive }
        if (locals.isEmpty()) {
            message = "Tidak ada file lokal untuk di-upload"
            return
        }
        if (driveApiOn) {
            viewModelScope.launch {
                uploading = true
                val ctx = getApplication<Application>()
                var ok = 0
                withContext(Dispatchers.IO) {
                    val akunTujuan = daftarAkunDrive.firstOrNull()
                    for (item in locals) if (akunTujuan != null && DriveApi.upload(ctx, item, akunTujuan)) ok++
                }
                uploading = false
                message = "Upload selesai: $ok berhasil, ${locals.size - ok} gagal"
                reloadDrive()
            }
            return
        }
        val tree = driveTreeUri
        if (tree == null) {
            message = "Hubungkan Google Drive dulu lewat tab Menu"
            return
        }
        viewModelScope.launch {
            uploading = true
            val ctx = getApplication<Application>()
            val (ok, fail) = withContext(Dispatchers.IO) { DriveRepo.upload(ctx, tree, locals) }
            uploading = false
            message = "Upload selesai: $ok berhasil, $fail gagal"
            reloadDrive()
        }
    }
}

private fun ringkasSebab(e: Throwable?): String {
    var cur = e ?: return "tidak diketahui"
    while (true) {
        if (cur is com.google.android.gms.common.api.ApiException) {
            return "Google API ${cur.statusCode}: ${cur.message}"
        }
        cur = cur.cause ?: return cur.message ?: cur.javaClass.simpleName
    }
}
