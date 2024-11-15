package Adapters
import Models.DateHeader
import Models.chatEvent
import Models.messages
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.Views.views.DownloadReceiver
import com.example.whatsappclone.Views.views.hackedViewHolder
import com.google.android.material.card.MaterialCardView
import utils.DoubleTapListener
import utils.formatAsTime

class ChatAdapter(private val list: MutableList<chatEvent>, private val mCurrentId: String,private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var highFiveClick: ((id: String, status: Boolean) -> Unit)? = null
    private val downloadReceiver = DownloadReceiver()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("TAG", "onCreateViewHolder: list: $list")
        return when (viewType) {
            Message_Sent -> MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chatsent, parent, false))
            Message_Received -> MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chatreceive, parent, false))
            Date_Header -> DateFormatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.date_header, parent, false))
            FILE -> FileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.files, parent, false))
            else -> hackedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.hackedlayout, parent, false))
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = list[position]) {
            is messages -> {
                if (item.type == "File") {
                    bindFileViewHolder(holder as FileViewHolder, item)
                } else {
                    bindMessageViewHolder(holder as MessageViewHolder, item)
                }
            }
            is DateHeader -> bindDateHeaderViewHolder(holder as DateFormatViewHolder, item)
            else -> Log.e("TAG", "Unsupported view type: ${item.javaClass.simpleName}")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindMessageViewHolder(holder: MessageViewHolder, message: messages) {
        holder.itemView.findViewById<TextView>(R.id.timeline).text = message.sentAt.formatAsTime()
        holder.itemView.findViewById<TextView>(R.id.ContentView).text = message.mssg
        val like = holder.itemView.findViewById<MaterialCardView>(R.id.like)
        val img = holder.itemView.findViewById<ImageView>(R.id.thumbs_up_button)

        img.visibility = if (message.liked) View.VISIBLE else View.GONE

        like?.let {
            it.setOnTouchListener(DoubleTapListener(holder.itemView.context) {
                message.liked = !message.liked
                highFiveClick?.invoke(message.msgId, message.liked)
                img.visibility = if (message.liked) View.VISIBLE else View.GONE
            })
        } ?: Log.e("TAG", "MaterialCardView 'like' is null")
    }

    private fun bindDateHeaderViewHolder(holder: DateFormatViewHolder, dateHeader: DateHeader) {
        holder.itemView.findViewById<TextView>(R.id.textView).text = dateHeader.date.toString()
    }

    private fun bindFileViewHolder(holder: FileViewHolder, message: messages) {
        holder.itemView.findViewById<TextView>(R.id.ContentView).text = message.msgId // Assuming mssg contains the file name or URL
        holder.itemView.findViewById<TextView>(R.id.timeline).text = message.sentAt.formatAsTime()

        holder.itemView.findViewById<Button>(R.id.download_button).setOnClickListener {
            checkAndRequestPermissions(holder.itemView.context as Activity, message.mssg) // Assuming fileUrl is a field in messages
        }
    }

    private fun checkAndRequestPermissions(activity: Activity, fileUrl: String) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
        } else {
            // Permissions are already granted, proceed with download
            downloadFileUsingDownloadManager(activity, fileUrl)
        }
    }

    private fun downloadFileUsingDownloadManager(context: Context, fileUrl: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setTitle("Downloading File")
        request.setDescription("File is being downloaded...")
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "File") // Customize your filename
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request) // Enqueue the download
    }

    override fun getItemViewType(position: Int): Int {
        return when (val event = list[position]) {
            is messages -> {
                if (event.type == "File") {
                    FILE
                } else if (event.senderId == mCurrentId) {
                    Message_Sent
                } else {
                    Message_Received
                }
            }
            is DateHeader -> Date_Header
            else -> Unsupported
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val context = recyclerView.context
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED // or ContextCompat.RECEIVER_EXPORTED if you want it to be accessible by other apps
        )
//        context.registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        // Unregister the BroadcastReceiver when the view is recycled to avoid memory leaks
        val context = holder.itemView.context
        try {
            context.unregisterReceiver(downloadReceiver)
        } catch (e: IllegalArgumentException) {
            // Ignore if the receiver wasn't registered
        }
    }

    companion object {
        const val Message_Sent = 0
        const val Message_Received = 1
        const val Date_Header = 2
        const val Unsupported = -1
        const val FILE = 3
        var currentState: Boolean = false
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class DateFormatViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view)
}