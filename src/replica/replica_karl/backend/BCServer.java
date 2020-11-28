package replica.replica_karl.backend;
import replica.replica_karl.Store.StoreImpl;
import replica.replica_karl.models.Store;
import replica.replica_karl.backend.StoreAgent;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.ws.Endpoint;

public class BCServer {
    public static StoreImpl BCStoreImpl;

    public static void main(String[] args) throws IOException {
        BCStoreImpl = new StoreImpl(Store.BC);
        replica.replica_karl.backend.StoreAgent agent = new StoreAgent(BCStoreImpl, 4003, 4443);
        agent.run();
        System.out.println("BC server started...");
}
}


