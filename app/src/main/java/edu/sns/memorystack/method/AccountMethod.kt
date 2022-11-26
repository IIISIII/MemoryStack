package edu.sns.memorystack.method

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.memorystack.data.PostData
import edu.sns.memorystack.data.UserProfile
import kotlinx.coroutines.tasks.await

class AccountMethod
{
    companion object
    {
        //blocking ==========================================================================

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
            val db = Firebase.firestore

            val data = db.collection("users")
                .document(uid)
                .get()
                .await()

            val name = data.get(UserProfile.KEY_NAME).toString()
            val nickname = data.get(UserProfile.KEY_NICKNAME).toString()
            val email = data.get(UserProfile.KEY_EMAIL).toString()
            val phone = data.get(UserProfile.KEY_PHONE).toString()
            val imagePath = data.get(UserProfile.KEY_PROFILE_IMG)?.toString()

            if(name.isNullOrBlank() || nickname.isNullOrBlank() || email.isNullOrBlank() || phone.isNullOrBlank())
                return null

            return UserProfile(name, nickname, email, null, phone, imagePath)
        }

        //업데이트 성공하면 true 실패하면 false 반환
        suspend fun updateUserProfile(uid: String, profile: UserProfile?): Boolean
        {
            if(profile == null)
                return false

            val db = Firebase.firestore

            profile!!.let {
                val updateProfile = getUserProfile(uid)?.toHashMap()

                updateProfile?.let { map ->
                    if(it.nickname.isNotBlank())
                        map[UserProfile.KEY_NICKNAME] = it.nickname
                    if(it.phone.isNotBlank())
                        map[UserProfile.KEY_PHONE] = it.phone

                    db.collection("users")
                        .document(uid)
                        .update(map as Map<String, Any>)
                        .await()
                } ?: return false
            }

            return true
        }

        suspend fun updateUserProfileImage(uid: String, fileId: Long): Boolean
        {
            val db = Firebase.firestore

            val users = db.collection("users")

            try {
                val updateProfile = getUserProfile(uid) ?: return false

                updateProfile.let {
                    val imgPath = StorageMethod.uploadFile(uid, fileId, "profiles/${uid}/profile") ?: return false
                    users
                        .document(uid)
                        .update(UserProfile.KEY_PROFILE_IMG, imgPath)
                        .await()
                } ?: return false

                return true
            } catch (err: Exception) {}

            return false
        }

        suspend fun getAllUid(except: String): ArrayList<String>
        {
            val db = Firebase.firestore
            val list = ArrayList<String>()

            val users = db.collection("users")
                .orderBy(UserProfile.KEY_NICKNAME, Query.Direction.ASCENDING)
                .get()
                .await()

            for(document in users) {
                if(document.id != except)
                    list.add(document.id)
            }

            return list;
        }

        data class Capsule(val list: ArrayList<String>, val last: DocumentSnapshot?)

        suspend fun getUserIdLimit(except: String, limit: Int, last: DocumentSnapshot?): Capsule
        {
            val db = Firebase.firestore
            val list = ArrayList<String>()

            var lastDoc: DocumentSnapshot? = null

            val users = db.collection("users")
                .orderBy(UserProfile.KEY_NICKNAME, Query.Direction.ASCENDING)

            val query = if(last == null)
                    users
                else
                    users.startAfter(last)

            val result = query
                .limit((limit + 1).toLong())
                .get()
                .await()

            var count = 0
            for(document in result) {
                if(count == limit)
                    break
                if(document.id != except) {
                    list.add(document.id)
                    lastDoc = document
                    count ++
                }
            }

            return Capsule(list, lastDoc);
        }

        suspend fun registerToken(token: String)
        {
            val user = Firebase.auth.currentUser ?: return
            val uid = user.uid

            val db = Firebase.firestore

            val result = db.collection("users")
                .document(uid)
                .update(UserProfile.KEY_TOKEN, token)
                .await()
        }

        suspend fun getTokenById(uids: ArrayList<String>, except: String?): ArrayList<String>
        {
            val subLists = ArrayList<List<String>>()
            var index = 0;
            while(index < uids.size) {
                if(index + 10 < uids.size)
                    subLists.add(uids.subList(index, index + 10))
                else
                    subLists.add(uids.subList(index, uids.size))
                index += 10
            }

            val list = ArrayList<String>()

            val db = Firebase.firestore
            val users = db.collection("users")

            val tasks = ArrayList<Task<QuerySnapshot>>()
            for(userlist in subLists) {
                val result = users
                    .whereIn(FieldPath.documentId(), userlist)
                    .get()
                tasks.add(result)
            }
            for(task in tasks) {
                val result = task.await()
                for(post in result.documents) {
                    if(post.id != except) {
                        post.getString(UserProfile.KEY_TOKEN)?.let {
                            list.add(it)
                        }
                    }
                }
            }

            return list
        }

        //non-blocking ==========================================================================

        fun getUserProfile(uid: String, onSuccess: (UserProfile) -> Unit, onFailed: () -> Unit)
        {
            val db = Firebase.firestore

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener {
                    val name = it.get(UserProfile.KEY_NAME).toString()
                    val nickname = it.get(UserProfile.KEY_NICKNAME).toString()
                    val email = it.get(UserProfile.KEY_EMAIL).toString()
                    val phone = it.get(UserProfile.KEY_PHONE).toString()

                    if(name.isNullOrBlank() || nickname.isNullOrBlank() || email.isNullOrBlank() || phone.isNullOrBlank())
                        onSuccess(UserProfile(name, nickname, email, null, phone))
                    else
                        onFailed()
                }
                .addOnFailureListener {
                    onFailed()
                }
        }
    }
}