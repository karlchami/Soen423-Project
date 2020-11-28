package replica.replica_waqar.server_waqar;

import replica.replica_waqar.ServerImpl.ONCommandsImpl;

import Models.request.Request;
import Models.response.Response;
import com.google.gson.Gson;
import replica.replica_waqar.ServerImpl.ONCommandsImpl;

import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;

public class ONServer {

    public static DatagramSocket socket = null;
    public static DatagramSocket RMsocket = null;

    public static void main(String args[]){
        try{

            System.out.println("Starting ON Server");
            ONCommandsImpl store = new ONCommandsImpl();

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



    private static void receive(ONCommandsImpl obj) {
        String returnMessage = "";
        try {
            socket = new DatagramSocket(2001);
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String sentence = new String( request.getData(), request.getOffset(), request.getLength()).trim();

                String[] split = sentence.split("-");
                //action+"-"+username+"-"+itemId+"-"+cost
//                System.out.println("Function Received " + split[0]);
                if(split[0].equals("purchaseItem")) {
                    returnMessage = obj.purchaseLocalItem(split[1],split[2]);
                }
                if(split[0].equals("findItem")) {
                    returnMessage = obj.findLocalItem(split[2]);
                }if(split[0].equals("returnItem")) {
                    returnMessage = obj.returnLocalStock(split[1], split[2]);
                }if(split[0].equals("getBudget")) {
                    returnMessage = Integer.toString(obj.getLocalBudget(split[1]));
                }if(split[0].equals("setBudget")) {
                    returnMessage = obj.setLocalBudget(split[1],split[3]);
                }if(split[0].equals("getOldPrice")) {
                    returnMessage = obj.getLocalOldItemPrice(split[2],split[1]);
                }if(split[0].equals("getNewPrice")) {
                    returnMessage = obj.getLocalNewItemPrice(split[2],split[1]);
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

    private static void receiveFromRM(ONCommandsImpl obj) {

        String returnMessage = "";
        int FEPort = 5555;
        try {
            InetAddress FEHost = InetAddress.getByName("localhost");
            RMsocket = new DatagramSocket(3001);
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                RMsocket.receive(request);
                String sentence = new String( request.getData(), request.getOffset(), request.getLength()).trim();

                if(sentence.equals("Exit")){
                    System.out.println("Killing ON Server");
                    returnMessage = "ON IS DEAD";
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

                String status_code = ""; // Will actually be set by parsing message
                Request dumbo = new Request(sentence.split("-")[1]);
                System.out.println(dumbo.getStore());
                if(dumbo.getRequest_details().getMethod_name().equals("addItem")){
                    returnMessage = obj.addItem(
                            dumbo.getRequest_details().getParameters().get("managerID").toString(),
                            dumbo.getRequest_details().getParameters().get("itemID").toString(),
                            dumbo.getRequest_details().getParameters().get("itemName").toString(),
                            Integer.parseInt(dumbo.getRequest_details().getParameters().get("quantity").toString()),
                            Integer.parseInt(dumbo.getRequest_details().getParameters().get("price").toString()));
                }
                if(dumbo.getRequest_details().getMethod_name().equals("exchangeItem")){
                    returnMessage = obj.exchangeLogic(
                            dumbo.getRequest_details().getParameters().get("customerID").toString(),
                            dumbo.getRequest_details().getParameters().get("newitemID").toString(),
                            dumbo.getRequest_details().getParameters().get("olditemID").toString(),
                            dumbo.getRequest_details().getParameters().get("dateOfExchange").toString());
                }
                if(dumbo.getRequest_details().getMethod_name().equals("findItem")){
                    returnMessage = obj.findItem(
                            dumbo.getRequest_details().getParameters().get("customerID").toString(),
                            dumbo.getRequest_details().getParameters().get("itemName").toString());
                }
                if(dumbo.getRequest_details().getMethod_name().equals("listItemAvailability")){
                    returnMessage = obj.listItemAvailability(
                            dumbo.getRequest_details().getParameters().get("managerID").toString());
                }
                if(dumbo.getRequest_details().getMethod_name().equals("purchaseItem")){
                    returnMessage = obj.purchaseItem(
                            dumbo.getRequest_details().getParameters().get("customerID").toString(),
                            dumbo.getRequest_details().getParameters().get("itemID").toString(),
                            dumbo.getRequest_details().getParameters().get("dateOfPurchase").toString());
                }
                if(dumbo.getRequest_details().getMethod_name().equals("removeItem")){
                    returnMessage = obj.removeItem(
                            dumbo.getRequest_details().getParameters().get("managerID").toString(),
                            dumbo.getRequest_details().getParameters().get("itemID").toString(),
                            Integer.parseInt(dumbo.getRequest_details().getParameters().get("quantity").toString()));
                }
                if(dumbo.getRequest_details().getMethod_name().equals("returnItem")){
                    returnMessage = obj.returnItem(
                            dumbo.getRequest_details().getParameters().get("customerID").toString(),
                            dumbo.getRequest_details().getParameters().get("itemID").toString(),
                            dumbo.getRequest_details().getParameters().get("dateOfReturn").toString());
                }
                Response response = new Response(String.valueOf(dumbo.getSequence_id()), "waqar",
                        dumbo.getRequest_details().getMethod_name(), returnMessage, status_code);
                Gson gson = new Gson();
                String json = gson.toJson(response);
                if(!sentence.startsWith("R")) {
                    byte[] sendData = json.getBytes();
                    DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), FEHost,
                            FEPort);
                    RMsocket.send(reply);
                }else{
                    return;
                }
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
