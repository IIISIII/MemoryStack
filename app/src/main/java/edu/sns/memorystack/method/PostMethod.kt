package edu.sns.memorystack.method

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.model.Document
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.sns.memorystack.data.PostData
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class PostMethod
{
    companion object
    {
        suspend fun post(uid: String, fileId: Long?, msg: String): Boolean
        {
            val db = Firebase.firestore
            val userPosts = db.collection("posts")

            val upload = StorageMethod.uploadFile(uid, fileId, "images/${uid}/${System.currentTimeMillis()}") ?: return false

            userPosts
                .document()
                .set(hashMapOf(
                    PostData.KEY_UID to uid,
                    PostData.KEY_TEXT to msg,
                    PostData.KEY_IMG to upload,
                    PostData.KEY_DATE to FieldValue.serverTimestamp()
                ))
                .await()

            return true
        }

        suspend fun getPostsByUid(uids: List<String>): ArrayList<PostData>
        {
            val subLists = ArrayList<List<String>>()
            var index = 0;
            while(index < uids.size) {
                if(index + 10 < uids.size)
                    subLists.add(uids.subList(index, index + 10))
                else
                    subLists.add(uids.subList(index, uids.size))
                index += 10
            }

            val list = ArrayList<PostData>()

            val db = Firebase.firestore

            val userPosts = db.collection("posts")

            val orderBy = userPosts.orderBy(PostData.KEY_DATE, Query.Direction.DESCENDING)

            val tasks = ArrayList<Task<QuerySnapshot>>()
            for(userlist in subLists) {
                val result = orderBy
                    .whereIn(PostData.KEY_UID, userlist)
                    .get()
                tasks.add(result)
            }
            for(task in tasks) {
                val result = task.await()
                for(post in result.documents) {
                    val user = post.get(PostData.KEY_UID)?.toString() ?: continue
                    val text = post.get(PostData.KEY_TEXT)?.toString() ?: continue
                    val imgPath = post.get(PostData.KEY_IMG)?.toString() ?: continue
                    val date = post.getTimestamp(PostData.KEY_DATE) ?: continue
                    list.add(PostData(user, imgPath, text, date))
                }
            }
            list.sortByDescending { it.date }

            return list
        }

        data class PostCapsule(val list: ArrayList<PostData>, val last: DocumentSnapshot?)

        suspend fun getPostsByIdLimit(uids: List<String>, limit: Int, document: DocumentSnapshot?): PostCapsule
        {
            val list = ArrayList<PostData>()
            val documentList = ArrayList<DocumentSnapshot>()
            var last: DocumentSnapshot? = null

            val subLists = ArrayList<List<String>>()
            var index = 0;
            while(index < uids.size) {
                if(index + 10 < uids.size)
                    subLists.add(uids.subList(index, index + 10))
                else
                    subLists.add(uids.subList(index, uids.size))
                index += 10
            }

            val db = Firebase.firestore
            val userPosts = db.collection("posts").orderBy(PostData.KEY_DATE, Query.Direction.DESCENDING)
            val query = if(document == null)
                userPosts
            else
                userPosts.startAfter(document)

            val tasks = ArrayList<Task<QuerySnapshot>>()
            for(userlist in subLists) {
                val task = query
                    .whereIn(PostData.KEY_UID, userlist)
                    .limit(limit.toLong())
                    .get()
                tasks.add(task)
            }
            for(task in tasks) {
                val result = task.await()
                documentList.addAll(result.documents)
            }
            documentList.sortByDescending { it.getTimestamp(PostData.KEY_DATE) }

            val size = if(documentList.size > limit) limit else documentList.size

            if(size > 0) {
                val limitedList = documentList.subList(0, size);
                last = documentList[size - 1]
                for(post in limitedList) {
                    val user = post.get(PostData.KEY_UID)?.toString() ?: continue
                    val text = post.get(PostData.KEY_TEXT)?.toString() ?: continue
                    val imgPath = post.get(PostData.KEY_IMG)?.toString() ?: continue
                    val date = post.getTimestamp(PostData.KEY_DATE) ?: continue
                    list.add(PostData(user, imgPath, text, date))
                }
            }

            return PostCapsule(list, last)
        }
    }
}