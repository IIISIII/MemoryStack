package edu.sns.memorystack.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.PostListAdapter
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.data.PostData
import edu.sns.memorystack.method.FollowMethod
import kotlinx.coroutines.*

class PostListFragment: Fragment(), OnRefreshListener
{
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var postList: RecyclerView
    private lateinit var listAdapter: PostListAdapter
    private lateinit var model: PostViewModel

    private var job: Job? = null

    private var userList = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.post_list_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val auth = Firebase.auth
        val currentUser = auth.currentUser ?: return

        model = ViewModelProvider(this)[PostViewModel::class.java]
        model.reset()

        refreshLayout = view.findViewById(R.id.refresh_layout)
        refreshLayout.setOnRefreshListener(this)

        postList = view.findViewById(R.id.post_list)

        val listManager = LinearLayoutManager(view.context)
        listAdapter = PostListAdapter()

        postList.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            addItemDecoration(PostListItemDecoration(view.context.resources.getDimensionPixelSize(R.dimen.post_list_item_decoration_height)))

            layoutManager = listManager
            adapter = listAdapter

            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                    val itemTotalCount = recyclerView.adapter!!.itemCount - 1

                    if (!postList.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                        if(model.isAllowedLoading())
                            model.loadPage(userList)
                        listAdapter.removeLoading()
                    }
                }
            })
        }

        model.getAll().observe(viewLifecycleOwner) {
            listAdapter.addItemList(it)
            refreshLayout.isRefreshing = false
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            userList = FollowMethod.getFollowingList(currentUser.uid)
            userList.add(currentUser.uid)

            withContext(Dispatchers.Main) {
                model.loadPage(userList)
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

        model.reset()

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            userList = FollowMethod.getFollowingList(currentUser.uid)
            userList.add(currentUser.uid)

            withContext(Dispatchers.Main) {
                listAdapter.clear()
                model.loadPage(userList)
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

    class PostViewModel: ViewModel()
    {
        private val postList = MutableLiveData<ArrayList<PostData>>()

        private val repo = DataRepository.getInstance()

        private var flag = false

        fun getAll(): MutableLiveData<ArrayList<PostData>>
        {
            return postList
        }

        fun isAllowedLoading(): Boolean
        {
            return !flag
        }

        fun reset()
        {
            repo.resetLastVisiblePost()
            flag = false
        }

        fun loadPage(userList: ArrayList<String>)
        {
            if(!flag) {
                flag = true
                CoroutineScope(Dispatchers.IO).launch {
                    val posts = repo.getPostsByIdLimit(userList, 5)
                    withContext(Dispatchers.Main) {
                        if(posts.size > 0) {
                            flag = false
                            postList.value = posts
                        }
                    }
                }
            }
        }
    }
}