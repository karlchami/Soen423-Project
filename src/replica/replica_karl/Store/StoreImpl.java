package replica.replica_karl.Store;

import replica.replica_karl.models.Customer;
import replica.replica_karl.models.Manager;
import replica.replica_karl.models.Store;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

public class StoreImpl {
    // Store item details in such order : Name, Quantity, Price
    private Map<String, String> itemStore = new HashMap<String, String>();
    private Map<String, PriorityQueue<String>> itemWaitList = new HashMap<String, PriorityQueue<String>>();
    private HashMap<String, Customer> Customers = new HashMap<String, Customer>();
    private HashMap<String, ArrayList<String>> Eligibility = new HashMap<String, ArrayList<String>>();
    private HashMap<String, Manager> Managers = new HashMap<String, Manager>();
    private HashMap<String, Integer> ports = new HashMap<String, Integer>();
    private ArrayList<String> purchaseLog = new ArrayList<String>();
    private Store store;
    private Logger logger = null;

    public StoreImpl() {
    }

    public StoreImpl(Store store) throws IOException {
        super();
        this.store = store;
        this.ports.put("QC", 4001);
        this.ports.put("BC", 4003);
        this.ports.put("ON", 4002);
        this.logger = this.launchLogger();
        logger.info("Store server " + this.store.toString() + " is now running.");
        if (this.store.toString().equals("QC")) {
            this.itemStore.put("QC6231", "Tea,2,30");
            this.itemStore.put("QC6651", "Chocolates,2,30");
        } else if (this.store.toString().equals("ON")) {
            this.itemStore.put("ON6231", "Tea,1,10");
        }
    }

    public String getStorePrefix() {
        return this.store.toString();
    }

