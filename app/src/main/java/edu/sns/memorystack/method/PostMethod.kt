package edu.sns.memorystack.method

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.sns.memorystack.data.PostData
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
                    PostData.KEY_DATE to Timestamp(Date())
                ))
                .await()

            return true
        }

        suspend fun getPostsByUid(uids: List<String>): ArrayList<PostData>
        {
            val list = ArrayList<PostData>()

            val db = Firebase.firestore

            val userPosts = db.collection("posts")

            val result = userPosts
                .whereIn(PostData.KEY_UID, uids)
                .orderBy(PostData.KEY_DATE, Query.Direction.DESCENDING)
                .get()
                .await() ?: return list

            for(post in result.documents) {
                val user = post.get(PostData.KEY_UID)?.toString() ?: continue
                val text = post.get(PostData.KEY_TEXT)?.toString() ?: continue
                val imgPath = post.get(PostData.KEY_IMG)?.toString() ?: continue
                val date = post.getTimestamp(PostData.KEY_DATE) ?: continue
                list.add(PostData(user, imgPath, text, date))
            }

            return list
        }
    }
}