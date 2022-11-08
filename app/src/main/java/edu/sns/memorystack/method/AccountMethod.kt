package edu.sns.memorystack.method

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.UserProfile
import kotlinx.coroutines.tasks.await

class AccountMethod
{
    enum class AccountMethodResult {
        SUCCESS, FAILED, NAME_BLANK, NICKNAME_BLANK, EMAIL_BLANK, PASSWORD_BLANK, PHONE_BLANK, NAME_OVERLAP, NICKNAME_OVERLAP, EMAIL_OVERLAP, PHONE_OVERLAP
    }

    companion object
    {
        suspend fun createUser(profile: UserProfile): AccountMethodResult
        {
            if(profile.name.isBlank())
                return AccountMethodResult.NAME_BLANK
            if(profile.nickname.isBlank())
                return AccountMethodResult.NICKNAME_BLANK
            if(profile.email.isBlank())
                return AccountMethodResult.EMAIL_BLANK
            if(profile.password.isNullOrBlank())
                return AccountMethodResult.PASSWORD_BLANK
            if(profile.phone.isBlank())
                return AccountMethodResult.PHONE_BLANK

            val db = Firebase.firestore
            val auth = Firebase.auth
            val users = db.collection("users")

            if(isProfileDataOverlap(users, "nickname", profile.nickname))
                return AccountMethodResult.NICKNAME_OVERLAP
            if(isProfileDataOverlap(users, "email", profile.email))
                return AccountMethodResult.EMAIL_OVERLAP
            if(isProfileDataOverlap(users, "phone", profile.phone))
                return AccountMethodResult.PHONE_OVERLAP

            return try {
                val result = auth.createUserWithEmailAndPassword(profile.email, profile.password!!)
                    .await()

                if(result.user == null)
                    AccountMethodResult.FAILED
                else
                    AccountMethodResult.SUCCESS

            } catch (err: Exception) {
                AccountMethodResult.FAILED
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

        suspend fun login(email: String, password: String): AccountMethodResult
        {
            if(email.isBlank())
                return AccountMethodResult.EMAIL_BLANK
            if(password.isBlank())
                return AccountMethodResult.PASSWORD_BLANK

            val auth = Firebase.auth
            return try {
                val result = auth.signInWithEmailAndPassword(email, password)
                    .await()

                if(result.user == null)
                    AccountMethodResult.FAILED
                else
                    AccountMethodResult.SUCCESS

            } catch (err: Exception) {
                AccountMethodResult.FAILED
            }
        }

        suspend fun getUserProfile(uid: String): UserProfile?
        {
            val db = Firebase.firestore;

            val data = db.collection("users")
                .document(uid)
                .get()
                .await()

            val name = data.get("name").toString()
            val nickname = data.get("nickname").toString()
            val email = data.get("email").toString()
            val phone = data.get("phone").toString()

            if(name.isNullOrBlank() || nickname.isNullOrBlank() || email.isNullOrBlank() || phone.isNullOrBlank())
                return null

            return UserProfile(name, nickname, email, null, phone)
        }
    }
}