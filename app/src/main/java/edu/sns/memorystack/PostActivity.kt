package edu.sns.memorystack

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.databinding.ActivityPostBinding
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.FollowMethod
import edu.sns.memorystack.method.PostMethod
import edu.sns.memorystack.network.FirebaseViewModel
import edu.sns.memorystack.network.NotificationBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostActivity : AppCompatActivity()
{
    companion object {
        const val IMG_KEY = "imageId"
    }

    private val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private val repo = DataRepository.getInstance()

    private var sendFlag = false
    private var sendCount = 0;

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.label_post)

        firebaseViewModel.myResponse.observe(this) {
            if(sendFlag) {
                if(--sendCount == 0)
                    finish()
            }
        }

        auth = Firebase.auth
        currentUser = auth.currentUser

        if(currentUser == null) {
            finish()
            return
        }

        init()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init()
    {
        intent?.let {
            val id = it.getLongExtra(IMG_KEY, -1L)

            CoroutineScope(Dispatchers.IO).launch {
                getImageFromId(id)?.let { bitmap ->
                    withContext(Dispatchers.Main) {
                        binding.imageLoading.visibility = View.GONE
                        binding.postImage.setImageBitmap(bitmap)
                        binding.postImage.visibility = View.VISIBLE
                    }
                }
            }

            binding.post.setOnClickListener {
                binding.post.isEnabled = false
                binding.postText.isEnabled = false
                val postText = binding.postText.editText?.text.toString() ?: ""

                if(postText.isBlank()) {
                    binding.post.isEnabled = true
                    binding.postText.isEnabled = true
                    return@setOnClickListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val uid = currentUser!!.uid
                    val result = PostMethod.post(uid, id, postText)

                    val profile = repo.getUserProfile(uid, true)

                    sendFlag = true
                    sendPushMessage(uid, profile!!.name, "", postText)
                }
            }
        } ?: finish()
    }

    private fun getImageFromId(id: Long?): Bitmap?
    {
        id?.let {
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
            try {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    return ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                return MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } catch (err: Exception) {}
        }
        return null
    }

    private suspend fun sendPushMessage(userId: String, userName: String, postId: String, postText: String)
    {
        val flist = FollowMethod.getFollowerList(userId)
        val tokens = AccountMethod.getTokenById(flist, userId)
        sendCount = tokens.size

        for(token in tokens) {
            val data = NotificationBody.NotificationData(userId, userName, postId, postText)
            val body = NotificationBody(token, data)

            firebaseViewModel.sendNotification(body)
        }
    }
}