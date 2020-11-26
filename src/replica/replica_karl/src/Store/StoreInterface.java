package replica.replica_karl.src.Store;

import java.text.ParseException;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

public interface StoreInterface {
    public boolean addItem(String managerID, String itemID, String itemName, int quantity, int price);
    public boolean removeItem(String managerID, String itemID, int quantity) ;
    public String listItemAvailability (String managerID);

    public String purchaseItem (String customerID, String itemID, String dateOfPurchase);
    public String findItem (String customerID, String itemName);
    public boolean returnItem (String customerID, String itemID, String dateOfReturn) ;
    public boolean exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange);
    
    public void addCustomer(String customerID);
    public void addManager(String managerID);
    public void addCustomerWaitList(String customerID, String itemID);
    
    public void receive() throws NumberFormatException, ParseException;

}