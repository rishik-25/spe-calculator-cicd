package com.spe.calculator.dto;

import java.math.BigDecimal;

/** Successful response: the operation, both operands and the result. */
public record CalculationResponse(String operation, BigDecimal a, BigDecimal b, BigDecimal result) {
}