package Store;
import javax.json.Json;
import javax.json.JsonReader;


public class ResponseHelper {
	
	public static String jsonBuilder(String sequence_id, String response_details) {
        String main_structure = Json.createObjectBuilder()
                .add("replica_id", "karl")
                .add("sequence_id", sequence_id)
                .add("response_details", response_details)
                .build()
                .toString();
		return main_structure;
	}
	
	public static String addItemResponse(String sequence_id, String status_code, String managerID, String itemID, String itemName, int quantity, int price) {
		String message = managerID + " add (" + itemID + "," + itemName + "," + quantity + "," + price + ") " + status_code;
        
		String parameter_structure = Json.createObjectBuilder()
                .add("managerID", managerID)
                .add("itemID", itemID)
                .add("itemName", itemName)
                .add("quantity", quantity)
                .add("price", price)
                .build()
                .toString();
        
		String response_structure = Json.createObjectBuilder()
                .add("method_name", "addItem")
                .add("message", message)
                .add("status_code", status_code)
                .add("parameters", parameter_structure)
                .build()
                .toString();
		
		return jsonBuilder(sequence_id, response_structure);	
	}
	
	public static String removeItemResponse(String sequence_id, String status_code, String managerID, String itemID, int quantity) {
		String message = managerID + " removal " + quantity + " x " + itemID + " " + status_code;
        
		String parameter_structure = Json.createObjectBuilder()
                .add("managerID", managerID)
                .add("itemID", itemID)
                .add("quantity", quantity)
                .build()
                .toString();
        
		String response_structure = Json.createObjectBuilder()
                .add("method_name", "removeItem")
                .add("message", message)
                .add("status_code", status_code)
                .add("parameters", parameter_structure)
                .build()
                .toString();
		
		return jsonBuilder(sequence_id, response_structure);
	}

	public static String listItemAvailabilityResponse(String sequence_id, String status_code, String managerID, String available_items) {
		String message = available_items;
        
		String parameter_structure = Json.createObjectBuilder()
				.add("managerID", managerID)
                .add("available_items", available_items)
                .build()
                .toString();
        
		String response_structure = Json.createObjectBuilder()
                .add("method_name", "listItemAvailability")
                .add("message", message)
                .add("status_code", status_code)
                .add("parameters", parameter_structure)
                .build()
                .toString();
		
		return jsonBuilder(sequence_id, response_structure);
	}
	
	public static String purchaseItemResponse(String sequence_id, String status_code, String customerID, String itemID, String dateOfPurchase) {
		String message = customerID + " purchase " + itemID + " on " + dateOfPurchase + " " + status_code;
        
		String parameter_structure = Json.createObjectBuilder()
                .add("customerID", customerID)
                .add("itemID", itemID)
                .add("dateOfPurchase", dateOfPurchase)
                .build()
                .toString();
        
		String response_structure = Json.createObjectBuilder()
                .add("method_name", "purchaseItem")
                .add("message", message)
                .add("status_code", status_code)
                .add("parameters", parameter_structure)
                .build()
                .toString();
		
		return jsonBuilder(sequence_id, response_structure);
	}
	
	public static String findItemResponse(String sequence_id, String status_code, String customerID, String itemName, String found_items) {
		String message = found_items;
		
		String parameter_structure = Json.createObjectBuilder()
                .add("customerID", customerID)
                .add("itemName", itemName)
                .build()
                .toString();
        
		String response_structure = Json.createObjectBuilder()
                .add("method_name", "findItem")
                .add("message", message)
                .add("status_code", status_code)
                .add("parameters", parameter_structure)
                .build()
                .toString();
		
		return jsonBuilder(sequence_id, response_structure);
	}
	
	public static String exchangeItemResponse(String sequence_id, String status_code, String customerID, String newItemID, String oldItemID, String dateOfExchange) {
		String message = customerID + " exchange of " + oldItemID + " with " + newItemID + " on " + dateOfExchange + " " + status_code;
		
		String parameter_structure = Json.createObjectBuilder()
                .add("customerID", customerID)
                .add("newItemID", newItemID)
                .add("oldItemID", oldItemID)
                .add("dateOfExchange", dateOfExchange)
                .build()
                .toString();
        
		String response_structure = Json.createObjectBuilder()
                .add("method_name", "exchangeItem")
                .add("message", message)
                .add("status_code", status_code)
                .add("parameters", parameter_structure)
                .build()
                .toString();
		
		return jsonBuilder(sequence_id, response_structure);
	}
}
