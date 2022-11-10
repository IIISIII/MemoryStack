package edu.sns.memorystack.method

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.google.firebase.Timestamp
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
            val userPosts = db.collection("posts/${uid}/list")

            val upload = uploadFile(uid, fileId, System.currentTimeMillis().toString()) ?: return false

            userPosts
                .document()
                .set(hashMapOf(
                    "text" to msg,
                    "imgPath" to upload,
                    "date" to Timestamp(Date())
                ))
                .await()

            return true
        }

        private suspend fun uploadFile(uid: String, fileId: Long?, fileName: String): String?
        {
            fileId ?: return null

            val storage = Firebase.storage
            val ref = storage.reference.child("images/${uid}/${fileName}")
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileId)
            val result = ref.putFile(uri).await()

            return result.metadata?.path
        }

        suspend fun getPosts(uid: String): ArrayList<PostData>
        {
            val list = ArrayList<PostData>()

            val db = Firebase.firestore
            val userPosts = db.collection("posts/${uid}/list")

            val result = userPosts.get().await() ?: return list

            for(post in result.documents) {
                val text = post.get("text")?.toString() ?: continue
                val imgPath = post.get("imgPath")?.toString() ?: continue
                val date = post.get("date")?.toString() ?: continue
                list.add(PostData(imgPath, text, Timestamp(Date(date))))
            }

            return list
        }

        suspend fun getImage(imagePath: String): Bitmap {
            val imageRef = Firebase.storage.reference.child(imagePath)

            val bytes = imageRef.getBytes(Long.MAX_VALUE).await()

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}