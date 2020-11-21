package Models.Exchange;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ExchangeResponseDetails {

    private String method_name;
    private String message;
    private String status_code;
    private ExchangeParameters parameters;

    public ExchangeResponseDetails(String JSONString){
        try {
            JSONParser jp = new JSONParser();
            JSONObject JObject = (JSONObject) jp.parse(JSONString);
            this.method_name = JObject.get("method_name").toString();
            this.message = JObject.get("message").toString();
            this.status_code = JObject.get("status_code").toString();
            this.parameters = new ExchangeParameters(JObject.get("parameters").toString());

        }catch (Exception e) {e.printStackTrace();}
    }

    public String getMethod_name() {
        return method_name;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus_code() {
        return status_code;
    }

    public void setStatus_code(String status_code) {
        this.status_code = status_code;
    }

    public ExchangeParameters getParameters() {
        return parameters;
    }

    public void setParameters(ExchangeParameters exchangeParameters) {
        this.parameters = exchangeParameters;
    }


}