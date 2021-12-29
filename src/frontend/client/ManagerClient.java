package frontend.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import frontend.utils.UserInput;
import org.omg.CORBA.ORB;

import frontend.corba.frontend;
import frontend.utils.ClientLauncher;
import Models.Store;

public class ManagerClient {
    private Logger logger;
    private String managerID;

    public ManagerClient(String managerID) {
        this.managerID = managerID;
//        this.logger = this.startLogger();
    }

    // Handles logging
    public Logger startLogger() {
        Logger logger = Logger.getLogger("client-log");
        FileHandler fh;
        try {
            fh = new FileHandler("C:\\Users\\Waqar's PC\\Downloads\\Sample Source Code  Java IDL (CORBA)-20201013\\Reference Book\\soen423-project\\bin\\frontend\\logs\\client\\" + this.managerID + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    public static void main(String[] args) {
        try {
            UserInput in = new UserInput();

            System.out.println("Choose store location:");
            String input = in.promptStore();
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
            System.out.println("Enter Manager ID: ");
            String IDNumber = in.promptID();
            String clientID = store.toString() + "M" + IDNumber;
            System.out.println("Manager ID: " + clientID);

            ORB orb = ORB.init(args, null);
            ManagerClient manager = new ManagerClient(clientID);
            TimeUnit.MILLISECONDS.sleep(100);

            frontend server = ClientLauncher.getFEInterface(orb);
            String itemID;
            String itemName;
            int price;
            int quantity;
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("1. Add Item");
                System.out.println("2. Remove Item ");
                System.out.println("3. List Available Items ");
                int managerOption = Integer.parseInt(in.promptManagerOptions());
                String response;
                switch (managerOption) {
                    case 1:
                        System.out.println("----ADD ITEM----");
                        System.out.println("Enter ID:");
                        itemID = in.promptID();
                        System.out.println("Enter name:");
                        itemName = in.promptItemName();
                        System.out.println("Enter price:");
                        price = in.promptPrice();
                        System.out.println("Enter quantity:");
                        quantity = in.promptAddQuantity();
                        //manager.logger.info("Manager client with ID: " + manager.managerID + " attempt to add item: " + store + itemID);
                        response = server.addItem(manager.managerID, store + itemID, itemName, quantity, price);
                        System.out.println(response);
                        System.out.printf("%n");
                        break;
                    case 2:
                        System.out.println("----REMOVE ITEM----");
                        System.out.println("Enter ID:");
                        itemID = in.promptID();
                        System.out.println("Enter quantity (enter -1 to completely remove item):");
                        quantity = in.promptRemoveQuantity();
                        //manager.logger.info("Manager ID "+ manager.managerID + " attempt to remove item: " + store + itemID);
                        response = server.removeItem(manager.managerID, store + itemID, quantity);
                        System.out.println(response);
                        System.out.printf("%n");
                        break;
                    case 3:
                        System.out.println("----LIST AVAILABE ITEMS----");
                        //manager.logger.info("Manager "+ manager.managerID + " attempt to view available items");
                        response = server.listItemAvailability(manager.managerID);
                        System.out.println(response);
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }
    }
}
