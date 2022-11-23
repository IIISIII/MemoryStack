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
import edu.sns.memorystack.data.ProfileRepository
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.PostMethod
import edu.sns.memorystack.method.StorageMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class PostListAdapter(): RecyclerView.Adapter<PostListAdapter.ViewHolder>()
{
    private var itemList: ArrayList<PostData>? = null

    class ViewHolder(val layout: View): RecyclerView.ViewHolder(layout)
    {
        companion object {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd")
            val profileRepo = ProfileRepository.getInstance()
        }

        fun bind(post: PostData?)
        {
            if(post == null)
                return

            val userImage = layout.findViewById<ImageView>(R.id.user_profile_image)
            val userNickname = layout.findViewById<TextView>(R.id.user_nickname)
            val date = layout.findViewById<TextView>(R.id.date)
            val postImage = layout.findViewById<ImageView>(R.id.post_image)
            val postText = layout.findViewById<TextView>(R.id.post_text)
            val postLoading = layout.findViewById<ProgressBar>(R.id.image_loading)

            postText.text = post.text
            date.text = dateFormat.format(post.date.toDate())

            CoroutineScope(Dispatchers.IO).launch {
                profileRepo.getUserProfile(post.uid)?.let {
                    withContext(Dispatchers.Main) {
                        userNickname.text = it.nickname
                    }
                }

                StorageMethod.getImage(post.imgPath)?.let {
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