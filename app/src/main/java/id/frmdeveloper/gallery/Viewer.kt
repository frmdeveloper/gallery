package id.frmdeveloper.gallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Job
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.viewinterop.AndroidView
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

/**
 * Foto dgn zoom: cubit buat zoom, KETUK 2x buat zoom SMOOTH beranimasi ke
 * titik yg diketuk (balik ke 1x juga animasi). Pas lagi zoom dan digeser
 * sampai MENTOK di tepi lalu didorong lagi, pindah ke foto sebelah
 * (onPindahFoto ±1) — pola galeri Samsung/iOS.
 */
@Composable
private fun ZoomableImage(
    item: MediaItem,
    onTap: () -> Unit,
    aktif: Boolean = false,
    onSkalaBerubah: (Float) -> Unit = {},
    onPindahFoto: (Int) -> Unit = {},
) {
    var scale by remember(item.key) { mutableFloatStateOf(1f) }
    var offset by remember(item.key) { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()
    var animJob by remember(item.key) { mutableStateOf<Job?>(null) }
    // Laporkan skala halaman AKTIF ke ViewerScreen (gerbang tarik-tutup:
    // tarik ke bawah cuma jalan pas foto lagi tidak di-zoom)
    LaunchedEffect(scale, aktif) {
        if (aktif) onSkalaBerubah(scale)
    }

    fun batas(s: Float, w: Int, h: Int) = Offset(
        (w * (s - 1f) / 2f).coerceAtLeast(0f),
        (h * (s - 1f) / 2f).coerceAtLeast(0f),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Background viewer IKUT TEMA (screenshot Galeri Samsung asli user:
            // mode terang = PUTIH, bukan hitam pekat kayak versi lama kita)
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(item.key) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { titik ->
                        animJob?.cancel()
                        val dariSkala = scale
                        val dariOffset = offset
                        val keSkala = if (dariSkala > 1.05f) 1f else 2.5f
                        val pusat = Offset(size.width / 2f, size.height / 2f)
                        val mentah = if (keSkala == 1f) {
                            Offset.Zero
                        } else {
                            titik - pusat - (titik - pusat - dariOffset) * (keSkala / dariSkala)
                        }
                        val b = batas(keSkala, size.width, size.height)
                        val keOffset = Offset(
                            mentah.x.coerceIn(-b.x, b.x),
                            mentah.y.coerceIn(-b.y, b.y),
                        )
                        animJob = scope.launch {
                            animate(
                                dariSkala, keSkala,
                                animationSpec = tween(320, easing = FastOutSlowInEasing),
                            ) { nilai, _ ->
                                val f = if (keSkala != dariSkala) {
                                    (nilai - dariSkala) / (keSkala - dariSkala)
                                } else {
                                    1f
                                }
                                scale = nilai
                                offset = Offset(
                                    dariOffset.x + (keOffset.x - dariOffset.x) * f,
                                    dariOffset.y + (keOffset.y - dariOffset.y) * f,
                                )
                            }
                        }
                    },
                )
            }
            .pointerInput(item.key) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var pindahTerkunci = false
                    var lebihX = 0f
                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2 || scale > 1f) {
                            animJob?.cancel()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                            scale = newScale
                            if (newScale > 1f) {
                                val b = batas(newScale, size.width, size.height)
                                val mau = offset + panChange
                                val dijepit = Offset(
                                    mau.x.coerceIn(-b.x, b.x),
                                    mau.y.coerceIn(-b.y, b.y),
                                )
                                // Geser 1 jari yg KELEBIHAN setelah mentok tepi:
                                // akumulasi — lewat ambang = pindah foto sebelah.
                                if (pressedCount == 1 && !pindahTerkunci) {
                                    lebihX += mau.x - dijepit.x
                                    if (lebihX < -260f || lebihX > 260f) {
                                        val arah = if (lebihX < 0) 1 else -1
                                        pindahTerkunci = true
                                        scale = 1f
                                        offset = Offset.Zero
                                        onPindahFoto(arah)
                                    }
                                }
                                offset = dijepit
                            } else {
                                offset = Offset.Zero
                            }
                            event.changes.forEach { if (it.position != it.previousPosition) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale, scaleY = scale,
                    translationX = offset.x, translationY = offset.y,
                ),
        )
    }
}


/**
 * Tombol bulat KACA (frosted) ala iOS — alas kontrol pemutar video:
 * putih transparan + garis tepi tipis, ikon putih di tengah.
 */
@Composable
private fun TombolKaca(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ukuran: Dp = 38.dp,
    enabled: Boolean = true,
    konten: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.size(ukuran).kacaCairHitam(CircleShape, bayangan = 8.dp),
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxSize()) {
            konten()
        }
    }
}


/**
 * Kontrol PRATINJAU video — baris tombol IKON SAJA di tengah bawah
 * (posisi baris pill foto Samsung, ~89% tinggi layar):
 * [play/pause] [layar penuh -> pemutar lengkap] [bisu] bersebelahan,
 * tanpa teks sama sekali (perintah user 2026-11-07).
 */
