package replica.replica_karl.backend;

import replica.replica_karl.Store.StoreImpl;
import replica.replica_karl.models.Store;
import replica.replica_karl.backend.StoreAgent;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.ws.Endpoint;

public class QCServer {
    public static StoreImpl QCStoreImpl;

    public static void main(String[] args) throws IOException, NumberFormatException, ParseException {
        QCStoreImpl = new StoreImpl(Store.QC);
        Runnable task = () -> {
            try {
                QCStoreImpl.receive();
            } catch (NumberFormatException | ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        replica.replica_karl.backend.StoreAgent agent = new StoreAgent(QCStoreImpl, 4001, 4441);
        agent.run();
        System.out.println("QC server started...");
    }
}

