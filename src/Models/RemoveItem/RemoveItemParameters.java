package Models.RemoveItem;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RemoveItemParameters {
    private String managerID;
    private String itemID;
    private String quantity;

    public RemoveItemParameters(String JSONString){
        try{
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.managerID = JObject.get("managerID").toString();
            this.itemID = JObject.get("itemID").toString();
            this.quantity = JObject.get("quantity").toString();
        }catch (Exception e) {e.printStackTrace();}
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
