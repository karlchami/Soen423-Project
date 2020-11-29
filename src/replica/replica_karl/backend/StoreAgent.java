package replica.replica_karl.backend;

import Models.request.Request;
import Models.response.Response;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import replica.replica_karl.Store.StoreImpl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
        Runnable rmTask = this::receiveRM;
        Thread rmThread = new Thread(rmTask);
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
                if (message.equals("received-response")) {
                    continue;
                }
                if (message.contains("FAILED")) {
                    continue;
                }
                if (message.contains("CRASHED")) {
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
                        int price = Integer.parseInt(parameters.get("price").toString());
                        String responseMessage = store.addItem(managerID, itemID, itemName, quantity, price);
                        System.out.print(responseMessage);
                        sendResponse(request, responseMessage);
                        break;

                    case "removeItem":
                        managerID = parameters.get("managerID").toString();
                        itemID = parameters.get("itemID").toString();
                        quantity = Integer.parseInt(parameters.get("quantity").toString());

                        responseMessage = store.removeItem(managerID, itemID, quantity);
                        sendResponse(request, responseMessage);
                        break;

                    case "listItemAvailability":
                        managerID = parameters.get("managerID").toString();

                        responseMessage = store.listItemAvailability(managerID);
                        sendResponse(request, responseMessage);
                        break;

                    case "purchaseItem":
                        String customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();
                        String dateOfPurchase = parameters.get("dateOfPurchase").toString();

                        responseMessage = store.purchaseItem(customerID, itemID, dateOfPurchase);
                        sendResponse(request, responseMessage);
                        break;

                    case "findItem":
                        customerID = parameters.get("customerID").toString();
                        itemName = parameters.get("itemName").toString();

                        responseMessage = store.findItem(customerID, itemName);
                        sendResponse(request, responseMessage);
                        break;

                    case "returnItem":
                        customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();
                        String dateOfReturn = parameters.get("dateOfReturn").toString();

                        responseMessage = store.returnItem(customerID, itemID, dateOfReturn);
                        sendResponse(request, responseMessage);
                        break;

                    case "exchangeItem":
                        customerID = parameters.get("customerID").toString();
                        String newItemID = parameters.get("newItemID").toString();
                        String oldItemID = parameters.get("oldItemID").toString();
                        String dateOfExchange = parameters.get("dateOfExchange").toString();

                        responseMessage = store.exchangeItem(customerID, newItemID, oldItemID, dateOfExchange);
                        sendResponse(request, responseMessage);
                        break;

                    case "addCustomerWaitlist":
                        customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();

                        store.addCustomerWaitList(customerID, itemID);
                        //sendResponse(request, responseMessage);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(Request request, String message) {
        String statusCode = "Success"; // parse from message

        Response response = new Response(
                String.valueOf(request.getSequence_id()),
                "karl",
                request.getRequest_details().getMethod_name(),
                message,
                statusCode
        );

        Gson gson = new Gson();
        String responseString = gson.toJson(response);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(4000);

            InetAddress hostName = InetAddress.getByName("132.205.95.146"); // CHANGE TO FRONT-END HOST NAME

            byte[] bytes = responseString.getBytes();
            System.out.println(responseString);
            DatagramPacket responsePacket = new DatagramPacket(bytes, bytes.length, hostName, 5555); // CHANGE TO FE PORT
            socket.send(responsePacket);

            byte[] buffer = new byte[1000];
            DatagramPacket frontEndPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(frontEndPacket);

            String frontEndReply = new String(frontEndPacket.getData(), 0, frontEndPacket.getLength());

            System.out.println(frontEndReply);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
