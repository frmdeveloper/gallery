package id.frmdeveloper.gallery

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * === LIQUID GLASS iOS ===
 *
 * Token warna iOS (dari HIG Apple): biru #007AFF / #0A84FF, indigo
 * #5E5CE6, hijau #34C759, merah #FF3B30, oranye #FF9500, abu #8E8E93.
 */
val BiruIos = Color(0xFF007AFF)
val BiruIosGelap = Color(0xFF0A84FF)
val IndigoIos = Color(0xFF5E5CE6)
val HijauIos = Color(0xFF34C759)
val MerahIos = Color(0xFFFF3B30)
val OranyeIos = Color(0xFFFF9500)
val KuningIos = Color(0xFFFFCC00)
val PinkIos = Color(0xFFFF2D55)
val AbuIos = Color(0xFF8E8E93)

/** Alias lama (One UI) biar kode yg masih nyebut nama ini tetap jalan. */
val OneUiBlue = BiruIos

@Composable
fun gelapAktif(): Boolean = isSystemInDarkTheme()

// =====================================================================
// LIQUID GLASS v4 (v1.95) — pakai library referensi Backdrop punya
// Kyant (AndroidLiquidGlass, Apache-2.0), BUKAN shader rakitan sendiri
// lagi. Tiga versi rakitan (v1.92-v1.94) ditolak user: gak transparan,
// gak cembung, gak bagus. Yg bikin punya Kyant bener (dari dokumentasi
// resminya):
//  - Urutan efek WAJIB: vibrancy -> blur -> lens. Lensa membengkokkan
//    gambar yg SUDAH diblur.
//  - Lensa = pergeseran ngikutin NORMAL SDF bentuk (tegak lurus tepi)
//    dgn profil setengah lingkaran 1 - sqrt(1 - x^2): tengah bersih,
//    distorsi cuma di zona tepi setebal refractionHeight, kuat sampe
//    puluhan dp -> itu "cembung"-nya.
//  - Transparansi dijaga lapisan permukaan (onDrawSurface) di atas
//    efek, bukan dgn ngutak-atik isi shader.
//  - Highlight + shadow + inner shadow bawaan library, dihitung dari
//    SDF yg sama.
//
// Dua backdrop:
//  - [LocalKacaKonten]: mesh + SELURUH konten layar (direkam oleh
//    [KotakTerekam]). Dipakai kaca MELAYANG di luar kotak itu: bar tab,
//    mini player, bar mode pilih -> mereka nyampel konten asli di
//    belakangnya (grid foto dll).
//  - [LocalKacaMesh]: mesh latar doang (direkam oleh [LatarKacaApp]).
//    Dipakai kaca DI DALAM layar (kartu Menu, panel album, krom mode
//    pilih) — mereka tidak boleh nyampel backdrop konten karena mereka
//    sendiri tinggal di dalam rekamannya (umpan balik).
// =====================================================================

val LocalKacaKonten = compositionLocalOf<LayerBackdrop?> { null }
val LocalKacaMesh = compositionLocalOf<LayerBackdrop?> { null }

/**
 * Backdrop MEDIA buat penampil foto/video: diisi ViewerScreen (rekaman
 * pager) & ditimpa VideoPage (rekaman permukaan pemutarnya sendiri).
 * Dipakai [kacaCairHitam] biar krom di atas media nyampel media asli.
 */
val LocalKacaMedia = compositionLocalOf<LayerBackdrop?> { null }

/** true buat composable yg tinggal DI DALAM kotak terekam konten. */
val LocalDalamRekaman = compositionLocalOf { false }

/** Penyedia akar: bikin dua backdrop & bagikan lewat composition local. */
@Composable
fun LatarKacaTerekam(modifier: Modifier = Modifier, isi: @Composable () -> Unit) {
    val konten = rememberLayerBackdrop()
    val mesh = rememberLayerBackdrop()
    Box(modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalKacaKonten provides konten,
            LocalKacaMesh provides mesh,
        ) { isi() }
    }
}

