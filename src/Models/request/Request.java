package Models.request;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Request {
    private int sequence_id;
    private String store;
    private RequestDetails request_details;

    public Request(String JSONString) {
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.request_details = new RequestDetails(JObject.get("request_details").toString());
            this.sequence_id = Integer.parseInt(JObject.get("sequence_id").toString());
            this.store = JObject.get("store").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public int getSequence_id() {
        return sequence_id;
    }

    public void setSequence_id(int sequence_id) {
        this.sequence_id = sequence_id;
    }

    public RequestDetails getRequest_details() {
        return request_details;
    }

    public void setRequest_details(RequestDetails request_details) {
        this.request_details = request_details;
    }
}
