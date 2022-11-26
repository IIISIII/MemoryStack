package edu.sns.memorystack.data

data class UserProfile(val name: String = "", var nickname: String = "", val email: String = "", val password: String? = null, val phone: String = "", val imgPath: String? = null)
{
    companion object
    {
        const val KEY_NAME = "name"
        const val KEY_NICKNAME = "nickname"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_PHONE = "phone"
        const val KEY_PROFILE_IMG = "profile_img"
        const val KEY_TOKEN = "token"
    }

    fun toHashMap(): HashMap<String, String> {
        if(imgPath != null)
            return hashMapOf(
                KEY_NAME to name,
                KEY_NICKNAME to nickname,
                KEY_EMAIL to email,
                KEY_PHONE to phone,
                KEY_PROFILE_IMG to imgPath
            )
        return hashMapOf(
            KEY_NAME to name,
            KEY_NICKNAME to nickname,
            KEY_EMAIL to email,
            KEY_PHONE to phone
        )
    }

    fun compare(other: UserProfile): Boolean
    {
        if(other.name != name)
            return false
        else if(other.nickname != nickname)
            return false
        else if(other.email != email)
            return false
        else if(other.phone != phone)
            return false
        else if(other.imgPath != imgPath)
            return false
        return true
    }
}
