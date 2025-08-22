package ssd.proj.p2p;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jcajce.provider.digest.SHA3;

import ssd.proj.Blockchain;

public class Node implements Serializable {
    private static final long serialVersionUID = 1L;

    // SERIALIZÁVEIS
    private byte[] id;
    private PublicKey pubKey;
    private String address;
    protected int port;

    // TRANSIENTES (não serializáveis ou não necessários)
    private transient Map<String, byte[]> store;
    private transient PrivateKey privKey;
    private transient KeyPair keypair;
    private transient Blockchain blockchain;
    private transient boolean status;
    private transient int influence;
    private transient int nonce;

    // CONSTRUTOR
    public Node(String address, int port) {
        this.keypair = generateKeyPair();
        this.pubKey = keypair.getPublic();
        this.privKey = keypair.getPrivate();
        this.id = generateNodeId(this.pubKey);
        this.address = address;
        this.port = port;
        this.influence = 0;
        this.store = new HashMap<>();
        this.blockchain = new Blockchain();
        this.status = true;
        this.nonce = challenger(pubKey.getEncoded());
    }

    // GETTERS
    public byte[] getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public int getPort() {
        return port;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public boolean isStatus() {
        return status;
    }

    public PrivateKey getPrivKey() {
        return this.privKey;
    }

    public Map<String, byte[]> getStore() {
        return store;
    }

    // SETTERS / AÇÕES
    public void shutdown() {
        status = false;
        System.out.println("Node at " + address + ":" + port + " is shutting down.");
    }

    public void restart() {
        status = true;
        System.out.println("Node at " + address + ":" + port + " is restarting.");
    }

    public void storeData(String key, byte[] data) {
        store.put(key, data);
    }

    public byte[] retrieveData(String key) {
        return store.get(key);
    }

    public void updateBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public void increaseInfluence() {
        this.influence++;
    }

    public void decreaseInfluence() {
        this.influence--;
    }

    public boolean isBootstrapNode() {
        return this.port == 50000;
    }

    // ID E DISTÂNCIA
    public byte[] generateNodeId(PublicKey pubKey) {
        SHA3.DigestSHA3 digest = new SHA3.Digest256();
        return digest.digest(pubKey.getEncoded());
    }

    public BigInteger distance(byte[] other) {
        return new BigInteger(1, id).xor(new BigInteger(1, other));
    }

    // CHALLENGER (Proof-of-Work leve)
    private static final int difficulty = 2;

    public static int challenger(byte[] data) {
        MessageDigest algorithm;
        try {
            algorithm = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA3-256 algorithm not available", e);
        }

        int nonce = 0;
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
        buffer.put(data);

        while (true) {
            buffer.putInt(data.length, nonce);
            byte[] hash = algorithm.digest(buffer.array());
            if (startsWithNZeroBits(hash, difficulty)) return nonce;
            nonce++;
        }
    }

    private static boolean startsWithNZeroBits(byte[] array, int n) {
        StringBuilder buffer = new StringBuilder();
        for (byte b : array) buffer.append(String.format("%02x", b));
        return buffer.substring(0, n).equals("0".repeat(n));
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keypairgen = KeyPairGenerator.getInstance("RSA");
            keypairgen.initialize(1024);
            return keypairgen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[ERROR] No algorithm found!");
            return null;
        }
    }

    // toString() — para debug
    @Override
    public String toString() {
        return "Node " + Base64.getEncoder().encodeToString(id) + ":\n" +
                "\tIP address: " + address + "\n" +
                "\tPort: " + port + "\n";
    }
}
