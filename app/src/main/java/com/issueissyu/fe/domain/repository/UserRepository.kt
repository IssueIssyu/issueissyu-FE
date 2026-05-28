package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getProfile(): Flow<User>
    suspend fun updateNickname(nickname: String)
    suspend fun updateUserAddress(lat: Double, lng: Double): Result<String>
    suspend fun updateProfileImage(imageUrl: String)
    suspend fun checkNicknameDuplicate(nickname: String): Boolean
}