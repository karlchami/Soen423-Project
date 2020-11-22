package models;

public class Manager {
	private String managerID;
	private Store store;
	
	public Manager(String managerID, Store store) {
		this.managerID = managerID;
		this.store = store;
		
	}
	public String getManagerID() {
		return managerID;
	}
	public Store getStore() {
		return store;
	}

}
