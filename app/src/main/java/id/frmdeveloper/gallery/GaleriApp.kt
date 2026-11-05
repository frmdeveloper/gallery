package id.frmdeveloper.gallery

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

/**
 * Application: daftarkan fetcher cover lagu ke ImageLoader bawaan Coil,
 * biar semua AsyncImage yg modelnya [CoverLagu] membaca gambar langsung
 * dari tag berkas lagu (bukan albumart MediaStore).
 */
class GaleriApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                add(FetcherCoverLagu.Factory(applicationContext))
            }
            .build()
}
