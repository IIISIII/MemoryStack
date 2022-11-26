package edu.sns.memorystack.adapter

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.memorystack.R
import edu.sns.memorystack.method.StorageMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryListAdapter(val cursor: Cursor, val onClick: (Long) -> Unit): RecyclerView.Adapter<GalleryListAdapter.ViewHolder>()
{
    class ViewHolder(val layout: View, val context: Context): RecyclerView.ViewHolder(layout)
    {
        fun bind(id: Long, onClick: (Long) -> Unit)
        {
            val imageView = layout.findViewById<ImageView>(R.id.image)

            CoroutineScope(Dispatchers.IO).launch {
                StorageMethod.getImageFromMediaStore(context, id)?.let {
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(it)
                    }
                }
            }

            imageView.setOnClickListener {
                onClick(id)
            }
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
        holder.bind(cursor.getLong(id), onClick)
    }

    override fun getItemCount(): Int
    {
        return cursor.count
    }
}