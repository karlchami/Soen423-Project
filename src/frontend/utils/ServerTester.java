package frontend.utils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ServerTester {
	public static void main(String args[]) {
		
		// Testing Sequencer response then multi cast to RMs
		try {
			new Thread(() -> {
				try {
				  DatagramSocket socket = new DatagramSocket(1234, InetAddress.getLocalHost());
				  while(true) {
					byte[] buffer = new byte[1000];
    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
    	            socket.receive(request);
    	            System.out.println(request.getData().toString().trim());
    	            sendMessage(request.getAddress(), request.getPort());
    	            
    	            DatagramPacket send_to_rm1 = new DatagramPacket(request.getData(), request.getData().length, InetAddress.getLocalHost(), 3000);
    	            socket.send(send_to_rm1);
    	            
    	            DatagramPacket send_to_rm2 = new DatagramPacket(request.getData(), request.getData().length, InetAddress.getLocalHost(), 3001);
    	            socket.send(send_to_rm2);
    	            
    	            DatagramPacket send_to_rm3 = new DatagramPacket(request.getData(), request.getData().length, InetAddress.getLocalHost(), 3002);
    	            socket.send(send_to_rm3);
				  }		  
				} catch (SocketException ex) {
					System.out.println("Sequencer failed with " + ex.getMessage());
				} catch (IOException ex) {}
    	  }).start();
    	  
    	  // Testing Karl's RM receive
    	  new Thread(() -> {		  
				  try {
					  DatagramSocket socket = new DatagramSocket(4321, InetAddress.getLocalHost());
					  while (true) {
					  byte[] buffer = new byte[1000];
	    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	    	            socket.receive(request);
	    	            System.out.println(request.getData().toString().trim());	    	            
	    	            sendMessage(InetAddress.getLocalHost(), 5100);
					  }
				  }
				  catch (SocketException ex) {
    	            System.out.println("Karl's RM failed with " + ex.getMessage());
    	        } catch (IOException ex) {
    	        }
          }).start();

    	  // Testing Nick's RM receive
    	  new Thread(() -> {		  
				  try {
					  DatagramSocket socket = new DatagramSocket(4321, InetAddress.getLocalHost());
					  while (true) {
					  byte[] buffer = new byte[1000];
	    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	    	            socket.receive(request);
	    	            System.out.println(request.getData().toString().trim());	    	            
	    	            sendMessage(InetAddress.getLocalHost(), 5100);
					  }
				  }
				  catch (SocketException ex) {
    	            System.out.println("Karl's RM failed with " + ex.getMessage());
    	        } catch (IOException ex) {
    	        }
          }).start();
    	  
    	  // Testing Waqar's RM receive
    	  new Thread(() -> {		  
				  try {
					  DatagramSocket socket = new DatagramSocket(4321, InetAddress.getLocalHost());
					  while (true) {
					  byte[] buffer = new byte[1000];
	    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	    	            socket.receive(request);
	    	            System.out.println(request.getData().toString().trim());	    	            
	    	            sendMessage(InetAddress.getLocalHost(), 5100);
					  }
				  }
				  catch (SocketException ex) {
    	            System.out.println("Karl's RM failed with " + ex.getMessage());
    	        } catch (IOException ex) {
    	        }
          }).start();
    	  
      } 
      catch (Exception ex) {
         ex.printStackTrace( );
      } 
	} 
	
	// Sends UDP "received" message 
	private static void sendMessage(InetAddress requestAddress, int requestPort) {
		try (DatagramSocket sendSocket = new DatagramSocket()) {
			String message = "RECEIVED";
		    byte[] resultBytes = message.getBytes();
		    DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
		    sendSocket.send(request);
		} catch (IOException ex) {
		}
	}
}