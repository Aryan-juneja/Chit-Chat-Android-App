package com.example.whatsappclone.Views.views



import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.widget.Toast

class DownloadReceiver : BroadcastReceiver() {
    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor: Cursor? = downloadManager.query(query)

        if (cursor != null && cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Toast.makeText(context, "File downloaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
            }
        }
        cursor?.close()
    }
}
