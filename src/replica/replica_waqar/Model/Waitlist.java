package Model;

public class Waitlist {
    private String customerID;
    private String itemID;

    public Waitlist(String customerID, String itemID) {
        this.customerID = customerID;
        this.itemID = itemID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
}
