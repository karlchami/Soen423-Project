package frontend.implementation;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import Models.response.Response;
import frontend.utils.ClientLauncher;
import org.omg.CORBA.*;

import frontend.corba.frontendPOA;
import frontend.utils.Tuple;
import frontend.utils.RequestBuilder;

public class frontendImpl extends frontendPOA {

    private org.omg.CORBA.ORB orb;
    private Logger log = null;
    private DatagramSocket socket;

    // Sequencer
    private int sequencer_port = 4100;
    private InetAddress sequencer_address = InetAddress.getLocalHost();

    // Stores RM info
    private ArrayList<Tuple<InetAddress, Integer, String>> rm_info = new ArrayList<>();
    private static int delay;

    public frontendImpl(ORB orb) throws IOException {
        super();

        this.orb = orb;
        // Local
        int port = 5555;
        this.socket = new DatagramSocket(port);

        // Add RM info to tuple
        // TODO: Agree on port number for each RM
        InetAddress local_host = InetAddress.getLocalHost();
        rm_info.add(new Tuple<>(local_host, 3000, "karl"));
        rm_info.add(new Tuple<>(local_host, 3001, "waqar"));
        rm_info.add(new Tuple<>(local_host, 6000, "nick"));
        // Set response delay (for reliable UDP) in milliseconds
        delay = 1000;

//        log = startLogger();
//        log.info("Frontend started on port " + port);
    }

    public static void main(String[] args) {
        ClientLauncher.initializeORB(args);
    }

