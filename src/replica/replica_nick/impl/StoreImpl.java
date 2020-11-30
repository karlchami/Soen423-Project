package replica.replica_nick.impl;

import replica.replica_nick.models.Customer;
import replica.replica_nick.models.Item;
import replica.replica_nick.models.Purchase;
import replica.replica_nick.utility.StoreLogger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;

public class StoreImpl {
    // region Instance Variables
    private String storePrefix;
    private HashMap<String, Item> inventory;
    private HashMap<String, Customer> customers;
    private HashMap<String, LinkedList<String>> waitList;
    private StoreLogger logger;
    // endregion

    // region Constructors
    public StoreImpl(String storePrefix) {
        super();
        this.storePrefix = storePrefix;
        inventory = new HashMap<>();
        customers = new HashMap<>();
        waitList = new HashMap<>();
        logger = new StoreLogger(storePrefix);
        setDefaultValues();
    }

    // endregion

    public String getStorePrefix() {
        return storePrefix;
    }

    // region Store Setup

    private void setDefaultValues() {
//        inventory.put(storePrefix + "1001", new Item("Laptop", 10, 600));
//        inventory.put(storePrefix + "1002", new Item("iPhone", 8, 1000));
//        inventory.put(storePrefix + "1003", new Item("Monitor", 12, 1300));
//        inventory.put(storePrefix + "1004", new Item("Universal Remote", 18, 40));
//        inventory.put(storePrefix + "1005", new Item("Toaster", 4, 300));
//        inventory.put(storePrefix + "1006", new Item("TV", 6, 500));
//        inventory.put(storePrefix + "1007", new Item("Dishwasher", 0, 800));
//        inventory.put(storePrefix + "1008", new Item("Earphones", 30, 20));
//        inventory.put(storePrefix + "1009", new Item("PlayStation", 5, 500));
//        inventory.put(storePrefix + "1010", new Item("USB Cable", 15, 25));

//        if (storePrefix.equals("QC")) {
//            inventory.put(storePrefix + "6231", new Item("Tea", 2, 30));
//            inventory.put(storePrefix + "6651", new Item("Chocolates", 2, 30));
//        } else if (storePrefix.equals("ON")) {
//            inventory.put(storePrefix + "6231", new Item("Tea", 1, 10));
//            inventory.put(storePrefix + "7000", new Item("Dice Set", 10, 25));
//        }
    }

    // endregion

    // region Manager Actions

    public synchronized String addItem(String managerID, String itemID, String itemName, int quantity, int price) {
        String result;

        if (!managerID.substring(0, 2).equalsIgnoreCase(itemID.substring(0, 2))) {
            result = "Failed to add item " + itemID + ". You can only add items to your own store.";
        } else if (inventory.containsKey(itemID)) {
            Item item = inventory.get(itemID);
            if (!item.getName().equals(itemName)) {
                inventory.get(itemID).addQuantity(quantity);
                inventory.get(itemID).setName(itemName);
                updateWaitList(itemID);
                result = managerID + " - Success. Added " + quantity + " units of item " + itemID + " to inventory.";
            } else {
                inventory.get(itemID).addQuantity(quantity);
                updateWaitList(itemID);
                result = managerID + " - Success. Added " + quantity + " units of item " + itemID + " to inventory.";
            }
        } else {
            inventory.put(itemID, new Item(itemName, quantity, price));
            result = managerID + " - Success. Added " + quantity + " units of NEW item " + itemID + " to inventory.";
        }
        logger.log(managerID, result);
        return result;
    }

    public synchronized String removeItem(String managerID, String itemID, int quantity) {
        String result;

        if (!managerID.substring(0, 2).equalsIgnoreCase(itemID.substring(0, 2))) {
            result = managerID + " - Failed to remove item " + itemID + ". You can only remove items from your own store.";
        } else if (inventory.containsKey(itemID)) {
            Item item = inventory.get(itemID);
            if (quantity == -1) {
                inventory.remove(itemID);
                result = managerID + " - Success. Completely removed item " + itemID + " from inventory.";
            } else if (item.getQuantity() >= quantity) {
                item.removeQuantity(quantity);
                result = managerID + " - Success. Removed " + quantity + " units of item " + itemID + " from inventory.";
            } else {
                result = managerID + " - Failed to remove item " + itemID + ". Quantity entered must not exceed the current quantity in stock.";
            }
        } else {
            result = managerID + " - Failed to remove item " + itemID + ". Item does not exist.";
        }
        logger.log(managerID, result);
        return result;
    }

