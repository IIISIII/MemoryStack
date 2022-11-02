package edu.sns.memorystack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.sns.memorystack.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}