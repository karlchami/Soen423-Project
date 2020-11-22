package Models.ListItem;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ListItemParameters {

    private String managerID;
    private String[] available_items;

    public ListItemParameters(String JSONString){
        try{
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.managerID = JObject.get("managerID").toString();
            JSONArray jsonArray = (JSONArray) JObject.get("available_items");
            this.available_items = convertArray(jsonArray);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String[] convertArray(JSONArray jsonArray){
        String[] arr = new String[jsonArray.size()];
        for(int i=0; i<arr.length; i++) {
            arr[i] = jsonArray.get(i).toString();
        }
        return arr;
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }

    public String[] getAvailable_items() {
        return available_items;
    }

    public void setAvailable_items(String[] available_items) {
        this.available_items = available_items;
    }
}
