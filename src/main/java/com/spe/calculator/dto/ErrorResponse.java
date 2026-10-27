package com.spe.calculator.dto;

/** The one error format used for every HTTP 400 response. */
public record ErrorResponse(int status, String error, String message, String path) {
}
