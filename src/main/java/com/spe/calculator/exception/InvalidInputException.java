package com.spe.calculator.exception;

/** Thrown when an operand is syntactically valid but outside the supported range. */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}