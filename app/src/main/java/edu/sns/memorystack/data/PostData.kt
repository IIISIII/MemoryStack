package edu.sns.memorystack.data

import com.google.firebase.Timestamp

data class PostData(val uid: String, val imgPath: String, val text: String, val date: Timestamp)
{
    companion object
    {
        const val KEY_UID = "uid"
        const val KEY_IMG = "imgPath"
        const val KEY_TEXT = "text"
        const val KEY_DATE = "date"
    }
}
