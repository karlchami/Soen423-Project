package frontend.client;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;

import frontend.corba.frontend;
import frontend.utils.ClientLauncher;
import Models.Store;

public class CustomerClient {
    private Logger logger;
    private String customerID;

    public CustomerClient(String customerID) {
        this.customerID = customerID;
//        this.logger = this.startLogger();
    }

    // Handles logging
    public Logger startLogger() {
        Logger logger = Logger.getLogger("client-log");
        FileHandler fh;
        try {
            fh = new FileHandler("C:\\Users\\Waqar's PC\\Downloads\\Sample Source Code  Java IDL (CORBA)-20201013\\Reference Book\\soen423-project\\bin\\frontend\\" + this.customerID + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose store location:");
        String input = scanner.next();
        Store store = null;
        switch (input) {
            case "BC":
                store = Store.BC;
                break;
            case "ON":
                store = Store.ON;
                break;
            case "QC":
                store = Store.QC;
                break;

        }
        System.out.println("Enter Customer ID: ");
        String IDNumber = scanner.next();
        String clientID = store.toString() + "U" + IDNumber;
        System.out.println("Customer ID: " + clientID);
        try {
            ORB orb = ORB.init(args, null);
            CustomerClient customer = new CustomerClient(clientID);
            TimeUnit.SECONDS.sleep(1);

            frontend server = ClientLauncher.getFEInterface(orb);
            int customerOption;
            String itemID;
            String inputDate;
            String itemName;
            String newItemID;
            String response;
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("1. Purchase Item");
                System.out.println("2. Find Item ");
                System.out.println("3. Return Item ");
                System.out.println("4. Exchange Item ");
                customerOption = scanner.nextInt();
                switch (customerOption) {
                    case 1:
                        System.out.println("----PURCHASE ITEM----");
                        System.out.println("Enter item ID:");
                        itemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter the date of purchase (MMMM dd, yyyy):");
                        inputDate = scanner.nextLine();
                        response = server.purchaseItem(customer.customerID, itemID, inputDate);
                        // TODO: Fix here
                        if ("Out of stock".equals(response)) {
                            System.out.println("Item is out of stock, waitlist?");
                            String option = scanner.next();
                            if (option.equals("y")) {
                                server.addCustomerWaitList(customer.customerID, itemID);
                                System.out.println("Successfully waitlisted. Item will be automatically bought when available.");
                            }
                        } else {
                            //customer.logger.info(response);
                            System.out.println(response);
                        }
                        break;
                    case 2:
                        System.out.println("----FIND ITEM----");
                        System.out.println("Enter item name:");
                        itemName = scanner.next();
                        response = server.findItem(customer.customerID, itemName);
                        System.out.println(response);
                        //customer.logger.info(response);
                        break;
                    case 3:
                        System.out.println("----RETURN ITEM----");
                        System.out.println("Enter item ID:");
                        itemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter the return date (MMMM dd, yyyy):");
                        inputDate = scanner.nextLine();
                        response = server.returnItem(customer.customerID, itemID, inputDate);
                        System.out.println(response);
                        //customer.logger.info(response);
                        break;
                    case 4:
                        System.out.println("----EXCHANGE ITEM ----");
                        System.out.println("Enter old item ID:");
                        itemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter new item ID:");
                        newItemID = scanner.next();
                        scanner.nextLine();
                        System.out.println("Enter return date (MMMM dd, yyyy):");
                        inputDate = scanner.nextLine();
                        response = server.exchangeItem(customer.customerID, newItemID, itemID, inputDate);
                        System.out.println(response);
                        //customer.logger.info(response);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }
    }
}
