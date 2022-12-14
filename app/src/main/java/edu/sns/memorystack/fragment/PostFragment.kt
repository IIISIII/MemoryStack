package edu.sns.memorystack.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import edu.sns.memorystack.PostActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.GalleryListAdapter
import edu.sns.memorystack.method.StorageMethod

class PostFragment: Fragment(), OnRefreshListener
{
    private lateinit var fragmentLayout: View
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var imageList: RecyclerView

    private val requestPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        val context = requireContext()
        if (!it) {
            AlertDialog.Builder(context, R.style.AlertDialogTheme).apply {
                setTitle(R.string.text_warning)
                setMessage(R.string.no_post_permission)
            }.show()
        }
        else
            init(context, fragmentLayout)
    }

    private val onClick: (Long) -> Unit = {
        val intent = Intent(context, PostActivity::class.java)
        intent.putExtra(PostActivity.IMG_KEY, it)
        startActivity(intent)
    }

    private val permission = if(Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.post_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        fragmentLayout = view

        val context = requireContext()

        val requestBtn = view.findViewById<Button>(R.id.request_permission)
        requestBtn.setOnClickListener {
            requestSinglePermission(context, view, permission)
        }

        if(context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
            init(context, view)
    }

    override fun onRefresh()
    {
        refresh(requireContext())
        refreshLayout.isRefreshing = false
    }

    private fun init(context: Context, layout: View)
    {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
            return

        val requestBtn = layout.findViewById<Button>(R.id.request_permission)
        requestBtn.visibility = View.GONE

        refreshLayout = layout.findViewById(R.id.refresh_layout)
        refreshLayout.setOnRefreshListener(this)

        imageList = layout.findViewById(R.id.image_list)

        refreshLayout.visibility = View.VISIBLE

        val listManager = GridLayoutManager(context, 3)

        StorageMethod.getCursor(context)?.let {
            val listAdapter = GalleryListAdapter(it, onClick)
            imageList.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(20)

                layoutManager = listManager
                adapter = listAdapter
            }
        }
    }

    private fun refresh(context: Context)
    {
        StorageMethod.getCursor(context)?.let {
            val listAdapter = GalleryListAdapter(it, onClick)
            imageList.adapter = listAdapter
        }
    }

    private fun requestSinglePermission(context: Context, layout: View, permission: String)
    {
        if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            init(context, layout)
            return
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(context, R.style.AlertDialogTheme).apply {
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