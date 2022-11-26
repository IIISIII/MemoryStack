package edu.sns.memorystack.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import edu.sns.memorystack.OtherActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.data.PostData
import edu.sns.memorystack.databinding.FollowListItemBinding
import edu.sns.memorystack.databinding.PostListLoadingItemBinding
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.FollowMethod
import edu.sns.memorystack.method.StorageMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowListAdapter(private var uids: ArrayList<String?>, private val currentUid: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    companion object
    {
        const val TYPE_ITEM = 0
        const val TYPE_LOADING = 1
    }

    class ItemViewHolder(val binding: FollowListItemBinding,  val context: Context): ViewHolder(binding.root)
    {
        private var flag = false
        private val repo = DataRepository.getInstance()

        fun bind(uid : String, currentUid: String)
        {
            val nickname: TextView = binding.listNickname
            val follow = binding.buttonFollow
            val follow_img = binding.followImg
            val email = binding.listEmail

            follow.setOnClickListener {
                if(flag)
                    return@setOnClickListener

                follow.text = "..."
                flag = true
                CoroutineScope(Dispatchers.IO).launch {
                    if(FollowMethod.isFollowing(currentUid, uid))
                        FollowMethod.unfollow(currentUid, uid)
                    else
                        FollowMethod.follow(currentUid, uid)

                    val result = FollowMethod.isFollowing(currentUid, uid);
                    withContext(Dispatchers.Main) {
                        follow.setText(if(result) R.string.text_unfollow_btn else R.string.text_follow_btn)
                        flag = false
                    }
                }
            }

            binding.root.setOnClickListener{
                val intent = Intent(context, OtherActivity::class.java)
                intent.putExtra(OtherActivity.UID, uid)
                context.startActivity(intent)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val profile = repo.getUserProfile(uid)
                val isFollowing = FollowMethod.isFollowing(currentUid, uid)
                withContext(Dispatchers.Main) {
                    email.text = profile?.email
                    nickname.text = profile?.nickname
                    follow.setText(if(isFollowing) R.string.text_unfollow_btn else R.string.text_follow_btn)
                }
                profile?.imgPath?.let {
                    val image = repo.getImage(it)
                    withContext(Dispatchers.Main) {
                        follow_img.setImageBitmap(image)
                    }
                } ?:
                    withContext(Dispatchers.Main) {
                        follow_img.setImageResource(R.drawable.usericon_small)
                    }
            }
        }
    }

    class LoadingViewHolder(private val binding: PostListLoadingItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_ITEM -> {
                val binding = FollowListItemBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding, parent.context)
            }
            else -> {
                val binding = PostListLoadingItemBinding.inflate(inflater, parent, false)
                LoadingViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int)
    {
        if(viewHolder is ItemViewHolder) {
            val user : String = uids[position]!!
            viewHolder.bind(user, currentUid)
        }
    }

    override fun getItemCount(): Int
    {
        return uids.size
    }

    override fun getItemViewType(position: Int): Int
    {
        return when(uids[position]) {
            null -> TYPE_LOADING
            else -> TYPE_ITEM
        }
    }

    fun addItemList(list: ArrayList<String>)
    {
        val size = uids.size
        uids.addAll(list)
        notifyItemInserted(size)
        notifyItemRangeChanged(size, list.size)
    }

    fun clear()
    {
        uids.clear()
        notifyDataSetChanged()
    }

    fun removeLoading()
    {
        if(uids.size == 0)
            return
        val last = uids.lastIndex
        if(uids[last] == null) {
            uids.removeAt(last)
            notifyItemChanged(last)
        }
    }
}