package manager;



import Models.AddItem.AddItem;
import Models.Exchange.ExchangeItem;
import Models.FindItem.FindItem;
import Models.ListItem.ListItem;
import Models.PurchaseItem.PurchaseItem;
import Models.RemoveItem.RemoveItem;
import Models.ReturnItem.ReturnItem;
import Models.waitlist.AddCustomerWaitlist;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import replica.replica_waqar.server_waqar.BCServer;
import replica.replica_waqar.server_waqar.ONServer;
import replica.replica_waqar.server_waqar.QCServer;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.PriorityQueue;



public class ReplicaManager {
    public static PriorityQueue<String> pq = new PriorityQueue<String>(20);
    public static ArrayList<String> received_commands;
    public static Thread QC;
    public static Thread ON;
    public static Thread BC;

    public static void main(String args[]){

        try{
            startBC();
            parseJSON();

            received_commands = new ArrayList<String>();
            Runnable task = () -> {
                receive();
            };
            Thread thread = new Thread(task);
            thread.start();


        }
        catch (Exception e) {
        }


    }


    private static void startBC(){
        Runnable task = () -> {
            try {
                String[] args = new String[1];
                BCServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        BC = new Thread(task);
        BC.start();

    }

    private static void startON(){
        Runnable task = () -> {
            try {
                String[] args = new String[1];
                ONServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        ON = new Thread(task);
        ON.start();
    }

    private static void startQC(){
        Runnable task = () -> {
            try {
                String[] args = new String[1];
                QCServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        QC = new Thread(task);
        QC.start();
    }

    public static boolean heartbeat(){
        return true;

    }

    public static void parseJSON(){
        try {
            String testString = "{\n" +
                    "    \"replica_id\" : \"karl/waqar/nick\",\n" +
                    "    \"sequence_id\" : \"5\",\n" +
                    "    \"response_details\" : {\n" +
                    "        \"method_name\" : \"returnItem\",\n" +
                    "        \"message\" : \"customerID return of itemID on dateOfReturn\",\n" +
                    "        \"status_code\" : \"successful/failed\",\n" +
                    "        \"parameters\" : {\n" +
                    "            \"customerID\" : \"QCU1001\",\n" +
                    "            \"itemID\" : \"QC6000\",\n" +
                    "            \"dateOfReturn\" : \"20-11-2020\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";


            testSeperate(testString);




        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testSeperate(String JSONString){
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            JSONObject JObject2 = (JSONObject) JObject.get("response_details");
            String method = JObject2.get("method_name").toString();
            if(method.equals("addItem")){
                AddItem item = new AddItem(JSONString);
                //Do Whatever
            }
            if(method.equals("addCustomerWaitlist")){
                AddCustomerWaitlist item = new AddCustomerWaitlist(JSONString);
                //Do Whatever
            }
            if(method.equals("exchangeItem")){
                ExchangeItem item = new ExchangeItem(JSONString);
            }
            if(method.equals("findItem")){
                FindItem item = new FindItem(JSONString);
                //Do Whatever
            }
            if(method.equals("listItemAvailability")){
                ListItem item = new ListItem(JSONString);
                //Do Whatever
            }
            if(method.equals("purchaseItem")){
                PurchaseItem item = new PurchaseItem(JSONString);
                //Do Whatever
            }
            if(method.equals("removeItem")){
                RemoveItem item = new RemoveItem(JSONString);
                //Do Whatever
            }
            if(method.equals("returnItem")){
                ReturnItem item = new ReturnItem(JSONString);
                System.out.println(item.getResponseDetails().getParameters().getDateOfReturn());
                System.out.println(item.getResponseDetails().getParameters().getCustomerID());
                System.out.println(item.getResponseDetails().getParameters().getItemID());
                //Do Whatever
            }

        }catch (Exception e){e.printStackTrace();}

    }



    private static void receive() {
        DatagramSocket socket = null;
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
                if(split[0].equals("Restart")) {

                }
                if(split[0].equals("SendForward")) {

                }if(split[0].equals("GetMessage")) {

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


    private static String sendUDP(int port, String username, String itemId, String action, int cost, String oldItem) {
        DatagramSocket socket = null;
        String UDPMessage = action+"-"+username+"-"+itemId+"-"+cost+"-" +oldItem;
        String result="";
        try {
            result ="";
            socket = new DatagramSocket();
            byte[] messageToSend = UDPMessage.getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(messageToSend, UDPMessage.length(), hostName, port);
            socket.send(request);

            byte[] bf = new byte[256];
            DatagramPacket reply = new DatagramPacket(bf, bf.length);
            socket.receive(reply);
            result = new String(reply.getData());
            // String[] parts = result.split("-");
            // result = parts[0];
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (socket != null)
                socket.close();
        }
        return result;

    }




}
