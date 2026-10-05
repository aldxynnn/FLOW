package com.aldxynnn.flowbackend.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiError(val message: String)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun badRequest(ex: RuntimeException): ResponseEntity<ApiError> = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(ex.message ?: "Request rejected"))

    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorized(ex: UnauthorizedException): ResponseEntity<ApiError> = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError(ex.message ?: "Unauthorized"))

    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(ex: NoSuchElementException): ResponseEntity<ApiError> = ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError(ex.message ?: "Not found"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(message))
    }
}
