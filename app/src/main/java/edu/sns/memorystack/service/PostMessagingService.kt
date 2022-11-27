package edu.sns.memorystack.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.sns.memorystack.MainActivity
import edu.sns.memorystack.R
import edu.sns.memorystack.data.DataRepository
import edu.sns.memorystack.data.UserProfile
import edu.sns.memorystack.method.AccountMethod
import edu.sns.memorystack.network.NotificationBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostMessagingService: FirebaseMessagingService()
{
    companion object {
        const val CHANNEL_NAME = "Posting"
        const val CHANNEL_NAME_2 = "Form Server"
        const val CHANNEL_ID = "MemoryStack"
    }

    private val repo = DataRepository.getInstance()

    override fun onNewToken(token: String)
    {
        super.onNewToken(token)
        //변경된 토큰 저장
        CoroutineScope(Dispatchers.IO).launch {
            AccountMethod.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage)
    {
        super.onMessageReceived(message)
        Log.i("push", "${message.data}")

        if(message.data.isNotEmpty()) {
            val userId = message.data[NotificationBody.KEY_USERID]!!
            val userName = message.data[NotificationBody.KEY_USERNAME]!!
            val postId = message.data[NotificationBody.KEY_POSTID]!!
            val postText = message.data[NotificationBody.KEY_POSTTEXT]!!

            sendMessageNotification(userId, userName, postId, postText)
        }
        //send from server
        else if(message.notification != null)
            sendNotification(message.notification?.title, message.notification?.body!!)
    }

    // 서버에서 직접 보냈을 때
    private fun sendNotification(title: String?, body: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 액티비티 중복 생성 방지
        val pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT) // 일회성

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 소리
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title) // 제목
            .setContentText(body) // 내용
            .setSmallIcon(R.drawable.stack_logo) // 아이콘
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 예외처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME_2, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 , notificationBuilder.build()) // 알림 생성
    }


    // 다른 기기에서 서버로 보냈을 때
    private fun sendMessageNotification(userId: String, userName: String, postId: String, postText: String){

        CoroutineScope(Dispatchers.IO).launch {
            val profile = repo.getUserProfile(userId, true)
            val img = repo.getImage(profile?.imgPath)

            withContext(Dispatchers.Main) {
                notifyMessage(profile!!, img)
            }
        }
    }

    private fun notifyMessage(profile: UserProfile, img: Bitmap?)
    {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 액티비티 중복 생성 방지
        val pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT) // 일회성

        val postMsg = this.getString(R.string.content_post)
        // messageStyle 로
        val user: Person = Person.Builder()
            .setName(profile.nickname)
            .setIcon(if(img == null) IconCompat.createWithResource(this, R.drawable.usericon_small) else IconCompat.createWithBitmap(img))
            .build()

        val message = NotificationCompat.MessagingStyle.Message(postMsg, System.currentTimeMillis(), user)
        val messageStyle = NotificationCompat.MessagingStyle(user).addMessage(message)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 소리
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(profile.nickname) // 제목
            .setContentText(postMsg) // 내용
            .setStyle(messageStyle)
            .setSmallIcon(R.drawable.stack_logo) // 아이콘
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 예외처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW) // 소리없앰
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 , notificationBuilder.build()) // 알림 생성
    }
}