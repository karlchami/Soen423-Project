package backend;
import Store.StoreImpl;
import models.Store;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.ws.Endpoint;

public class QCServer {
    public static StoreImpl QCStoreImpl;

    public static void main(String[] args) throws IOException {
        System.out.println("QC server started...");
        QCStoreImpl = new StoreImpl(Store.QC);
        Endpoint endpoint = Endpoint.publish("http://localhost:8080/QC", QCStoreImpl);
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
}
}

