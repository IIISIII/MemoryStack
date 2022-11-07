package edu.sns.memorystack.method

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.UserProfile
import kotlinx.coroutines.tasks.await

class AccountMethod
{
    companion object
    {
        fun createUser(profile: UserProfile, onSuccess: () -> Unit, onFailed: (String) -> Unit)
        {
            if(profile.name.isBlank()) {
                onFailed("Please enter User Name")
                return
            }
            if(profile.nickname.isBlank()) {
                onFailed("Please enter Nickname")
                return
            }
            if(profile.email.isBlank()) {
                onFailed("Please enter E-mail")
                return
            }
            if(profile.password.isNullOrBlank()) {
                onFailed("Please enter Password")
                return
            }
            if(profile.phone.isBlank()) {
                onFailed("Please enter Phone")
                return
            }

            val db = Firebase.firestore
            val auth = Firebase.auth
            try {
                auth.createUserWithEmailAndPassword(profile.email, profile.password!!)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            it.result.user?.uid?.let { uid ->
                                db.collection("users")
                                    .document(uid)
                                    .set(profile.toHashMap())
                                    .addOnSuccessListener {
                                        onSuccess()
                                    }
                                    .addOnFailureListener { err ->
                                        onFailed(err.message ?: "")
                                    }
                            }
                        } else
                            onFailed(it.exception?.message ?: "")
                    }
            } catch (err: Exception) {
                onFailed(err.message ?: "")
            }
        }

        fun login(email: String, password: String, onSuccess: () -> Unit, onFailed: (String) -> Unit)
        {
            if(email.isBlank()) {
                onFailed("Please enter E-mail")
                return
            }
            if(password.isBlank()) {
                onFailed("Please enter Password")
                return
            }

            val auth = Firebase.auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(it.isSuccessful)
                        onSuccess()
                    else
                        onFailed(it.exception?.message ?: "")
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