package edu.sns.memorystack.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.memorystack.PostDataActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.data.PostData
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.databinding.PostListItemBinding
import edu.sns.memorystack.databinding.PostListLoadingItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class PostListAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var itemList: ArrayList<PostData?> = ArrayList()
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    class ItemViewHolder(private val binding: PostListItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root)
    {
        companion object {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val repo = DataRepository.getInstance()
        }

        var profile: UserProfile? = null

        fun bind(post: PostData)
        {
            val userImage = binding.userProfileImage
            val userNickname = binding.userNickname
            val date = binding.date
            val postImage = binding.postImage
            val postText = binding.postText
            val postLoading = binding.imageLoading
            val layout = binding.card

            val formatDate = dateFormat.format(post.date.toDate())

            postText.text = post.text
            date.text = formatDate

            postImage.setOnClickListener {
                postImage.scaleType = if(postImage.scaleType == ImageView.ScaleType.CENTER_CROP)
                    ImageView.ScaleType.FIT_CENTER
                else
                    ImageView.ScaleType.CENTER_CROP
            }

            layout.setOnClickListener {
                val intent = Intent(context, PostDataActivity::class.java)
                intent.putExtra(PostDataActivity.KEY_UID, post.uid)
                intent.putExtra(PostDataActivity.KEY_POST_IMG, post.imgPath)
                intent.putExtra(PostDataActivity.KEY_POST_DATE, formatDate)
                intent.putExtra(PostDataActivity.KEY_POST_TEXT, post.text)
                context.startActivity(intent)
            }

            CoroutineScope(Dispatchers.IO).launch {
                profile = repo.getUserProfile(post.uid, false)
                profile?.let {
                    withContext(Dispatchers.Main) {
                        userNickname.text = it.nickname
                    }

                    it.imgPath?.let { path ->
                        val image = repo.getImage(path)
                        withContext(Dispatchers.Main) {
                            userImage.setImageBitmap(image)
                        }
                    } ?:
                        withContext(Dispatchers.Main) {
                            userImage.setImageResource(R.drawable.usericon)
                        }
                }

                repo.getImage(post.imgPath)?.let {
                    withContext(Dispatchers.Main) {
                        postImage.setImageBitmap(it)
                        postLoading.visibility = View.GONE
                        postImage.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    class LoadingViewHolder(private val binding: PostListLoadingItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = PostListItemBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding, parent.context)
            }
            else -> {
                val binding = PostListLoadingItemBinding.inflate(inflater, parent, false)
                LoadingViewHolder(binding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        if(holder is ItemViewHolder)
            holder.bind(itemList[position]!!)
    }

    override fun getItemCount(): Int
    {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(itemList[position]) {
            null -> VIEW_TYPE_LOADING
            else -> VIEW_TYPE_ITEM
        }
    }

    fun addItemList(list: ArrayList<PostData>)
    {
        val size = itemList.size
        itemList.addAll(list)
        itemList.add(null)
        notifyItemInserted(size)
        notifyItemRangeChanged(size, list.size)
    }

    fun clear()
    {
        itemList.clear()
        notifyDataSetChanged()
    }

    fun removeLoading()
    {
        if(itemList.size == 0)
            return
        val last = itemList.lastIndex
        if(itemList[last] == null) {
            itemList.removeAt(last)
            notifyItemChanged(last)
        }
    }
}