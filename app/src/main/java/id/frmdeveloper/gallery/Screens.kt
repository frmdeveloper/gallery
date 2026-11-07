package id.frmdeveloper.gallery

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.geometry.Rect
import androidx.compose.material.icons.filled.RotateRight
import android.graphics.BitmapFactory
import kotlin.math.roundToInt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.border
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import coil.size.Precision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import coil.request.videoFrameMillis
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.filled.Storage
import android.content.ClipDescription
import android.view.View
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.fillMaxHeight

// ---------- Helper berbagi ----------

fun shareItems(context: Context, items: List<MediaItem>) {
    if (items.isEmpty()) return
    val intent: Intent
    if (items.size == 1) {
        val item = items.first()
        intent = Intent(Intent.ACTION_SEND).apply {
            type = item.mime.ifEmpty { "*/*" }
            putExtra(Intent.EXTRA_STREAM, item.uri)
        }
    } else {
        intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(items.map { it.uri }))
        }
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val clip = ClipData.newUri(context.contentResolver, "media", items.first().uri)
    items.drop(1).forEach { clip.addItem(ClipData.Item(it.uri)) }
    intent.clipData = clip
    context.startActivity(Intent.createChooser(intent, "Bagikan").apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    })
}

/** Bagikan file audio LOKAL (lagu Drive tidak bisa dibagikan sebagai berkas). */
fun shareLagu(context: Context, lagu: List<AudioItem>) {
    val lokal = lagu.filter { it.akunDrive.isBlank() }
    if (lokal.isEmpty()) return
    val uris = ArrayList(lokal.map { it.uri })
    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uris.first())
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "audio/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val clip = ClipData.newUri(context.contentResolver, "lagu", uris.first())
    uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
    intent.clipData = clip
    context.startActivity(Intent.createChooser(intent, "Bagikan").apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    })
}

// ---------- Grid media (dipakai Gambar, Album, Favorit) ----------

private sealed interface GridEntry {
    data class Header(val text: String) : GridEntry
    data class Cell(val item: MediaItem) : GridEntry
}

/** Tingkat cubit ala Samsung: 0=2 kolom, 1=3, 2=5, 3=7 per-bulan, 4=10 per-tahun */
fun kolomUntukLevel(level: Int): Int = when (level) {
    0 -> 2; 1 -> 3; 2 -> 5; 3 -> 7; else -> 10
}

/**
 * Detektor cubit SENDIRI yg jalan di PointerEventPass.Initial — ngintip event
 * SEBELUM handler scroll grid & klik cell mengonsumsinya.
 * (Versi lama pakai detectTransformGestures di modifier grid yg sama: event
 * keburu dimakan scroll handler, callback zoom gak pernah kepanggil di HP.)
 * zoomIn=true  -> jari melebar (thumbnail membesar, kolom berkurang)
 * zoomIn=false -> jari menciut (thumbnail mengecil, kolom bertambah)
 * Satu gesture cuma memicu SATU kali ganti tingkat.
 */
private suspend fun PointerInputScope.detectCubitGrid(onCubit: (zoomIn: Boolean) -> Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var jarakAwal = 0f
        var sudahMemicu = false
        var selesai = false
        while (!selesai) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val ditekan = event.changes.filter { it.pressed }
            if (ditekan.size >= 2) {
                val jarak = (ditekan[0].position - ditekan[1].position).getDistance()
                if (jarakAwal == 0f) {
                    jarakAwal = jarak
                } else if (!sudahMemicu) {
                    val rasio = jarak / jarakAwal
                    if (rasio > 1.22f) { onCubit(true); sudahMemicu = true }
                    else if (rasio < 0.80f) { onCubit(false); sudahMemicu = true }
                }
                // cegah grid ikut scroll selama lagi cubit
                event.changes.forEach { if (it.positionChanged()) it.consume() }
            } else {
                jarakAwal = 0f
                sudahMemicu = false
            }
            selesai = event.changes.none { it.pressed }
        }
    }
}

/**
 * Animasi zoom MURAH ala grid Gambar: pas [kunci] berubah, seluruh wadah
 * di-snap sedikit lalu spring balik ke 1f. Dipakai grid Album + folder
 * Audio — dulu keduanya ganti kolom INSTAN tanpa animasi (terasa kaku).
 */
@Composable
fun Modifier.animasiZoom(kunci: Any, membesar: Boolean): Modifier {
    val skala = remember { Animatable(1f) }
    var sebelum by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(kunci) {
        if (sebelum != null && sebelum != kunci) {
            skala.snapTo(if (membesar) 0.93f else 1.07f)
            skala.animateTo(1f, spring(dampingRatio = 0.86f, stiffness = 400f))
        }
        sebelum = kunci
    }
    return this.graphicsLayer { scaleX = skala.value; scaleY = skala.value }
}


/**
 * Simpan & pulihkan posisi scroll ke SharedPreferences — SENGAJA tidak
 * pakai rememberSaveable: cara itu (v1.37) ke-reset di HP user (transisi
 * layar AnimatedContent + data yg dibangun belakangan). Cara manual ini
 * kebal komposisi: pulihkan begitu data siap, simpan tiap berubah.
 * Panggil dari DALAM cabang mode yg aktif biar scrollToItem cuma jalan
 * pas daftarnya beneran tampil.
 */
@Composable
private fun IngatPosisiGrid(
    state: LazyGridState,
    kunci: String,
    jumlah: Int,
    aktif: Boolean = true,
) {
    val ctx = LocalContext.current
    val aktifKini by rememberUpdatedState(aktif)
    var sudahPulih by remember(kunci) { mutableStateOf(false) }
    LaunchedEffect(jumlah, kunci) {
        if (!sudahPulih && jumlah > 0) {
            val idx = UiStore.loadInt(ctx, "pos_i_" + kunci, 0)
            val off = UiStore.loadInt(ctx, "pos_o_" + kunci, 0)
            if (idx > 0) state.scrollToItem(minOf(idx, jumlah - 1), off)
            sudahPulih = true
        }
    }
    LaunchedEffect(state, kunci) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (i, o) ->
                if (sudahPulih && aktifKini) {
                    delay(120)
                    UiStore.saveInt(ctx, "pos_i_" + kunci, i)
                    UiStore.saveInt(ctx, "pos_o_" + kunci, o)
                }
            }
    }
}

@Composable
private fun IngatPosisiList(
    state: LazyListState,
    kunci: String,
    jumlah: Int,
    aktif: Boolean = true,
) {
    val ctx = LocalContext.current
    val aktifKini by rememberUpdatedState(aktif)
    var sudahPulih by remember(kunci) { mutableStateOf(false) }
    LaunchedEffect(jumlah, kunci) {
        if (!sudahPulih && jumlah > 0) {
            val idx = UiStore.loadInt(ctx, "pos_i_" + kunci, 0)
            val off = UiStore.loadInt(ctx, "pos_o_" + kunci, 0)
            if (idx > 0) state.scrollToItem(minOf(idx, jumlah - 1), off)
            sudahPulih = true
        }
    }
    LaunchedEffect(state, kunci) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (i, o) ->
                if (sudahPulih && aktifKini) {
                    delay(120)
                    UiStore.saveInt(ctx, "pos_i_" + kunci, i)
                    UiStore.saveInt(ctx, "pos_o_" + kunci, o)
                }
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    items: List<MediaItem>,
    level: Int,
    onLevelChange: (Int) -> Unit,
    selection: Set<String>,
    selecting: Boolean,
    isFavorite: (MediaItem) -> Boolean,
    onItemClick: (MediaItem) -> Unit,
    onItemLongClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    kunciPosisi: String = "grid",
    tanpaTanggal: Boolean = false,
    onMulaiSeret: (List<MediaItem>) -> Unit = {},
) {
    val context = LocalContext.current
    // Grouping ribuan item DIBANGUN DI BACKGROUND (Dispatchers.Default), bukan
    // di main thread pas komposisi — versi lama nge-block UI tiap items/level
    // berubah (sumber lag parah v1.3 pas Drive ikut kegabung). Entries lama
    // dipertahankan selama yg baru dibangun, jadi grid gak kedip kosong.
    var entries by remember { mutableStateOf<List<GridEntry>>(emptyList()) }
    LaunchedEffect(items, level, tanpaTanggal) {
        val dibangun = withContext(Dispatchers.Default) {
            // Di dalam album: TANPA header tanggal, semua foto campur rata
            // dalam satu grid (perintah user)
            if (tanpaTanggal) return@withContext items.map { GridEntry.Cell(it) }
            val groups: Map<String, List<MediaItem>> = when (level) {
                3 -> items.groupBy { formatBulan(it.dateMillis) }
                4 -> items.groupBy { formatTahun(it.dateMillis) }
                else -> items.groupBy { formatTanggalLengkap(it.dateMillis) }
            }
            val list = mutableListOf<GridEntry>()
            for ((judul, grup) in groups) {
                list += GridEntry.Header(judul)
                grup.forEach { list += GridEntry.Cell(it) }
            }
            list
        }
        entries = dibangun
    }
    // Animasi zoom = TIAP SEL beranimasi ukuran+posisi (animateItem di sel,
    // sama kayak Album sejak v1.16) — permintaan user: jangan cuma skala
    // wadahnya doang ("gak bergeser ukuran"). Dulu skala wadah dipilih demi
    // hemat, tapi thumbnail sekarang sudah di-decode seukuran sel jadi
    // animateItem di sel yg TAMPAK saja tetap ringan.
    val levelKini by rememberUpdatedState(level)
    val onLevelChangeKini by rememberUpdatedState(onLevelChange)
    // Ukuran decode thumbnail = selebar sel grid. Dulu request TANPA size ->
    // Coil men-decode foto resolusi PENUH buat tiap thumbnail kecil = lag berat
    // di HP. Sekarang decode persis seukuran sel.
    val kolomKini = kolomUntukLevel(level)
    val konfigurasi = LocalConfiguration.current
    val kepadatan = LocalDensity.current
    val ukuranThumbPx = remember(konfigurasi.screenWidthDp, kolomKini) {
        with(kepadatan) { (konfigurasi.screenWidthDp.toFloat() / kolomKini).dp.roundToPx() }
            .coerceIn(96, 720)
    }
    val statusGrid = rememberLazyGridState()
    IngatPosisiGrid(statusGrid, "grid_" + kunciPosisi, entries.size)
    LazyVerticalGrid(
        state = statusGrid,
        columns = GridCells.Fixed(kolomUntukLevel(level)),
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectCubitGrid { zoomIn ->
                    onLevelChangeKini(
                        if (zoomIn) (levelKini - 1).coerceAtLeast(0)
                        else (levelKini + 1).coerceAtMost(4)
                    )
                }
            },
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        items(
            entries,
            // key stabil: tanpa ini tiap recompose grid menyusun ulang semua sel
            // (sumber lag kedua). contentType bantu daur ulang komposisi.
            key = { entry ->
                when (entry) {
                    is GridEntry.Header -> "hdr:" + entry.text
                    is GridEntry.Cell -> entry.item.key
                }
            },
            contentType = { entry -> if (entry is GridEntry.Header) 0 else 1 },
            span = { entry ->
                when (entry) {
                    is GridEntry.Header -> GridItemSpan(maxLineSpan)
                    is GridEntry.Cell -> GridItemSpan(1)
                }
            },
        ) { entry ->
            when (entry) {
                is GridEntry.Header -> {
                    Text(
                        text = entry.text,
                        style = if (level >= 3) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
                is GridEntry.Cell -> {
                    val item = entry.item
                    val terpilih = item.key in selection
                    // Model seret SESUAI video referensi user (2026-11-06):
                    // tekan-tahan = MASUK mode pilih; di dalam mode pilih,
                    // tekan-tahan+geser foto YG TERPILIH = menyeret SEMUA yg
                    // terpilih (ke album panel / keluar ke aplikasi lain).
                    // Di luar mode pilih TIDAK ada seretan langsung.
                    val modInteraksi = when {
                        selecting && terpilih -> Modifier
                            // Detektor seret CUSTOM (scope overload): detektor
                            // bawaan (tap-gesture long-press) KALAH balapan
                            // lawan scroll LazyGrid di HP user — seretan tidak
                            // pernah mulai (v1.61-v1.64). Di sini: tahan diam
                            // 450ms ATAU geser melewati touch slop setelah
                            // menekan >=120ms = mulai seret; perubahan disentuh
                            // (consume) SEBELUM scroll grid (leluhur) sempat
                            // mulai. Gerakan melewati slop <120ms = scroll.
                            .dragAndDropSource(
                                drawDragDecoration = {
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.92f),
                                        cornerRadius = CornerRadius(10.dp.toPx()),
                                    )
                                    drawRoundRect(
                                        color = Color(0xFF0381FE),
                                        cornerRadius = CornerRadius(10.dp.toPx()),
                                        style = Stroke(width = 2.dp.toPx()),
                                    )
                                },
                                block = {
                                    val slopPx = viewConfiguration.touchSlop
                                    while (true) {
                                        awaitPointerEventScope {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            var berubah = down
                                            var mulaiSeret = false
                                            while (true) {
                                                val sisaMs = 450L - (berubah.uptimeMillis - down.uptimeMillis)
                                                val event = if (sisaMs > 0L) {
                                                    withTimeoutOrNull(sisaMs) { awaitPointerEvent() }
                                                } else {
                                                    null
                                                }
                                                if (event == null) {
                                                    if (berubah.pressed) mulaiSeret = true
                                                    break
                                                }
                                                val ch = event.changes.firstOrNull { it.id == down.id }
                                                    ?: break
                                                berubah = ch
                                                if (!ch.pressed) break
                                                val jarak = (ch.position - down.position).getDistance()
                                                if (jarak > slopPx) {
                                                    if (ch.uptimeMillis - down.uptimeMillis >= 120L) {
                                                        ch.consume()
                                                        mulaiSeret = true
                                                    }
                                                    break
                                                }
                                            }
                                            if (mulaiSeret) {
                                                val daftar = items.filter { it.key in selection }
                                                    .ifEmpty { listOf(item) }
                                                onMulaiSeret(daftar)
                                                val clip = ClipData(
                                                    ClipDescription(
                                                        "Galeri FRM",
                                                        daftar.map { it.mime }.distinct().toTypedArray(),
                                                    ),
                                                    ClipData.Item(daftar.first().uri),
                                                )
                                                daftar.drop(1).forEach {
                                                    clip.addItem(ClipData.Item(it.uri))
                                                }
                                                startTransfer(
                                                    DragAndDropTransferData(
                                                        clipData = clip,
                                                        localState = daftar.joinToString(",") { it.key },
                                                        flags = View.DRAG_FLAG_GLOBAL or
                                                            View.DRAG_FLAG_GLOBAL_URI_READ,
                                                    )
                                                )
                                            }
                                        }
                                    }
                                },
                            )
                            .clickable { onItemClick(item) }
                        selecting -> Modifier.clickable { onItemClick(item) }
                        else -> Modifier.combinedClickable(
                            onClick = { onItemClick(item) },
                            onLongClick = { onItemLongClick(item) },
                        )
                    }
                    Box(
                        modifier = (if (level >= 3) Modifier else Modifier.animateItem(placementSpec = spring(dampingRatio = 0.9f, stiffness = 300f)))
                            .aspectRatio(1f)
                            .then(
                                if (level >= 3) Modifier
                                else Modifier.background(MaterialTheme.colorScheme.background)
                            )
                            .then(modInteraksi)
                    ) {
                        // MODE LITE (level >= 3, thumbnail kecil): di tingkat ini
                        // ratusan sel tampil sekaligus. Decode FRAME VIDEO per sel
                        // (MediaMetadataRetriever) itu operasi termahal di galeri —
                        // biang lag pas zoom out mentok di tab Gambar. Video cuma
                        // placeholder + ikon play; foto tetap tampil normal.
                        val lite = level >= 3
                        if (lite && item.isVideo) {
                            Box(
                                Modifier.fillMaxSize().background(Color(0xFF262626)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow, null, tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            val request = remember(item.key, context, ukuranThumbPx) {
                                val sumber = if (item.thumbUrl.isNotBlank()) item.thumbUrl else item.uri
                                val builder = ImageRequest.Builder(context).data(sumber)
                                    .size(ukuranThumbPx, ukuranThumbPx)
                                    .precision(Precision.INEXACT)
                                if (item.isVideo) builder.videoFrameMillis(500)
                                builder.crossfade(false).build()
                            }
                            AsyncImage(
                                model = request,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        if (item.isVideo && !lite) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Text(formatDurasi(item.durationMs), color = Color.White, fontSize = 10.sp)
                            }
                        }
                        if (!lite && isFavorite(item)) {
                            Icon(
                                Icons.Filled.Favorite, "Favorit", tint = Color.White,
                                modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).size(16.dp),
                            )
                        }
                        if (selecting && terpilih) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            )
                            Icon(
                                Icons.Filled.CheckCircle, "Terpilih",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(5.dp)
                                    .background(Color.White, CircleShape)
                                    .size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Krom mode pilih MENGAMBANG (ala screenshot Galeri Samsung) ----------
// Diukur dari screenshot user 2026-11-07: pil jumlah (kiri atas) + pil
// "Batal" (kanan atas) melayang; bar pil bawah (Buat/Berbagi/Hapus/Lainnya)
// lebar rapat di tengah; "Lainnya" membuka KARTU menu mengambang (sudut
// 28dp) tepat di atas bar, item 15sp, grup kedua dipisah garis titik-titik.

@Composable
private fun ItemBarPilih(ikon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(ikon, label, tint = Color(0xFF191919), modifier = Modifier.size(21.dp))
        Text(label, fontSize = 10.sp, color = Color(0xFF191919))
    }
}

@Composable
private fun ItemMenuLainnya(teks: String, onClick: () -> Unit) {
    Text(
        teks, fontSize = 15.sp, color = Color(0xFF191919),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
    )
}

/** Garis titik-titik pemisah grup di kartu menu Lainnya. */
@Composable
private fun GarisTitikMenu() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .height(2.dp),
    ) {
        val langkah = 8.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawCircle(Color(0xFFBDBDBD), radius = 1.2.dp.toPx(), center = Offset(x, size.height / 2))
            x += langkah
        }
    }
}

@Composable
fun KromPilihMengambang(
    tampak: Boolean,
    count: Int,
    driveConnected: Boolean,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onUpload: () -> Unit,
    onBuatAlbum: () -> Unit,
    onSalinClipboard: () -> Unit,
    onSalinKeAlbum: () -> Unit,
    onPindahKeAlbum: () -> Unit,
    onFavorit: () -> Unit,
) {
    var menuLainnya by remember { mutableStateOf(false) }
    LaunchedEffect(tampak) { if (!tampak) menuLainnya = false }
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = tampak,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(220)) +
                slideInVertically(tween(300, easing = FastOutSlowInEasing)) { -it / 2 },
            exit = fadeOut(tween(180)) +
                slideOutVertically(tween(240)) { -it / 2 },
        ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .height(40.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Circle, null, tint = Color(0xFF191919),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$count", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF191919),
                )
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .height(40.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .clickable(onClick = onClose)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Batal", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = Color(0xFF191919),
                )
            }
        }
        }
        AnimatedVisibility(
            visible = menuLainnya,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { menuLainnya = false },
            )
        }
        AnimatedVisibility(
            visible = menuLainnya,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(200)) +
                slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it },
            exit = fadeOut(tween(160)) +
                slideOutVertically(tween(260)) { it },
        ) {
            Column(
                Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp)
                    .fillMaxWidth(0.8f)
                    .widthIn(max = 340.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(vertical = 10.dp),
            ) {
                ItemMenuLainnya("Salin ke clipboard") { menuLainnya = false; onSalinClipboard() }
                ItemMenuLainnya("Salin ke album") { menuLainnya = false; onSalinKeAlbum() }
                ItemMenuLainnya("Pindahkan ke album") { menuLainnya = false; onPindahKeAlbum() }
                ItemMenuLainnya("Tambah ke favorit") { menuLainnya = false; onFavorit() }
                if (driveConnected) {
                    GarisTitikMenu()
                    ItemMenuLainnya("Upload ke Drive") { menuLainnya = false; onUpload() }
                }
            }
        }
        AnimatedVisibility(
            visible = tampak,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(220)) +
                slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it },
            exit = fadeOut(tween(180)) +
                slideOutVertically(tween(240)) { it },
        ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
                .shadow(8.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemBarPilih(Icons.Filled.AddPhotoAlternate, "Buat") { onBuatAlbum() }
            ItemBarPilih(Icons.Filled.Share, "Berbagi") { onShare() }
            ItemBarPilih(Icons.Filled.Delete, "Hapus") { onDelete() }
            ItemBarPilih(Icons.Filled.MoreVert, "Lainnya") { menuLainnya = !menuLainnya }
        }
        }
    }
}

