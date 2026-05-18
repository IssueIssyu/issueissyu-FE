package com.issueissyu.fe.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.issueissyu.fe.data.remote.api.IssueApiService
import javax.inject.Inject

interface IssueRepository {
    // suspend fun getIssues(): Flow<List<Issue>>
}

class DefaultIssueRepository @Inject constructor(
    private val apiService: IssueApiService,
    private val dataStore: DataStore<Preferences>
) : IssueRepository {
    // Implement repository methods here
}
