package id.frmdeveloper.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var vmGaleri: GalleryViewModel? = null
    private val peluncurResolusiDrive = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { hasil ->
        vmGaleri?.selesaikanResolusiDrive(
            hasil.resultCode == android.app.Activity.RESULT_OK,
            hasil.data,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DriveApi.init(this)
        // Jembatan login Drive dialog Google: ViewModel minta Activity
        // menampilkan lembar pilih akun/izin (PendingIntent Identity API)
        val vmUtama = androidx.lifecycle.ViewModelProvider(this)[GalleryViewModel::class.java]
        vmGaleri = vmUtama
        vmUtama.peluncurResolusiDrive = { pi ->
            runCatching {
                peluncurResolusiDrive.launch(
                    androidx.activity.result.IntentSenderRequest.Builder(pi.intentSender).build()
                )
            }
        }
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components {
                    // Fetcher cover lagu WAJIB di sini: loader singleton
                    // Coil diganti total di onCreate ini, jadi pendaftaran
                    // lewat GaleriApp (ImageLoaderFactory) kalah — tanpa
                    // baris ini model CoverLagu tidak punya fetcher sama
                    // sekali dan semua cover lagu error -> gradasi.
                    add(FetcherCoverLagu.Factory(applicationContext))
                    add(VideoFrameDecoder.Factory())
                }
                .crossfade(false)
                // Item Drive API (thumbnailLink & alt=media) butuh header
                // Authorization — interceptor ini nempel token cuma buat
                // host Google, request lain lewat apa adanya.
                .okHttpClient {
                    okhttp3.OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val req = chain.request()
                            val host = req.url.host
                            val bertoken = host.endsWith("googleusercontent.com") ||
                                host == "www.googleapis.com"
                            // Buat unduhan berkas (…/files/<id>) pakai token
                            // akun PEMILIK berkas itu; thumbnail (host
                            // googleusercontent, tanpa id) pakai akun utama
                            val segmen = req.url.pathSegments
                            val idxFiles = segmen.indexOf("files")
                            val idBerkas = if (host == "www.googleapis.com" &&
                                idxFiles >= 0 && idxFiles + 1 < segmen.size
                            ) {
                                segmen[idxFiles + 1]
                            } else {
                                null
                            }
                            val token = when {
                                !bertoken -> null
                                idBerkas != null -> DriveApi.accessTokenUntukFile(idBerkas)
                                else -> DriveApi.accessTokenBlocking()
                            }
                            if (token != null) {
                                chain.proceed(
                                    req.newBuilder().header("Authorization", "Bearer $token").build()
                                )
                            } else {
                                chain.proceed(req)
                            }
                        }
                        .build()
                }
                .build()
        )
        setContent {
            GaleriFRMTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize()) {
                        AppRoot()
                        LayarSplash()
                    }
                }
            }
        }
    }
}

/** Bangun MediaItem dari URI milik aplikasi lain (intent VIEW). */
private fun buatItemDariUri(context: android.content.Context, uri: Uri): MediaItem {
    val cr = context.contentResolver
    var nama = "Media"
    var ukuran = 0L
    var lebar = 0
    var tinggi = 0
    var durasi = 0L
    runCatching {
        cr.query(
            uri,
            arrayOf(android.provider.OpenableColumns.DISPLAY_NAME, android.provider.OpenableColumns.SIZE),
            null, null, null,
        )?.use { c ->
            if (c.moveToFirst()) {
                nama = c.getString(0)?.ifBlank { nama } ?: nama
                ukuran = runCatching { c.getLong(1) }.getOrDefault(0L)
            }
        }
    }
    val mime = runCatching { cr.getType(uri) }.getOrNull() ?: ""
    val video = mime.startsWith("video/")
    runCatching {
        if (video) {
            val mmr = android.media.MediaMetadataRetriever()
            try {
                mmr.setDataSource(context, uri)
                lebar = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                tinggi = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                durasi = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            } finally {
                mmr.release()
            }
        } else {
            cr.openInputStream(uri)?.use { input ->
                val opsi = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                android.graphics.BitmapFactory.decodeStream(input, null, opsi)
                lebar = opsi.outWidth
                tinggi = opsi.outHeight
            }
        }
    }
    return MediaItem(
        key = uri.toString(),
        uri = uri,
        name = nama,
        mime = mime.ifBlank { if (video) "video/*" else "image/*" },
        size = ukuran,
        dateMillis = 0L,
        isVideo = video,
        durationMs = durasi,
        width = lebar,
        height = tinggi,
    )
}

