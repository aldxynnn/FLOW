package com.aldxynnn.flow.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): LoginResponse
    @GET("auth/me") suspend fun me(): MeResponse
    @GET("trips") suspend fun trips(): List<Trip>
    @POST("trips") suspend fun createTrip(@Body request: CreateTripRequest): Trip
    @POST("trips/{id}/assign") suspend fun assignTrip(@Path("id") id: Long, @Body request: AssignTripRequest): Trip
    @POST("trips/{id}/start") suspend fun startTrip(@Path("id") id: Long): Trip
    @POST("trips/{id}/complete") suspend fun completeTrip(@Path("id") id: Long): Trip
    @POST("trips/{id}/location") suspend fun updateLocation(@Path("id") id: Long, @Body request: LocationRequest): Trip
    @GET("dashboard/summary") suspend fun dashboardSummary(): DashboardSummary
    @GET("drivers") suspend fun drivers(): List<DriverSummary>
    @GET("vehicles") suspend fun vehicles(): List<Vehicle>
    @POST("vehicles") suspend fun createVehicle(@Body request: VehicleRequest): Vehicle
    @GET("users") suspend fun users(): List<UserSummary>
    @POST("users") suspend fun createUser(@Body request: CreateUserRequest): UserSummary
    @PATCH("users/{id}/enabled") suspend fun setUserEnabled(@Path("id") id: Long, @Body request: SetUserEnabledRequest): UserSummary
}
