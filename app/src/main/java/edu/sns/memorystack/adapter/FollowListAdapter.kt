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
import edu.sns.memorystack.OtherActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.FollowMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowListAdapter(private var uids: ArrayList<String>, private val currentUid: String) : RecyclerView.Adapter<FollowListAdapter.ViewHolder>()
{

    //viewHolder 생성, 아이템
     class ViewHolder(itemView: View,  val context: Context): RecyclerView.ViewHolder(itemView)
    {
        private var flag = false

        fun bind(uid : String, currentUid: String){
            val nickname: TextView = itemView.findViewById(R.id.list_nickname)
            val follow = itemView.findViewById<Button>(R.id.button_follow)
            val follow_img = itemView.findViewById<ImageView>(R.id.follow_img)
            val email = itemView.findViewById<TextView>(R.id.list_email)

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
                        follow.text = if(result) "unfollow" else "follow"
                        flag = false
                    }
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                val profile = AccountMethod.getUserProfile(uid)
                val isFollowing = FollowMethod.isFollowing(currentUid, uid)
                withContext(Dispatchers.Main){
                    email.text = profile?.email
                    nickname.text = profile?.nickname
                    follow.text = if(isFollowing) "unfollow" else "follow"

                    follow_img.setOnClickListener{
                        val intent = Intent(context, OtherActivity::class.java)
                        intent.putExtra(OtherActivity.UID, uid)
                        context.startActivity(intent)
                    }
                }

            }
        }


    }
    //레이아웃 지정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_follow, parent, false)
        val view = ViewHolder(itemView, parent.context)
        return view
    }
    //ArrayList에 각 항목 대입
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)
    {
        val user : String = uids[position]
        viewHolder.bind(user, currentUid)
    }
    //사이즈만틈 반환
    override fun getItemCount(): Int
    {
        return uids.size
    }

    fun setUserList(list: ArrayList<String>)
    {
        uids = list
        notifyDataSetChanged()
    }
}