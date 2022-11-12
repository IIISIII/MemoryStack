package edu.sns.memorystack.adapter

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.memorystack.PostActivity
import edu.sns.memorystack.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryListAdapter(val cursor: Cursor): RecyclerView.Adapter<GalleryListAdapter.ViewHolder>()
{
    companion object {
        val COLLECTION: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    class ViewHolder(val layout: View, val context: Context): RecyclerView.ViewHolder(layout)
    {
        fun bind(id: Long)
        {
            val imageView = layout.findViewById<ImageView>(R.id.image)

            CoroutineScope(Dispatchers.IO).launch {
                getImageFromId(context, id)?.let {
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(it)
                    }
                }
            }

            imageView.setOnClickListener {
                val intent = Intent(context, PostActivity::class.java)
                intent.putExtra(PostActivity.IMG_KEY, id)
                context.startActivity(intent)
            }
        }

        private fun getImageFromId(context: Context, id: Long): Bitmap?
        {
            id.let {
                val uri = ContentUris.withAppendedId(COLLECTION, it)
                try {
                    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        context.contentResolver.loadThumbnail(uri, Size(100, 100), null)
                    else
                        MediaStore.Images.Thumbnails.getThumbnail(context.contentResolver, it, MediaStore.Images.Thumbnails.MINI_KIND, BitmapFactory.Options())
                } catch (err: Exception) {}
            }
            return null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_list_item, parent, false)
        val params = view.layoutParams

        val pWidth = parent.measuredWidth
        val pHeight = parent.measuredHeight
        params.height = if(pWidth > pHeight) pHeight else pHeight / 3
        view.layoutParams = params

        val vh = ViewHolder(view, parent.context)

        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        cursor.moveToPosition(position)
        val id = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)
        holder.bind(cursor.getLong(id))
    }

    override fun getItemCount(): Int
    {
        return cursor.count
    }
}