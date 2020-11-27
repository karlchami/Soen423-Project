package frontend.utils;

public class RequestBuilder {	  
	
    public RequestBuilder() {
	}
	
	public static String jsonRequestBuilder(String method_name, String parameters, String store) {
        String json = "{\n" +
        		"    \"store\" : \"" + store + "\",\n" +
                "    \"sequence_id\" : -1,\n" +
                "    \"request_details\" : {\n" +
                "        \"method_name\" : \"" + method_name + "\",\n" +
                "        \"parameters\" : " + parameters + "\n" +
                "     }\n" +
                "}";
        return json;
	}
	
	public static String addItemRequest(String managerID, String itemID, String itemName, int quantity, int price, String store) {
        String parameters = "{\n" +
                "            \"managerID\" : \"" + managerID + "\",\n" +
                "            \"itemID\" : \"" + itemID + "\",\n" +
                "            \"itemName\" : \"" + itemName + "\",\n" +
                "            \"quantity\" : \"" + quantity + "\",\n" +
                "            \"price\" : \"" + price + "\"\n" +
                "        }";
        return jsonRequestBuilder("addItem", parameters, store);
	}
	
	public static String removeItemRequest(String managerID, String itemID, int quantity, String store) {
        String parameters = "{\n" +
                "            \"managerID\" : \"" + managerID + "\",\n" +
                "            \"itemID\" : \"" + itemID + "\",\n" +
                "            \"quantity\" : \"" + quantity + "\"\n" +
                "        }";
        return jsonRequestBuilder("removeItem", parameters, store);
	}

	public static String listItemAvailabilityRequest(String managerID, String store) {
        String parameters = "{\n" +
                "            \"managerID\" : \"" + managerID + "\"\n" +
                "        }";
        return jsonRequestBuilder("listItemAvailability", parameters, store);
	}
	
	public static String purchaseItemRequest(String customerID, String itemID, String dateOfPurchase, String store) {
        String parameters = "{\n" +
                "            \"customerID\" : \"" + customerID + "\",\n" +
                "            \"itemID\" : \"" + itemID + "\",\n" +
                "            \"dateOfPurchase\" : \"" + dateOfPurchase + "\"\n" +
                "        }";
        return jsonRequestBuilder("purchaseItem", parameters, store);
	}
	
	public static String findItemRequest(String customerID, String itemName, String store) {
        String parameters = "{\n" +
                "            \"customerID\" : \"" + customerID + "\",\n" +
                "            \"itemName\" : \"" + itemName + "\"\n" +
                "        }";
        return jsonRequestBuilder("findItem", parameters, store);	
	}
	
	public static String returnItemRequest(String customerID, String itemID, String dateOfReturn, String store) {
        String parameters = "{\n" +
                "            \"customerID\" : \"" + customerID + "\",\n" +
                "            \"itemID\" : \"" + itemID + "\",\n" +
                "            \"dateOfReturn\" : \"" + dateOfReturn + "\"\n" +
                "        }";
        return jsonRequestBuilder("returnItem", parameters, store);	
	}
	
	public static String exchangeItemRequest(String customerID, String newItemID, String oldItemID, String dateOfExchange, String store) {
        String parameters = "{\n" +
                "            \"customerID\" : \"" + customerID + "\",\n" +
                "            \"oldItemID\" : \"" + oldItemID + "\",\n" +
                "           \"newItemID\" : \"" + newItemID + "\",\n" +
                "            \"dateOfExchange\" : \"" + dateOfExchange + "\"\n" +
                "        }";
        return jsonRequestBuilder("exchangeItem", parameters, store);
	}
	
	public static String addCustomerWaitListRequest(String customerID, String itemID, String store) {
        String parameters = "{\n" +
                "            \"customerID\" : \"" + customerID + "\",\n" +
                "            \"itemID\" : \"" + itemID + "\"\n" +
                "        }";
        return jsonRequestBuilder("addCustomerWaitlist", parameters, store);
	}
}
