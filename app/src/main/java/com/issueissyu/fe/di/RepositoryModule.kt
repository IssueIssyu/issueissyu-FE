package com.issueissyu.fe.di

import com.issueissyu.fe.data.repository.DefaultIssueRepository
import com.issueissyu.fe.data.repository.IssueRepository
import com.issueissyu.fe.data.repository.UserRepository
import com.issueissyu.fe.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIssueRepository(
        defaultIssueRepository: DefaultIssueRepository
    ): IssueRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
