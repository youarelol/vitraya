package com.vitraya.adjudication.engine.utils;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.InsurerEncryptedClaimRequest;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionUtils {
    // Below values are used for the Niva A2S Communication
    private static final String AES = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 32; // 256 bits

    // Below values are used for the Niva Iframe
    private static final String ENCRYPTION_KEY = "8x/A%D*G-KaPdSgV";
    public static final String INTERNAL_SECREAT_KEY = "Cx/1%d*G@KacVS3V";
    private static final String CHARACTER_ENCODING = "UTF-8";
//    public static final String NVA_IFRAME_SECRET_KEY = "EZP08zBK6tSrvQ/iNxVp15ghw4ascIQ=";

    @Value("${API_PARAM_ENCRYPTION_DECRYTION_KEY}")
    private String apiParamEncryptiondecryptionKey;

    @Value("${NVA_IFRAME_SECRET_KEY}")
    private String nivaIframeSecretKey;

    public static final String API_PARAM_ENCRYPTION_KEY = "EZP08zBK6tSrvQ/iNxVp15ghw4ascIQ=";
    /**
     * Method for Encrypt Plain String Data using AES and make and call to download claim document from Niva A2S
     *
     * @param plainText
     * @return encryptedText
     */
    public String encrypt(String plainText) {
        return encrypt(plainText, ENCRYPTION_KEY);
    }

    public String encryptInternalData(String plainText) {
        return encrypt(plainText, INTERNAL_SECREAT_KEY);
    }


    public String encrypt(String plainText, String key) {
        String encryptedText = null;
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            byte[] keyByte = key.getBytes(CHARACTER_ENCODING);
            SecretKeySpec secretKey = new SecretKeySpec(keyByte, AES);
            IvParameterSpec ivparameterspec = new IvParameterSpec(keyByte);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(CHARACTER_ENCODING));
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(cipherText);
        } catch (Exception e) {
            System.err.println("Encrypt Exception : " + e.getMessage());
        }

        return encryptedText;
    }

    public String decrypt(String encryptedText, String key) {
        String decryptedText = null;
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            byte[] keyByte = key.getBytes(CHARACTER_ENCODING);
            SecretKeySpec secretKey = new SecretKeySpec(keyByte, AES);
            IvParameterSpec ivparameterspec = new IvParameterSpec(keyByte);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] cipherText = decoder.decode(encryptedText);
            byte[] plainTextBytes = cipher.doFinal(cipherText);
            decryptedText = new String(plainTextBytes, CHARACTER_ENCODING);
        } catch (Exception e) {
            System.err.println("Decrypt Exception : " + e.getMessage());
        }
        return decryptedText;
    }

    // Create SecretKey from predefined key
    public static SecretKey createKey(String key) {
        byte[] keyBytes = new byte[AES_KEY_SIZE];
        byte[] keyBytesTemp = key.getBytes();
        System.arraycopy(keyBytesTemp, 0, keyBytes, 0, Math.min(keyBytesTemp.length, AES_KEY_SIZE));
        return new SecretKeySpec(keyBytes, AES);
    }

    /**
     * Encrypt the input text using the AES key and can be used for all new implementations
     *
     * @param data
     * @param secretKey
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        IvParameterSpec iv = new IvParameterSpec(new byte[16]); // 16-byte IV for AES
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes); // Encode encrypted bytes to Base64
    }

    // Decrypt the input text using the AES key
    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        IvParameterSpec iv = new IvParameterSpec(new byte[16]); // 16-byte IV for AES
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData); // Decode Base64 to bytes
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }


    public InsurerEncryptedClaimRequest decryptIframeData(String encryptedData) throws Exception {
        try {
            // Generate AES key
            SecretKey secretKey = createKey(nivaIframeSecretKey);
            String decryptedText = decrypt(encryptedData, secretKey);

            return GsonUtils.fromJson(decryptedText, InsurerEncryptedClaimRequest.class);
        } catch (Exception e) {
            log.error("Error while decrypting the encrypted data: ", e);
        }

        return null;
    }

    public static void main(String[] args) {
        String data = "{\"vitrayaClaimId\" : \"VCC_10509588_2231_2_1749536560 - 24697\",\"reviewer\" : \"MedicalAdju\",\"insurerCode\" : \"i32\"}";
        String key = "rZnuB3EJmIaGqPVq05xXtE1NKYEG9HsJ"; // 32-byte key for AES-256
        try {
            SecretKey secretKey = createKey(key);
            String encryptedData = encrypt(data, secretKey);
            System.out.println("Encrypted Data: " + encryptedData);

            String decryptedData = decrypt(encryptedData, secretKey);
            System.out.println("Decrypted Data: " + decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String validateEncrytedString(String encryptionString) {
        if (encryptionString == null || encryptionString.isEmpty()) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        if (encryptionString.contains("_")) {
            encryptionString = encryptionString.replaceAll("_", "/");
        }

        return encryptionString;
    }
}
