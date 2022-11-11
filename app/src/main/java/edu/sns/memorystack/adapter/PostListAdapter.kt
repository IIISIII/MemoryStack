package edu.sns.memorystack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.memorystack.R
import edu.sns.memorystack.data.PostData
import edu.sns.memorystack.method.PostMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostListAdapter(): RecyclerView.Adapter<PostListAdapter.ViewHolder>()
{
    private var itemList: ArrayList<PostData>? = null

    class ViewHolder(val layout: View): RecyclerView.ViewHolder(layout)
    {
        fun bind(post: PostData?)
        {
            if(post == null)
                return

            val postImage = layout.findViewById<ImageView>(R.id.post_image)
            val postText = layout.findViewById<TextView>(R.id.post_text)
            val postLoading = layout.findViewById<ProgressBar>(R.id.image_loading)

            postText.text = post.text

            CoroutineScope(Dispatchers.IO).launch {
                PostMethod.getImage(post.imgPath)?.let {
                    withContext(Dispatchers.Main) {
                        postImage.setImageBitmap(it)
                        postLoading.visibility = View.GONE
                        postImage.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(itemList?.get(position))
    }

    override fun getItemCount(): Int
    {
        return if(itemList == null)
            0
        else
            itemList!!.size
    }

    fun setItemList(list: ArrayList<PostData>)
    {
        itemList = list
        notifyDataSetChanged()
    }
}