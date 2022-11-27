package edu.sns.memorystack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.databinding.ActivityPostDataBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostDataActivity : AppCompatActivity()
{
    companion object {
        const val KEY_UID = "uid"
        const val KEY_POST_IMG = "post_img"
        const val KEY_POST_TEXT = "post_text"
        const val KEY_POST_DATE = "post_date"
    }

    private val binding by lazy {
        ActivityPostDataBinding.inflate(layoutInflater)
    }

    private val repo = DataRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.label_post_data)

        val currentUid = Firebase.auth.currentUser?.uid

        intent?.let {
            val uid = it.getStringExtra(KEY_UID).toString()
            val postImg = it.getStringExtra(KEY_POST_IMG)
            val postText = it.getStringExtra(KEY_POST_TEXT).toString()
            val postDate = it.getStringExtra(KEY_POST_DATE).toString()

            binding.date.text = postDate
            binding.postText.text = postText

            CoroutineScope(Dispatchers.IO).launch {
                val profile = repo.getUserProfile(uid, false)
                val image = repo.getImage(postImg)

                withContext(Dispatchers.Main) {
                    binding.userNickname.text = profile?.nickname
                    binding.postImage.setImageBitmap(image)
                    binding.imageLoading.visibility = View.GONE
                    binding.postImage.visibility = View.VISIBLE

                    binding.postImage.setOnClickListener {
                        binding.postImage.scaleType = if(binding.postImage.scaleType == ImageView.ScaleType.CENTER_CROP)
                            ImageView.ScaleType.FIT_CENTER
                        else
                            ImageView.ScaleType.CENTER_CROP
                    }
                }

                val userImg = repo.getImage(profile?.imgPath)

                withContext(Dispatchers.Main) {
                    userImg?.let {
                        binding.userProfileImage.setImageBitmap(userImg)
                    }
                    binding.userProfileImage.setOnClickListener {
                        val intent = Intent(this@PostDataActivity, OtherActivity::class.java)
                        intent.putExtra(OtherActivity.UID, uid)
                        startActivity(intent)
                    }
                }
            }
        }
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
}