@Composable
private fun PratinjauKontrol(
    kontrolTampil: Boolean,
    panelTerbuka: Boolean,
    muter: Boolean,
    selesai: Boolean,
    bisu: Boolean,
    onTogglePutar: () -> Unit,
    onToggleBisu: () -> Unit,
    onBukaPlayer: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = kontrolTampil && !panelTerbuka,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 71.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(46.dp).kacaCairHitam(CircleShape, bayangan = 6.dp),
                ) {
                    IconButton(onClick = onTogglePutar) {
                        Icon(
                            if (muter) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            if (muter) "Jeda" else "Putar",
                            tint = Color.White, modifier = Modifier.size(23.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(46.dp).kacaCairHitam(CircleShape, bayangan = 6.dp),
                ) {
                    IconButton(onClick = onBukaPlayer) {
                        Icon(
                            Icons.Filled.Fullscreen, "Pemutar lengkap",
                            tint = Color.White, modifier = Modifier.size(23.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(46.dp).kacaCairHitam(CircleShape, bayangan = 6.dp),
                ) {
                    IconButton(onClick = onToggleBisu) {
                        Icon(
                            if (bisu) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            if (bisu) "Suara mati" else "Suara nyala",
                            tint = Color.White, modifier = Modifier.size(23.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoPage(
    item: MediaItem,
    isCurrent: Boolean,
    favorit: Boolean,
    onToggleFavorite: () -> Unit,
    onInfo: () -> Unit,
    tampilBerbagi: Boolean,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    adaPrev: Boolean,
    adaNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modeMusik: Boolean = false,
    onModeMusik: (Boolean) -> Unit = {},
    onLompatAcak: () -> Unit = {},
    onSerahkanKeLatar: (Long) -> Unit = {},
    sambungan: Pair<String, Long>? = null,
    onSambunganTerpakai: () -> Unit = {},
    onKunciPager: (Boolean) -> Unit = {},
    panelTerbuka: Boolean = false,
    onBarTampil: (Boolean) -> Unit = {},
    onPlayerPenuh: (Boolean) -> Unit = {},
    onPutarLayar: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val scope = rememberCoroutineScope()
    val player = remember(item.key) {
        val builder = ExoPlayer.Builder(context)
        // Video Drive API butuh header Authorization; token diambil dari cache
        // (sudah dipanaskan pas scan Drive) — tanpa jaringan di main thread.
        if (item.driveId.isNotBlank()) {
            val token = DriveApi.cachedTokenUntukFile(item.driveId)
            if (token != null) {
                val dataSource = androidx.media3.datasource.DefaultHttpDataSource.Factory()
                    .setDefaultRequestProperties(mapOf("Authorization" to "Bearer $token"))
                builder.setMediaSourceFactory(
                    androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSource)
                )
            }
        }
        builder.build().apply {
            setMediaItem(ExoMediaItem.fromUri(item.uri))
            prepare()
            // Lanjutkan dari posisi terakhir ditonton (kalau ada & masuk akal)
            val simpanan = PosisiVideoStore.baca(context, item.key)
            if (simpanan > 5_000) seekTo(simpanan)
        }
    }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }
    var kontrolVisible by remember(item.key) { mutableStateOf(true) }
    var terkunci by remember(item.key) { mutableStateOf(false) }
    var muter by remember(item.key) { mutableStateOf(false) }
    var selesai by remember(item.key) { mutableStateOf(false) }
    var posisiMs by remember(item.key) { mutableLongStateOf(0L) }
    var durasiMs by remember(item.key) { mutableLongStateOf(item.durationMs) }
    var geser by remember(item.key) { mutableStateOf<Float?>(null) }
    var ccAktif by remember(item.key) { mutableStateOf(true) }
    var adaCc by remember(item.key) { mutableStateOf(false) }
    var menuOpen by remember(item.key) { mutableStateOf(false) }
    var rasioIdx by remember(item.key) { mutableIntStateOf(0) }
    var layarPenuh by remember(item.key) { mutableStateOf(false) }
    var tick by remember(item.key) { mutableIntStateOf(0) }
    // Dua lapis ala Samsung: PRATINJAU (video autoplay di halaman penampil,
    // krom penampil yg ngambang) -> tombol pemutar masuk PLAYER PENUH
    // (kontrol kaca lengkap). Back dari player = balik ke pratinjau.
    var playerPenuh by remember(item.key) { mutableStateOf(false) }
    var bisu by remember(item.key) { mutableStateOf(true) }
    LaunchedEffect(kontrolVisible, isCurrent) {
        if (isCurrent) onBarTampil(kontrolVisible)
    }
    LaunchedEffect(playerPenuh, isCurrent) {
        if (isCurrent) onPlayerPenuh(playerPenuh)
    }
    LaunchedEffect(bisu) { player.volume = if (bisu) 0f else 1f }
    BackHandler(enabled = playerPenuh && !modeMusik) {
        playerPenuh = false
        bisu = true
    }

    // Kembali pas mode musik aktif = keluar mode dulu, bukan keluar penampil
    BackHandler(enabled = modeMusik) { onModeMusik(false) }

    fun toast(teks: String) {
        android.widget.Toast.makeText(context, teks, android.widget.Toast.LENGTH_SHORT).show()
    }
    fun sentuh() {
        tick++
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                muter = isPlaying
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    selesai = true
                    kontrolVisible = true
                    PosisiVideoStore.hapus(context, item.key)
                } else if (state == Player.STATE_READY) {
                    selesai = false
                }
            }
            override fun onTracksChanged(tracks: Tracks) {
                adaCc = tracks.groups.any { it.type == C.TRACK_TYPE_TEXT }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    DisposableEffect(item.key) {
        onDispose {
            if (!selesai) {
                PosisiVideoStore.simpan(context, item.key, player.currentPosition.coerceAtLeast(0))
            }
            player.release()
        }
    }
    // (Reset orientasi + system bar pas keluar penampil sekarang diurus
    // ViewerScreen sekali per sesi — reset per halaman di sini bikin
    // orientasi kelepas di tengah sesi & tombol putar kayak gak jalan.)
    LaunchedEffect(isCurrent) {
        if (isCurrent) player.play() else player.pause()
    }
    // Keluar aplikasi pas mode musik & lagi muter: oper ke musik latar
    val pemilikHidupVP = LocalLifecycleOwner.current
    DisposableEffect(pemilikHidupVP, modeMusik, isCurrent) {
        val pengamat = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && modeMusik && isCurrent && muter && !selesai) {
                onSerahkanKeLatar(player.currentPosition)
                player.pause()
            }
        }
        pemilikHidupVP.lifecycle.addObserver(pengamat)
        onDispose { pemilikHidupVP.lifecycle.removeObserver(pengamat) }
    }
    // Ambil alih dari latar: lanjutkan dari posisi layanan
    LaunchedEffect(isCurrent, sambungan) {
        val smb = sambungan
        if (isCurrent && smb != null && smb.first == item.key) {
            player.seekTo(smb.second)
            player.play()
            onSambunganTerpakai()
        }
    }
    LaunchedEffect(isCurrent) {
        var terakhirSimpan = 0L
        while (isCurrent) {
            if (geser == null) posisiMs = player.currentPosition.coerceAtLeast(0)
            val posKini = player.currentPosition.coerceAtLeast(0)
            if (kotlin.math.abs(posKini - terakhirSimpan) > 4_000) {
                terakhirSimpan = posKini
                PosisiVideoStore.simpan(context, item.key, posKini)
            }
            val d = player.duration
            if (d > 0) durasiMs = d
            delay(300)
        }
    }
    // Kontrol sembunyi sendiri setelah 3 detik pas video muter
    LaunchedEffect(kontrolVisible, muter, tick, terkunci) {
        if (kontrolVisible && muter && !terkunci) {
            delay(3200)
            kontrolVisible = false
        }
    }

    fun setLayarPenuh(on: Boolean) {
        layarPenuh = on
        val w = activity?.window ?: return
        val c = androidx.core.view.WindowCompat.getInsetsController(w, w.decorView)
        if (on) {
            c.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            c.systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            c.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
        sentuh()
    }

    fun putarLayar() {
        // Mekanisme bareng ViewerScreen: memutar LAYAR (orientasi)
        onPutarLayar()
        sentuh()
    }

    fun toggleCc() {
        if (!adaCc) {
            toast("Video ini tidak punya subtitle")
            return
        }
        val params = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, ccAktif)
            .build()
        player.trackSelectionParameters = params
        ccAktif = !ccAktif
        sentuh()
    }

    fun tangkapFrame() {
        val tv = playerViewRef?.videoSurfaceView as? android.view.TextureView
        val bmp = tv?.bitmap
        if (bmp == null) {
            toast("Frame belum siap ditangkap")
            return
        }
        scope.launch(Dispatchers.IO) {
            val ok = runCatching {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "Frame_${System.currentTimeMillis()}.jpg")
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GaleriFRM")
                    put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uriBaru = context.contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
                ) ?: error("insert gagal")
                context.contentResolver.openOutputStream(uriBaru)?.use { out ->
                    check(bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out))
                }
                values.clear()
                values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uriBaru, values, null, null)
            }.isSuccess
            withContext(Dispatchers.Main) {
                toast(if (ok) "Frame disimpan ke Pictures/GaleriFRM" else "Gagal menyimpan frame")
            }
        }
        sentuh()
    }


    val backdropHalaman = rememberLayerBackdrop()
    Box(
        Modifier
            .fillMaxSize()
            .background(
                if (playerPenuh || modeMusik) Color.Black
                else MaterialTheme.colorScheme.background
            ),
    ) {
        CompositionLocalProvider(LocalKacaMedia provides backdropHalaman) {
        if (modeMusik) {
            LayarModeMusikVideo(
                item = item,
                player = player,
                muter = muter,
                selesai = selesai,
                favorit = favorit,
                posisiMs = posisiMs,
                durasiMs = durasiMs,
                geser = geser,
                adaPrev = adaPrev,
                adaNext = adaNext,
                onGeser = { nilai -> geser = nilai; onKunciPager(nilai != null) },
                onToggleFavorite = onToggleFavorite,
                onPrev = onPrev,
                onNext = onNext,
                onLompatAcak = onLompatAcak,
                onKeluar = { onModeMusik(false) },
                onDaftarView = { playerViewRef = it },
            )
        } else {
        AndroidView(
            factory = { ctx ->
                val pv = android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.player_view, null) as PlayerView
                pv.player = player
                playerViewRef = pv
                pv
            },
            modifier = Modifier.fillMaxSize().layerBackdrop(backdropHalaman),
        )
        // Lapisan ketuk: tampil/sembunyi kontrol (geser halaman tetap jalan)
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(item.key) {
                    detectTapGestures(onTap = {
                        if (!terkunci) {
                            kontrolVisible = !kontrolVisible
                            sentuh()
                        }
                    })
                },
        )

        if (!playerPenuh) {
            PratinjauKontrol(
                kontrolTampil = kontrolVisible,
                panelTerbuka = panelTerbuka,
                muter = muter,
                selesai = selesai,
                bisu = bisu,
                onTogglePutar = {
                    if (selesai) {
                        player.seekTo(0)
                        player.play()
                    } else if (muter) {
                        player.pause()
                    } else {
                        player.play()
                    }
                    sentuh()
                },
                onToggleBisu = { bisu = !bisu },
                onBukaPlayer = {
                    bisu = false
                    playerPenuh = true
                    kontrolVisible = true
                    sentuh()
                },
            )
        } else if (terkunci) {
            TombolKaca(
                onClick = { terkunci = false; kontrolVisible = true; sentuh() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 18.dp, bottom = 84.dp),
            ) {
                Icon(Icons.Filled.Lock, "Buka kunci", tint = Color.White, modifier = Modifier.size(19.dp))
            }
        } else {
            // Kontrol muncul/hilang dengan FADE SMOOTH pas diketuk /
            // auto-sembunyi (perintah user: tap sekali -> bar hilang smooth).
            AnimatedVisibility(
                visible = kontrolVisible,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.fillMaxSize(),
            ) {
            Box(Modifier.fillMaxSize()) {
            // Gradasi halus atas & bawah ala iOS (gantinya pill hitam tebal)
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.36f), Color.Transparent),
                        ),
                    ),
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(176.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.44f)),
                        ),
                    ),
            )

            // ---- Atas: judul + tombol kaca (mode musik - menu) ----
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                TombolKaca(onClick = { onModeMusik(true); sentuh() }) {
                    Icon(Icons.Filled.MusicNote, "Mode musik", tint = Color.White, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Box {
                    TombolKaca(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, "Lainnya", tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Tangkap frame") },
                            onClick = { menuOpen = false; tangkapFrame() },
                        )
                        DropdownMenuItem(
                            text = { Text("Rincian") },
                            onClick = { menuOpen = false; onInfo() },
                        )
                        if (tampilBerbagi) {
                            DropdownMenuItem(
                                text = { Text("Berbagi") },
                                onClick = { menuOpen = false; onShare() },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Hapus") },
                            onClick = { menuOpen = false; onDelete() },
                        )
                    }
                }
            }

            // ---- Tengah: transport kaca (prev - putar/jeda - next) ----
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(30.dp),
            ) {
                TombolKaca(
                    onClick = {
                        // ⏮: udah jalan >3 dtk = atur durasi mentok kiri (dari 0);
                        // masih di awal = beneran video sebelumnya (perintah user)
                        if (player.currentPosition > 3000) player.seekTo(0) else onPrev()
                        sentuh()
                    },
                    ukuran = 46.dp,
                    enabled = adaPrev || posisiMs > 3000,
                ) {
                    Icon(
                        Icons.Filled.SkipPrevious, "Sebelumnya",
                        tint = Color.White.copy(alpha = if (adaPrev || posisiMs > 3000) 1f else 0.35f),
                        modifier = Modifier.size(24.dp),
                    )
                }
                TombolKaca(
                    onClick = {
                        if (selesai) {
                            player.seekTo(0)
                            player.play()
                        } else if (muter) {
                            player.pause()
                        } else {
                            player.play()
                        }
                        sentuh()
                    },
                    ukuran = 74.dp,
                ) {
                    Icon(
                        if (muter) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        if (muter) "Jeda" else "Putar",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp),
                    )
                }
                TombolKaca(onClick = { onNext(); sentuh() }, ukuran = 46.dp, enabled = adaNext) {
                    Icon(
                        Icons.Filled.SkipNext, "Berikutnya",
                        tint = Color.White.copy(alpha = if (adaNext) 1f else 0.35f),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // ---- Bawah: SATU kapsul kaca — waktu + seek + baris aksi ----
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .kacaCairHitam(RoundedCornerShape(26.dp), bayangan = 14.dp),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            formatDurasi((geser?.toLong() ?: posisiMs)),
                            color = Color.White, fontSize = 11.sp,
                            modifier = Modifier.width(38.dp),
                        )
                        Slider(
                            value = (geser ?: posisiMs.toFloat()).coerceIn(0f, durasiMs.coerceAtLeast(1).toFloat()),
                            onValueChange = { geser = it; onKunciPager(true) },
                            onValueChangeFinished = {
                                geser?.let { player.seekTo(it.toLong()) }
                                geser = null
                                onKunciPager(false)
                                sentuh()
                            },
                            valueRange = 0f..durasiMs.coerceAtLeast(1).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.35f),
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "-" + formatDurasi(durasiMs - (geser?.toLong() ?: posisiMs)),
                            color = Color.White, fontSize = 11.sp,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { setLayarPenuh(!layarPenuh) }) {
                            Icon(
                                if (layarPenuh) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                "Layar penuh", tint = Color.White, modifier = Modifier.size(21.dp),
                            )
                        }
                        IconButton(onClick = {
                            rasioIdx = (rasioIdx + 1) % 3
                            playerViewRef?.resizeMode = when (rasioIdx) {
                                1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                2 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                            toast(
                                when (rasioIdx) {
                                    1 -> "Rasio: Penuhi (zoom)"
                                    2 -> "Rasio: Regang"
                                    else -> "Rasio: Pas"
                                },
                            )
                            sentuh()
                        }) {
                            Icon(Icons.Filled.AspectRatio, "Rasio", tint = Color.White, modifier = Modifier.size(21.dp))
                        }
                        TextButton(onClick = { toggleCc() }) {
                            Text(
                                "CC",
                                color = Color.White.copy(alpha = if (adaCc && ccAktif) 1f else 0.45f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                        IconButton(onClick = { putarLayar() }) {
                            Icon(Icons.Filled.ScreenRotation, "Putar layar", tint = Color.White, modifier = Modifier.size(21.dp))
                        }
                        IconButton(onClick = { onToggleFavorite(); sentuh() }) {
                            Icon(
                                if (favorit) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                "Favorit", tint = Color.White, modifier = Modifier.size(21.dp),
                            )
                        }
                        IconButton(onClick = { terkunci = true; sentuh() }) {
                            Icon(Icons.Filled.Lock, "Kunci layar", tint = Color.White, modifier = Modifier.size(21.dp))
                        }
                    }
                }
            }
            }
        }
        }

        }
        }
    }
}

