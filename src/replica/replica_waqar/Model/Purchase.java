package Model;

public class Purchase {

    private String itemID;
    private String customerID;
    private int price;

    public Purchase(String itemID, int price, String customerID) {
        this.itemID = itemID;
        this.price = price;
        this.customerID = customerID;
    }


    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
}
