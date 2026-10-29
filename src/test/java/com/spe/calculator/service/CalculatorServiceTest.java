package com.spe.calculator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.spe.calculator.exception.DivisionByZeroException;
import com.spe.calculator.exception.InvalidInputException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    private static BigDecimal n(String value) {
        return new BigDecimal(value);
    }

    private static void assertResult(String expected, BigDecimal actual) {
        assertEquals(0, n(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    // ---- addition ----
    @Test
    void addsTwoPositiveNumbers() {
        assertResult("6", service.add(n("2"), n("3")));
    }

    @Test
    void addsNegativeNumbers() {
        assertResult("-5", service.add(n("-2"), n("-3")));
    }

    @Test
    void addsDecimalsExactly() {
        assertResult("0.3", service.add(n("0.1"), n("0.2")));
    }

    @Test
    void acceptsOperandAtUpperBoundary() {
        assertResult("1000000000000001", service.add(n("1E15"), n("1")));
    }

    // ---- subtraction ----
    @Test
    void subtractsTwoNumbers() {
        assertResult("3", service.subtract(n("5"), n("2")));
    }

    @Test
    void subtractionCanBeNegative() {
        assertResult("-3", service.subtract(n("2"), n("5")));
    }

    // ---- multiplication ----
    @Test
    void multipliesTwoNumbers() {
        assertResult("6", service.multiply(n("2"), n("3")));
    }

    @Test
    void multiplyByZeroGivesZero() {
        assertResult("0", service.multiply(n("7"), n("0")));
    }

    @Test
    void multipliesDecimals() {
        assertResult("3", service.multiply(n("1.5"), n("2")));
    }

    // ---- division ----
    @Test
    void dividesEvenly() {
        assertResult("3", service.divide(n("6"), n("2")));
    }

    @Test
    void dividesWithDecimalResult() {
        assertResult("3.5", service.divide(n("7"), n("2")));
    }

    @Test
    void roundsRepeatingDecimalsToTenPlaces() {
        assertResult("0.3333333333", service.divide(n("1"), n("3")));
    }

    // ---- division by zero ----
    @Test
    void divisionByZeroThrows() {
        assertThrows(DivisionByZeroException.class, () -> service.divide(n("6"), n("0")));
    }

    @Test
    void divisionByDecimalZeroThrows() {
        assertThrows(DivisionByZeroException.class, () -> service.divide(n("6"), n("0.0")));
    }

    // ---- invalid ranges ----
    @Test
    void rejectsOperandAboveMaximum() {
        assertThrows(InvalidInputException.class, () -> service.add(n("1E16"), n("1")));
    }

    @Test
    void rejectsTooManyDecimalPlaces() {
        assertThrows(InvalidInputException.class, () -> service.add(n("0.0000000000000001"), n("1")));
    }
}