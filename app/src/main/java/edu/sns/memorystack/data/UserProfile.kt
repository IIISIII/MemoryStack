package edu.sns.memorystack.data

data class UserProfile(val name: String, val nickname: String, val email: String, val password: String?, val phone: String) {
    fun toHashMap(): HashMap<String, String> {
        return hashMapOf(
            "name" to name,
            "nickname" to nickname,
            "email" to email,
            "phone" to phone
        )
    }
}
