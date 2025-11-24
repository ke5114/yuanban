package org.example.Data;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Base64;

public class WechatDecryptUtil {
    private static final Logger logger = LoggerFactory.getLogger(WechatDecryptUtil.class);
    private static final String AES_ALGORITHM = "AES/CBC/PKCS7Padding";

    static {
        // 加载BouncyCastle加密库
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 解密微信手机号
     * @param encryptedData 加密的手机号数据
     * @param sessionKey 微信返回的session_key
     * @param iv 加密向量
     * @return 解密后的JSON字符串（包含手机号）
     */
    public static String decryptPhone(String encryptedData, String sessionKey, String iv) {
        try {
            // Base64解码
            byte[] encryptedDataBytes = Base64.getDecoder().decode(encryptedData);
            byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            // 初始化加密算法
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
            AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
            params.init(new IvParameterSpec(ivBytes));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, params);

            // 解密并转字符串
            byte[] result = cipher.doFinal(encryptedDataBytes);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("微信手机号解密失败", e);
            throw new RuntimeException("手机号解密失败");
        }
    }
}
