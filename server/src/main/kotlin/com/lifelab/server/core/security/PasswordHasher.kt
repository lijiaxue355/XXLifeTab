package com.lifelab.server.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private val random = SecureRandom()

    data class PasswordHash(
        val hash: String,
        val salt: String,
    )

    fun hash(password: String): PasswordHash {
        val salt = ByteArray(SALT_LENGTH_BYTES).also(random::nextBytes)
        val hash = deriveKey(password, salt)
        return PasswordHash(
            hash = Base64.getEncoder().encodeToString(hash),
            salt = Base64.getEncoder().encodeToString(salt),
        )
    }

    fun verify(password: String, expectedHash: String, encodedSalt: String): Boolean {
        val salt = runCatching { Base64.getDecoder().decode(encodedSalt) }.getOrNull()
            ?: return false
        val expected = runCatching { Base64.getDecoder().decode(expectedHash) }.getOrNull()
            ?: return false
        return MessageDigest.isEqual(deriveKey(password, salt), expected)
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }
}
