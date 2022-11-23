package edu.sns.memorystack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.databinding.ActivityOtherBinding
import edu.sns.memorystack.databinding.ProfileEditBinding
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.FollowMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtherActivity : AppCompatActivity() {

    companion object {
        const val UID = "email"
    }
    private val binding by lazy {
       ActivityOtherBinding.inflate(layoutInflater)
    }
    private lateinit var clicked_uid :String
    private var db : FirebaseFirestore = Firebase.firestore
    var currentUser = Firebase.auth.currentUser
    var currentuid = currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        println("------------------------!!!!!!!!!!!")
        val uid = intent.getStringExtra(UID)
        //firestore에서 email이 동일한 uid 찾기
        //var db : FirebaseFirestore = Firebase.firestore
        val itemsCollectionRef = db.collection("users")
        CoroutineScope(Dispatchers.IO).launch {
              val user = AccountMethod.getUserProfile(uid!!)
              withContext(Dispatchers.Main){
                  binding.otherNickname.text = user?.nickname
              }
        }
        //팔로우
        val follower = binding.otherFollow
        follower.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                if(FollowMethod.isFollowing(currentuid!!, uid!!)) {
                    FollowMethod.unfollow(currentuid!!, uid!!)
                }
                else {
                    FollowMethod.follow(currentuid!!, uid!!)
                }
                val result = FollowMethod.isFollowing(currentuid!!, uid!!)

                val list = FollowMethod.getFollowingList(uid)

                withContext(Dispatchers.Main) {
                    if(result)
                        binding.otherFollowing.text = "unfollow ${list.size}"
                    else
                        binding.otherFollowing.text = "follow ${list.size}"
                }
            }
           //follow(uid!!)
        }
        //팔로워 팔로잉 수 가져오기
        if (uid != null) {
            //getFollowingFollower(uid)
        }
    }

    fun follow(uid: String){
        val follower = db.collection("follow").document(uid).collection("follower")
        val following = db.collection("follow").document(uid).collection("following")

        val itemMap = hashMapOf(
            "uid" to currentuid
        )

        //팔로우 하고 있는 경우 / 취소
        if(follower.get().equals(uid)){

        }
        //팔로우 하고 있지 않은 경우 / 팔로우
        else {
            currentuid?.let { follower.document(it).set(itemMap) }
        }
    }
    fun getFollowingFollower(uid : String){
        val follower = db.collection("follow").document(uid).collection("follower")
        val following = db.collection("follow").document(uid).collection("following")

        //uid 가져와짐
        following.get().addOnSuccessListener {
            for(d in it){
                println("following-----${d.id}, ${d["uid"]}")
                binding.otherFollowing.text = "following :  ${it.size()}"
            }
        }
        follower.get().addOnSuccessListener {
            for(d in it){
                println("follower-----${d.id}, ${d["uid"]}")
                binding.otherFollower.text = "follower :  ${it.size()}"
            }
        }
    }
}