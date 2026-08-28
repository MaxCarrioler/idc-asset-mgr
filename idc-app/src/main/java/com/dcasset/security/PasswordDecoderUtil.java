package com.dcasset.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 登录密码解密工具：与前端 CryptoJS 约定一致
 * （AES/CFB/NoPadding，key 与 iv 相同，密文为 Base64）
 */
public final class PasswordDecoderUtil {

    private PasswordDecoderUtil() {
    }

    public static String decrypt(String encodedPassword, String key) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return null;
        }
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new IvParameterSpec(keyBytes));
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(encodedPassword));
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("密码解密失败");
        }
    }
}
