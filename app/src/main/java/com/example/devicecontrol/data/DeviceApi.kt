package com.example.devicecontrol.data

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DeviceApi {
    @FormUrlEncoded
    @POST("common/sms/sendCode")
    suspend fun sendCode(
        @Field("phone") phone: String,
        @Field("template") template: String = "reg",
    ): ApiEnvelope<EmptyData>

    @FormUrlEncoded
    @POST("user/reg")
    suspend fun login(
        @Field("channel") channel: String = ApiConfig.LOGIN_CHANNEL,
        @Field("phone") phone: String,
        @Field("verify") verify: String,
    ): ApiEnvelope<LoginData>

    @FormUrlEncoded
    @POST("user/balance")
    suspend fun queryBalance(@Field("token") token: String): ApiEnvelope<BalanceData>

    @FormUrlEncoded
    @POST("goods/latestUsed")
    suspend fun getLatestUsed(
        @Field("categoryCode") categoryCode: String = "5",
        @Field("token") token: String,
    ): ApiEnvelope<List<DeviceItem>>

    @FormUrlEncoded
    @POST("goods/normal/skus")
    suspend fun goodsid2sku(
        @Field("goodsId") goodsId: String,
        @Field("token") token: String,
    ): ApiEnvelope<List<SkuData>>

    @FormUrlEncoded
    @POST("goods/normal/details")
    suspend fun getImei(
        @Field("goodsId") goodsId: String,
        @Field("token") token: String,
    ): ApiEnvelope<ImeiData>

    @FormUrlEncoded
    @POST("userIntegral/checkUserIsRisk")
    suspend fun useIntergral(@Field("token") token: String): ApiEnvelope<EmptyData>

    @FormUrlEncoded
    @POST("payChannelRoute/addUserAfterPayChannel")
    suspend fun addUserAfterPayChannel(
        @Field("method") method: String = "15",
        @Field("token") token: String,
    ): ApiEnvelope<EmptyData>

    @FormUrlEncoded
    @POST("orderRisk/isCheckLocation")
    suspend fun isCheckLocation(
        @Field("categoryCode") categoryCode: String = "04",
        @Field("imei") imei: String,
        @Field("token") token: String,
    ): ApiEnvelope<EmptyData>

    @FormUrlEncoded
    @POST("goods/water/unlock")
    suspend fun unlockWater(
        @Field("skuId") skuId: String,
        @Field("promotions") promotions: String,
        @Field("token") token: String,
    ): ApiEnvelope<UnlockData>

    @FormUrlEncoded
    @POST("goods/water/sync")
    suspend fun syncWater(
        @Field("skuId") skuId: String,
        @Field("token") token: String,
    ): ApiEnvelope<SyncData>

    @FormUrlEncoded
    @POST("order/afterPay/creating")
    suspend fun createAfterPay(
        @Field("orderNo") orderNo: String,
        @Field("token") token: String,
    ): ApiEnvelope<AfterPayCreatingData>

    @FormUrlEncoded
    @POST("order/detail")
    suspend fun orderDetail(
        @Field("orderId") orderId: String,
        @Field("token") token: String,
    ): ApiEnvelope<OrderDetailData>
}
