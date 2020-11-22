package ServerImpl;

import Model.Customer;
import Model.Item;
import Model.Purchase;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.*;
import java.net.DatagramSocket;
import java.net.InetAddress;


@WebService(endpointInterface = "ServerImpl.SOAPInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ONCommandsImpl implements SOAPInterface {

    private Map<String, Item> Stock;
    private static Map<String, Queue> WaitList;
    private Map <String, Customer> Customers;
    private ArrayList<String> foreignCustomers ;
    private ArrayList<Purchase> Purchases;


    public ONCommandsImpl() throws RemoteException{
        super();
        try {
            this.Stock = new HashMap<>();
            WaitList = new HashMap<>();
            this.Customers = new HashMap<>();
            this.foreignCustomers = new ArrayList<String>();
            this.Purchases = new ArrayList<Purchase>();
            Stock.put("ON1012", new Item("1012", "Coke_Zero", "ON",27, 15));
            Stock.put("ON1023", new Item("1023", "Coke_Green", "ON",12, 8));
            Stock.put("ON1034", new Item("1034", "Coke", "ON",13, 16));
            Stock.put("ON1060", new Item("1060", "Pepsi", "ON",4, 25));
            Stock.put("ON1061", new Item("1061", "BC_Cola", "ON",0, 25));
            Stock.put("ON1085", new Item("1085", "AB_Cola", "ON",0, 34));
            Customers.put("ONU1001", new Customer());
            Customers.put("ONU1500", new Customer());


        }catch(Exception e){
            System.out.println(e);
        }
    }

    synchronized public String addItem(String managerID, String itemID, String itemName,int qty, int price) {
        try {
            qty = emptyWaitlist(itemID, qty);
            System.out.println(qty);
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty()+qty);
            String logMessage = "\naddItem Executed on existing item by " + managerID + " | Modifications successfully made to Server ON | " +
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty()  + " | " +  this.Stock.get(itemID).getPrice() + "\n\n";
            writeLog(logMessage);
            return stripNonValidXMLCharacters(logMessage);
        }catch(Exception e) {
            Stock.put(itemID, new Item(itemID.substring(2, 6), itemName, itemID.substring(0, 2), qty, price));
            this.Stock.get(itemID);
            String logMessage = "\naaddItem Executed on newly added item by " + managerID + " | Modifications successfully made to Server ON | " +
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + " | " +  this.Stock.get(itemID).getPrice() + "\n\n";
            return stripNonValidXMLCharacters(logMessage);
        }
    }

    public int emptyWaitlist(String itemId,int qty){
        itemId = "ON" + itemId.substring(2,6);
        try{
            System.out.println(this.WaitList.get(itemId).isEmpty());
            System.out.println(qty);
            while(qty > 0 && !this.WaitList.get(itemId).isEmpty()){
                String satisfiedCustomer = (String) this.WaitList.get(itemId).poll();
                String logMessage = "\n purchaseItem Executed on waitlist item by " + satisfiedCustomer
                        + " | Modifications made to Server ON |\n " + "Updated Values \n ID |  Qty \n" + itemId
                        + " | "
                        + --qty + "\n";
                try {
                    Purchases.add(new Purchase(itemId, Stock.get(itemId).getPrice(), satisfiedCustomer));
                }catch (Exception e){}
                System.out.println(logMessage);
                writeLog(logMessage);
            }
            return qty;
        }catch (Exception e){

        }
        return qty;
    }


    synchronized public String removeItem(String managerID, String itemID, int qty) {
        try{
            int currentQuantity = Stock.get(itemID).getItemQty();
            if(qty == -1){
                Stock.remove(itemID);
                String returnMessage = "("+ (returnTimeStamp()) + ") "+"removeItem Executed on existing item by " + managerID
                        + " | Modifications made to Server QC | Item Deleted "+"\n\n";
                writeLog(returnMessage);
                return stripNonValidXMLCharacters(returnMessage);
            }
            if(currentQuantity<qty){
                String returnMessage = "("+ (returnTimeStamp()) + ") "+"\nremoveItem Executed on existing item by " + managerID
                        + " | Modifications not made to Server ON | Desired Removal " + qty
                        + " higher than current quantity: " + currentQuantity +"\n\n";
                writeLog(returnMessage);
                return stripNonValidXMLCharacters(returnMessage);
            }
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty()-qty);
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"\nremoveItem Executed on existing item by " + managerID
                    + " | Modifications successfully made to Server ON |"+
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty()  + " | " +  this.Stock.get(itemID).getPrice()+ "\n\n";
            writeLog(returnMessage);
            return stripNonValidXMLCharacters(returnMessage);
        }catch(Exception e){
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"removeItem Executed on by " + managerID
                    + " | Modifications made to Server QC | Desired Item was not or is no longer in store stock \n\n";
            return stripNonValidXMLCharacters(returnMessage);
        }
    }

    public String listItemAvailability(String managerID) {
        if (validateManager(managerID)){
            String items = "ID | Item Name | Qty \n";
            for (String i : this.Stock.keySet()){
                items = items.concat(this.Stock.get(i).getItemID() + " | " + this.Stock.get(i).getItemName() + " | "
                        + this.Stock.get(i).getItemQty() + "\n\n");
            }
            return stripNonValidXMLCharacters(items);

        }else{
            try{
                return new String("Invalid Access Request");
            }catch(Exception e){

            }
        }
        return("Invalid Access Request");
    }

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        String logID = itemID;
        System.out.println("purchase item");
        if(OwnsItem(customerID,itemID)){
            return "Customer already owns this item";
        }
        try{
            String locallyAvailable = purchaseLocalItem(customerID,itemID);
            if(!locallyAvailable.startsWith("410")){
                String logMessage = "("+ (returnTimeStamp()) + ") "+"\napurchaseItem Executed on in-stock item by " + customerID
                        + " | Modifications made to Server ON |\n " + "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(logID).getItemID()
                        + " | " + this.Stock.get(logID).getItemName() + " | "
                        + this.Stock.get(logID).getItemQty()  + " | " +  this.Stock.get(logID).getPrice()+ "\n";
                writeLog(logMessage);
                return stripNonValidXMLCharacters(locallyAvailable);
            }
            else{
                String QCItem = sendUDP(2003, customerID, itemID, "purchaseItem",0,"");
                System.out.println("QC: " + QCItem);
                if(!QCItem.startsWith("410")) {
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"\napurchaseItem Executed on in-stock out-of-server item by " + customerID
                            + " | Modifications made to Server QC |\n " ;
                    writeLog(logMessage);
                    return stripNonValidXMLCharacters(QCItem);
                }
                String BCItem = sendUDP(2002, customerID, itemID, "purchaseItem",0,"");
                System.out.println("BC" + BCItem);
                if(!BCItem.startsWith("410")){
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"\napurchaseItem Executed on in-stock out-of-server item by " + customerID
                            + " | Modifications made to Server BC |\n";
                    writeLog(logMessage);
                    return stripNonValidXMLCharacters(BCItem);
                }
                if(QCItem.startsWith("41010") || BCItem.startsWith("41010")|| locallyAvailable.startsWith("41010")){
                    return "You have already used your one purchase at this foreign store per company policy";
                }
            }
            writeLog("Purchase request by " + customerID +". There is no stock for this item in any of our stores. Item ID: "+ itemID  + " \n" + "Customer added to waitlist");
            writeLog("Customer" + customerID + " added to waitlist for Item ID: " + itemID);
            addToWaitList(customerID,logID);

            return("There is no stock for this item in any of our stores. Customer added to waitlist");
        }catch(Exception e){
            System.out.println("400");
        }
        try{
            writeLog("Purchase request by " + customerID +". There is no stock for this item in any of our stores. Item ID: "+ itemID  + " \n");
            addToWaitList(customerID,logID);
        }catch(Exception e){
            e.printStackTrace();
        }
        return "There is no stock for this item in any of our stores. Customer added to waitlist";
    }

    synchronized public String purchaseLocalItem(String customerID, String itemID){

        if(enoughStock(itemID)) {
            if(!dealWithBudget(customerID, itemID)){
                return "Customer does not have enough balance for this purchase";
            }
            if(!firstShop(customerID))
                return "41010";
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty() - 1);
            Purchases.add(new Purchase(itemID, Stock.get(itemID).getPrice(),customerID));
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"Sale successful. Updated Stock for this item \n ID " +
                    "| Item Name | Qty \n" + this.Stock.get(itemID).getItemID()
                    + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty()  + " | " +  this.Stock.get(itemID).getPrice()+ "\n";
            return returnMessage;
        }else{
            return("410");
        }
    }

    public boolean firstShop(String customerId){
        String search = "";
        System.out.println(customerId);
        if(!customerId.substring(0,2).equals("ON")){
            for (int i=0; i<foreignCustomers.size();i++){
                if(foreignCustomers.get(i).equals(customerId))
                    search = foreignCustomers.get(i);
            }
            if(search.equals("")){
                foreignCustomers.add(customerId);
                return true;
            }else return false;

        }
        return true;

    }

    public boolean dealWithBudget(String customerId, String itemID){
        try{
            if(customerId.substring(0,2).equals("QC")){
                String QCItem = sendUDP(2003, customerId, itemID, "getBudget",0,"");
                int budget = Integer.parseInt(QCItem.trim());
                int cost = this.Stock.get(itemID).getPrice();
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId, cost));
                    return true;
                }
            }else if(customerId.substring(0,2).equals("BC")){
                String BCItem = sendUDP(2002, customerId, itemID, "getBudget",0,"");
                int budget = Integer.parseInt(BCItem.trim());
                int cost = this.Stock.get(itemID).getPrice();
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId, cost));
                    return true;
                }
            }else if(customerId.substring(0,2).equals("ON")){
                String ONItem = sendUDP(2001, customerId, itemID, "getBudget",0,"");
                int budget = Integer.parseInt(ONItem.trim());
                int cost = this.Stock.get(itemID).getPrice();
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId, cost));
                    return true;
                }

            }
        }catch(Exception e){
            this.Customers.put(itemID, new Customer());
            return false;
        }
        return false;
    }

    public String dealWithCosts(String customerId, int cost){
        if(customerId.substring(0,2).equals("QC")){
            String QCItem = sendUDP(2003, customerId, "itemID", "setBudget",cost,"");
            return QCItem;
        }if(customerId.substring(0,2).equals("ON")){
            String ONItem = sendUDP(2001, customerId, "itemID", "setBudget",cost,"");
            return ONItem;
        }if(customerId.substring(0,2).equals("BC")){
            String BCItem = sendUDP(2002, customerId, "itemID", "setBudget",cost,"");
            return BCItem;
        }
        return "200";
    }

    public int getLocalBudget(String customerId){
        try{
            int budget = this.Customers.get(customerId).getBudget();
            return budget;
        }catch (Exception e){
            this.Customers.put(customerId,new Customer());
            return this.Customers.get(customerId).getBudget();
        }
    }

    public String setLocalBudget(String customerId, String cost){
        int newBudget = this.Customers.get(customerId).getBudget()-Integer.parseInt(cost.trim());
        this.Customers.get(customerId).setBudget(newBudget);
        return Integer.toString(newBudget);
    }


    public String returnItem(String customerID, String itemID, String dateOfReturn) {
        try{
            if (returnPossible(dateOfReturn)){
                if(!OwnsItem(customerID,itemID)){
                    return "Customer does not own this item";
                }
                if(itemID.substring(0,2).equals("QC")){
                    String QCItem = sendUDP(2003, customerID, itemID, "returnItem",0,"");
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server QC |\n " +QCItem + "\n";
                    writeLog(logMessage);
                    int price = getNewItemPrice(itemID,customerID);
                    setLocalBudget(customerID,getLocalBudget(customerID)+price,true);
                    return stripNonValidXMLCharacters(QCItem);
                }else if(itemID.substring(0,2).equals("ON")){
                    String ONItem = sendUDP(2001, customerID, itemID, "returnItem",0,"");
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server ON |\n " + ONItem + "\n";
                    writeLog(logMessage);
                    int price = getNewItemPrice(itemID,customerID);
                    setLocalBudget(customerID,getLocalBudget(customerID)+price,true);
                    return stripNonValidXMLCharacters(ONItem);
                }else if(itemID.substring(0,2).equals("BC")){
                    String BCItem = sendUDP(2002, customerID, itemID, "returnItem",0,"");
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server BC |\n " + BCItem + "\n";
                    writeLog(logMessage);
                    int price = getNewItemPrice(itemID,customerID);
                    setLocalBudget(customerID,getLocalBudget(customerID)+price,true);
                    return stripNonValidXMLCharacters(BCItem);
                }
            }else{
                return "Return no longer possible. This article is not covered under the return policy";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new String("400");
    }

    synchronized public String returnLocalStock(String customerID, String itemID){
        try{
            int qty = 1;
            qty = emptyWaitlist(itemID, qty);
            if(qty == 0){
                removeLocalSale(customerID,itemID);
                return "Return Successful, the item was assigned to a client on the waitlist";
            }
            this.Stock.get(itemID).setItemQty(this.Stock.get(itemID).getItemQty()+qty);
            String returnMessage = "Return successful. Updated Stock for this item \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID()
                    + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n";
            removeLocalSale(customerID,itemID);
            return returnMessage;

        }catch (Exception e){
            e.printStackTrace();
            this.Stock.put(itemID,new Item(itemID.substring(2,6), "No Longer Sold Return", "QC",1, 100000));
        }
        String returnMessage = "Return successful. Item stocked, but no longer in regular sale items Price adjusted until manager input \n ID | Item Name | Qty \n"
                + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                + this.Stock.get(itemID).getItemQty() + "\n";
        removeLocalSale(customerID,itemID);
        return returnMessage;
    }

    public void removeLocalSale(String customerID, String itemID){
        try {
            for (int i = 0; i < Purchases.size(); i++) {
                if (Purchases.get(i).getCustomerID().equals(customerID) && Purchases.get(i).getItemID().equals(itemID)) {
                    int toRemove = -1;
                    for (int j = 0; j < foreignCustomers.size(); j++) {
                        if (foreignCustomers.get(j).equals(Purchases.get(i).getCustomerID()))
                            toRemove = j;
                    }
                    if(toRemove != -1){
                        foreignCustomers.remove(toRemove);
                    }
                    Purchases.remove(i);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String findItem(String customerID,  String itemName) {
        try{
            String itemID = getItemIDbyName(itemName);
            String localItem = sendUDP(2001, customerID, itemName,"findItem",0,"");
            String QCItem = sendUDP(2003, customerID, itemName,"findItem",0,"");
            String BCItem = sendUDP(2002, customerID, itemName,"findItem",0,"");
            String returnMessage = localItem+QCItem+BCItem;
            String logMessage = "("+ (returnTimeStamp()) + ") "+"\nafindItem executed by " + customerID
                    + " | Modifications not made to Servers | Logged Response :  \n" + returnMessage;
            writeLog(logMessage);
            return stripNonValidXMLCharacters(returnMessage);
        }catch(Exception e){

        }
        return new String("No Stock at the Ontario Store");
    }

    public String findLocalItem(String itemName){
        String itemID = getItemIDbyName(itemName);
        String localItem;
        try {
            itemID = "ON" + itemID;
            System.out.println(itemID);
            localItem = itemID + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n";
        }catch (Exception e){
            return "No Stock of this item at the ON Store \n";
        }
        return localItem;
    }

    private String getItemIDbyName(String itemName){
        for (String i : this.Stock.keySet()){
            String itemID = "";
            if (this.Stock.get(i).getItemName().equals(itemName)){
                return this.Stock.get(i).getItemID();
            }

        }
        return "404014";
    }


    public static boolean validateManager(String username){
        System.out.println(username);
        boolean valid = username.substring(2,3).equals("M") ? true: false;
        return valid;

    }

    public boolean enoughStock(String key){
        try{
            if(this.Stock.get(key).getItemQty() > 0){
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }



    public static boolean returnPossible(String formattedDate){
        int day = Integer.parseInt(formattedDate.substring(0,2));
        int month = Integer.parseInt(formattedDate.substring(2,4));
        int year = Integer.parseInt(formattedDate.substring(4,8));
        LocalDate oldTime = LocalDate.of(year, month, day);
        LocalDate maxReturnDate  = LocalDate.now().plusDays(30);
        boolean isPossible = maxReturnDate.isAfter(oldTime);
        return isPossible;
    }

    public void addToWaitList(String customerID, String itemID){
        itemID = "ON" + itemID.substring(2,6);
        try {
            WaitList.get(itemID).add(customerID);
        }
        catch(Exception e){
            Queue queue = new LinkedList();
            queue.add(customerID);
            WaitList.put(itemID,queue);
        }
    }

    public static void writeLog(String message) throws IOException {
        String filePath = "C:\\Users\\Waqar's PC\\IdeaProjects\\SOEN423-A1\\src\\ServerLogs\\ONServer.txt";
        try {
            File myObj = new File(filePath);
            System.out.println(myObj.exists());
            if (!myObj.exists()) {
                myObj.createNewFile();
                System.out.println("File created");
                PrintWriter pw = new PrintWriter(new FileWriter(myObj));
                pw.write(message);
                pw.close();
            } else {
                System.out.println("File already exists.");
                PrintWriter pw = new PrintWriter(new FileWriter(myObj, true));
                pw.append(message);
                pw.close();
            }
        }catch(Exception e){}


    }

    public String exchangeLogic(String customerID, String itemID, String oldItemID, String dateOfExchange){
        try {
            int price = getOldItemPrice(oldItemID, customerID);
            int newPrice = getNewItemPrice(itemID,customerID);
            boolean ownsItem = OwnsItem(customerID,oldItemID);
            boolean hasBudget = hasExchangeBudget(customerID, itemID, price, newPrice);
            if(!returnPossible(dateOfExchange)){
                return "Return no longer possible. This article is not covered under the return policy";
            }
            if(newPrice == -1 || newPrice == 0){
                return "The product you are trying to exchange for is not in stock";
            }
            if(!ownsItem){
                return "Customer does not own the item already";
            }
            if(!hasBudget){
                return "Budget not available for this exchange.";
            }
            if(customerID.substring(0,2).equals("ON")){
                returnItem(customerID,oldItemID,returnCurrentTime());
                String purchaseMessage = purchaseItem(customerID,itemID,returnCurrentTime()).trim();
                System.out.println(purchaseMessage);
                if(purchaseMessage.startsWith("(")){
                    return "Item Exchanged";
                }else{
                    return stripNonValidXMLCharacters(purchaseMessage);
                }
            }else{
                if(returnFirstShop(customerID,itemID)){
                    returnItem(customerID,oldItemID,returnCurrentTime());
                    String purchaseMessage = purchaseItem(customerID,itemID,returnCurrentTime());
                    if(purchaseMessage.startsWith("(")){
                        return "Item Exchanged";
                    }else{
                        return stripNonValidXMLCharacters(purchaseMessage);
                    }
                }else{
                    if(itemID.substring(0,2).equals(oldItemID.substring(0,2))){
                        returnItem(customerID,oldItemID,returnCurrentTime());
                        String purchaseMessage = purchaseItem(customerID,itemID,returnCurrentTime());
                        if(purchaseMessage.startsWith("(")){
                            return "Item Exchanged";
                        }else{
                            return stripNonValidXMLCharacters(purchaseMessage);
                        }
                    }else{
                        return "Exchange not possible while respecting return foreign-purchase limits";
                    }
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return "200";
    }




    public boolean hasExchangeBudget(String customerId, String itemID, int price, int newPrice){
        try{
            if(customerId.substring(0,2).equals("QC")){
                String QCItem = sendUDP(2003, customerId, itemID, "getBudget",0, "");
                int budget = Integer.parseInt(QCItem.trim());
                if(budget + price>= newPrice){
                    return true;
                }
            }else if(customerId.substring(0,2).equals("BC")){
                String BCItem = sendUDP(2002, customerId, itemID, "getBudget",0, "");
                int budget = Integer.parseInt(BCItem.trim());
                if(budget + price >= newPrice){
                    return true;
                }
            }else if(customerId.substring(0,2).equals("ON")){
                String ONItem = sendUDP(2001, customerId, itemID, "getBudget",0, "");
                int budget = Integer.parseInt(ONItem.trim());
                if(budget + price >= newPrice){
                    return true;
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean fixExchangeBudget(String customerId, String itemID, int oldPrice, int newPrice){
        try{
            if(customerId.substring(0,2).equals("QC")){
                String QCItem = sendUDP(2003, customerId, itemID, "getBudget",0, "");
                int budget = Integer.parseInt(QCItem.trim());
                int newBudget = budget + oldPrice;
                System.out.println(setLocalBudget(customerId,newBudget,true));
                return true;
            }else if(customerId.substring(0,2).equals("BC")){
                String BCItem = sendUDP(2002, customerId, itemID, "getBudget",0, "");
                int budget = Integer.parseInt(BCItem.trim());
                int newBudget = budget + oldPrice;
                System.out.println(setLocalBudget(customerId,newBudget,true));
                return true;
            }else if(customerId.substring(0,2).equals("ON")){
                String ONItem = sendUDP(2001, customerId, itemID, "getBudget",0, "");
                int budget = Integer.parseInt(ONItem.trim());
                int newBudget = budget + oldPrice;
                System.out.println(setLocalBudget(customerId,newBudget,true));
                return true;

            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public String setLocalBudget(String customerId, int budget, boolean flip){
        int newBudget = budget;
        this.Customers.get(customerId).setBudget(newBudget);
        return Integer.toString(newBudget);
    }

    public int getOldItemPrice(String oldItemID, String customerId){
        System.out.println("getOldItemPrice-"+oldItemID+"-"+customerId);
        if(oldItemID.substring(0,2).equals("BC")){
            String BCPrice = sendUDP(2002, customerId, oldItemID, "getOldPrice",0, "").trim();
            System.out.println("BC Price: " + BCPrice);
            return Integer.parseInt(BCPrice);
        }
        if(oldItemID.substring(0,2).equals("QC")){
            String QCPrice = sendUDP(2003, customerId, oldItemID, "getOldPrice",0, "").trim();
            return Integer.parseInt(QCPrice);
        }
        if(oldItemID.substring(0,2).equals("ON")){
            String ONPrice = sendUDP(2001, customerId, oldItemID, "getOldPrice",0, "").trim();
            return Integer.parseInt(ONPrice);
        }
        return -1;
    }

    public String getLocalOldItemPrice(String itemID, String customerID){
        try {
            int oldPrice = 0;
            for (int i = 0; i < Purchases.size(); i++) {
                if (Purchases.get(i).getCustomerID().equals(customerID) && Purchases.get(i).getItemID().equals(itemID))
                    oldPrice = Purchases.get(i).getPrice();
            }
            return Integer.toString(oldPrice);
        }catch (Exception e){
            return "-1";
        }
    }

    public int getNewItemPrice(String itemID, String customerId){
        if(itemID.substring(0,2).equals("BC")){
            String BCPrice = sendUDP(2002, customerId, itemID, "getNewPrice",0, "").trim();
            System.out.println("BC Price: " + BCPrice);
            return Integer.parseInt(BCPrice);
        }
        if(itemID.substring(0,2).equals("QC")){
            String QCPrice = sendUDP(2003, customerId, itemID, "getNewPrice",0, "").trim();
            return Integer.parseInt(QCPrice);
        }
        if(itemID.substring(0,2).equals("ON")){
            String ONPrice = sendUDP(2001, customerId, itemID, "getNewPrice",0, "").trim();
            return Integer.parseInt(ONPrice);
        }
        return -1;
    }

    public String getLocalNewItemPrice(String itemID, String customerID){
        try {
            int newPrice = 0;
            for (String i : this.Stock.keySet()){
                if(this.Stock.get(i).getItemID().equals(itemID.substring(2,6))){
                    System.out.println(this.Stock.get(i).getItemID() + " " + this.Stock.get(i).getPrice());
                    return Integer.toString(this.Stock.get(i).getPrice());
                }
            }
            return Integer.toString(newPrice);
        }catch (Exception e){
            return "-1";
        }
    }

    public boolean OwnsItem(String customerID, String itemID){
        if(itemID.substring(0,2).equals("BC")){
            String BCReturn = sendUDP(2002, customerID, itemID, "ownsItem",0, "").trim();
            if(BCReturn.equals("true"))
                return true;
        }
        if(itemID.substring(0,2).equals("QC")){
            String QCReturn = sendUDP(2003, customerID, itemID, "ownsItem",0, "").trim();
            if(QCReturn.equals("true"))
                return true;
        }
        if(itemID.substring(0,2).equals("ON")){
            String ONReturn = sendUDP(2001, customerID, itemID, "ownsItem",0, "").trim();
            if(ONReturn.equals("true"))
                return true;
        }
        return false;
    }

    public String localOwnsItem(String customerID, String itemID){
        String flag = "false";
        for(int i=0;i<Purchases.size();i++){
            if(Purchases.get(i).getItemID().equals(itemID) && Purchases.get(i).getCustomerID().equals(customerID)){
                flag = "true";
            }
        }
        return flag;
    }

    public static String returnCurrentTime(){
        LocalDate currentTime = LocalDate.now();
        DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("ddMMyyyy");
        String formattedDate = currentTime.format(formattedTime);
        return formattedDate;
    }

    public boolean returnFirstShop(String customerId, String itemID){
        if(itemID.substring(0,2).equals("BC")){
            String BCPrice = sendUDP(2002, customerId, itemID, "getFirstShop",0, "").trim();
            if(BCPrice.equals("true"))
                return true;
            return false;
        }
        if(itemID.substring(0,2).equals("QC")){
            String QCPrice = sendUDP(2003, customerId, itemID, "getFirstShop",0, "").trim();
            if(QCPrice.equals("true"))
                return true;
            return false;
        }
        if(itemID.substring(0,2).equals("ON")){
            String ONPrice = sendUDP(2001, customerId, itemID, "getFirstShop",0, "").trim();
            if(ONPrice.equals("true"))
                return true;
            return false;
        }
        return false;
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


    public static String returnTimeStamp(){
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm");
        String returnTime = formattedTime.format(currentTime);
        return returnTime;
    }

    //Obtained XML Character error fix at http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
    public String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                    (current == 0xA) ||
                    (current == 0xD) ||
                    ((current >= 0x20) && (current <= 0xD7FF)) ||
                    ((current >= 0xE000) && (current <= 0xFFFD)) ||
                    ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

}