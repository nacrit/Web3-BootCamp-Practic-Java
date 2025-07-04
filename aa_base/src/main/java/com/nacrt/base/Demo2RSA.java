package com.nacrt.base;

import java.security.*;
import java.util.Base64;

public class Demo2RSA {
    /**
     * 实践非对称加密 RSA（编程语言不限）：
     * 先生成一个公私钥对
     * 用私钥对符合 POW 4 个 0 开头的哈希值的 “昵称 + nonce” 进行私钥签名
     * 用公钥验证
     * 提交程序你的 Github 链接
     */
    public static void main(String[] args) throws Exception {
        // 1. 生成公私密
        KeyPair keyPair = generateRsaKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        // 2. 生成 POW 4 个 0 开头的哈希值的 “昵称 + nonce”
        String powInput = performPOW();
        System.out.println("powInput: " + powInput);

        // 3. 私钥签名，需要 明文和私钥
        String signedData = sign(powInput.getBytes(), privateKey);
        System.out.println("base64密文: " + signedData);

        // 4. 公钥验证，需要 明文、密文、公钥
        boolean isVerified = verify(powInput.getBytes(), signedData, publicKey);
        System.out.println("验签结果: " + isVerified);

    }


    private static String performPOW() throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance("SHA-256");
        long nonce = 0L;
        while (true) {
            String message = "nacrt" + nonce++;
            if (bytesToHexString(instance.digest(message.getBytes())).startsWith("0000")) {
                return message;
            }
        }
    }


    /**
     * 字节数组转hex字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex.length() == 1 ? "0" + hex : hex);
        }
        return hexString.toString();
    }


    /**
     * 生成 RSA 公私钥对
     */
    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    /**
     * 使用私钥对数据进行签名
     */
    public static String sign(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 使用公钥验证签名
     */
    public static boolean verify(byte[] data, String signature, PublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(Base64.getDecoder().decode(signature));
    }


}