/**
 * MODE MUSIK buat video (perintah user 2026-11-05): tampilan persis
 * pemutar musik — kartu kaca, judul, seek dgn sisa waktu negatif,
 * transport — tapi kotak "cover"-nya memutar VIDEO (PlayerView yg sama,
 * player-nya tidak diganti, cuma wadah tampilannya pindah ke sini).
 */
@Composable
private fun LayarModeMusikVideo(
    item: MediaItem,
    player: ExoPlayer,
    muter: Boolean,
    selesai: Boolean,
    favorit: Boolean,
    posisiMs: Long,
    durasiMs: Long,
    geser: Float?,
    adaPrev: Boolean,
    adaNext: Boolean,
    onGeser: (Float?) -> Unit,
    onToggleFavorite: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onLompatAcak: () -> Unit,
    onKeluar: () -> Unit,
    onDaftarView: (PlayerView) -> Unit,
) {
    // Latar = FRAME video utama sendiri: permukaan cover (TextureView)
    // dipotret kecil (~112px) berkala, ditampilkan buram memenuhi layar.
    // Player KEDUA (v1.33–v1.39) DIBUANG: dua decoder + dua stream
    // berebut di HP ini bikin latar macet (diem), telat (delay) & glitch
    // (acak-acakan) — laporan user. Frame sendiri = selalu sinkron,
    // tanpa stream/decoder tambahan. Pas video jeda, frame terakhir
    // tetap tampil (memang begitu seharusnya latar).
    val contextMusik = LocalContext.current
    val izinSpektrumVideo = ingatIzinSpektrum(minta = false)
    var viewCover by remember { mutableStateOf<PlayerView?>(null) }
    // LATAR AMBIENT: thumbnail video diblur + zoom pelan bolak-balik.
    // Jepret frame LIVE dibuang: nyomot frame dari permukaan video yg
    // sama bikin tampilannya kedip-kedip di HP ini (PixelCopy, laporan
    // user v1.43) & cara jepret sebelumnya ngelag. Thumbnail statis =
    // nol jepret, nol ganggu decoder — dijamin stabil; pas ganti video
    // tetap pudar silang dari Coil.
    val transisiZoom = rememberInfiniteTransition(label = "zoomLatar")
    val skalaLatar by transisiZoom.animateFloat(
        initialValue = 1.6f,
        targetValue = 1.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skalaLatar",
    )
    // LATAR JEPRETAN CROSSFADE (dikembalikan atas laporan user: versi
    // jepretan v1.42 LANCAR; yg rusak itu pas tempo diperpendek 400/450
    // di v1.43 — pudaran belum selesai udah disambar frame baru jadi
    // kedip. Di sini: jepret 700ms, pudar 550ms — pudar SELALU selesai
    // sebelum frame berikutnya datang, dan lebih rapat dari v1.42).
    // Frame diambil dari TextureView cover pakai PixelCopy (async).
    var bingkaiMasuk by remember(item.key) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var bingkaiTampil by remember(item.key) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var bingkaiPudar by remember(item.key) { mutableStateOf<android.graphics.Bitmap?>(null) }
    val alphaPudar = remember { Animatable(0f) }
    LaunchedEffect(bingkaiMasuk) {
        val masuk = bingkaiMasuk ?: return@LaunchedEffect
        bingkaiPudar = masuk
        alphaPudar.snapTo(0f)
        alphaPudar.animateTo(1f, animationSpec = tween(550))
        bingkaiTampil = masuk
        bingkaiPudar = null
    }
    val penanganUtama = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    LaunchedEffect(item.key) {
        var lagiJepret = false
        while (true) {
            val tv = viewCover?.videoSurfaceView as? android.view.TextureView
            val tekstur = tv?.surfaceTexture
            if (!lagiJepret && tv != null && tekstur != null && tv.isAvailable &&
                (player.isPlaying || (bingkaiTampil == null && bingkaiMasuk == null)) &&
                tv.width > 0
            ) {
                val lebarKecil = 112
                val tinggiKecil = (lebarKecil * tv.height / tv.width).coerceIn(1, 200)
                val tujuan = android.graphics.Bitmap.createBitmap(
                    lebarKecil, tinggiKecil, android.graphics.Bitmap.Config.ARGB_8888,
                )
                lagiJepret = true
                val permukaan = android.view.Surface(tekstur)
                val gagal = runCatching {
                    android.view.PixelCopy.request(permukaan, null, tujuan, { hasil ->
                        if (hasil == android.view.PixelCopy.SUCCESS) {
                            bingkaiMasuk = android.graphics.Bitmap.createBitmap(tujuan)
                        }
                        permukaan.release()
                        lagiJepret = false
                    }, penanganUtama)
                }.isFailure
                if (gagal) {
                    permukaan.release()
                    lagiJepret = false
                }
            }
            delay(700)
        }
    }
    // === Kontrol disamakan PERSIS dgn pemutar audio (perintah user) ===
    var acakVideo by remember(item.key) { mutableStateOf(false) }
    var ulangVideo by remember(item.key) { mutableIntStateOf(0) }
    LaunchedEffect(ulangVideo) {
        player.repeatMode = if (ulangVideo == 2) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }
    LaunchedEffect(selesai) {
        if (selesai && ulangVideo == 1) {
            if (acakVideo) onLompatAcak() else if (adaNext) onNext()
        }
    }
    DisposableEffect(item.key) {
        onDispose { player.repeatMode = Player.REPEAT_MODE_OFF }
    }
    val audioManager = remember {
        contextMusik.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
    }
    val maksVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var volume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())
    }
    LaunchedEffect(posisiMs) {
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
    }
    val namaOutput = remember(item.key) {
        runCatching {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .firstOrNull {
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                }?.productName?.toString()?.takeIf { it.isNotBlank() } ?: "Speaker"
        }.getOrDefault("Speaker")
    }
    fun toastMode(teks: String) {
        android.widget.Toast.makeText(contextMusik, teks, android.widget.Toast.LENGTH_SHORT).show()
    }
    Box(Modifier.fillMaxSize().background(Color(0xFF101418))) {
        // Latar: frame jepretan diblur memenuhi layar, dua lapis pudar
        // silang; sebelum frame pertama siap pakai thumbnail Drive.
        val modifLatar = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = skalaLatar
                scaleY = skalaLatar
                if (Build.VERSION.SDK_INT >= 31) {
                    renderEffect = androidx.compose.ui.graphics.BlurEffect(
                        90f, 90f, androidx.compose.ui.graphics.TileMode.Clamp,
                    )
                }
            }
        val filterAmbient = ColorFilter.colorMatrix(
            ColorMatrix().apply { setToSaturation(1.7f) }
        )
        val tampilKini = bingkaiTampil
        val pudarKini = bingkaiPudar
        if (tampilKini != null) {
            Image(
                bitmap = tampilKini.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifLatar,
                colorFilter = filterAmbient,
            )
        }
        if (pudarKini != null) {
            Image(
                bitmap = pudarKini.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifLatar.graphicsLayer { alpha = alphaPudar.value },
                colorFilter = filterAmbient,
            )
        }
        if (tampilKini == null && pudarKini == null && item.thumbUrl.isNotBlank()) {
            AsyncImage(
                model = item.thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifLatar,
                colorFilter = filterAmbient,
            )
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
        // Kartu PAS DI TENGAH layar, PERSIS kayak pemutar audio
        // (perintah user 2026-11-10): dua tombol pojok atas DIHAPUS —
        // keluar mode musik = tombol Back (BackHandler modeMusik).
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            var menuLainVideo by remember { mutableStateOf(false) }
            var rincianVideo by remember { mutableStateOf(false) }
            KartuKacaPemutar { u ->
                // Kotak "cover" = video yg lagi muter
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(u * 24))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val pv = android.view.LayoutInflater.from(ctx)
                                .inflate(R.layout.player_view, null) as PlayerView
                            pv.player = player
                            pv.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            viewCover = pv
                            onDaftarView(pv)
                            pv
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.height(u * 52))
                // Judul + "Video" (kiri) • ikon gelombang (kanan) =
                // menu lama (acak / favorit / putar ulang / rincian);
                // visual tetap persis referensi tanpa titik tiga.
                BarisJudulPemutar(item.name, "Video", u) {
                    Box {
                        Box(
                            Modifier
                                .size(u * 64, u * 76)
                                .clickable { menuLainVideo = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            SpektrumBergerak(
                                modifier = Modifier.size(u * 36),
                                aktif = muter,
                                live = izinSpektrumVideo,
                                sesiId = player.audioSessionId,
                            )
                        }
                        DropdownMenu(
                            expanded = menuLainVideo,
                            onDismissRequest = { menuLainVideo = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Acak") },
                                leadingIcon = { Icon(Icons.Filled.Shuffle, null) },
                                trailingIcon = if (acakVideo) {
                                    { Icon(Icons.Filled.Check, null) }
                                } else null,
                                onClick = { acakVideo = !acakVideo; menuLainVideo = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Favorit") },
                                leadingIcon = {
                                    Icon(
                                        if (favorit) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        null,
                                    )
                                },
                                trailingIcon = if (favorit) {
                                    { Icon(Icons.Filled.Check, null) }
                                } else null,
                                onClick = { onToggleFavorite(); menuLainVideo = false },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Putar ulang: " + when (ulangVideo) {
                                            1 -> "Semua"
                                            2 -> "Satu"
                                            else -> "Mati"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (ulangVideo == 2) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                        null,
                                    )
                                },
                                onClick = { ulangVideo = (ulangVideo + 1) % 3; menuLainVideo = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Rincian") },
                                leadingIcon = { Icon(Icons.Filled.Info, null) },
                                onClick = { menuLainVideo = false; rincianVideo = true },
                            )
                        }
                    }
                }
                if (rincianVideo) {
                    AlertDialog(
                        onDismissRequest = { rincianVideo = false },
                        title = { Text("Rincian") },
                        text = {
                            Text(
                                buildString {
                                    append("Nama: ${item.name}\n")
                                    append("Jenis: Video\n")
                                    append("Durasi: ${formatDurasi(durasiMs)}")
                                    append(
                                        "\nSumber: " + if (item.akunDrive.isNotBlank()) {
                                            "Google Drive (${item.akunDrive})"
                                        } else {
                                            "Perangkat"
                                        }
                                    )
                                    if (item.dateMillis > 0) {
                                        append(
                                            "\nTanggal: " + java.text.SimpleDateFormat(
                                                "d MMM yyyy", java.util.Locale("id", "ID"),
                                            ).format(java.util.Date(item.dateMillis))
                                        )
                                    }
                                }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { rincianVideo = false }) { Text("Tutup") }
                        },
                    )
                }
                Spacer(Modifier.height(u * 48))
                // Bar durasi pil gemuk tanpa knob (sama persis pemutar audio)
                val durasiAmanMusik = durasiMs.coerceAtLeast(1)
                val posisiKiniMusik = geser ?: posisiMs.toFloat()
                // BarGeserTipis menyimpan callback komposisi PERTAMA —
                // rememberUpdatedState selalu nunjuk nilai terbaru.
                val geserKini by rememberUpdatedState(geser)
                val durasiKiniMusik by rememberUpdatedState(durasiAmanMusik)
                BarGeserTipis(
                    fraksi = (posisiKiniMusik / durasiAmanMusik).coerceIn(0f, 1f),
                    onGeser = { f -> onGeser(f * durasiKiniMusik) },
                    onSelesai = {
                        geserKini?.let { player.seekTo(it.toLong()) }
                        onGeser(null)
                    },
                    tinggi = u * 17,
                    knob = false,
                )
                Spacer(Modifier.height(u * 10))
                BarisWaktuPemutar(
                    formatDurasi(posisiKiniMusik.toLong()),
                    "-" + formatDurasi((durasiMs - posisiKiniMusik.toLong()).coerceAtLeast(0)),
                    u,
                )
                Spacer(Modifier.height(u * 26))
                // Transport PERSIS referensi: bintang • prev • jeda • next
                BarisTransportPemutar(
                    u = u,
                    favorit = favorit,
                    onFavorit = onToggleFavorite,
                    onPrev = {
                        if (posisiMs > 3000) player.seekTo(0)
                        else if (acakVideo) onLompatAcak() else onPrev()
                    },
                    onPlayPause = {
                        if (selesai) {
                            player.seekTo(0)
                            player.play()
                        } else if (muter) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    },
                    muter = muter,
                    onNext = { if (acakVideo) onLompatAcak() else onNext() },
                    prevAktif = adaPrev || acakVideo || posisiMs > 3000,
                    nextAktif = adaNext || acakVideo,
                )
                Spacer(Modifier.height(u * 62))
                // Bar volume pil tanpa knob, diapit ikon speaker
                BarisVolumePemutar(
                    u = u,
                    fraksi = if (maksVolume > 0) volume / maksVolume else 0f,
                    onGeser = { f ->
                        val vBaru = (f * maksVolume).toInt()
                        volume = vBaru.toFloat()
                        runCatching {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vBaru, 0)
                        }
                    },
                )
                Spacer(Modifier.height(u * 24))
                // Pil output ala AirPlay
                PillOutputPemutar(u, namaOutput) {
                    toastMode("Output suara: $namaOutput")
                }
            }
        }
    }
}

