package Models.request;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RequestDetails {
    private String method_name;
    private JSONObject parameters;

    public RequestDetails(String JSONString) {
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.method_name = JObject.get("method_name").toString();
            this.parameters = (JSONObject) JObject.get("parameters");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMethod_name() {
        return method_name;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }

    public JSONObject getParameters() {
        return parameters;
    }

    public void setParameters(JSONObject parameters) {
        this.parameters = parameters;
    }
}
