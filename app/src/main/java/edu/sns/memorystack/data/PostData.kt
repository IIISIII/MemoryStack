package edu.sns.memorystack.data

import com.google.firebase.Timestamp

data class PostData(val uid: String, val imgPath: String, val text: String, val date: Timestamp)