@Composable
private fun FastOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    // Persis screenshot Galeri Samsung asli user: pill bawah IKON SAJA,
    // tanpa label teks di bawah ikon.
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(56.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(25.dp),
        )
    }
}

private fun formatTanggalRincian(millis: Long): String =
    java.text.SimpleDateFormat("EEEE, dd MMMM yyyy · HH.mm", java.util.Locale("id", "ID"))
        .format(java.util.Date(millis))

private fun labelKualitas(w: Int, h: Int): String {
    val pendek = minOf(w, h)
    return when {
        pendek >= 2160 -> "UHD"
        pendek >= 1080 -> "FHD"
        pendek >= 720 -> "HD"
        else -> "SD"
    }
}

private fun jalurTampil(item: MediaItem): String {
    if (item.isDrive) {
        return "Google Drive" +
            if (item.akunDrive.isNotBlank()) " • ${item.akunDrive}" else ""
    }
    val akar = if (item.path.startsWith("/storage/") &&
        !item.path.startsWith("/storage/emulated/0")
    ) {
        "/Kartu SD"
    } else {
        "/Penyimpanan internal"
    }
    val rel = item.relativePath.trim().trimEnd('/')
    return if (rel.isEmpty()) akar else "$akar/$rel"
}

/** Codec video/audio + fps dari MediaExtractor (lokal saja; null kalau gagal). */
private fun bacaInfoCodec(
    context: android.content.Context,
    uri: Uri,
): Triple<String, String, String>? {
    val extractor = android.media.MediaExtractor()
    return try {
        extractor.setDataSource(context, uri, null)
        var video = ""
        var audio = ""
        var fps = ""
        for (i in 0 until extractor.trackCount) {
            val fmt = extractor.getTrackFormat(i)
            val mime = fmt.getString(android.media.MediaFormat.KEY_MIME) ?: continue
            when {
                mime.startsWith("video/") -> {
                    video = when (mime) {
                        "video/avc" -> "H.264"
                        "video/hevc" -> "H.265"
                        "video/av01" -> "AV1"
                        "video/x-vnd.on2.vp9" -> "VP9"
                        else -> mime.removePrefix("video/").uppercase()
                    }
                    if (fmt.containsKey(android.media.MediaFormat.KEY_FRAME_RATE)) {
                        val f = fmt.getInteger(android.media.MediaFormat.KEY_FRAME_RATE)
                        if (f > 0) fps = "${f}fps"
                    }
                }
                mime.startsWith("audio/") -> {
                    audio = when (mime) {
                        "audio/mp4a-latm" -> "AAC"
                        "audio/mpeg" -> "MP3"
                        "audio/opus" -> "Opus"
                        "audio/flac" -> "FLAC"
                        else -> mime.removePrefix("audio/").uppercase()
                    }
                }
            }
        }
        if (video.isEmpty() && audio.isEmpty()) null else Triple(video, audio, fps)
    } catch (e: Exception) {
        null
    } finally {
        extractor.release()
    }
}

