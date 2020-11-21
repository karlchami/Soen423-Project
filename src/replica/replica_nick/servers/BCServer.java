package replica.replica_nick.servers;

import replica.replica_nick.impl.StoreImpl;

import javax.xml.ws.Endpoint;

public class BCServer {
    public static void main(String[] args) {
        StoreImpl store = new StoreImpl("BC");
        Endpoint.publish("http://localhost:8083/BC", store);

        StoreAgent agent = new StoreAgent(store, 5003);
        agent.run();
        System.out.println("BC server is running...");
    }
}
