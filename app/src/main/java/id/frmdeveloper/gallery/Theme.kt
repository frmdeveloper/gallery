package id.frmdeveloper.gallery

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Aksen biru One UI (fakta token warna dari REFERENCE.md: #387AFF)
val OneUiBlue = Color(0xFF387AFF)
private val BlueDark = Color(0xFF2E65D4)
private val BlueLight = Color(0xFF598FFF)

private val LightColors = lightColorScheme(
    primary = OneUiBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EDFF),
    onPrimaryContainer = BlueDark,
    secondary = BlueLight,
    background = Color.White,
    onBackground = Color(0xFF000000),
    surface = Color.White,
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFF555555),
)

private val DarkColors = darkColorScheme(
    primary = BlueLight,
    onPrimary = Color.Black,
    primaryContainer = BlueDark,
    onPrimaryContainer = Color.White,
    secondary = OneUiBlue,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF171717),
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFBBBBBB),
)

@Composable
fun GaleriFRMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // SENGAJA tanpa dynamic color (Material You): skema dinamis bikin background
    // ketint warna wallpaper (kelihatan abu-abu). Galeri Samsung pakai warna tetap:
    // putih bersih di mode terang, hitam di mode gelap.
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
