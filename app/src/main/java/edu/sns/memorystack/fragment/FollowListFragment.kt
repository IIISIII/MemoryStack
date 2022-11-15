package edu.sns.memorystack.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import edu.sns.memorystack.R
import edu.sns.memorystack.adapter.FollowListAdapter
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.data.User

class FollowListFragment: Fragment()
{
    //사용할 변수 미리 선언
    private lateinit var recyclerView: RecyclerView
    private lateinit var userArrayList : ArrayList<UserProfile>
    private lateinit var followListAdapter: FollowListAdapter
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.follow_list_page, container, false)
    }
    //
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        //데이터 보여주기
        recyclerView = view.findViewById(R.id.follow_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()

        followListAdapter = FollowListAdapter(userArrayList)

        recyclerView.adapter = followListAdapter

        //변경시 값 가져오기
        EventChangeListener()
        //button

    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("users")//.orderBy("email", Query.Direction.ASCENDING) //오름차순 정렬
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) { //에러처리
                    if(error != null){
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    //error가 아닌경우
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){ //문서가 추가되면
                            userArrayList.add(dc.document.toObject(UserProfile::class.java))

                        }
                    }
                    //변경사항 확인
                    followListAdapter.notifyDataSetChanged()
                }
            })
    }

}