/**
 * Panel rincian ala Samsung (screenshot user): tanggal tebal + Ubah,
 * nama & jalur abu, Info gambar/video + spek dipisah "|".
 * Chip "# Tambah tag" SENGAJA gak ikut — app ini gak punya sistem tag.
 */
@Composable
private fun PanelRincian(
    item: MediaItem,
    onGeserPanel: (Float) -> Unit,
    bacaGeserPanel: () -> Float,
    onSelesaiGeserPanel: () -> Unit,
    onUbah: () -> Unit,
) {
    val context = LocalContext.current
    var infoCodec by remember(item.key) { mutableStateOf<Triple<String, String, String>?>(null) }
    LaunchedEffect(item.key) {
        if (item.isVideo && !item.isDrive) {
            infoCodec = withContext(Dispatchers.IO) { bacaInfoCodec(context, item.uri) }
        }
    }
    val gulir = rememberScrollState()
    // Tarik panel mengikuti jari: sisa geser pas scroll mentok di atas
    // diteruskan ke fraksi panel secara LANGSUNG (onGeserPanel); seret ke
    // atas pas panel lagi ketarik dikonsumsi duluan buat mulihin posisi.
    // Pas gestur lepas, onSelesaiGeserPanel mutusin nutup / memantul.
    val koneksiTarik = object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (source == NestedScrollSource.UserInput && available.y < 0f) {
                val sisa = bacaGeserPanel()
                if (sisa > 0f) {
                    val pakai = minOf(-available.y, sisa)
                    onGeserPanel(-pakai)
                    return Offset(0f, -pakai)
                }
            }
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (source == NestedScrollSource.UserInput && available.y > 0f &&
                gulir.value == 0
            ) {
                onGeserPanel(available.y)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            onSelesaiGeserPanel()
            return super.onPreFling(available)
        }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity,
        ): Velocity {
            onSelesaiGeserPanel()
            return super.onPostFling(consumed, available)
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .nestedScroll(koneksiTarik),
    ) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(gulir)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                formatTanggalRincian(item.dateMillis),
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                "Ubah",
                color = Color(0xFF1976D2), fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onUbah),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            item.name, fontSize = 13.sp, color = Color(0xFF777777),
            maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
        Text(
            jalurTampil(item), fontSize = 13.sp, color = Color(0xFF777777),
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            if (item.isVideo) "Info video" else "Info gambar",
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        val info1 = buildList {
            add(formatUkuran(item.size))
            if (item.width > 0 && item.height > 0) {
                add("${item.width}x${item.height}")
                if (item.isVideo) {
                    add(labelKualitas(item.width, item.height))
                } else {
                    val mp = (item.width.toLong() * item.height + 500_000L) / 1_000_000L
                    add("${mp}MP")
                }
            }
        }
        Text(
            info1.joinToString("  |  "),
            fontSize = 13.sp, color = Color(0xFF777777),
        )
        if (item.isVideo) {
            val info2 = buildList {
                if (item.durationMs > 0) add(formatDurasi(item.durationMs))
                infoCodec?.let { (v, a, f) ->
                    if (v.isNotEmpty()) add(v)
                    if (a.isNotEmpty()) add(a)
                    if (f.isNotEmpty()) add(f)
                }
            }
            if (info2.isNotEmpty()) {
                Text(
                    info2.joinToString("  |  "),
                    fontSize = 13.sp, color = Color(0xFF777777),
                )
            }
        }
    }
    }
}

