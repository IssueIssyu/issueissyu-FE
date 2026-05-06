package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getProfile(): Flow<User>
    suspend fun updateNickname(nickname: String)
    suspend fun updateProfileImage(imageUrl: String)
    suspend fun checkNicknameDuplicate(nickname: String): Boolean
}