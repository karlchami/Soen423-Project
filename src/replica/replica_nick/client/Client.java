package replica.replica_nick.client;

import replica.replica_nick.impl.StoreInterface;
import replica.replica_nick.utility.UserInput;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public class Client {
    private static StoreInterface store;
    private static UserInput in = new UserInput();
    private static String userID;
    private static String storePrefix;
    private static String itemID;

    public static void main(String[] args) {
        boolean again = false;
        do {
            userID = in.promptUserID();
            storePrefix = userID.substring(0, 2).toUpperCase();
            connectToWebServer();

            try {
                if (isManager()) {
                    System.out.println(managerOptions());
                } else {
                    String reply = customerOptions();
                    System.out.println(reply);
                    if (reply.contains("out of stock") && !reply.contains("Exchange")) {
                        System.out.println(customerWaitList());
                    }
                }
                again = in.promptContinue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (again);
    }

    private static boolean isManager() {
        return String.valueOf(userID.charAt(2)).equalsIgnoreCase("M");
    }

    private static void connectToWebServer() {
        String host = "";
        switch (storePrefix) {
            case "QC":
                host = "http://localhost:8081/" + storePrefix + "?wsdl";
                break;
            case "ON":
                host = "http://localhost:8082/" + storePrefix + "?wsdl";
                break;
            case "BC":
                host = "http://localhost:8083/" + storePrefix + "?wsdl";
                break;
        }
        try {
            URL url = new URL(host);
            QName qName = new QName("http://impl.replica_nick.replica/", "StoreImplService");
            Service storeService = Service.create(url, qName);
            store = storeService.getPort(StoreInterface.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // region Manager Options

    private static String managerOptions() {
        System.out.println("Welcome to the " + storePrefix + " store. Here are your options as a manager:\n");
        String option = in.promptManagerOptions();

        switch (option) {
            case "1":
                return managerAdd();
            case "2":
                return managerRemove();
            case "3":
                return managerList();
            default:
                return "Error";
        }
    }

    private static String managerAdd() {
        String itemID = in.promptItemID();
        String itemName = in.promptItemName();
        int quantity = in.promptAddQuantity();
        double price = in.promptPrice();

        return store.addItem(userID, itemID, itemName, quantity, price);
    }

    private static String managerRemove() {
        String itemID = in.promptItemID();
        int quantity = in.promptRemoveQuantity();

        return store.removeItem(userID, itemID, quantity);
    }

    private static String managerList() {
        return store.listItemAvailability(userID);
    }

    // endregion

    // region Customer Options

    private static String customerOptions() {
        System.out.println("Welcome to the " + storePrefix + " store. Here are your options as a customer:\n");
        String option = in.promptCustomerOptions();

        switch (option) {
            case "1":
                return customerPurchase();
            case "2":
                return customerFind();
            case "3":
                return customerReturn();
            case "4":
                return customerExchange();
            default:
                return "Error";
        }
    }

    private static String customerPurchase() {
        itemID = in.promptItemID();
        String dateOfPurchase = in.promptDate();

        return store.purchaseItem(userID, itemID, dateOfPurchase);
    }

    private static String customerFind() {
        String itemName = in.promptItemName();

        return store.findItem(userID, itemName);
    }

    private static String customerReturn() {
        String itemID = in.promptItemID();
        String dateOfReturn = in.promptDate();

        return store.returnItem(userID, itemID, dateOfReturn);
    }

    private static String customerExchange() {
        System.out.println("[old item]:");
        String oldItemID = in.promptItemID();
        System.out.println("[new item]");
        String newItemID = in.promptItemID();
        String dateOfExchange = in.promptDate();

        return store.exchangeItem(userID, newItemID, oldItemID, dateOfExchange);
    }

    private static String customerWaitList() {
        if (in.promptWaitList()) {
            return store.addToWaitList(itemID, userID);
        }
        return "You have not been added to the wait list.";
    }

    // endregion
}