/** Salin URI item terpilih ke clipboard sistem (buat ditempel di aplikasi lain). */
private fun salinKeClipboard(vm: GalleryViewModel, ctx: Context, pilihan: List<MediaItem>) {
    if (pilihan.isEmpty()) return
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newUri(ctx.contentResolver, "Galeri FRM", pilihan.first().uri)
    pilihan.drop(1).forEach { clip.addItem(ClipData.Item(it.uri)) }
    cm.setPrimaryClip(clip)
    vm.beriTahu("${pilihan.size} disalin ke clipboard")
}

/** Sheet pemilih album tujuan (buat Salin/Pindahkan dari menu Lainnya). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PemilihAlbumSheet(
    albums: List<Album>,
    judul: String,
    onPilih: (Album) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            judul, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        LazyColumn(Modifier.fillMaxWidth()) {
            items(albums, key = { it.id }) { album ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPilih(album) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        album.cover?.let { sampul ->
                            AsyncImage(
                                model = if (sampul.thumbUrl.isNotBlank()) sampul.thumbUrl else sampul.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            album.name, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text("${album.count}", fontSize = 12.sp, color = WarnaSubMenu)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ---------- Layar Gambar (timeline) ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    vm: GalleryViewModel,
    selection: Set<String>,
    selecting: Boolean,
    onItemClick: (MediaItem) -> Unit,
    onItemLongClick: (MediaItem) -> Unit,
    onStartSelecting: () -> Unit,
    onCloseSelecting: () -> Unit,
    onShareSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onUploadSelected: () -> Unit,
) {
    val ctxUi = LocalContext.current
    var level by rememberSaveable { mutableIntStateOf(UiStore.loadInt(ctxUi, "zoom_gambar", 1)) }
    LaunchedEffect(level) { UiStore.saveInt(ctxUi, "zoom_gambar", level) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    // Kembali pas lagi nyari = tutup pencarian dulu (sebelum handler induk)
    androidx.activity.compose.BackHandler(enabled = searching) {
        searching = false
        query = ""
    }
    // Panel album: state dideklarasikan di bawah (panelTerbuka/albumTerpilihId)
    // — handler ini membaca state itu lewat referensi komposisi yg sama.
    var menuOpen by remember { mutableStateOf(false) }
    // Timeline = lokal + Drive digabung, urut tanggal terbaru (kayak Galeri Samsung)
    val items = remember(vm.media, vm.driveItems, query) {
        val gabung = (vm.media + vm.driveItems).sortedByDescending { it.dateMillis }
        if (query.isBlank()) gabung else gabung.filter { it.name.contains(query, ignoreCase = true) }
    }
    // === Panel album (usap kanan / pas nyeret foto): split ala screenshot
    // user — album di kiri, grid foto di kanan ===
    var panelTerbuka by rememberSaveable { mutableStateOf(false) }
    var albumTerpilihId by rememberSaveable { mutableStateOf<String?>(null) }
    androidx.activity.compose.BackHandler(
        enabled = !searching && (albumTerpilihId != null || panelTerbuka),
    ) {
        if (albumTerpilihId != null) albumTerpilihId = null else panelTerbuka = false
    }
    val daftarAlbumPanel = remember(vm.localAlbums, vm.driveAlbums, vm.favorites, vm.media, vm.driveItems) {
        buildList {
            val fav = vm.favoritesAlbum()
            if (fav.count > 0) add(fav)
            addAll(vm.driveAlbums)
            addAll(vm.localAlbums)
        }.map { it.denganIkon(ctxUi) }
    }
    val albumTerpilih = daftarAlbumPanel.firstOrNull { it.id == albumTerpilihId }
    val tampilGrid = remember(items, albumTerpilih, vm.media, vm.driveItems, vm.favorites, query) {
        val a = albumTerpilih ?: return@remember items
        val dasar = when (a.kind) {
            AlbumKind.LOCAL -> vm.media.filter { "local:${it.bucketId}" == a.id }
            AlbumKind.DRIVE -> vm.driveItems.filter { item -> a.items.any { d -> d.key == item.key } }
            AlbumKind.FAVORITES -> (vm.media + vm.driveItems).filter { it.key in vm.favorites }
        }.ifEmpty { a.items }.sortedByDescending { it.dateMillis }
        if (query.isBlank()) dasar else dasar.filter { it.name.contains(query, ignoreCase = true) }
    }
    // === Mode pilih: krom mengambang + sheet album + dialog album baru ===
    val pilihanKini = remember(items, selection) { items.filter { it.key in selection } }
    var modeAlbumSheet by remember { mutableStateOf<String?>(null) }
    var buatAlbumTerbuka by remember { mutableStateOf(false) }
    var namaAlbumBaru by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        albumTerpilih?.name ?: "Gambar", fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    IconButton(onClick = { searching = !searching }) { Icon(Icons.Filled.Search, "Cari") }
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, "Lainnya") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Pilih item") },
                                leadingIcon = { Icon(Icons.Filled.SelectAll, null) },
                                onClick = { menuOpen = false; onStartSelecting() },
                            )
                            DropdownMenuItem(
                                text = { Text(if (panelTerbuka) "Sembunyikan album" else "Tampilkan album") },
                                leadingIcon = { Icon(Icons.Filled.PhotoLibrary, null) },
                                onClick = { menuOpen = false; panelTerbuka = !panelTerbuka },
                            )
                            DropdownMenuItem(
                                text = { Text("Segarkan") },
                                leadingIcon = { Icon(Icons.Filled.Refresh, null) },
                                onClick = { menuOpen = false; vm.reloadLocal() },
                            )
                        }
                    }
                },
            )
        AnimatedVisibility(
            visible = searching,
            enter = expandVertically(tween(240)) + fadeIn(tween(240)),
            exit = shrinkVertically(tween(240)) + fadeOut(tween(180)),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cari nama file…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        // Lebar panel DIANIMASIKAN (weight 0 -> 0,196): rel meluncur halus
        // masuk/keluar dan grid bergeser mengikuti, bukan muncul mendadak.
        val lebarPanel by animateFloatAsState(
            targetValue = if (panelTerbuka) 0.196f else 0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "lebarPanel",
        )
        val wPanel = lebarPanel.coerceIn(0.002f, 0.196f)
        Row(Modifier.fillMaxSize()) {
            if (panelTerbuka || lebarPanel > 0.002f) {
                PanelAlbum(
                    vm = vm,
                    albums = daftarAlbumPanel,
                    terpilihId = albumTerpilihId,
                    totalFoto = vm.media.size + vm.driveItems.size,
                    onPilih = { albumTerpilihId = it },
                    onGeserTutup = { panelTerbuka = false },
                    onSetelahDrop = onCloseSelecting,
                    modifier = Modifier.weight(wPanel),
                )
            }
            Box(
                Modifier
                    .weight(1f - wPanel)
                    .fillMaxHeight()
                    .pointerInput(panelTerbuka) {
                        // Usap ke kanan dari area grid MANA PUN = buka panel
                        // album (v1.61 cuma strip 22dp di tepi — terlalu
                        // sempit, dari tengah layar tidak pernah kepicu).
                        // Grid vertikal tidak memakai geser horizontal, jadi
                        // detektor ini tidak merebut scroll atas-bawah.
                        var total = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { total = 0f },
                            onDragEnd = {
                                if (total > 90f && !panelTerbuka) panelTerbuka = true
                                else if (total < -90f && panelTerbuka) panelTerbuka = false
                            },
                            onHorizontalDrag = { _, jumlah -> total += jumlah },
                        )
                    },
            ) {
                when {
                    vm.loading -> Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                    tampilGrid.isEmpty() -> Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (albumTerpilih == null) "Tidak ada foto atau video" else "Album kosong",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> MediaGrid(
                        kunciPosisi = if (albumTerpilih == null) "timeline" else "panel_" + albumTerpilih.id,
                        tanpaTanggal = albumTerpilih != null,
                        onMulaiSeret = { panelTerbuka = true },
                        items = tampilGrid,
                        level = level,
                        onLevelChange = { level = it },
                        selection = selection,
                        selecting = selecting,
                        isFavorite = { vm.isFavorite(it) },
                        onItemClick = onItemClick,
                        onItemLongClick = onItemLongClick,
                        contentPadding = PaddingValues(bottom = 96.dp),
                    )
                }
                // Tombol bulet kembali melayang (ala screenshot) pas panel
                // kebuka / lagi di dalam album panel
                TombolBuletKembali(
                    tampak = panelTerbuka || albumTerpilih != null,
                    onClick = {
                        if (albumTerpilihId != null) albumTerpilihId = null
                        else panelTerbuka = false
                    },
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }
        }
    }
    KromPilihMengambang(
        tampak = selecting,
        count = selection.size,
            driveConnected = vm.driveApiOn || vm.driveTreeUri != null,
            onClose = onCloseSelecting,
            onShare = onShareSelected,
            onDelete = onDeleteSelected,
            onUpload = onUploadSelected,
            onBuatAlbum = { namaAlbumBaru = ""; buatAlbumTerbuka = true },
            onSalinClipboard = { salinKeClipboard(vm, ctxUi, pilihanKini) },
            onSalinKeAlbum = { modeAlbumSheet = "salin" },
            onPindahKeAlbum = { modeAlbumSheet = "pindah" },
            onFavorit = {
                var n = 0
                for (pil in pilihanKini) if (!vm.isFavorite(pil)) { vm.toggleFavorite(pil); n++ }
                vm.beriTahu(if (n == 0) "Sudah di Favorit semua" else "$n ditambah ke Favorit")
                onCloseSelecting()
            },
    )
    if (modeAlbumSheet != null) {
        PemilihAlbumSheet(
            albums = daftarAlbumPanel,
            judul = if (modeAlbumSheet == "salin") "Salin ke album" else "Pindahkan ke album",
            onPilih = { album ->
                if (modeAlbumSheet == "salin") vm.salinBanyakKeAlbum(pilihanKini, album)
                else vm.pindahBanyakKeAlbum(pilihanKini, album)
                modeAlbumSheet = null
                onCloseSelecting()
            },
            onDismiss = { modeAlbumSheet = null },
        )
    }
    if (buatAlbumTerbuka) {
        AlertDialog(
            onDismissRequest = { buatAlbumTerbuka = false },
            title = { Text("Album baru") },
            text = {
                OutlinedTextField(
                    value = namaAlbumBaru,
                    onValueChange = { namaAlbumBaru = it },
                    label = { Text("Nama album") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        buatAlbumTerbuka = false
                        vm.pindahKeFolderBaru(pilihanKini, namaAlbumBaru)
                        onCloseSelecting()
                    },
                    enabled = namaAlbumBaru.isNotBlank(),
                ) { Text("Buat") }
            },
            dismissButton = {
                TextButton(onClick = { buatAlbumTerbuka = false }) { Text("Batal") }
            },
        )
    }
    }
}

/** Tombol bulet kembali melayang di atas grid (panel kebuka / lagi di album panel). */
@Composable
private fun TombolBuletKembali(tampak: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = tampak,
        modifier = modifier,
        enter = fadeIn(tween(200)) +
            scaleIn(tween(280, easing = FastOutSlowInEasing), initialScale = 0.6f),
        exit = fadeOut(tween(150)) + scaleOut(tween(200), targetScale = 0.6f),
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .padding(12.dp)
                .size(42.dp)
                .shadow(3.dp, CircleShape)
                .background(Color.White, CircleShape),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, "Kembali",
                tint = Color.Black,
            )
        }
    }
}

