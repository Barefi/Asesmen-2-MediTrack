package com.barefi0012.asesmen2.model

data class OperationError(
    val code: String? = null,
    val message: String? = null
)

data class OperationStatus(
    val success: Boolean? = null,
    val status: String? = null,
    val message: String? = null,
    val error: OperationError? = null
) {
    val isSuccess: Boolean
        get() = success == true || status.equals("success", ignoreCase = true)

    val errorMessage: String?
        get() = message ?: error?.message
}
