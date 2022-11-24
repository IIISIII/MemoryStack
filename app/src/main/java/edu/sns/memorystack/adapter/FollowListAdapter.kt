package edu.sns.memorystack.adapter

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ClipDrawable.VERTICAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import edu.sns.memorystack.EditProfileActivity
import edu.sns.memorystack.OtherActivity
import edu.sns.memorystack.PostActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.data.FollowDTO
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.data.User
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text

class FollowListAdapter(private val userProfile: ArrayList<String>) : RecyclerView.Adapter<FollowListAdapter.ViewHolder>() {

    //viewHolder 생성, 아이템
     class ViewHolder(itemView: View,  val context: Context): RecyclerView.ViewHolder(itemView) {

        //액티비티 이동
        fun bind(userProfile : String){
            //val name: TextView = itemView.findViewById(R.id.list_name)
            var nickname: TextView = itemView.findViewById(R.id.list_nickname)
            val follow = itemView.findViewById<Button>(R.id.button_follow)
            val follow_img = itemView.findViewById<ImageView>(R.id.follow_img)
            val email = itemView.findViewById<TextView>(R.id.list_email)
            CoroutineScope(Dispatchers.IO).launch {
                val profile = AccountMethod.getUserProfile(userProfile)
                withContext(Dispatchers.Main){
                    follow.setOnClickListener {
                        if(follow.text == "follow")
                            follow.text = "unfollow"
                        else if(follow.text == "unfollow")
                            follow.text = "follow"
                    }
                    //
                    email.text = profile?.email
                    //name.text = profile?.name
                    nickname.text = profile?.nickname
                    //
                    follow_img.setOnClickListener{
                        val intent = Intent(context, OtherActivity::class.java)
                        intent.putExtra(OtherActivity.UID, userProfile)
                        context.startActivity(intent)
                    }
                }

            }
        }


    }
    //레이아웃 지정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_follow, parent, false)
        val view = ViewHolder(itemView, parent.context)
        return view
    }
    //ArrayList에 각 항목 대입
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int){
        val user : String = userProfile[position]
        viewHolder.bind(user)
    }
    //사이즈만틈 반환
    override fun getItemCount(): Int{
        return userProfile.size
    }
}