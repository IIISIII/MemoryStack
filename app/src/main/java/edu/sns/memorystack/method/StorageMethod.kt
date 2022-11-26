package edu.sns.memorystack.method

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
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

        //non-blocking ========================================================================
        fun getImage(imagePath: String, onSuccess: (Bitmap) -> Unit, onFailed: () -> Unit)
        {
            val imageRef = Firebase.storage.reference.child(imagePath)

            imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                onSuccess(bitmap)
            }
            .addOnFailureListener {
                onFailed()
            }
        }

        //=====================================================================================
        private val COLLECTION: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        fun getCursor(context: Context): Cursor?
        {
            val projection = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN)

            return context.contentResolver.query(COLLECTION, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC")
        }

        fun getImageFromMediaStore(context: Context, id: Long): Bitmap?
        {
            id.let {
                val uri = ContentUris.withAppendedId(COLLECTION, it)
                try {
                    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        context.contentResolver.loadThumbnail(uri, Size(100, 100), null)
                    else
                        MediaStore.Images.Thumbnails.getThumbnail(context.contentResolver, it, MediaStore.Images.Thumbnails.MINI_KIND, BitmapFactory.Options())
                } catch (err: Exception) {}
            }
            return null
        }
    }
}