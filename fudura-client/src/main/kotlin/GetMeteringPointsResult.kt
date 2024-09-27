package com.zenmo.fudura

import kotlinx.serialization.Serializable

@Serializable
data class GetMeteringPointsResult(
    val eans: List<EanDetails>,
)

@Serializable
data class EanDetails(
    val ean: String,
    val meteringPoints: List<String>,
    val authorizationPeriods: List<AuthorizationPeriod>,
)

@Serializable
data class AuthorizationPeriod(
    val from: String,
    val to: String,
)
