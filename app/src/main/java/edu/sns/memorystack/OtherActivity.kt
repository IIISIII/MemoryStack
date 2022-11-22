package edu.sns.memorystack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.databinding.ActivityOtherBinding
import edu.sns.memorystack.databinding.ProfileEditBinding
import edu.sns.memorystack.method.AccountMethod
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
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uid = intent.getStringExtra(UID)
        //firestore에서 email이 동일한 uid 찾기
        var db : FirebaseFirestore = Firebase.firestore
        val itemsCollectionRef = db.collection("users")
        CoroutineScope(Dispatchers.IO).launch {
              val user = AccountMethod.getUserProfile(uid!!)
              withContext(Dispatchers.Main){
                  binding.otherNickname.text = user?.nickname
              }
            }
    }
}