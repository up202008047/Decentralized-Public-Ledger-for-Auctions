package ssd.proj;

import ssd.proj.Mtree;

import java.util.ArrayList;
import java.util.Date;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.checkerframework.checker.units.qual.t;

public class Block {
    private String hash;
    private final String previousHash;
    private ArrayList<Transaction> transactions;
    private long timeStamp;
    private int nonce;
    private final int id;
    private String mroot;
    private final Mtree mtree;

    public Block(int id, String previousHash) {
        this.transactions = new ArrayList<>();
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = null;
        this.nonce = 0;
        this.id = id;
        this.mtree = new Mtree();
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public void setData(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getId() {
        return id;
    }

    public boolean isGenesis() {
        return id == 0;
    }

    public String calcularBlockHash() {
        String toHash = previousHash + timeStamp + nonce + mroot;
        MessageDigest mdigest;
        byte[] bytes = null;
        try {
            mdigest = MessageDigest.getInstance("SHA-256");
            bytes = mdigest.digest(toHash.getBytes(UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("ERROR - HASH FUNCTION");

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

public String mineBlock(int difficulty) {
    System.out.println("⛏️  [Mining] Mining block " + id + "...");
    this.mroot = mtree.getMRoot(transactions);
    String prefixString = new String(new char[difficulty]).replace('\0', '0');
    hash = calcularBlockHash();
    while (!hash.substring(0, difficulty).equals(prefixString)) {
        nonce++;
        hash = calcularBlockHash();
    }
    System.out.println("✅ [Mining Complete] Block " + id + " mined with hash: " + hash);
    return hash;
}


    @Override
    public String toString() {
        return "Block nr." + id + " {\n" +
                "\thash='" + hash + "',\n" +
                "\tpreviousHash='" + previousHash + "',\n" +
                "\ttransactions=" + transactions + ",\n" +
                "\ttimeStamp=" + timeStamp + ",\n" +
                "\tnonce=" + nonce + "\n" +
                '}';
    }

}