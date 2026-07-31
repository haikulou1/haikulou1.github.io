package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * Encapsulates JDK {@link MessageDigest} hashing for a small, fixed set of
 * algorithms. Produces lowercase hex digests via {@link HexFormat}.
 */
@Service
public class HashService {

    private static final Set<String> SUPPORTED = Set.of("md5", "sha-256", "sha-512");
    private static final String DEFAULT_ALGORITHM = "sha-256";

    public boolean isSupported(String algorithm) {
        return algorithm != null && SUPPORTED.contains(algorithm.toLowerCase());
    }

    /**
     * Hashes {@code input} with the given {@code algorithm} (md5 / sha-256 / sha-512).
     * A blank algorithm falls back to the default sha-256.
     *
     * @throws IllegalArgumentException if the algorithm is not supported
     */
    public String hash(String input, String algorithm) {
        String algo = (algorithm == null || algorithm.isBlank()) ? DEFAULT_ALGORITHM : algorithm.toLowerCase();
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unsupported algorithm: " + algorithm, e);
        }
    }
}
