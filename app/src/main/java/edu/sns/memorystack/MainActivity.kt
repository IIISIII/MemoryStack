package edu.sns.memorystack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.databinding.ActivityMainBinding
import edu.sns.memorystack.method.AccountMethod

class MainActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        currentUser = auth.currentUser
        if(currentUser == null)
            moveToLoginActivity()
    }

    override fun onResume()
    {
        super.onResume()

        currentUser?.let {
            AccountMethod.getUserProfile(it.uid, ::onResult)
        }
    }

    override fun onNewIntent(intent: Intent?)
    {
        super.onNewIntent(intent)
    }

    private fun moveToLoginActivity()
    {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun onResult(profile: UserProfile?)
    {
        if(profile == null)
            return
        binding.test.text = profile!!.nickname
    }
}