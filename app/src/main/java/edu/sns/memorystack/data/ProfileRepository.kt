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
            val onSuccess: (UserProfile) -> Unit = { data ->
                if(!data.compare(it))
                    hashMap[uid] = data
            }
            val onFailed: () -> Unit = {}
            AccountMethod.getUserProfile(uid, onSuccess, onFailed)
            return it
        }
        AccountMethod.getUserProfile(uid)?.let {
            hashMap[uid] = it
            return it
        }
        return null
    }
}