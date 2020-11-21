package Model;

public class Item {
    public String itemID;
    public String itemName;
    public String storeID;
    public int itemQty;
    public int price;

    public Item(String itemID, String itemName, String storeID, int itemQty, int price) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.storeID = storeID;
        this.itemQty = itemQty;
        this.price = price;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public int getItemQty() {
        return itemQty;
    }

    public void setItemQty(int itemQty) {
        this.itemQty = itemQty;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
