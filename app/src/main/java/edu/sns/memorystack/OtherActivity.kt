package edu.sns.memorystack

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.databinding.ActivityOtherBinding
import edu.sns.memorystack.method.FollowMethod
import edu.sns.memorystack.method.PostMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtherActivity : AppCompatActivity() {

    companion object {
        const val UID = "uid"
    }

    private val binding by lazy {
       ActivityOtherBinding.inflate(layoutInflater)
    }

    private val repo = DataRepository.getInstance()

    private var other: String? = null
    private var currentUid: String? = null

    private var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.label_follow)

        val currentUser = Firebase.auth.currentUser
        currentUid = currentUser?.uid

        currentUid ?: restart()

        intent?.let {
            other = it.getStringExtra(UID)
            if(other == null) {
                finish()
                return
            }
            init(other!!)
        } ?: finish()
    }

    private fun init(uid: String)
    {
        flag = true

        if(currentUid == uid)
            binding.followBtn.setText(R.string.text_edit_profile)

        binding.followBtn.setOnClickListener {
            if(currentUid != other) {
                if (!flag) {
                    flag = true
                    binding.followBtn.text = "..."
                    CoroutineScope(Dispatchers.IO).launch {
                        if (FollowMethod.isFollowing(currentUid!!, uid))
                            FollowMethod.unfollow(currentUid!!, uid)
                        else
                            FollowMethod.follow(currentUid!!, uid)

                        val result = FollowMethod.isFollowing(currentUid!!, uid);
                        withContext(Dispatchers.Main) {
                            binding.followBtn.setText(if (result) R.string.text_unfollow_btn else R.string.text_follow_btn)
                            flag = false
                        }
                    }
                }
            }
            else {
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val profile = repo.getUserProfile(uid, false)
            profile?.let { profile ->
                val posts = PostMethod.getPostsByUid(listOf(uid))
                val followers = FollowMethod.getFollowerList(uid)

                withContext(Dispatchers.Main) {
                    binding.nickname.text = profile.nickname
                    binding.postCount.text = posts.size.toString()
                    binding.followerCount.text = followers.size.toString()

                    flag = false
                }

                if(currentUid != other) {
                    val isFollowing = FollowMethod.isFollowing(currentUid!!, uid)
                    withContext(Dispatchers.Main) {
                        binding.followBtn.setText(if(isFollowing) R.string.text_unfollow_btn else R.string.text_follow_btn)
                    }
                }

                profile.imgPath?.let {
                    repo.getImage(it)?.let { bitmap ->
                        withContext(Dispatchers.Main) {
                            binding.profileImage.setImageBitmap(bitmap)
                        }
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
}