package io.opentelemetry.kotlin.error

class FakeSdkErrorHandler : SdkErrorHandler {

    val errors = mutableListOf<SdkError>()

    val apiMisuses: List<SdkError.ApiMisuse>
        get() = errors.filterIsInstance<SdkError.ApiMisuse>()

    val userCodeErrors: List<SdkError.UserCodeError>
        get() = errors.filterIsInstance<SdkError.UserCodeError>()

    val sdkCodeErrors: List<SdkError.SdkCodeError>
        get() = errors.filterIsInstance<SdkError.SdkCodeError>()

    fun hasErrors(): Boolean = errors.isNotEmpty()

    override fun onError(error: SdkError) {
        errors.add(error)
    }
}
