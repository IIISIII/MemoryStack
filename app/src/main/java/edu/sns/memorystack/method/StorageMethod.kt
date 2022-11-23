package edu.sns.memorystack.method

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class StorageMethod
{
    companion object
    {
        //blocking =================================================================================

        suspend fun uploadFile(uid: String, fileId: Long?, path: String): String?
        {
            fileId ?: return null

            try {
                val storage = Firebase.storage
                val ref = storage.reference.child(path)
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileId)
                val result = ref.putFile(uri).await()

                return result.metadata?.path
            } catch (_: Exception) {}

            return null
        }

        suspend fun getImage(imagePath: String): Bitmap?
        {
            try {
                val imageRef = Firebase.storage.reference.child(imagePath)

                val bytes = imageRef.getBytes(Long.MAX_VALUE).await()

                return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {}

            return null
        }
    }
}