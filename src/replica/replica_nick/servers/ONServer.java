package replica.replica_nick.servers;

import replica.replica_nick.impl.StoreImpl;

public class ONServer {
    public static void main(String[] args) {
        StoreImpl store = new StoreImpl("ON");

        StoreAgent agent = new StoreAgent(store, 5002, 5552);
        agent.run();

        System.out.println("ON server is running...");
    }
}
