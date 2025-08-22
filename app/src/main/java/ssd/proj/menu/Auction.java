package ssd.proj.menu;

import java.io.Serializable;
import java.security.PublicKey;

public class Auction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String auctionId;
    private String name;
    private String itemDescription;
    private int auctionDuration;
    private boolean auctionStarted;
    private double startingBid;
    private double currentBid;
    private double winnerBid;
    private PublicKey auctioneer;
    private PublicKey currentLeader;
    private PublicKey winner;
    private final double reservedPrice;

    public Auction(String auctionId, String name, String itemDescription, int auctionDuration, double startingBid,
            double maxPrice, PublicKey auctioneer) {
        this.auctionId = auctionId;
        this.name = name;
        this.itemDescription = itemDescription;
        this.auctionDuration = auctionDuration;
        this.startingBid = startingBid;
        this.currentBid = 0;
        this.reservedPrice = maxPrice;
        this.auctioneer = auctioneer;
        this.auctionStarted = false;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getName() {
        return name;
    }


    public boolean isAuctionStarted() {
        return auctionStarted;
    }

    public double getStartingBid() {
        return startingBid;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public double getWinnerBid() {
        return winnerBid;
    }

    public PublicKey getAuctioneer() {
        return auctioneer;
    }

    public PublicKey getCurrentLeader() {
        return currentLeader;
    }

    public PublicKey getWinner() {
        return winner;
    }

    public double getReservedPrice() {
        return reservedPrice;
    }

    public synchronized boolean startAuction() {
        if (auctionStarted) {
            return false;
        }
        auctionStarted = true;
        return true;
    }

    public synchronized boolean placeBid(double bid, PublicKey bidder) {
        if (bid <= currentBid) {
            return false;
        }
        currentBid = bid;
        currentLeader = bidder;
        return true;
    }

    public synchronized boolean endAuction() {
        if (!auctionStarted) {
            return false;
        }
        auctionStarted = false;
        winnerBid = currentBid;
        winner = currentLeader;
        return true;
    }

}
