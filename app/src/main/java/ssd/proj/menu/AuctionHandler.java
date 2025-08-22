package ssd.proj.menu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ssd.proj.Blockchain;
import ssd.proj.Transaction;
import ssd.proj.p2p.*;

public class AuctionHandler {

    private Map<String, Auction> auctions; // Map to store all the ongoing auctions
    private NetworkCommunicator communicator;
    private RoutingTable routingTable;

    public AuctionHandler(NetworkCommunicator communicator, RoutingTable routingTable) {
        this.auctions = new ConcurrentHashMap<>();
        this.communicator = communicator;
        this.routingTable = routingTable;
    }

    public String createAuction(PublicKey auctioneer, PrivateKey privKey, String name, String itemDescription,
            int auctionDuration, double startingBid, double reservedPrice) {
        String auctionId = String.valueOf(Math.random()).substring(2);
        Auction auction = new Auction(auctionId, name, itemDescription, auctionDuration, startingBid, reservedPrice,
                auctioneer);
        auctions.put(auctionId, auction);

        auction.startAuction();

        String action = "Auction Started - " + name + "," + itemDescription;
        Transaction startTransaction = new Transaction("Auction started", Transaction.Action.START, reservedPrice,
                auctioneer, privKey);
        Blockchain blockchain = routingTable.getLocalNode().getBlockchain();
        blockchain.addTransaction(startTransaction);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(auction); // Serializa o leilão
            byte[] auctionData = bos.toByteArray();

            KademliaMessage createMessage = new KademliaMessage(KademliaMessage.MessageType.STORE,
                    routingTable.getLocalNode().getId());
            createMessage.setKey(auctionId); // ID do leilão
            createMessage.setData(auctionData);
            createMessage.setSenderIp(routingTable.getLocalNode().getAddress());
            createMessage.setSenderPort(routingTable.getLocalNode().getPort());
            System.out.println("📤 Broadcasting auction:");
            System.out.println("↪ ID: " + auctionId);
            System.out.println("↪ Name: " + auction.getName());
            System.out.println("↪ Data size: " + auctionData.length + " bytes");
            System.out.println("=== Blockchain Atual ===");
            System.out.println(routingTable.getLocalNode().getBlockchain());

            broadcastMessage(createMessage);
        } catch (IOException e) {
            System.err.println("❌ [createAuction] Failed to broadcast auction: " + e.getMessage());
        }

        return auctionId;
    }

    public synchronized String placeBid(String auctionId, double bid, PublicKey bidder, PrivateKey privKey) {
        Auction auction = auctions.get(auctionId);
        if (auction.isAuctionStarted() && !bidder.equals(auction.getAuctioneer())) {
            if (bid <= auction.getCurrentBid()) {
                return "LOW_BID";
            }
            auction.placeBid(bid, bidder);
            propagateBid(auctionId, bid, bidder);
            String transactionDetails = createTransactionDetails(bidder, "Bid placed");
            Transaction bidTransaction = new Transaction("User bid " + bid, Transaction.Action.BID, bid, bidder,
                    privKey);
            Blockchain blockchain = routingTable.getLocalNode().getBlockchain();
            blockchain.addTransaction(bidTransaction);
            return "SUCCESSFUL_BID";
        } else if (bidder.equals(auction.getAuctioneer())) {
            return "UNSUCCESSFUL_BID";
        }
        return "UNAVAILABLE";
    }

    private void broadcastMessage(KademliaMessage message) {
        String localIp = routingTable.getLocalNode().getAddress();
        int localPort = routingTable.getLocalNode().getPort();

        for (Node node : routingTable.getAllNodes()) {
            // Evita enviar para o próprio
            if (!node.getAddress().equals(localIp) || node.getPort() != localPort) {
                communicator.sendMessage(node, message);
            }
        }
    }

    private void propagateBid(String auctionId, double bid, PublicKey bidder) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(bid);
            oos.writeObject(bidder);
            byte[] bidData = bos.toByteArray();

            KademliaMessage bidMessage = new KademliaMessage(KademliaMessage.MessageType.BID,
                    routingTable.getLocalNode().getId());
            bidMessage.setKey(auctionId);
            bidMessage.setData(bidData);
            bidMessage.setSuccess(true);

            List<Node> nodes = routingTable.getAllNodes();
            for (Node node : nodes) {
                communicator.sendMessage(node, bidMessage);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to propagate.");
            throw new RuntimeException(e);
        }
    }

    public String endAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);
        PrivateKey privKey = routingTable.getLocalNode().getPrivKey();

        if (auction.endAuction()) {
            Blockchain blockchain = routingTable.getLocalNode().getBlockchain();
            if (auction.getCurrentBid() < auction.getReservedPrice()) {
                auction.endAuction();
                propagateAuctionEnd(auction);
                Transaction trans = new Transaction("Auction closed.", Transaction.Action.CLOSED, 0,
                        auction.getAuctioneer(), privKey);

                blockchain.addTransaction(trans);
                return "OFF";
            }
            auction.endAuction();
            propagateAuctionEnd(auction);
            Transaction trans = new Transaction("Auction closed, item sold.", Transaction.Action.SOLD, 0,
                    auction.getAuctioneer(), privKey);

            blockchain.addTransaction(trans);
            return "SOLD";
        }
        return "ON";
    }

    private void propagateAuctionEnd(Auction auction) {
        KademliaMessage endAuction = new KademliaMessage(KademliaMessage.MessageType.END_AUCTION,
                routingTable.getLocalNode().getId());
        endAuction.setKey(auction.getAuctionId());

        String messageWinner = "Auction Ended. Congratulations: " + auction.getWinner() + "with bid"
                + auction.getWinnerBid();
        endAuction.setData(messageWinner.getBytes());
        endAuction.setSuccess(true);

        List<Node> nodes = routingTable.getAllNodes();
        for (Node node : nodes) {
            communicator.sendMessage(node, endAuction);
        }
    }

    private String createTransactionDetails(PublicKey publicKey, String description) {
        return "{publicKey: \"" + publicKey.toString() + "\", description: \"" + description + "\"}";
    }

    public Map<String, Auction> getAuctions() {
        return auctions;
    }

    public Map<String, Auction> getActiveAuctions() {
        Map<String, Auction> active = new ConcurrentHashMap<>();
        for (Map.Entry<String, Auction> auction : auctions.entrySet()) {
            if (auction.getValue().isAuctionStarted()) {
                active.put(auction.getValue().getAuctionId(), auction.getValue());
            }
        }
        return active;
    }
}