/**
 * Kotak yg isinya (mesh + layar) DIREKAM sebagai backdrop konten.
 * Kaca melayang di LUAR kotak ini nyampel rekaman ini; isi di dalamnya
 * ditandai [LocalDalamRekaman] biar pakai backdrop mesh saja.
 */
@Composable
fun KotakTerekam(
    modifier: Modifier = Modifier,
    isi: @Composable () -> Unit,
) {
    val konten = LocalKacaKonten.current
    Box(
        modifier
            .fillMaxSize()
            .then(if (konten != null) Modifier.layerBackdrop(konten) else Modifier),
    ) {
        CompositionLocalProvider(LocalDalamRekaman provides true) { isi() }
    }
}

private fun pathDari(outline: Outline): Path = Path().apply {
    when (outline) {
        is Outline.Rectangle -> addRect(outline.rect)
        is Outline.Rounded -> addRoundRect(outline.roundRect)
        is Outline.Generic -> addPath(outline.path)
    }
}

/**
 * Tumpukan cahaya tetesan di atas isi gradasi (buat jalur cadangan
 * tanpa backdrop & kaca media): kilau dari atas, bintik pantulan +
 * intinya, bayangan bibir bawah. Canvas ter-clip ke bentuk.
 */
private fun DrawScope.gambarTumpukanCahaya(
    outline: Outline,
    isi: Brush,
    kilauAtas: Float,
    bintik: Float,
    bintikInti: Float,
    bayangBawah: Float,
) {
    with(drawContext.canvas) {
        save()
        clipPath(pathDari(outline))
    }
    drawRect(isi)
    drawRect(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = kilauAtas), Color.Transparent),
            center = Offset(size.width * 0.5f, -size.height * 0.35f),
            radius = (size.width * 0.95f).coerceAtLeast(1f),
        ),
    )
    drawRect(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = bintik), Color.Transparent),
            center = Offset(size.width * 0.20f, size.height * 0.08f),
            radius = (size.width * 0.50f).coerceAtLeast(1f),
        ),
    )
    drawRect(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = bintikInti), Color.Transparent),
            center = Offset(size.width * 0.16f, size.height * 0.10f),
            radius = (size.width * 0.16f).coerceAtLeast(1f),
        ),
    )
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = bayangBawah)),
            startY = size.height * 0.70f,
            endY = size.height,
        ),
    )
    with(drawContext.canvas) { restore() }
}

/** Kaca gradasi polos (cadangan: backdrop gak ada / API < 31 / bentuk bukan sudut). */
@Composable
private fun Modifier.kacaPolos(
    bentuk: Shape,
    gelap: Boolean,
    bayangan: Dp,
): Modifier {
    val isi = if (gelap) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.11f),
                Color.White.copy(alpha = 0.07f),
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.80f),
                Color.White.copy(alpha = 0.64f),
                Color.White.copy(alpha = 0.54f),
            ),
        )
    }
    val tepi = if (gelap) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.62f),
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.10f),
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.50f),
                Color.White.copy(alpha = 0.28f),
            ),
        )
    }
    return this
        .shadow(
            bayangan, bentuk,
            ambientColor = Color.Black.copy(alpha = 0.20f),
            spotColor = Color.Black.copy(alpha = 0.20f),
        )
        .drawBehind {
            gambarTumpukanCahaya(
                outline = bentuk.createOutline(size, layoutDirection, this),
                isi = isi,
                kilauAtas = if (gelap) 0.18f else 0.34f,
                bintik = if (gelap) 0.14f else 0.24f,
                bintikInti = if (gelap) 0.16f else 0.20f,
                bayangBawah = if (gelap) 0.16f else 0.08f,
            )
        }
        .border(1.dp, tepi, bentuk)
}

/**
 * Kaca cair Liquid Glass asli (library Backdrop): elemen melayang
 * nyampel backdrop konten, elemen di dalam layar nyampel backdrop
 * mesh. Efek: vibrancy -> blur -> lens (cembung SDF + dispersi).
 * Lapisan permukaan tipis di atasnya jaga keterbacaan, kayak resep
 * resmi: terang = putih 0.30, gelap = putih 0.10.
 */