private sealed interface Screen {
    data object Timeline : Screen
    data object Albums : Screen
    data object MenuTab : Screen
    data object Audio : Screen
    data class AudioFolder(val folderId: String) : Screen
    data object Sampah : Screen
    data class AlbumDetail(val album: Album) : Screen
    data class Viewer(val items: List<MediaItem>, val index: Int, val from: Screen) : Screen
}

/** Kedalaman navigasi buat arah geser transisi: tab = 0, detail = 1, viewer = 2. */
private fun kedalamanLayar(s: Screen): Int = when (s) {
    is Screen.Timeline, is Screen.Albums, is Screen.Audio, is Screen.MenuTab -> 0
    is Screen.Viewer -> 2
    else -> 1
}

private data class TabDef(val screen: Screen, val label: String, val icon: ImageVector)

@Composable
private fun BottomPillBar(current: Screen, onSelect: (Screen) -> Unit) {
    val tabs = listOf(
        TabDef(Screen.Timeline, "Gambar", Icons.Filled.Image),
        TabDef(Screen.Albums, "Album", Icons.Filled.Collections),
        TabDef(Screen.Audio, "Audio", Icons.Filled.MusicNote),
        TabDef(Screen.MenuTab, "Menu", Icons.Filled.Menu),
    )
    Surface(
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (tab in tabs) {
                val selected = current::class == tab.screen::class
                Box(
                    modifier = Modifier
                        .width(84.dp)
                        .height(48.dp)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(24.dp),
                        )
                        .clickable { onSelect(tab.screen) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            tab.icon, contentDescription = tab.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            tab.label, fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Splash ala WhatsApp: latar putih, ikon galeri di tengah, logo FRM di
 * bawah. Tampil singkat di atas konten saat aplikasi dibuka (konten di
 * belakangnya sudah termuat), lalu hilang sendiri.
 */
@Composable
private fun LayarSplash() {
    var tampil by remember { mutableStateOf(true) }
    var memudar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2000)
        memudar = true
        delay(520)
        tampil = false
    }
    if (!tampil) return
    // Keluarnya MEMUDAR halus (bukan potong kasar) + ikon membesar sedikit
    val alphaKeluar by animateFloatAsState(
        targetValue = if (memudar) 0f else 1f,
        animationSpec = tween(480, easing = FastOutSlowInEasing),
        label = "splashKeluar",
    )
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alphaKeluar }
            .background(Color.White),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_art),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(128.dp)
                .shadow(10.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp))
                .graphicsLayer {
                    val membesar = 1f + (1f - alphaKeluar) * 0.07f
                    scaleX = membesar
                    scaleY = membesar
                },
        )
        Image(
            painter = painterResource(R.drawable.logo_frm),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .height(56.dp),
        )
    }
}

