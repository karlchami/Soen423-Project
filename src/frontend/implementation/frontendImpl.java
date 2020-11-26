package frontend.implementation;

import java.net.*;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.*;
import java.rmi.AlreadyBoundException;
import org.omg.CORBA.*;

import frontend.corba.frontendPOA;
import frontend.utils.Tuple;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import frontend.utils.RequestBuilder;

public class frontendImpl extends frontendPOA  {

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
	
	public frontendImpl(ORB orb, String frontend_id) throws AlreadyBoundException, IOException {
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
			// Set response delay (for reliable UDP)
			delay = 999;
									
			log = startLogger();
			log.info("Frontend started on port " + port);	
	}
	
	public String addItem(String managerID, String itemID, String itemName, int quantity, int price) {
		String request = RequestBuilder.addItemRequest(managerID, itemID, itemName, quantity, price);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return getResponseMessage(response);
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
			return getResponseMessage(response);
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
			return getResponseMessage(response);
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
			return getResponseMessage(response);
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
			return getResponseMessage(response);
		} catch (IOException e) {
			return e.getMessage();
		}
	}
		
	public String returnItem (String customerID, String itemID, String dateOfReturn) {	
		String request = RequestBuilder.returnItemRequest(customerID, itemID, dateOfReturn);
		String message = messageBuild(address, port, request);
		sendRequest(message, sequencer_address, sequencer_port);
		try {
			String response = recieve(socket);
			return getResponseMessage(response);
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
			return getResponseMessage(response);
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
			return getResponseMessage(response);
		} catch (IOException e) {
			return e.getMessage();
		}
	}
		
    private static String messageBuild(InetAddress address, int port, String request) {
		return address.toString() + ";" + Integer.toString(port) + ";" + request;
	}
    
    public static void sendRequest(String message, InetAddress inet_address, int port) {
        boolean received = false;
        byte[] message_bytes = message.getBytes();
        DatagramPacket request = new DatagramPacket(message_bytes, message_bytes.length, inet_address, port);
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(delay);
    		// Keeps listening until it gets a response, if no response comes in after delay, resend and wait again until received
            while (!received) {
                sendSocket.send(request);
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sendSocket.receive(reply);
                    String response = new String(reply.getData()).trim();
                    // TODO: Agree on a reply message that denotes a received request
                    if (response.equals("received")) {
                        received = true;
                    }
                } catch (SocketTimeoutException e) {
                	continue;
                }
            }
        } catch (IOException ex) {
        	return;
        }
    }
    
    private String recieve(DatagramSocket socket) throws IOException {
    	String response_message;
    	// Expected number of replies (RM info tuple size)
    	int reply_count = rm_info.size();  	
    	// Holds info about RMs that send UDP back to FE, used for tracking responses
    	ArrayList<Tuple<InetAddress, Integer, String>> received_rm = new ArrayList<Tuple<InetAddress, Integer, String>>(); 
    	// Determines whether we should keep receiving responses or not
    	Boolean receive = true;
    	
    	try {
    		while(receive) {
    			byte[] buffer = new byte[1000];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);
                // Add RM to received RMs tuple after receiving
                Tuple<InetAddress, Integer, String> rm = new Tuple<InetAddress, Integer, String>(response.getAddress(), response.getPort(), new String(response.getData()));
                
                if (!received_rm.contains(rm)) {
                	received_rm.add(rm);
                	// Sends back a reply to RM denoting that response is received
                	// TODO: Agree on reply message
                	sendRequest("received-response", response.getAddress(), response.getPort());
                }
                if(reply_count == received_rm.size()) {
                	receive = false;
                }
    		}
    		// Compares RM responses and returns the correct answer
    		response_message = compareResponses(received_rm);
    		return response_message;
    		
    	} catch (SocketException ex) {
    		// Should make sure we get a response
    		response_message = compareResponses(received_rm);
    		return response_message;
        } catch (IOException ex) {
        	throw ex;
        }
    }
    
    public String[] decodeResponse(String JSONString){
    	// [replica_id, sequence_id, response_details]
    	String[] decoded_response = new String[3];
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            decoded_response[0] = JObject.get("response_details").toString();
            decoded_response[1] = JObject.get("sequence_id").toString();
            decoded_response[2] = JObject.get("replica_id").toString();
        }catch (Exception e) {
        	e.printStackTrace();
        }
        return decoded_response;
    }
    
    public String[] decodeResponseDetails(String JSONString) {
    	// [status_code, message]
    	String[] decoded_details = new String[2];
    	try {
    		JSONParser jp = new JSONParser();
    		JSONObject JObject = (JSONObject) jp.parse(JSONString);
    		decoded_details[0] = JObject.get("status_code").toString();
    		decoded_details[1] = JObject.get("message").toString();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return decoded_details;
    }
    
    // Compares JSON responses from different RMs and returns the 
    public String compareResponses(ArrayList<Tuple<InetAddress, Integer, String>> received_rm) {
    	String response_message = null;
    	int correct_responses = 0;
    	
    	// To get the response message majority
    	for(Tuple<InetAddress, Integer, String> single_rm : received_rm) {
    		// getName is the equivalent of getResponseMessage
    		String raw_response = single_rm.getName().trim();
    		String[] decoded_response = this.decodeResponse(raw_response);
    		
    		// Get all response information needed
    		String replica_id = decoded_response[0];
    		String sequence_id = decoded_response[1];
    		String[] response_details = this.decodeResponseDetails(decoded_response[2]);
    		String status_code = response_details[0];
    		String message = response_details[1];
    		
    		// Comparison logic
    		if(correct_responses == 0) {
    			response_message = message;
    		} else if (response_message.equals(message)) {
    			correct_responses++;
    		} else {
    			correct_responses--;    			
    		}
    		
    		// Detect crashed RMs aka RMs that didn't reply
    		if(received_rm.size() < correct_responses) {
    			// Take a copy of RM arraylist
    			ArrayList<Tuple<InetAddress, Integer, String>> crashed_rms = new ArrayList<Tuple<InetAddress, Integer, String>>(rm_info);
    			// Remove all non-defective RMs from crashed list
    			crashed_rms.removeAll(received_rm);
    	    	for(Tuple<InetAddress, Integer, String> rm : crashed_rms) {
    	    		notify_rm(rm.getInetAddress(), rm.getPort(), "CRASHED-RM");
    	    	}
    		}
    		
    		// To notify failed RMs aka RMs that had a wrong response message or failed status code
    		for(Tuple<InetAddress, Integer, String> rm : received_rm) {
        		// getName is the equivalent of getResponseMessage
        		String raw = single_rm.getName().trim();
        		String[] decoded = this.decodeResponse(raw);	
        		String[] details = this.decodeResponseDetails(decoded[2]);
        		String code = details[0];
        		String msg = details[1];
        		
        		if( ( !response_message.equals(msg) ) | ( code.equals("Failed")) ) {
    	    		notify_rm(rm.getInetAddress(), rm.getPort(), "FAILED-RM");
        		}
    		}
    	}
    	
		return null;
    }
    
    // Notifies all RMs in multicast about a single RM failure
    private void notify_rm(InetAddress address, int port, String status) {
    	for (Tuple<InetAddress, Integer, String> rm: rm_info) {
    		String message = status + "," + address + "," + port;
    		new Thread(() -> {
    			sendRequest(message, rm.getInetAddress(), rm.getPort());
            }).start();
    	}
    }
    
    // Decodes Json string and returns message key only
    public String getResponseMessage(String response_json) {
    	String message = "";
    	return message;
    }
    
    // Handles logging
	public Logger startLogger() {
	    Logger logger = Logger.getLogger("frontend-log");
	    FileHandler fh;
	    try {
	        fh = new FileHandler("frontend/logs/" + frontend_id + ".log");
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