    public synchronized String listItemAvailability(String managerID) {
        StringBuilder itemList = new StringBuilder();

        inventory.forEach((itemID, item) -> {
            itemList.append(itemID)
                    .append(",")
                    .append(item.getName())
                    .append(",")
                    .append(item.getQuantity())
                    .append(",")
                    .append(item.getPrice())
                    .append(";");
        });

        logger.log(managerID, "Retrieved list of item availability for " + storePrefix + " store.");
        return itemList.toString();
    }

    // endregion

    // region Customer Actions

    public synchronized String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        Customer customer = getCustomer(customerID);
        String itemPrefix = storePrefix(itemID);
        int price = checkPrice(itemID);

        String result = purchaseCheck(customerID, itemID);

        if (failed(result)) {
            logger.log(customerID, result);
            return result;
        }

        if (itemPrefix.equals(storePrefix)) {
            Item item = inventory.get(itemID);
            item.decrementQuantity();
            result = customerID + " - Success. You have purchased a " + item.getName() + " (" + itemID + ") for $" + price;
            logger.log(customerID, result);
        } else {
            result = forwardRequest(itemPrefix, "purchase_" + customerID + "_" + itemID);
            customer.setLimit(itemID);
        }
        customer.addPurchase(new Purchase(itemID, price, date(dateOfPurchase)));
        customer.decreaseBalance(price);
        return result;
    }

    public synchronized String findItem(String customerID, String itemName) {
        StringBuilder results = new StringBuilder();

        results.append(searchResults(itemName));

        String requestMessage = "find_" + itemName;

        if (!storePrefix.equals("QC")) {
            results.append(sendPacket(requestMessage, 5001));
        }
        if (!storePrefix.equals("ON")) {
            results.append(sendPacket(requestMessage, 5002));
        }
        if (!storePrefix.equals("BC")) {
            results.append(sendPacket(requestMessage, 5003));
        }

        logger.log(customerID, "You searched for item \"" + itemName + "\".");
        return results.toString();
    }

    public synchronized String returnItem(String customerID, String itemID, String dateOfReturn) {
        Customer customer = getCustomer(customerID);
        Purchase purchase = customer.findPurchase(itemID);
        String itemPrefix = storePrefix(itemID);

        String result = returnCheck(customerID, itemID, dateOfReturn);

        if (failed(result)) {
            logger.log(customerID, result);
            return result;
        }

        if (itemPrefix.equals(storePrefix)) {
            Item item = inventory.get(itemID);
            if (item != null) {
                item.incrementQuantity();
                result = customerID + " - Success. You have returned a " + item.getName() + " (" + itemID + ") for $" + item.getPrice();
            } else {
                result = customerID + " - Success. You have returned item + " + itemID + " (discontinued) for $" + purchase.getPrice();
            }
            logger.log(customerID, result);
            updateWaitList(itemID);
        } else {
            customer.resetLimit(itemID);
            result = forwardRequest(itemPrefix, "return_" + customerID + "_" + itemID + "_" + purchase.getPrice());
        }

        customer.removePurchase(purchase);
        customer.increaseBalance(purchase.getPrice());
        return result;
    }

    public synchronized String exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        String result;

        result = returnCheck(customerID, oldItemID, dateOfExchange);

        if (failed(result)) {
            result = customerID + " - Exchange failed. " + result;
            logger.log(customerID, result);
            return result;
        }

        Customer customer = getCustomer(customerID);
        int oldPrice = customer.findPurchase(oldItemID).getPrice();

        customer.increaseBalance(oldPrice);
        customer.resetLimit(oldItemID);
        result = purchaseCheck(customerID, newItemID);
        customer.decreaseBalance(oldPrice);
        customer.setLimit(oldItemID);

        if (failed(result)) {
            result = customerID + " - Exchange failed. " + result;
            logger.log(customerID, result);
            return result;
        }

        returnItem(customerID, oldItemID, dateOfExchange);
        purchaseItem(customerID, newItemID, dateOfExchange);
        result = customerID + " - Success. You have exchanged item " + oldItemID + " for item " + newItemID + ".";
        logger.log(customerID, result);
        return result;
    }

    // endregion

    // region Store Logic

    public synchronized String searchResults(String itemName) {
        StringBuilder results = new StringBuilder();
        inventory.forEach((itemID, item) -> {
            if (item.getName().equalsIgnoreCase(itemName)) {
                results.append(itemID)
                        .append(",")
                        .append(item.getName())
                        .append(",")
                        .append(item.getQuantity())
                        .append(",")
                        .append(item.getPrice())
                        .append(";");
            }
        });
        return results.toString();
    }

    private synchronized String purchaseCheck(String customerID, String itemID) {
        String result;
        Customer customer = getCustomer(customerID);
        String itemPrefix = storePrefix(itemID);
        int qty = checkQuantity(itemID);
        int price = checkPrice(itemID);

        if (qty == -1 || price == -1) {
            result = customerID + " - Failed to purchase item " + itemID + ". There are no items with this ID.";
        } else if (qty <= 0) {
            result = customerID + " - Failed to purchase item " + itemID + ". Item is out of stock.";
        } else if (customer.getBalance() < price) {
            result = customerID + " - Failed to purchase item " + itemID + ". Insufficient funds.";
        } else if (!itemPrefix.equals(storePrefix) && customer.isLimitReached(itemID)) {
            result = customerID + " - Failed to purchase item " + itemID + ". You have reached your 1 item limit for the " + itemPrefix + " store.";
        } else {
            result = "Success";
        }
        return result;
    }

    private synchronized String returnCheck(String customerID, String itemID, String dateOfReturn) {
        String result;
        Purchase purchase = getCustomer(customerID).findPurchase(itemID);

        if (purchase == null) {
            result = customerID + " - Failed to return item " + itemID + ". Could not find purchase.";
        } else if (purchase.getDateOfPurchase().isBefore(date(dateOfReturn).minusDays(30))) {
            result = customerID + " - Failed to return item " + itemID + ". You must return items within 30 days of purchase.";
        } else {
            return "Success";
        }
        return result;
    }

    public synchronized String nonLocalPurchase(String customerID, String itemID) {
        Item item = inventory.get(itemID);
        item.decrementQuantity();
        String result = customerID + " - Success. You have purchased a " + item.getName() + " (" + itemID + ") for $" + item.getPrice();
        logger.log(customerID, result);
        return result;
    }

    public synchronized String nonLocalReturn(String customerID, String itemID, int price) {
        String result;
        Item item = inventory.get(itemID);

        if (item != null) {
            item.incrementQuantity();
            result = customerID + " - Success. You have returned a " + item.getName() + " (" + itemID + ") for $" + item.getPrice();
        } else {
            result = customerID + " - Success. You have returned item " + itemID + " (discontinued) for $" + price;
        }

        Runnable task = () -> {
            updateWaitList(itemID);
        };
        Thread thread = new Thread(task);
        thread.start();

        logger.log(customerID, result);
        return result;
    }

    public synchronized String addToWaitList(String itemID, String customerID) {
        String result;

        if (itemID.substring(0, 2).equalsIgnoreCase(storePrefix)) {
            waitList.putIfAbsent(itemID, new LinkedList<>());
            waitList.get(itemID).add(customerID);

            result = "You have been added to the wait list for item " + itemID + ".";
            logger.log(customerID, result);
            return result;
        }
        result = forwardRequest(itemID.substring(0, 2), "addwaitlist_" + itemID + "_" + customerID);
        return result;
    }

    private synchronized void updateWaitList(String itemID) {
        if (!waitList.containsKey(itemID)) {
            return;
        }

        Item item = inventory.get(itemID);
        if (item == null) {
            waitList.remove(itemID);
            return;
        }

        LinkedList<String> queue = waitList.get(itemID);
        String successMessage = "Your wait listed item has become available. " +
                "You have purchased a " + item.getName() + " (" + itemID + ") for $" + item.getPrice();
        String failedMessage = "Your wait listed item has become available. " +
                "Failed to purchase item " + itemID + ". Insufficient funds.";

        while (item.getQuantity() > 0) {
            String head = null;
            Customer customer = null;
            boolean local = true;

            do {
                if (customer != null) {
                    logger.log(head, failedMessage);
                }
                head = queue.poll();
                if (head == null) {
                    return;
                }
                if (!head.substring(0, 2).equalsIgnoreCase(storePrefix)) {
                    String reply = forwardRequest(head.substring(0, 2), "waitlist_" + head + "_" + itemID + "_" + item.getPrice());
                    if (reply.contains("Success")) {
                        item.decrementQuantity();
                        local = false;
                        logger.log(head, successMessage);
                        break;
                    } else if (reply.contains("Insufficient funds")) {
                        logger.log(head, failedMessage);
                    } else if (reply.contains("Limit")) {
                        logger.log(head, "Your wait listed item has become available. " +
                                "Failed to purchase item " + itemID + ". " +
                                "You have reached your 1 item limit for the " + storePrefix + " store.");
                    }
                    continue;
                }
                customer = customers.get(head);
            } while (customer == null || customer.getBalance() < item.getPrice());

            if (!local) {
                continue;
            }

            customer.addPurchase(new Purchase(itemID, item.getPrice(), LocalDate.now()));
            customer.decreaseBalance(item.getPrice());
            item.decrementQuantity();
            logger.log(head, successMessage);
        }
    }

    public synchronized String nonLocalWaitList(String customerID, String itemID, int price) {
        String result;
        Customer customer = getCustomer(customerID);

        if (customer.getBalance() < price) {
            result = "Insufficient funds";
        } else if (customer.isLimitReached(itemID)) {
            result = "Limit";
        } else {
            customer.addPurchase(new Purchase(itemID, price, LocalDate.now()));
            customer.decreaseBalance(price);
            customer.setLimit(itemID);
            result = "Success";
        }
        return result;
    }

    private synchronized int checkQuantity(String itemID) {
        String prefix = storePrefix(itemID);
        if (prefix.equals(storePrefix)) {
            Item item = inventory.get(itemID);
            return item != null ? item.getQuantity() : -1;
        }
        String qty = forwardRequest(prefix, "quantity_" + itemID);
        return Integer.parseInt(qty);
    }

    public synchronized String getQuantity(String itemID) {
        Item item = inventory.get(itemID);
        return item != null ? Integer.toString(item.getQuantity()) : "-1";
    }

    private synchronized int checkPrice(String itemID) {
        String prefix = storePrefix(itemID);
        if (prefix.equals(storePrefix)) {
            Item item = inventory.get(itemID);
            return item != null ? item.getPrice() : -1;
        }
        String price = forwardRequest(prefix, "price_" + itemID);
        return Integer.parseInt(price);
    }

    public synchronized String getPrice(String itemID) {
        Item item = inventory.get(itemID);
        return item != null ? Integer.toString(item.getPrice()) : "-1";
    }

    private String storePrefix(String id) {
        return id.substring(0, 2);
    }

    private boolean failed(String result) {
        return !result.contains("Success");
    }

    // endregion

    // region Helper Methods

    private Customer getCustomer(String customerID) {
        customers.putIfAbsent(customerID, new Customer());
        return customers.get(customerID);
    }

    private String sendPacket(String message, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);

            byte[] messageBytes = message.getBytes();
            InetAddress address = InetAddress.getByName("localhost");

            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
            socket.send(packet);

            byte[] replyBuffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(replyBuffer, replyBuffer.length);
            socket.receive(reply);
            return new String(reply.getData(), 0, reply.getLength());
        } catch (SocketTimeoutException e) {
            System.out.println(storePrefix + "server: did not receive reply from server at port " + port + " for message:");
            System.out.println(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error communicating with server at port " + port + ".\n";
    }

    private String forwardRequest(String storePrefix, String requestMessage) {
        int port;
        switch (storePrefix) {
            case "QC":
                port = 5001;
                break;
            case "ON":
                port = 5002;
                break;
            case "BC":
                port = 5003;
                break;
            default:
                return "Invalid item ID.";
        }
        return sendPacket(requestMessage, port);
    }

    private LocalDate date(String date) {
        String[] dateValues = date.split("-");

        int day = Integer.parseInt(dateValues[0]);
        int month = Integer.parseInt(dateValues[1]);
        int year = Integer.parseInt(dateValues[2]);

        return LocalDate.of(year, month, day);
    }

    // endregion
}
