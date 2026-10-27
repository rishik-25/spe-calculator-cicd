package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import com.spe.calculator.exception.InvalidInputException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * All arithmetic lives here, using BigDecimal so 0.1 + 0.2 is exactly 0.3
 * (double would give 0.30000000000000004).
 */
@Service
public class CalculatorService {

    /** Largest absolute operand accepted: 1E15. */
    public static final BigDecimal MAX_ABSOLUTE_VALUE = new BigDecimal("1E15");
    /** Most decimal places accepted in an operand. */
    public static final int MAX_SCALE = 15;
    /** Division results are rounded (HALF_UP) to this many decimal places. */
    public static final int DIVISION_SCALE = 10;

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        validate("a", a);
        validate("b", b);
        return normalize(a.add(b));
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        validate("a", a);
        validate("b", b);
        return normalize(a.subtract(b));
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        validate("a", a);
        validate("b", b);
        return normalize(a.multiply(b));
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        validate("a", a);
        validate("b", b);
        if (b.signum() == 0) {
            throw new DivisionByZeroException();
        }
        return normalize(a.divide(b, DIVISION_SCALE, RoundingMode.HALF_UP));
    }

    /** Rejects huge or extremely precise values that could exhaust memory. */
    private void validate(String name, BigDecimal value) {
        if (value.abs().compareTo(MAX_ABSOLUTE_VALUE) > 0 || Math.abs(value.scale()) > MAX_SCALE) {
            throw new InvalidInputException("Operand '" + name
                    + "' must be between -1E15 and 1E15 and use at most 15 decimal places");
        }
    }

    /** 3.0000000000 becomes 3, and 1E+1 becomes 10, so the JSON stays readable. */
    private BigDecimal normalize(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}