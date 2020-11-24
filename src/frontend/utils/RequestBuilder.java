package frontend.utils;

public class RequestBuilder {	  
	
    public RequestBuilder() {
	}
	
	public static String jsonRequestBuilder(String method_name, String parameters) {
        String json = "{\n" +
                "    \"sequence_id\" : -1,\n" +
                "    \"request_details\" : {\n" +
                "        \"method_name\" : \"" + method_name + "\",\n" +
                "        \"parameters\" : " + parameters + "\n" +
                "    }\n" +
                "}";
        return json;
	}
	
	public static String addItemRequest(String managerID, String itemID, String itemName, int quantity, int price) {
        String parameters = "{\n" +
                "    \"managerID\" : \"" + managerID + "\",\n" +
                "    \"itemID\" : \"" + itemID + "\",\n" +
                "    \"itemName\" : \"" + itemName + "\",\n" +
                "    \"quantity\" : \"" + quantity + "\",\n" +
                "    \"price\" : \"" + price + "\"\n" +
                "}";
        return jsonRequestBuilder("addItem", parameters);
	}
	
	public static String removeItemRequest(String managerID, String itemID, int quantity) {
        String parameters = "{\n" +
                "    \"managerID\" : \"" + managerID + "\",\n" +
                "    \"itemID\" : \"" + itemID + "\",\n" +
                "    \"quantity\" : \"" + quantity + "\"\n" +
                "}";
        return jsonRequestBuilder("removeItem", parameters);
	}

	public static String listItemAvailabilityRequest(String managerID) {
        String parameters = "{\n" +
                "    \"managerID\" : \"" + managerID + "\"\n" +
                "}";
        return jsonRequestBuilder("listItemAvailability", parameters);
	}
	
	public static String purchaseItemRequest(String customerID, String itemID, String dateOfPurchase) {
        String parameters = "{\n" +
                "    \"customerID\" : \"" + customerID + "\",\n" +
                "    \"itemID\" : \"" + itemID + "\",\n" +
                "    \"dateOfPurchase\" : \"" + dateOfPurchase + "\"\n" +
                "}";
        return jsonRequestBuilder("purchaseItem", parameters);
	}
	
	public static String findItemRequest(String customerID, String itemName) {
        String parameters = "{\n" +
                "    \"customerID\" : \"" + customerID + "\",\n" +
                "    \"itemName\" : \"" + itemName + "\"\n" +
                "}";
        return jsonRequestBuilder("findItem", parameters);	
	}
	
	public static String exchangeItemRequest(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        String parameters = "{\n" +
                "    \"customerID\" : \"" + customerID + "\",\n" +
                "    \"oldItemID\" : \"" + oldItemID + "\",\n" +
                "    \"newItemID\" : \"" + newItemID + "\",\n" +
                "    \"dateOfExchange\" : \"" + dateOfExchange + "\"\n" +
                "}";
        return jsonRequestBuilder("exchangeItem", parameters);
	}
}
