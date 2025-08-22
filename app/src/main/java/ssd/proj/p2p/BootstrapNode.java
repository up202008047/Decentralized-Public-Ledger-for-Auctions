package ssd.proj.p2p;

public class BootstrapNode extends Node {
    public BootstrapNode(String address, int port) {
        super(address, port);
        this.port = 50000;
    }

}
