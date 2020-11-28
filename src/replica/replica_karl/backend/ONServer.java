package replica.replica_karl.backend;
import replica.replica_karl.Store.StoreImpl;
import replica.replica_karl.models.Store;
import replica.replica_karl.backend.StoreAgent;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.ws.Endpoint;

public class ONServer {
    public static StoreImpl ONStoreImpl;

    public static void main(String[] args) throws IOException {
        ONStoreImpl = new StoreImpl(Store.ON);
        replica.replica_karl.backend.StoreAgent agent = new StoreAgent(ONStoreImpl, 4002, 4442);
        agent.run();
        System.out.println("ON server started...");
}
}

