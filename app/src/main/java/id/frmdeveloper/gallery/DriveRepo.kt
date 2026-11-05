package id.frmdeveloper.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns

/**
 * Akses Google Drive MURNI lewat Storage Access Framework (SAF):
 * user memilih folder Drive lewat ACTION_OPEN_DOCUMENT_TREE, aplikasi menyimpan
 * persistable URI permission. Tanpa Google Drive API / API pihak ketiga.
 */
object DriveStore {
    private const val PREF = "galeri_drive_prefs"
    private const val KEY_TREE = "drive_tree_uri"
    private const val KEY_API = "drive_api_enabled"

    fun loadTreeUri(context: Context): Uri? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TREE, null)?.let { Uri.parse(it) }

    fun saveTreeUri(context: Context, uri: Uri?) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_TREE, uri?.toString()).apply()
    }

    /**
     * Drive API TIDAK login otomatis (perintah user 2026-11-04): bawaan OFF.
     * User login manual lewat tab Menu; status login tersimpan sampai logout.
     */
    fun loadApiEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_API, false)

    fun saveApiEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_API, enabled).apply()
    }

    // ---- Akun Drive lain (login manual loopback OAuth) ----
    private const val KEY_CUSTOM_RT = "drive_custom_refresh_token"
    private const val KEY_EMAIL = "drive_account_email"

    /** Refresh token hasil login akun lain; null = pakai akun tersimpan (gd.js). */
    fun loadCustomRt(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_RT, null)?.ifBlank { null }

    fun saveCustomRt(context: Context, rt: String?) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_CUSTOM_RT, rt).apply()
    }

    fun loadAccountEmail(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, "") ?: ""

    fun saveAccountEmail(context: Context, email: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_EMAIL, email).apply()
    }

    // ---- Multi-akun Drive (daftar akun hasil login manual) ----
    private const val KEY_AKUN_LIST = "drive_akun_list_json"

    /**
     * Semua akun Drive yg login di HP ini. Migrasi otomatis: kalau daftar
     * masih kosong tapi ada login lama (rt + email tunggal), login lama itu
     * jadi akun pertama.
     */
    fun loadDaftarAkun(context: Context): List<AkunDrive> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val mentah = prefs.getString(KEY_AKUN_LIST, null)
        val hasil = mutableListOf<AkunDrive>()
        if (!mentah.isNullOrBlank()) {
            runCatching {
                val arr = org.json.JSONArray(mentah)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val e = o.optString("e")
                    val r = o.optString("r")
                    if (e.isNotBlank() && r.isNotBlank()) hasil += AkunDrive(e, r)
                }
            }
        }
        if (hasil.isEmpty()) {
            val rtLama = loadCustomRt(context)
            val emailLama = loadAccountEmail(context)
            if (rtLama != null && emailLama.isNotBlank()) {
                hasil += AkunDrive(emailLama, rtLama)
                saveDaftarAkun(context, hasil)
            }
        }
        return hasil
    }

    fun saveDaftarAkun(context: Context, daftar: List<AkunDrive>) {
        val arr = org.json.JSONArray()
        daftar.forEach { a ->
            arr.put(org.json.JSONObject().put("e", a.email).put("r", a.refreshToken))
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_AKUN_LIST, arr.toString()).apply()
        // Sinkronkan kunci lama ke akun pertama biar jalur lama tetap waras
        saveCustomRt(context, daftar.firstOrNull()?.refreshToken)
        saveAccountEmail(context, daftar.firstOrNull()?.email ?: "")
    }

    /** Tambah akun baru (email sama = tokennya diperbarui). */
    fun tambahAkun(context: Context, akun: AkunDrive): List<AkunDrive> {
        val daftar = loadDaftarAkun(context).filter { it.email != akun.email } + akun
        saveDaftarAkun(context, daftar)
        return daftar
    }

    /** Keluarkan satu akun dari daftar. */
    fun hapusAkun(context: Context, email: String): List<AkunDrive> {
        val daftar = loadDaftarAkun(context).filter { it.email != email }
        saveDaftarAkun(context, daftar)
        return daftar
    }
}

/** Satu akun Google Drive hasil login manual (email + refresh token). */
data class AkunDrive(val email: String, val refreshToken: String)

