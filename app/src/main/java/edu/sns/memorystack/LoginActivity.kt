package edu.sns.memorystack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.databinding.LoginLayoutBinding

class LoginActivity : AppCompatActivity()
{
    private val binding by lazy {
        LoginLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if(binding.autoLogin.isChecked) {
            //auto login
        }

        binding.loginButton.setOnClickListener loginButton@ {
            val nickname = binding.nickname.text.toString()
            val password = binding.password.text.toString()

            if(nickname.isEmpty()) {
                binding.nickname.error = "Please write Nickname"
                return@loginButton
            }
            if(password.isEmpty()) {
                binding.password.error = "Please write Password"
                return@loginButton
            }

            Firebase.auth.signInWithEmailAndPassword(nickname, password)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        //login success
                    }
                    else {
                        //login failed
                    }
                }
        }
    }
}