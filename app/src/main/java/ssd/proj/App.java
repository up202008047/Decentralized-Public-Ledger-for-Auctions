package ssd.proj;

import ssd.proj.menu.*;
import ssd.proj.p2p.*;

import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        try {
            String mode = args.length > 0 ? args[0] : "client";
            int port = mode.equalsIgnoreCase("bootstrap") ? 50000 : getFreePort();

            Node node = mode.equalsIgnoreCase("bootstrap")
                    ? new BootstrapNode("127.0.0.1", port)
                    : new Node("127.0.0.1", port);

            RoutingTable routingTable = new RoutingTable(node);
            NetworkCommunicator communicator = new NetworkCommunicator(port);
            Blockchain blockchain = node.getBlockchain();
            AuctionHandler auctionHandler = new AuctionHandler(communicator, routingTable);
            MessageHandler handler = new MessageHandler(communicator, routingTable, blockchain, auctionHandler);

            communicator.startListening(handler);

            if (mode.equalsIgnoreCase("bootstrap")) {
                System.out.println("🟢 Bootstrap node ready on port " + port + ".");
                while (true)
                    Thread.sleep(1000);
            } else {
                System.out.println("🟢 Client node started on port " + port + ".");
                Node bootstrap = new Node("127.0.0.1", 50000);
                handler.bootstrap(bootstrap);

                // Abre menu
                Menu menu = new Menu(auctionHandler);

                menu.displayMenu();

            }

        } catch (SocketException e) {
            System.err.println("❌ Error initializing network: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("❌ Interrupted: " + e.getMessage());
        }
    }

    // Utilitário para gerar porta aleatória entre 50001 e 59999
    private static int getFreePort() {
        return 50001 + (int) (Math.random() * 9999);
    }
}