@Composable
private fun AppRoot(vm: GalleryViewModel = viewModel()) {
    val context = LocalContext.current
    // Tab terakhir yg dibuka DIPULIHKAN pas aplikasi dibuka (UiStore);
    // izin belum ada -> tetap mulai dari Gambar (di sana gerbang izinnya).
    var screen by remember {
        mutableStateOf<Screen>(
            when (UiStore.loadTab(context)) {
                "album" -> Screen.Albums
                "audio" -> Screen.Audio
                "menu" -> Screen.MenuTab
                else -> Screen.Timeline
            }
        )
    }
    LaunchedEffect(screen) {
        val tab = when (screen) {
            is Screen.Timeline -> "gambar"
            is Screen.Albums -> "album"
            is Screen.Audio -> "audio"
            is Screen.MenuTab -> "menu"
            else -> null
        }
        if (tab != null) UiStore.saveTab(context, tab)
    }
    // Pemutar musik MELAYANG (overlay di atas layar yg lagi kebuka), bukan
    // layar pengganti — pas ditarik turun yg keliatan daftar lagunya,
    // bukan hitam (perintah user 2026-11-05).
    var pemutarTerbuka by remember { mutableStateOf(false) }
    var selecting by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf<Set<String>>(emptySet()) }
    val snackbar = remember { SnackbarHostState() }

    // ---- Mode dari aplikasi lain: LIHAT (ACTION_VIEW) / PILIH (PICK / GET_CONTENT) ----
    val aktivitas = context as? android.app.Activity
    val intentAwal = remember { aktivitas?.intent }
    val modePilih = intentAwal?.action == android.content.Intent.ACTION_PICK ||
        intentAwal?.action == android.content.Intent.ACTION_GET_CONTENT
    val pilihBanyak = modePilih &&
        intentAwal?.getBooleanExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, false) == true
    val uriLihat = if (intentAwal?.action == android.content.Intent.ACTION_VIEW) intentAwal.data else null
    var modeLihatLuar by remember { mutableStateOf(false) }
    var lihatSudahDibuka by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (modePilih) {
            screen = Screen.Timeline
            if (pilihBanyak) selecting = true
        }
    }

    // VIEW: tunggu media lokal termuat; ketemu di pool -> buka dgn konteks
    // daftarnya, gak ketemu -> viewer satu item dari URI asing itu.
    LaunchedEffect(uriLihat, vm.media.size) {
        val uri = uriLihat ?: return@LaunchedEffect
        if (lihatSudahDibuka) return@LaunchedEffect
        if (vm.media.isEmpty()) {
            kotlinx.coroutines.delay(2500)
            if (lihatSudahDibuka) return@LaunchedEffect
        }
        val cocok = vm.media.firstOrNull { it.uri.toString() == uri.toString() }
        modeLihatLuar = true
        lihatSudahDibuka = true
        screen = if (cocok != null) {
            Screen.Viewer(vm.media, vm.media.indexOf(cocok), Screen.Timeline)
        } else {
            val itemLuar = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                buatItemDariUri(context, uri)
            }
            Screen.Viewer(listOf(itemLuar), 0, Screen.Timeline)
        }
    }

    fun selesaiPilih(daftar: List<MediaItem>) {
        val act = aktivitas ?: return
        if (daftar.isEmpty()) return
        val hasil = android.content.Intent()
        hasil.data = daftar.first().uri
        if (daftar.size > 1) {
            val clip = android.content.ClipData.newUri(context.contentResolver, "Galeri FRM", daftar.first().uri)
            daftar.drop(1).forEach { clip.addItem(android.content.ClipData.Item(it.uri)) }
            hasil.clipData = clip
        }
        hasil.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        act.setResult(android.app.Activity.RESULT_OK, hasil)
        act.finish()
    }

    // ---- Izin media ----
    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    fun izinDiberikan(): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    var granted by remember { mutableStateOf(izinDiberikan()) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.all { it } || izinDiberikan()
        if (granted) vm.reloadLocal()
    }
    LaunchedEffect(Unit) {
        if (!granted) permLauncher.launch(permissions) else vm.reloadLocal()
    }
    // Izin belum ada/ditolak: paksa ke tab Gambar (gerbang izin cuma di sana),
    // jangan sampai tab tersimpan bikin user nyangkut di tab lain tanpa izin.
    LaunchedEffect(granted) {
        if (!granted && screen !is Screen.Timeline) screen = Screen.Timeline
    }

    // ---- Alur GANTI COVER lagu: izin tulis berkas -> pilih foto -> VM nulis
    // gambarnya ke tag berkas (ID3/APIC buat MP3). Dipicu dari cover pemutar.
    val pemilihCover = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uriFoto ->
        val lagu = vm.laguGantiCover
        if (uriFoto != null && lagu != null) {
            vm.mulaiPotong(lagu, uriFoto)
        } else {
            vm.batalGantiCover()
        }
    }
    val izinTulisCover = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { hasil ->
        if (hasil.resultCode == android.app.Activity.RESULT_OK && vm.laguGantiCover != null) {
            pemilihCover.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            if (vm.laguGantiCover != null) vm.beriTahu("Izin menulis berkas lagu ditolak")
            vm.batalGantiCover()
        }
    }
    LaunchedEffect(vm.laguGantiCover) {
        val lagu = vm.laguGantiCover ?: return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= 30) {
            runCatching {
                val pi = MediaStore.createWriteRequest(context.contentResolver, listOf(lagu.uri))
                izinTulisCover.launch(IntentSenderRequest.Builder(pi.intentSender).build())
            }.onFailure {
                vm.beriTahu("Tidak bisa minta izin menulis berkas lagu")
                vm.batalGantiCover()
            }
        } else {
            pemilihCover.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // ---- Izin PINDAH file (seret foto ke album): MediaStore minta izin
    // tulis lewat RecoverableSecurityException yg disimpan VM ----
    val izinPindahLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { hasil ->
        vm.selesaikanPindahSetelahIzin(hasil.resultCode == android.app.Activity.RESULT_OK)
    }
    LaunchedEffect(vm.pindahPerluIzin) {
        val p = vm.pindahPerluIzin ?: return@LaunchedEffect
        runCatching {
            izinPindahLauncher.launch(IntentSenderRequest.Builder(p.pendingIntent.intentSender).build())
        }.onFailure { vm.selesaikanPindahSetelahIzin(false) }
    }

    // ---- Izin GANTI NAMA file (Rincian → Ubah): pola sama kayak izin
    // pindah, state tertundanya di VM ----
    val izinGantiNamaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { hasil ->
        vm.selesaikanGantiNamaSetelahIzin(hasil.resultCode == android.app.Activity.RESULT_OK)
    }
    LaunchedEffect(vm.gantiNamaPerluIzin) {
        val g = vm.gantiNamaPerluIzin ?: return@LaunchedEffect
        runCatching {
            izinGantiNamaLauncher.launch(IntentSenderRequest.Builder(g.pendingIntent.intentSender).build())
        }.onFailure { vm.selesaikanGantiNamaSetelahIzin(false) }
    }

    // ---- Izin AUDIO terpisah (tab Audio): nolak audio TIDAK boleh
    // mengunci galeri foto/video, jadi launchernya sendiri. ----
    val izinAudioPerm = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    fun izinAudioDiberikan(): Boolean =
        ContextCompat.checkSelfPermission(context, izinAudioPerm) == PackageManager.PERMISSION_GRANTED
    var audioGranted by remember { mutableStateOf(izinAudioDiberikan()) }
    val audioPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        audioGranted = izinAudioDiberikan()
        if (audioGranted) vm.reloadAudio()
    }
    LaunchedEffect(Unit) {
        vm.reloadDrive()
    }

    // ---- Pesan snackbar ----
    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }
    LaunchedEffect(vm.uploading) {
        if (vm.uploading) snackbar.showSnackbar("Meng-upload ke Drive…")
    }

    // ---- Launcher hapus (MediaStore.createDeleteRequest, API 30+) ----
    var afterDeleteGoBack by remember { mutableStateOf(false) }
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        vm.reloadLocal()
        if (screen is Screen.Sampah) vm.muatSampah()
        selection = emptySet()
        selecting = false
        if (afterDeleteGoBack) {
            val s = screen
            if (s is Screen.Viewer) screen = s.from
            afterDeleteGoBack = false
        }
    }
    // ---- Hapus ala Samsung: SELALU masuk SAMPAH + ada dialog konfirmasi ----
    // Lokal murni -> dialog sampah SISTEM (createTrashRequest, persis
    // screenshot user: "Pindahkan video ini ke Sampah?").
    // Ada item Drive -> dialog konfirmasi gaya yg sama dari aplikasi dulu
    // (API Drive tidak punya dialog sistem), baru trash dieksekusi.
    var konfirmasiHapus by remember { mutableStateOf<List<MediaItem>?>(null) }
    var konfirmasiHapusGoBack by remember { mutableStateOf(false) }

    // URI dikonversi ke koleksi per tipe (Images/Video): koleksi Files
    // ditolak request MediaStore (penyebab crash lama).
    fun urisKetik(items: List<MediaItem>): List<Uri> = items.map { item ->
        val id = runCatching { android.content.ContentUris.parseId(item.uri) }.getOrDefault(-1L)
        if (id > 0) {
            android.content.ContentUris.withAppendedId(
                if (item.isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id,
            )
        } else {
            item.uri
        }
    }

    fun luncurkanSampahLokal(locals: List<MediaItem>, goBackAfter: Boolean) {
        val hasil = runCatching {
            val pending = MediaStore.createTrashRequest(context.contentResolver, urisKetik(locals), true)
            afterDeleteGoBack = goBackAfter
            deleteLauncher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
        }
        hasil.onFailure { e ->
            vm.beriTahu("Gagal menghapus: ${e.message ?: "error tidak dikenal"}")
        }
    }

    /** Pulihkan item dari sampah lokal (trash=false). */
    fun luncurkanPulihLokal(items: List<MediaItem>) {
        val hasil = runCatching {
            val pending = MediaStore.createTrashRequest(context.contentResolver, urisKetik(items), false)
            afterDeleteGoBack = false
            deleteLauncher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
        }
        hasil.onFailure { e ->
            vm.beriTahu("Gagal memulihkan: ${e.message ?: "error tidak dikenal"}")
        }
    }

    /** Hapus item lokal PERMANEN dari layar sampah. */
    fun luncurkanHapusPermanenLokal(items: List<MediaItem>) {
        val hasil = runCatching {
            val pending = MediaStore.createDeleteRequest(context.contentResolver, urisKetik(items))
            afterDeleteGoBack = false
            deleteLauncher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
        }
        hasil.onFailure { e ->
            vm.beriTahu("Gagal menghapus permanen: ${e.message ?: "error tidak dikenal"}")
        }
    }

    fun eksekusiHapus(items: List<MediaItem>, goBackAfter: Boolean) {
        val driveApiItems = items.filter { it.driveId.isNotBlank() }
        val locals = items.filter { !it.isDrive }
        if (driveApiItems.isNotEmpty()) {
            vm.deleteDriveItems(driveApiItems) {
                selection = emptySet()
                selecting = false
                // Kembali dari viewer cuma kalau TIDAK ada sisa lokal yg
                // masih menunggu dialog sistemnya sendiri.
                if (goBackAfter && locals.isEmpty()) {
                    val s = screen
                    if (s is Screen.Viewer) screen = s.from
                }
            }
        }
        if (locals.isNotEmpty()) {
            luncurkanSampahLokal(locals, goBackAfter)
        }
    }

    fun requestDelete(items: List<MediaItem>, goBackAfter: Boolean = false) {
        if (items.isEmpty()) return
        if (items.any { it.driveId.isNotBlank() }) {
            konfirmasiHapus = items
            konfirmasiHapusGoBack = goBackAfter
        } else {
            luncurkanSampahLokal(items.filter { !it.isDrive }, goBackAfter)
        }
    }

    // Dialog konfirmasi khusus jalur Drive — teks meniru dialog sistem
    konfirmasiHapus?.let { items ->
        val teksTanya = when {
            items.size > 1 -> "Pindahkan ${items.size} item ke Sampah?"
            items[0].isVideo -> "Pindahkan video ini ke Sampah?"
            else -> "Pindahkan foto ini ke Sampah?"
        }
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = null },
            text = { Text(teksTanya) },
            confirmButton = {
                TextButton(onClick = {
                    val goBack = konfirmasiHapusGoBack
                    konfirmasiHapus = null
                    eksekusiHapus(items, goBack)
                }) { Text("Pindahkan ke Sampah", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiHapus = null }) { Text("Batal") }
            },
        )
    }

    fun selectedItems(): List<MediaItem> =
        (vm.media + vm.driveItems).filter { it.key in selection }

    fun onItemClick(sourceItems: List<MediaItem>, item: MediaItem) {
        if (modePilih) {
            if (item.isDrive) {
                vm.beriTahu("Item Drive belum bisa dipilih dari aplikasi lain")
                return
            }
            if (pilihBanyak) {
                selection = if (item.key in selection) selection - item.key else selection + item.key
            } else {
                selesaiPilih(listOf(item))
            }
            return
        }
        if (selecting) {
            selection = if (item.key in selection) selection - item.key else selection + item.key
        } else {
            val idx = sourceItems.indexOfFirst { it.key == item.key }
            if (idx >= 0) {
                screen = Screen.Viewer(sourceItems, idx, screen)
            }
        }
    }
    fun onItemLongClick(item: MediaItem) {
        if (modePilih && !pilihBanyak) {
            if (!item.isDrive) selesaiPilih(listOf(item))
            return
        }
        selecting = true
        selection = selection + item.key
    }
    fun closeSelecting() {
        selecting = false
        selection = emptySet()
    }

    // ---- Tombol KEMBALI ----
    // Layar DETAIL tetap bertingkat (pilih -> batal; Viewer/Player -> asal;
    // AlbumDetail -> Album; Sampah -> Menu; AudioFolder -> Audio; login
    // nunggu -> batal). Di TAB AKAR (Gambar/Album/Audio/Menu) handler ini
    // DIMATIKAN: sekali kembali LANGSUNG KELUAR aplikasi — perintah user
    // 2026-11-05 (pola lama mampir Gambar dulu + tekan 2x DICABUT, karena
    // sekarang tab terakhir sudah diingat dan dipulihkan pas buka lagi).
    val perluHandlerKembali = pemutarTerbuka || selecting || vm.loginMenunggu || modePilih ||
        screen is Screen.Viewer || screen is Screen.AlbumDetail ||
        screen is Screen.Sampah || screen is Screen.AudioFolder
    BackHandler(enabled = perluHandlerKembali) {
        val s = screen
        when {
            pemutarTerbuka -> pemutarTerbuka = false
            modePilih -> {
                aktivitas?.setResult(android.app.Activity.RESULT_CANCELED)
                aktivitas?.finish()
            }
            modeLihatLuar && s is Screen.Viewer -> aktivitas?.finish()
            selecting -> closeSelecting()
            s is Screen.Viewer -> screen = s.from
            s is Screen.AlbumDetail -> screen = Screen.Albums
            s is Screen.Sampah -> screen = Screen.MenuTab
            s is Screen.AudioFolder -> screen = Screen.Audio
            vm.loginMenunggu -> vm.batalLogin()
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Transisi antar-layar (permintaan user: jangan kaku):
        // maju = geser kiri, KEMBALI = geser kanan (dari kedalaman layar),
        // buka foto = nge-ZOOM membesar, tutup foto = MENCIUT mengecil.
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                val dari = kedalamanLayar(initialState)
                val ke = kedalamanLayar(targetState)
                when {
                    targetState is Screen.Viewer ->
                        (scaleIn(tween(300), initialScale = 0.84f) + fadeIn(tween(300))) togetherWith
                            fadeOut(tween(160))
                    initialState is Screen.Viewer ->
                        fadeIn(tween(220)) togetherWith
                            (scaleOut(tween(260), targetScale = 0.84f) + fadeOut(tween(260)))
                    ke > dari ->
                        (slideInHorizontally(tween(320)) { it / 3 } + fadeIn(tween(320))) togetherWith
                            (slideOutHorizontally(tween(320)) { -it / 5 } + fadeOut(tween(200)))
                    ke < dari ->
                        (slideInHorizontally(tween(320)) { -it / 3 } + fadeIn(tween(320))) togetherWith
                            (slideOutHorizontally(tween(320)) { it / 5 } + fadeOut(tween(200)))
                    else -> fadeIn(tween(200)) togetherWith fadeOut(tween(160))
                }
            },
            label = "layar",
        ) { s ->
        when (s) {
            is Screen.Timeline -> {
                if (!granted) {
                    PermissionPlaceholder { permLauncher.launch(permissions) }
                } else {
                    TimelineScreen(
                        vm = vm,
                        selection = selection,
                        selecting = selecting,
                        onItemClick = { item ->
                            val gabung = (vm.media + vm.driveItems).sortedByDescending { it.dateMillis }
                            onItemClick(gabung, item)
                        },
                        onItemLongClick = ::onItemLongClick,
                        onStartSelecting = { selecting = true },
                        onCloseSelecting = ::closeSelecting,
                        onShareSelected = { shareItems(context, selectedItems()) },
                        onDeleteSelected = { requestDelete(selectedItems()) },
                        onUploadSelected = { vm.uploadToDrive(selectedItems()) },
                        sembunyikanKromPilih = modePilih,
                    )
                }
            }
            is Screen.Albums -> {
                AlbumsScreen(
                    vm = vm,
                    onOpenAlbum = { album -> screen = Screen.AlbumDetail(album) },
                )
            }
            is Screen.MenuTab -> {
                MenuScreen(
                    vm = vm,
                    onOpenFavorites = { screen = Screen.AlbumDetail(vm.favoritesAlbum()) },
                    onLoginAkunLain = { vm.loginAkunLain() },
                    onOpenTrash = { screen = Screen.Sampah },
                )
            }
            is Screen.Audio -> {
                AudioScreen(
                    vm = vm,
                    izinAudio = audioGranted,
                    onMintaIzin = {
                        // Izin notifikasi sekalian (buat notifikasi "sedang diputar")
                        val daftar = if (Build.VERSION.SDK_INT >= 33) {
                            arrayOf(izinAudioPerm, Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            arrayOf(izinAudioPerm)
                        }
                        audioPermLauncher.launch(daftar)
                    },
                    onOpenFolder = { screen = Screen.AudioFolder(it.id) },
                    onOpenPlayer = { pemutarTerbuka = true },
                )
            }
            is Screen.AudioFolder -> {
                val folder = vm.audioFolders.firstOrNull { it.id == s.folderId }
                    ?: AudioFolder(id = s.folderId, name = "Audio", items = vm.laguDaftar)
                AudioTracksScreen(
                    vm = vm,
                    folder = folder,
                    onBack = { screen = Screen.Audio },
                    onOpenPlayer = { pemutarTerbuka = true },
                )
            }
            is Screen.Sampah -> {
                TrashScreen(
                    sampahLokal = vm.sampahLokal,
                    sampahDrive = vm.sampahDrive,
                    driveTersambung = vm.driveApiOn,
                    loading = vm.sampahLoading,
                    onBack = { screen = Screen.MenuTab },
                    onPulihkan = { items ->
                        if (items.firstOrNull()?.isDrive == true) vm.pulihkanDriveItems(items)
                        else luncurkanPulihLokal(items)
                    },
                    onHapusPermanen = { items ->
                        if (items.firstOrNull()?.isDrive == true) vm.hapusPermanenDriveItems(items)
                        else luncurkanHapusPermanenLokal(items)
                    },
                    onSegarkan = { vm.muatSampah() },
                )
            }
            is Screen.AlbumDetail -> {
                AlbumDetailScreen(
                    vm = vm,
                    album = s.album,
                    selection = selection,
                    selecting = selecting,
                    onBack = { screen = Screen.Albums },
                    onItemClick = { items, item -> onItemClick(items, item) },
                    onItemLongClick = ::onItemLongClick,
                    onCloseSelecting = ::closeSelecting,
                    onShareSelected = { shareItems(context, selectedItems()) },
                    onDeleteSelected = { requestDelete(selectedItems()) },
                    onUploadSelected = { vm.uploadToDrive(selectedItems()) },
                    onStartSelecting = { selecting = true },
                )
            }
            is Screen.Viewer -> {
                ViewerScreen(
                    items = s.items,
                    startIndex = s.index,
                    isFavorite = { vm.isFavorite(it) },
                    onToggleFavorite = { vm.toggleFavorite(it) },
                    onDelete = { item -> requestDelete(listOf(item), goBackAfter = true) },
                    onBack = { if (modeLihatLuar) aktivitas?.finish() else screen = s.from },
                    videoSedangDiLatar = vm.videoDiLatar != null,
                    onSerahkanVideo = { videos, mulai, pos -> vm.serahkanVideoKeLatar(videos, mulai, pos) },
                    onAmbilAlihVideo = { vm.ambilAlihDariLatar() },
                    onGantiNama = { item, nama -> vm.gantiNama(item, nama) },
                )
            }
        }
        }

        // Tab bar mengambang ala Samsung — cuma di layar-layar utama
        val showTabs = !modePilih && (screen is Screen.Timeline || screen is Screen.Albums ||
            screen is Screen.MenuTab || screen is Screen.Audio)
        // Mini player musik nempel di atas pill tab selama ada lagu dimuat;
        // di dalam folder audio juga tampil (di sana nggak ada pill tab,
        // jadi nempel bawah biasa)
        val diFolderAudio = screen is Screen.AudioFolder
        if ((showTabs || diFolderAudio) && !selecting && vm.laguDimainkan != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = if (diFolderAudio) 16.dp else 88.dp),
                contentAlignment = Alignment.Center,
            ) {
                MiniPlayerBar(vm = vm, onOpenPlayer = { pemutarTerbuka = true })
            }
        }
        if (showTabs && !selecting) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                BottomPillBar(current = screen, onSelect = { screen = it })
            }
        }

        // Bar penutup mode PILIH banyak (dari aplikasi lain)
        if (modePilih && pilihBanyak) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 6.dp,
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = {
                            aktivitas?.setResult(android.app.Activity.RESULT_CANCELED)
                            aktivitas?.finish()
                        }) { Text("Batal") }
                        Button(
                            onClick = { selesaiPilih(selectedItems().filter { !it.isDrive }) },
                            enabled = selection.isNotEmpty(),
                        ) { Text("Selesai (${selection.size})") }
                    }
                }
            }
        }

        // === PEMUTAR MELAYANG: lembaran meluncur dari bawah DI ATAS layar
        // yg lagi kebuka — di belakangnya daftar lagu asli, bukan hitam.
        AnimatedVisibility(
            visible = pemutarTerbuka,
            enter = slideInVertically(tween(340)) { it } + fadeIn(tween(200)),
            exit = slideOutVertically(tween(320)) { it } + fadeOut(tween(140)),
            modifier = Modifier.fillMaxSize(),
        ) {
            PlayerScreen(vm = vm, onBack = { pemutarTerbuka = false })
        }

        // === LAYAR POTONG PERSEGI buat cover (di atas pemutar) ===
        vm.fotoPotong?.let { (laguPotong, uriPotong) ->
            LayarPotongCover(
                lagu = laguPotong,
                uriFoto = uriPotong,
                onBatal = { vm.batalPotong() },
                onSimpan = { jpeg -> vm.tulisCoverLagu(laguPotong, jpeg) },
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (vm.laguDimainkan != null && showTabs) 156.dp else 84.dp),
        )
    }
}

@Composable
private fun PermissionPlaceholder(onRequest: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Galeri FRM butuh izin melihat foto & video di HP ini.")
            Button(onClick = onRequest, modifier = Modifier.padding(top = 12.dp)) {
                Text("Berikan izin")
            }
        }
    }
}
