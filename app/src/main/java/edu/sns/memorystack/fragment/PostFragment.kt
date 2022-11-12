package edu.sns.memorystack.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
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
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.GalleryListAdapter

class PostFragment: Fragment(), OnRefreshListener
{
    private lateinit var fragmentLayout: View
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var imageList: RecyclerView

    private val requestPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        val context = requireContext()
        if (!it) {
            AlertDialog.Builder(context).apply {
                setTitle("Warning")
                setMessage(context.getString(R.string.no_post_permission))
            }.show()
        }
        else
            init(context, fragmentLayout)
    }

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
            requestSinglePermission(context, view, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            init(context, view)
    }

    override fun onRefresh()
    {
        refresh(requireContext())
        refreshLayout.isRefreshing = false
    }

    private fun init(context: Context, layout: View)
    {
        if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return

        val requestBtn = layout.findViewById<Button>(R.id.request_permission)
        requestBtn.visibility = View.GONE

        getCursor(context)?.let {
            refreshLayout = layout.findViewById(R.id.refresh_layout)
            refreshLayout.setOnRefreshListener(this)

            imageList = layout.findViewById(R.id.image_list)

            refreshLayout.visibility = View.VISIBLE

            val listManager = GridLayoutManager(context, 3)
            val listAdapter = GalleryListAdapter(it)

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
        getCursor(context)?.let {
            val listAdapter = GalleryListAdapter(it)
            imageList.adapter = listAdapter
        }
    }

    private fun getCursor(context: Context): Cursor?
    {
        val projection = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN)

        return context.contentResolver.query(GalleryListAdapter.COLLECTION, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC")
    }

    private fun requestSinglePermission(context: Context, layout: View, permission: String)
    {
        if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            init(context, layout)
            return
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(context).apply {
                setTitle("Reason")
                setMessage(context.getString(R.string.req_post_permission_reason))
                setPositiveButton("Allow") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("Deny") { _, _ -> }
            }.show()
        }
        else
            requestPermLauncher.launch(permission)
    }
}