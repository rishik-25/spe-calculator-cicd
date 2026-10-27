package com.spe.calculator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests: the full Spring context, driven through MockMvc (no real server needed). */
@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- happy paths: status code, content type and JSON body ----
    @Test
    void addReturnsStructuredJson() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.a").value(2))
                .andExpect(jsonPath("$.b").value(3))
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    void subtractReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/subtract").param("a", "5").param("b", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("subtract"))
                .andExpect(jsonPath("$.result").value(3));
    }

    @Test
    void multiplyReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/multiply").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("multiply"))
                .andExpect(jsonPath("$.result").value(6));
    }

    @Test
    void divideReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "6").param("b", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("divide"))
                .andExpect(jsonPath("$.result").value(3));
    }

    @Test
    void divideRoundsRepeatingDecimals() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "1").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0.3333333333));
    }

    // ---- error handling: always HTTP 400 with the same JSON shape ----
    @Test
    void divisionByZeroReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "6").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("DIVISION_BY_ZERO"))
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"))
                .andExpect(jsonPath("$.path").value("/api/v1/divide"));
    }

    @Test
    void missingParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Required parameter 'b' is missing"));
    }

    @Test
    void bothParametersMissingReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/multiply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_PARAMETER"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "NaN", "Infinity", "2x"})
    void nonNumericParameterReturns400(String badValue) throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", badValue).param("b", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.path").value("/api/v1/add"));
    }

    @Test
    void outOfRangeOperandReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "1E16").param("b", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    // ---- actuator ----
    @Test
    void healthEndpointReportsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}