/**
 * Panel album meluncur dari kiri (split ala screenshot user, DIUKUR dari
 * screenshot-nya): lebar 19,6% layar; tiap album = kartu vertikal (cover
 * 50dp sudut 8dp, nama 13sp di bawahnya, jumlah 11sp abu); album terpilih
 * berbingkai biru di cover-nya; latar putih polos.
 */
@Composable
private fun PanelAlbum(
    vm: GalleryViewModel,
    albums: List<Album>,
    terpilihId: String?,
    totalFoto: Int,
    tampilSemuaFoto: Boolean = true,
    onPilih: (String?) -> Unit,
    onGeserTutup: () -> Unit,
    onSetelahDrop: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Lebar rel TETAP 19,6% layar; yg dianimasikan pemanggil adalah lebar
    // WADAH LUAR (weight) — isi rel tidak ikut menyempit, cuma makin
    // terlihat/tertutup lewat clipToBounds = meluncur halus ala referensi.
    val lebarRel = (LocalConfiguration.current.screenWidthDp * 0.196f).dp
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clipToBounds()
            .pointerInput(Unit) {
                var total = 0f
                detectHorizontalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = { if (total < -70f) onGeserTutup() },
                    onHorizontalDrag = { _, jumlah -> total += jumlah },
                )
            },
    ) {
        Column(
            Modifier
                .width(lebarRel)
                .fillMaxHeight()
                .background(Color.White),
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 6.dp),
            ) {
                if (tampilSemuaFoto) {
                    item {
                        BarisSemuaFotoPanel(
                            totalFoto = totalFoto,
                            terpilih = terpilihId == null,
                        ) { onPilih(null) }
                    }
                }
                items(albums, key = { it.id }) { album ->
                    BarisAlbumPanel(vm, album, terpilih = album.id == terpilihId, onSetelahDrop = onSetelahDrop) {
                        onPilih(album.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun BarisSemuaFotoPanel(totalFoto: Int, terpilih: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (terpilih) {
                        Modifier.border(2.dp, Color(0xFF0381FE), RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PhotoLibrary, "Semua foto",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "Semua foto", fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = WarnaTeksMenu, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp, top = 3.dp),
        )
        Text(
            "$totalFoto", fontSize = 11.sp, color = WarnaSubMenu,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

/** Satu kartu album di panel — juga TARGET DROP seretan foto. */
@Composable
private fun BarisAlbumPanel(
    vm: GalleryViewModel,
    album: Album,
    terpilih: Boolean,
    onSetelahDrop: () -> Unit = {},
    onClick: () -> Unit,
) {
    val sorotState = remember { mutableStateOf(false) }
    val disorot = sorotState.value
    val targetDrop = remember(album) {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                sorotState.value = true
            }

            override fun onExited(event: DragAndDropEvent) {
                sorotState.value = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                sorotState.value = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                sorotState.value = false
                val dragEvent = event.toAndroidDragEvent()
                val pool = vm.media + vm.driveItems
                // Seretan dari grid membawa SEMUA kunci yg terpilih (dipisah
                // koma) di localState; fallback: cocokkan URI di ClipData.
                val dariKunci = (dragEvent.localState as? String)
                    ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                    ?.mapNotNull { k -> pool.firstOrNull { it.key == k } }
                    .orEmpty()
                val daftar = if (dariKunci.isNotEmpty()) dariKunci else {
                    val cd = dragEvent.clipData ?: return false
                    (0 until cd.itemCount).mapNotNull { i ->
                        cd.getItemAt(i).uri?.toString()?.let { u ->
                            pool.firstOrNull { it.key == u || it.uri.toString() == u }
                        }
                    }
                }
                if (daftar.isEmpty()) return false
                vm.pindahBanyakKeAlbum(daftar, album)
                onSetelahDrop()
                return true
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (disorot) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else Color.Transparent
            )
            .dragAndDropTarget(
                shouldStartDragAndDrop = { e ->
                    e.mimeTypes().any { it.startsWith("image/") || it.startsWith("video/") }
                },
                target = targetDrop,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    when {
                        disorot -> Modifier.border(
                            2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)
                        )
                        terpilih -> Modifier.border(
                            2.dp, Color(0xFF0381FE), RoundedCornerShape(8.dp)
                        )
                        else -> Modifier
                    }
                ),
        ) {
            val sampul = album.cover
            if (sampul != null) {
                AsyncImage(
                    model = if (sampul.thumbUrl.isNotBlank()) sampul.thumbUrl else sampul.uri,
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            album.name, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = WarnaTeksMenu, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp, top = 3.dp),
        )
        Text(
            "${album.count}", fontSize = 11.sp, color = WarnaSubMenu,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

// ---------- Layar Album ----------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumsScreen(
    vm: GalleryViewModel,
    onOpenAlbum: (Album) -> Unit,
) {
    val ctx = LocalContext.current
    val statusDaftarAlbum = rememberLazyListState()
    val statusGridAlbum = rememberLazyGridState()
    var kolom by rememberSaveable { mutableIntStateOf(UiStore.loadInt(ctx, "kolom_album", 3)) }
    // Mode DAFTAR ala Samsung: pinch out sampai MENTOK di grid -> berubah
    // jadi daftar (cover rounded gede di kiri + nama + jumlah). Pinch in
    // di mode daftar -> balik ke grid.
    var modeDaftar by rememberSaveable { mutableStateOf(UiStore.loadBool(ctx, "daftar_album", false)) }
    LaunchedEffect(kolom, modeDaftar) {
        UiStore.saveInt(ctx, "kolom_album", kolom)
        UiStore.saveBool(ctx, "daftar_album", modeDaftar)
    }
    var mencari by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    // Default urutan album = Nama A–Z (perintah user). Simpanan pakai
    // UiStore (SharedPreferences) — rememberSaveable terbukti TIDAK awet di
    // HP user (sort balik ke A-Z sendiri pas pindah layar; pola sama kayak
    // bug scroll v1.38), UiStore awet (dipakai zoom/kolom/urutan kustom).
    var urutan by remember { mutableIntStateOf(UiStore.loadInt(ctx, "urutan_album_v2", 1)) }
    LaunchedEffect(urutan) { UiStore.saveInt(ctx, "urutan_album_v2", urutan) }
    var menuOpen by remember { mutableStateOf(false) }
    // Urutan KUSTOM (urutan == 4): id album sesuai susunan user (seret)
    var urutanKustom by remember {
        mutableStateOf(
            (UiStore.loadString(ctx, "urutan_album_kustom", "") ?: "")
                .split(",").filter { it.isNotBlank() }
        )
    }

    val albums = remember(vm.localAlbums, vm.driveAlbums, vm.favorites, vm.media, vm.driveItems) {
        buildList {
            val fav = vm.favoritesAlbum()
            if (fav.count > 0) add(fav)
            addAll(vm.driveAlbums)
            addAll(vm.localAlbums)
        }.map { it.denganIkon(ctx) }
    }
    val tampil = remember(albums, query, urutan, urutanKustom) {
        val saring = if (query.isBlank()) albums
        else albums.filter { it.name.contains(query, ignoreCase = true) }
        when (urutan) {
            1 -> saring.sortedBy { it.name.lowercase() }
            2 -> saring.sortedByDescending { it.count }
            3 -> saring.sortedByDescending { a -> a.items.maxOfOrNull { it.dateMillis } ?: 0L }
            4 -> {
                val petaKustom = urutanKustom.withIndex().associate { (i, id) -> id to i }
                saring.sortedBy { petaKustom[it.id] ?: Int.MAX_VALUE }
            }
            else -> saring.sortedBy { it.name.lowercase() }
        }
    }

    // === Seret album (tekan-tahan) buat mindah urutan (perintah user) ===
    val tampilKini by rememberUpdatedState(tampil)
    var idSeret by remember { mutableStateOf<String?>(null) }
    var geserSeret by remember { mutableStateOf(Offset.Zero) }
    var asalSeret by remember { mutableStateOf<IntOffset?>(null) }
    fun simpanKustom(ids: List<String>) {
        urutanKustom = ids
        UiStore.saveString(ctx, "urutan_album_kustom", ids.joinToString(","))
    }
    val haptikAlbum = LocalHapticFeedback.current
    fun mulaiSeret(album: Album) {
        if (query.isNotBlank() || idSeret != null) return
        haptikAlbum.performHapticFeedback(HapticFeedbackType.LongPress)
        if (urutan != 4 || urutanKustom.isEmpty()) {
            urutan = 4
            simpanKustom(tampilKini.map { it.id })
        }
        val infoGrid = statusGridAlbum.layoutInfo.visibleItemsInfo.firstOrNull { it.key == album.id }
        val infoDaftar = statusDaftarAlbum.layoutInfo.visibleItemsInfo.firstOrNull { it.key == album.id }
        idSeret = album.id
        asalSeret = when {
            infoGrid != null -> infoGrid.offset
            infoDaftar != null -> IntOffset(0, infoDaftar.offset)
            else -> null
        }
        geserSeret = Offset.Zero
    }
    fun prosesSeret() {
        val id = idSeret ?: return
        val asal = asalSeret ?: return
        val info = if (modeDaftar) {
            val lebarDaftar = statusDaftarAlbum.layoutInfo.viewportSize.width
            statusDaftarAlbum.layoutInfo.visibleItemsInfo.map {
                Triple(it.key, IntOffset(0, it.offset), IntSize(lebarDaftar, it.size))
            }
        } else {
            statusGridAlbum.layoutInfo.visibleItemsInfo.map { Triple(it.key, it.offset, it.size) }
        }
        val punyaku = info.firstOrNull { it.first == id } ?: return
        val tengahX = asal.x + punyaku.third.width / 2 + geserSeret.x
        val tengahY = asal.y + punyaku.third.height / 2 + geserSeret.y
        val target = info.firstOrNull { t ->
            t.first != id &&
                tengahX >= t.second.x && tengahX < t.second.x + t.third.width &&
                tengahY >= t.second.y && tengahY < t.second.y + t.third.height
        } ?: return
        val ids = tampilKini.map { it.id }.toMutableList()
        val dari = ids.indexOf(id)
        val ke = ids.indexOf(target.first as String)
        if (dari < 0 || ke < 0 || dari == ke) return
        ids.removeAt(dari)
        ids.add(ke, id)
        geserSeret = Offset(
            geserSeret.x + asal.x - target.second.x,
            geserSeret.y + asal.y - target.second.y,
        )
        asalSeret = target.second
        simpanKustom(ids)
    }
    fun selesaiSeret() {
        idSeret = null
        geserSeret = Offset.Zero
        asalSeret = null
    }
    fun Modifier.gayaSeret(album: Album): Modifier = this
        .zIndex(if (album.id == idSeret) 2f else 0f)
        .graphicsLayer {
            if (album.id == idSeret) {
                translationX = geserSeret.x
                translationY = geserSeret.y
                scaleX = 1.04f
                scaleY = 1.04f
                alpha = 0.92f
            }
        }
        .pointerInput(album.id) {
            detectDragGesturesAfterLongPress(
                onDragStart = { mulaiSeret(album) },
                onDrag = { change, jumlah ->
                    change.consume()
                    geserSeret += jumlah
                    prosesSeret()
                },
                onDragEnd = { selesaiSeret() },
                onDragCancel = { selesaiSeret() },
            )
        }

    @Composable
    fun sampul(album: Album, ukuranPx: Int, modifier: Modifier) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            album.cover?.let { cover ->
                val coverRequest = remember(cover.key, ctx, ukuranPx) {
                    val sumberCover = if (cover.thumbUrl.isNotBlank()) cover.thumbUrl else cover.uri
                    ImageRequest.Builder(ctx).data(sumberCover)
                        .size(ukuranPx, ukuranPx)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build()
                }
                AsyncImage(
                    model = coverRequest,
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            // Lencana SUMBER penyimpanan: Drive = logo PNG asli, HP = ikon
            // HP, kartu SD = ikon SD, USB = ikon flashdisk, Favorit = hati
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (album.ikon) {
                    IkonSumber.DRIVE -> Image(
                        painter = painterResource(R.drawable.logo_drive),
                        contentDescription = "Google Drive",
                        modifier = Modifier.size(14.dp),
                    )
                    IkonSumber.HP -> Icon(
                        Icons.Filled.PhoneAndroid, "Penyimpanan HP",
                        tint = Color.White, modifier = Modifier.size(13.dp),
                    )
                    IkonSumber.SD -> Icon(
                        Icons.Filled.SdCard, "Kartu SD",
                        tint = Color.White, modifier = Modifier.size(13.dp),
                    )
                    IkonSumber.USB -> Icon(
                        Icons.Filled.Usb, "USB",
                        tint = Color.White, modifier = Modifier.size(13.dp),
                    )
                    IkonSumber.FAVORIT -> Icon(
                        Icons.Filled.Favorite, "Favorit",
                        tint = Color(0xFFFF6B81), modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Album", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { mencari = !mencari; if (!mencari) query = "" }) {
                    Icon(Icons.Filled.Search, "Cari album")
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, "Lainnya")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(if (modeDaftar) "Tampilan grid" else "Tampilan daftar") },
                            onClick = { menuOpen = false; modeDaftar = !modeDaftar },
                        )
                        DropdownMenuItem(
                            text = { Text((if (urutan == 1) "✓ " else "") + "Nama A–Z") },
                            onClick = { menuOpen = false; urutan = 1 },
                        )
                        DropdownMenuItem(
                            text = { Text((if (urutan == 2) "✓ " else "") + "Paling banyak isinya") },
                            onClick = { menuOpen = false; urutan = 2 },
                        )
                        DropdownMenuItem(
                            text = { Text((if (urutan == 3) "✓ " else "") + "Terbaru") },
                            onClick = { menuOpen = false; urutan = 3 },
                        )
                        DropdownMenuItem(
                            text = { Text((if (urutan == 4) "✓ " else "") + "Kustom") },
                            onClick = { menuOpen = false; urutan = 4 },
                        )
                    }
                }
            },
        )
        if (mencari) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cari album…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        Text(
            "Semua album",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
        )
        if (tampil.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (query.isBlank()) "Belum ada album" else "Tidak ada album yang cocok",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (modeDaftar) {
            IngatPosisiList(statusDaftarAlbum, "album_daftar", tampil.size, aktif = query.isBlank())
            LazyColumn(
                state = statusDaftarAlbum,
                userScrollEnabled = idSeret == null,
                modifier = Modifier
                    .fillMaxSize()
                    .animasiZoom(kunci = modeDaftar, membesar = false)
                    .pointerInput(Unit) {
                        detectCubitGrid { zoomIn ->
                            if (zoomIn) {
                                modeDaftar = false
                                kolom = 3
                            }
                        }
                    },
                contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
            ) {
                items(tampil, key = { it.id }) { album ->
                    Row(
                        modifier = (if (album.id == idSeret) Modifier else Modifier.animateItem(placementSpec = spring(dampingRatio = 0.9f, stiffness = 300f)))
                            .gayaSeret(album)
                            .fillMaxWidth()
                            .clickable { onOpenAlbum(album) }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        sampul(
                            album, 228,
                            Modifier.size(74.dp).clip(RoundedCornerShape(16.dp)),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                album.name, fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${album.count}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        } else {
            IngatPosisiGrid(statusGridAlbum, "album_grid", tampil.size, aktif = query.isBlank())
            LazyVerticalGrid(
                state = statusGridAlbum,
                userScrollEnabled = idSeret == null,
                columns = GridCells.Fixed(kolom),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectCubitGrid { zoomIn ->
                            when {
                                zoomIn -> kolom = 2
                                kolom == 2 -> kolom = 3
                                else -> modeDaftar = true
                            }
                        }
                    },
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(tampil, key = { it.id }) { album ->
                    Column(
                        modifier = (if (album.id == idSeret) Modifier else Modifier.animateItem(placementSpec = spring(dampingRatio = 0.9f, stiffness = 300f)))
                            .gayaSeret(album)
                            .clickable { onOpenAlbum(album) }
                    ) {
                        sampul(
                            album, 512,
                            Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(18.dp)),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            album.name, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${album.count}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ---------- Layar detail album ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    vm: GalleryViewModel,
    album: Album,
    selection: Set<String>,
    selecting: Boolean,
    onBack: () -> Unit,
    onItemClick: (List<MediaItem>, MediaItem) -> Unit,
    onItemLongClick: (MediaItem) -> Unit,
    onCloseSelecting: () -> Unit,
    onShareSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onUploadSelected: () -> Unit,
    onStartSelecting: () -> Unit = {},
) {
    val ctxUiDetail = LocalContext.current
    var level by rememberSaveable { mutableIntStateOf(UiStore.loadInt(ctxUiDetail, "zoom_album_detail", 1)) }
    LaunchedEffect(level) { UiStore.saveInt(ctxUiDetail, "zoom_album_detail", level) }
    // === Panel album juga ada DI DALAM album (perintah user): usap kanan /
    // menu / mulai nyeret foto membukanya; ketuk album lain = pindah isi ===
    var panelTerbuka by rememberSaveable { mutableStateOf(false) }
    var albumTerpilihId by remember(album.id) { mutableStateOf(album.id) }
    var menuDetailOpen by remember { mutableStateOf(false) }
    val daftarAlbumDetail = remember(vm.localAlbums, vm.driveAlbums, vm.favorites, vm.media, vm.driveItems) {
        buildList {
            val fav = vm.favoritesAlbum()
            if (fav.count > 0) add(fav)
            addAll(vm.driveAlbums)
            addAll(vm.localAlbums)
        }.map { it.denganIkon(ctxUiDetail) }
    }
    val albumAktif = daftarAlbumDetail.firstOrNull { it.id == albumTerpilihId } ?: album
    // TIDAK ada BackHandler lokal di sini (perintah user 2026-11-07):
    // tombol Kembali di dalam album LANGSUNG keluar album (handler induk di
    // MainActivity), BUKAN menutup panel / mengembalikan album panel dulu.
    // Item album dihitung ulang dari sumber terbaru (lokal / drive / favorit)
    val freshItems = remember(albumAktif.id, vm.media, vm.driveItems, vm.favorites) {
        when (albumAktif.kind) {
            AlbumKind.LOCAL -> vm.media.filter { "local:${it.bucketId}" == albumAktif.id }
            AlbumKind.DRIVE -> vm.driveItems.filter { item ->
                vm.driveAlbums.any { it.id == albumAktif.id && it.items.any { d -> d.key == item.key } }
            }
            AlbumKind.FAVORITES -> (vm.media + vm.driveItems).filter { it.key in vm.favorites }
        }.ifEmpty { albumAktif.items }
    }
    // === Mode pilih: krom mengambang + sheet album + dialog album baru ===
    val pilihanKini = remember(freshItems, selection) { freshItems.filter { it.key in selection } }
    var modeAlbumSheet by remember { mutableStateOf<String?>(null) }
    var buatAlbumTerbuka by remember { mutableStateOf(false) }
    var namaAlbumBaru by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(albumAktif.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, "Kembali")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuDetailOpen = true }) {
                            Icon(Icons.Filled.MoreVert, "Lainnya")
                        }
                        DropdownMenu(expanded = menuDetailOpen, onDismissRequest = { menuDetailOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Pilih item") },
                                leadingIcon = { Icon(Icons.Filled.SelectAll, null) },
                                onClick = { menuDetailOpen = false; onStartSelecting() },
                            )
                            DropdownMenuItem(
                                text = { Text(if (panelTerbuka) "Sembunyikan album" else "Tampilkan album") },
                                leadingIcon = { Icon(Icons.Filled.PhotoLibrary, null) },
                                onClick = { menuDetailOpen = false; panelTerbuka = !panelTerbuka },
                            )
                        }
                    }
                },
            )
        // Lebar panel DIANIMASIKAN (weight 0 -> 0,196): rel meluncur halus
        // masuk/keluar dan grid bergeser mengikuti, bukan muncul mendadak.
        val lebarPanel by animateFloatAsState(
            targetValue = if (panelTerbuka) 0.196f else 0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "lebarPanel",
        )
        val wPanel = lebarPanel.coerceIn(0.002f, 0.196f)
        Row(Modifier.fillMaxSize()) {
            if (panelTerbuka || lebarPanel > 0.002f) {
                PanelAlbum(
                    vm = vm,
                    albums = daftarAlbumDetail,
                    terpilihId = albumAktif.id,
                    totalFoto = vm.media.size + vm.driveItems.size,
                    tampilSemuaFoto = false,
                    onPilih = { if (it != null) albumTerpilihId = it },
                    onGeserTutup = { panelTerbuka = false },
                    onSetelahDrop = onCloseSelecting,
                    modifier = Modifier.weight(wPanel),
                )
            }
            Box(
                Modifier
                    .weight(1f - wPanel)
                    .fillMaxHeight()
                    .pointerInput(panelTerbuka) {
                        var total = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { total = 0f },
                            onDragEnd = {
                                if (total > 90f && !panelTerbuka) panelTerbuka = true
                                else if (total < -90f && panelTerbuka) panelTerbuka = false
                            },
                            onHorizontalDrag = { _, jumlah -> total += jumlah },
                        )
                    },
            ) {
                if (freshItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Album kosong", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    MediaGrid(
                        kunciPosisi = "album_" + albumAktif.id,
                        tanpaTanggal = true,
                        onMulaiSeret = { panelTerbuka = true },
                        items = freshItems,
                        level = level,
                        onLevelChange = { level = it },
                        selection = selection,
                        selecting = selecting,
                        isFavorite = { vm.isFavorite(it) },
                        onItemClick = { item -> onItemClick(freshItems, item) },
                        onItemLongClick = onItemLongClick,
                        contentPadding = PaddingValues(bottom = 96.dp),
                    )
                }
                TombolBuletKembali(
                    tampak = panelTerbuka || albumTerpilihId != album.id,
                    onClick = {
                        if (albumTerpilihId != album.id) albumTerpilihId = album.id
                        else panelTerbuka = false
                    },
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }
        }
    }
    KromPilihMengambang(
        tampak = selecting,
        count = selection.size,
            driveConnected = vm.driveApiOn || vm.driveTreeUri != null,
            onClose = onCloseSelecting,
            onShare = onShareSelected,
            onDelete = onDeleteSelected,
            onUpload = onUploadSelected,
            onBuatAlbum = { namaAlbumBaru = ""; buatAlbumTerbuka = true },
            onSalinClipboard = { salinKeClipboard(vm, ctxUiDetail, pilihanKini) },
            onSalinKeAlbum = { modeAlbumSheet = "salin" },
            onPindahKeAlbum = { modeAlbumSheet = "pindah" },
            onFavorit = {
                var n = 0
                for (pil in pilihanKini) if (!vm.isFavorite(pil)) { vm.toggleFavorite(pil); n++ }
                vm.beriTahu(if (n == 0) "Sudah di Favorit semua" else "$n ditambah ke Favorit")
                onCloseSelecting()
            },
    )
    if (modeAlbumSheet != null) {
        PemilihAlbumSheet(
            albums = daftarAlbumDetail,
            judul = if (modeAlbumSheet == "salin") "Salin ke album" else "Pindahkan ke album",
            onPilih = { album ->
                if (modeAlbumSheet == "salin") vm.salinBanyakKeAlbum(pilihanKini, album)
                else vm.pindahBanyakKeAlbum(pilihanKini, album)
                modeAlbumSheet = null
                onCloseSelecting()
            },
            onDismiss = { modeAlbumSheet = null },
        )
    }
    if (buatAlbumTerbuka) {
        AlertDialog(
            onDismissRequest = { buatAlbumTerbuka = false },
            title = { Text("Album baru") },
            text = {
                OutlinedTextField(
                    value = namaAlbumBaru,
                    onValueChange = { namaAlbumBaru = it },
                    label = { Text("Nama album") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        buatAlbumTerbuka = false
                        vm.pindahKeFolderBaru(pilihanKini, namaAlbumBaru)
                        onCloseSelecting()
                    },
                    enabled = namaAlbumBaru.isNotBlank(),
                ) { Text("Buat") }
            },
            dismissButton = {
                TextButton(onClick = { buatAlbumTerbuka = false }) { Text("Batal") }
            },
        )
    }
    }
}

// ---------- Layar Menu (gaya Pengaturan One UI, hasil ukur screenshot) ----------
// Latar #F1F1F3, kartu putih sudut 20dp margin 10dp, label seksi abu
// #76767A 13sp, email biru #1976D2, pemisah #E3E3E5 mulai dari awal teks.

private val WarnaLatarMenu = Color(0xFFF1F1F3)
private val WarnaLabelSeksi = Color(0xFF76767A)
private val WarnaBiruTautan = Color(0xFF1976D2)
private val WarnaTeksMenu = Color(0xFF191919)
private val WarnaSubMenu = Color(0xFF8A8A8E)
private val WarnaIkonMenu = Color(0xFF48484A)

@Composable
private fun LabelSeksi(teks: String) {
    Text(
        teks,
        color = WarnaLabelSeksi,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 26.dp, top = 22.dp, bottom = 8.dp),
    )
}

@Composable
private fun KartuMenu(isi: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White),
        content = isi,
    )
}