@Composable
fun Modifier.kacaCair(
    bentuk: Shape,
    gelap: Boolean,
    bayangan: Dp = 12.dp,
): Modifier {
    val dalamRekaman = LocalDalamRekaman.current
    val backdrop = if (dalamRekaman) LocalKacaMesh.current else LocalKacaKonten.current
    val bentukSudut = bentuk as? CornerBasedShape
    if (backdrop == null || bentukSudut == null ||
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    ) {
        return this.kacaPolos(bentuk, gelap, bayangan)
    }
    val densitas = LocalDensity.current
    val blurPx = with(densitas) { 8.dp.toPx() }
    val lensTinggiPx = with(densitas) { 12.dp.toPx() }
    val lensJumlahPx = with(densitas) { 26.dp.toPx() }
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { bentukSudut },
        effects = {
            vibrancy()
            blur(blurPx)
            if (Build.VERSION.SDK_INT >= 33) {
                lens(
                    refractionHeight = lensTinggiPx,
                    refractionAmount = lensJumlahPx,
                    depthEffect = true,
                    chromaticAberration = true,
                )
            }
        },
        onDrawSurface = {
            drawRect(
                if (gelap) Color.White.copy(alpha = 0.10f)
                else Color.White.copy(alpha = 0.30f),
            )
        },
    )
}

/**
 * Kaca hitam di atas media (foto/video): selalu gelap. Kalau ada
 * backdrop media di lingkup ini ([LocalKacaMedia] — penampil), pakai
 * Liquid Glass asli library Backdrop (vibrancy -> blur -> lens) dgn
 * permukaan hitam tipis; kalau tidak ada (mis. di atas blur cover
 * pemutar musik), jatuh ke gradasi + tumpukan cahaya.
 */
@Composable
fun Modifier.kacaCairHitam(
    bentuk: Shape,
    bayangan: Dp = 12.dp,
): Modifier {
    val media = LocalKacaMedia.current
    val bentukSudut = bentuk as? CornerBasedShape
    if (media != null && bentukSudut != null &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        val densitasMedia = LocalDensity.current
        val blurMediaPx = with(densitasMedia) { 10.dp.toPx() }
        val lensMediaTinggiPx = with(densitasMedia) { 12.dp.toPx() }
        val lensMediaJumlahPx = with(densitasMedia) { 24.dp.toPx() }
        return this.drawBackdrop(
            backdrop = media,
            shape = { bentukSudut },
            effects = {
                vibrancy()
                blur(blurMediaPx)
                if (Build.VERSION.SDK_INT >= 33) {
                    lens(
                        refractionHeight = lensMediaTinggiPx,
                        refractionAmount = lensMediaJumlahPx,
                        depthEffect = true,
                        chromaticAberration = true,
                    )
                }
            },
            onDrawSurface = { drawRect(Color.Black.copy(alpha = 0.22f)) },
        )
    }
    val isi = Brush.verticalGradient(
        listOf(
            Color.Black.copy(alpha = 0.24f),
            Color.Black.copy(alpha = 0.33f),
            Color.Black.copy(alpha = 0.42f),
        ),
    )
    val tepi = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.95f),
            Color.White.copy(alpha = 0.32f),
            Color.White.copy(alpha = 0.12f),
        ),
    )
    return this
        .shadow(
            bayangan, bentuk,
            ambientColor = Color.Black.copy(alpha = 0.30f),
            spotColor = Color.Black.copy(alpha = 0.30f),
        )
        .drawBehind {
            gambarTumpukanCahaya(
                outline = bentuk.createOutline(size, layoutDirection, this),
                isi = isi,
                kilauAtas = 0.30f,
                bintik = 0.18f,
                bintikInti = 0.28f,
                bayangBawah = 0.20f,
            )
        }
        .border(1.dp, tepi, bentuk)
}

