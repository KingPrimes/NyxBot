package com.nyx.bot.utils;

import com.nyx.bot.core.SpringValues;

import javax.crypto.Cipher;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class x {

    public static KeyPair get() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }


    public static PublicKey p(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(URLDecoder.decode(publicKeyStr, StandardCharsets.UTF_8));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }


    public static PrivateKey pr(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(URLDecoder.decode(privateKeyStr, StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }


    public static String e(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return URLEncoder.encode(Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes())), StandardCharsets.UTF_8);
    }

    public static String d() {
        try {
            SpringValues bean = SpringUtils.getBean(SpringValues.class);
            return d(bean.e, pr(bean.d));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String d(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(URLDecoder.decode(encryptedData, StandardCharsets.UTF_8))));
    }
}
