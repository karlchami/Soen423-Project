package Models.FindItem;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FindItemParameters {
    private String customerID;
    private String itemName;
    private String[] found_items;

    public FindItemParameters(String JSONString){
        try{
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.customerID = JObject.get("customerID").toString();
            this.itemName = JObject.get("itemName").toString();
            JSONArray jsonArray = (JSONArray) JObject.get("found_items");
            this.found_items = convertArray(jsonArray);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getCustomerID() {
        return customerID;
    }

    public String[] convertArray(JSONArray jsonArray){
        String[] arr = new String[jsonArray.size()];
        for(int i=0; i<arr.length; i++) {
            arr[i] = jsonArray.get(i).toString();
        }
        return arr;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String[] getFound_items() {
        return found_items;
    }

    public void setFound_items(String[] found_items) {
        this.found_items = found_items;
    }
}
