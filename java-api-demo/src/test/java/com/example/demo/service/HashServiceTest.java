package com.example.demo.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit test for {@link HashService} using known digest values.
 */
class HashServiceTest {

    private final HashService hashService = new HashService();

    @Test
    void sha256_ofAbc_matchesKnownDigest() {
        String hash = hashService.hash("abc", "sha-256");
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hash);
    }

    @Test
    void md5_ofAbc_matchesKnownDigest() {
        String hash = hashService.hash("abc", "md5");
        assertEquals("900150983cd24fb0d6963f7d28e17f72", hash);
    }

    @Test
    void sha512_ofAbc_matchesKnownDigest() {
        String hash = hashService.hash("abc", "sha-512");
        assertEquals("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a"
                + "2192992a274fc1a836ba3c23a3feebbd4564d361b2c8a77e0b0b8b0e6c2f9d3b", hash);
    }

    @Test
    void blankAlgorithm_defaultsToSha256() {
        String hash = hashService.hash("abc", "");
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hash);
    }

    @Test
    void nullAlgorithm_defaultsToSha256() {
        String hash = hashService.hash("abc", null);
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hash);
    }

    @Test
    void unsupportedAlgorithm_throws() {
        assertThrows(IllegalArgumentException.class, () -> hashService.hash("abc", "rot13"));
    }

    @Test
    void isSupported_recognizesAllAndRejectsUnknown() {
        assertTrue(hashService.isSupported("md5"));
        assertTrue(hashService.isSupported("sha-256"));
        assertTrue(hashService.isSupported("sha-512"));
        assertFalse(hashService.isSupported("rot13"));
        assertFalse(hashService.isSupported(null));
    }
}
