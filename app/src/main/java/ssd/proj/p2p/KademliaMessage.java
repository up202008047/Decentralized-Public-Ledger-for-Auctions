package ssd.proj.p2p;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;

public class KademliaMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        PING, PONG, STORE, FIND_NODE, FIND_VALUE, RESPONSE, END_AUCTION, BID
    }

    private MessageType type;
    private byte[] senderId; 
    private BigInteger rpcId; 
    private String key; 
    private byte[] data; 
    private boolean success; 

    // Constructor
    public KademliaMessage(MessageType type, byte[] senderId) {
        this.type = type;
        this.senderId = senderId;
        this.rpcId = new BigInteger(160, new SecureRandom()); // Generate a quasi-random 160-bit identifier
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getSenderId() {
        return senderId;
    }

    public void setSenderId(byte[] senderId) {
        this.senderId = senderId;
    }

    public BigInteger getRpcId() {
        return rpcId;
    }

    public void setRpcId(BigInteger rpcId) {
        this.rpcId = rpcId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    private String senderIp;
    private int senderPort;

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public void setSenderPort(int senderPort) {
        this.senderPort = senderPort;
    }

    public static byte[] serialize(KademliaMessage message) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(message);
        out.flush();
        return bos.toByteArray();
    }

    public static KademliaMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (KademliaMessage) in.readObject();
    }
}
