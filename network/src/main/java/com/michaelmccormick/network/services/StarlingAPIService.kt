package com.michaelmccormick.network.services

import com.michaelmccormick.network.entities.AccountListEntity
import com.michaelmccormick.network.entities.CreateSavingsGoalRequestEntity
import com.michaelmccormick.network.entities.CreateSavingsGoalResponseEntity
import com.michaelmccormick.network.entities.FeedListEntity
import com.michaelmccormick.network.entities.SavingsGoalListEntity
import com.michaelmccormick.network.entities.SavingsGoalTransferEntity
import com.michaelmccormick.network.entities.TopUpRequestEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StarlingAPIService {
    @Headers(ACCEPT_JSON)
    @GET("accounts")
    suspend fun getAccounts(): AccountListEntity

    @Headers(ACCEPT_JSON)
    @GET("feed/account/{accountUid}/category/{categoryUid}/transactions-between")
    suspend fun getAccountFeed(
        @Path("accountUid") accountUid: String,
        @Path("categoryUid") categoryUid: String,
        @Query("minTransactionTimestamp") minTransactionTimestamp: String,
        @Query("maxTransactionTimestamp") maxTransactionTimestamp: String,
    ): FeedListEntity

    @Headers(ACCEPT_JSON)
    @GET("account/{accountUid}/savings-goals")
    suspend fun getSavingsGoals(
        @Path("accountUid") accountUid: String,
    ): SavingsGoalListEntity

    @Headers(ACCEPT_JSON)
    @PUT("account/{accountUid}/savings-goals")
    suspend fun createSavingsGoal(
        @Path("accountUid") accountUid: String,
        @Body createSavingsGoalRequestEntity: CreateSavingsGoalRequestEntity,
    ): CreateSavingsGoalResponseEntity

    @Headers(ACCEPT_JSON)
    @PUT("account/{accountUid}/savings-goals/{savingsGoalUid}/add-money/{transferUid}")
    suspend fun addMoneyToSavingsGoal(
        @Path("accountUid") accountUid: String,
        @Path("savingsGoalUid") savingsGoalUid: String,
        @Path("transferUid") transferUid: String,
        @Body topUpRequest: TopUpRequestEntity,
    ): SavingsGoalTransferEntity

    private companion object {
        const val ACCEPT_JSON = "Accept: application/json"
    }
}
