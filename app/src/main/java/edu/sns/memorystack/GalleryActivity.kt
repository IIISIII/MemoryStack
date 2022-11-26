package edu.sns.memorystack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.sns.memorystack.adapter.GalleryListAdapter
import edu.sns.memorystack.databinding.PostPageBinding
import edu.sns.memorystack.method.StorageMethod

class GalleryActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    companion object {
        const val KEY_IMG = "img"
    }

    private val binding by lazy {
        PostPageBinding.inflate(layoutInflater)
    }

    private val requestPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.text_warning)
                setMessage(R.string.no_post_permission)
            }.show()
        }
        else
            init()
    }

    private val onClick: (Long) -> Unit = {
        val resultIntent = Intent()
        resultIntent.putExtra(KEY_IMG, it)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private val permission = if(Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.label_gallery)

        val requestBtn = binding.requestPermission
        requestBtn.setOnClickListener {
            requestSinglePermission(permission)
        }

        if(checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
            init()
    }

    override fun onRefresh()
    {
        refresh()
        binding.refreshLayout.isRefreshing = false
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
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
            return

        val requestBtn = binding.requestPermission
        requestBtn.visibility = View.GONE

        val refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener(this)

        val imageList = binding.imageList

        refreshLayout.visibility = View.VISIBLE

        val listManager = GridLayoutManager(this, 3)

        StorageMethod.getCursor(this)?.let {
            val listAdapter = GalleryListAdapter(it, onClick)
            imageList.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(20)

                layoutManager = listManager
                adapter = listAdapter
            }
        }
    }

    private fun refresh()
    {
        StorageMethod.getCursor(this)?.let {
            val listAdapter = GalleryListAdapter(it, onClick)
            binding.imageList.adapter = listAdapter
        }
    }

    private fun requestSinglePermission(permission: String)
    {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            init()
            return
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.text_reason)
                setMessage(R.string.req_post_permission_reason)
                setPositiveButton(R.string.text_allow) { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton(R.string.text_deny) { _, _ -> }
            }.show()
        }
        else
            requestPermLauncher.launch(permission)
    }
}