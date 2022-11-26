package edu.sns.memorystack.method

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
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

            val following = me.collection("following")
            val follower = otherD.collection("follower")

            val time = FieldValue.serverTimestamp()
            following.document(other).set(mapOf("date" to time)).await()
            follower.document(uid).set(mapOf("date" to time)).await()
        }

        suspend fun unfollow(uid: String, other: String)
        {
            val db = Firebase.firestore
            val follow = db.collection("follow")

            val me = follow.document(uid)
            val otherD = follow.document(other)

            val following = me.collection("following")
            val follower = otherD.collection("follower")

            following.document(other).delete().await()
            follower.document(uid).delete().await()
        }

        suspend fun isFollowing(uid: String, other: String): Boolean
        {
            try {
                val db = Firebase.firestore
                val follow = db.collection("follow")

                val me = follow.document(uid)

                val following = me.collection("following")

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

                val following = me.collection("following")
                    .orderBy("date", Query.Direction.DESCENDING)

                val followList = following.get().await()

                for(f in followList)
                    list.add(f.id)
            } catch (err: Exception) {}
            return list
        }

        suspend fun getFollowerList(uid: String): ArrayList<String>
        {
            val list = ArrayList<String>()
            try {
                val db = Firebase.firestore
                val follow = db.collection("follow")

                val me = follow.document(uid)

                val follower = me.collection("follower")
                    .orderBy("date", Query.Direction.DESCENDING)

                val followList = follower.get().await()

                for(f in followList)
                    list.add(f.id)
            } catch (err: Exception) {}
            return list
        }
    }
}