/** Garis pemisah antar baris — mulai dari awal teks, bukan tepi kartu. */
@Composable
private fun PemisahMenu() {
    HorizontalDivider(
        thickness = 1.dp,
        color = Color(0xFFE3E3E5),
        modifier = Modifier.padding(start = 60.dp),
    )
}

@Composable
private fun BarisMenu(
    judul: String,
    sub: String? = null,
    subBiru: Boolean = false,
    onClick: (() -> Unit)? = null,
    ikon: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) { ikon() }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(judul, color = WarnaTeksMenu, fontSize = 16.sp)
            if (sub != null) {
                Text(
                    sub,
                    color = if (subBiru) WarnaBiruTautan else WarnaSubMenu,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    vm: GalleryViewModel,
    onOpenFavorites: () -> Unit,
    onLoginAkunLain: () -> Unit = {},
    onOpenTrash: () -> Unit = {},
) {
    var akunKeluar by remember { mutableStateOf<String?>(null) }
    val ctxMenu = LocalContext.current
    // === Tambah akun: dialog Google dikunci ke akun yg dipilih user ===
    var pilihAkun by remember { mutableStateOf<List<String>?>(null) }
    fun saringAkunBelumMasuk(daftar: List<String>): List<String> =
        daftar.filter { e -> vm.daftarAkunDrive.none { it.email.equals(e, ignoreCase = true) } }
    fun sesudahDaftarAkun(daftar: List<String>) {
        // Pemilih SELALU tampil selama ada akun terbaca (di dalamnya ada
        // jalur "Akun lain…") — jangan mentok di toast. Daftar kosong =
        // izin ditolak / akun tak terbaca -> langsung jalur browser.
        if (daftar.isEmpty()) {
            onLoginAkunLain()
        } else {
            pilihAkun = saringAkunBelumMasuk(daftar)
        }
    }
    val peluncurIzinAkun = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { diizinkan ->
        sesudahDaftarAkun(if (diizinkan) akunGoogleDiHp(ctxMenu) else emptyList())
    }
    // Pemilih akun RESMI Google (disediakan Play Services): menampilkan
    // SEMUA akun Google di HP + pilihan tambah akun, tanpa tunduk pada
    // batas visibilitas AccountManager (enumerasi cuma memunculkan
    // frmchan0 di HP user — v1.54/v1.55 mentok). Hasilnya email akun
    // pilihan -> dialog otorisasi dikunci ke akun itu, TANPA browser.
    val peluncurPemilihGoogle = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { hasil ->
        val email = hasil.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
            ?: hasil.data?.getStringExtra("authAccount")
        if (hasil.resultCode == android.app.Activity.RESULT_OK && !email.isNullOrBlank()) {
            if (vm.daftarAkunDrive.any { it.email.equals(email, ignoreCase = true) }) {
                vm.beriTahu("Akun $email sudah masuk")
            } else {
                vm.loginDenganAkun(email)
            }
        }
    }
    fun mulaiTambahAkun() {
        val intentPemilih = runCatching {
            com.google.android.gms.common.AccountPicker.newChooseAccountIntent(
                com.google.android.gms.common.AccountPicker.AccountChooserOptions.Builder()
                    .setAllowableAccountsTypes(listOf("com.google"))
                    .setAlwaysShowAccountPicker(true)
                    .setTitleOverrideText("Pilih akun Google")
                    .build()
            )
        }.getOrNull()
        if (intentPemilih != null) {
            peluncurPemilihGoogle.launch(intentPemilih)
            return
        }
        // Cadangan (pemilih Google tak tersedia): enumerasi + dialog sendiri.
        val izin = androidx.core.content.ContextCompat.checkSelfPermission(
            ctxMenu, android.Manifest.permission.GET_ACCOUNTS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (izin) {
            sesudahDaftarAkun(akunGoogleDiHp(ctxMenu))
        } else {
            peluncurIzinAkun.launch(android.Manifest.permission.GET_ACCOUNTS)
        }
    }
    // === Pembaruan mandiri: cek rilis GitHub -> unduh APK -> pasang ===
    var statusUpdate by remember { mutableStateOf<String?>(null) }
    var sibukUpdate by remember { mutableStateOf(false) }
    val lingkupMenu = rememberCoroutineScope()
    suspend fun jalankanPembaruan() {
        try {
            statusUpdate = "Memeriksa pembaruan…"
            val (versiBaru, urlApk, ukuranApk) = withContext(Dispatchers.IO) {
                val koneksi = (URL("https://api.github.com/repos/frmdeveloper/gallery/releases/latest")
                    .openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20000
                    readTimeout = 20000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "GaleriFRM")
                }
                val teks = koneksi.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(teks)
                val tag = json.getString("tag_name").removePrefix("v")
                val aset = json.getJSONArray("assets")
                var url = ""
                var ukuran = 0L
                for (i in 0 until aset.length()) {
                    val a = aset.getJSONObject(i)
                    if (a.getString("name").endsWith(".apk")) {
                        url = a.getString("browser_download_url")
                        ukuran = a.getLong("size")
                        break
                    }
                }
                Triple(tag, url, ukuran)
            }
            val terpasang = runCatching {
                ctxMenu.packageManager.getPackageInfo(ctxMenu.packageName, 0).versionName ?: "0"
            }.getOrNull() ?: "0"
            if (urlApk.isBlank() || !versiLebihBaru(versiBaru, terpasang)) {
                vm.beriTahu("Udah versi terbaru ($terpasang)")
                statusUpdate = null
                return
            }
            if (!ctxMenu.packageManager.canRequestPackageInstalls()) {
                vm.beriTahu("Izinkan pasang aplikasi dari sumber ini dulu, terus ketuk lagi")
                runCatching {
                    ctxMenu.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + ctxMenu.packageName),
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                statusUpdate = null
                return
            }
            val berkas = File(ctxMenu.cacheDir, "update/GaleriFRM-v$versiBaru.apk")
            if (!berkas.exists() || berkas.length() != ukuranApk) {
                statusUpdate = "Mengunduh v$versiBaru…"
                withContext(Dispatchers.IO) {
                    berkas.parentFile?.mkdirs()
                    val koneksi = (URL(urlApk).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 20000
                        readTimeout = 30000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "GaleriFRM")
                    }
                    koneksi.inputStream.use { masuk ->
                        berkas.outputStream().use { keluar ->
                            val penyangga = ByteArray(64 * 1024)
                            var total = 0L
                            var persenLapor = -1
                            while (true) {
                                val n = masuk.read(penyangga)
                                if (n < 0) break
                                keluar.write(penyangga, 0, n)
                                total += n
                                if (ukuranApk > 0) {
                                    val persen = (total * 100 / ukuranApk).toInt()
                                    if (persen != persenLapor) {
                                        persenLapor = persen
                                        withContext(Dispatchers.Main) {
                                            statusUpdate = "Mengunduh v$versiBaru… $persen%"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            statusUpdate = null
            val uriApk = FileProvider.getUriForFile(
                ctxMenu, "id.frmdeveloper.gallery.fileprovider", berkas,
            )
            ctxMenu.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uriApk, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            vm.beriTahu("Gagal memeriksa pembaruan: ${e.message ?: "jaringan bermasalah"}")
            statusUpdate = null
        } finally {
            sibukUpdate = false
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(WarnaLatarMenu)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Menu",
            color = WarnaTeksMenu,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 2.dp),
        )

        // ===== Akun cloud: semua akun berkumpul dlm satu kartu =====
        LabelSeksi("Akun cloud")
        KartuMenu {
            if (vm.daftarAkunDrive.isEmpty()) {
                BarisMenu(
                    judul = "Google Drive",
                    sub = if (vm.loginMenunggu) {
                        "Menunggu login… ketuk buat batal"
                    } else {
                        "Belum terhubung — ketuk buat login"
                    },
                    onClick = {
                        // Login pertama juga lewat lembar pemilih akun Google
                        // (jalur yg sama kayak Tambah akun, v1.56) — bukan
                        // dialog otorisasi langsung yg milih akun sendiri.
                        if (vm.loginMenunggu) vm.batalLogin() else mulaiTambahAkun()
                    },
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_drive),
                        contentDescription = "Google Drive",
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                vm.daftarAkunDrive.forEachIndexed { idx, akun ->
                    if (idx > 0) PemisahMenu()
                    // Bentuk baris akun: ikon Drive kiri, email di atas,
                    // sisa penyimpanan Drive di bawahnya.
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { akunKeluar = akun.email }
                            .padding(horizontal = 20.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.logo_drive),
                                contentDescription = "Google Drive",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(akun.email, color = WarnaBiruTautan, fontSize = 16.sp)
                            val q = vm.kuotaDrive[akun.email]
                            Text(
                                when {
                                    q == null -> "Memeriksa penyimpanan…"
                                    q.first > 0L -> "Sisa ${formatUkuranBerkas((q.first - q.second).coerceAtLeast(0L))} dari ${formatUkuranBerkas(q.first)}"
                                    else -> "Terpakai ${formatUkuranBerkas(q.second)}"
                                },
                                color = WarnaSubMenu, fontSize = 13.sp,
                            )
                        }
                    }
                }
                PemisahMenu()
                BarisMenu(
                    judul = "Tambah akun",
                    sub = if (vm.loginMenunggu) "Menunggu login…" else null,
                    onClick = {
                        if (vm.loginMenunggu) vm.batalLogin() else mulaiTambahAkun()
                    },
                ) {
                    Icon(
                        Icons.Filled.Add, "Tambah akun",
                        tint = WarnaIkonMenu, modifier = Modifier.size(24.dp),
                    )
                }
                if (vm.driveLoading) {
                    PemisahMenu()
                    BarisMenu(judul = "Memuat isi Drive…") {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
                vm.driveError?.let { err ->
                    PemisahMenu()
                    BarisMenu(judul = "Drive bermasalah", sub = err) {
                        Icon(
                            Icons.Filled.Info, null,
                            tint = Color(0xFFD32F2F), modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        // ===== Umum =====
        LabelSeksi("Umum")
        KartuMenu {
            BarisMenu(
                judul = "Favorit",
                sub = "${vm.favorites.size} item",
                onClick = onOpenFavorites,
            ) {
                Icon(
                    Icons.Filled.Favorite, null,
                    tint = WarnaIkonMenu, modifier = Modifier.size(22.dp),
                )
            }
            PemisahMenu()
            BarisMenu(
                judul = "Sampah",
                sub = "Item yang dihapus (HP + Google Drive)",
                onClick = onOpenTrash,
            ) {
                Icon(
                    Icons.Filled.Delete, null,
                    tint = WarnaIkonMenu, modifier = Modifier.size(22.dp),
                )
            }
            if (vm.daftarAkunDrive.isNotEmpty()) {
                PemisahMenu()
                BarisMenu(
                    judul = "Segarkan Drive",
                    sub = "Pindai ulang semua akun",
                    onClick = { vm.reloadDrive() },
                ) {
                    Icon(
                        Icons.Filled.Refresh, null,
                        tint = WarnaIkonMenu, modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // ===== Penyimpanan lokal: bentuk baris sama kayak akun Drive =====
        val daftarVolume = remember { infoVolumePenyimpanan(ctxMenu) }
        if (daftarVolume.isNotEmpty()) {
            LabelSeksi("Penyimpanan")
            KartuMenu {
                daftarVolume.forEachIndexed { idx, vol ->
                    if (idx > 0) PemisahMenu()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                when (vol.jenis) {
                                    "internal" -> Icons.Filled.PhoneAndroid
                                    "sd" -> Icons.Filled.SdCard
                                    "usb" -> Icons.Filled.Usb
                                    else -> Icons.Filled.Storage
                                },
                                null, tint = WarnaIkonMenu, modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(vol.nama, color = WarnaTeksMenu, fontSize = 16.sp)
                            Text(
                                "Sisa ${formatUkuranBerkas(vol.bebas)} dari ${formatUkuranBerkas(vol.total)}",
                                color = WarnaSubMenu, fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }

        // ===== Lainnya =====
        LabelSeksi("Lainnya")
        KartuMenu {
            BarisMenu(
                judul = "Source GitHub",
                sub = "Galeri FRM 1.79",
                onClick = {
                    runCatching {
                        ctxMenu.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/frmdeveloper/gallery"),
                            ),
                        )
                    }
                },
            ) {
                Icon(
                    Icons.Filled.Code, null,
                    tint = WarnaIkonMenu, modifier = Modifier.size(22.dp),
                )
            }
            PemisahMenu()
            BarisMenu(
                judul = "Pembaruan aplikasi",
                sub = statusUpdate ?: "Ketuk buat memeriksa — unduh & pasang langsung",
                onClick = {
                    if (!sibukUpdate) {
                        sibukUpdate = true
                        lingkupMenu.launch { jalankanPembaruan() }
                    }
                },
            ) {
                if (sibukUpdate) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.Filled.Update, null,
                        tint = WarnaIkonMenu, modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_frm),
                contentDescription = null,
                modifier = Modifier.height(30.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "By Fauzan Rifki Maulana",
                color = WarnaSubMenu, fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(28.dp))
    }

    pilihAkun?.let { daftar ->
        AlertDialog(
            onDismissRequest = { pilihAkun = null },
            title = { Text("Pilih akun Google") },
            text = {
                Column {
                    if (daftar.isEmpty()) {
                        Text(
                            "Semua akun Google yg terbaca di HP ini sudah masuk. " +
                                "Akun lain tetap bisa ditambahkan:",
                            color = WarnaSubMenu, fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    daftar.forEach { email ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pilihAkun = null
                                    vm.loginDenganAkun(email)
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.AccountCircle, null,
                                tint = WarnaIkonMenu, modifier = Modifier.size(26.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(email, color = WarnaTeksMenu, fontSize = 15.sp)
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                pilihAkun = null
                                onLoginAkunLain()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Add, null,
                            tint = WarnaIkonMenu, modifier = Modifier.size(26.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Akun lain (tidak ada di HP ini)…",
                            color = WarnaTeksMenu, fontSize = 15.sp,
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pilihAkun = null }) { Text("Batal") }
            },
        )
    }
    akunKeluar?.let { email ->
        AlertDialog(
            onDismissRequest = { akunKeluar = null },
            title = { Text("Keluarkan akun?") },
            text = {
                Text(
                    "Akun $email akan dikeluarkan dari aplikasi ini. " +
                        "Isi Drive dari akun ini berhenti tampil; akun lain tidak terpengaruh."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.keluarAkunDrive(email)
                    akunKeluar = null
                }) { Text("Keluarkan") }
            },
            dismissButton = {
                TextButton(onClick = { akunKeluar = null }) { Text("Batal") }
            },
        )
    }
}


/**
 * Layar SAMPAH — dua sumber: sampah HP (MediaStore trashed) dan sampah
 * Google Drive (trashed=true). Ketuk/tekan item buat memilih, lalu
 * Pulihkan atau Hapus permanen dari bar bawah.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    sampahLokal: List<MediaItem>,
    sampahDrive: List<MediaItem>,
    driveTersambung: Boolean,
    loading: Boolean,
    onBack: () -> Unit,
    onPulihkan: (List<MediaItem>) -> Unit,
    onHapusPermanen: (List<MediaItem>) -> Unit,
    onSegarkan: () -> Unit,
) {
    val context = LocalContext.current
    var tabDrive by remember { mutableStateOf(false) }
    var pilihan by remember { mutableStateOf<Set<String>>(emptySet()) }
    var konfirmasiPermanen by remember { mutableStateOf<List<MediaItem>?>(null) }

    LaunchedEffect(Unit) { onSegarkan() }

    val items = if (tabDrive) sampahDrive else sampahLokal
    val terpilih = items.filter { it.key in pilihan }
    fun gantiTab(keDrive: Boolean) {
        tabDrive = keDrive
        pilihan = emptySet()
    }
    fun toggle(item: MediaItem) {
        pilihan = if (item.key in pilihan) pilihan - item.key else pilihan + item.key
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Sampah", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                }
            },
            actions = {
                IconButton(onClick = onSegarkan) {
                    Icon(Icons.Filled.Refresh, "Segarkan")
                }
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !tabDrive,
                onClick = { gantiTab(false) },
                label = { Text("Di HP ini (${sampahLokal.size})") },
            )
            FilterChip(
                selected = tabDrive,
                onClick = { gantiTab(true) },
                label = { Text("Google Drive (${sampahDrive.size})") },
            )
        }
        Text(
            text = if (tabDrive) {
                "Sampah Drive dikosongkan permanen oleh Google setelah 30 hari."
            } else {
                "Item di sampah HP terhapus permanen otomatis setelah 30 hari."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                loading && items.isEmpty() -> Text(
                    "Memuat sampah…",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                tabDrive && !driveTersambung -> Text(
                    "Google Drive belum terhubung.\nLogin dulu lewat tab Menu.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                items.isEmpty() -> Text(
                    if (tabDrive) "Sampah Drive kosong" else "Sampah kosong",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items, key = { it.key }) { item ->
                        val dipilih = item.key in pilihan
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(Color.LightGray)
                                .combinedClickable(
                                    onClick = { toggle(item) },
                                    onLongClick = { toggle(item) },
                                ),
                        ) {
                            val reqSampah = remember(item.key) {
                                val sumber = if (item.thumbUrl.isNotBlank()) item.thumbUrl else item.uri
                                val b = ImageRequest.Builder(context).data(sumber)
                                    .size(300, 300)
                                    .precision(Precision.INEXACT)
                                if (item.isVideo) b.videoFrameMillis(500)
                                b.crossfade(false).build()
                            }
                            AsyncImage(
                                model = reqSampah,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Text(
                                text = item.name,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                            if (item.isVideo) {
                                Icon(
                                    Icons.Filled.PlayArrow, null, tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center).size(30.dp),
                                )
                            }
                            if (dipilih) {
                                Box(
                                    Modifier.fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                )
                                Icon(
                                    Icons.Filled.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        if (terpilih.isNotEmpty()) {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${terpilih.size} dipilih", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { pilihan = emptySet() }) { Text("Batal") }
                        Button(onClick = {
                            val t = terpilih
                            pilihan = emptySet()
                            onPulihkan(t)
                        }) { Text("Pulihkan") }
                        TextButton(onClick = { konfirmasiPermanen = terpilih }) {
                            Text("Hapus permanen", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    konfirmasiPermanen?.let { targets ->
        AlertDialog(
            onDismissRequest = { konfirmasiPermanen = null },
            title = { Text("Hapus permanen?") },
            text = { Text("${targets.size} item akan dihapus selamanya dan TIDAK bisa dipulihkan.") },
            confirmButton = {
                TextButton(onClick = {
                    konfirmasiPermanen = null
                    pilihan = emptySet()
                    onHapusPermanen(targets)
                }) { Text("Hapus permanen", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiPermanen = null }) { Text("Batal") }
            },
        )
    }
}

// ---------- MUSIK: sampul, daftar, pemutar, mini player ----------

/** Pasangan warna gradasi CERAH buat cover kosong — stabil per lagu (dari kuncinya). */
fun pasanganGradasiCerah(kunci: String): Pair<Color, Color> {
    val palet = listOf(
        Color(0xFFFF8A65) to Color(0xFFFF4081),
        Color(0xFF4FC3F7) to Color(0xFF7C4DFF),
        Color(0xFF69F0AE) to Color(0xFF00B0FF),
        Color(0xFFFFD54F) to Color(0xFFFF7043),
        Color(0xFFEA80FC) to Color(0xFFEC407A),
        Color(0xFF4DD0E1) to Color(0xFF536DFE),
        Color(0xFFFFAB40) to Color(0xFFFF5252),
        Color(0xFF82B1FF) to Color(0xFF448AFF),
    )
    return palet[kunci.hashCode().mod(palet.size)]
}

/** Kotak cover art lagu; cover kosong = gradasi cerah + ikon nada. */
@Composable
fun SampulLagu(lagu: AudioItem, ukuran: Dp, radius: Dp = 8.dp, versi: Int = 0) {
    val ctx = LocalContext.current
    val px = with(LocalDensity.current) { ukuran.roundToPx() }
    val (g1, g2) = pasanganGradasiCerah(lagu.key)
    Box(
        modifier = Modifier
            .size(ukuran)
            .clip(RoundedCornerShape(radius))
            // Cover kosong TIDAK gelap lagi: gradasi CERAH per lagu di baliknya;
            // cover asli (kalau ada) menutupi ini penuh.
            .background(Brush.linearGradient(listOf(g1, g2))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.MusicNote, null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(ukuran / 2),
        )
        if (lagu.path.isNotBlank() || lagu.artUri != Uri.EMPTY) {
            AsyncImage(
                model = remember(lagu.key, px, versi) {
                    ImageRequest.Builder(ctx).data(CoverLagu(lagu))
                        .memoryCacheKey("${lagu.key}#coverv$versi")
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .size(px, px)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build()
                },
                contentDescription = lagu.album,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// SATU urutan buat SELURUH Audio (perintah user): ganti di tab langsung
// ngubah folder, isi folder, & hasil cari sekaligus. Tanpa "Urutan bawaan".
private val LABEL_URUTAN_AUDIO = listOf(
    "Nama A–Z", "Nama Z–A", "Tanggal terbaru", "Tanggal terlama",
    "Paling banyak isinya", "Durasi terpanjang", "Durasi terpendek",
)

private fun urutkanLagu(daftar: List<AudioItem>, urutan: Int): List<AudioItem> = when (urutan) {
    2 -> daftar.sortedByDescending { it.title.lowercase() }
    3 -> daftar.sortedByDescending { it.dateMillis }
    4 -> daftar.sortedBy { it.dateMillis }
    // "Paling banyak isinya" gak berlaku buat lagu -> samakan A-Z
    5 -> daftar.sortedBy { it.title.lowercase() }
    6 -> daftar.sortedByDescending { it.durationMs }
    7 -> daftar.sortedBy { it.durationMs }
    else -> daftar.sortedBy { it.title.lowercase() }
}

private fun urutkanFolderAudio(daftar: List<AudioFolder>, urutan: Int): List<AudioFolder> = when (urutan) {
    2 -> daftar.sortedByDescending { it.name.lowercase() }
    3 -> daftar.sortedByDescending { f -> f.items.maxOfOrNull { it.dateMillis } ?: 0L }
    4 -> daftar.sortedBy { f -> f.items.maxOfOrNull { it.dateMillis } ?: 0L }
    5 -> daftar.sortedByDescending { it.count }
    6 -> daftar.sortedByDescending { f -> f.items.sumOf { it.durationMs } }
    7 -> daftar.sortedBy { f -> f.items.sumOf { it.durationMs } }
    else -> daftar.sortedBy { it.name.lowercase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuUrutanAudio(urutan: Int, onPilih: (Int) -> Unit) {
    var terbuka by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { terbuka = true }) {
            Icon(Icons.Filled.MoreVert, "Urutkan")
        }
        DropdownMenu(expanded = terbuka, onDismissRequest = { terbuka = false }) {
            LABEL_URUTAN_AUDIO.forEachIndexed { i, label ->
                DropdownMenuItem(
                    text = { Text((if (urutan == i + 1) "✓ " else "") + label) },
                    onClick = { terbuka = false; onPilih(i + 1) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AudioScreen(
    vm: GalleryViewModel,
    izinAudio: Boolean,
    onMintaIzin: () -> Unit,
    onOpenFolder: (AudioFolder) -> Unit,
    onOpenPlayer: () -> Unit = {},
) {
    val ctxAudio = LocalContext.current
    var kolom by rememberSaveable { mutableIntStateOf(UiStore.loadInt(ctxAudio, "kolom_audio", 3)) }
    var modeDaftar by rememberSaveable { mutableStateOf(UiStore.loadBool(ctxAudio, "daftar_audio", false)) }
    LaunchedEffect(kolom, modeDaftar) {
        UiStore.saveInt(ctxAudio, "kolom_audio", kolom)
        UiStore.saveBool(ctxAudio, "daftar_audio", modeDaftar)
    }
    val statusDaftarAudio = rememberLazyListState()
    val statusGridAudio = rememberLazyGridState()
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(izinAudio) {
        if (izinAudio) vm.reloadAudio()
    }
    var urutanAudio by remember { mutableIntStateOf(UiStore.loadInt(ctxAudio, "urutan_audio", 1)) }
    LaunchedEffect(urutanAudio) { UiStore.saveInt(ctxAudio, "urutan_audio", urutanAudio) }
    val tampil = remember(vm.audioFolders, urutanAudio) {
        urutkanFolderAudio(vm.audioFolders, urutanAudio)
    }
    // Pencarian di tab Audio = cari FILE audio (judul/artis/album), BUKAN
    // nama folder (perintah user); folder cuma tampil pas tidak mencari.
    val hasilCari = remember(vm.laguDaftar, query, urutanAudio) {
        if (query.isBlank()) {
            emptyList()
        } else {
            urutkanLagu(
                vm.laguDaftar.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
                },
                urutanAudio,
            )
        }
    }

    // Mode pilih lagu (tap & tahan, kayak foto)
    var pilihanLagu by remember { mutableStateOf<Set<String>>(emptySet()) }
    val memilihLagu = pilihanLagu.isNotEmpty()
    var rincianLagu by remember { mutableStateOf<AudioItem?>(null) }
    val penghapusLagu = remember { mutableStateOf<List<AudioItem>>(emptyList()) }
    val peluncurHapusLagu = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { hasil ->
        val target = penghapusLagu.value
        penghapusLagu.value = emptyList()
        if (hasil.resultCode == android.app.Activity.RESULT_OK && target.isNotEmpty()) {
            vm.beriTahu("${target.size} lagu dihapus")
            vm.reloadAudio()
        }
    }
    BackHandler(enabled = memilihLagu) { pilihanLagu = emptySet() }
    fun aksiHapusLagu(daftar: List<AudioItem>) {
        val lokal = daftar.filter { it.akunDrive.isBlank() }
        if (lokal.isEmpty()) {
            vm.beriTahu("Lagu Drive tidak bisa dihapus dari sini")
            return
        }
        if (lokal.size < daftar.size) {
            vm.beriTahu("${daftar.size - lokal.size} lagu Drive dilewati")
        }
        penghapusLagu.value = lokal
        val permintaan = MediaStore.createDeleteRequest(
            ctxAudio.contentResolver, lokal.map { it.uri },
        )
        peluncurHapusLagu.launch(
            IntentSenderRequest.Builder(permintaan.intentSender).build()
        )
    }
    IngatPosisiList(statusDaftarAudio, "audio_daftar", tampil.size, aktif = query.isBlank())
    IngatPosisiGrid(statusGridAudio, "audio_grid", tampil.size, aktif = query.isBlank())
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Audio", fontWeight = FontWeight.Bold) },
            actions = {
                Text(
                    "${vm.laguDaftar.size} lagu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp),
                )
                MenuUrutanAudio(urutanAudio) { urutanAudio = it }
            },
        )
        if (izinAudio) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cari lagu, artis, album…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )
            Text(
                if (query.isBlank()) "Semua folder" else "Hasil pencarian",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
            )
        }
        when {
            !izinAudio -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Izin audio belum diberikan",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onMintaIzin) { Text("Izinkan akses audio") }
                }
            }
            query.isNotBlank() -> if (hasilCari.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (vm.audioSedangDimuat) "Memuat audio…" else "Tidak ada lagu yang cocok",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 170.dp),
                ) {
                    items(hasilCari, key = { it.key }) { lagu ->
                        BarisLagu(
                            vm, lagu,
                            terpilih = lagu.key in pilihanLagu,
                            memilih = memilihLagu,
                            onLongClick = { pilihanLagu = pilihanLagu + lagu.key },
                        ) {
                            if (memilihLagu) {
                                pilihanLagu = if (lagu.key in pilihanLagu) {
                                    pilihanLagu - lagu.key
                                } else {
                                    pilihanLagu + lagu.key
                                }
                            } else if (vm.laguDimainkan?.key == lagu.key) {
                                onOpenPlayer()
                            } else {
                                vm.putarLagu(lagu, hasilCari)
                                onOpenPlayer()
                            }
                        }
                    }
                }
            }
            // Masih memuat (buka tab pertama kali / lagi sinkron): spinner
            // dulu — tulisan "tidak ada" cuma kalau muatan KELAR & kosong
            vm.audioSedangDimuat && tampil.isEmpty() ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Memuat audio…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            tampil.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Tidak ada file musik di HP ini",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            modeDaftar -> LazyColumn(
                state = statusDaftarAudio,
                modifier = Modifier
                    .fillMaxSize()
                    .animasiZoom(kunci = modeDaftar, membesar = false)
                    .pointerInput(Unit) {
                        detectCubitGrid { zoomIn ->
                            if (zoomIn) {
                                modeDaftar = false
                                kolom = 3
                            }
                        }
                    },
                contentPadding = PaddingValues(top = 6.dp, bottom = 170.dp),
            ) {
                items(tampil, key = { it.id }) { folder ->
                    Row(
                        modifier = Modifier
                            .animateItem(placementSpec = spring(dampingRatio = 0.9f, stiffness = 300f))
                            .fillMaxWidth()
                            .combinedClickable(onClick = { onOpenFolder(folder) }, onLongClick = {})
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        folder.cover?.let { SampulLagu(it, 74.dp, radius = 16.dp, versi = vm.versiCover) }
                            ?: Box(
                                Modifier
                                    .size(74.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                folder.name, fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${folder.count} lagu" + if (folder.sumber == "drive") " • Drive" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            else -> LazyVerticalGrid(
                state = statusGridAudio,
                columns = GridCells.Fixed(kolom),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectCubitGrid { zoomIn ->
                            when {
                                zoomIn -> kolom = 2
                                kolom == 2 -> kolom = 3
                                else -> modeDaftar = true
                            }
                        }
                    },
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 170.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(tampil, key = { it.id }) { folder ->
                    Column(
                        modifier = Modifier
                            .animateItem(placementSpec = spring(dampingRatio = 0.9f, stiffness = 300f))
                            .combinedClickable(onClick = { onOpenFolder(folder) }, onLongClick = {}),
                    ) {
                        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
                            folder.cover?.let {
                                SampulLagu(it, ukuran = maxWidth, radius = 18.dp, versi = vm.versiCover)
                            } ?: Box(
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            folder.name, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${folder.count} lagu" + if (folder.sumber == "drive") " • Drive" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
    val daftarDipilihLagu = hasilCari.filter { it.key in pilihanLagu }
    KromPilihLaguMengambang(
        tampak = memilihLagu,
        count = pilihanLagu.size,
        semuaFavorit = daftarDipilihLagu.isNotEmpty() &&
            daftarDipilihLagu.all { it.key in vm.favorites },
        satuDipilih = daftarDipilihLagu.size == 1,
        bisaUbahSampul = daftarDipilihLagu.size == 1 &&
            daftarDipilihLagu.first().akunDrive.isBlank(),
        onClose = { pilihanLagu = emptySet() },
        onPutar = {
            val d = daftarDipilihLagu
            if (d.isNotEmpty()) {
                pilihanLagu = emptySet()
                vm.putarLagu(d.first(), d)
                onOpenPlayer()
            }
        },
        onShare = {
            val d = daftarDipilihLagu
            if (d.any { it.akunDrive.isBlank() }) {
                shareLagu(ctxAudio, d)
                pilihanLagu = emptySet()
            } else {
                vm.beriTahu("Lagu Drive tidak bisa dibagikan langsung")
            }
        },
        onDelete = {
            val d = daftarDipilihLagu
            pilihanLagu = emptySet()
            aksiHapusLagu(d)
        },
        onFavorit = {
            val d = daftarDipilihLagu
            val target = d.isNotEmpty() && !d.all { it.key in vm.favorites }
            d.forEach {
                if ((it.key in vm.favorites) != target) vm.toggleFavoritLagu(it)
            }
            pilihanLagu = emptySet()
        },
        onUbahSampul = {
            val l = daftarDipilihLagu.firstOrNull()
            pilihanLagu = emptySet()
            if (l != null) vm.mulaiGantiCover(l)
        },
        onRincian = { rincianLagu = daftarDipilihLagu.firstOrNull() },
    )
    DialogRincianLagu(rincianLagu) { rincianLagu = null }
    }
}

/** Krom pilih LAGU mengambang — pola sama persis KromPilihMengambang foto:
 * pil jumlah + Batal di atas, pil aksi di bawah, kartu Lainnya meluncur naik.
 * Semua AnimatedVisibility selalu ter-compose, visibilitas dari flag saja. */
@Composable
fun KromPilihLaguMengambang(
    tampak: Boolean,
    count: Int,
    semuaFavorit: Boolean,
    satuDipilih: Boolean,
    bisaUbahSampul: Boolean,
    onClose: () -> Unit,
    onPutar: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onFavorit: () -> Unit,
    onUbahSampul: () -> Unit,
    onRincian: () -> Unit,
) {
    var menuLainnya by remember { mutableStateOf(false) }
    LaunchedEffect(tampak) { if (!tampak) menuLainnya = false }
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = tampak,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(220)) +
                slideInVertically(tween(300, easing = FastOutSlowInEasing)) { -it / 2 },
            exit = fadeOut(tween(180)) +
                slideOutVertically(tween(240)) { -it / 2 },
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .height(40.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Circle, null, tint = Color(0xFF191919),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$count", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF191919),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .height(40.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .clickable(onClick = onClose)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Batal", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        color = Color(0xFF191919),
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = menuLainnya,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { menuLainnya = false },
            )
        }
        AnimatedVisibility(
            visible = menuLainnya,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(200)) +
                slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it },
            exit = fadeOut(tween(160)) +
                slideOutVertically(tween(260)) { it },
        ) {
            Column(
                Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 156.dp)
                    .fillMaxWidth(0.8f)
                    .widthIn(max = 340.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(vertical = 10.dp),
            ) {
                if (bisaUbahSampul) {
                    ItemMenuLainnya("Ubah sampul") { menuLainnya = false; onUbahSampul() }
                }
                if (satuDipilih) {
                    ItemMenuLainnya("Rincian") { menuLainnya = false; onRincian() }
                }
            }
        }
        AnimatedVisibility(
            visible = tampak,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(220)) +
                slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it },
            exit = fadeOut(tween(180)) +
                slideOutVertically(tween(240)) { it },
        ) {
            Row(
                Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ItemBarPilih(Icons.Filled.PlayArrow, "Putar") { onPutar() }
                ItemBarPilih(Icons.Filled.Share, "Berbagi") { onShare() }
                ItemBarPilih(
                    if (semuaFavorit) Icons.Filled.Star else Icons.Filled.StarBorder,
                    "Favorit",
                ) { onFavorit() }
                ItemBarPilih(Icons.Filled.Delete, "Hapus") { onDelete() }
                if (satuDipilih) {
                    ItemBarPilih(Icons.Filled.MoreVert, "Lainnya") { menuLainnya = !menuLainnya }
                }
            }
        }
    }
}

/** Dialog rincian satu lagu (dipakai mode pilih audio). */
@Composable
fun DialogRincianLagu(lagu: AudioItem?, onDismiss: () -> Unit) {
    if (lagu == null) return
    val berkasLokal = remember(lagu.key) {
        if (lagu.akunDrive.isBlank() && lagu.path.isNotBlank()) {
            java.io.File(lagu.path).takeIf { it.exists() }
        } else null
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rincian") },
        text = {
            Text(
                buildString {
                    append("Judul: ${lagu.title}\n")
                    append("Artis: ${lagu.artist}\n")
                    if (lagu.album.isNotBlank()) append("Album: ${lagu.album}\n")
                    append("Durasi: ${formatDurasi(lagu.durationMs)}\n")
                    append(
                        "Sumber: " + if (lagu.akunDrive.isNotBlank()) {
                            "Google Drive (${lagu.akunDrive})"
                        } else {
                            "Perangkat"
                        }
                    )
                    if (lagu.path.isNotBlank()) append("\nLokasi: ${lagu.path}")
                    berkasLokal?.let {
                        append("\nUkuran: ${formatUkuranBerkas(it.length())}")
                    }
                    if (lagu.dateMillis > 0) {
                        append(
                            "\nTanggal: " + java.text.SimpleDateFormat(
                                "d MMM yyyy", java.util.Locale("id", "ID"),
                            ).format(java.util.Date(lagu.dateMillis))
                        )
                    }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        },
    )
}


/** Satu baris lagu (dipakai daftar lagu per folder & hasil cari audio).
 * Tap & tahan = masuk MODE PILIH kayak foto (perintah user), bukan menu. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BarisLagu(
    vm: GalleryViewModel,
    lagu: AudioItem,
    terpilih: Boolean = false,
    memilih: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    val aktif = vm.laguDimainkan?.key == lagu.key
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (terpilih) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            SampulLagu(lagu, 52.dp, versi = vm.versiCover)
            if (memilih) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(20.dp)
                        .then(
                            if (terpilih) {
                                Modifier.background(Color(0xFF0381FE), CircleShape)
                            } else {
                                Modifier
                                    .background(Color.White, CircleShape)
                                    .border(1.5.dp, Color(0xFF9E9E9E), CircleShape)
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (terpilih) {
                        Icon(
                            Icons.Filled.Check, null, tint = Color.White,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                lagu.title,
                fontWeight = if (aktif) FontWeight.Bold else FontWeight.Medium,
                color = if (aktif) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                // Audio Drive durasinya 0 dari API — jangan tampilkan "• 0:00"
                if (lagu.durationMs > 0) "${lagu.artist} • ${formatDurasi(lagu.durationMs)}" else lagu.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (aktif) {
            Icon(
                if (vm.muterLagu) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTracksScreen(
    vm: GalleryViewModel,
    folder: AudioFolder,
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    val ctxTracks = LocalContext.current
    // Ambil versi segar dari VM (reload bisa mengubah isi folder)
    val folderKini = vm.audioFolders.firstOrNull { it.id == folder.id } ?: folder
    var query by rememberSaveable { mutableStateOf("") }
    var urutanAudio by remember { mutableIntStateOf(UiStore.loadInt(ctxTracks, "urutan_audio", 1)) }
    LaunchedEffect(urutanAudio) { UiStore.saveInt(ctxTracks, "urutan_audio", urutanAudio) }
    val tampil = remember(folderKini.items, query, urutanAudio) {
        val dasar = if (query.isBlank()) {
            folderKini.items
        } else {
            folderKini.items.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
            }
        }
        urutkanLagu(dasar, urutanAudio)
    }

    // Mode pilih lagu (tap & tahan, kayak foto)
    var pilihanLagu by remember { mutableStateOf<Set<String>>(emptySet()) }
    val memilihLagu = pilihanLagu.isNotEmpty()
    var rincianLagu by remember { mutableStateOf<AudioItem?>(null) }
    val penghapusLagu = remember { mutableStateOf<List<AudioItem>>(emptyList()) }
    val peluncurHapusLagu = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { hasil ->
        val target = penghapusLagu.value
        penghapusLagu.value = emptyList()
        if (hasil.resultCode == android.app.Activity.RESULT_OK && target.isNotEmpty()) {
            vm.beriTahu("${target.size} lagu dihapus")
            vm.reloadAudio()
        }
    }
    BackHandler(enabled = memilihLagu) { pilihanLagu = emptySet() }
    fun aksiHapusLagu(daftar: List<AudioItem>) {
        val lokal = daftar.filter { it.akunDrive.isBlank() }
        if (lokal.isEmpty()) {
            vm.beriTahu("Lagu Drive tidak bisa dihapus dari sini")
            return
        }
        if (lokal.size < daftar.size) {
            vm.beriTahu("${daftar.size - lokal.size} lagu Drive dilewati")
        }
        penghapusLagu.value = lokal
        val permintaan = MediaStore.createDeleteRequest(
            ctxTracks.contentResolver, lokal.map { it.uri },
        )
        peluncurHapusLagu.launch(
            IntentSenderRequest.Builder(permintaan.intentSender).build()
        )
    }
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    folderKini.name, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                }
            },
            actions = {
                Text(
                    "${folderKini.count} lagu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp),
                )
                MenuUrutanAudio(urutanAudio) { urutanAudio = it }
            },
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Cari lagu, artis, album…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (tampil.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Tidak ada lagu yang cocok",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 170.dp),
            ) {
                items(tampil, key = { it.key }) { lagu ->
                    BarisLagu(
                        vm, lagu,
                        terpilih = lagu.key in pilihanLagu,
                        memilih = memilihLagu,
                        onLongClick = { pilihanLagu = pilihanLagu + lagu.key },
                    ) {
                        if (memilihLagu) {
                            pilihanLagu = if (lagu.key in pilihanLagu) {
                                pilihanLagu - lagu.key
                            } else {
                                pilihanLagu + lagu.key
                            }
                        } else if (vm.laguDimainkan?.key == lagu.key) {
                            // Lagu yg SAMA lagi diputar: jangan diulang
                            // dari awal — langsung masuk pemutarnya aja
                            onOpenPlayer()
                        } else {
                            vm.putarLagu(lagu, tampil)
                            onOpenPlayer()
                        }
                    }
                }
            }
        }
    }
    val daftarDipilihLagu = tampil.filter { it.key in pilihanLagu }
    KromPilihLaguMengambang(
        tampak = memilihLagu,
        count = pilihanLagu.size,
        semuaFavorit = daftarDipilihLagu.isNotEmpty() &&
            daftarDipilihLagu.all { it.key in vm.favorites },
        satuDipilih = daftarDipilihLagu.size == 1,
        bisaUbahSampul = daftarDipilihLagu.size == 1 &&
            daftarDipilihLagu.first().akunDrive.isBlank(),
        onClose = { pilihanLagu = emptySet() },
        onPutar = {
            val d = daftarDipilihLagu
            if (d.isNotEmpty()) {
                pilihanLagu = emptySet()
                vm.putarLagu(d.first(), d)
                onOpenPlayer()
            }
        },
        onShare = {
            val d = daftarDipilihLagu
            if (d.any { it.akunDrive.isBlank() }) {
                shareLagu(ctxTracks, d)
                pilihanLagu = emptySet()
            } else {
                vm.beriTahu("Lagu Drive tidak bisa dibagikan langsung")
            }
        },
        onDelete = {
            val d = daftarDipilihLagu
            pilihanLagu = emptySet()
            aksiHapusLagu(d)
        },
        onFavorit = {
            val d = daftarDipilihLagu
            val target = d.isNotEmpty() && !d.all { it.key in vm.favorites }
            d.forEach {
                if ((it.key in vm.favorites) != target) vm.toggleFavoritLagu(it)
            }
            pilihanLagu = emptySet()
        },
        onUbahSampul = {
            val l = daftarDipilihLagu.firstOrNull()
            pilihanLagu = emptySet()
            if (l != null) vm.mulaiGantiCover(l)
        },
        onRincian = { rincianLagu = daftarDipilihLagu.firstOrNull() },
    )
    DialogRincianLagu(rincianLagu) { rincianLagu = null }
    }
}

@Composable
fun MiniPlayerBar(
    vm: GalleryViewModel,
    onOpenPlayer: () -> Unit,
) {
    val lagu = vm.laguDimainkan ?: return
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onOpenPlayer).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SampulLagu(lagu, 48.dp, versi = vm.versiCover)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    lagu.title, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    lagu.artist, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { vm.laguSebelum() }) {
                Icon(Icons.Filled.SkipPrevious, "Sebelumnya")
            }
            IconButton(onClick = { vm.toggleLagu() }) {
                Icon(
                    if (vm.muterLagu) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (vm.muterLagu) "Jeda" else "Putar",
                    modifier = Modifier.size(32.dp),
                )
            }
            IconButton(onClick = { vm.laguBerikut() }) {
                Icon(Icons.Filled.SkipNext, "Berikutnya")
            }
            IconButton(onClick = { vm.tutupPemutar() }) {
                Icon(
                    Icons.Filled.Close, "Tutup pemutar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Bar geser TIPIS ala iOS (seek & volume) — bukan Slider Material yg gendut. */
@Composable
fun BarGeserTipis(
    fraksi: Float,
    onGeser: (Float) -> Unit,
    onSelesai: () -> Unit,
    tinggi: Dp = 5.dp,
    knob: Boolean = true,
) {
    var lebar by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .onSizeChanged { lebar = it.width }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { onSelesai() },
                    onDragCancel = { onSelesai() },
                ) { change, _ ->
                    change.consume()
                    if (lebar > 0) onGeser((change.position.x / lebar).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    if (lebar > 0) {
                        onGeser((pos.x / lebar).coerceIn(0f, 1f))
                        onSelesai()
                    }
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier.fillMaxWidth().height(tinggi)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.28f)),
        )
        Box(
            Modifier.fillMaxWidth(fraksi.coerceIn(0f, 1f)).height(tinggi)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White),
        )
        if (knob) {
            Box(
                Modifier
                    .offset {
                        IntOffset(
                            (lebar * fraksi.coerceIn(0f, 1f)).toInt() - 6.dp.roundToPx(),
                            0,
                        )
                    }
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

/** Latar pemutar: cover diblur (ngikut cover lagu) — cover kosong = gradasi warna, BUKAN hitam polos. */
@Composable
private fun LatarPemutar(lagu: AudioItem?, versi: Int = 0) {
    // Basis SELALU gradasi CERAH + blob cahaya. Cover asli (kalau ada DAN
    // berhasil dimuat) ditumpuk di atasnya dlm keadaan blur. artUri lagu
    // lokal bisa non-kosong padahal gambarnya TIDAK ada (URI albumart
    // MediaStore tanpa berkas di baliknya) — onError menyerah balik ke
    // gradasi ini (dulu jalur itu bikin SELURUH layar hitam).
    var artGagal by remember(lagu?.key, versi) { mutableStateOf(false) }
    val ctxLatar = LocalContext.current
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF7C4DFF), Color(0xFF2979FF), Color(0xFF00BFA5)),
            ),
        ),
    )
    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(
                listOf(Color(0xFFFF80AB).copy(alpha = 0.38f), Color.Transparent),
                center = Offset(120f, 240f),
                radius = 620f,
            ),
        ),
    )
    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(
                listOf(Color(0xFFFFFF8D).copy(alpha = 0.30f), Color.Transparent),
                center = Offset(950f, 1900f),
                radius = 700f,
            ),
        ),
    )
    if (lagu != null && (lagu.path.isNotBlank() || lagu.artUri != Uri.EMPTY) && !artGagal) {
        AsyncImage(
            model = remember(lagu.key, versi) {
                ImageRequest.Builder(ctxLatar).data(CoverLagu(lagu))
                    .memoryCacheKey("${lagu.key}#coverv$versi")
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build()
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.35f) }),
            onError = { artGagal = true },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.8f
                    scaleY = 1.8f
                    if (Build.VERSION.SDK_INT >= 31) {
                        renderEffect = BlurEffect(120f, 120f, TileMode.Clamp)
                    }
                },
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.30f)))
    } else {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.10f)))
    }
}

@Composable
fun PlayerScreen(
    vm: GalleryViewModel,
    onBack: () -> Unit,
) {
    val lagu = vm.laguDimainkan
    var geser by remember { mutableStateOf<Float?>(null) }
    val ctx = LocalContext.current
    val audioManager = remember {
        ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maksVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var volume by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
    // Sinkronkan slider volume dgn tombol fisik (posisi lagu berubah 2x/detik)
    LaunchedEffect(vm.posisiLagu) {
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
    }
    val namaOutput = remember(vm.laguDimainkan?.key) {
        runCatching {
            val bt = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .firstOrNull {
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                }
            bt?.productName?.toString()?.takeIf { it.isNotBlank() } ?: "Speaker"
        }.getOrDefault("Speaker")
    }

    // Tarik ke bawah buat nutup pemutar (kayak sheet iOS): konten ngikutin
    // jari; lepas di atas 260px -> tutup, di bawah itu mental balik.
    val tarikPemutar = remember { Animatable(0f) }
    val cakupanPemutar = rememberCoroutineScope()
    // MELAYANG: akar transparan (layar di belakangnya = daftar lagu asli),
    // lembaran pemutarnya dikasih jarak atas + sudut atas membulat + klip.
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Latar blur cover LAYAR PENUH dari tepi atas layar: dulu latar
        // ini tinggal DI DALAM lembaran, di belakangnya peredup hitam 45%
        // — bagian atas layar jadi kelihatan abu-abu (perintah user
        // 2026-11-05: background pemutar harus full screen).
        LatarPemutar(lagu, vm.versiCover)
      Box(
        Modifier.fillMaxSize()
            .graphicsLayer { translationY = tarikPemutar.value }
            .padding(top = 14.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        cakupanPemutar.launch {
                            if (tarikPemutar.value > 260f) onBack()
                            else tarikPemutar.animateTo(
                                0f, spring(dampingRatio = 0.8f, stiffness = 380f),
                            )
                        }
                    },
                    onDragCancel = {
                        cakupanPemutar.launch {
                            tarikPemutar.animateTo(
                                0f, spring(dampingRatio = 0.8f, stiffness = 380f),
                            )
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount > 0f || tarikPemutar.value > 0f) {
                            change.consume()
                            cakupanPemutar.launch {
                                tarikPemutar.snapTo(
                                    (tarikPemutar.value + dragAmount).coerceAtLeast(0f),
                                )
                            }
                        }
                    },
                )
            },
      ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // Bar atas tipis: tombol turun/tutup ala iOS
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown, "Tutup pemutar",
                        tint = Color.White, modifier = Modifier.size(30.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.beriTahu("Antrean putar = isi folder yang dibuka") }) {
                    Icon(Icons.Filled.QueueMusic, "Antrean", tint = Color.White.copy(alpha = 0.85f))
                }
            }

            if (lagu == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada lagu yang diputar", color = Color.White.copy(alpha = 0.7f))
                }
                return@Column
            }

            Spacer(Modifier.weight(0.85f))

            // === KARTU KACA ala iOS — proporsi DIUKUR dari screenshot user:
            // lebar kartu ±79% layar, radius ±30dp, cover inset ±18dp radius 12dp.
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.34f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.13f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Cover inset
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            // Latar SELALU gradasi cerah per lagu: artUri bisa
                            // non-kosong tapi gambarnya tidak ada -> muat gagal
                            // dan yg tampil ya latar ini (cover asli menutupi
                            // penuh pas berhasil dimuat).
                            .background(
                                Brush.linearGradient(
                                    pasanganGradasiCerah(lagu.key).let { (a, b) -> listOf(a, b) },
                                ),
                            )
                            .clickable { vm.mulaiGantiCover(lagu) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.MusicNote, null,
                            tint = Color.White.copy(alpha = 0.92f),
                            modifier = Modifier.size(96.dp),
                        )
                        if (lagu.artUri != Uri.EMPTY) {
                            AsyncImage(
                                model = remember(lagu.key, vm.versiCover) {
                                    ImageRequest.Builder(ctx).data(CoverLagu(lagu))
                                        .memoryCacheKey("${lagu.key}#coverv${vm.versiCover}")
                                        .diskCachePolicy(CachePolicy.DISABLED)
                                        .build()
                                },
                                contentDescription = lagu.album,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        if (lagu.uri.scheme == "content") {
                            Box(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Edit, "Ganti cover",
                                    tint = Color.White, modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    var menuLainLagu by remember { mutableStateOf(false) }
                    var rincianLagu by remember { mutableStateOf(false) }
                    // Judul + artis (kiri) • ikon gelombang (kanan)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                lagu.title, fontSize = 19.sp, fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                lagu.artist, fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.55f),
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(onClick = { vm.beriTahu("Lirik belum tersedia") }) {
                            Icon(
                                Icons.Filled.GraphicEq, "Lirik",
                                tint = Color.White.copy(alpha = 0.75f),
                            )
                        }
                        Box {
                            IconButton(onClick = { menuLainLagu = true }) {
                                Icon(
                                    Icons.Filled.MoreVert, "Lainnya",
                                    tint = Color.White.copy(alpha = 0.75f),
                                )
                            }
                            DropdownMenu(
                                expanded = menuLainLagu,
                                onDismissRequest = { menuLainLagu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Acak") },
                                    leadingIcon = { Icon(Icons.Filled.Shuffle, null) },
                                    trailingIcon = if (vm.acakLagu) {
                                        { Icon(Icons.Filled.Check, null) }
                                    } else null,
                                    onClick = { vm.toggleAcakLagu(); menuLainLagu = false },
                                )
                                DropdownMenuItem(
                                    text = { Text("Favorit") },
                                    leadingIcon = {
                                        Icon(
                                            if (lagu.key in vm.favorites) Icons.Filled.Star else Icons.Filled.StarBorder,
                                            null,
                                        )
                                    },
                                    trailingIcon = if (lagu.key in vm.favorites) {
                                        { Icon(Icons.Filled.Check, null) }
                                    } else null,
                                    onClick = { vm.toggleFavoritLagu(lagu); menuLainLagu = false },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Putar ulang: " + when (vm.ulangLagu) {
                                                1 -> "Semua"
                                                2 -> "Satu"
                                                else -> "Mati"
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (vm.ulangLagu == 2) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                            null,
                                        )
                                    },
                                    onClick = { vm.siklusUlangLagu(); menuLainLagu = false },
                                )
                                DropdownMenuItem(
                                    text = { Text("Rincian") },
                                    leadingIcon = { Icon(Icons.Filled.Info, null) },
                                    onClick = { menuLainLagu = false; rincianLagu = true },
                                )
                            }
                        }
                    }
                    if (rincianLagu) {
                        val berkasLokal = remember(lagu.key) {
                            if (lagu.akunDrive.isBlank() && lagu.path.isNotBlank()) {
                                java.io.File(lagu.path).takeIf { it.exists() }
                            } else null
                        }
                        AlertDialog(
                            onDismissRequest = { rincianLagu = false },
                            title = { Text("Rincian") },
                            text = {
                                Text(
                                    buildString {
                                        append("Judul: ${lagu.title}\n")
                                        append("Artis: ${lagu.artist}\n")
                                        if (lagu.album.isNotBlank()) append("Album: ${lagu.album}\n")
                                        append("Durasi: ${formatDurasi(lagu.durationMs)}\n")
                                        append(
                                            "Sumber: " + if (lagu.akunDrive.isNotBlank()) {
                                                "Google Drive (${lagu.akunDrive})"
                                            } else {
                                                "Perangkat"
                                            }
                                        )
                                        if (lagu.path.isNotBlank()) append("\nLokasi: ${lagu.path}")
                                        berkasLokal?.let {
                                            append("\nUkuran: ${formatUkuranBerkas(it.length())}")
                                        }
                                        if (lagu.dateMillis > 0) {
                                            append(
                                                "\nTanggal: " + java.text.SimpleDateFormat(
                                                    "d MMM yyyy", java.util.Locale("id", "ID"),
                                                ).format(java.util.Date(lagu.dateMillis))
                                            )
                                        }
                                    }
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { rincianLagu = false }) { Text("Tutup") }
                            },
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    // Seekbar TIPIS custom + waktu berjalan / SISA (negatif)
                    val durasiAman = vm.durasiLagu.coerceAtLeast(1)
                    val posisiKini = geser ?: vm.posisiLagu.toFloat()
                    BarGeserTipis(
                        fraksi = (posisiKini / durasiAman).coerceIn(0f, 1f),
                        onGeser = { f -> geser = f * durasiAman },
                        onSelesai = {
                            geser?.let { vm.cariLaguKe(it.toLong()) }
                            geser = null
                        },
                        tinggi = 5.dp,
                        knob = true,
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            formatDurasi(posisiKini.toLong()),
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "-" + formatDurasi((vm.durasiLagu - posisiKini.toLong()).coerceAtLeast(0)),
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    // Transport: ⏮ • ⏯ • ⏭ DI TENGAH (perintah user);
                    // acak/favorit/ulang pindah ke menu ⋮ di samping Lirik
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        IconButton(onClick = { vm.laguSebelum() }, modifier = Modifier.size(56.dp)) {
                            Icon(
                                Icons.Filled.SkipPrevious, "Sebelumnya",
                                tint = Color.White, modifier = Modifier.size(40.dp),
                            )
                        }
                        Spacer(Modifier.width(28.dp))
                        IconButton(onClick = { vm.toggleLagu() }, modifier = Modifier.size(72.dp)) {
                            Icon(
                                if (vm.muterLagu) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                if (vm.muterLagu) "Jeda" else "Putar",
                                tint = Color.White, modifier = Modifier.size(56.dp),
                            )
                        }
                        Spacer(Modifier.width(28.dp))
                        IconButton(onClick = { vm.laguBerikut() }, modifier = Modifier.size(56.dp)) {
                            Icon(
                                Icons.Filled.SkipNext, "Berikutnya",
                                tint = Color.White, modifier = Modifier.size(40.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    // Volume pill GEMUK ala iOS (tanpa knob)
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
                    // Pill output ala AirPlay ("iPhone" di screenshot)
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier.clickable {
                            vm.beriTahu("Output suara: $namaOutput")
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
}

// ---------- Layar potong persegi buat cover lagu ----------

/** Muat foto dari URI (maks ~2400px) + putar mengikuti orientasi EXIF. */
private fun muatBitmapTerarah(ctx: android.content.Context, uri: Uri): Bitmap? {
    val resolver = ctx.contentResolver
    val batas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, batas) }
    var contoh = 1
    while (maxOf(batas.outWidth, batas.outHeight) / contoh > 2400) contoh *= 2
    val opsi = BitmapFactory.Options().apply { inSampleSize = contoh }
    var hasil = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, opsi)
    } ?: return null
    val orientasi = runCatching {
        resolver.openInputStream(uri)?.use {
            android.media.ExifInterface(it).getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL,
            )
        }
    }.getOrNull() ?: android.media.ExifInterface.ORIENTATION_NORMAL
    val derajat = when (orientasi) {
        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (derajat != 0f) {
        val m = android.graphics.Matrix().apply { postRotate(derajat) }
        hasil = Bitmap.createBitmap(hasil, 0, 0, hasil.width, hasil.height, m, true)
    }
    return hasil
}

/**
 * Potong PERSEGI gaya editor Samsung: FOTO DIAM, yg bergerak KOTAK
 * potongnya — seret di dalam kotak buat mindah, tarik sudutnya buat
 * mengubah ukuran (tetap persegi, jangkar di sudut seberangnya). Tombol
 * putar memutar foto 90°. "Selesai" merender isi kotak jadi JPEG.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayarPotongCover(
    lagu: AudioItem,
    uriFoto: Uri,
    onBatal: () -> Unit,
    onSimpan: (ByteArray) -> Unit,
) {
    val ctx = LocalContext.current
    var bmp by remember { mutableStateOf<Bitmap?>(null) }
    var gagalMuat by remember { mutableStateOf(false) }
    LaunchedEffect(uriFoto) {
        val hasil = withContext(Dispatchers.IO) {
            runCatching { muatBitmapTerarah(ctx, uriFoto) }.getOrNull()
        }
        if (hasil != null) bmp = hasil else gagalMuat = true
    }
    var wadah by remember { mutableStateOf(IntSize.Zero) }
    // Kotak potong dlm koordinat piksel wadah; Rect.Zero = belum disiapkan
    var kotak by remember { mutableStateOf(Rect.Zero) }

    val gambar = bmp
    // Persegi tampilan foto (ContentScale.Fit) di dlm wadah
    val rectFoto: Rect? = if (gambar != null && wadah.width > 0 && wadah.height > 0) {
        val skala = minOf(
            wadah.width.toFloat() / gambar.width,
            wadah.height.toFloat() / gambar.height,
        )
        val dw = gambar.width * skala
        val dh = gambar.height * skala
        Rect(
            left = (wadah.width - dw) / 2f,
            top = (wadah.height - dh) / 2f,
            right = (wadah.width + dw) / 2f,
            bottom = (wadah.height + dh) / 2f,
        )
    } else {
        null
    }
    LaunchedEffect(rectFoto) {
        val f = rectFoto ?: return@LaunchedEffect
        if (kotak == Rect.Zero) {
            val sisi = minOf(f.width, f.height) * 0.96f
            kotak = Rect(
                left = f.center.x - sisi / 2f,
                top = f.center.y - sisi / 2f,
                right = f.center.x + sisi / 2f,
                bottom = f.center.y + sisi / 2f,
            )
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        // Header: Batal & Selesai di ATAS (perintah user — di bawah
        // ketutup navbar; di atas dikasih statusBarsPadding biar gak
        // ketutup status bar juga)
        Row(
            modifier = Modifier.fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBatal) {
                Text("Batal", color = Color(0xFF4CD964), fontSize = 15.sp)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                val g = gambar
                val f = rectFoto
                if (g != null && f != null && kotak != Rect.Zero && f.width > 0f) {
                    val skala = g.width / f.width
                    var sisiPx = (kotak.width * skala).roundToInt()
                    sisiPx = minOf(sisiPx, g.width, g.height)
                    val sx = ((kotak.left - f.left) * skala).roundToInt()
                        .coerceIn(0, g.width - sisiPx)
                    val sy = ((kotak.top - f.top) * skala).roundToInt()
                        .coerceIn(0, g.height - sisiPx)
                    val potongan = Bitmap.createBitmap(g, sx, sy, sisiPx, sisiPx)
                    val akhir = if (sisiPx > 1024) {
                        Bitmap.createScaledBitmap(potongan, 1024, 1024, true)
                    } else {
                        potongan
                    }
                    val baos = java.io.ByteArrayOutputStream()
                    akhir.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                    onSimpan(baos.toByteArray())
                }
            }) {
                Text(
                    "Selesai", color = Color(0xFF4CD964), fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Box(
            Modifier.weight(1f).fillMaxWidth().onSizeChanged { wadah = it },
            contentAlignment = Alignment.Center,
        ) {
            when {
                gambar != null && rectFoto != null -> {
                    Image(
                        bitmap = gambar.asImageBitmap(),
                        contentDescription = "Foto buat cover ${lagu.title}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (kotak != Rect.Zero) {
                        LapisanPotong(
                            foto = rectFoto,
                            kotak = kotak,
                            onKotak = { kotak = it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                gagalMuat -> Text(
                    "Foto tidak terbaca",
                    color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp,
                )
                else -> CircularProgressIndicator(color = Color.White)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onClick = {
                val g = gambar ?: return@IconButton
                val m = android.graphics.Matrix().apply { postRotate(90f) }
                bmp = Bitmap.createBitmap(g, 0, 0, g.width, g.height, m, true)
                kotak = Rect.Zero
            }) {
                Icon(
                    Icons.Filled.RotateRight, "Putar 90°",
                    tint = Color.White, modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

/**
 * Lapisan kotak potong di atas foto diam: peredup di luar kotak, garis
 * bantu sepertiga, gagang sudut L, dan gestur seret — di dalam kotak =
 * pindah, di sudut = ubah ukuran persegi (jangkar sudut seberangnya).
 */
@Composable
private fun LapisanPotong(
    foto: Rect,
    kotak: Rect,
    onKotak: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dens = LocalDensity.current
    fun pxDp(px: Float) = with(dens) { px.toDp() }
    var ukuran by remember { mutableStateOf(IntSize.Zero) }
    // 0 diam, 1 pindah, 2 TL, 3 TR, 4 BL, 5 BR
    var mode by remember { mutableIntStateOf(0) }
    var seret by remember { mutableStateOf(kotak) }
    LaunchedEffect(kotak) { if (mode == 0) seret = kotak }
    val aktif = if (mode != 0) seret else kotak
    val tangkap = 72f
    val sisiMin = 90f
    Box(
        modifier
            .onSizeChanged { ukuran = it }
            .pointerInput(foto, kotak) {
                detectDragGestures(
                    onDragStart = { pos ->
                        val k = kotak
                        mode = when {
                            (pos - k.topLeft).getDistance() <= tangkap -> 2
                            (pos - Offset(k.right, k.top)).getDistance() <= tangkap -> 3
                            (pos - Offset(k.left, k.bottom)).getDistance() <= tangkap -> 4
                            (pos - k.bottomRight).getDistance() <= tangkap -> 5
                            k.contains(pos) -> 1
                            else -> 0
                        }
                        seret = k
                    },
                    onDragEnd = {
                        if (mode != 0) onKotak(seret)
                        mode = 0
                    },
                    onDragCancel = { mode = 0 },
                ) { change, drag ->
                    if (mode == 0) return@detectDragGestures
                    change.consume()
                    val k = seret
                    seret = when (mode) {
                        1 -> {
                            val dx = drag.x.coerceIn(
                                foto.left - k.left, foto.right - k.right,
                            )
                            val dy = drag.y.coerceIn(
                                foto.top - k.top, foto.bottom - k.bottom,
                            )
                            k.translate(dx, dy)
                        }
                        2 -> {
                            val mentah = maxOf(
                                k.right - (k.left + drag.x),
                                k.bottom - (k.top + drag.y),
                            )
                            val s2 = mentah.coerceIn(
                                sisiMin, minOf(k.right - foto.left, k.bottom - foto.top),
                            )
                            Rect(k.right - s2, k.bottom - s2, k.right, k.bottom)
                        }
                        3 -> {
                            val mentah = maxOf(
                                (k.right + drag.x) - k.left,
                                k.bottom - (k.top + drag.y),
                            )
                            val s2 = mentah.coerceIn(
                                sisiMin, minOf(foto.right - k.left, k.bottom - foto.top),
                            )
                            Rect(k.left, k.bottom - s2, k.left + s2, k.bottom)
                        }
                        4 -> {
                            val mentah = maxOf(
                                k.right - (k.left + drag.x),
                                (k.bottom + drag.y) - k.top,
                            )
                            val s2 = mentah.coerceIn(
                                sisiMin, minOf(k.right - foto.left, foto.bottom - k.top),
                            )
                            Rect(k.right - s2, k.top, k.right, k.top + s2)
                        }
                        else -> {
                            val mentah = maxOf(
                                (k.right + drag.x) - k.left,
                                (k.bottom + drag.y) - k.top,
                            )
                            val s2 = mentah.coerceIn(
                                sisiMin, minOf(foto.right - k.left, foto.bottom - k.top),
                            )
                            Rect(k.left, k.top, k.left + s2, k.top + s2)
                        }
                    }
                }
            },
    ) {
        if (aktif == Rect.Zero || ukuran.width == 0) return@Box
        val redup = Color.Black.copy(alpha = 0.55f)
        // Peredup di luar kotak
        Box(
            Modifier.offset { IntOffset(0, 0) }
                .size(pxDp(ukuran.width.toFloat()), pxDp(aktif.top))
                .background(redup),
        )
        Box(
            Modifier.offset { IntOffset(0, aktif.bottom.roundToInt()) }
                .size(
                    pxDp(ukuran.width.toFloat()),
                    pxDp(ukuran.height - aktif.bottom),
                )
                .background(redup),
        )
        Box(
            Modifier.offset { IntOffset(0, aktif.top.roundToInt()) }
                .size(pxDp(aktif.left), pxDp(aktif.height))
                .background(redup),
        )
        Box(
            Modifier.offset { IntOffset(aktif.right.roundToInt(), aktif.top.roundToInt()) }
                .size(pxDp(ukuran.width - aktif.right), pxDp(aktif.height))
                .background(redup),
        )
        // Garis bantu sepertiga di dalam kotak
        val garis = Color.White.copy(alpha = 0.35f)
        for (i in 1..2) {
            val gx = aktif.left + aktif.width * i / 3f
            Box(
                Modifier.offset { IntOffset(gx.roundToInt(), aktif.top.roundToInt()) }
                    .size(pxDp(1.5f), pxDp(aktif.height))
                    .background(garis),
            )
            val gy = aktif.top + aktif.height * i / 3f
            Box(
                Modifier.offset { IntOffset(aktif.left.roundToInt(), gy.roundToInt()) }
                    .size(pxDp(aktif.width), pxDp(1.5f))
                    .background(garis),
            )
        }
        // Bingkai putih tipis
        Box(
            Modifier.offset {
                IntOffset(aktif.left.roundToInt(), aktif.top.roundToInt())
            }
                .size(pxDp(aktif.width), pxDp(aktif.height))
                .border(1.5.dp, Color.White.copy(alpha = 0.85f)),
        )
        // Gagang sudut bentuk L (tebal, putih)
        val gagang = 26f // panjang kaki L (px)
        val tebal = 4f
        val sudut = listOf(
            Offset(aktif.left, aktif.top) to (1f to 1f),
            Offset(aktif.right, aktif.top) to (-1f to 1f),
            Offset(aktif.left, aktif.bottom) to (1f to -1f),
            Offset(aktif.right, aktif.bottom) to (-1f to -1f),
        )
        for ((titik, arah) in sudut) {
            val (ax, ay) = arah
            // kaki horizontal
            Box(
                Modifier.offset {
                    IntOffset(
                        (if (ax > 0) titik.x else titik.x - gagang).roundToInt(),
                        (titik.y - tebal / 2f).roundToInt(),
                    )
                }
                    .size(pxDp(gagang), pxDp(tebal))
                    .background(Color.White),
            )
            // kaki vertikal
            Box(
                Modifier.offset {
                    IntOffset(
                        (titik.x - tebal / 2f).roundToInt(),
                        (if (ay > 0) titik.y else titik.y - gagang).roundToInt(),
                    )
                }
                    .size(pxDp(tebal), pxDp(gagang))
                    .background(Color.White),
            )
        }
    }
}

private fun versiLebihBaru(baru: String, lama: String): Boolean {
    val b = baru.trim().removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val l = lama.trim().removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(b.size, l.size)) {
        val x = b.getOrElse(i) { 0 }
        val y = l.getOrElse(i) { 0 }
        if (x != y) return x > y
    }
    return false
}
