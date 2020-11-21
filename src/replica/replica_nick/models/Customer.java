package replica.replica_nick.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Customer {
    private double balance;
    private ArrayList<Purchase> purchases;
    private boolean qcLimitReached = false;
    private boolean onLimitReached = false;
    private boolean bcLimitReached = false;

    public Customer() {
        balance = 1000;
        purchases = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    public void increaseBalance(double amount) {
        this.balance += amount;
    }

    public void decreaseBalance(double amount) {
        this.balance -= amount;
    }

    public void addPurchase(Purchase purchase) {
        purchases.add(purchase);
        sortByMostRecent();
    }

    public void removePurchase(Purchase purchase) {
        purchases.remove(purchase);
        sortByMostRecent();
    }

    public Purchase findPurchase(String itemID) {
        for (Purchase purchase : purchases) {
            if (purchase.getItemID().equalsIgnoreCase(itemID)) {
                return purchase;
            }
        }
        return null;
    }

    public boolean isLimitReached(String itemID) {
        switch (itemID.substring(0, 2)) {
            case "QC":
                return qcLimitReached;
            case "ON":
                return onLimitReached;
            case "BC":
                return bcLimitReached;
            default:
                return true;
        }
    }

    public void setLimit(String itemID) {
        switch (itemID.substring(0, 2)) {
            case "QC":
                qcLimitReached = true;
                break;
            case "ON":
                onLimitReached = true;
                break;
            case "BC":
                bcLimitReached = true;
                break;
        }
    }

    public void resetLimit(String itemID) {
        switch (itemID.substring(0, 2)) {
            case "QC":
                qcLimitReached = false;
                break;
            case "ON":
                onLimitReached = false;
                break;
            case "BC":
                bcLimitReached = false;
                break;
        }
    }

    private void sortByMostRecent() {
        purchases.sort(Comparator.comparing(Purchase::getDateOfPurchase));
        Collections.reverse(purchases);
    }
}

