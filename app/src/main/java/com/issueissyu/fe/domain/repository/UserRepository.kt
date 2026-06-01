package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.User
import com.issueissyu.fe.domain.model.mypage.MyIssuePage
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getProfile(): Flow<User>
    suspend fun updateNickname(nickname: String)
    suspend fun updateUserAddress(lat: Double, lng: Double): Result<String>
    suspend fun updateProfileImage(imageUrl: String)
    suspend fun checkNicknameDuplicate(nickname: String): Boolean
    suspend fun getMyIssues(size: Int = 10, cursor: String? = null): Result<MyIssuePage>
}