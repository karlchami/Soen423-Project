package replica.replica_nick.impl;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService()
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface StoreInterface {

    public String addItem(String managerID, String itemID, String itemName, int quantity, double price);

    public String removeItem(String managerID, String itemID, int quantity);

    public String listItemAvailability(String managerID);

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase);

    public String findItem(String customerID, String itemName);

    public String returnItem(String customerID, String itemID, String dateOfReturn);

    public String exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange);

    public String addToWaitList(String itemID, String customerID);
}
