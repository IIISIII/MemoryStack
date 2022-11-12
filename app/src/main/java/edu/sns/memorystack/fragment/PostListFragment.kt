package edu.sns.memorystack.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.PostListAdapter
import edu.sns.memorystack.method.PostMethod
import kotlinx.coroutines.*

class PostListFragment: Fragment(), OnRefreshListener
{
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var postList: RecyclerView
    private lateinit var listAdapter: PostListAdapter

    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.post_list_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val auth = Firebase.auth
        val currentUser = auth.currentUser ?: return

        refreshLayout = view.findViewById(R.id.refresh_layout)
        refreshLayout.setOnRefreshListener(this)

        postList = view.findViewById<RecyclerView>(R.id.post_list)

        val listManager = LinearLayoutManager(view.context)
        listAdapter = PostListAdapter()

        postList.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            addItemDecoration(PostListItemDecoration(view.context.resources.getDimensionPixelSize(R.dimen.post_list_item_decoration_height)))

            layoutManager = listManager
            adapter = listAdapter
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            val posts = PostMethod.getPostsByUid(listOf(currentUser.uid))
            withContext(Dispatchers.Main) {
                listAdapter.setItemList(posts)
            }
        }
    }

    override fun onRefresh()
    {
        val auth = Firebase.auth
        val currentUser = auth.currentUser

        if(currentUser == null) {
            refreshLayout.isRefreshing = false
            return
        }

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            val posts = PostMethod.getPostsByUid(listOf(currentUser.uid))
            withContext(Dispatchers.Main) {
                listAdapter.setItemList(posts)
                refreshLayout.isRefreshing = false
            }
        }
    }

    class PostListItemDecoration(val height: Int): RecyclerView.ItemDecoration()
    {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State)
        {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.top = height
        }
    }
}