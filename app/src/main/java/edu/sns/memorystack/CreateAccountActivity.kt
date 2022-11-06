package edu.sns.memorystack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.databinding.ActivityCreateAccountBinding
import edu.sns.memorystack.method.AccountMethod

class CreateAccountActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityCreateAccountBinding.inflate(layoutInflater)
    }

    private lateinit var profile: UserProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.createAccount.setOnClickListener {
            val name = binding.name.text.toString()
            val nickname = binding.nickname.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val phone = binding.phone.text.toString()

            uiSetEnable(false)
            profile = UserProfile(name, nickname, email, password, phone)
            AccountMethod.createUser(profile, ::onSuccess, ::onFailed)
        }
    }

    private fun onSuccess()
    {
        val failedFunc = { _: String ->
            finish()
        }
        AccountMethod.login(profile.email, profile.password!!, ::finish, failedFunc)
    }

    private fun onFailed(msg: String)
    {
        uiSetEnable(true)
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    private fun uiSetEnable(enable: Boolean)
    {
        binding.name.isEnabled = enable
        binding.nickname.isEnabled = enable
        binding.email.isEnabled = enable
        binding.password.isEnabled = enable
        binding.phone.isEnabled = enable
        binding.createAccount.isEnabled = enable
    }
}