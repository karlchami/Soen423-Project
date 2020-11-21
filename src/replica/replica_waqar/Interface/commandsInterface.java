package Interface;
import Model.Item;

import java.rmi.*;
import java.util.Map;


public interface commandsInterface extends Remote {

        public String addItem(String managerID, String itemID, String itemName,int qty, int price) throws java.rmi.RemoteException ;

        public String removeItem(String managerID, String itemID, int qty) throws java.rmi.RemoteException;

        public String listItemAvailability(String managerID) throws java.rmi.RemoteException ;

        public String purchaseItem(String customerID, String itemID, String dateOfPurchase) throws java.rmi.RemoteException;

        public String returnItem(String customerID, String itemID, String dateOfReturn) throws java.rmi.RemoteException;

        public String findItem(String customerID, String itemName) throws java.rmi.RemoteException;

        public String exchangeLogic(String customerID, String itemID, String oldItemID, String dateOfReturn) throws java.rmi.RemoteException;


}
