package ssd.proj;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private final List<Block> blockchain;
    private final int maxTrans = 4;
    private final int difficulty = 2;

    public Blockchain() {
        this.blockchain = new ArrayList<>();
        this.blockchain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        Block genesisBlock = new Block(0, null);

        KeyPair keyPair = CryptoUtils.generateKeyPair();
        Transaction genesisTransaction = new Transaction(
                "New Blockchain",
                Transaction.Action.GENESIS,
                0, // bidAmount = 0 para transação génese
                keyPair.getPublic(),
                keyPair.getPrivate());

        genesisBlock.addTransaction(genesisTransaction);
        return genesisBlock;
    }

    public List<Block> getBlockchain() {
        return blockchain;
    }

    public Block getLatestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    public Block getBlock(int index) {
        return blockchain.get(index);
    }

    public void addBlock(Block block) {
        if (validateBlock(getLatestBlock(), block))
            this.blockchain.add(block);
        else
            System.out.println("[ERROR] Invalid Block!");
    }

    public boolean addTransaction(Transaction transaction) {
        if (!validateTransaction(transaction)) {
            System.out.println("[ERROR] Transaction not valid!");
            return false;
        }

        if (getLatestBlock().getTransactions().size() < maxTrans) {
            getLatestBlock().addTransaction(transaction);
            System.out.println(
                    "📝 [Transaction Added] to Block " + getLatestBlock().getId() + ": " + transaction.getAction());
        } else {
            getLatestBlock().mineBlock(difficulty);
            System.out.println(
                    "📦 [Block Full] Block " + getLatestBlock().getId() + " is full. Broadcasting new block...");

            Block newBlock = new Block(blockchain.size(), getLatestBlock().getHash());
            addBlock(newBlock);
            getLatestBlock().addTransaction(transaction);
            System.out.println("🧱 [New Block] Block " + newBlock.getId() + " created.");
        }

        return true;
    }

    public boolean validateTransaction(Transaction transaction) {
        return CryptoUtils.verifySignature(
                transaction.getAction(),
                transaction.getSignedMessage(),
                transaction.getSenderKey());
    }

    private boolean validateBlock(Block prevBlock, Block block) {

        if (block.getTimeStamp() <= prevBlock.getTimeStamp()) {
            System.out.println("[ERROR] Block timestamp is lower than latest!");
            return false;
        }
        if (block.getPreviousHash() != null && !block.getPreviousHash().equals(prevBlock.getHash())) {
            System.out.println("[ERROR] Block previous hash is incorrect!");
            return false;
        }
        if (block.getHash() != null && block.getHash().substring(0, difficulty)
                .equals(new String(new char[difficulty]).replace('\0', '0'))) {
            System.out.println("[ERROR] Block hash has inaccurate difficulty!");
            return false;
        }

        return true;
    }

    public boolean validateBlockchain() {
        for (Block b : this.blockchain)
            if (!b.isGenesis())
                if (!validateBlock(this.blockchain.get(b.getId() - 1), b)) {
                    System.out.println("[ERROR] Block is invalid!");
                    return false;
                }

        return true;
    }

    public int getDifficulty() {
        return difficulty;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Blockchain{\n");
        for (Block block : blockchain)
            out.append(block).append("\n");
        out.append("}");

        return out.toString();
    }
}
