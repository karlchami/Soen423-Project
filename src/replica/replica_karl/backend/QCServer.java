package replica.replica_karl.backend;
import replica.replica_karl.Store.StoreImpl;
import replica.replica_karl.models.Store;
import replica.replica_karl.backend.StoreAgent;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.ws.Endpoint;

public class QCServer {
    public static StoreImpl QCStoreImpl;

    public static void main(String[] args) throws IOException {
        QCStoreImpl = new StoreImpl(Store.QC);
        replica.replica_karl.backend.StoreAgent agent = new StoreAgent(QCStoreImpl, 4001, 4441);
        agent.run();
        System.out.println("QC server started...");
}
}