/**
 * Kaca KAPSUL TAB terpilih: lapisan Liquid Glass KEDUA yg ditumpuk di
 * atas kaca bar tab (perintah user 2026-11-12 — "yg diselect jangan
 * putih tapi ada liquid glass lagi diatasnya, jadi tumpuk"). Nyampel
 * backdrop konten yg sama dgn bar, tapi lensanya LEBIH KUAT & blur-nya
 * LEBIH TIPIS (lensa jernih di atas kaca buram) + permukaan lebih
 * terang -> kelihatan kayak tetesan kaca melayang di atas bar.
 * Cadangan API < 31 / backdrop kosong: isian putih lama.
 */
@Composable
fun Modifier.kacaKapsulTab(
    bentuk: Shape,
    gelap: Boolean,
): Modifier {
    val backdrop = LocalKacaKonten.current
    val bentukSudut = bentuk as? CornerBasedShape
    if (backdrop == null || bentukSudut == null ||
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    ) {
        return this
            .background(
                if (gelap) Color.White.copy(alpha = 0.20f)
                else Color.White.copy(alpha = 0.88f),
                bentuk,
            )
            .border(
                1.dp,
                Color.White.copy(alpha = if (gelap) 0.28f else 0.75f),
                bentuk,
            )
    }
    val densitas = LocalDensity.current
    val blurPx = with(densitas) { 2.dp.toPx() }
    val lensTinggiPx = with(densitas) { 14.dp.toPx() }
    val lensJumlahPx = with(densitas) { 30.dp.toPx() }
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { bentukSudut },
        effects = {
            vibrancy()
            blur(blurPx)
            if (Build.VERSION.SDK_INT >= 33) {
                lens(
                    refractionHeight = lensTinggiPx,
                    refractionAmount = lensJumlahPx,
                    depthEffect = true,
                    chromaticAberration = true,
                )
            }
        },
        onDrawSurface = {
            drawRect(
                if (gelap) Color.White.copy(alpha = 0.22f)
                else Color.White.copy(alpha = 0.45f),
            )
        },
    )
}

/** Kotak permukaan Liquid Glass (pengganti Surface polos). */
@Composable
fun PermukaanKaca(
    bentuk: Shape,
    modifier: Modifier = Modifier,
    gelap: Boolean = gelapAktif(),
    bayangan: Dp = 12.dp,
    isi: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.kacaCair(bentuk, gelap, bayangan), content = isi)
}

/**
 * Latar aplikasi: mesh gradasi lembut ala wallpaper iOS — dasar biru
 * keabu-abuan + blob biru (kiri atas), ungu (kanan atas), mint (kiri
 * bawah), merah muda (kanan bawah) + kilau putih di atas. Akarnya
 * sekaligus DIREKAM sebagai backdrop mesh ([LocalKacaMesh]).
 */
@Composable
fun LatarKacaApp(modifier: Modifier = Modifier) {
    val gelap = gelapAktif()
    val mesh = LocalKacaMesh.current
    Box(
        modifier
            .fillMaxSize()
            .then(if (mesh != null) Modifier.layerBackdrop(mesh) else Modifier)
            .background(if (gelap) Color(0xFF04060A) else Color(0xFFE9EEF6)),
    ) {
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-130).dp)
                .size(440.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (gelap) Color(0xFF1D4E89) else Color(0xFF9CC4FF))
                                .copy(alpha = if (gelap) 0.55f else 0.65f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 110.dp, y = (-100).dp)
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (gelap) Color(0xFF3B2A72) else Color(0xFFCFC4FF))
                                .copy(alpha = if (gelap) 0.50f else 0.60f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-120).dp, y = 120.dp)
                .size(420.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (gelap) Color(0xFF0C4A44) else Color(0xFFBDEBDD))
                                .copy(alpha = if (gelap) 0.50f else 0.60f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 110.dp)
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (gelap) Color(0xFF5E2140) else Color(0xFFFFCFE0))
                                .copy(alpha = if (gelap) 0.45f else 0.55f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        // Kilau cahaya dari atas
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (gelap) 0.05f else 0.35f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}
