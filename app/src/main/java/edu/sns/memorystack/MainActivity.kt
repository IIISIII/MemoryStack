package edu.sns.memorystack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import edu.sns.memorystack.databinding.ActivityMainBinding
import edu.sns.memorystack.fragment.*

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener
{
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
        //logo표시
        supportActionBar?.setIcon(R.drawable.stack_logo)
        getSupportActionBar()?.setDisplayUseLogoEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        auth = Firebase.auth
        currentUser = auth.currentUser
        if(currentUser == null)
            moveToLoginActivity()
        else
            init()
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
            return 5
        }

        override fun createFragment(position: Int): Fragment
        {
            return when(position) {
                0 -> PostListFragment()
                1 -> FollowListFragment()
                2 -> PostFragment()
                3 -> DirectMessageFragment()
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
            R.id.dm -> {
                binding.container.currentItem = 3
                return true
            }
            R.id.profile -> {
                binding.container.currentItem = 4
                return true
            }
            else -> {
                return false
            }
        }
    }
}