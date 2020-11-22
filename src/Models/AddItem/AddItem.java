package Models.AddItem;

import Models.Exchange.ExchangeResponseDetails;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AddItem {

    private String sequence_id;
    private String replica_id;
    private AddItemResponseDetails responseDetails;


    public AddItem(String JSONString){
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.responseDetails = new AddItemResponseDetails(JObject.get("response_details").toString());
            this.sequence_id = JObject.get("sequence_id").toString();
            this.replica_id = JObject.get("replica_id").toString();
        }catch (Exception e) {e.printStackTrace();}
    }

    public String getSequence_id() {
        return sequence_id;
    }

    public void setSequence_id(String sequence_id) {
        this.sequence_id = sequence_id;
    }

    public String getReplica_id() {
        return replica_id;
    }

    public void setReplica_id(String replica_id) {
        this.replica_id = replica_id;
    }

    public AddItemResponseDetails getResponseDetails() {
        return responseDetails;
    }

    public void setResponseDetails(AddItemResponseDetails responseDetails) {
        this.responseDetails = responseDetails;
    }
}
