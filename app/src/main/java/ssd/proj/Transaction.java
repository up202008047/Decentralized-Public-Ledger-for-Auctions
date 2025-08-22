package ssd.proj;

import java.security.*;

public class Transaction {

    public enum Action {
        GENESIS,
        START,
        BID,
        SOLD,
        CLOSED
    }

    private final PublicKey senderKey;
    private final String action;
    private final Action type;
    private final double bidAmount;
    private final long timestamp;
    private final byte[] signedMessage;

    public Transaction(String action, Action type, double bidAmount, PublicKey pubkey, PrivateKey privkey) {
        this.senderKey = pubkey;
        this.action = action;
        this.type = type;
        this.bidAmount = bidAmount;
        this.timestamp = System.currentTimeMillis();
        this.signedMessage = CryptoUtils.sign(action, privkey);
    }

    public PublicKey getSenderKey() {
        return senderKey;
    }

    public String getAction() {
        return action;
    }

    public Action getType() {
        return type;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getSignedMessage() {
        return signedMessage;
    }

    @Override
    public String toString() {
        return "{" + type + " - \"" + action + "\" + " + bidAmount + " - " + timestamp + "}";
    }
}
