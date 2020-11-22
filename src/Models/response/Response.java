package Models.response;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Response {
    private String sequence_id;
    private String replica_id;
    private ResponseDetails responseDetails;

    public Response(String JSONString) {
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.responseDetails = new ResponseDetails(JObject.get("response_details").toString());
            this.sequence_id = JObject.get("sequence_id").toString();
            this.replica_id = JObject.get("replica_id").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public ResponseDetails getResponseDetails() {
        return responseDetails;
    }

    public void setResponseDetails(ResponseDetails responseDetails) {
        this.responseDetails = responseDetails;
    }

}
