package edu.sns.memorystack.data

import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.method.PostMethod
import edu.sns.memorystack.method.StorageMethod

class DataRepository private constructor()
{
    companion object
    {
        private val instance = DataRepository()

        fun getInstance(): DataRepository
        {
            return instance
        }
    }

    private val hashMap: HashMap<String, UserProfile> = HashMap()

    suspend fun getUserProfile(uid: String, flag: Boolean): UserProfile?
    {
        if(!flag) {
            hashMap[uid]?.let {
                val onSuccess: (UserProfile) -> Unit = { data ->
                    if (!data.compare(it))
                        hashMap[uid] = data
                }
                val onFailed: () -> Unit = {}
                AccountMethod.getUserProfile(uid, onSuccess, onFailed)
                return it
            }
        }
        AccountMethod.getUserProfile(uid)?.let {
            hashMap[uid] = it
            return it
        }
        return null
    }

    private val imageHashMap: HashMap<String, Bitmap> = HashMap()

    suspend fun getImage(imgPath: String?): Bitmap?
    {
        if(imgPath == null)
            return null
        imageHashMap[imgPath]?.let {
            val onSuccess: (Bitmap) -> Unit = { bitmap ->
                imageHashMap[imgPath] = bitmap
            }
            val onFailed: () -> Unit = {}
            StorageMethod.getImage(imgPath, onSuccess, onFailed)
            return it
        }
        StorageMethod.getImage(imgPath)?.let {
            imageHashMap[imgPath] = it
            return it
        }
        return null
    }

    private var lastVisiblePost: DocumentSnapshot? = null

    fun resetLastVisiblePost()
    {
        lastVisiblePost = null
    }

    suspend fun getPostsByIdLimit(uids: List<String>, limit: Int): ArrayList<PostData>
    {
        val capsule = PostMethod.getPostsByIdLimit(uids, limit, lastVisiblePost)
        lastVisiblePost = capsule.last
        return capsule.list
    }

    private var lastVisibleUser: DocumentSnapshot? = null

    fun resetLastVisibleUser()
    {
        lastVisibleUser = null
    }

    suspend fun getUserIdLimit(except: String, limit: Int): ArrayList<String>
    {
        val capsule = AccountMethod.getUserIdLimit(except, limit, lastVisibleUser)
        lastVisibleUser = capsule.last
        return capsule.list
    }
}