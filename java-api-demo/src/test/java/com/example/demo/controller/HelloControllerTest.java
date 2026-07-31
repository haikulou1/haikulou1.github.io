package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc integration test for {@link HelloController} and {@link HashController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hello_returnsHelloWorldJson() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello World"));
    }

    @Test
    void hash_sha256_abc_returnsExpectedDigest() throws Exception {
        mockMvc.perform(get("/api/hash")
                        .param("algorithm", "sha-256")
                        .param("input", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("sha-256"))
                .andExpect(jsonPath("$.input").value("abc"))
                .andExpect(jsonPath("$.hash").value(
                        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"));
    }

    @Test
    void hash_missingInput_returns400() throws Exception {
        mockMvc.perform(get("/api/hash")
                        .param("algorithm", "sha-256"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("input parameter is required"));
    }

    @Test
    void hash_unsupportedAlgorithm_returns400() throws Exception {
        mockMvc.perform(get("/api/hash")
                        .param("algorithm", "rot13")
                        .param("input", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported algorithm: rot13"));
    }

    @Test
    void hash_oversizedInput_returns400() throws Exception {
        String oversizedInput = "a".repeat(1_048_577);
        mockMvc.perform(get("/api/hash")
                        .param("algorithm", "sha-256")
                        .param("input", oversizedInput))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "input exceeds maximum length of 1048576 characters"));
    }
}
