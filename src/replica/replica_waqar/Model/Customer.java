package Model;

public class Customer {
    private int budget;

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public Customer() {
        budget = 1000;
    }
}
