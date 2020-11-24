package replica.replica_nick.servers;

import Models.request.Request;
import org.json.simple.JSONObject;
import replica.replica_nick.impl.StoreImpl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StoreAgent {
    private StoreImpl store;
    private int storePort;
    private int rmPort;

    public StoreAgent(StoreImpl store, int storePort, int rmPort) {
        this.store = store;
        this.storePort = storePort;
        this.rmPort = rmPort;
    }

    public void run() {
        Runnable storeTask = this::receivePacket;
        Runnable rmTask = this::receiveRM;

        Thread storeThread = new Thread(storeTask);
        Thread rmThread = new Thread(rmTask);

        storeThread.start();
        rmThread.start();
    }

    private void receiveRM() {
        try (DatagramSocket socket = new DatagramSocket(rmPort)) {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());

                if (message.equals("Exit")) {
                    System.out.println("Killing " + store.getStorePrefix() + " Server (RM thread)");
                    return;
                } else if (message.equals("Heartbeart")) {
                    String reply = "TRUE";
                    byte[] bytes = reply.getBytes();
                    DatagramPacket replyPacket = new DatagramPacket(bytes, bytes.length, packet.getAddress(), packet.getPort());
                    socket.send(replyPacket);
                    continue;
                }

                Request request = new Request(message);
                JSONObject parameters = request.getRequest_details().getParameters();

                switch (request.getRequest_details().getMethod_name()) {
                    case "addItem":
                        String managerID = parameters.get("managerID").toString();
                        String itemID = parameters.get("itemID").toString();
                        String itemName = parameters.get("itemName").toString();
                        int quantity = Integer.parseInt(parameters.get("quantity").toString());
                        double price = Double.parseDouble(parameters.get("price").toString());

                        store.addItem(managerID, itemID, itemName, quantity, price);
                        break;

                    case "removeItem":
                        managerID = parameters.get("managerID").toString();
                        itemID = parameters.get("itemID").toString();
                        quantity = Integer.parseInt(parameters.get("quantity").toString());

                        store.removeItem(managerID, itemID, quantity);
                        break;

                    case "listItemAvailability":
                        managerID = parameters.get("managerID").toString();

                        store.listItemAvailability(managerID);
                        break;

                    case "purchaseItem":
                        String customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();
                        String dateOfPurchase = parameters.get("dateOfPurchase").toString();

                        store.purchaseItem(customerID, itemID, dateOfPurchase);
                        break;

                    case "findItem":
                        customerID = parameters.get("customerID").toString();
                        itemName = parameters.get("itemName").toString();

                        store.findItem(customerID, itemName);
                        break;

                    case "returnItem":
                        customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();
                        String dateOfReturn = parameters.get("dateOfReturn").toString();

                        System.out.println(store.returnItem(customerID, itemID, dateOfReturn));
                        break;

                    case "exchangeItem":
                        customerID = parameters.get("customerID").toString();
                        String newItemID = parameters.get("newitemID").toString();
                        String oldItemID = parameters.get("olditemID").toString();
                        String dateOfExchange = parameters.get("dateOfExchange").toString();

                        store.exchangeItem(customerID, newItemID, oldItemID, dateOfExchange);
                        break;

                    case "addCustomerWaitlist":
                        customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();

                        store.addToWaitList(itemID, customerID);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void receivePacket() {
        try (DatagramSocket socket = new DatagramSocket(storePort)) {
            while (true) {
                byte[] requestBuffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(requestBuffer, requestBuffer.length);
                socket.receive(request);

                String requestMessage = new String(request.getData(), 0, request.getLength());

                if (requestMessage.equals("Exit")) {
                    System.out.println("Killing " + store.getStorePrefix() + " Server (store thread)");
                    return;
                }

                String replyMessage = performRequest(requestMessage);
                byte[] replyBytes = replyMessage.getBytes();
                DatagramPacket reply = new DatagramPacket(replyBytes, replyBytes.length, request.getAddress(), request.getPort());
                socket.send(reply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String performRequest(String request) {
        String[] params = request.split("_");
        switch (params[0]) {
            case "purchase":
                return store.nonLocalPurchase(params[1], params[2]);
            case "find":
                return store.searchResults(params[1]);
            case "return":
                return store.nonLocalReturn(params[1], params[2], Double.parseDouble(params[3]));
            case "waitlist":
                return store.nonLocalWaitList(params[1], params[2], Double.parseDouble(params[3]));
            case "addwaitlist":
                return store.addToWaitList(params[1], params[2]);
            case "quantity":
                return store.getQuantity(params[1]);
            case "price":
                return store.getPrice(params[1]);
            default:
                return "Invalid request";
        }
    }
}
