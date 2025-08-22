package ssd.proj.p2p;

import java.io.*;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

import ssd.proj.Blockchain;
import ssd.proj.Transaction;
import ssd.proj.menu.*;

public class MessageHandler {
    private final NetworkCommunicator communicator;
    private final RoutingTable routingTable;
    private final Blockchain blockchain;
    private final AuctionHandler auctionHandler;

    public MessageHandler(NetworkCommunicator communicator, RoutingTable routingTable, Blockchain blockchain,
            AuctionHandler auctionHandler) {
        this.communicator = communicator;
        this.routingTable = routingTable;
        this.blockchain = blockchain;
        this.auctionHandler = auctionHandler;
    }

    public void handleMessageincoming(KademliaMessage message) throws IOException {
        System.out.println("📩 Received message of type: " + message.getType());
        switch (message.getType()) {
            case PING:
                handlePing(message);
                break;
            case PONG:
                handlePong(message);
                break;
            case STORE:
                handleStore(message);
                break;
            case FIND_NODE:
                handleFindNode(message);
                break;
            case FIND_VALUE:
                handleFindValue(message);
                break;
            case RESPONSE:
                handleResponse(message);
                break;
            case END_AUCTION:
                handleEndAuction(message);
                break;
            case BID:
                handleBid(message);
                break;

            default:
                System.out.println("Unknown message type received");
        }
    }

    private void handleBid(KademliaMessage message) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(message.getData());
            ObjectInputStream ois = new ObjectInputStream(bis);
            double bidAmount = (double) ois.readObject();
            PublicKey bidder = (PublicKey) ois.readObject();
            String auctionId = message.getKey();

