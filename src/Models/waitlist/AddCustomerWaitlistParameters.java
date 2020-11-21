package Models.waitlist;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AddCustomerWaitlistParameters {
    private String customerID;
    private String itemID;

    public AddCustomerWaitlistParameters(String JSONString){
        try{
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.customerID = JObject.get("customerID").toString();
            this.itemID = JObject.get("itemID").toString();
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
}
