package edu.sns.memorystack

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView.ScaleType
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.databinding.ActivityProfileEditBinding
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.StorageMethod
import kotlinx.coroutines.*
import java.util.regex.Pattern

class EditProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProfileEditBinding.inflate(layoutInflater)
    }

    private val repo = DataRepository.getInstance()

    private var loading = false
    private var nicknameFlag = false
    private var phoneFlag = false
    private var profileFlag = false

    private var profile: UserProfile? = null

    private var profileImage: Bitmap? = null

    private var imageId: Long? = null

    private val requestImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode != RESULT_OK)
            return@registerForActivityResult
        it.data?.getLongExtra(GalleryActivity.KEY_IMG, -1L)?.let { id ->
            if(id == -1L)
                return@registerForActivityResult
            imageId = id

            binding.profileImage.setImageBitmap(StorageMethod.getImageFromMediaStore(this, id))

            profileFlag = true
            binding.saveProfile.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser == null)
            restart()
        else
            init(currentUser.uid)
    }

    override fun onResume()
    {
        super.onResume()

        val auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser == null)
            restart()
    }

    private fun init(uid: String)
    {
        val pattern = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")

        CoroutineScope(Dispatchers.IO).launch {
            profile = repo.getUserProfile(uid)
            profile?.imgPath?.let {
                profileImage = repo.getImage(it)
                profileImage?.let { bitmap ->
                    withContext(Dispatchers.Main) {
                        binding.profileImage.setImageBitmap(bitmap)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                binding.editNickname.editText?.setText(profile?.nickname)
                binding.editPhone.editText?.setText(profile?.phone)
                if(profileImage != null)
                    binding.profileImage.setImageBitmap(profileImage)
                loading = true
            }
        }

        var job: Job? = null

        binding.editNickname.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                nicknameFlag = false
                s.toString().let {
                    job?.cancel()

                    if(it != profile?.nickname) {
                        if(it.isNotBlank()) {
                            job = CoroutineScope(Dispatchers.IO).launch {
                                val result = AccountMethod.isProfileDataOverlap(UserProfile.KEY_NICKNAME, it)
                                withContext(Dispatchers.Main) {
                                    binding.editNickname.isErrorEnabled = result
                                    binding.editNickname.error = if(result) "${it} is already valid" else null

                                    nicknameFlag = !result
                                    binding.saveProfile.isEnabled = nicknameFlag && phoneFlag && loading
                                }
                            }
                        }
                        else {
                            binding.editNickname.isErrorEnabled = true
                            binding.editNickname.error = "Nickname can't be empty"
                        }
                    }
                    else {
                        binding.editNickname.isErrorEnabled = false
                        nicknameFlag = true
                    }
                }

                binding.saveProfile.isEnabled = nicknameFlag && phoneFlag && loading
            }
        })

        binding.editPhone.editText?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?)
            {
                phoneFlag = false
                s.toString().let {
                    job?.cancel()

                    if(it != profile?.phone) {
                        val match = pattern.matcher(s.toString())

                        if(match.matches()) {
                            job = CoroutineScope(Dispatchers.IO).launch {
                                val result = AccountMethod.isProfileDataOverlap(UserProfile.KEY_PHONE, it)
                                withContext(Dispatchers.Main) {
                                    binding.editPhone.isErrorEnabled = result
                                    binding.editPhone.error = if(result) "${it} is already valid" else null

                                    phoneFlag = !result
                                    binding.saveProfile.isEnabled = nicknameFlag && phoneFlag && loading
                                }
                            }
                        }
                        else {
                            binding.editPhone.isErrorEnabled = true
                            binding.editPhone.error = "Incorrect phone number"
                        }
                    }
                    else {
                        binding.editPhone.isErrorEnabled = false
                        phoneFlag = true
                    }

                    binding.saveProfile.isEnabled =  nicknameFlag && phoneFlag && loading
                }
            }
        })

        binding.profileImage.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            requestImageLauncher.launch(intent)
        }

        binding.saveProfile.setOnClickListener {
            val nickname = binding.editNickname.editText?.text.toString()
            val phone = binding.editPhone.editText?.text.toString()

            setEnable(false)

            CoroutineScope(Dispatchers.IO).launch {
                AccountMethod.updateUserProfile(uid, UserProfile("", nickname, "", "", phone))
                imageId?.let {
                    AccountMethod.updateUserProfileImage(uid, it)
                }

                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }

        binding.logOut.setOnClickListener {
            Firebase.auth.signOut()

            restart()
        }
    }

    private fun restart()
    {
        val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
        intent!!.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
            finishAffinity()
        }
    }

    private fun setEnable(enable: Boolean)
    {
        binding.editNickname.isEnabled = enable
        binding.editPhone.isEnabled = enable
        binding.saveProfile.isEnabled = enable
    }
}