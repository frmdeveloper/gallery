package id.frmdeveloper.gallery

import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Job
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
    rotasi: Float,
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
                    rotationZ = rotasi,
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
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = modifier.size(ukuran),
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxSize()) {
            konten()
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
    // Lepas mode immersif + orientasi paksa pas halaman ditinggal
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { w ->
                val c = androidx.core.view.WindowCompat.getInsetsController(w, w.decorView)
                c.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    LaunchedEffect(isCurrent) {
        if (isCurrent) player.play() else player.pause()
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
        val a = activity ?: return
        val sekarang = context.resources.configuration.orientation
        a.requestedOrientation = if (sekarang == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
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


    Box(Modifier.fillMaxSize().background(Color.Black)) {
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
                onGeser = { geser = it },
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
            modifier = Modifier.fillMaxSize(),
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

        if (terkunci) {
            TombolKaca(
                onClick = { terkunci = false; kontrolVisible = true; sentuh() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 18.dp, bottom = 84.dp),
            ) {
                Icon(Icons.Filled.Lock, "Buka kunci", tint = Color.White, modifier = Modifier.size(19.dp))
            }
        } else if (kontrolVisible) {
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
                TombolKaca(onClick = { onPrev(); sentuh() }, ukuran = 46.dp, enabled = adaPrev) {
                    Icon(
                        Icons.Filled.SkipPrevious, "Sebelumnya",
                        tint = Color.White.copy(alpha = if (adaPrev) 1f else 0.35f),
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
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            onValueChange = { geser = it },
                            onValueChangeFinished = {
                                geser?.let { player.seekTo(it.toLong()) }
                                geser = null
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
    // Latar LIVE: player KEDUA memutar video yg sama (bisu, diulang),
    // dirender memenuhi layar + diburamkan. Cuplikan bitmap per 700ms
    // (v1.32) bikin lag — ini murni permukaan video, jauh lebih mulus.
    val contextMusik = LocalContext.current
    val playerLatar = remember(item.key) {
        val builder = ExoPlayer.Builder(contextMusik)
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
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            // Samakan posisi AWAL sebelum tampil — tanpa ini latar
            // mulai dari 0 & buffering sendiri, telat beberapa detik
            seekTo(player.currentPosition)
            playWhenReady = muter
        }
    }
    DisposableEffect(item.key) {
        onDispose { playerLatar.release() }
    }
    // Ikuti player utama: play/pause sama; drift kecil digeser halus
    // via kecepatan ±8% (nggak kelihatan di latar buram), drift besar
    // di-seek. v1.33 longgar bgt (cek 1,5 dtk, toleransi 1,2 dtk)
    // makanya latar telat sampai ±3 detik.
    LaunchedEffect(Unit) {
        while (true) {
            if (muter && !selesai) {
                if (!playerLatar.isPlaying) playerLatar.play()
                val drift = playerLatar.currentPosition - player.currentPosition
                when {
                    drift > 600 || drift < -600 -> {
                        playerLatar.setPlaybackSpeed(1f)
                        playerLatar.seekTo(player.currentPosition)
                    }
                    drift > 80 -> playerLatar.setPlaybackSpeed(0.92f)
                    drift < -80 -> playerLatar.setPlaybackSpeed(1.08f)
                    else -> playerLatar.setPlaybackSpeed(1f)
                }
            } else if (playerLatar.isPlaying) {
                playerLatar.pause()
            }
            delay(300)
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
        // Latar: video asli diputar player latar, penuhi layar + buram
        AndroidView(
            factory = { ctx ->
                val pv = android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.player_view, null) as PlayerView
                pv.player = playerLatar
                pv.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                pv
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.6f
                    scaleY = 1.6f
                    if (Build.VERSION.SDK_INT >= 31) {
                        renderEffect = androidx.compose.ui.graphics.BlurEffect(
                            90f, 90f, androidx.compose.ui.graphics.TileMode.Clamp,
                        )
                    }
                },
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onKeluar) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown, "Kembali ke pemutar video",
                        tint = Color.White, modifier = Modifier.size(30.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { toastMode("Antrean putar = daftar video yang dibuka") }) {
                    Icon(Icons.Filled.QueueMusic, "Antrean", tint = Color.White.copy(alpha = 0.85f))
                }
            }
            Spacer(Modifier.weight(0.85f))
            // Kartu kaca ala pemutar musik
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.34f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.13f)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 34.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Kotak "cover" = video yg lagi muter
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center,
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val pv = android.view.LayoutInflater.from(ctx)
                                    .inflate(R.layout.player_view, null) as PlayerView
                                pv.player = player
                                pv.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                onDaftarView(pv)
                                pv
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.name, fontSize = 19.sp, fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "Video", fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.55f), maxLines = 1,
                            )
                        }
                        IconButton(onClick = { toastMode("Lirik belum tersedia") }) {
                            Icon(
                                Icons.Filled.GraphicEq, "Lirik",
                                tint = Color.White.copy(alpha = 0.75f),
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    // Seekbar TIPIS yg sama persis kayak pemutar audio
                    val durasiAmanMusik = durasiMs.coerceAtLeast(1)
                    val posisiKiniMusik = geser ?: posisiMs.toFloat()
                    BarGeserTipis(
                        fraksi = (posisiKiniMusik / durasiAmanMusik).coerceIn(0f, 1f),
                        onGeser = { f -> onGeser(f * durasiAmanMusik) },
                        onSelesai = {
                            geser?.let { player.seekTo(it.toLong()) }
                            onGeser(null)
                        },
                        tinggi = 5.dp,
                        knob = true,
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            formatDurasi(posisiKiniMusik.toLong()),
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "-" + formatDurasi((durasiMs - posisiKiniMusik.toLong()).coerceAtLeast(0)),
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    // Transport PERSIS pemutar audio: acak • bintang • ⏮ • ⏯ GEDE • ⏭ • ulang
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(onClick = { acakVideo = !acakVideo }, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.Filled.Shuffle, "Acak",
                                tint = if (acakVideo) Color.White else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(44.dp)) {
                            Icon(
                                if (favorit) Icons.Filled.Star else Icons.Filled.StarBorder,
                                "Favorit",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        IconButton(
                            onClick = { if (acakVideo) onLompatAcak() else onPrev() },
                            enabled = adaPrev || acakVideo,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                Icons.Filled.SkipPrevious, "Sebelumnya",
                                tint = Color.White.copy(alpha = if (adaPrev || acakVideo) 1f else 0.35f),
                                modifier = Modifier.size(40.dp),
                            )
                        }
                        IconButton(
                            onClick = {
                                if (selesai) {
                                    player.seekTo(0)
                                    player.play()
                                } else if (muter) {
                                    player.pause()
                                } else {
                                    player.play()
                                }
                            },
                            modifier = Modifier.size(72.dp),
                        ) {
                            Icon(
                                if (muter) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                if (muter) "Jeda" else "Putar",
                                tint = Color.White, modifier = Modifier.size(56.dp),
                            )
                        }
                        IconButton(
                            onClick = { if (acakVideo) onLompatAcak() else onNext() },
                            enabled = adaNext || acakVideo,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                Icons.Filled.SkipNext, "Berikutnya",
                                tint = Color.White.copy(alpha = if (adaNext || acakVideo) 1f else 0.35f),
                                modifier = Modifier.size(40.dp),
                            )
                        }
                        IconButton(onClick = { ulangVideo = (ulangVideo + 1) % 3 }, modifier = Modifier.size(40.dp)) {
                            Icon(
                                if (ulangVideo == 2) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                "Ulang",
                                tint = if (ulangVideo > 0) Color.White else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    // Bar volume GEMUK ala iOS (sama kayak pemutar audio)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.VolumeMute, null,
                            tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp),
                        )
                        Box(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            BarGeserTipis(
                                fraksi = if (maksVolume > 0) volume / maksVolume else 0f,
                                onGeser = { f ->
                                    val vBaru = (f * maksVolume).toInt()
                                    volume = vBaru.toFloat()
                                    runCatching {
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vBaru, 0)
                                    }
                                },
                                onSelesai = {},
                                tinggi = 9.dp,
                                knob = false,
                            )
                        }
                        Icon(
                            Icons.Filled.VolumeUp, null,
                            tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    // Pill output ala AirPlay (sama kayak pemutar audio)
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier.clickable {
                            toastMode("Output suara: $namaOutput")
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Cast, null,
                                tint = Color.White, modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(namaOutput, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
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
        Icon(icon, contentDescription = label, modifier = Modifier.size(25.dp))
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
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))) { items.size }
    var barsVisible by remember { mutableStateOf(true) }
    var infoItem by remember { mutableStateOf<MediaItem?>(null) }
    var modeMusikVideo by remember { mutableStateOf(false) }
    val current = items.getOrNull(pagerState.currentPage)
    var rotasi by remember(current?.key) { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    // --- TARIK KE BAWAH BUAT KEMBALI (pull-to-dismiss, ala galeri Samsung/iOS):
    // foto digeser turun + mengecil ngikutin jari; lepas di atas ambang ->
    // kembali; di bawah ambang -> spring balik. Cuma pas foto TIDAK di-zoom.
    val tarik = remember { Animatable(0f) }
    var skalaKini by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(pagerState.currentPage) {
        if (tarik.value != 0f) tarik.snapTo(0f)
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
      Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = tarik.value
                val s = 1f - (tarik.value / 2400f).coerceIn(0f, 0.16f)
                scaleX = s
                scaleY = s
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (tarik.value > 300f) {
                            onBack()
                        } else {
                            scope.launch {
                                tarik.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 380f))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            tarik.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 380f))
                        }
                    },
                ) { change, jumlah ->
                    val itemKini = items.getOrNull(pagerState.currentPage)
                    if (skalaKini <= 1.01f && itemKini?.isVideo != true) {
                        change.consume()
                        scope.launch { tarik.snapTo((tarik.value + jumlah).coerceAtLeast(0f)) }
                    }
                }
            },
      ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val item = items[page]
            if (item.isVideo) {
                VideoPage(
                    item = item,
                    isCurrent = page == pagerState.currentPage,
                    favorit = isFavorite(item),
                    onToggleFavorite = { onToggleFavorite(item) },
                    onInfo = { infoItem = item },
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
                )
            } else {
                ZoomableImage(
                    item = item,
                    rotasi = if (page == pagerState.currentPage) rotasi else 0f,
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

        // Bar melayang bersama HANYA buat foto — video punya kontrol sendiri
        // ala pemutar Samsung (di dalam VideoPage).
        if (barsVisible && current?.isVideo != true) {
            // Tombol kembali melayang bulat — persis screenshot Samsung asli user
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 14.dp, top = 10.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Pill bawah melayang, IKON SAJA — urutan Samsung:
            // Favorit · Ubah · Rincian · Berbagi · Hapus
            current?.let { item ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 30.dp),
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
                        FastOption(
                            icon = Icons.Filled.Refresh,
                            label = "Putar",
                            onClick = { rotasi += 90f },
                        )
                        FastOption(
                            icon = Icons.Filled.Info,
                            label = "Rincian",
                            onClick = { infoItem = item },
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

    infoItem?.let { item ->
        AlertDialog(
            onDismissRequest = { infoItem = null },
            title = { Text("Rincian") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nama: ${item.name}")
                    Text("Tanggal: ${formatTanggalSingkat(item.dateMillis)}")
                    Text("Ukuran: ${formatUkuran(item.size)}")
                    if (item.width > 0 && item.height > 0) Text("Dimensi: ${item.width} × ${item.height}")
                    if (item.isVideo && item.durationMs > 0) Text("Durasi: ${formatDurasi(item.durationMs)}")
                    Text("Jenis: ${item.mime.ifEmpty { "-" }}")
                    Text(
                        "Lokasi: " + if (item.isDrive) "Google Drive" else item.relativePath.ifEmpty { "-" }
                    )
                }
            },
            confirmButton = { TextButton(onClick = { infoItem = null }) { Text("Oke") } },
        )
    }
}
