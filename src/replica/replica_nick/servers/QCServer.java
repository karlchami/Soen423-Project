package replica.replica_nick.servers;

import replica.replica_nick.impl.StoreImpl;

public class QCServer {
    public static void main(String[] args) {
        StoreImpl store = new StoreImpl("QC");

        StoreAgent agent = new StoreAgent(store, 5001, 5551);
        agent.run();

        System.out.println("QC server is running...");
    }
}
