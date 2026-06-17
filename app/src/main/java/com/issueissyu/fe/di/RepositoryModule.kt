package com.issueissyu.fe.di

import com.issueissyu.fe.data.repository.AlarmRepositoryImpl
import com.issueissyu.fe.data.repository.AuthRepositoryImpl
import com.issueissyu.fe.data.repository.CollectionRepositoryImpl
import com.issueissyu.fe.data.repository.IssueRepositoryImpl
import com.issueissyu.fe.domain.repository.IssueRepository
import com.issueissyu.fe.domain.repository.UserRepository
import com.issueissyu.fe.data.repository.UserRepositoryImpl
import com.issueissyu.fe.data.repository.LocationRepositoryImpl
import com.issueissyu.fe.domain.repository.AuthRepository
import com.issueissyu.fe.domain.repository.LocationRepository

import com.issueissyu.fe.data.repository.CommunityRepositoryImpl
import com.issueissyu.fe.data.repository.BillingRepositoryImpl
import com.issueissyu.fe.data.repository.MapRepositoryImpl
import com.issueissyu.fe.data.repository.PinRepositoryImpl
import com.issueissyu.fe.domain.repository.AlarmRepository
import com.issueissyu.fe.data.repository.UserCollectionsStoreImpl
import com.issueissyu.fe.domain.repository.CollectionRepository
import com.issueissyu.fe.domain.repository.UserCollectionsStore
import com.issueissyu.fe.domain.repository.CommunityRepository
import com.issueissyu.fe.domain.repository.BillingRepository
import com.issueissyu.fe.domain.repository.MapRepository
import com.issueissyu.fe.domain.repository.PinRepository
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
        impl: IssueRepositoryImpl
    ): IssueRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

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

    @Binds
    @Singleton
    abstract fun bindPinRepository(
        impl: PinRepositoryImpl
    ): PinRepository

    @Binds
    @Singleton
    abstract fun bindMapRepository(
        impl: MapRepositoryImpl
    ): MapRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        impl: BillingRepositoryImpl
    ): BillingRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(
        impl: CollectionRepositoryImpl
    ): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        impl: AlarmRepositoryImpl
    ): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindUserCollectionsStore(
        impl: UserCollectionsStoreImpl
    ): UserCollectionsStore
}