@Composable
fun ViewerScreen(
    items: List<MediaItem>,
    startIndex: Int,
    isFavorite: (MediaItem) -> Boolean,
    onToggleFavorite: (MediaItem) -> Unit,
    onDelete: (MediaItem) -> Unit,
    onBack: () -> Unit,
    onGantiNama: (MediaItem, String) -> Unit = { _, _ -> },
    videoSedangDiLatar: Boolean = false,
    onSerahkanVideo: (List<MediaItem>, MediaItem, Long) -> Unit = { _, _, _ -> },
    onAmbilAlihVideo: () -> Pair<String, Long>? = { null },
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))) { items.size }
    var barsVisible by remember { mutableStateOf(true) }
    var rincianTerbuka by remember { mutableStateOf(false) }
    var itemGantiNama by remember { mutableStateOf<MediaItem?>(null) }
    var modeMusikVideo by remember { mutableStateOf(false) }
    var videoPlayerPenuh by remember { mutableStateOf(false) }
    // Reset orientasi paksa + system bar SEKALI pas keluar penampil
    // (dulu per halaman video — halaman yg ke-dispose di tengah sesi
    // ngelepas orientasi & bikin tombol putar layar kayak gak jalan).
    val activityVS = LocalContext.current as? android.app.Activity
    DisposableEffect(Unit) {
        onDispose {
            activityVS?.requestedOrientation =
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activityVS?.window?.let { w ->
                val c = androidx.core.view.WindowCompat.getInsetsController(w, w.decorView)
                c.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    // Tombol PUTAR (foto + pemutar video): memutar LAYAR (orientasi
    // aktivitas) — target dibaca dari konfigurasi AKTUAL pas ditekan +
    // Toast konfirmasi biar user bisa verifikasi tombolnya kepencet &
    // arah putarnya (diagnostik laporan "tidak merespon" 2026-11-07).
    fun putarLayarVS() {
        val a = activityVS
        if (a == null) {
            android.widget.Toast.makeText(
                context, "Putar layar: aktivitas tidak ketemu",
                android.widget.Toast.LENGTH_SHORT,
            ).show()
            return
        }
        val lanskap = a.resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE
        a.requestedOrientation = if (lanskap) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        android.widget.Toast.makeText(
            context,
            if (lanskap) "Putar layar: ke potret" else "Putar layar: ke lanskap",
            android.widget.Toast.LENGTH_SHORT,
        ).show()
    }
    var sambungan by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var kunciPager by remember { mutableStateOf(false) }
    val current = items.getOrNull(pagerState.currentPage)
    val scope = rememberCoroutineScope()
    // Balik ke aplikasi pas video lagi dioper ke latar: ambil alih —
    // lompat ke video yg lagi jalan di layanan; VideoPage-nya yg
    // nerusin posisi lewat [sambungan].
    val pemilikHidupVS = LocalLifecycleOwner.current
    DisposableEffect(pemilikHidupVS, videoSedangDiLatar) {
        val pengamat = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && videoSedangDiLatar) {
                val hasil = onAmbilAlihVideo()
                if (hasil != null) {
                    sambungan = hasil
                    val idx = items.indexOfFirst { it.key == hasil.first }
                    if (idx >= 0 && idx != pagerState.currentPage) {
                        scope.launch { pagerState.scrollToPage(idx) }
                    }
                }
            }
        }
        pemilikHidupVS.lifecycle.addObserver(pengamat)
        onDispose { pemilikHidupVS.lifecycle.removeObserver(pengamat) }
    }
    // --- TARIK KE BAWAH BUAT KEMBALI (pull-to-dismiss, ala galeri Samsung/iOS):
    // foto digeser turun + mengecil ngikutin jari; lepas di atas ambang ->
    // kembali; di bawah ambang -> spring balik. Cuma pas foto TIDAK di-zoom.
    val tarik = remember { Animatable(0f) }
    var skalaKini by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(pagerState.currentPage) {
        if (tarik.value != 0f) tarik.snapTo(0f)
        if (items.getOrNull(pagerState.currentPage)?.isVideo != true) barsVisible = true
    }

    // Panel rincian: media menyusut ke atas, panel naik dari bawah. Fraksi
    // digerakkan Animatable (buka/tutup) MINUS geserPanelPx (tarikan jari
    // di mode rincian) — jadi panel NGIKUTIN JARI pas ditarik, bukan diem
    // terus respon pas dilepas (teguran user 2026-11-07).
    val fraksiPanelAnim = remember { Animatable(0f) }
    var geserPanelPx by remember { mutableFloatStateOf(0f) }
    var tinggiLayarPx by remember { mutableIntStateOf(1) }
    var lagiSettle by remember { mutableStateOf(false) }
    LaunchedEffect(rincianTerbuka) {
        if (!lagiSettle) {
            fraksiPanelAnim.animateTo(
                if (rincianTerbuka) 0.52f else 0f,
                tween(durationMillis = 340, easing = FastOutSlowInEasing),
            )
        }
    }
    fun fraksiEfektif() = (fraksiPanelAnim.value -
        geserPanelPx / tinggiLayarPx.coerceAtLeast(1)).coerceIn(0f, 0.52f)
    val fraksiPanel = fraksiEfektif()
    fun selesaikanGeserPanel() {
        val efektif = fraksiEfektif()
        if (efektif < 0.30f) {
            lagiSettle = true
            rincianTerbuka = false
            scope.launch {
                fraksiPanelAnim.snapTo(efektif)
                geserPanelPx = 0f
                fraksiPanelAnim.animateTo(
                    0f,
                    tween(durationMillis = 280, easing = FastOutSlowInEasing),
                )
                lagiSettle = false
            }
        } else {
            scope.launch {
                animate(
                    geserPanelPx, 0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
                ) { v, _ -> geserPanelPx = v }
            }
        }
    }
    fun selesaikanBukaPanel() {
        val efektif = fraksiEfektif()
        if (efektif > 0.16f) {
            lagiSettle = true
            rincianTerbuka = true
            scope.launch {
                fraksiPanelAnim.snapTo(efektif)
                geserPanelPx = 0f
                fraksiPanelAnim.animateTo(
                    0.52f,
                    tween(durationMillis = 280, easing = FastOutSlowInEasing),
                )
                lagiSettle = false
            }
        } else {
            scope.launch {
                animate(
                    geserPanelPx, 0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
                ) { v, _ -> geserPanelPx = v }
            }
        }
    }
    val backdropMedia = rememberLayerBackdrop()
    BackHandler(enabled = rincianTerbuka) { rincianTerbuka = false }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { tinggiLayarPx = it.height },
    ) {
      CompositionLocalProvider(LocalKacaMedia provides backdropMedia) {
      Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .fillMaxHeight(1f - fraksiPanel)
            .graphicsLayer {
                translationY = tarik.value
                val s = 1f - (tarik.value / 2400f).coerceIn(0f, 0.16f)
                scaleX = s
                scaleY = s
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        when {
                            rincianTerbuka -> selesaikanGeserPanel()
                            geserPanelPx < 0f -> selesaikanBukaPanel()
                            tarik.value > 300f -> onBack()
                            else -> {
                                scope.launch {
                                    tarik.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 380f))
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        when {
                            rincianTerbuka -> selesaikanGeserPanel()
                            geserPanelPx < 0f -> selesaikanBukaPanel()
                            else -> {
                                scope.launch {
                                    tarik.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 380f))
                                }
                            }
                        }
                    },
                ) { change, jumlah ->
                    if (rincianTerbuka) {
                        // Mode rincian: seret di area media ngeser panel
                        // LANGSUNG ngikutin jari (dua arah)
                        change.consume()
                        geserPanelPx = (geserPanelPx + jumlah).coerceAtLeast(0f)
                    } else {
                        val itemKini = items.getOrNull(pagerState.currentPage)
                        val bolehSeret = skalaKini <= 1.01f &&
                            (itemKini?.isVideo != true || !videoPlayerPenuh)
                        if (bolehSeret) {
                            change.consume()
                            if (geserPanelPx < 0f || (jumlah < 0f && tarik.value <= 0f)) {
                                // Usap ke atas: panel rincian kebuka
                                // NGIKUTIN JARI (geser negatif); seret
                                // balik ke bawah mulihin ke 0 dulu sebelum
                                // pull-to-dismiss ngambil alih.
                                geserPanelPx = (geserPanelPx + jumlah)
                                    .coerceIn(-0.52f * tinggiLayarPx, 0f)
                            } else {
                                scope.launch {
                                    tarik.snapTo((tarik.value + jumlah).coerceAtLeast(0f))
                                }
                            }
                        }
                    }
                }
            },
      ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().layerBackdrop(backdropMedia),
            userScrollEnabled = !modeMusikVideo && !kunciPager,
        ) { page ->
            val item = items[page]
            if (item.isVideo) {
                VideoPage(
                    item = item,
                    isCurrent = page == pagerState.currentPage,
                    favorit = isFavorite(item),
                    onToggleFavorite = { onToggleFavorite(item) },
                    onInfo = { rincianTerbuka = true },
                    tampilBerbagi = !item.isDrive,
                    onShare = { shareItems(context, listOf(item)) },
                    onDelete = { onDelete(item) },
                    adaPrev = page > 0,
                    adaNext = page < items.size - 1,
                    onPrev = { scope.launch { pagerState.animateScrollToPage(page - 1) } },
                    onNext = { scope.launch { pagerState.animateScrollToPage(page + 1) } },
                    onLompatAcak = {
                        val kandidat = items.indices.filter { items[it].isVideo && it != page }
                        if (kandidat.isNotEmpty()) {
                            scope.launch { pagerState.animateScrollToPage(kandidat.random()) }
                        }
                    },
                    modeMusik = modeMusikVideo,
                    onModeMusik = { modeMusikVideo = it },
                    onSerahkanKeLatar = { pos ->
                        onSerahkanVideo(items.filter { it.isVideo }, item, pos)
                    },
                    sambungan = sambungan,
                    onSambunganTerpakai = { sambungan = null },
                    onKunciPager = { kunciPager = it },
                    panelTerbuka = rincianTerbuka,
                    onBarTampil = { barsVisible = it },
                    onPlayerPenuh = { videoPlayerPenuh = it },
                    onPutarLayar = { putarLayarVS() },
                )
            } else {
                ZoomableImage(
                    item = item,
                    onTap = { barsVisible = !barsVisible },
                    aktif = page == pagerState.currentPage,
                    onSkalaBerubah = { skalaKini = it },
                    onPindahFoto = { arah ->
                        val tujuan = page + arah
                        if (tujuan in items.indices) {
                            scope.launch { pagerState.animateScrollToPage(tujuan) }
                        }
                    },
                )
            }
        }
      }

        // Panel rincian naik dari bawah pas fraksi > 0 (geser ke atas /
        // tombol Rincian); isinya item yg lagi tampil.
        if (fraksiPanel > 0.01f) {
            current?.let { item ->
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(fraksiPanel)
                        .graphicsLayer {
                            alpha = (fraksiPanel / 0.52f).coerceIn(0f, 1f)
                        },
                ) {
                    PanelRincian(
                        item = item,
                        onGeserPanel = { delta ->
                            geserPanelPx = (geserPanelPx + delta).coerceAtLeast(0f)
                        },
                        bacaGeserPanel = { geserPanelPx },
                        onSelesaiGeserPanel = { selesaikanGeserPanel() },
                        onUbah = { itemGantiNama = item },
                    )
                    // Dialog UBAH NAMA berkas (tombol Ubah di Rincian) —
                    // ekstensi dipertahankan, yg diedit nama dasarnya saja.
                    itemGantiNama?.let { target ->
                        val ekstensi = target.name.substringAfterLast('.', "").let {
                            if (it.isNotEmpty() && it.length <= 5) ".$it" else ""
                        }
                        var teksNama by remember(target.key) {
                            mutableStateOf(target.name.removeSuffix(ekstensi))
                        }
                        AlertDialog(
                            onDismissRequest = { itemGantiNama = null },
                            title = { Text("Ubah nama berkas") },
                            text = {
                                OutlinedTextField(
                                    value = teksNama,
                                    onValueChange = { teksNama = it },
                                    singleLine = true,
                                    label = { Text("Nama berkas") },
                                    suffix = if (ekstensi.isNotEmpty()) {
                                        { Text(ekstensi) }
                                    } else null,
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val basis = teksNama.trim()
                                    if (basis.isNotEmpty() &&
                                        !basis.contains('/') && !basis.contains('\\')
                                    ) {
                                        onGantiNama(target, basis + ekstensi)
                                        itemGantiNama = null
                                    }
                                }) { Text("Simpan") }
                            },
                            dismissButton = {
                                TextButton(onClick = { itemGantiNama = null }) {
                                    Text("Batal")
                                }
                            },
                        )
                    }
                }
            }
        }

        // Bar melayang bersama HANYA buat foto — video punya kontrol sendiri
        // ala pemutar Samsung (di dalam VideoPage). Muncul/hilangnya bar
        // dianimasikan (perintah user: tap sekali -> bar hilang smooth).
        if (!rincianTerbuka && (current?.isVideo != true || !videoPlayerPenuh)) {
            // Tombol kembali melayang bulat — persis screenshot Samsung asli user
            AnimatedVisibility(
                visible = barsVisible,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(180)),
                modifier = Modifier.align(Alignment.TopStart),
            ) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 14.dp, top = 10.dp)
                    .kacaCairHitam(CircleShape, bayangan = 10.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, "Kembali",
                        tint = Color.White,
                    )
                }
            }
            }

            // Pill bawah melayang, IKON SAJA — urutan Samsung:
            // Favorit · Ubah · Rincian · Berbagi · Hapus
            current?.let { item ->
                AnimatedVisibility(
                    visible = barsVisible,
                    enter = fadeIn(tween(200)) +
                        slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it / 2 },
                    exit = fadeOut(tween(180)) +
                        slideOutVertically(tween(240)) { it / 2 },
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 10.dp)
                        .kacaCairHitam(RoundedCornerShape(50), bayangan = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FastOption(
                            icon = if (isFavorite(item)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            label = "Favorit",
                            onClick = { onToggleFavorite(item) },
                        )
                        if (!item.isDrive) {
                            FastOption(
                                icon = Icons.Filled.Edit,
                                label = "Ubah",
                                onClick = {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_EDIT).apply {
                                            setDataAndType(item.uri, item.mime.ifEmpty { "image/*" })
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Ubah dengan"))
                                    }
                                },
                            )
                        }
                        if (!item.isVideo) {
                            FastOption(
                                icon = Icons.Filled.ScreenRotation,
                                label = "Putar",
                                onClick = { putarLayarVS() },
                            )
                        }
                        FastOption(
                            icon = Icons.Filled.Info,
                            label = "Rincian",
                            onClick = { rincianTerbuka = true },
                        )
                        if (!item.isDrive) {
                            FastOption(
                                icon = Icons.Filled.Share,
                                label = "Berbagi",
                                onClick = { shareItems(context, listOf(item)) },
                            )
                        }
                        FastOption(
                            icon = Icons.Filled.Delete,
                            label = "Hapus",
                            onClick = { onDelete(item) },
                        )
                    }
                }
                }
            }
        }
        }
    }

}
