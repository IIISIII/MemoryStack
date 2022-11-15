package edu.sns.memorystack.data

data class FollowDTO(
    var followerCount : Int = 1,
    var followers : MutableMap<String,Boolean> = HashMap(),

    var followingCount : Int = 1,
    var followings : MutableMap<String,Boolean> = HashMap()
)
