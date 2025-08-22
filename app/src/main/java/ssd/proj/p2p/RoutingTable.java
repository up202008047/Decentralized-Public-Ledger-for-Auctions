package ssd.proj.p2p;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RoutingTable {

    private List<Map<String, Node>> buckets; // cada node tem o seu Map, logo vamos criar uma lista de HashMaps
    private Node localNode;

    private static final int K = 20; // Typically, K is 20 in Kademlia

    private static final int BUCKET_SIZE = 256;

    public RoutingTable(Node localNode) {
        this.localNode = localNode;
        this.buckets = new ArrayList<>(BUCKET_SIZE);
        for (int i = 0; i < BUCKET_SIZE; i++) {
            this.buckets.add(new HashMap<>());
        }
    }

    public List<Map<String, Node>> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Map<String, Node>> buckets) {
        this.buckets = buckets;
    }

    public Node getLocalNode() {
        return localNode;
    }

    public void setLocalNode(Node localNode) {
        this.localNode = localNode;
    }

    public void addNode(Node node) {
        if (node == null) {
            System.err.println("❌ [addNode] Node is null. Skipping.");
            return;
        }

        byte[] id = node.getId();
        if (id == null) {
            System.err.println("❌ [addNode] Node ID is null. Cannot determine bucket.");
            return;
        }

        String idStr = Base64.getEncoder().encodeToString(id);
        // System.out.println("🔄 [addNode] Adding node with ID: " + idStr);

        int index = getBucket(id);
        // System.out.println("📌 [addNode] Bucket index for node: " + index);

        Map<String, Node> bucket = buckets.get(index);

        // System.out.println("🟢 [addNode] after bucket creation");

        // System.out.println("🟢 [addNode] before check for null: " + bucket.size());

        if (bucket == null) {
            System.err.println("❌ [addNode] Bucket at index " + index + " is null.");
            return;
        }

        // System.out.println("🟢 [addNode] aftercheck for null: " + bucket.size());

        // System.out.println("🟢 [addNode] Bucket before insert: " + bucket.size());
        try {
            bucket.put(idStr, node);
            // System.out.println("✅ [addNode] Node added to bucket " + index + " (now size:
            // " + bucket.size() + ")");
        } catch (Exception e) {
            System.err.println("❌ [addNode] Failed to add node to bucket " + index + ": " + e.getMessage());
            e.printStackTrace();
        }
        // System.out.println("✅ [addNode] Node tried to be added to bucket " + index +
        // " (now size: " + bucket.size() + ")");
    }

    public Node findNode(byte[] id) {
        String idStr = Base64.getEncoder().encodeToString(id);
        for (Map<String, Node> bucket : buckets) {
            if (bucket.containsKey(idStr)) {
                return bucket.get(idStr);
            }
        }
        return null;
    }

    public List<Node> findClosestNodes(String targetId) {
        System.out.println("🔍 [findClosestNodes] Looking for closest nodes to key: " + targetId);
        PriorityQueue<NodeDistancePair> nodeQueue = new PriorityQueue<>(Comparator.comparing(pair -> pair.distance));
        BigInteger target;
        try {
            byte[] decoded = Base64.getDecoder().decode(targetId);
            target = new BigInteger(1, decoded); // correto!
        } catch (NumberFormatException e) {
            System.err.println("❌ [findClosestNodes] Invalid targetId format: " + targetId);
            return new ArrayList<>();
        }

        for (Map<String, Node> bucket : buckets) {
            for (Node node : bucket.values()) {
                BigInteger distance = new BigInteger(1, node.getId()).xor(target);
                nodeQueue.add(new NodeDistancePair(node, distance));
            }
        }

        List<Node> closestNodes = new ArrayList<>();
        for (int i = 0; i < K && !nodeQueue.isEmpty(); i++) {
            closestNodes.add(nodeQueue.poll().node);
        }

        System.out.println("✅ [findClosestNodes] Found " + closestNodes.size() + " node(s)");
        return closestNodes;
    }

    private static class NodeDistancePair {
        Node node;
        BigInteger distance;

        NodeDistancePair(Node node, BigInteger distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    private int getBucket(byte[] id) {
        BigInteger distance = localNode.distance(id);
        return distance.bitLength() == 0 ? 0 : distance.bitLength() - 1;
    }

    public List<Node> getAllNodes() {
        List<Node> allNodes = new ArrayList<>();
        for (Map<String, Node> bucket : buckets) {
            allNodes.addAll(bucket.values());
        }
        return allNodes;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Node ").append(localNode).append(": \n");

        for (int i = 0; i < buckets.size() - 1; i++)
            result.append(buckets.get(i)).append(",");
        result.append(buckets.get(buckets.size() - 1));

        return result.toString();
    }

}
