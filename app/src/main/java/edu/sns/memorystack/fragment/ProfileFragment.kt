package edu.sns.memorystack.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import edu.sns.memorystack.EditProfileActivity
import edu.sns.memorystack.LoginActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.method.FollowMethod
import edu.sns.memorystack.method.PostMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment: Fragment(), SwipeRefreshLayout.OnRefreshListener
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.profile_page, container, false)
    }

    private val repo = DataRepository.getInstance()

    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var userImage: CircleImageView
    private lateinit var nickname: TextView
    private lateinit var postCount: TextView
    private lateinit var followerCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val auth = Firebase.auth
        val currentUser = auth.currentUser ?: return

        refreshLayout = view.findViewById(R.id.refreshLayout)
        userImage = view.findViewById<CircleImageView>(R.id.profile_image)
        nickname = view.findViewById<TextView>(R.id.nickname)
        postCount = view.findViewById<TextView>(R.id.post_count)
        followerCount = view.findViewById<TextView>(R.id.follower_count)

        refreshLayout.setOnRefreshListener(this)

        val edit = view.findViewById<Button>(R.id.follow_btn)

        edit.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        init(currentUser.uid)
    }

    override fun onRefresh()
    {
        val auth = Firebase.auth
        val currentUser = auth.currentUser ?: return

        init(currentUser.uid)

        refreshLayout.isRefreshing = false
    }

    private fun init(uid: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val profile = repo.getUserProfile(uid)
            val posts = PostMethod.getPostsByUid(listOf(uid))
            val followers = FollowMethod.getFollowerList(uid)

            withContext(Dispatchers.Main) {
                nickname.text = profile?.nickname
                postCount.text = posts.size.toString()
                followerCount.text = followers.size.toString()
            }

            profile?.imgPath?.let {
                repo.getImage(it)?.let { bitmap ->
                    withContext(Dispatchers.Main) {
                        userImage.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
}