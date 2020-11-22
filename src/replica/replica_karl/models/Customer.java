package models;

public class Customer {
    private String customerID;
    private long balance;
    private Store store;
    
    public Customer(String customerID, Store store){
        this.customerID = customerID;
        this.store = store;
        this.balance = 1000;
    }
    public String getCustomerID(){
        return this.customerID;
    }
    public long getBalance(){
        return this.balance;
    }
    public void setBalance(long balance){
        this.balance = balance;
    }
	public Store getStore() {
		return store;
	}
}
