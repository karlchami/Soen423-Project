package replica.replica_nick.servers;

import replica.replica_nick.impl.StoreImpl;

import javax.xml.ws.Endpoint;

public class QCServer {
    public static void main(String[] args) {
        StoreImpl store = new StoreImpl("QC");
        Endpoint.publish("http://localhost:8081/QC", store);

        StoreAgent agent = new StoreAgent(store, 5001);
        agent.run();
        System.out.println("QC server is running...");
    }
}
