package dev.falcon.garagefinderpro

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Utilities {
    fun deriveKeyFromPassword(password: String, salt: ByteArray, iterations: Int, keyLength: Int): SecretKeySpec {
        val pbKeySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = secretKeyFactory.generateSecret(pbKeySpec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    fun encryptData(data: String, password: String, salt: ByteArray, iterations: Int, keyLength: Int, iv: ByteArray): String {
        val secretKey = deriveKeyFromPassword(password, salt, iterations, keyLength)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decryptData(encryptedData: String, password: String, salt: ByteArray, iterations: Int, keyLength: Int, iv: ByteArray): String {
        val secretKey = deriveKeyFromPassword(password, salt, iterations, keyLength)
        val encryptedDataBytes = Base64.getDecoder().decode(encryptedData)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedDataBytes)
        return String(decryptedBytes)
    }

    fun generateIV(): ByteArray {
        val iv = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        return iv
    }

}

//check the above programm
fun main(){
    var plainText  = "Hello World"

    var password = "password"

    var salt = ByteArray(16)

    var iterations = 10000

    var keyLength = 256

    var initializationVector = ByteArray(16)

    var utilities = Utilities()

    var encryptedText = utilities.encryptData(plainText, password, salt, iterations, keyLength, initializationVector)

    println("Encrypted Text: $encryptedText")
    initializationVector = ByteArray(16)

    var decryptedText = utilities.decryptData(encryptedText, password, salt, iterations, keyLength, initializationVector)

    println("Decrypted Text: $decryptedText")
}