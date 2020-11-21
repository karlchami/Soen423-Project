package replica.replica_nick.servers;

import replica.replica_nick.impl.StoreImpl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StoreAgent {
    private StoreImpl store;
    private int port;

    public StoreAgent(StoreImpl store, int port) {
        this.store = store;
        this.port = port;
    }

    public void run() {
        Runnable task = this::receivePacket;
        Thread thread = new Thread(task);
        thread.start();
    }

    private void receivePacket() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                byte[] requestBuffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(requestBuffer, requestBuffer.length);
                socket.receive(request);

                String replyMessage = performRequest(new String(request.getData(), 0, request.getLength()));
                byte[] replyBytes = replyMessage.getBytes();
                DatagramPacket reply = new DatagramPacket(replyBytes, replyBytes.length, request.getAddress(), request.getPort());
                socket.send(reply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String performRequest(String request) {
        String[] requestParams = request.split("_");
        switch (requestParams[0]) {
            case "purchase":
                return purchaseItem(requestParams);
            case "find":
                return findItem(requestParams);
            case "return":
                return returnItem(requestParams);
            case "waitlist":
                return confirmWaitlist(requestParams);
            case "addwaitlist":
                return addToWaitList(requestParams);
            case "quantity":
                return store.getQuantity(requestParams[1]);
            case "price":
                return store.getPrice(requestParams[1]);
            default:
                return "Invalid request";
        }
    }

    private String purchaseItem(String[] params) {
        return store.nonLocalPurchase(params[1], params[2]);
    }

    private String findItem(String[] params) {
        return store.searchResults(params[1]);
    }

    private String returnItem(String[] params) {
        return store.nonLocalReturn(params[1], params[2], Double.parseDouble(params[3]));
    }

    private String confirmWaitlist(String[] params) {
        return store.nonLocalWaitList(params[1], params[2], Double.parseDouble(params[3]));
    }

    private String addToWaitList(String[] params) {
        return store.addToWaitList(params[1], params[2]);
    }


}
