package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.mypage.MyIssuePage

interface UserRepository {
    suspend fun updateNickname(nickname: String)
    suspend fun updateUserAddress(lat: Double, lng: Double): Result<String>
    suspend fun checkNicknameDuplicate(nickname: String): Boolean
    suspend fun getMyIssues(size: Int = 10, cursor: String? = null): Result<MyIssuePage>
}
