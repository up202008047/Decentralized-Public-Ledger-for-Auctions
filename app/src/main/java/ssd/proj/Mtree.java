package ssd.proj;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import static java.nio.charset.StandardCharsets.*;


public class Mtree {

        public String getMRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getAction());
        }
        while (count > 1) {
            ArrayList<String> currentTreeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                currentTreeLayer.add(calculateBlockHash(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = currentTreeLayer.size();
            previousTreeLayer = currentTreeLayer;
        }
        return (previousTreeLayer.size() == 1) ? previousTreeLayer.get(0) : "";
    }

        private String calculateBlockHash(String input) {
        MessageDigest digest;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(input.getBytes(UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("ERROR - HASH FUNC");
        }
        StringBuilder buffer = new StringBuilder();
        if (bytes != null) {
            for (byte b : bytes) {
                buffer.append(String.format("%02x", b));
            }
        } else {
            System.out.println("ERROR - N BYTES NULL");
            return "";
        }
        return buffer.toString();
    }

}