data class DriveScan(
    val albums: List<Album>,
    val allItems: List<MediaItem>,
)

object DriveRepo {

    private data class DocRow(
        val docId: String,
        val name: String,
        val mime: String,
        val size: Long,
        val modified: Long,
    )

    private fun listChildren(context: Context, treeUri: Uri, parentDocId: String): List<DocRow> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
        val rows = mutableListOf<DocRow>()
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { c ->
                val iId = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val iName = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val iMime = c.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val iSize = c.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val iMod = c.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                while (c.moveToNext()) {
                    rows += DocRow(
                        docId = if (iId >= 0) c.getString(iId) ?: continue else continue,
                        name = if (iName >= 0) c.getString(iName) ?: "(tanpa nama)" else "(tanpa nama)",
                        mime = if (iMime >= 0) c.getString(iMime) ?: "" else "",
                        size = if (iSize >= 0 && !c.isNull(iSize)) c.getLong(iSize) else 0,
                        modified = if (iMod >= 0 && !c.isNull(iMod)) c.getLong(iMod) else 0,
                    )
                }
            }
        }
        return rows
    }

    private fun DocRow.toMediaItem(context: Context, treeUri: Uri, folderName: String): MediaItem {
        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        return MediaItem(
            key = docUri.toString(),
            uri = docUri,
            name = name,
            mime = mime,
            size = size,
            dateMillis = modified,
            isVideo = mime.startsWith("video/"),
            bucketId = "drive:$folderName",
            bucketName = folderName,
            relativePath = "",
            isDrive = true,
        )
    }

    private fun isMedia(mime: String) = mime.startsWith("image/") || mime.startsWith("video/")

    /** Enumerasi root + 1 level subfolder (tiap subfolder jadi sub-album). */
    fun scan(context: Context, treeUri: Uri): DriveScan {
        val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootRows = listChildren(context, treeUri, rootDocId)
        val albums = mutableListOf<Album>()
        val all = mutableListOf<MediaItem>()

        val rootItems = rootRows.filter { isMedia(it.mime) }
            .map { it.toMediaItem(context, treeUri, "Google Drive") }
            .sortedByDescending { it.dateMillis }
        if (rootItems.isNotEmpty()) {
            albums += Album(id = "drive:root", name = "Google Drive", kind = AlbumKind.DRIVE, items = rootItems)
            all += rootItems
        }
        for (row in rootRows) {
            if (row.mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                val subItems = listChildren(context, treeUri, row.docId)
                    .filter { isMedia(it.mime) }
                    .map { it.toMediaItem(context, treeUri, row.name) }
                    .sortedByDescending { it.dateMillis }
                if (subItems.isNotEmpty()) {
                    albums += Album(id = "drive:${row.docId}", name = row.name, kind = AlbumKind.DRIVE, items = subItems)
                    all += subItems
                }
            }
        }
        return DriveScan(albums = albums, allItems = all.sortedByDescending { it.dateMillis })
    }

    fun displayName(context: Context, uri: Uri): String {
        var name: String? = null
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) name = c.getString(idx)
                }
            }
        }
        return name ?: uri.lastPathSegment ?: "Google Drive"
    }

    fun takePermission(context: Context, treeUri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching { context.contentResolver.takePersistableUriPermission(treeUri, flags) }
    }

    fun releasePermission(context: Context, treeUri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching { context.contentResolver.releasePersistableUriPermission(treeUri, flags) }
    }

    /**
     * Upload item lokal ke ROOT tree Drive. Balik Pair(sukses, gagal).
     */
    fun upload(context: Context, treeUri: Uri, items: List<MediaItem>): Pair<Int, Int> {
        val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootDocId)
        var ok = 0
        var fail = 0
        for (item in items) {
            val done = runCatching {
                val newDoc = DocumentsContract.createDocument(
                    context.contentResolver, parentUri,
                    item.mime.ifEmpty { "application/octet-stream" }, item.name
                ) ?: error("createDocument null")
                val input = context.contentResolver.openInputStream(item.uri) ?: error("input null")
                val output = context.contentResolver.openOutputStream(newDoc) ?: error("output null")
                input.use { ins -> output.use { outs -> ins.copyTo(outs) } }
                true
            }.getOrDefault(false)
            if (done) ok++ else fail++
        }
        return ok to fail
    }
}
