package edu.sns.memorystack

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.databinding.ActivityPostBinding
import edu.sns.memorystack.method.PostMethod
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        currentUser = auth.currentUser

        if(currentUser == null) {
            finish()
            return
        }

        init()
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
                    val result = PostMethod.post(currentUser!!.uid, id, postText)
                    withContext(Dispatchers.Main) {
                        if(result)
                            finish()
                        else {
                            binding.post.isEnabled = true
                            binding.postText.isEnabled = true
                        }
                    }
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
}