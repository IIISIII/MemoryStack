package edu.sns.memorystack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private var emailFlag = false
    private var passwordFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setIcon(R.drawable.stack_logo)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.label_login)

        binding.loginButton.isEnabled = false

        val pattern = android.util.Patterns.EMAIL_ADDRESS

        binding.emailText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                val match = pattern.matcher(s.toString())
                binding.emailText.apply {
                    isErrorEnabled = !match.matches()
                    emailFlag = !isErrorEnabled
                    error = if(isErrorEnabled) getString(R.string.error_email_format) else null
                }

                binding.loginButton.isEnabled = emailFlag && passwordFlag
            }
        })

        binding.passwordText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                passwordFlag = s.toString().isNotBlank()

                binding.loginButton.isEnabled = emailFlag && passwordFlag
            }
        })

        binding.loginButton.setOnClickListener {
            val email = binding.emailText.editText?.text.toString()
            val password = binding.passwordText.editText?.text.toString()

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

        var result = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                result = AccountMethod.login(id, password)
                if(result) {
                    withContext(Dispatchers.Main) {
                        moveToMainActivity()
                    }
                }
            } catch(err: Exception) {}
            if(!result) {
                withContext(Dispatchers.Main) {
                    errorLog(getString(R.string.error_login_fail))
                    uiSetEnable(true)
                }
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
        val isBlank = msg.isBlank()
        binding.emailText.isErrorEnabled = !isBlank
        binding.emailText.error = if(isBlank) null else msg
    }

    private fun uiSetEnable(enable: Boolean)
    {
        binding.emailText.isEnabled = enable
        binding.passwordText.isEnabled = enable
        binding.loginButton.isEnabled = enable
        binding.createAccount.isEnabled = enable
    }
}