    public String addItem(String managerID, String itemID, String itemName, int quantity, int price) {
        String request = RequestBuilder.addItemRequest(managerID, itemID, itemName, quantity, price);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public String removeItem(String managerID, String itemID, int quantity) {
        String request = RequestBuilder.removeItemRequest(managerID, itemID, quantity);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public String listItemAvailability(String managerID) {
        String request = RequestBuilder.listItemAvailabilityRequest(managerID);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        String request = RequestBuilder.purchaseItemRequest(customerID, itemID, dateOfPurchase);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public String findItem(String customerID, String itemName) {
        String request = RequestBuilder.findItemRequest(customerID, itemName);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public String returnItem(String customerID, String itemID, String dateOfReturn) {
        String request = RequestBuilder.returnItemRequest(customerID, itemID, dateOfReturn);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public String exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        String request = RequestBuilder.exchangeItemRequest(customerID, newItemID, oldItemID, dateOfExchange);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public String addCustomerWaitList(String customerID, String itemID) {
        String request = RequestBuilder.addCustomerWaitListRequest(customerID, itemID);
        sendRequest(request, sequencer_address, sequencer_port);
		return receive(socket);
    }

    public static void sendRequest(String message, InetAddress inet_address, int port) {
        int received = 0;
        byte[] message_bytes = message.getBytes();
        DatagramPacket request = new DatagramPacket(message_bytes, message_bytes.length, inet_address, port);
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(delay);
            // Keeps listening until it gets a response, if no response comes in after delay, resend and wait again until received
            while (received < 2) {
                sendSocket.send(request);
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sendSocket.receive(reply);

                    String response = new String(reply.getData(), 0, reply.getLength());
                    if (response.equals("received")) {
                        received = 2;
                    }
                } catch (SocketTimeoutException e) {
                    received++;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessageNoReply(String message, InetAddress address, int port) {
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            byte[] resultBytes = message.getBytes();
            DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, address, port);
            sendSocket.send(request);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String receive(DatagramSocket socket) {
        String response_message;

        // Expected number of replies (RM info tuple size)
        int reply_count = 0;

        // Holds info about RMs that send UDP back to FE, used for tracking responses
        ArrayList<Tuple<InetAddress, Integer, String>> received_rm = new ArrayList<>();

        // Determines whether we should keep receiving responses or not
        boolean receive = true;

        try {
			socket.setSoTimeout(5000);
            while (receive) {
                byte[] buffer = new byte[1000];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                System.out.println(new String(response.getData(), 0, response.getLength()));

                Tuple<InetAddress, Integer, String> rm = new Tuple<>(response.getAddress(), response.getPort(), new String(response.getData(), 0, response.getLength()));

                if (!received_rm.contains(rm)) {
                    // Add RM to received RMs tuple after receiving
                    received_rm.add(rm);
                    reply_count++;

                    // Sends back a reply to RM denoting that response is received
                    sendMessageNoReply("received-response", response.getAddress(), response.getPort());
                }
                if (reply_count == 3) { // change back to 3
                    receive = false;
                }
            }
            // Compares RM responses and returns the correct answer
            response_message = compareResponses(received_rm);
            return response_message;

        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out after receiving " + reply_count + " messages.");
            return compareResponses(received_rm);
        } catch (IOException e) {
        	e.printStackTrace();
        	return "An error occurred.";
		}
    }

    // Compares JSON responses from different RMs and returns the 
    public String compareResponses(ArrayList<Tuple<InetAddress, Integer, String>> received_rm) {
        String response_message = null;
        int correct_responses = 0;

        // Store rm information with the rightmost element to be replica name instead of message
        ArrayList<Tuple<InetAddress, Integer, String>> received_rm_info = new ArrayList<>();

        // To get the response message majority
        for (Tuple<InetAddress, Integer, String> single_rm : received_rm) {
            // getName is the equivalent of getResponseMessage
            Response rawResponse = new Response(single_rm.getName());

            String replica_id = rawResponse.getReplica_id();
            String message = rawResponse.getResponse_details().getMessage();

            Tuple<InetAddress, Integer, String> rm_info_name = new Tuple<>(single_rm.getInetAddress(), single_rm.getPort(), replica_id);
            received_rm_info.add(rm_info_name);

            // Comparison logic
            if (correct_responses == 0) {
                response_message = message;
                correct_responses++;
            } else if (response_message.equals(message)) {
                correct_responses++;
            } else {
                correct_responses--;
            }

        }

        // Detect crashed RMs aka RMs that didn't reply
        if (received_rm.size() < 3) {
            // Take a copy of RM arraylist
            ArrayList<Tuple<InetAddress, Integer, String>> crashed_rms = new ArrayList<>(rm_info);
            // Remove all non-defective RMs from crashed list
            crashed_rms.removeAll(received_rm_info);
            for (Tuple<InetAddress, Integer, String> rm : crashed_rms) {
                notify_rm(rm.getInetAddress(), rm.getPort(), "CRASHED-RM", rm.getName());
            }
        }

        // To notify failed RMs aka RMs that had a wrong response message or failed status code
        for (Tuple<InetAddress, Integer, String> rm : received_rm) {
            // getName is the equivalent of getResponseMessage
            Response rawResponse = new Response(rm.getName());
            String msg = rawResponse.getResponse_details().getMessage();

            if ((!response_message.equals(msg))) {
                notify_rm(rm.getInetAddress(), rm.getPort(), "FAILED-RM", rawResponse.getReplica_id());
            }
        }

        return response_message;
    }

    // Notifies all RMs in multicast about a single RM failure
    private void notify_rm(InetAddress address, int port, String status, String replica_name) {
        for (Tuple<InetAddress, Integer, String> rm : rm_info) {
            String message = replica_name + "," + status + "," + address + "," + port;
            new Thread(() -> sendRequest(message, rm.getInetAddress(), rm.getPort())).start();
        }
    }

    // Handles logging
    public Logger startLogger() {
        Logger logger = Logger.getLogger("frontend-log");
        FileHandler fh;
        try {
            fh = new FileHandler("C:\\Users\\Waqar's PC\\Downloads\\Sample Source Code  Java IDL (CORBA)-20201013\\Reference Book\\soen423-project\\bin\\frontend\\logs\\frontend\\" + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    public void shutdown() {
        this.orb.shutdown(false);
    }

}