package id.frmdeveloper.gallery

import android.media.audiofx.Visualizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sqrt

/** Sesi audio pemutar musik utama (diisi LayananMusik pas player dibuat)
 * biar UI bisa masang Visualizer ke suara yang lagi diputar. */
object SesiVisual {
    // State (bukan var polos): UI harus ikut ke-trigger pas service
    // nerbitin sesi ini — var polos bikin LaunchedEffect gak pernah
    // jalan ulang dan Visualizer gak pernah mulai (bug v1.88).
    var idMusik by mutableStateOf(0)
}

/**
 * Spektrum LIVE dari suara beneran (Visualizer/FFT) — perintah user
 * 2026-11-11: "spektrumnya ngikutin suara lagu".
 *
 * [level] = 6 nilai 0..1 buat 6 batang ala iOS, diupdate listener FFT
 * (~20x/detik, batas API). Normalisasi per-band pakai puncak adaptif
 * (turun pelan) biar tetap hidup di volume kecil/besar. Semua jalur
 * gagal (izin gak ada, sesi 0, HP nolak Visualizer) -> [berjalan] false
 * dan UI balik ke animasi cadangan.
 */
object SpektrumLive {
    val level = mutableStateListOf(0f, 0f, 0f, 0f, 0f, 0f)
    var berjalan by mutableStateOf(false)
        private set

    private var viz: Visualizer? = null
    private var sesi = 0
    private val puncak = FloatArray(6) { 0.30f }

    // Tepi bin FFT per batang (skala logaritmik; bin 0-1 = DC/Nyquist dilewati)
    private val tepiBin = intArrayOf(2, 5, 11, 23, 47, 95, 380)

    @Synchronized
    fun mulai(sesiBaru: Int) {
        if (sesiBaru == 0) return
        if (viz != null && sesi == sesiBaru) {
            runCatching { viz?.enabled = true }
            berjalan = viz != null
            return
        }
        lepasInternal()
        sesi = sesiBaru
        viz = runCatching {
            val v = Visualizer(sesiBaru)
            v.captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
            v.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int,
                    ) = Unit

                    override fun onFftDataCapture(
                        visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int,
                    ) {
                        if (fft != null) proses(fft)
                    }
                },
                Visualizer.getMaxCaptureRate(),
                false,
                true,
            )
            v.enabled = true
            v
        }.getOrNull()
        berjalan = viz != null
        if (!berjalan) sesi = 0
    }

    private fun proses(fft: ByteArray) {
        // Tata letak FFT Visualizer: [0]=DC, [1]=Nyquist, lalu pasangan
        // (riil, imajiner) per bin k di indeks 2k & 2k+1.
        val jumlahBin = fft.size / 2
        for (b in 0 until 6) {
            val dari = tepiBin[b]
            val sampai = minOf(tepiBin[b + 1], jumlahBin)
            var jumlah = 0f
            var hitung = 0
            var k = dari
            while (k < sampai) {
                val re = fft[2 * k].toInt().toFloat()
                val im = fft[2 * k + 1].toInt().toFloat()
                jumlah += sqrt(re * re + im * im)
                hitung++
                k++
            }
            val mag = if (hitung > 0) (jumlah / hitung) / 128f else 0f
            // GERBANG HENING (laporan user 2026-11-11: pas suara sepi
            // spektrum masih gerak): tanpa gerbang ini, normalisasi
            // puncak adaptif ngangkat NOISE lantai jadi gerakan penuh
            // pas lagu lagi hening. Di bawah lantai absolut = hening,
            // batang langsung jatuh ke titik.
            if (mag < 0.012f) {
                level[b] = level[b] + (0f - level[b]) * 0.35f
                continue
            }
            if (mag > puncak[b]) puncak[b] = mag
            else puncak[b] = maxOf(puncak[b] * 0.995f, 0.15f)
            val target = (mag / maxOf(puncak[b], 0.01f)).coerceIn(0f, 1f)
            val lama = level[b]
            // Naik ngikutin ketukan, turun lebih halus biar gak patah-patah
            level[b] = if (target > lama) target else lama + (target - lama) * 0.35f
        }
    }

    @Synchronized
    fun berhenti() {
        runCatching { viz?.enabled = false }
        berjalan = false
    }

    private fun lepasInternal() {
        runCatching { viz?.release() }
        viz = null
        sesi = 0
    }
}
