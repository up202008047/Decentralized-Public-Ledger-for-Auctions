package ssd.proj.p2p;

public class Contact {
    private final String nodeId;
    private final String ipAddress;
    private final int udpPort;

    public String getNodeId() {
        return nodeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public Contact(String nodeId, String ipAddress, int udpPort) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.udpPort = udpPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Contact contact = (Contact) o;

        return nodeId.equals(contact.nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }

}
