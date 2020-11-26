package replica.replica_karl.src.backend;
import replica.replica_karl.src.Store.StoreImpl;
import replica.replica_karl.src.models.Store;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.ws.Endpoint;

public class ONServer {
    public static StoreImpl ONStoreImpl;

    public static void main(String[] args) throws IOException {
        System.out.println("ON server started...");
        ONStoreImpl = new StoreImpl(Store.ON);
        Endpoint endpoint = Endpoint.publish("http://localhost:8081/ON", ONStoreImpl);
        Runnable task = () -> {
			try {
				ONStoreImpl.receive();
			} catch (NumberFormatException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
        Thread thread = new Thread(task);
        thread.start();
}
}

