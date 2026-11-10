package id.frmdeveloper.gallery

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tema LIQUID GLASS iOS (rombak total 2026-11-10): palet warna HIG
 * Apple (biru #007AFF/#0A84FF, indigo #5E5CE6, merah #FF3B30), sudut
 * BESAR ala iOS di semua komponen Material (dialog, menu, sheet,
 * tombol, kartu), tipografi rapat ala SF (judul semibold, spasi huruf
 * agak negatif). Permukaan kaca tembus cahaya diatur di Glass.kt;
 * skema warna di sini = fondasi solid di bawah kacanya.
 */
private val LightColors = lightColorScheme(
    primary = BiruIos,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E9FF),
    onPrimaryContainer = Color(0xFF0040A0),
    secondary = IndigoIos,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E1FF),
    onSecondaryContainer = Color(0xFF1D1B8F),
    tertiary = HijauIos,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3F5DC),
    error = MerahIos,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF7A130C),
    background = Color(0xFFE9EEF6),
    onBackground = Color(0xFF0B0C10),
    surface = Color.White,
    onSurface = Color(0xFF0B0C10),
    surfaceVariant = Color(0xFFE4EAF2),
    onSurfaceVariant = Color(0xFF5B6068),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF4F7FB),
    surfaceContainer = Color(0xFFEDF1F7),
    surfaceContainerHigh = Color(0xFFE5EAF1),
    surfaceContainerHighest = Color(0xFFDCE2EC),
    outline = Color(0xFFB9C2CF),
    outlineVariant = Color(0xFFD5DBE5),
    inverseSurface = Color(0xFF14171C),
    inverseOnSurface = Color(0xFFF2F4F8),
    inversePrimary = Color(0xFF66A8FF),
)

private val DarkColors = darkColorScheme(
    primary = BiruIosGelap,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0B3A6E),
    onPrimaryContainer = Color(0xFFCFE6FF),
    secondary = IndigoIos,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E2C66),
    onSecondaryContainer = Color(0xFFD9D7FF),
    tertiary = HijauIos,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF0E4423),
    error = Color(0xFFFF453A),
    onError = Color.Black,
    errorContainer = Color(0xFF5E1712),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF04060A),
    onBackground = Color(0xFFF4F6FA),
    surface = Color(0xFF12161E),
    onSurface = Color(0xFFF4F6FA),
    surfaceVariant = Color(0xFF1D232E),
    onSurfaceVariant = Color(0xFFA8B0BD),
    surfaceContainerLowest = Color(0xFF090C11),
    surfaceContainerLow = Color(0xFF0E1219),
    surfaceContainer = Color(0xFF12161E),
    surfaceContainerHigh = Color(0xFF181E28),
    surfaceContainerHighest = Color(0xFF202836),
    outline = Color(0xFF39424F),
    outlineVariant = Color(0xFF262D38),
    inverseSurface = Color(0xFFF2F4F8),
    inverseOnSurface = Color(0xFF14171C),
    inversePrimary = BiruIos,
)

/** Sudut ala iOS: serba membulat besar, tidak ada sudut tajam. */
private val BentukIos = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Tipografi ala SF: judul tebal rapat, isi lega dibaca. */
private val TipografiIos = Typography(
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 34.sp,
        lineHeight = 40.sp, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 28.sp,
        lineHeight = 34.sp, letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 20.sp,
        lineHeight = 25.sp, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp,
    ),
)

@Composable
fun GaleriFRMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Tanpa dynamic color (Material You) — palet iOS tetap, tidak
    // ketint warna wallpaper.
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        shapes = BentukIos,
        typography = TipografiIos,
        content = content,
    )
}
