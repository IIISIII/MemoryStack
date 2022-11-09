package edu.sns.memorystack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.databinding.ActivityMainBinding
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.PostMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        binding.post.setOnClickListener {
            uploadDialog()
        }
        binding.getPosts.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val list = PostMethod.getPosts(currentUser!!.uid)

                if(list.size > 0) {
                    val data = list[0]
                    val img = PostMethod.getImage(data.imgPath)
                    withContext(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(img)
                    }
                }

                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, list.size.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun uploadDialog() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null)

            AlertDialog.Builder(this)
                .setTitle("Choose Photo")
                .setCursor(cursor, { _, i ->
                    cursor?.run {
                        moveToPosition(i)
                        val idIdx = getColumnIndex(MediaStore.Images.ImageColumns._ID)
                        val nameIdx = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)

                        CoroutineScope(Dispatchers.IO).launch {
                            PostMethod.post(currentUser!!.uid, getLong(idIdx), "test")
                        }
                    }
                }, MediaStore.Images.ImageColumns.DISPLAY_NAME).create().show()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onResume()
    {
        super.onResume()

        if(currentUser == null)
            currentUser = auth.currentUser

        currentUser?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val profile = AccountMethod.getUserProfile(it.uid)

                withContext(Dispatchers.Main) {
                    binding.test.text = "${profile?.nickname}\n${profile?.email}\n${profile?.name}\n${profile?.phone}" ?: "null"
                }
            }
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