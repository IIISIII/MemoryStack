package edu.sns.memorystack.network

data class NotificationBody(val to: String, val data: NotificationData)
{
    companion object {
        const val KEY_USERID = "userId"
        const val KEY_USERNAME = "userName"
        const val KEY_POSTID = "postId"
        const val KEY_POSTTEXT = "postText"
    }

    data class NotificationData(val userId: String, val userName: String, val postId: String, val postText: String)
}
