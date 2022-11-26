package edu.sns.memorystack.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.EditProfileActivity
import edu.sns.memorystack.LoginActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.data.FollowDTO
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragment: Fragment()
{
    var db :  FirebaseFirestore = Firebase.firestore
    var auth = Firebase.auth
    var currentUser = auth.currentUser
    var uid = currentUser?.uid

    //
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.profile_page, container, false)
    }
    //
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        //파이어베이스 auth
        //val auth = Firebase.auth
        //val currentUser = auth.currentUser ?: return
        //val uid = currentUser?.uid
        println("####################${uid}")
        //파이어베이스 database
        //val db :  FirebaseFirestore = Firebase.firestore
        val itemsCollectionRef = db.collection("users")
        //nickname 설정
        val nickname = view.findViewById<TextView>(R.id.nickname)
        //uid이용하여 nickname 지정
        /*
       itemsCollectionRef.document(uid.toString()).get()
            .addOnSuccessListener {
                nickname.text = it["nickname"].toString()
                println(nickname.setText(it["nickname"].toString()))
            }

         */
        CoroutineScope(Dispatchers.IO).launch {
            val user_profile = AccountMethod.getUserProfile(uid.toString())
            nickname.text = user_profile?.nickname
        }
        //edit처리
        val edit = view.findViewById<Button>(R.id.edit_profile)

        edit.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        //getFollowerAndFollowing()
        uid?.let { getFollowingFollower(it) }
    }

    fun getFollowingFollower(uid : String){
        val follower = db.collection("follow").document(uid).collection("follower")
        val following = db.collection("follow").document(uid).collection("following")

        //uid 가져와짐
        following.get().addOnSuccessListener {
            for(d in it){
                println("following-----${d.id}, ${d["uid"]}")
                view?.findViewById<TextView>(R.id.followingCount)?.text = it.size().toString()
            }
        }
        follower.get().addOnSuccessListener {
            for(d in it){
                println("follower-----${d.id}, ${d["uid"]}")
                view?.findViewById<TextView>(R.id.followerCount)?.text = it.size().toString()
            }
        }
    }

    //팔로워, 팔로잉 수 받아옴
//    fun getFollowerAndFollowing(){
//        db.collection("follow").document(uid!!).addSnapshotListener { value, error ->
//            if(value == null) return@addSnapshotListener
//            val followDTO = value.toObject(FollowDTO::class.java)
//            if(followDTO?.followingCount != null){
//                view?.findViewById<TextView>(R.id.followingCount)?.text = followDTO.followingCount.toString()
//            }
//            if(followDTO?.followerCount != null) {
//                view?.findViewById<TextView>(R.id.followerCount)?.text =
//                    followDTO.followerCount.toString()
//            }
//        }
//    }
}