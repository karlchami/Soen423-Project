package replica.replica_nick.servers;

import Models.request.Request;
import Models.response.Response;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import replica.replica_nick.impl.StoreImpl;

import java.net.*;

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
                boolean respond = true;

                String message = new String(packet.getData(), 0, packet.getLength());

                if (message.equals("Exit")) {
                    String reply = "Killing " + store.getStorePrefix() + " Server (RM thread)";
                    byte[] bytes = reply.getBytes();
                    DatagramPacket replyPacket = new DatagramPacket(bytes, bytes.length, packet.getAddress(), packet.getPort());
                    socket.send(replyPacket);
                    System.out.println(reply);
                    return;
                } else if (message.equals("Heartbeat")) {
                    String reply = "TRUE";
                    byte[] bytes = reply.getBytes();
                    DatagramPacket replyPacket = new DatagramPacket(bytes, bytes.length, packet.getAddress(), packet.getPort());
                    socket.send(replyPacket);
                    continue;
                }

                if (message.charAt(0) == 'x') {
                    message = message.substring(1);
                    respond = false;
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
                        if (respond) {
                            sendResponse(request, responseMessage);
                        }
                        break;

                    case "removeItem":
                        managerID = parameters.get("managerID").toString();
                        itemID = parameters.get("itemID").toString();
                        quantity = Integer.parseInt(parameters.get("quantity").toString());

                        responseMessage = store.removeItem(managerID, itemID, quantity);
                        if (respond) {
                            sendResponse(request, responseMessage);
                        }
                        break;

                    case "listItemAvailability":
                        managerID = parameters.get("managerID").toString();

                        responseMessage = store.listItemAvailability(managerID);
                        if (respond) {
                            sendResponse(request, responseMessage);
                        }
                        break;

                    case "purchaseItem":
                        String customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();
                        String dateOfPurchase = parameters.get("dateOfPurchase").toString();

                        responseMessage = store.purchaseItem(customerID, itemID, dateOfPurchase);
                        if (respond) {
                            sendResponse(request, responseMessage);
                        }
                        break;

                    case "findItem":
                        customerID = parameters.get("customerID").toString();
                        itemName = parameters.get("itemName").toString();

                        responseMessage = store.findItem(customerID, itemName);
                        if (respond) {
                            sendResponse(request, responseMessage);
                        }
                        break;

                    case "returnItem":
                        customerID = parameters.get("customerID").toString();
                        itemID = parameters.get("itemID").toString();
                        String dateOfReturn = parameters.get("dateOfReturn").toString();

                        responseMessage = store.returnItem(customerID, itemID, dateOfReturn);
                        if (respond) {
                            sendResponse(request, responseMessage);
                        }
                        break;

                    case "exchangeItem":
                        customerID = parameters.get("customerID").toString();
                        String newItemID = parameters.get("newItemID").toString();
                        String oldItemID = parameters.get("oldItemID").toString();
                        String dateOfExchange = parameters.get("dateOfExchange").toString();

                        responseMessage = store.exchangeItem(customerID, newItemID, oldItemID, dateOfExchange);
                        if (respond) {
                            sendResponse(request, responseMessage);
                        }
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

    private void sendResponse(Request request, String message) {
        String statusCode = message.contains("Failed") ? "Failed" : "Success";

        Response response = new Response(
                String.valueOf(request.getSequence_id()),
                "nick",
                request.getRequest_details().getMethod_name(),
                message,
                statusCode
        );

        Gson gson = new Gson();
        String responseString = gson.toJson(response);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(4000);

            InetAddress hostName = InetAddress.getByName("localhost"); // CHANGE TO FRONT-END HOST NAME

            byte[] bytes = responseString.getBytes();
            System.out.println(responseString);
            DatagramPacket responsePacket = new DatagramPacket(bytes, bytes.length, hostName, 5555); // CHANGE TO FE PORT
            socket.send(responsePacket);

            byte[] buffer = new byte[1000];
            DatagramPacket frontEndPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(frontEndPacket);

            String frontEndReply = new String(frontEndPacket.getData(), 0, frontEndPacket.getLength());

            System.out.println(frontEndReply);

        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
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
                    String reply = "Killing " + store.getStorePrefix() + " Server (store thread)";
                    byte[] bytes = reply.getBytes();
                    DatagramPacket replyPacket = new DatagramPacket(bytes, bytes.length, request.getAddress(), request.getPort());
                    socket.send(replyPacket);
                    System.out.println(reply);
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
                return store.nonLocalReturn(params[1], params[2], Integer.parseInt(params[3]));
            case "waitlist":
                return store.nonLocalWaitList(params[1], params[2], Integer.parseInt(params[3]));
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
