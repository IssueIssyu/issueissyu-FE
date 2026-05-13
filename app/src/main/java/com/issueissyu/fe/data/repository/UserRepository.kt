package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.model.DemoPin
import com.issueissyu.fe.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getProfile(): Flow<User>
    fun getProfileImageRes(): Flow<Int>
    fun getBookmarkedPins(): Flow<List<DemoPin>>
    suspend fun updateNickname(nickname: String)
    suspend fun updateProfileImage(imageUrl: String)
    suspend fun updateProfileImageRes(resId: Int)
    suspend fun toggleBookmark(pin: DemoPin)
    suspend fun checkNicknameDuplicate(nickname: String): Boolean
}