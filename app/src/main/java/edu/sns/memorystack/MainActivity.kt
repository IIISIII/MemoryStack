package edu.sns.memorystack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import edu.sns.memorystack.databinding.ActivityMainBinding
import edu.sns.memorystack.fragment.*
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener
{
    companion object {
        const val KEY_REFRESH = "refresh"
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private var init = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setIcon(R.drawable.stack_logo)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = Firebase.auth
        currentUser = auth.currentUser
        if(currentUser == null)
            moveToLoginActivity()
        else
            init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    override fun onResume()
    {
        super.onResume()

        if(currentUser == null)
            currentUser = auth.currentUser

        currentUser?.let {
            if(!init)
                init()
        }
    }

    private fun init()
    {
        binding.container.adapter = ViewPagerAdapter(this)
        binding.container.registerOnPageChangeCallback(
            object: ViewPager2.OnPageChangeCallback()
            {
                override fun onPageSelected(position: Int)
                {
                    super.onPageSelected(position)
                    binding.navigation.menu.getItem(position).isChecked = true
                }
            }
        )
        binding.navigation.setOnItemSelectedListener(this)

        init = true

        FirebaseMessaging.getInstance().token.addOnCompleteListener { // it: Task<String!>
            val token = if (it.isSuccessful) it.result else null
            token?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    AccountMethod.registerToken(token)
                }
            }
        }
    }

    private fun moveToLoginActivity()
    {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    class ViewPagerAdapter(val fragment: FragmentActivity): FragmentStateAdapter(fragment)
    {
        override fun getItemCount(): Int
        {
            return 4
        }

        override fun createFragment(position: Int): Fragment
        {
            return when(position) {
                0 -> PostListFragment()
                1 -> FollowListFragment()
                2 -> PostFragment()
                else -> ProfileFragment()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home -> {
                binding.container.currentItem = 0
                return true
            }
            R.id.follow -> {
                binding.container.currentItem = 1
                return true
            }
            R.id.post -> {
                binding.container.currentItem = 2
                return true
            }
            R.id.profile -> {
                binding.container.currentItem = 3
                return true
            }
            else -> {
                return false
            }
        }
    }
}