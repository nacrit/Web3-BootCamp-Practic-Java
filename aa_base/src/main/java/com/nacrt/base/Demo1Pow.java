package com.nacrt.base;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Demo1Pow {
    /**
     * 题目#1
     * 实践 POW， 编写程序（编程语言不限）用自己的昵称 + nonce，不断修改nonce 进行 sha256 Hash 运算：
     * 直到满足 4 个 0 开头的哈希值，打印出花费的时间、Hash 的内容及Hash值。
     * 再次运算直到满足 5 个 0 开头的哈希值，打印出花费的时间、Hash 的内容及Hash值。
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {
        long nonce = 0L, start = System.currentTimeMillis();
        while (true) {
            String message = "nacrt" + nonce++;
            String sha256HexStr = sha256(message.getBytes());
            if (isNZeroStart(4, sha256HexStr)) {
                System.out.println("用时: " + (System.currentTimeMillis() - start)
                        + "ms, hash的内容: " + message
                        + ", 4个0开头的hash值: " + sha256HexStr
                );
                break;
            }
        }
        nonce = 0L;
        start = System.currentTimeMillis();
        while (true) {
            String message = "nacrt" + nonce++;
            String sha256HexStr = sha256(message.getBytes());
            if (isNZeroStart(5, sha256HexStr)) {
                System.out.println("用时 = " + (System.currentTimeMillis() - start)
                        + "ms, hash的内容:  " + message
                        + ", 5个0开头的hash值:  " + sha256HexStr
                );
                break;
            }
        }
    }

    private static String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance("SHA-256");
        byte[] digest = instance.digest(bytes);
        return bytesToHexString(digest);
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex.length() == 1 ? "0" + hex : hex);
        }
        return hexString.toString();
    }

    private static boolean isNZeroStart(int n, String str) {
        String nZero = IntStream.range(0, n).mapToObj(i -> "0").collect(Collectors.joining());
        return str.startsWith(nZero);
    }
}