            Auction auction = auctionHandler.getAuctions().get(auctionId);
            if (auction != null && auction.isAuctionStarted()) {
                boolean success = auction.placeBid(bidAmount, bidder);
                if (success) {
                    System.out.println("✅ [handleBid] Received and applied bid of " + bidAmount + " on auction "
                            + auction.getName());
                }
            } else {
                System.out.println("❌ [handleBid] Auction not found or not active.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ [handleBid] Failed to deserialize bid: " + e.getMessage());
        }
    }

    private void handleEndAuction(KademliaMessage message) {
        String auctionId = message.getKey();
        Auction auction = auctionHandler.getAuctions().get(auctionId);
        if (auction != null) {
            auction.endAuction(); // ou remove, se quiser eliminar completamente
            System.out.println("🛑 Auction [" + auction.getName() + "] ended via broadcast.");
        } else {
            System.out.println("❗ Received END_AUCTION for unknown auction ID: " + auctionId);
        }
    }

    public void pingAll() {
        KademliaMessage ping_message = new KademliaMessage(KademliaMessage.MessageType.PING,
                routingTable.getLocalNode().getId());
        for (Node node : routingTable.getAllNodes()) {
            if (node.isStatus()) {
                System.out.println("Sent PING to node: " + node.getAddress());
                communicator.sendMessage(node, ping_message);
            } else {
                System.out.println("Node is shut down: " + node.getAddress());
                node.shutdown();
            }
        }
    }

    private void handlePing(KademliaMessage message) throws IOException {
        Node senderNode = new Node(message.getSenderIp(), message.getSenderPort());
        routingTable.addNode(senderNode);

        KademliaMessage response = new KademliaMessage(KademliaMessage.MessageType.PONG,
                routingTable.getLocalNode().getId());
        communicator.sendMessage(senderNode, response);

        System.out.println("Handled PING from node at " + senderNode.getAddress() + ":" + senderNode.getPort());
    }

    private void handlePong(KademliaMessage message) {
        Node senderNode = new Node(message.getSenderIp(), message.getSenderPort());
        routingTable.addNode(senderNode);
        System.out.println("Received PONG from node at " + senderNode.getAddress() + ":" + senderNode.getPort());
    }

    private void handleStore(KademliaMessage message) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(message.getData());
            ObjectInputStream in = new ObjectInputStream(bis);
            Object obj = in.readObject();
            if (obj instanceof Transaction transaction) {
                blockchain.addTransaction(transaction);
                System.out.println("📜 Stored transaction from " + message.getSenderIp());
            } else if (obj instanceof Auction auction) {
                // Atualiza localmente os leilões recebidos
                System.out.println("📦 Received auction: " + auction.getName());
                auctionHandler.getAuctions().put(auction.getAuctionId(), auction);
                System.out.println(
                        "✅ Auction stored locally: " + auction.getName() + " [ID: " + auction.getAuctionId() + "]");

            }

            System.out.println("Stored transaction from node " + message.getSenderIp());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to store data: " + e.getMessage());
        }
    }

    private void handleFindNode(KademliaMessage message) {
        System.out.println(
                "🔍 handleFindNode: preparing response to " + message.getSenderIp() + ":" + message.getSenderPort());
        Node senderNode = new Node(message.getSenderIp(), message.getSenderPort());

        routingTable.addNode(senderNode);

        List<Node> closestNodes = routingTable.findClosestNodes(message.getKey());

        // 1) Serializar NÓS num bloco isolado
        byte[] data;
        try {
            System.out.println("🔧 About to serialize " + closestNodes.size() + " node(s)...");
            data = serializeNodeList(closestNodes);
            System.out.println("📦 Serialized node list size: " + data.length + " bytes");
        } catch (IOException e) {
            System.err.println("❌ SERIALIZATION FAILED: " + e.getMessage());
            e.printStackTrace();
            return; // sem dados não vale a pena continuar
        }

        KademliaMessage response = new KademliaMessage(
                KademliaMessage.MessageType.RESPONSE,
                routingTable.getLocalNode().getId());
        response.setData(data);
        response.setSuccess(true);
        response.setSenderIp(routingTable.getLocalNode().getAddress());
        response.setSenderPort(routingTable.getLocalNode().getPort());

        System.out.println("↗ About to send RESPONSE UDP packet...");
        communicator.sendMessage(senderNode, response);
        System.out.println("📤 RESPONSE sent to client at "
                + senderNode.getAddress() + ":" + senderNode.getPort());
        System.out.println("Routing Table:");
        // System.out.println(routingTable);
        System.out.println(routingTable.getAllNodes());

    }

    private void handleFindValue(KademliaMessage message) {
        byte[] value = routingTable.getLocalNode().getStore().get(message.getKey());
        Node senderNode = new Node(message.getSenderIp(), message.getSenderPort());
        if (value != null) {
            KademliaMessage response = new KademliaMessage(KademliaMessage.MessageType.RESPONSE,
                    routingTable.getLocalNode().getId());
            response.setData(value);
            response.setSuccess(true);
            communicator.sendMessage(senderNode, response);
        } else {
            handleFindNode(message); // fallback to return closest nodes
        }
    }

    private void handleResponse(KademliaMessage message) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(message.getData());
            ObjectInputStream ois = new ObjectInputStream(bis);
            List<Node> nodes = (List<Node>) ois.readObject();

            for (Node node : nodes) {
                routingTable.addNode(node);
            }

            System.out.println("✅ Updated routing table with nodes from RESPONSE.");
            System.out.println(routingTable.getAllNodes());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Failed to handle RESPONSE: " + e.getMessage());
        }
    }

    private byte[] serializeNodeList(List<Node> nodes) throws IOException {
        System.out.println("🔧 serializeNodeList: trying to serialize " + nodes.size() + " node(s)");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(nodes);
        oos.flush();
        return bos.toByteArray();
    }

    public void bootstrap(Node bootstrapNode) {
        KademliaMessage findMessage = new KademliaMessage(
                KademliaMessage.MessageType.FIND_NODE,
                routingTable.getLocalNode().getId());
        findMessage.setKey(Base64.getEncoder().encodeToString(routingTable.getLocalNode().getId()));
        findMessage.setSenderIp(routingTable.getLocalNode().getAddress());
        findMessage.setSenderPort(routingTable.getLocalNode().getPort());

        communicator.sendMessage(bootstrapNode, findMessage);
        System.out.println("Sent FIND_NODE to bootstrap node: " + bootstrapNode.getAddress());
    }
}
