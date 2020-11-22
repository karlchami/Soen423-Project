package Store;

import models.Customer;
import models.Manager;
import models.Store;

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
import java.util.*;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(endpointInterface = "Store.StoreInterface")
public class StoreImpl implements StoreInterface {
    // Store item details in such order : Name, Quantity, Price
    private Map<String, String> itemStore = new HashMap<String, String>();
    private Map<String, PriorityQueue<String>> itemWaitList = new HashMap<String, PriorityQueue<String>>();
    private HashMap<String, Customer> Customers = new HashMap<String, Customer>();
    private HashMap<String, Manager> Managers = new HashMap<String, Manager>();
    private HashMap<String, Integer> ports = new HashMap<String, Integer>();
    private ArrayList<String> purchaseLog = new ArrayList<String>();
    private Store store;
    private Logger logger = null;
    
    public StoreImpl() {}
    
    public StoreImpl(Store store) throws IOException {
        super();
        this.store = store;
        this.ports.put("QC", 5555);
        this.ports.put("BC", 4444);
        this.ports.put("ON", 7777);
        this.logger = this.launchLogger();
        logger.info("Store server " + this.store.toString()+ " is now running.");
    }
    
    // Server logger
    // TODO: Change log path
    public Logger launchLogger() {
        Logger logger = Logger.getLogger("ServerLog");
        FileHandler fh;
        try {
            fh = new FileHandler("X:\\soen423-a3\\src\\logs\\server\\"+this.store.toString()+"_server.log");
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
    
	@Override
	public boolean addItem(String managerID, String itemID, String itemName, int quantity, int price) {
		// If item already exists in store modify quantity
        if (this.itemStore.containsKey(itemID)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            // Index 1 to get quantity from itemStore
            int current_quantity = Integer.parseInt(item_details[1]);
            int new_quantity = current_quantity + quantity;
            item_details[1] = Integer.toString(new_quantity);
            itemStore.replace(itemID, String.join(",", item_details));
            logger.info("Manager " + managerID + " updated item " + itemID + "in " + this.store + " store.");

        } else {
        	// If item does not exist just add it to itemStore
        	itemStore.put(itemID, itemName + "," + quantity + "," + price);
            logger.info("Manager " + managerID + " created item " + " (" + itemID + ", " + price +"$, " + quantity + " left" + ") " + "in " + this.store + " store.");
        }
        // If item is in waiting list automatically purchase item for wait-listed customers for the available quantity
        if(this.itemWaitList.containsKey(itemID)){
            PriorityQueue<String> client_queue = this.itemWaitList.get(itemID);
            for(String clientID : client_queue){
            	// If client in local store
                if(clientID.startsWith(this.store.toString())){
                    this.purchaseItem(clientID, itemID, new Date().toString());
                }
                // If client in another store
                else{
                    int port = this.ports.get(clientID.substring(0,2));
                    String cmd = "PURCHASE-ITEM,"+ clientID + "," + itemID + "," + new Date().toString();
                    this.sendCommand(port, cmd);
                }
            }
        }
        return true;
	}
	
	@Override
	public boolean removeItem(String managerID, String itemID, int quantity) {
		// If item exists in itemStore
        if (this.itemStore.containsKey(itemID)) {
        	// If quantity set to 0 means remove item
            if(quantity == 0){
                this.itemStore.remove(itemID);
                logger.info("Manager " + managerID + " removed item " + itemID + "in " + this.store + " store.");
                return true;
            }
            // If quantity positive treat the request
            else if(quantity > 0) {
                String[] item_details = this.itemStore.get(itemID).split(",");
                int current_quantity = Integer.parseInt(item_details[1]);
                int new_quantity = current_quantity - quantity;
                // If resulting quantity is 0 or less remove item
                if(new_quantity <= 0) {
                	this.itemStore.remove(itemID);
                    logger.info("Manager " + managerID + " removed item " + this.store + itemID + " in " + this.store + " store.");
                    return true;
                }
                item_details[1] = Integer.toString(new_quantity);
                itemStore.replace(itemID, String.join(",", item_details));
                logger.info("Manager " + managerID + " removed (" + quantity + " items) " + this.store + itemID + " in " + this.store + " store.");
                return true;
            }            
            System.out.println("You cannot enter a negative quantity.");
            logger.info("Manager " + managerID + " entered a negative quantity to remove item " + this.store + itemID + " in " + this.store + " store.");
            return false;
        } else {
        	// If item does not exist in itemStore
            System.out.println("This item does not exist in the " + this.store + " store.");
            logger.info("Manager " + managerID + " attempted to remove non-existing item " + this.store + itemID + " in " + this.store + " store.");
            return false;
        }
	}
	
	@Override
	public String listItemAvailability(String managerID) {
        logger.info("Manager " + managerID + " requested a list of all items in " + this.store + " store.");
        String item_availability = "";
        for (Map.Entry<String,String> entry: this.itemStore.entrySet()){
            String[] item_details = entry.getValue().split(",");
        	item_availability += "ID: " + entry.getKey() + ", " + item_details[0] + ", " + item_details[1] + " left, " + item_details[2] + "$" +"\n";
        }
        return item_availability;
	}
	
	// Local customer item purchase
	public String LocalPurchaseItem(String customerID, String itemID, String dateOfPurchase) {
        Customer customer = Customers.get(customerID);
        long available_balance = customer.getBalance(); 
        // If item is in the store
        if (this.itemStore.containsKey(itemID)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            int current_quantity = Integer.parseInt(item_details[1]);
            long price = Long.parseLong(item_details[2]);
            // If item is available
            if(current_quantity > 0){
            	long remaining_balance = available_balance - price;
            	// If customer has enough funds to buy item
                if(remaining_balance >= 0){
                	// Set new customer balance
                    customer.setBalance(remaining_balance);
                    // Decrement item quantity by 1
                    item_details[1] = Integer.toString(current_quantity - 1);
                    itemStore.replace(itemID, String.join(",", item_details));
                    this.purchaseLog.add(itemID + "," + customerID + "," + dateOfPurchase);
                    logger.info("Customer " + customerID + " purchased item " + itemID + " successfully on " + dateOfPurchase);
                    return "Purchased";
                }
                else{
                	logger.info("Customer " + customerID + " was not able to purchase item " + itemID + " due to insufficient funds.");
                    return "Insufficient funds";
                }
            }
            else{
                logger.info(itemID + "is out of stock");
                return "Out of stock";
            }
        } else {
            System.out.println("Item " + itemID + " does not exist in store " + this.store);
            return "Does not exist";
        }
	}
	
	// Foreign customer item purchase
	public String ForeignPurchaseItem(String customerID, long balance, String itemID, String dateOfPurchase) {
		// If item exists in foreign store
        if (this.itemStore.containsKey(itemID)) {
            String[] item_details = this.itemStore.get(itemID).split(",");
            int current_quantity = Integer.parseInt(item_details[1]);
            int price = Integer.parseInt(item_details[2]);
            if(current_quantity > 0){
            	long remaining_balance = balance - price;
                if(remaining_balance >= 0){
                    item_details[1] = Integer.toString(current_quantity - 1);
                    itemStore.replace(itemID, String.join(",", item_details));
                    this.purchaseLog.add(itemID + "," + customerID + "," + dateOfPurchase);
                    return "Purchased," + remaining_balance;
                }
                else{
                    return "Insufficient funds";
                }
            }
            else{
                return "Out of stock";
            }
        } else {
            System.out.println("Item " + itemID + " does not exist in store " + this.store);
            return "Does not exist";
        }
	}
	
	@Override
	public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        String purchase_store = itemID.substring(0,2);
        Customer customer = this.Customers.get(customerID);
        String current_store = this.store.toString();
        // If purchase is local
        if (purchase_store.equals(current_store)) {
            logger.info("Customer " + customerID + " purchasing an item in local store " + this.store);
            return this.LocalPurchaseItem(customerID, itemID, dateOfPurchase);
        } else {
        	// If purchase is foreign send purchase request to the UDP server for the specific store
            logger.info("Customer " + customerID + " purchasing an item in foreign store " + purchase_store);
                logger.info("Sending UDP command to " + purchase_store + " store...");
                String cmd = "PURCHASE-ITEM-FOREIGN" + "," + customerID + "," + customer.getBalance() + "," + itemID + "," + dateOfPurchase;
                String response = this.sendCommand(this.ports.get(itemID.substring(0, 2)), cmd);
                if (response.startsWith("Purchased")) {
                    long remaining_balance = Long.parseLong(response.split(",")[1].trim());
                    customer.setBalance(remaining_balance);
                    return ("Successfully purchased.");
                } else {
                    return response;
                }
            }
    }
	
	public String LocalFindItem(String itemName) {
        String no_found_items = "";
        for (Map.Entry<String, String> entry : this.itemStore.entrySet()) {
            String name = entry.getValue().split(",")[0];
            if (itemName.trim().equals(name)) {
                return entry.getValue();
            }
        }
        return no_found_items;
	}
	
	@Override
	public String findItem(String customerID, String itemName) {
		// Get all items in local store
        String found_items = this.LocalFindItem(itemName);
        for(Map.Entry<String,Integer> entry: this.ports.entrySet()){
            if(entry.getKey().equals(this.store.toString())){
            	continue;
            }
            else {
                String cmd = "FIND-ITEM," + itemName;
                logger.info("Store server sending UDP request to find item.");
                String result = this.sendCommand(entry.getValue(), cmd);
                if(result.trim().equals("")) {
                	continue;
                }
                else {
                    found_items = found_items + ";" + this.sendCommand(entry.getValue(), cmd);
                }
            }
            if(found_items.equals("")){
                return "No item found with name " + itemName;
            }
        }
        return found_items.trim();
	}
	
	public boolean validateReceipt(String customerID, String itemID, String date) throws ParseException {
        for(String receipt : this.purchaseLog){
            String[] receipt_details = receipt.split(",");
            String log_itemID = receipt_details[0];
            String log_customerID = receipt_details[1];
            DateFormat format = new SimpleDateFormat("MMMM d yyyy", Locale.ENGLISH);
            Date purchase_date = format.parse(receipt_details[2]); 
            long return_range = 30l * 24 * 60 * 60 * 1000;
            Date limit_date = new Date(purchase_date.getTime() + return_range);
            Date return_date = format.parse(date); 
            // If receipt item matches with client and returned itemID
            if(log_itemID.equals(itemID) && log_customerID.equals(customerID)) {
                if (return_date.compareTo(limit_date) < 0) {
                    return true;
                }
            }
        }
        return false;
	}
	
    public String ForeignReturnItem(String customerID, String itemID, String dateOfReturn) throws NumberFormatException, ParseException {
        // Validate if this return is eligible by checking store receipts
    	if(this.validateReceipt(customerID, itemID, dateOfReturn)){
            String[] item_details = this.itemStore.get(itemID).split(",");
            int item_price = Integer.parseInt(item_details[2]);
         // Requires manager to add item back
            this.addItem("RETURN", itemID, item_details[0], 1, item_price);
            logger.info("Customer " + customerID + " returned " + itemID + " on " + dateOfReturn);
            // Return refund amount
            String refund_amount = this.itemStore.get(itemID).split(",")[1];
            return "SUCCESS,"+ refund_amount;
        }
        else{
            return "FAILED";
        }
    }
    
	@Override
	public boolean returnItem(String customerID, String itemID, String dateOfReturn) {
        Customer customer = this.Customers.get(customerID);
        String item_store = itemID.substring(0,2); 
        String current_store = this.store.toString();
        // If item to be returned in local store
        if(item_store.equals(current_store)){
            try {
            	// Validate if this return is eligible by checking store receipts
				if(this.validateReceipt(customerID, itemID, dateOfReturn)){
				    String[] item_details = this.itemStore.get(itemID).split(",");
				    int item_price = Integer.parseInt(item_details[2]);
				    // Requires manager to add item back
				    this.addItem("RETURN", itemID, item_details[0], 1, item_price);
				    // Refund the customer
				    long current_balance = customer.getBalance();
				    customer.setBalance(current_balance + item_price);
				    logger.info("Customer " + customerID + " returned " + itemID + "on " + dateOfReturn);
				    return true;
				}
				else{
					// If not valid receipt do not return item
				    return false;
				}
			} catch (NumberFormatException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else{
        	// If item to be returned in a foreign store
            int port = this.ports.get(item_store);
            logger.info("Store Server sending UDP request to return item...");
            String response = this.sendCommand(port,"RETURN," + itemID + "," + customerID + "," + dateOfReturn);
            if(response.startsWith("FAILED")){
                return false;
            }
            else if(response.startsWith("SUCCESS")){
            	String[] item_details = this.sendCommand(port,"GET-ITEM," + itemID).split(",");
                int item_price = Integer.parseInt(item_details[2].trim());
                long current_balance = customer.getBalance();
                customer.setBalance(current_balance + (long) item_price);
                logger.info("Customer " + customerID + " returned " + itemID + " on " + dateOfReturn);
                return true;
            }
        }
        return false;
	}
	
	@Override
	public boolean exchangeItem(String customerID, String newitemID, String oldItemID, String dateOfExchange) {
        int oldItemPrice = 0;
        int newItemPrice = 0;
		// If item to be exchanged is in local store
        if(oldItemID.startsWith(this.store.toString())){
        	// Validate if this return is eligible by checking store receipts
            try {
				if(!this.validateReceipt(customerID, oldItemID, dateOfExchange)){
				    this.logger.info("Cannot exchange unbought item.");
				    return false;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String[] item_details = this.itemStore.get(oldItemID).split(",");
            oldItemPrice = Integer.parseInt(item_details[2]);
        }
        else{
        	String item_store = oldItemID.substring(0,2);
            if(this.sendCommand(this.ports.get(item_store), "VALIDATE-RECEIPT," + customerID + "," + oldItemID + "," + dateOfExchange).equals("false")){
                this.logger.info("Cannot exchange unbought item.");
                return false;
            }
            String[] item_details = this.sendCommand(this.ports.get(oldItemID.substring(0,2)), "GET-ITEM," + oldItemID).trim().split(",");
            oldItemPrice = Integer.parseInt(item_details[2]);
        }
        if(newitemID.startsWith(this.store.toString())){
            String[] item_details = this.itemStore.get(newitemID).split(",");
            if(Integer.parseInt(item_details[1]) == 0){
                this.logger.info("Item is out of stock.");
                return false;
            }
            newItemPrice = Integer.parseInt(item_details[2]);
        }
        else{
            String[] item_details = this.sendCommand(this.ports.get(newitemID.substring(0,2)), "GET-ITEM," + newitemID).trim().split(",");
            if(Integer.parseInt(item_details[1]) == 0){
                this.logger.info("Item is out of stock.");
                return false;
            }
            newItemPrice = Integer.parseInt(item_details[2]);
        }
        //check budget
        Customer customer = this.Customers.get(customerID);
        long different_to_pay = newItemPrice-oldItemPrice;
        if(customer.getBalance() < different_to_pay){
            this.logger.info("Insufficient funds to perform the exchange.");
            return false;
        }
        this.returnItem(customerID, oldItemID, dateOfExchange);
        this.purchaseItem(customerID, newitemID, dateOfExchange);
        return true;
	}
	
	public void addLocalCustomerWaitList(String customerID, String itemID) {
		// Check if wait-list for item exist and add customer to it
        if (this.itemWaitList.containsKey(itemID)) {
            this.itemWaitList.get(itemID).add(customerID);
        } else {
        	// Create a new key for item if does not exist
            PriorityQueue<String> queue = new PriorityQueue<String>();
            queue.add(customerID);
            this.itemWaitList.put(itemID, queue);
        }
        logger.info("Added " + customerID + "to the waitlist");		
	}
	
	public void addCustomerWaitList(String customerID, String itemID) {
		// If item belongs to local store then add to local wait list
        if (itemID.substring(0, 2).equals(this.store.toString())) {
            this.addLocalCustomerWaitList(customerID, itemID);
        }
        else{
        	// If item belongs to foreign store then add to foreign wait list
            int port = this.ports.get(itemID.substring(0,2));
            String message = "WAITLIST," + customerID + "," + itemID;
            this.sendCommand(port,message);
        }
	}
	
	public void addCustomer(String customerID) {
        Customer customer = new Customer(customerID, this.store);
        this.Customers.put(customerID, customer);
	}
	
	public void addManager(String managerID) {
        Manager manager = new Manager(managerID, this.store);
        this.Managers.put(managerID, manager);
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
            System.out.println("Command sent to respective store server " + port + " is: " + new String(request.getData()));
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            response = new String(reply.getData());
            System.out.println("Response received from the respective store server " + port + " is: " + new String(reply.getData()));
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } 
        catch (Exception e){System.out.println(e.getMessage());}
        finally {
            if (aSocket != null)
                aSocket.close();
        }
        return response;
		
	}
	
	public void receive() throws NumberFormatException, ParseException{
		System.out.println("test");
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(this.ports.get(this.store.toString()));
            byte[] buffer = new byte[1000];
            System.out.println("UDP Server "+this.store.toString() + " has started listening...");
            String replyMessage = null;
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String[] requestArgs = new String(request.getData()).split(",");
                if(requestArgs[0].equals("PURCHASE-ITEM-FOREIGN")){
                    String customerID = requestArgs[1];
                    int balance = Integer.parseInt(requestArgs[2]);
                    String itemID = requestArgs[3];
                    String date = requestArgs[4];
					replyMessage = this.ForeignPurchaseItem(customerID, balance, itemID, date);
                }else if(requestArgs[0].equals("WAITLIST")){
                    String customerID = requestArgs[1];
                    String itemID = requestArgs[2];
                    this.addLocalCustomerWaitList(customerID, itemID);
                }else if(requestArgs[0].equals("FIND-ITEM")){
                    String itemName = requestArgs[1];
                    replyMessage = this.LocalFindItem(itemName);
                } else if(requestArgs[0].equals("GET-ITEM")){
                    replyMessage = this.itemStore.get(requestArgs[1].trim());
                }else if(requestArgs[0].equals("RETURN")){
                    String itemID = requestArgs[1];
                    String customerID = requestArgs[2];
                    String dateOfReturn = requestArgs[3];
                    replyMessage = this.ForeignReturnItem(customerID, itemID, dateOfReturn);
                }
                else if(requestArgs[0].equals("PURCHASE-ITEM")){
                    String customerID = requestArgs[1];
                    String itemID = requestArgs[2];
                    String date = requestArgs[3];
                    replyMessage = this.purchaseItem(customerID, itemID, date);
                }
                else if(requestArgs[0].equals("VALIDATE-RECEIPT")){
                    String customerID = requestArgs[1];
                    String itemID = requestArgs[2];
                    String date = requestArgs[3];
                    replyMessage = String.valueOf(this.validateReceipt(customerID, itemID, date));
                }
                DatagramPacket reply = new DatagramPacket(replyMessage.getBytes(), replyMessage.length(), request.getAddress(),request.getPort());
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
	
	public void shutdown() {
		// TODO Auto-generated method stub	
	}
}
