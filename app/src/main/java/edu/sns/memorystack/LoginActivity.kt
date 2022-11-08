package edu.sns.memorystack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.databinding.ActivityLoginBinding
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            uiSetEnable(false)
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

        CoroutineScope(Dispatchers.IO).launch {
            val result = AccountMethod.login(id, password)

            withContext(Dispatchers.Main) {
                when (result) {
                    AccountMethod.AccountMethodResult.SUCCESS -> {
                        moveToMainActivity()
                    }
                    AccountMethod.AccountMethodResult.EMAIL_BLANK -> {
                        errorLog("Please enter E-mail")
                    }
                    AccountMethod.AccountMethodResult.PASSWORD_BLANK -> {
                        errorLog("Please enter Password")
                    }
                    else -> {
                        errorLog("Please check E-mail and Password")
                    }
                }
                uiSetEnable(true)
            }
        }
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

    private fun uiSetEnable(enable: Boolean)
    {
        binding.email.isEnabled = enable
        binding.password.isEnabled = enable
        binding.loginButton.isEnabled = enable
        binding.createAccount.isEnabled = enable
    }
}