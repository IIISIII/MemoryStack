package edu.sns.memorystack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.PostListAdapter
import edu.sns.memorystack.method.PostMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostListFragment: Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.post_list_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val auth = Firebase.auth
        val currentUser = auth.currentUser ?: return

        val list = view.findViewById<RecyclerView>(R.id.post_list)

        val listManager = LinearLayoutManager(view.context)
        val listAdapter = PostListAdapter()

        list.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(20)

            layoutManager = listManager
            adapter = listAdapter
        }

        CoroutineScope(Dispatchers.IO).launch {
            val posts = PostMethod.getPostsByUid(listOf(currentUser.uid))
            withContext(Dispatchers.Main) {
                listAdapter.setItemList(posts)
            }
        }
    }
}