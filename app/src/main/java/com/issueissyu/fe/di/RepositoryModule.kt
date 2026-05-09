package com.issueissyu.fe.di

import com.issueissyu.fe.data.repository.DefaultIssueRepository
import com.issueissyu.fe.data.repository.IssueRepository
import com.issueissyu.fe.data.repository.UserRepository
import com.issueissyu.fe.data.repository.UserRepositoryImpl
import com.issueissyu.fe.data.repository.PinRepository
import com.issueissyu.fe.data.repository.PinRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
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
    abstract fun bindPinRepository(
        impl: PinRepositoryImpl
    ): PinRepository
}
