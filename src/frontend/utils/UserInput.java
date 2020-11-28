package frontend.utils;

import java.time.LocalDate;
import java.util.Scanner;

public class UserInput {
    private Scanner sc = new Scanner(System.in);

    // region Prompt Methods

    public String promptAny() {
        return sc.nextLine();
    }

    public String promptStore() {
        String store = sc.nextLine();

        while (!validateStore(store)) {
            System.out.println("Invalid store location. Please enter QC, ON, or BC.");
            store = sc.nextLine();
        }
        return store.toUpperCase();
    }

    public String promptManagerOptions() {
//        System.out.println("1. Add item\n" +
//                "2. Remove item\n" +
//                "3. List available items\n" +
//                "\nPlease enter the number of the action you want to perform:");

        String option = sc.nextLine();

        while (!option.equals("1") && !option.equals("2") && !option.equals("3")) {
            System.out.println("Invalid option. Please enter 1, 2, or 3.");
            option = sc.nextLine();
        }
        return option;
    }

    public String promptCustomerOptions() {
//        System.out.println("1. Purchase item\n" +
//                "2. Find item\n" +
//                "3. Return item\n" +
//                "4. Exchange item\n" +
//                "\nPlease enter the number of the action you want to perform:");

        String option = sc.nextLine();

        while (!option.equals("1") && !option.equals("2") && !option.equals("3") && !option.equals("4")) {
            System.out.println("Invalid option. Please enter 1, 2, 3, or 4.");
            option = sc.nextLine();
        }
        return option;
    }

    public String promptUserID() {
//        System.out.println("Please enter your ID:");
        String userID = sc.nextLine();

        while (!validateUserIDNumber(userID)) {
            System.out.println("Invalid user ID. Please enter a 4-digit number.");
            userID = sc.nextLine();
        }
        return userID.toUpperCase();
    }

    public String promptItemID() {
//        System.out.println("Please enter the item ID:");
        String itemID = sc.nextLine();

        while (!validateItemID(itemID)) {
            System.out.println("Invalid item ID. Please try again");
            itemID = sc.nextLine();
        }
        return itemID.toUpperCase();
    }

    public String promptItemName() {
        System.out.println("Please enter the item name:");
        String itemName = sc.nextLine();

        while (!validateItemName(itemName)) {
            System.out.println("Item name cannot contain underscores, commas, or semicolons. Please try again.");
        }
        return itemName;
    }

    public int promptAddQuantity() {
//        System.out.println("Please enter the quantity to add:");
        int qty;
        try {
            qty = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            qty = -99;
        }

        while (qty < 0) {
            System.out.println("The quantity must be a non-negative number. Please try again.");
            try {
                qty = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                qty = -99;
            }
        }
        return qty;
    }

    public int promptRemoveQuantity() {
//        System.out.println("Please enter the quantity to remove (enter -1 to completely remove the item from inventory):");
        int qty;
        try {
            qty = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            qty = -99;
        }

        while (qty < -1) {
            System.out.println("The quantity must be a non-negative number. Please try again.");
            try {
                qty = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                qty = -99;
            }
        }
        return qty;
    }

    public int promptPrice() {
//        System.out.println("Please enter the price of the item:");
        int price;
        try {
            price = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            price = -1;
        }

        while (price <= 0) {
            System.out.println("Price must be a positive integer. Please try again.");
            try {
                price = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                price = -1;
            }
        }
        return price;
    }

    public String promptDate() {
//        System.out.println("Please enter today's date in the format DD-MM-YYYY:");
        String date = sc.nextLine();

        while (!validateDate(date)) {
            System.out.println("Invalid date. Please try again.");
            date = sc.nextLine();
        }
        return date;
    }

    public boolean promptWaitList() {
        System.out.println("Would you like to be added to the wait list? (y/n)");
        String response = sc.nextLine();

        while (!validateYesOrNo(response)) {
            System.out.println("Invalid response. Please enter 'y' or 'n'.");
            response = sc.nextLine();
        }
        return response.equalsIgnoreCase("y");
    }

    public boolean promptContinue() {
        System.out.println("\nWould you like to continue? (y/n)");
        String response = sc.nextLine();

        while (!validateYesOrNo(response)) {
            System.out.println("Invalid response. Please enter 'y' or 'n'.");
            response = sc.nextLine();
        }
        return response.equalsIgnoreCase("y");
    }

    // endregion

    // region Validate methods

    private static boolean validateStore(String store) {
        return store.equalsIgnoreCase("QC") || store.equalsIgnoreCase("ON") || store.equalsIgnoreCase("BC");
    }

    private static boolean validateUserID(String userID) {
        if (userID.length() != 7) {
            return false;
        } else if (!userID.substring(0, 2).equalsIgnoreCase("QC")
                && !userID.substring(0, 2).equalsIgnoreCase("ON")
                && !userID.substring(0, 2).equalsIgnoreCase("BC")) {
            return false;
        } else if (!userID.substring(2, 3).equalsIgnoreCase("U")
                && !userID.substring(2, 3).equalsIgnoreCase("M")) {
            return false;
        }

        try {
            Integer.parseInt(userID.substring(3));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private static boolean validateUserIDNumber(String userID) {
        try {
            Integer.parseInt(userID);
        } catch (NumberFormatException e) {
            return false;
        }
        return userID.length() == 4;
    }

    private static boolean validateItemID(String itemID) {
        if (itemID.length() != 6) {
            return false;
        } else if (!itemID.substring(0, 2).equalsIgnoreCase("QC")
                && !itemID.substring(0, 2).equalsIgnoreCase("ON")
                && !itemID.substring(0, 2).equalsIgnoreCase("BC")) {
            return false;
        }

        try {
            Integer.parseInt(itemID.substring(2));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private static boolean validateItemName(String itemName) {
        return !itemName.contains("_") && !itemName.contains(",") && !itemName.contains((";"));
    }

    private static boolean validateYesOrNo(String response) {
        return response.equalsIgnoreCase("y") || response.equalsIgnoreCase("n");
    }

    private static boolean validateDate(String date) {
        String[] dateValues = date.split("-");
        if (dateValues.length != 3) {
            return false;
        }

        try {
            int day = Integer.parseInt(dateValues[0]);
            int month = Integer.parseInt(dateValues[1]);
            int year = Integer.parseInt(dateValues[2]);

            if (year < 1000 || year > 9999) {
                return false;
            }
            LocalDate.of(year, month, day);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // endregion
}
