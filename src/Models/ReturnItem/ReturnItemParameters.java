package Models.ReturnItem;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ReturnItemParameters {


    private String customerID;
    private String itemID;
    private String dateOfReturn;

    public ReturnItemParameters(String JSONString){
        try{
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.customerID = JObject.get("customerID").toString();
            this.itemID = JObject.get("itemID").toString();
            this.dateOfReturn = JObject.get("dateOfReturn").toString();
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

    public String getDateOfReturn() {
        return dateOfReturn;
    }

    public void setDateOfReturn(String dateOfReturn) {
        this.dateOfReturn = dateOfReturn;
    }
}
