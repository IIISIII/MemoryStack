package edu.sns.memorystack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.memorystack.R
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.data.User

class FollowListAdapter(private val userProfile: ArrayList<UserProfile>) : RecyclerView.Adapter<FollowListAdapter.ViewHolder>() {

    //viewHolder 생성, 아이템
    public class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val email: TextView = itemView.findViewById(R.id.list_email)
        val name: TextView = itemView.findViewById(R.id.list_name)
        var nickname: TextView = itemView.findViewById(R.id.list_nickname)
        val button_follow = itemView.findViewById<Button>(R.id.button_follow)
    }
    //레이아웃 지정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_follow, parent, false)
        return ViewHolder(itemView)
    }
    //ArrayList에 각 항목 대입
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int){
        val user : UserProfile = userProfile[position]
        viewHolder.email.text = user.email
        viewHolder.name.text = user.name
        viewHolder.nickname.text = user.nickname
        //버튼에 대한 이벤트 처리
        val follow = viewHolder.button_follow
        follow.setOnClickListener {
            if(follow.text == "follow")
                follow.text = "unfollow"
            else if(follow.text == "unfollow")
                follow.text = "follow"
        }
    }
    //사이즈만틈 반환
    override fun getItemCount(): Int{
        return userProfile.size
    }
}