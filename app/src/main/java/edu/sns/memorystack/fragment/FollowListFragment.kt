package edu.sns.memorystack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.FollowListAdapter
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowListFragment: Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<String?>
    private lateinit var followListAdapter: FollowListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.follow_list_page, container, false)
    }

    private lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var uid: String

    private var isRefreshing = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        uid = Firebase.auth.currentUser?.uid ?: return

        refreshLayout = view.findViewById(R.id.refresh_layout)

        recyclerView = view.findViewById(R.id.follow_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()

        followListAdapter = FollowListAdapter(userArrayList, uid)

        recyclerView.adapter = followListAdapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager(activity).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        CoroutineScope(Dispatchers.IO).launch {
            val uids = AccountMethod.getAllUid(uid)
            withContext(Dispatchers.Main) {
                followListAdapter.addItemList(uids)
            }
        }

        refreshLayout.setOnRefreshListener(this)
    }

    override fun onRefresh()
    {
        if(!isRefreshing) {
            isRefreshing = true
            CoroutineScope(Dispatchers.IO).launch {
                val uids = AccountMethod.getAllUid(uid)
                withContext(Dispatchers.Main) {
                    followListAdapter.clear()
                    followListAdapter.addItemList(uids)
                    refreshLayout.isRefreshing = false
                    isRefreshing = false
                }
            }
        }
    }
}