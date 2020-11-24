package Models.request;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Request {
    private int sequence_id;
    private String replica_id;
    private String store;
    private RequestDetails request_details;

    public Request(String JSONString) {
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.request_details = new RequestDetails(JObject.get("request_details").toString());
            this.sequence_id = Integer.parseInt(JObject.get("sequence_id").toString());
            this.replica_id = JObject.get("replica_id").toString();
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

    public String getReplica_id() {
        return replica_id;
    }

    public void setReplica_id(String replica_id) {
        this.replica_id = replica_id;
    }

    public RequestDetails getRequest_details() {
        return request_details;
    }

    public void setRequest_details(RequestDetails request_details) {
        this.request_details = request_details;
    }

    public static void main(String[] args) {
        String s = "{\n" +
                "    \"replica_id\" : \"karl/waqar/nick\",\n" +
                "    \"sequence_id\" : -1,\n" +
                "    \"response_details\" : {\n" +
                "        \"method_name\" : \"returnItem\",\n" +
                "        \"message\" : \"customerID return of itemID on dateOfReturn\",\n" +
                "        \"status_code\" : \"successful/failed\",\n" +
                "        \"parameters\" : {\n" +
                "            \"customerID\" : \"QCU1001\",\n" +
                "            \"itemID\" : \"QC6000\",\n" +
                "            \"dateOfReturn\" : \"20-11-2020\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Request r = new Request(s);

//        System.out.println(r.getSequence_id());
//        System.out.println(r.getReplica_id());
//        System.out.println(r.getRequestDetails().getMethod_name());
//
//        System.out.println(r.getRequestDetails().getParameters().get("customerID"));
//        System.out.println(r.getRequestDetails().getParameters().get("itemID"));
//        System.out.println(r.getRequestDetails().getParameters().get("dateOfReturn"));
//
//        Gson g = new Gson();
//        String t = g.toJson(r);
//
//        System.out.println(t);
    }
}
