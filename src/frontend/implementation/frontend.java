package frontend.implementation;

import java.net.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.text.ParseException;

import frontend.corba.frontendPOA;

public class frontend extends frontendPOA  {

	private org.omg.CORBA.ORB orb = null;
	
	private static int port;
	private Logger log = null;
	private String frontend_id;
	
	public frontend(ORB orb, String frontend_id) throws AlreadyBoundException, IOException {
			super();
		
			this.orb = orb;
			this.frontend_id = frontend_id;
			
			log = startLogger(frontend_id);
			log.info("Frontend started on port " + port);	
	}
	
	public String addItem(String managerID, String itemID, String itemName, int quantity, int price) {
		return null;
	}

	public String removeItem(String managerID, String itemID, int quantity) {
		return null;
	}
	
	public String listItemAvailability(String managerID){		
		return null;
	}
	
	public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
		return null;	
	}
	
	public String findItem(String customerID, String itemName) {
		return null;
	}
		
	public String returnItem (String customerID, String itemID, String dateOfReturn) {	
		return null;
	}	
	
	public String exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
		return null;
	}
		
	public String addCustomerWaitList(String customerID, String itemID) {
		return null;
	}
		
    public Logger startLogger(String frontend_id) {
        Logger logger = Logger.getLogger("frontend-log");
        FileHandler fh;
        try {
            fh = new FileHandler("logs/frontend/" + frontend_id + ".log");
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
    
	public void shutdown() {
		this.orb.shutdown(false);
	}
	
}