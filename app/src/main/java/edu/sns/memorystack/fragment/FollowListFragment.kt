package edu.sns.memorystack.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.EditProfileActivity
import edu.sns.memorystack.OtherActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.FollowListAdapter
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.data.User
import edu.sns.memorystack.method.AccountMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowListFragment: Fragment()
{
    //사용할 변수 미리 선언
    private lateinit var recyclerView: RecyclerView
    private lateinit var userArrayList : ArrayList<String>
    private lateinit var followListAdapter: FollowListAdapter
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.follow_list_page, container, false)
    }
    //
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        val currentId = Firebase.auth.currentUser?.uid

        //데이터 보여주기
        recyclerView = view.findViewById(R.id.follow_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()

        followListAdapter = FollowListAdapter(userArrayList, currentId!!)

        recyclerView.adapter = followListAdapter
        //밑줄 그어 구분 짓기
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager(activity).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        CoroutineScope(Dispatchers.IO).launch {
            val uids = AccountMethod.getAllUid(currentId)
            withContext(Dispatchers.Main) {
                followListAdapter.setUserList(uids)
            }
        }

        //변경시 값 가져오기
        //EventChangeListener()
    }

//    private fun EventChangeListener() {
//        db = FirebaseFirestore.getInstance()
//        db.collection("users").orderBy("nickname", Query.Direction.ASCENDING) //오름차순 정렬
//            .addSnapshotListener(object : EventListener<QuerySnapshot> {
//                override fun onEvent(
//                    value: QuerySnapshot?,
//                    error: FirebaseFirestoreException?
//                ) { //에러처리
//                    if(error != null){
//                        Log.e("Firestore Error", error.message.toString())
//                        return
//                    }
//                    //error가 아닌경우
//                    for(dc : DocumentChange in value?.documentChanges!!){
//                        if(dc.type == DocumentChange.Type.ADDED){ //문서가 추가되면
//                            //userArrayList.add(dc.document.toObject(UserProfile::class.java))
//                            userArrayList.add(dc.document.id)
//                        }
//                    }
//                    //변경사항 확인
//                    followListAdapter.notifyDataSetChanged()
//                }
//            })
//    }

}
