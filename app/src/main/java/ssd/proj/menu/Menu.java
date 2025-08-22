package ssd.proj.menu;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Menu {
    public AuctionHandler auctionHandler;
    private Scanner scanner;
    public PublicKey publicKey;
    public PrivateKey privateKey;

    public Menu(AuctionHandler auctionHandler) {
        this.auctionHandler = auctionHandler;
        this.scanner = new Scanner(System.in);
        KeyPair keyPair = generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    public void displayMenu() {
        try {
            while (true) {
                System.out.println("(1) Check Active Auctions");
                System.out.println("(2) Place Bid");
                System.out.println("(3) Create Auction");
                System.out.println("(4) My Auctions");
                System.out.println("(0) Exit");
                System.out.print("Choose an option: ");

                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine()); // Lê a linha toda e converte para int
                } catch (NumberFormatException e) {
                    System.out.println("ERROR -  Invalid input");
                    continue;
                }

                switch (choice) {
                    case 1:
                        activeAuctions();
                        break;
                    case 2:
                        placeBid();
                        break;
                    case 3:
                        createAuction();
                        break;
                    case 4:
                        myAuctions();
                        break;
                    case 0:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("ERROR - No input provided!");
        }
    }

    private void activeAuctions() {
        int i = 0;
        System.out.println("Active Auctions:");

        if (auctionHandler.getActiveAuctions().entrySet().isEmpty()) {
            System.out.println("No active auctions.");
        }

        for (Map.Entry<String, Auction> auctionPair : auctionHandler.getActiveAuctions().entrySet()) {
            System.out.println("Auction " + i + ": ");
            System.out.println("\t" + auctionPair.getValue().getName());
            System.out.println("\tStarting bid: " + auctionPair.getValue().getStartingBid());
            System.out.println("\tCurrent bid: " + auctionPair.getValue().getCurrentBid());
            i++;
        }
    }

    private void createAuction() {
        scanner.nextLine();
        System.out.print("Auction Name: ");
        String name = scanner.nextLine();

        System.out.print("Item Description: ");
        String itemDescription = scanner.nextLine();

        System.out.print("Auction Duration (in hours): ");
        int auctionDuration = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Starting Bid: $");
        double startingBid = scanner.nextDouble();

        System.out.print("Reserved Price: $");
        double reservedPrice = scanner.nextDouble();

        String auctionId = auctionHandler.createAuction(this.publicKey, this.privateKey, name, itemDescription,
                auctionDuration, startingBid, reservedPrice);
        Auction auction = new Auction(auctionId, name, itemDescription, auctionDuration, startingBid, reservedPrice,
                this.publicKey);
        System.out.println("Creating a new Auction with ID: " + auctionId);
    }

    private void placeBid() {
        int i = 0;
        int option;
        double bidAmount;

        if (auctionHandler.getActiveAuctions().entrySet().isEmpty()) {
            System.out.println("No auction to bid on! Wait for new auctions...");
        }

        System.out.println("Select your auction to bid on:");
        for (Map.Entry<String, Auction> auctionPair : auctionHandler.getActiveAuctions().entrySet()) {
            do {
                System.out.println("Auction " + i + ": ");
                System.out.println("\tName: " + auctionPair.getValue().getName());
                System.out.println("\tStarting bid: " + auctionPair.getValue().getStartingBid());
                System.out.println("\tCurrent bid: " + auctionPair.getValue().getCurrentBid());

                System.out.println("0: Back");
                System.out.println("1: Enter Auction");
                if (auctionHandler.getActiveAuctions().entrySet().size() - 1 > i) {
                    System.out.println("2: Next Auction");
                }

                option = scanner.nextInt();

                switch (option) {
                    case 0:
                        return;
                    case 1:
                        System.out.print("Enter bid amount (or 0 to go back): ");
                        bidAmount = scanner.nextDouble();

                        if (bidAmount == 0)
                            break;

                        String response = auctionHandler.placeBid(auctionPair.getKey(), bidAmount, this.publicKey,
                                this.privateKey);
                        switch (response) {
                            case ("LOW_BID"):
                                System.out.println("[ERROR] The bid amount is lower than the current or starting bid.");
                                break;
                            case ("SUCCESSFUL_BID"):
                                System.out.println("Bid was placed.");
                                break;
                            case ("UNSUCCESSFUL_BID"):
                                System.out.println("[ERROR] The bidder can't bid in his own auction.");
                                break;
                            default:
                                System.out.println("[ERROR] Could not process bid.");
                                System.out.println("response: " + response);
                                break;
                        }
                    case 2:
                        i++;
                        break;
                    default:
                        System.out.println("[ERROR] Invalid input, try again.");
                }
            } while (option != 1 && option != 2);
        }
    }

    private void myAuctions() {
        int option, list = 1;

        do {
            System.out.println("(0) Back");
            System.out.println("(1)Active Auctions");
            System.out.println("(2) All Auctions");
            option = scanner.nextInt();

            switch (option) {
                case 0:
                    return;
                case 1:
                    int j = 0;
                    if (auctionHandler.getActiveAuctions().entrySet().isEmpty()) {
                        System.out.println("No active auctions.");
                        break;
                    }
                    for (Map.Entry<String, Auction> auctionPair : auctionHandler.getActiveAuctions().entrySet()) {
                        do {
                            if (auctionPair.getValue().getAuctioneer().equals(this.publicKey)) {
                                System.out.println("Auction " + j + ": ");
                                System.out.println("\tName: " + auctionPair.getValue().getName());
                                System.out.println("\tStarting bid: " + auctionPair.getValue().getStartingBid());
                                System.out.println("\tCurrent bid: " + auctionPair.getValue().getCurrentBid());

                                System.out.println("(0) Back");
                                System.out.println("(1) Close Auction");
                                if (auctionHandler.getActiveAuctions().entrySet().size() - 1 > j) {
                                    System.out.println("2: Next Auction");
                                }

                                list = scanner.nextInt();
                                switch (list) {
                                    case 0:
                                        return;
                                    case 1:
                                        closeAuction(auctionPair.getValue().getAuctionId());
                                    case 2:
                                        break;
                                    default:
                                        System.out.println("[ERROR] Invalid input, try again.");
                                }
                            }
                        } while (list != 1 && list != 2);
                        j++;
                    }
                    break;
                case 2:
                    int k = 0;
                    for (Map.Entry<String, Auction> auctionPair : auctionHandler.getAuctions().entrySet()) {
                        if (auctionPair.getValue().getAuctioneer().equals(this.publicKey)) {
                            System.out.println("Auction " + k + ": ");
                            System.out.println("Status " + auctionPair.getValue().isAuctionStarted());
                        }
                        k++;
                    }
                    break;
                default:
                    System.out.println("[ERROR] Invalid input, try again.");
                    break;
            }
        } while (option != 1);
    }

    private void closeAuction(String id) {
        auctionHandler.endAuction(id);
        System.out.println("Ending auction " + id);
    }

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keypairgen = KeyPairGenerator.getInstance("RSA");
            keypairgen.initialize(1024);
            return keypairgen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("ERROR- No algorithm found");
        }
        return null;
    }
}
