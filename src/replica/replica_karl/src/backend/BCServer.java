package replica.replica_karl.src.backend;
import replica.replica_karl.src.Store.StoreImpl;
import replica.replica_karl.src.models.Store;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.ws.Endpoint;

public class BCServer {
    public static StoreImpl BCStoreImpl;

    public static void main(String[] args) throws IOException {
        System.out.println("BC server started...");
        BCStoreImpl = new StoreImpl(Store.BC);
        Endpoint endpoint = Endpoint.publish("http://localhost:8082/BC", BCStoreImpl);
        Runnable task = () -> {
			try {
				BCStoreImpl.receive();
			} catch (NumberFormatException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
        Thread thread = new Thread(task);
        thread.start();
}
}


