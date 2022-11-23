package edu.sns.memorystack.data

import com.google.firebase.Timestamp

data class ChattingData(val sender: String, val receiver: String, val type: Type, val message: String, val date: Timestamp)
{
    companion object
    {
        val KEY_SENDER = "sender"
        val KEY_RECEIVER = "receiver"
        val KEY_TYPE = "type"
        val KEY_MESSAGE = "message"
        val KEY_DATE = "date"
    }

    enum class Type
    {
        MESSAGE, IMG
    }
}