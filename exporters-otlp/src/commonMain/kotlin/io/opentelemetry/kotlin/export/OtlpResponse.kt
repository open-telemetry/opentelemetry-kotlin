package io.opentelemetry.kotlin.export

internal sealed class OtlpResponse(val statusCode: Int) {

    object Success : OtlpResponse(200) {
        override fun toString(): String {
            return "Success(statusCode=$statusCode)"
        }
    }

    class ClientError(statusCode: Int, val errorMessage: String?) : OtlpResponse(statusCode) {
        override fun toString(): String {
            return "ClientError(errorMessage=$errorMessage, statusCode=$statusCode)"
        }
    }

    class ServerError(statusCode: Int, val errorMessage: String?) : OtlpResponse(statusCode) {
        override fun toString(): String {
            return "ServerError(errorMessage=$errorMessage, statusCode=$statusCode)"
        }
    }

    object Unknown : OtlpResponse(-1) {
        override fun toString(): String {
            return "Unknown(statusCode=$statusCode)"
        }
    }
}
