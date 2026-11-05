# Galeri Drive — aturan R8 rilis
# ViewModel dibuat via refleksi oleh lifecycle — jangan dikecilkan namanya.
-keep class id.frmchan.drivegallery.GalleryViewModel { *; }
# Lapisan Drive (dipakai lintas file; jaga biar gak ke-strip/rename aneh)
-keep class id.frmchan.drivegallery.DriveApi { *; }
-keep class id.frmchan.drivegallery.DriveAuth { *; }
-keep class id.frmchan.drivegallery.DriveLogin { *; }
-keep class id.frmchan.drivegallery.DriveLogin$Sesi { *; }
# Model data
-keep class id.frmchan.drivegallery.MediaItem { *; }
-keep class id.frmchan.drivegallery.AudioItem { *; }
-keep class id.frmchan.drivegallery.AudioFolder { *; }
-keep class id.frmchan.drivegallery.LayananMusik { *; }
# jaudiotagger milih parser/tag per format secara internal — jangan di-strip R8
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**
-keep class id.frmchan.drivegallery.Album { *; }
# org.json bawaan Android — jangan disentuh
-dontwarn org.json.**
