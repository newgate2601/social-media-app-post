package com.example.social_media_app_post.security;

import lombok.extern.slf4j.Slf4j;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class KeyConverter {
    public static RSAPublicKey getPublicKeyFromString(String publicKeyStr) throws Exception {
        publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        log.error("Public Key: " + publicKeyStr);
        return rsaPublicKey;
    }

    public static RSAPrivateKey getPrivateKeyFromString(String privateKeyStr) throws Exception {
        privateKeyStr = privateKeyStr.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        log.error("Private Key: " + privateKeyStr);
        return rsaPrivateKey;
    }
}
