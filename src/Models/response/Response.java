package Models.response;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Response {
    private String sequence_id;
    private String replica_id;
    private ResponseDetails response_details;

    public Response(String JSONString) {
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.response_details = new ResponseDetails(JObject.get("response_details").toString());
            this.sequence_id = JObject.get("sequence_id").toString();
            this.replica_id = JObject.get("replica_id").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Response(String sequence_id, String replica_id, String method_name, String message, String status_code) {
        this.sequence_id = sequence_id;
        this.replica_id = replica_id;
        this.response_details = new ResponseDetails(method_name, message, status_code);
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

    public ResponseDetails getResponse_details() {
        return response_details;
    }

    public void setResponse_details(ResponseDetails response_details) {
        this.response_details = response_details;
    }

}
