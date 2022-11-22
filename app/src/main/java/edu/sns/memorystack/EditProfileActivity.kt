package edu.sns.memorystack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.databinding.ProfileEditBinding
import edu.sns.memorystack.fragment.ProfileFragment
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ProfileEditBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //
        val editProfile = binding.editNickname
        val editPhone = binding.editTextPhone
        val save_button = binding.saveProfile
        //uid받기
        val auth = Firebase.auth
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        //firestore
        val db : FirebaseFirestore = Firebase.firestore
        val itemsCollectionRef = db.collection("users")
        //


        val profile_frag = ProfileFragment()
        //버튼 클릭시 닉네임 변경
        save_button.setOnClickListener{
            val nickname = editProfile.text.toString()
            val phone = editPhone.text.toString()
            val edit = UserProfile("",nickname,"","",phone)
            CoroutineScope(Dispatchers.IO).launch{
                AccountMethod.updateUserProfile(uid, edit)
            }
            //itemsCollectionRef.document(uid).update("nickname", nickname)
            finish()
        }
    }
}