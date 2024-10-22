package com.zenmo.zummon.companysurvey

fun interface Validator {
    fun validate(survey: Survey): ValidationResult
}

data class ValidationResult(
    val status: Status,
    val message: String,
)

enum class Status {
    VALID,
    INVALID,
    MISSING_DATA,
}

val validateContractedCapacity = Validator { survey: Survey ->
    val gridConnection = survey.getSingleGridConnection()

    val contractedConnectionCapacity = gridConnection.electricity.getContractedConnectionCapacityKw()
    val physicalConnectionCapacity = gridConnection.electricity.grootverbruik?.physicalCapacityKw

    return@Validator when {
        contractedConnectionCapacity == null -> ValidationResult(
            status = Status.MISSING_DATA,
            message = "No contracted connection capacity given",
        )
        physicalConnectionCapacity == null -> ValidationResult(
            status = Status.MISSING_DATA,
            message = "No physical connection capacity given",
        )
        contractedConnectionCapacity <= physicalConnectionCapacity -> ValidationResult(
            status = Status.VALID,
            message = "Contracted connection capacity fits within physical connection",
        )
        else -> ValidationResult(
            status = Status.INVALID,
            message = "Contracted connection capacity ${contractedConnectionCapacity} kW " +
                    "is too large for physical connection ${physicalConnectionCapacity}"
        )
    }
}
