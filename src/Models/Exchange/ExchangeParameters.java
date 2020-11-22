package Models.Exchange;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

public class ExchangeParameters {

    //ExchangeItem
    private String customerID;
    private String oldItemID;
    private String newItemID;
    private String dateOfExchange;

    public ExchangeParameters(String JSONString){
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.customerID = JObject.get("customerID").toString();
            this.oldItemID = JObject.get("olditemID").toString();
            this.newItemID = JObject.get("newitemID").toString();
            this.dateOfExchange = JObject.get("dateOfExchange").toString();
        }catch (Exception e) {

        }
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getOldItemID() {
        return oldItemID;
    }

    public void setOldItemID(String oldItemID) {
        this.oldItemID = oldItemID;
    }

    public String getNewItemID() {
        return newItemID;
    }

    public void setNewItemID(String newItemID) {
        this.newItemID = newItemID;
    }

    public String getDateOfExchange() {
        return dateOfExchange;
    }

    public void setDateOfExchange(String dateOfExchange) {
        this.dateOfExchange = dateOfExchange;
    }
}