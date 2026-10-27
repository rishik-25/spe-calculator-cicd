package com.spe.calculator.controller;

import com.spe.calculator.dto.CalculationResponse;
import com.spe.calculator.service.CalculatorService;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/add")
    public CalculationResponse add(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
        return new CalculationResponse("add", a, b, calculatorService.add(a, b));
    }

    @GetMapping("/subtract")
    public CalculationResponse subtract(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
        return new CalculationResponse("subtract", a, b, calculatorService.subtract(a, b));
    }

    @GetMapping("/multiply")
    public CalculationResponse multiply(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
        return new CalculationResponse("multiply", a, b, calculatorService.multiply(a, b));
    }

    @GetMapping("/divide")
    public CalculationResponse divide(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
        return new CalculationResponse("divide", a, b, calculatorService.divide(a, b));
    }
}