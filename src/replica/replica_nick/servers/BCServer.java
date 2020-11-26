package replica.replica_nick.servers;

import replica.replica_nick.impl.StoreImpl;

public class BCServer {
    public static void main(String[] args) {
        StoreImpl store = new StoreImpl("BC");

        StoreAgent agent = new StoreAgent(store, 5003, 5553);
        agent.run();

        System.out.println("BC server is running...");
    }
}
