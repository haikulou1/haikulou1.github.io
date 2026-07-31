package com.example.demo.controller;

import com.example.demo.service.HashService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GET /api/hash?algorithm={md5|sha-256|sha-512}&input={text}
 */
@RestController
@RequestMapping("/api")
public class HashController {

    private static final String DEFAULT_ALGORITHM = "sha-256";

    private final HashService hashService;

    public HashController(HashService hashService) {
        this.hashService = hashService;
    }

    @GetMapping("/hash")
    public ResponseEntity<?> hash(
            @RequestParam(value = "algorithm", required = false) String algorithm,
            @RequestParam(value = "input", required = false) String input) {

        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "input parameter is required"));
        }

        String algo = (algorithm == null || algorithm.isBlank()) ? DEFAULT_ALGORITHM : algorithm.toLowerCase();
        if (!hashService.isSupported(algo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "unsupported algorithm: " + algorithm));
        }

        String hash = hashService.hash(input, algo);
        return ResponseEntity.ok(Map.of(
                "algorithm", algo,
                "input", input,
                "hash", hash
        ));
    }
}
