package com.damian.viewer

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFound(e: UserNotFoundException): ErrorResponse {
        return ErrorResponse(HttpStatus.NOT_FOUND.value(), e.message)
    }

    data class ErrorResponse(val status: Int, val message: String?)
}