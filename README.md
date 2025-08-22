# Decentralized-Public-Ledger-for-Auctions

The report presents the design and implementation of a secure, decentralized auction platform based on a public blockchain and a structured P2P overlay network. The system supports English style auctions with tamper-proof transaction logging and resilient communication using S/Kademlia. Each operation such as auction creation, bidding, and closing is stored in the blockchain using cryptographically signed transactions, ensuring non-repudiation and transparency. The architecture was designed with modularity in mind, enabling future integration of Proof-of Reputation and advanced trust mechanisms. This work demonstrates the feasibility of building trustworthy distributed systems for sensitive operations like auctions.

To run the project:

1 -> ./gradlew clean build

2 -> ./run.sh bootstrap (only 1)

3 -> ./run.sh client 
