package edu.sns.memorystack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.databinding.ActivityCreateAccountBinding
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.*
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityCreateAccountBinding.inflate(layoutInflater)
    }

    private lateinit var scene1: Scene
    private lateinit var scene2: Scene

    private lateinit var profile: UserProfile

    private lateinit var viewModel: SceneViewModel

    private var nameFlag = false
    private var nicknameFlag = false
    private var phoneFlag = false

    private var emailFlag = false
    private var passwordFlag = false

    private var createFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.label_signin)

        viewModel = ViewModelProvider(this)[SceneViewModel::class.java]

        scene1 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.create_account_scene1, this)
        scene2 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.create_account_scene2, this)

        sceneFirstInit()

        scene1.setEnterAction {
            sceneFirstInit()
        }
        scene2.setEnterAction {
            sceneSecondInit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed()
    {
        if(createFlag)
            return
        if(viewModel.sceneFlag) {
            goToScene1()
            return
        }
        super.onBackPressed()
    }

    private fun sceneFirstInit()
    {
        viewModel.email = null
        viewModel.password = null
        viewModel.passwordMatch = null

        val nextBtn = binding.sceneRoot.findViewById<Button>(R.id.next_button)

        val nameText = binding.sceneRoot.findViewById<TextInputLayout>(R.id.username_text)
        val nicknameText = binding.sceneRoot.findViewById<TextInputLayout>(R.id.nickname_text)
        val phoneText = binding.sceneRoot.findViewById<TextInputLayout>(R.id.phone_text)

        val pattern = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")

        var job: Job? = null

        nextBtn.isEnabled = nameFlag && nicknameFlag && phoneFlag

        nameText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                if(viewModel.sceneFlag)
                    return

                val str = s.toString()
                viewModel.username = str
                nameFlag = str.isNotBlank()

                nameFlag.let{
                    nameText.isErrorEnabled = !it
                    nameText.error = if(!it) getString(R.string.error_name_blank) else null

                    nextBtn.isEnabled = nameFlag && nicknameFlag && it
                }
            }
        })

        nicknameText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                job?.cancel()

                if(viewModel.sceneFlag)
                    return

                nicknameFlag = false
                nextBtn.isEnabled = nameFlag && nicknameFlag && phoneFlag

                s.toString().let {
                    viewModel.nickname = it

                    if(it.isNotBlank()) {
                        job = CoroutineScope(Dispatchers.IO).launch {
                            val result = AccountMethod.isProfileDataOverlap(UserProfile.KEY_NICKNAME, it)
                            withContext(Dispatchers.Main) {
                                nicknameText.isErrorEnabled = result
                                nicknameText.error = if(result) getString(R.string.error_nickname_conflict) else null

                                nicknameFlag = !result
                                nextBtn.isEnabled = nameFlag && nicknameFlag && phoneFlag
                            }
                        }
                    }
                    else {
                        nicknameText.isErrorEnabled = true
                        nicknameText.error = getString(R.string.error_nickname_blank)
                    }
                }
            }
        })

        phoneText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                job?.cancel()

                if(viewModel.sceneFlag)
                    return

                phoneFlag = false
                s.toString().let {
                    viewModel.phone = it

                    nextBtn.isEnabled = nameFlag && nicknameFlag && phoneFlag

                    if(it.isNotBlank()) {
                        val match = pattern.matcher(s.toString())

                        phoneText.isErrorEnabled = !match.matches()
                        phoneText.error = if (phoneText.isErrorEnabled) getString(R.string.error_phone_format) else null

                        if (!phoneText.isErrorEnabled) {
                            job = CoroutineScope(Dispatchers.IO).launch {
                                val result = AccountMethod.isProfileDataOverlap(UserProfile.KEY_PHONE, it)
                                withContext(Dispatchers.Main) {
                                    if (result) {
                                        phoneText.isErrorEnabled = true
                                        phoneText.error = getString(R.string.error_phone_conflict)
                                    } else {
                                        phoneText.isErrorEnabled = false
                                        phoneText.error = null
                                    }
                                    phoneFlag = !result
                                    nextBtn.isEnabled = nameFlag && nicknameFlag && phoneFlag
                                }
                            }
                        }
                    }
                    else {
                        phoneText.isErrorEnabled = true
                        phoneText.error = getString(R.string.error_phone_blank)
                    }
                }
            }
        })

        viewModel.username?.let {
            nameText.editText?.setText(it)
        }
        viewModel.nickname?.let {
            nicknameText.editText?.setText(it)
        }
        viewModel.phone?.let {
            phoneText.editText?.setText(it)
        }

        nextBtn.setOnClickListener {
            goToScene2()
        }

        viewModel.sceneFlag = false
    }

    private fun sceneSecondInit()
    {
        val backBtn = binding.sceneRoot.findViewById<Button>(R.id.back_button)
        val createBtn = binding.sceneRoot.findViewById<Button>(R.id.create_account_button)

        val emailText = binding.sceneRoot.findViewById<TextInputLayout>(R.id.email_text)
        val passwordText = binding.sceneRoot.findViewById<TextInputLayout>(R.id.password_text)
        val passwordMatchText = binding.sceneRoot.findViewById<TextInputLayout>(R.id.password_match_text)

        val pattern = android.util.Patterns.EMAIL_ADDRESS

        var job: Job? = null

        emailText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                job?.cancel()

                if(!viewModel.sceneFlag)
                    return

                s.toString().let {
                    viewModel.email = it

                    emailFlag = false
                    createBtn.isEnabled = emailFlag && passwordFlag

                    if(it.isNotBlank()) {
                        val match = pattern.matcher(it)

                        emailText.isErrorEnabled = !match.matches()
                        emailText.error = if(emailText.isErrorEnabled) getString(R.string.error_email_format) else null

                        if(!emailText.isErrorEnabled) {
                            job = CoroutineScope(Dispatchers.IO).launch {
                                val result = AccountMethod.isProfileDataOverlap(UserProfile.KEY_EMAIL, it)
                                withContext(Dispatchers.Main) {
                                    if(result) {
                                        emailText.isErrorEnabled = true
                                        emailText.error = getString(R.string.error_email_conflict)
                                    }
                                    else {
                                        emailText.isErrorEnabled = false
                                        emailText.error = null
                                    }
                                    emailFlag = !result
                                    createBtn.isEnabled = emailFlag && passwordFlag
                                }
                            }
                        }
                    }
                    else {
                        emailText.isErrorEnabled = true
                        emailText.error = getString(R.string.error_email_blank)
                    }
                }
            }
        })

        passwordText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!viewModel.sceneFlag)
                    return

                s.toString().let {
                    viewModel.password = it
                    passwordFlag = it == passwordMatchText.editText?.text.toString() && it.isNotBlank()
                    passwordMatchText.isErrorEnabled = !passwordFlag

                    if(it.isNotBlank()) {
                        if(passwordMatchText.editText?.text.toString().isNotBlank())
                            passwordMatchText.error = if (!passwordFlag) getString(R.string.error_password_match) else null
                        passwordText.isErrorEnabled = false
                        passwordText.error = null
                    }
                    else {
                        passwordText.isErrorEnabled = true
                        passwordText.error = getString(R.string.error_password_blank)
                    }
                }
                createBtn.isEnabled = emailFlag && passwordFlag
            }
        })

        passwordMatchText.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                if(!viewModel.sceneFlag)
                    return

                s.toString().let {
                    viewModel.passwordMatch = it
                    passwordFlag = it == passwordText.editText?.text.toString() && it.isNotBlank()
                    passwordMatchText.isErrorEnabled = !passwordFlag

                    if(it.isNotBlank())
                        passwordMatchText.error = if (!passwordFlag) getString(R.string.error_password_match) else null
                    else
                        passwordMatchText.error = getString(R.string.error_password_blank)
                }
                createBtn.isEnabled = emailFlag && passwordFlag
            }
        })

        viewModel.email?.let {
            emailText.editText?.setText(it)
        }
        viewModel.password?.let {
            passwordText.editText?.setText(it)
        }
        viewModel.passwordMatch?.let {
            passwordMatchText.editText?.setText(it)
        }

        createBtn.isEnabled = false

        backBtn.setOnClickListener {
            goToScene1()
        }
        createBtn.setOnClickListener {
            createFlag = true

            emailText.isEnabled = false
            passwordText.isEnabled = false
            passwordMatchText.isEnabled = false
            backBtn.isEnabled = false
            createBtn.isEnabled = false

            val profile = UserProfile(viewModel.username!!, viewModel.nickname!!, viewModel.email!!, viewModel.password, viewModel.phone!!)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (AccountMethod.createUser(profile)) {
                        if (AccountMethod.login(profile.email, profile.password!!)) {
                            withContext(Dispatchers.Main) {
                                finish()
                            }
                        }
                    }
                } catch(err: Exception) {
                    withContext(Dispatchers.Main) {
                        err.message?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    emailText.isEnabled = true
                    passwordText.isEnabled = true
                    passwordMatchText.isEnabled = true
                    backBtn.isEnabled = true
                    createBtn.isEnabled = true
                }
            }
        }

        viewModel.sceneFlag = true
    }

    private fun goToScene1()
    {
        viewModel.email = null
        viewModel.password = null
        viewModel.passwordMatch = null

        TransitionManager.go(scene1, Fade())
    }

    private fun goToScene2()
    {
        TransitionManager.go(scene2, Fade())
    }

    class SceneViewModel: ViewModel()
    {
        var sceneFlag = false

        var username: String? = null
        var nickname: String? = null
        var phone: String? = null
        var email: String? = null
        var password: String? = null
        var passwordMatch: String? = null
    }
}