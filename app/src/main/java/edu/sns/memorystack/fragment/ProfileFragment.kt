package edu.sns.memorystack.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.R

class ProfileFragment: Fragment()
{

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
        val auth = Firebase.auth
        val currentUser = auth.currentUser ?: return
        val uid = currentUser?.uid
        println("####################${uid}")
        //파이어베이스 database
        val db :  FirebaseFirestore = Firebase.firestore
        val itemsCollectionRef = db.collection("users")
        //nickname 설정
        val nickname = view.findViewById<TextView>(R.id.nickname)

        //uid이용하여 nickname 지정
       itemsCollectionRef.document(uid.toString()).get()
            .addOnSuccessListener {
                nickname.text = it["nickname"].toString()
                println(nickname.setText(it["nickname"].toString()))
            }
        //edit처리
        val edit = view.findViewById<Button>(R.id.edit_profile)

        edit.setOnClickListener {
            println("button clicked#############################")
        }
    }
}


