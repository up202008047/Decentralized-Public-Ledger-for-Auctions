package ssd.proj;

import java.security.*;

public class CryptoUtils {

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("ERROR - RSA KEY GEN", e);
        }
    }

    public static byte[] sign(String message, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes());
            return signature.sign();
        } catch (Exception e) {
            System.out.println("ERROR - SIGNING TRANSACTION");
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignature(String message, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(message.getBytes());
            return sig.verify(signature);
        } catch (Exception e) {
            System.out.println("ERROR - VERIFYING SIGNATURE");
            throw new RuntimeException(e);
        }
    }
}
