package frontend.implementation;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
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
    private static boolean failureMode = false;

    // Sequencer
    private int sequencer_port = 4100;
    private InetAddress sequencer_address = InetAddress.getLocalHost();

    // Stores RM info
    private ArrayList<Tuple<InetAddress, Integer, String>> rm_info = new ArrayList<>();
    private HashMap<String, Integer> fail_count = new HashMap<>();
    private static int delay;

    public frontendImpl(ORB orb) throws IOException {
        super();

        this.orb = orb;
        // Local
        int port = 5555;
        this.socket = new DatagramSocket(port);

        // Add RM info to tuple
        InetAddress karl_host = InetAddress.getByName("132.205.95.111");
        InetAddress waqar_host = InetAddress.getByName("132.205.95.115");
        InetAddress nick_host = InetAddress.getByName("132.205.95.146");

        rm_info.add(new Tuple<>(karl_host, 6001, "karl"));
        rm_info.add(new Tuple<>(waqar_host, 2020, "waqar"));
        rm_info.add(new Tuple<>(nick_host, 6000, "nick"));
        // Set response delay (for reliable UDP) in milliseconds
        delay = 1000;

        fail_count.put("karl", 0);
        fail_count.put("waqar", 0);
        fail_count.put("nick", 0);


//        log = startLogger();
//        log.info("Frontend started on port " + port);
    }

    public static void main(String[] args) {
        selectMode();
        if (failureMode) {
            System.out.println("mode: software failure");
        } else {
            System.out.println("mode: process crash");
        }
        ClientLauncher.initializeORB(args);
    }

    private static void selectMode() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose a mode:\n" +
                "1. Software Failure\n" +
                "2. Process Crash");

        String mode = sc.nextLine();

        while (!mode.equals("1") && !mode.equals("2")) {
            System.out.println("Invalid option. Please enter 1 or 2.");
            sc.nextLine();
        }

        if (mode.equals("1")) {
            failureMode = true;
        }
    }

    public String addItem(String managerID, String itemID, String itemName, int quantity, int price) {
        String request = RequestBuilder.addItemRequest(managerID, itemID, itemName, quantity, price);
        sendRequest(request, sequencer_address, sequencer_port);
        return receive();
    }

    public String removeItem(String managerID, String itemID, int quantity) {
        String request = RequestBuilder.removeItemRequest(managerID, itemID, quantity);
        sendRequest(request, sequencer_address, sequencer_port);
        return receive();
    }

    public String listItemAvailability(String managerID) {
        String request = RequestBuilder.listItemAvailabilityRequest(managerID);
        sendRequest(request, sequencer_address, sequencer_port);
        return receive();
    }

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        String request = RequestBuilder.purchaseItemRequest(customerID, itemID, dateOfPurchase);
        sendRequest(request, sequencer_address, sequencer_port);
        return receive();
    }

    public String findItem(String customerID, String itemName) {
        String request = RequestBuilder.findItemRequest(customerID, itemName);
        sendRequest(request, sequencer_address, sequencer_port);
        return receive();
    }

    public String returnItem(String customerID, String itemID, String dateOfReturn) {
        String request = RequestBuilder.returnItemRequest(customerID, itemID, dateOfReturn);
        sendRequest(request, sequencer_address, sequencer_port);
        return receive();
    }

    public String exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        String request = RequestBuilder.exchangeItemRequest(customerID, newItemID, oldItemID, dateOfExchange);
        sendRequest(request, sequencer_address, sequencer_port);
        return receive();
    }

    public String addCustomerWaitList(String customerID, String itemID) {
        String request = RequestBuilder.addCustomerWaitListRequest(customerID, itemID);
        sendRequest(request, sequencer_address, sequencer_port);
        return "Successfully waitlisted. Item " + itemID + " will be automatically purchased when available.";
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

    private String receive() {
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
            if (failureMode) {
                return compareResponses(received_rm);
            } else {
                return response(received_rm);
            }

        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out after receiving " + reply_count + " messages.");
            handleCrash(received_rm);
            Response response = new Response(received_rm.get(0).getName());
            String message = response.getResponse_details().getMessage();
            String method = response.getResponse_details().getMethod_name();
            if (method.equals("findItem") || method.equals("listItemAvailability")) {
                return formatItemListMessage(message);
            } else {
                return message;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred.";
        }
    }

    private void handleCrash(ArrayList<Tuple<InetAddress, Integer, String>> received_rm) {
        // Detect crashed RMs aka RMs that didn't reply
        if (received_rm.size() < 3) {
            // Take a copy of RM arraylist
            ArrayList<String> crashed_rms = new ArrayList<>();
            crashed_rms.add("karl");
            crashed_rms.add("waqar");
            crashed_rms.add("nick");

            for (Tuple<InetAddress, Integer, String> rm : received_rm) {
                crashed_rms.remove(new Response(rm.getName()).getReplica_id());
            }
            for (String replica_id : crashed_rms) {
                notify_rm("CRASHED", replica_id);
            }
        }
    }

    public boolean compareItemLists(String item1, String item2) {
        String[] item1_array = item1.split(";");
        String[] item2_array = item2.split(";");
        boolean same_length = item1_array.length == item2_array.length;
        boolean same_content = Arrays.asList(item1_array).containsAll(Arrays.asList(item2_array));
        return same_length && same_content;
    }

    public String formatItemListMessage(String item_list) {
        if (item_list.equals("")) {
            return item_list;
        }
        String[] items = item_list.split(";");
        String message = "";
        for (String item : items) {
            String[] item_info = item.split(",");
            String id = item_info[0];
            String name = item_info[1];
            String quantity = item_info[2];
            String price = item_info[3];
            message += "ID: " + id + ", Name: " + name + ", Qty: " + quantity + ", Price: " + price + "$ \n";
        }
        return message;
    }

    // Compares JSON responses from different RMs and returns the 
    public String compareResponses(ArrayList<Tuple<InetAddress, Integer, String>> received_rm) {
        String response_message = null;
        int correct_responses = 0;
        // If the response to be returned is in list form
        boolean list_response = false;

        // To get the response message majority
        for (Tuple<InetAddress, Integer, String> rm : received_rm) {
            // getName is the equivalent of getResponseMessage
            Response response = new Response(rm.getName());
            String message = response.getResponse_details().getMessage();
            String method_name = response.getResponse_details().getMethod_name();

            if (method_name.equals("findItem") || method_name.equals("listItemAvailability")) {
                list_response = true;
                // Comparison logic for findItem and listItemAvailability
                if (correct_responses == 0) {
                    response_message = message;
                    correct_responses++;
                } else if (compareItemLists(response_message, message)) {
                    correct_responses++;
                } else {
                    correct_responses--;
                }
            } else {
                // Comparison logic for all other methods
                if (correct_responses == 0) {
                    response_message = message;
                    correct_responses++;
                } else if (response_message.equals(message)) {
                    correct_responses++;
                } else {
                    correct_responses--;
                }
            }

        }

        // To notify failed RMs aka RMs that had a wrong response message or failed status code
        for (Tuple<InetAddress, Integer, String> rm : received_rm) {
            // getName is the equivalent of getResponseMessage
            Response response = new Response(rm.getName());
            String replica_id = response.getReplica_id();
            String message = response.getResponse_details().getMessage();
            String method = response.getResponse_details().getMethod_name();

            if ((method.equals("findItem") || method.equals("listItemAvailability")) && !compareItemLists(message, response_message)) {
                fail_count.put(replica_id, fail_count.get(replica_id) + 1);
                notify_rm("FAILED", response.getReplica_id());
            } else if (!method.equals("findItem") && !method.equals("listItemAvailability") && !response_message.equals(message)) {
                fail_count.put(replica_id, fail_count.get(replica_id) + 1);
                notify_rm("FAILED", response.getReplica_id());
            } else {
                fail_count.put(replica_id, 0);
            }

            if (fail_count.get(replica_id) > 2 && failureMode) {
                notify_rm("FAILED,restart", response.getReplica_id());
                fail_count.put(replica_id, 0);
            }
        }

        // Return formatted list message
        if (list_response) {
            return formatItemListMessage(response_message);
        }
        return response_message;
    }

    private String response(ArrayList<Tuple<InetAddress, Integer, String>> received_rm) {
        return compareResponses(received_rm);
    }

    // Notifies all RMs in multicast about a single RM failure
    private void notify_rm(String status, String replica_name) {
        for (Tuple<InetAddress, Integer, String> rm : rm_info) {
            String message = replica_name + "," + status;
            new Thread(() -> sendMessageNoReply(message, rm.getInetAddress(), rm.getPort())).start();
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