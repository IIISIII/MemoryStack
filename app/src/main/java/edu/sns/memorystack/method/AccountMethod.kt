package edu.sns.memorystack.method

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.UserProfile
import kotlinx.coroutines.tasks.await

class AccountMethod
{
    companion object
    {
        suspend fun createUser(profile: UserProfile): Boolean
        {
            if(profile.name.isBlank())
                throw Exception("ProfileDataBlankError: Username is blank")
            if(profile.nickname.isBlank())
                throw Exception("ProfileDataBlankError: Nickname is blank")
            if(profile.email.isBlank())
                throw Exception("ProfileDataBlankError: E-mail is blank")
            if(profile.password.isNullOrBlank())
                throw Exception("ProfileDataBlankError: Password is blank")
            if(profile.phone.isBlank())
                throw Exception("ProfileDataBlankError: Phone is blank")

            val db = Firebase.firestore
            val auth = Firebase.auth
            val users = db.collection("users")

            if(isProfileDataOverlap(users, UserProfile.KEY_NICKNAME, profile.nickname))
                throw Exception("ProfileDataOverlapError: ${profile.nickname} is already valid")
            if(isProfileDataOverlap(users, UserProfile.KEY_EMAIL, profile.email))
                throw Exception("ProfileDataOverlapError: ${profile.email} is already valid")
            if(isProfileDataOverlap(users, UserProfile.KEY_PHONE, profile.phone))
                throw Exception("ProfileDataOverlapError: ${profile.phone} is already valid")

            return try {
                val result = auth.createUserWithEmailAndPassword(profile.email, profile.password!!)
                    .await()

                result.user?.let {
                    users.document(it.uid).set(profile.toHashMap()).await()
                }

                true
            } catch (err: Exception) {
                false
            }
        }

        private suspend fun isProfileDataOverlap(ref: CollectionReference, key: String, value: String): Boolean
        {
            return !ref.whereEqualTo(key, value).get().await().isEmpty
        }

        suspend fun isProfileDataOverlap(key: String, value: String): Boolean
        {
            val db = Firebase.firestore
            return !db.collection("users").whereEqualTo(key, value).get().await().isEmpty
        }

        suspend fun login(email: String, password: String): Boolean
        {
            if(email.isBlank() || password.isBlank())
                return false

            val auth = Firebase.auth
            return try {
                val result = auth.signInWithEmailAndPassword(email, password)
                    .await()
                result.user != null
            } catch (err: Exception) {
                false
            }
        }

        suspend fun getUserProfile(uid: String): UserProfile?
        {
            val db = Firebase.firestore;

            val data = db.collection("users")
                .document(uid)
                .get()
                .await()

            val name = data.get(UserProfile.KEY_NAME).toString()
            val nickname = data.get(UserProfile.KEY_NICKNAME).toString()
            val email = data.get(UserProfile.KEY_EMAIL).toString()
            val phone = data.get(UserProfile.KEY_PHONE).toString()

            if(name.isNullOrBlank() || nickname.isNullOrBlank() || email.isNullOrBlank() || phone.isNullOrBlank())
                return null

            return UserProfile(name, nickname, email, null, phone)
        }
    }
}