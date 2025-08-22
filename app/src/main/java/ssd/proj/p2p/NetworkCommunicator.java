package ssd.proj.p2p;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ssd.proj.*;

public class NetworkCommunicator {
    private DatagramSocket socket;
    private ExecutorService executor; // Used for handling incoming messages asynchronously

    public NetworkCommunicator(int port) throws SocketException {
        socket = new DatagramSocket(port);
        //socket.setSoTimeout(5000);
        executor = Executors.newSingleThreadExecutor();
    }

    public void startListening(MessageHandler messageHandler) {
        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    byte[] buf = new byte[8192];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    System.out.println("↘ Received UDP packet from " +
                            packet.getAddress().getHostAddress() + ":" + packet.getPort() +
                            " (" + packet.getLength() + " bytes)");

                    // Deserialize the message
                    KademliaMessage message = deserialize(packet.getData());
                    // System.out.println("📩 Received message of type: " + message.getType());
                    messageHandler.handleMessageincoming(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error receiving packet: " + e.getMessage());
            }
        });
    }

    public void sendMessage(Node receiver, KademliaMessage message) {
        try {
            byte[] buf = serialize(message);
            InetAddress addr = InetAddress.getByName(receiver.getAddress());
            int port = receiver.getPort();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, port);
            socket.send(packet);
            System.out.println("↗ Sent UDP packet to " + addr.getHostAddress() + ":" + port +
                    " (" + buf.length + " bytes)");
        } catch (IOException e) {
            System.err.println("❌ NetworkCommunicator.sendMessage failed: " + e);
            e.printStackTrace();
        }
    }

    private byte[] serialize(KademliaMessage message) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        oos.flush();
        return bos.toByteArray();
    }

    private KademliaMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (KademliaMessage) ois.readObject();
    }

    public void shutdown() {
        executor.shutdownNow();
        socket.close();
    }
}
