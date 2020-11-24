package frontend.implementation;

import java.net.*;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.*;
import java.rmi.AlreadyBoundException;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.text.ParseException;

import frontend.corba.frontendPOA;
import frontend.utils.Tuple;
import frontend.utils.RequestBuilder;

public class frontend extends frontendPOA  {

	private org.omg.CORBA.ORB orb = null;
	private String frontend_id;
	private Logger log = null;
	
	// Local
	private int port = 5555;
	private InetAddress address = InetAddress.getLocalHost();
	private DatagramSocket socket;
	
	// Sequencer
	private int sequencer_port = 3333;
	private InetAddress sequencer_address = InetAddress.getLocalHost();
	
	// Stores RM info
	private ArrayList<Tuple<InetAddress, Integer, String>> rm_info = new ArrayList<Tuple<InetAddress, Integer, String>>();
	private static int delay;
	
	public frontend(ORB orb, String frontend_id) throws AlreadyBoundException, IOException {
			super();
		
			this.orb = orb;
			this.frontend_id = frontend_id;
			this.socket = new DatagramSocket(this.port, this.address);
			
			// Add RM info to tuple
			// TODO: Agree on port number for each RM
			InetAddress local_host = InetAddress.getLocalHost();
			rm_info.add(new Tuple<InetAddress, Integer, String>(local_host, 3000, "karl"));
			rm_info.add(new Tuple<InetAddress, Integer, String>(local_host, 3001, "waqar"));
			rm_info.add(new Tuple<InetAddress, Integer, String>(local_host, 3002, "nick"));
			// Set response delay
			this.delay = 999;
									
			log = startLogger(frontend_id);
			log.info("Frontend started on port " + port);	
	}
	
	public String addItem(String managerID, String itemID, String itemName, int quantity, int price) {
		String request = RequestBuilder.addItemRequest(managerID, itemID, itemName, quantity, price);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}
	}

	public String removeItem(String managerID, String itemID, int quantity) {
		String request = RequestBuilder.removeItemRequest(managerID, itemID, quantity);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	
	public String listItemAvailability(String managerID){	
		String request = RequestBuilder.listItemAvailabilityRequest(managerID);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	
	public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
		String request = RequestBuilder.purchaseItemRequest(customerID, itemID, dateOfPurchase);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}	
	}
	
	public String findItem(String customerID, String itemName) {
		String request = RequestBuilder.findItemRequest(customerID, itemName);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}
	}
		
	public String returnItem (String customerID, String itemID, String dateOfReturn) {	
		String request = RequestBuilder.returnItemRequest(customerID, itemID, dateOfReturn);
		String message = messageBuild(address, port, request);sendRequest(message, sequencer_address, sequencer_port);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}
	}	
	
	public String exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
		String request = RequestBuilder.exchangeItemRequest(customerID, newItemID, oldItemID, dateOfExchange);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}
	}
		
	public String addCustomerWaitList(String customerID, String itemID) {
		String request = RequestBuilder.addCustomerWaitListRequest(customerID, itemID);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return response;
		} catch (IOException e) {
			return e.getMessage();
		}
	}
		
	public Logger startLogger(String frontend_id) {
	    Logger logger = Logger.getLogger("frontend-log");
	    FileHandler fh;
	    try {
	        fh = new FileHandler("frontedn/logs/" + frontend_id + ".log");
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
	
    private static String messageBuild(InetAddress address, int port, String arguments) {
		return address.toString() + ";" + Integer.toString(port) + ";" + arguments;
	}
    
    public static void sendRequest(String message, InetAddress inet_address, int port) {
        boolean received = false;
        byte[] message_bytes = message.getBytes();
        DatagramPacket request = new DatagramPacket(message_bytes, message_bytes.length, inet_address, port);
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(delay);
            while (!received) {
                sendSocket.send(request);
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sendSocket.receive(reply);
                    String response = new String(reply.getData());
                    if (response != null) {
                        received = true;
                    }
                } catch (SocketTimeoutException e) {
                	return;
                }
            }
        } catch (IOException ex) {
        	return;
        }
    }
    
    private String recieve(DatagramSocket socket) throws IOException {
		return "";
    }
    
	public void shutdown() {
		this.orb.shutdown(false);
	}
	
}