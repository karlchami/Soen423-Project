package replica.replica_waqar.server_waqar;


import Models.request.Request;
import replica.replica_waqar.ServerImpl.BCCommandsImpl;
import replica.replica_waqar.ServerImpl.ONCommandsImpl;

import java.net.*;

public class BCServer {
    public static DatagramSocket socket = null;
    public static DatagramSocket RMsocket = null;

    public static void main(String args[]){
        try{


             System.out.println("Starting BC Server");
             BCCommandsImpl store = new BCCommandsImpl();


              Runnable task = () -> {
                  receive(store);
              };
              Thread thread = new Thread(task);
              thread.start();

              Runnable task2 = () -> {
                    receiveFromRM(store);
              };
              Thread thread2 = new Thread(task2);
              thread2.start();

            while(true){
                Thread.sleep(2500);
            }


        }
        catch (Exception e) {
        }


    }




    private static void receive(BCCommandsImpl obj) {
        String returnMessage = "";
        try {
            socket = new DatagramSocket(2002);
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String sentence = new String( request.getData(), request.getOffset(), request.getLength()).trim();


                String[] split = sentence.split("-");
                //action+"-"+username+"-"+itemId+"-"+cost"-"+oldItem
                System.out.println("Function Received " + split[0]);
                if(split[0].equals("purchaseItem")) {
                    returnMessage = obj.purchaseLocalItem(split[1],split[2]);
                    System.out.println(returnMessage);
                }
                if(split[0].equals("findItem")) {
                    returnMessage = obj.findLocalItem(split[2]);
                    System.out.println(split[2]);
                    System.out.println(returnMessage);

                }if(split[0].equals("returnItem")) {
                    returnMessage = obj.returnLocalStock(split[1],split[2]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getBudget")) {
                    returnMessage = Integer.toString(obj.getLocalBudget(split[1]));
                    System.out.println(returnMessage);
                }if(split[0].equals("setBudget")) {
                    returnMessage = obj.setLocalBudget(split[1],split[3]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getOldPrice")) {
                    returnMessage = obj.getLocalOldItemPrice(split[2],split[1]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getNewPrice")) {
                    returnMessage = obj.getLocalNewItemPrice(split[2],split[1]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getFirstShop")) {
                    boolean value = obj.firstShop(split[1]);
                    if(value){
                        returnMessage = "true";
                    }else{
                        returnMessage = "false";
                    }
                    System.out.println(returnMessage);
                }if(split[0].equals("ownsItem")) {
                    returnMessage = obj.localOwnsItem(split[1],split[2]);
                    System.out.println(returnMessage);
                }

                byte[] sendData = returnMessage.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), request.getAddress(),
                        request.getPort());
                socket.send(reply);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private static void receiveFromRM(BCCommandsImpl obj) {

        String returnMessage = "";
        try {
            RMsocket = new DatagramSocket(3002);
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                RMsocket.receive(request);
                String sentence = new String( request.getData(), request.getOffset(), request.getLength()).trim();
                System.out.println(sentence);

                if(sentence.equals("Exit")){
                    System.out.println("Killing BC Server");
                    returnMessage = "BC IS DEAD";
                    byte[] sendData = returnMessage.getBytes();
                    DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), request.getAddress(),
                            request.getPort());
                    RMsocket.send(reply);
                    RMsocket.close();
                    socket.close();
                    RMsocket = null;
                    socket = null;
                    return;
                }
                if(sentence.equals("Heartbeat")){
                    returnMessage = "TRUE";
                    byte[] sendData = returnMessage.getBytes();
                    DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), request.getAddress(),
                            request.getPort());
                    RMsocket.send(reply);
                    RMsocket.close();
                    return;
                }
                Request dumbo = new Request(sentence);
                System.out.println(dumbo.getStore());

                if(dumbo.getRequestDetails().getMethod_name().equals("addItem")){
                    returnMessage = obj.addItem(
                            dumbo.getRequestDetails().getParameters().get("managerID").toString(),
                            dumbo.getRequestDetails().getParameters().get("itemID").toString(),
                            dumbo.getRequestDetails().getParameters().get("itemName").toString(),
                            Integer.parseInt(dumbo.getRequestDetails().getParameters().get("quantity").toString()),
                            Integer.parseInt(dumbo.getRequestDetails().getParameters().get("price").toString()));
                }
                if(dumbo.getRequestDetails().getMethod_name().equals("exchangeItem")){
                    returnMessage = obj.exchangeLogic(
                            dumbo.getRequestDetails().getParameters().get("customerID").toString(),
                            dumbo.getRequestDetails().getParameters().get("newitemID").toString(),
                            dumbo.getRequestDetails().getParameters().get("olditemID").toString(),
                            dumbo.getRequestDetails().getParameters().get("dateOfExchange").toString());
                }
                if(dumbo.getRequestDetails().getMethod_name().equals("findItem")){
                    returnMessage = obj.findItem(
                            dumbo.getRequestDetails().getParameters().get("customerID").toString(),
                            dumbo.getRequestDetails().getParameters().get("itemName").toString());
                }
                if(dumbo.getRequestDetails().getMethod_name().equals("listItemAvailability")){
                    returnMessage = obj.listItemAvailability(
                            dumbo.getRequestDetails().getParameters().get("managerID").toString());
                }
                if(dumbo.getRequestDetails().getMethod_name().equals("purchaseItem")){
                    returnMessage = obj.purchaseItem(
                            dumbo.getRequestDetails().getParameters().get("customerID").toString(),
                            dumbo.getRequestDetails().getParameters().get("itemID").toString(),
                            dumbo.getRequestDetails().getParameters().get("dateOfPurchase").toString());
                }
                if(dumbo.getRequestDetails().getMethod_name().equals("removeItem")){
                    returnMessage = obj.removeItem(
                            dumbo.getRequestDetails().getParameters().get("managerID").toString(),
                            dumbo.getRequestDetails().getParameters().get("itemID").toString(),
                            Integer.parseInt(dumbo.getRequestDetails().getParameters().get("quantity").toString()));
                }
                if(dumbo.getRequestDetails().getMethod_name().equals("returnItem")){
                    returnMessage = obj.returnItem(
                            dumbo.getRequestDetails().getParameters().get("customerID").toString(),
                            dumbo.getRequestDetails().getParameters().get("itemID").toString(),
                            dumbo.getRequestDetails().getParameters().get("dateOfReturn").toString());
                }



                System.out.println("The end!");
                byte[] sendData = returnMessage.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), request.getAddress(),
                        request.getPort());
                RMsocket.send(reply);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            if (RMsocket != null)
                RMsocket.close();
        }
    }




}
