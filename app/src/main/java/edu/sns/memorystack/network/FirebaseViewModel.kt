package edu.sns.memorystack.network

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FirebaseViewModel(application: Application) : AndroidViewModel(application)
{
    private val repository : FirebaseRepository = FirebaseRepository()
    val myResponse = repository.myResponse

    // 푸시 메세지 전송
    fun sendNotification(notification: NotificationBody)
    {
        viewModelScope.launch {
            repository.sendNotification(notification)
        }
    }
}