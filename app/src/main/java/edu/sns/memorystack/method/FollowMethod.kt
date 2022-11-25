package edu.sns.memorystack.method

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FollowMethod
{
    companion object
    {
        suspend fun follow(uid: String, other: String)
        {
            val db = Firebase.firestore
            val follow = db.collection("follow")

            val me = follow.document(uid)
            val otherD = follow.document(other)

            val following = me.collection("sender")
            val follower = otherD.collection("receiver")

            following.document(other).set("uid" to other).await()
            follower.document(uid).set("uid" to uid).await()
        }

        suspend fun unfollow(uid: String, other: String)
        {
            val db = Firebase.firestore
            val follow = db.collection("follow")

            val me = follow.document(uid)
            val otherD = follow.document(other)

            val following = me.collection("sender")
            val follower = otherD.collection("receiver")

            following.document(other).delete().await()
            follower.document(uid).delete().await()
        }

        suspend fun isFollowing(uid: String, other: String): Boolean
        {
            try {
                val db = Firebase.firestore
                val follow = db.collection("follow")

                val me = follow.document(uid)

                val following = me.collection("sender")

                val result = following.document(other).get().await()

                return result.exists()
            } catch (err: Exception) {}

            return false
        }

        suspend fun getFollowingList(uid: String): ArrayList<String>
        {
            val list = ArrayList<String>()
            try {
                val db = Firebase.firestore
                val follow = db.collection("follow")

                val me = follow.document(uid)

                val following = me.collection("sender")

                val followList = following.get().await()

                for(f in followList)
                    list.add(f.id)
            } catch (err: Exception) {}
            return list
        }
    }
}