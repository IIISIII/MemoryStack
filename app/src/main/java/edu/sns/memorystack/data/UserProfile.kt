package edu.sns.memorystack.data

data class UserProfile(val name: String, val nickname: String, val email: String, val password: String?, val phone: String) {
    companion object
    {
        val KEY_NAME = "name"
        val KEY_NICKNAME = "nickname"
        val KEY_EMAIL = "email"
        val KEY_PASSWORD = "password"
        val KEY_PHONE = "phone"
    }

    fun toHashMap(): HashMap<String, String> {
        return hashMapOf(
            KEY_NAME to name,
            KEY_NICKNAME to nickname,
            KEY_EMAIL to email,
            KEY_PHONE to phone
        )
    }
}
