package replica.replica_nick.models;

import java.time.LocalDate;

public class Purchase {
    private String itemID;
    private int price;
    private LocalDate dateOfPurchase;

    public Purchase(String itemID, int price, LocalDate dateOfPurchase) {
        this.itemID = itemID;
        this.price = price;
        this.dateOfPurchase = dateOfPurchase;
    }

    public String getItemID() {
        return itemID;
    }

    public int getPrice() {
        return price;
    }

    public LocalDate getDateOfPurchase() {
        return dateOfPurchase;
    }
}
