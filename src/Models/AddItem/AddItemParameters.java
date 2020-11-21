package Models.AddItem;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AddItemParameters {
    private String managerID;
    private String itemID;
    private String itemName;
    private String quantity;
    private String price;

    public AddItemParameters(String JSONString){
        try{
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.managerID = JObject.get("managerID").toString();
            this.itemID = JObject.get("itemID").toString();
            this.itemName = JObject.get("itemName").toString();
            this.quantity = JObject.get("quantity").toString();
            this.price = JObject.get("price").toString();
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
