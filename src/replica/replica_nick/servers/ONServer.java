package replica.replica_nick.servers;

import replica.replica_nick.impl.StoreImpl;

import javax.xml.ws.Endpoint;

public class ONServer {
    public static void main(String[] args) {
        StoreImpl store = new StoreImpl("ON");
        Endpoint.publish("http://localhost:8082/ON", store);

        StoreAgent agent = new StoreAgent(store, 5002);
        agent.run();
        System.out.println("ON server is running...");
    }
}
