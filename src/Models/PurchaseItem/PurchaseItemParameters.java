package Models.PurchaseItem;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PurchaseItemParameters {
    private String customerID;
    private String itemID;
    private String dateOfPurchase;

    public PurchaseItemParameters(String JSONString){
        try{
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.customerID = JObject.get("customerID").toString();
            this.itemID = JObject.get("itemID").toString();
            this.dateOfPurchase = JObject.get("dateOfPurchase").toString();
        }catch (Exception e) {e.printStackTrace();}
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getDateOfPurchase() {
        return dateOfPurchase;
    }

    public void setDateOfPurchase(String dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }
}
