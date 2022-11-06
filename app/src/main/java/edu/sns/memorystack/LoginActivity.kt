package edu.sns.memorystack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.databinding.ActivityLoginBinding
import edu.sns.memorystack.method.AccountMethod

class LoginActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            login(email, password)
        }

        binding.createAccount.setOnClickListener {
            moveToAccountActivity()
        }
    }

    override fun onResume()
    {
        super.onResume()
        Firebase.auth.currentUser?.let {
            moveToMainActivity()
        }
    }

    private fun login(id: String, password: String)
    {
        errorLog("")

        AccountMethod.login(id, password, ::moveToMainActivity, ::errorLog)
    }

    private fun moveToMainActivity()
    {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun moveToAccountActivity()
    {
        startActivity(Intent(this, CreateAccountActivity::class.java))
    }

    private fun errorLog(msg: String)
    {
        binding.errorText.text = msg
    }
}