package id.frmdeveloper.gallery

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * === LIQUID GLASS iOS (rombak tema total, perintah user 2026-11-10) ===
 *
 * Kaca cair ala iOS 26: permukaan tembus cahaya dengan (1) isi gradasi
 * vertikal — atas lebih terang (cahaya jatuh dari atas), (2) tepi
 * SPEKULAR: garis tepi gradasi yang terang di atas dan memudar ke
 * bawah (pantulan cahaya di bibir kaca), (3) bayangan lembut menyebar.
 * Di atas latar mesh aplikasi ([LatarKacaApp]) kaca menangkap warna
 * blob di belakangnya, jadi kelihatan hidup, bukan abu-abu rata.
 *
 * Dua resep:
 * - [kacaCair]: kaca putih (buat permukaan di atas latar aplikasi) —
 *   terang/gelap ngikutin tema sistem.
 * - [kacaCairHitam]: kaca hitam (buat permukaan di atas foto/video &
 *   latar blur cover) — selalu gelap kayak kaca iOS di atas media.
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

/** Isi + tepi spekular + bayangan kaca putih Liquid Glass. */
fun Modifier.kacaCair(
    bentuk: Shape,
    gelap: Boolean,
    bayangan: Dp = 12.dp,
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
                Color.White.copy(alpha = 0.48f),
                Color.White.copy(alpha = 0.20f),
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
        .background(isi, bentuk)
        .border(1.dp, tepi, bentuk)
}

/** Kaca hitam di atas media (foto/video/blur cover): selalu gelap. */
fun Modifier.kacaCairHitam(
    bentuk: Shape,
    bayangan: Dp = 12.dp,
): Modifier {
    val isi = Brush.verticalGradient(
        listOf(
            Color.Black.copy(alpha = 0.32f),
            Color.Black.copy(alpha = 0.45f),
            Color.Black.copy(alpha = 0.55f),
        ),
    )
    val tepi = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.50f),
            Color.White.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.10f),
        ),
    )
    return this
        .shadow(
            bayangan, bentuk,
            ambientColor = Color.Black.copy(alpha = 0.30f),
            spotColor = Color.Black.copy(alpha = 0.30f),
        )
        .background(isi, bentuk)
        .border(1.dp, tepi, bentuk)
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
 * bawah), merah muda (kanan bawah) + kilau putih di atas. Semua
 * permukaan kaca di aplikasi nangkap warna mesh ini.
 */
@Composable
fun LatarKacaApp(modifier: Modifier = Modifier) {
    val gelap = gelapAktif()
    Box(
        modifier
            .fillMaxSize()
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
