package io.opentelemetry.kotlin.error

class FakeSdkErrorHandler : SdkErrorHandler {

    class ApiMisuseDetails(
        val api: String,
        val details: String,
        val severity: SdkErrorSeverity
    )

    class SdkErrorDetails(
        val exc: Throwable,
        val details: String,
        val severity: SdkErrorSeverity
    )

    val apiMisuses = mutableListOf<ApiMisuseDetails>()
    val userCodeErrors = mutableListOf<SdkErrorDetails>()
    val sdkCodeErrors = mutableListOf<SdkErrorDetails>()

    fun hasErrors(): Boolean = apiMisuses.isNotEmpty() || userCodeErrors.isNotEmpty() || sdkCodeErrors.isNotEmpty()

    override fun onApiMisuse(
        api: String,
        details: String,
        severity: SdkErrorSeverity
    ) {
        apiMisuses.add(ApiMisuseDetails(api, details, severity))
    }

    override fun onUserCodeError(
        exc: Throwable,
        details: String,
        severity: SdkErrorSeverity
    ) {
        userCodeErrors.add(SdkErrorDetails(exc, details, severity))
    }

    override fun onSdkCodeError(
        exc: Throwable,
        details: String,
        severity: SdkErrorSeverity
    ) {
        sdkCodeErrors.add(SdkErrorDetails(exc, details, severity))
    }
}
