package com.issueissyu.fe.di

import com.issueissyu.fe.data.repository.AuthRepositoryImpl
import com.issueissyu.fe.domain.repository.DefaultIssueRepository
import com.issueissyu.fe.domain.repository.IssueRepository
import com.issueissyu.fe.domain.repository.UserRepository
import com.issueissyu.fe.data.repository.UserRepositoryImpl
import com.issueissyu.fe.data.repository.LocationRepositoryImpl
import com.issueissyu.fe.domain.repository.PinRepository
import com.issueissyu.fe.data.repository.PinRepositoryImpl

import com.issueissyu.fe.domain.repository.AuthRepository
import com.issueissyu.fe.domain.repository.LocationRepository

import com.issueissyu.fe.data.repository.CommunityRepositoryImpl
import com.issueissyu.fe.domain.repository.CommunityRepository
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

    @Binds
    @Singleton
    abstract fun bindPinRepository(
        impl: PinRepositoryImpl
    ): PinRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        impl: CommunityRepositoryImpl
    ): CommunityRepository
}
