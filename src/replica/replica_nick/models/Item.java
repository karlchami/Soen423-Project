package replica.replica_nick.models;

public class Item {
    private String name;
    private int quantity;
    private double price;

    public Item(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public synchronized void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public synchronized void removeQuantity(int quantity) {
        this.quantity -= quantity;
    }

    public synchronized void incrementQuantity() {
        this.quantity++;
    }

    public synchronized void decrementQuantity() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }

    @Override
    public String toString() {
        return name + "\tqty: " + quantity + "\tprice: $" + price;
    }
}
