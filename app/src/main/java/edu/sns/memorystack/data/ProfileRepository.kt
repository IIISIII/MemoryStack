package edu.sns.memorystack.data

import edu.sns.memorystack.method.AccountMethod

class ProfileRepository private constructor()
{
    companion object
    {
        private val instance = ProfileRepository()

        fun getInstance(): ProfileRepository
        {
            return instance
        }
    }

    private val hashMap: HashMap<String, UserProfile> = HashMap()

    suspend fun getUserProfile(uid: String): UserProfile?
    {
        hashMap[uid]?.let {
            return it
        }
        AccountMethod.getUserProfile(uid)?.let {
            hashMap[uid] = it
            return it
        }
        return null
    }
}