    // Server logger
    // TODO: Change log path
    public Logger launchLogger() {
        Logger logger = Logger.getLogger("ServerLog");
        FileHandler fh;
        try {
            fh = new FileHandler("G:\\My Documents\\soen423-project\\src\\replica\\replica_karl\\logs\\server\\" + this.store.toString() + "_server.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    public String addItem(String managerID, String itemID, String itemName, int quantity, int price) {
        String response;
        // If item already exists in store modify quantity
        if (this.itemStore.containsKey(itemID)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            // Index 1 to get quantity from itemStore
            int current_quantity = Integer.parseInt(item_details[1]);
            int new_quantity = current_quantity + quantity;
            item_details[1] = Integer.toString(new_quantity);
            itemStore.replace(itemID, String.join(",", item_details));
            //response = "Manager " + managerID + " updated item " + itemID + " in " + this.store + " store. \n";
            response = managerID + " - Success. Added " + quantity + " units of item " + itemID + " to inventory.";
            logger.info(response);
        } else {
            // If item does not exist just add it to itemStore
            itemStore.put(itemID, itemName + "," + quantity + "," + price);
            response = managerID + " - Success. Added " + quantity + " units of NEW item " + itemID + " to inventory.";
            logger.info(response);
        }
        // If item is in waiting list automatically purchase item for wait-listed customers for the available quantity
        if (this.itemWaitList.containsKey(itemID)) {
            PriorityQueue<String> client_queue = this.itemWaitList.get(itemID);
            for (String clientID : client_queue) {
                // If client in local store
                if (clientID.startsWith(this.store.toString())) {
                    this.purchaseItem(clientID, itemID, new Date().toString());
                    client_queue.remove(clientID);
                    //response += " Customer " + clientID + " purchased " + itemID + " from waitlist.";
                }
                // If client in another store
                else {
                    int port = this.ports.get(clientID.substring(0, 2));
                    String cmd = "PURCHASE-ITEM," + clientID + "," + itemID + "," + new Date().toString();
                    this.sendCommand(port, cmd);
                }
            }
        }
        return response;
    }

    public String removeItem(String managerID, String itemID, int quantity) {
        String response;
        // If item exists in itemStore
        if (this.itemStore.containsKey(itemID)) {
            // If quantity set to 0 means remove item
            if (quantity == -1) {
                this.itemStore.remove(itemID);
                response = managerID + " - Success. Completely removed item " + itemID + " from inventory.";
                logger.info(response);
                return response;
            }
            // If quantity positive treat the request
            else if (quantity > 0) {
                String[] item_details = this.itemStore.get(itemID).split(",");
                int current_quantity = Integer.parseInt(item_details[1]);
                int new_quantity = current_quantity - quantity;
                // If resulting quantity is 0 or less remove item
                if (new_quantity < 0) {
                    //this.itemStore.remove(itemID);
                    response = managerID + " - Failed to remove item " + itemID + ". Quantity entered must not exceed the current quantity in stock.";
                    logger.info(response);
                    return response;
                }
                if (new_quantity == 0) {
                    this.itemStore.remove(itemID);
                    response = managerID + " - Success. Completely removed item " + itemID + " from inventory.";
                    logger.info(response);
                    return response;
                }
                item_details[1] = Integer.toString(new_quantity);
                itemStore.replace(itemID, String.join(",", item_details));
                response = managerID + " - Success. Removed " + quantity + " units of item " + itemID + " from inventory.";
                logger.info(response);
                return response;
            }
            response = managerID + " - Failed to remove item " + itemID + ". Quantity entered must not exceed the current quantity in stock.";
            logger.info(response);
            return response;
        } else {
            // If item does not exist in itemStore
            response = managerID + " - Failed to remove item " + itemID + ". Item does not exist.";
            logger.info(response);
            return response;
        }
    }

    public String listItemAvailability(String managerID) {
        logger.info("Manager " + managerID + " requested a list of all items in " + this.store + " store.");
        String item_availability = "";
        for (Map.Entry<String, String> entry : this.itemStore.entrySet()) {
            String[] item_details = entry.getValue().split(",");
            item_availability += entry.getKey() + "," + item_details[0] + "," + item_details[1] + "," + item_details[2] + ";";
        }
        return item_availability;
    }

    // Local customer item purchase
    public String LocalPurchaseItem(String customerID, String itemID, String dateOfPurchase) {
        String response;
        Customer customer = Customers.get(customerID);
        long available_balance = customer.getBalance();
        // If item is in the store
        if (this.itemStore.containsKey(itemID)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            int current_quantity = Integer.parseInt(item_details[1]);
            long price = Long.parseLong(item_details[2]);
            // If item is available
            if (current_quantity > 0) {
                long remaining_balance = available_balance - price;
                // If customer has enough funds to buy item
                if (remaining_balance >= 0) {
                    // Set new customer balance
                    customer.setBalance(remaining_balance);
                    // Decrement item quantity by 1
                    item_details[1] = Integer.toString(current_quantity - 1);
                    itemStore.replace(itemID, String.join(",", item_details));
                    this.purchaseLog.add(itemID + "," + customerID + "," + dateOfPurchase);
                    response = customerID + " - Success. You have purchased a " + item_details[0] + " (" + itemID + ") for $" + price;
                    logger.info(response);
                    return response;
                } else {
                    response = customerID + " - Failed to purchase item " + itemID + ". Insufficient funds.";
                    logger.info(response);
                    return response;
                }
            } else {
                response = customerID + " - Failed to purchase item " + itemID + ". Item is out of stock.";
                logger.info(response);
                return response;
            }
        } else {
            response = customerID + " - Failed to purchase item " + itemID + ". There are no items with this ID.";
            System.out.println(response);
            return response;
        }
    }

    // Foreign customer item purchase
    public String ForeignPurchaseItem(String customerID, long balance, String itemID, String dateOfPurchase) {
        String response;
        // If item exists in foreign store
        if (this.itemStore.containsKey(itemID)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            int current_quantity = Integer.parseInt(item_details[1]);
            int price = Integer.parseInt(item_details[2]);
            if (current_quantity > 0) {
                long remaining_balance = balance - price;
                if (remaining_balance >= 0) {
                    item_details[1] = Integer.toString(current_quantity - 1);
                    itemStore.replace(itemID, String.join(",", item_details));
                    this.purchaseLog.add(itemID + "," + customerID + "," + dateOfPurchase);
                    response = (customerID + " - Success. You have purchased a " + item_details[0] + " (" + itemID + ") for $" + price).trim();
                    logger.info(response);
                    return "Success," + String.valueOf(remaining_balance) + "," + item_details[0] + "," + price;
                } else {
                    response = customerID + " - Failed to purchase item " + itemID + ". Insufficient funds.";
                    logger.info(response);
                    return response;
                }
            } else {
                response = customerID + " - Failed to purchase item " + itemID + ". Item is out of stock.";
                logger.info(response);
                return response;
            }
        } else {
            response = customerID + " - Failed to purchase item " + itemID + ". There are no items with this ID.";
            System.out.println(response);
            return response;
        }
    }

    public boolean customerEligible(String store, String customerID) {
        if (this.Eligibility.containsKey(store)) {
            return this.Eligibility.get(store).contains(customerID);
        }
        return this.Eligibility.containsKey(store);
    }

    public void addEligibility(String store, String customerID) {
        if (this.Eligibility.containsKey(store)) {
            this.Eligibility.get(store).add(customerID);
        } else {
            ArrayList<String> NONELIGIBLECUSTOMER = new ArrayList<String>();
            NONELIGIBLECUSTOMER.add(customerID);
            this.Eligibility.put(store, NONELIGIBLECUSTOMER);
        }
    }

    public void removeEligibility(String store, String customerID) {
        ArrayList<String> NONELIGIBLECUSTOMER = new ArrayList<String>();
        for (String customer_id : this.Eligibility.get(store)) {
            if (!customer_id.contains(customerID)) {
                NONELIGIBLECUSTOMER.add(customer_id);
            }
        }
        this.Eligibility.put(store, NONELIGIBLECUSTOMER);
    }

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        this.addCustomer(customerID);
        String purchase_store = itemID.substring(0, 2);
        Customer customer = this.Customers.get(customerID);
        String current_store = this.store.toString();
        // If purchase is local
        if (purchase_store.equals(current_store)) {
            logger.info("Customer " + customerID + " purchasing an item in local store " + this.store);
            return this.LocalPurchaseItem(customerID, itemID, dateOfPurchase);
        } else if (!this.customerEligible(purchase_store, customerID)) {
            // If purchase is foreign send purchase request to the UDP server for the specific store
            logger.info("Customer " + customerID + " purchasing an item in foreign store " + purchase_store);
            logger.info("Sending UDP command to " + purchase_store + " store...");
            String cmd = "PURCHASE-ITEM-FOREIGN" + "," + customerID + "," + customer.getBalance() + "," + itemID + "," + dateOfPurchase;
            String response = this.sendCommand(this.ports.get(itemID.substring(0, 2)), cmd);
            if (response.startsWith("Success")) {
                this.addEligibility(purchase_store, customerID);
                long remaining_balance = Long.parseLong(response.split(",")[1].trim());
                String item_name = response.split(",")[2].trim();
                String item_price = response.split(",")[3].trim();
                customer.setBalance(remaining_balance);
                response = (customerID + " - Success. You have purchased a " + item_name + " (" + itemID + ") for $" + item_price).trim();
            }
            return response;
        } else {
            String response = customerID + " - Failed to purchase item " + itemID + ". You have reached your 1 item limit for the " + purchase_store + " store.";
            logger.info(response);
            return response;
        }
    }

    public String LocalFindItemX(String itemName) {
        String no_found_items = "";
        for (Map.Entry<String, String> entry : this.itemStore.entrySet()) {
            String name = entry.getValue().split(",")[0];
            if (itemName.trim().equals(name)) {
                return entry.getKey() + "," + entry.getValue() + ";";
            }
        }
        return no_found_items;
    }

    public String LocalFindItem(String itemName) {
        StringBuilder results = new StringBuilder();
        for (Map.Entry<String, String> entry : this.itemStore.entrySet()) {
            String name = entry.getValue().split(",")[0];
            if (itemName.trim().equalsIgnoreCase(name)) {
                results.append(entry.getKey()).append(",").append(entry.getValue()).append(";");
            }
        }
        return results.toString();
    }

    public String findItem(String customerID, String itemName) {
        // Get all items in local store
        String found_items = this.LocalFindItem(itemName);
        for (Map.Entry<String, Integer> entry : this.ports.entrySet()) {
            if (entry.getKey().equals(this.store.toString())) {
                continue;
            } else {
                String cmd = "FIND-ITEM," + itemName;
                logger.info("Store server sending UDP request to find item.");
                String result = this.sendCommand(entry.getValue(), cmd);
                if (result.trim().equals("")) {
                    continue;
                } else {
                    found_items = found_items + this.sendCommand(entry.getValue(), cmd);
                }
            }
            if (found_items.equals("")) {
                return "";
            }
        }
        return found_items.trim();
    }

    public static boolean returnPossible(String formattedDate) {
        int day = Integer.parseInt(formattedDate.substring(0, 2));
        int month = Integer.parseInt(formattedDate.substring(3, 5));
        int year = Integer.parseInt(formattedDate.substring(6, 10));
        LocalDate oldTime = LocalDate.of(year, month, day);
        LocalDate maxReturnDate = LocalDate.now().plusDays(30);
        boolean isPossible = maxReturnDate.isAfter(oldTime);
        return isPossible;
    }


    public boolean validateReceipt(String customerID, String itemID, String date) throws ParseException {
        for (String receipt : this.purchaseLog) {
            String[] receipt_details = receipt.split(",");
            String log_itemID = receipt_details[0];
            String log_customerID = receipt_details[1];
            String purchase_date = receipt_details[2];

            // If receipt item matches with client and returned itemID
            if (log_itemID.equals(itemID) && log_customerID.equals(customerID)) {
                if (returnPossible(purchase_date)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String ForeignReturnItem(String customerID, String itemID, String dateOfReturn) throws NumberFormatException, ParseException {
        // Validate if this return is eligible by checking store receipts
        if (this.validateReceipt(customerID, itemID, dateOfReturn)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            int item_price = Integer.parseInt(item_details[2]);
            // Requires manager to add item back
            this.addItem("RETURN", itemID, item_details[0], 1, item_price);
            logger.info("Customer " + customerID + " returned " + itemID + " on " + dateOfReturn);
            // Return refund amount
            String refund_amount = this.itemStore.get(itemID).split(",")[1];
            return "SUCCESS," + refund_amount;
        } else {
            return "FAILED";
        }
    }

    public String returnItem(String customerID, String itemID, String dateOfReturn) {
        this.addCustomer(customerID);
        String response = null;
        Customer customer = this.Customers.get(customerID);
        String item_store = itemID.substring(0, 2);
        String current_store = this.store.toString();
        // If item to be returned in local store
        if (item_store.equals(current_store)) {
            try {
                // Validate if this return is eligible by checking store receipts
                if (this.validateReceipt(customerID, itemID, dateOfReturn)) {
                    String[] item_details = this.itemStore.get(itemID).split(",");
                    int item_price = Integer.parseInt(item_details[2]);
                    // Requires manager to add item back
                    this.addItem("RETURN", itemID, item_details[0], 1, item_price);
                    // Refund the customer
                    long current_balance = customer.getBalance();
                    customer.setBalance(current_balance + item_price);
                    response = customerID + " - Success. You have returned a" + item_details[0] + " (" + itemID + ") for $" + item_price;
                    logger.info(response.trim());
                } else {
                    // If not valid receipt do not return item
                    response = customerID + " - Failed to return item " + itemID + ". You must return items within 30 days of purchase.";
                    logger.info(response);
                }
            } catch (NumberFormatException | ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // If item to be returned in a foreign store
            int port = this.ports.get(item_store);
            logger.info("Store Server sending UDP request to return item...");
            String command_response = this.sendCommand(port, "RETURN," + itemID + "," + customerID + "," + dateOfReturn);
            this.removeEligibility(item_store, customerID);
            if (command_response.startsWith("FAILED")) {
                response = customerID + " - Failed to return item " + itemID + ". Could not find purchase.";
                logger.info(response);
                return response;
            } else if (command_response.startsWith("SUCCESS")) {
                String[] item_details = this.sendCommand(port, "GET-ITEM," + itemID).split(",");
                int item_price = Integer.parseInt(item_details[2].trim());
                long current_balance = customer.getBalance();
                customer.setBalance(current_balance + (long) item_price);
                response = customerID + " - Success. You have returned a" + item_details[0] + " (" + itemID + ") for $" + item_price;
                logger.info(response);
            }
        }
        return response;
    }

    public String exchangeItem(String customerID, String newitemID, String oldItemID, String dateOfExchange) {
        this.addCustomer(customerID);
        String response;
        int oldItemPrice = 0;
        int newItemPrice = 0;
        // If item to be exchanged is in local store
        if (oldItemID.startsWith(this.store.toString())) {
            // Validate if this return is eligible by checking store receipts
            try {
                if (!this.validateReceipt(customerID, oldItemID, dateOfExchange)) {
                    response = customerID + " - Exchange failed. " + customerID + " - Failed to return item " + oldItemID + ". Could not find purchase.";
                    this.logger.info(response);
                    return response;
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String[] item_details = this.itemStore.get(oldItemID).split(",");
            oldItemPrice = Integer.parseInt(item_details[2]);
        } else {
            String item_store = oldItemID.substring(0, 2);
            if (this.sendCommand(this.ports.get(item_store), "VALIDATE-RECEIPT," + customerID + "," + oldItemID + "," + dateOfExchange).equals("false")) {
                response = customerID + " - Exchange failed. " + customerID + " - Failed to return item " + oldItemID + ". Could not find purchase.";
                this.logger.info(response);
                return response;
            }
            String[] item_details = this.sendCommand(this.ports.get(oldItemID.substring(0, 2)), "GET-ITEM," + oldItemID).trim().split(",");
            oldItemPrice = Integer.parseInt(item_details[2]);
        }
        if (newitemID.startsWith(this.store.toString())) {
            String[] item_details = this.itemStore.get(newitemID).split(",");
            if (Integer.parseInt(item_details[1]) == 0) {
                response = customerID + " - Exchange failed. " + customerID + " - Failed to purchase item " + newitemID + ". Item is out of stock.";
                this.logger.info(response);
                return response;
            }
            newItemPrice = Integer.parseInt(item_details[2]);
        } else {
            String[] item_details = this.sendCommand(this.ports.get(newitemID.substring(0, 2)), "GET-ITEM," + newitemID).trim().split(",");
            if (Integer.parseInt(item_details[1]) == 0) {
                response = customerID + " - Exchange failed. " + customerID + " - Failed to purchase item " + newitemID + ". Item is out of stock.";
                this.logger.info(response);
                return response;
            }
            newItemPrice = Integer.parseInt(item_details[2]);
        }
        //check budget
        Customer customer = this.Customers.get(customerID);
        long different_to_pay = newItemPrice - oldItemPrice;
        if (customer.getBalance() < different_to_pay) {
            response = customerID + " - Exchange failed. " + customerID + " - Failed to purchase item " + newitemID + ". Insufficient funds.";
            this.logger.info(response);
            return response;
        }
        System.out.println(this.returnItem(customerID, oldItemID, dateOfExchange));
        System.out.println(this.purchaseItem(customerID, newitemID, dateOfExchange));
        return customerID + " - Success. You have exchanged item " + oldItemID + " for item " + newitemID + ".";
    }

    public String addLocalCustomerWaitList(String customerID, String itemID) {
        // Check if wait-list for item exist and add customer to it
        if (this.itemWaitList.containsKey(itemID)) {
            this.itemWaitList.get(itemID).add(customerID);
        } else {
            // Create a new key for item if does not exist
            PriorityQueue<String> queue = new PriorityQueue<String>();
            queue.add(customerID);
            this.itemWaitList.put(itemID, queue);
        }
        String response = "Store " + this.store + " Added " + customerID + " to the waitlist for item " + itemID;
        logger.info(response.trim());
        return response.trim();
    }

    public void addCustomerWaitList(String customerID, String itemID) {
        // If item belongs to local store then add to local wait list
        if (itemID.substring(0, 2).equals(this.store.toString())) {
            this.addLocalCustomerWaitList(customerID, itemID);
        } else {
            // If item belongs to foreign store then add to foreign wait list
            int port = this.ports.get(itemID.substring(0, 2));
            String message = "WAITLIST," + customerID + "," + itemID;
            this.sendCommand(port, message);
        }
    }

    public void addCustomer(String customerID) {
        if (!this.Customers.containsKey(customerID)) {
            Customer customer = new Customer(customerID, this.store);
            this.Customers.put(customerID, customer);
        }
    }

    public void addManager(String managerID) {
        Manager manager = new Manager(managerID, this.store);
        this.Managers.put(managerID, manager);
    }

    public void shutdown() {
        // TODO Auto-generated method stub
    }

    public String sendCommand(int port, String message) {
        DatagramSocket aSocket = null;
        String response = null;
        try {
            aSocket = new DatagramSocket();
            byte[] cmd = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(cmd, message.length(), aHost, port);
            aSocket.send(request);
            System.out.println("Command sent to respective store server " + port + " is: " + new String(request.getData(), 0, request.getLength()));
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            response = new String(reply.getData(), 0, reply.getLength());
            System.out.println("Response received from the respective store server " + port + " is: " + new String(reply.getData(), 0, reply.getLength()));
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();

        }
        return response;

    }

    public void receive() throws NumberFormatException, ParseException {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(this.ports.get(this.store.toString()));
            byte[] buffer = new byte[1000];
            System.out.println("UDP Server " + this.store.toString() + " has started listening...");
            String replyMessage = null;
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String[] requestArgs = new String(request.getData()).split(",");
                if (requestArgs[0].equals("PURCHASE-ITEM-FOREIGN")) {
                    String customerID = requestArgs[1];
                    int balance = Integer.parseInt(requestArgs[2]);
                    String itemID = requestArgs[3];
                    String date = requestArgs[4];
                    replyMessage = this.ForeignPurchaseItem(customerID, balance, itemID, date);
                } else if (requestArgs[0].equals("WAITLIST")) {
                    String customerID = requestArgs[1];
                    String itemID = requestArgs[2].trim();
                    replyMessage = this.addLocalCustomerWaitList(customerID, itemID);
                } else if (requestArgs[0].equals("FIND-ITEM")) {
                    String itemName = requestArgs[1];
                    replyMessage = this.LocalFindItem(itemName);
                } else if (requestArgs[0].equals("GET-ITEM")) {
                    replyMessage = this.itemStore.get(requestArgs[1].trim());
                } else if (requestArgs[0].equals("RETURN")) {
                    String itemID = requestArgs[1];
                    String customerID = requestArgs[2];
                    String dateOfReturn = requestArgs[3];
                    replyMessage = this.ForeignReturnItem(customerID, itemID, dateOfReturn);
                } else if (requestArgs[0].equals("PURCHASE-ITEM")) {
                    String customerID = requestArgs[1].trim();
                    String itemID = requestArgs[2];
                    String date = requestArgs[3];
                    replyMessage = this.purchaseItem(customerID, itemID, date);
                } else if (requestArgs[0].equals("VALIDATE-RECEIPT")) {
                    String customerID = requestArgs[1];
                    String itemID = requestArgs[2];
                    String date = requestArgs[3];
                    replyMessage = String.valueOf(this.validateReceipt(customerID, itemID, date));
                }
                DatagramPacket reply = new DatagramPacket(replyMessage.getBytes(), replyMessage.length(), request.getAddress(), request.getPort());
                aSocket.send(reply);
                buffer = new byte[1000];
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
