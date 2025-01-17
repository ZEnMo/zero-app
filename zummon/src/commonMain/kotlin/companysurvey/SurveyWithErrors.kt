package com.zenmo.zummon.companysurvey

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class SurveyWithErrors(
    val survey: Survey,
    val errors: List<String>,
) {
    companion object {
        fun fromJson(jsonString: String): SurveyWithErrors {
            return kotlinx.serialization.json.Json.decodeFromString(SurveyWithErrors.serializer(), jsonString)
        }
    }

    fun withSurvey(survey: Survey) = SurveyWithErrors(survey, errors)

    fun withPandId(pandId: PandID) = SurveyWithErrors(survey.withPandId(pandId), errors)

    fun withoutPandId(pandId: PandID) = SurveyWithErrors(survey.withoutPandId(pandId), errors)
}

