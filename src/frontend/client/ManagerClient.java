package frontend.client;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;

import frontend.corba.frontend;
import frontend.utils.ClientLauncher;
import Models.Store;

public class ManagerClient {
	private ORB orb;	
    private UUID uuid;
	private Logger logger;
	private String managerID;

	public ManagerClient(ORB orb, String managerID, UUID uuid) {
        this.orb = orb;
        this.managerID = managerID;
        this.uuid = uuid;
        this.logger = this.startLogger();
	}
	
	// Handles logging
	public Logger startLogger() {
	    Logger logger = Logger.getLogger("client-log");
	    FileHandler fh;
	    try {
	        fh = new FileHandler("frontend/logs/client/" + this.managerID + ".log");
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
	
	public static void main(String args[]) {
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
        System.out.println("Enter Manager ID: ");
        String IDNumber = scanner.next();
        String clientID = store.toString() + "M" + IDNumber;
        System.out.println("Manager ID: " + clientID);
        try{
      	  	ClientLauncher.initializeORB(args, store.toString());
      	  	ORB orb = ORB.init(args, null);
            ManagerClient manager = new ManagerClient(orb, clientID, UUID.randomUUID());
            frontend server = ClientLauncher.getFEInterface(orb, store.toString());
            int customerOption;
            String itemID;
            String itemName;
            int price;
            int quantity;
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("1. Add Item");
                System.out.println("2. Remove Item ");
                System.out.println("3. List Available Items ");
                customerOption = scanner.nextInt();
                String response;
                switch(customerOption){
                    case 1:
                        System.out.println("----ADD ITEM----");
                        System.out.println("Enter ID:");
                        itemID = scanner.next();
                       System.out.println("Enter name:");
                        itemName = scanner.next();
                        System.out.println("Enter price:");
                        price = scanner.nextInt();
                        System.out.println("Enter quantity:");
                        quantity = scanner.nextInt();
                        manager.logger.info("Manager client with ID: " + manager.managerID + " attempt to add item: " + store + itemID);
                        response = server.addItem(manager.managerID, store + itemID, itemName, quantity, price);
                        System.out.println(response);
                        System.out.printf("%n");
                        break;
                    case 2:
                        System.out.println("----REMOVE ITEM----");
                        System.out.println("Enter ID:");
                        itemID = scanner.next();
                        System.out.println("Enter quantity:");
                        quantity = scanner.nextInt();
                        manager.logger.info("Manager ID "+ manager.managerID + " attempt to remove item: " + store + itemID);
                        response = server.removeItem(manager.managerID, itemID, quantity);
                        System.out.println();
                        System.out.printf("%n");
                        break;
                    case 3:
                        System.out.println("----LIST AVAILABE ITEMS----");
                        manager.logger.info("Manager "+ manager.managerID + " attempt to view available items");
                        response = server.listItemAvailability(manager.managerID);
                        System.out.println(response);
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
       }
	} 
}
