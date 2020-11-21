package replica.replica_nick.client;

import replica.replica_nick.impl.StoreInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public class ClientTest {
    private static StoreInterface qcStore;
    private static StoreInterface onStore;
    private static StoreInterface bcStore;
    private static boolean verbose;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-v"))
            verbose = true;

        qcStore = getStoreInterface("http://localhost:8081/QC?wsdl");
        onStore = getStoreInterface("http://localhost:8082/ON?wsdl");
        bcStore = getStoreInterface("http://localhost:8083/BC?wsdl");

        System.out.println("Concurrency Tests");
        test1();
        test2();
        test3();
        test4();
        test5();
        test6();
        test7();
        test8();
        test9();
        test10();
        test11();
        test12();
        test13();
        test14();
    }

    private static StoreInterface getStoreInterface(String endpoint) {
        URL url = null;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        QName qName = new QName("http://impl/", "StoreImplService");
        Service service = Service.create(url, qName);

        return service.getPort(StoreInterface.class);
    }

    // region manager only

    private static void test1() {
        Runnable task1 = () -> {qcStore.addItem("QCM1001", "QC2001", "TC-1", 1, 50);};
        Runnable task2 = () -> {qcStore.addItem("QCM1002", "QC2001", "TC-1", 2, 40);};
        Runnable task3 = () -> {qcStore.addItem("QCM1003", "QC2001", "TC-1", 3, 40);};
        Runnable task4 = () -> {qcStore.addItem("QCM1004", "QC2001", "TC-1", 4, 40);};
        Runnable task5 = () -> {qcStore.addItem("QCM1005", "QC2001", "TC-1", 5, 40);};
        Runnable task6 = () -> {qcStore.addItem("QCM1006", "QC2001", "TC-1", 6, 40);};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = qcStore.listItemAvailability("QCM9999"); // QC2001 qty = 1 or 20

        printInventory(result);
        printResult("TC-1", result.contains("QC2001\tTC-1\tqty: 1") || result.contains("QC2001\tTC-1\tqty: 20"));
    }

    private static void test2() {
        qcStore.addItem("QCM9999", "QC2002", "TC-2", 30, 100);

        Runnable task1 = () -> {qcStore.removeItem("QCM1001", "QC2002", 1);};
        Runnable task2 = () -> {qcStore.removeItem("QCM1002", "QC2002", 2);};
        Runnable task3 = () -> {qcStore.removeItem("QCM1003", "QC2002", 3);};
        Runnable task4 = () -> {qcStore.removeItem("QCM1004", "QC2002", 4);};
        Runnable task5 = () -> {qcStore.removeItem("QCM1005", "QC2002", 5);};
        Runnable task6 = () -> {qcStore.removeItem("QCM1006", "QC2002", 6);};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = qcStore.listItemAvailability("QCM9999"); // QC2002 qty = 9

        printInventory(result);
        printResult("TC-2", result.contains("QC2002\tTC-2\tqty: 9"));
    }

    private static void test3() {
        qcStore.addItem("QCM9999", "QC2003", "TC-3", 100, 100);

        Runnable task1 = () -> {qcStore.addItem("QCM1001", "QC2003", "TC-3", 12, 100);};
        Runnable task2 = () -> {qcStore.addItem("QCM1002", "QC2003", "TC-3", 34, 100);};
        Runnable task3 = () -> {qcStore.addItem("QCM1003", "QC2003", "TC-3", 68, 100);};
        Runnable task4 = () -> {qcStore.removeItem("QCM1004", "QC2003", 2);};
        Runnable task5 = () -> {qcStore.removeItem("QCM1005", "QC2003", 17);};
        Runnable task6 = () -> {qcStore.removeItem("QCM1006", "QC2003", 25);};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = qcStore.listItemAvailability("QCM9999"); // QC2003 qty = 170

        printInventory(result);
        printResult("TC-3", result.contains("QC2003\tTC-3\tqty: 170"));
    }

    // endregion manager only

    // region customer only

    private static void test4() {
        qcStore.addItem("QCM9999", "QC2004", "TC-4", 100, 100);

        Runnable task1 = () -> {qcStore.purchaseItem("QCU1001", "QC2004", "1-1-2020");};
        Runnable task2 = () -> {qcStore.purchaseItem("QCU1002", "QC2004", "1-1-2020");};
        Runnable task3 = () -> {onStore.purchaseItem("ONU1001", "QC2004", "1-1-2020");};
        Runnable task4 = () -> {onStore.purchaseItem("ONU1002", "QC2004", "1-1-2020");};
        Runnable task5 = () -> {bcStore.purchaseItem("BCU1001", "QC2004", "1-1-2020");};
        Runnable task6 = () -> {bcStore.purchaseItem("BCU1002", "QC2004", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = qcStore.listItemAvailability("QCM9999"); // QC2004 qty = 94

        printInventory(result);
        printResult("TC-4", result.contains("QC2004\tTC-4\tqty: 94"));
    }

    private static void test5() {
        qcStore.addItem("QCM9999", "QC2005", "TC-5", 100, 100);

        qcStore.purchaseItem("QCU2001", "QC2005", "1-1-2020");
        qcStore.purchaseItem("QCU2002", "QC2005", "1-1-2020");
        onStore.purchaseItem("ONU2001", "QC2005", "1-1-2020");
        onStore.purchaseItem("ONU2002", "QC2005", "1-1-2020");
        bcStore.purchaseItem("BCU2001", "QC2005", "1-1-2020");
        bcStore.purchaseItem("BCU2002", "QC2005", "1-1-2020");

        Runnable task1 = () -> {qcStore.returnItem("QCU2001", "QC2005", "1-1-2020");};
        Runnable task2 = () -> {qcStore.returnItem("QCU2002", "QC2005", "1-1-2020");};
        Runnable task3 = () -> {onStore.returnItem("ONU2001", "QC2005", "1-1-2020");};
        Runnable task4 = () -> {onStore.returnItem("ONU2002", "QC2005", "1-1-2020");};
        Runnable task5 = () -> {bcStore.returnItem("BCU2001", "QC2005", "1-1-2020");};
        Runnable task6 = () -> {bcStore.returnItem("BCU2002", "QC2005", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = qcStore.listItemAvailability("QCM9999"); // QC2005 qty = 100

        printInventory(result);
        printResult("TC-5", result.contains("QC2005\tTC-5\tqty: 100"));
    }

    private static void test6() {
        qcStore.addItem("QCM9999", "QC2006", "TC-6", 100, 100);

        qcStore.purchaseItem("QCU2002", "QC2006", "1-1-2020");
        onStore.purchaseItem("ONU2002", "QC2006", "1-1-2020");
        bcStore.purchaseItem("BCU2002", "QC2006", "1-1-2020");

        Runnable task1 = () -> {qcStore.purchaseItem("QCU2001", "QC2006", "1-1-2020");};
        Runnable task2 = () -> {onStore.purchaseItem("ONU2001", "QC2006", "1-1-2020");};
        Runnable task3 = () -> {bcStore.purchaseItem("BCU2001", "QC2006", "1-1-2020");};
        Runnable task4 = () -> {qcStore.returnItem("QCU2002", "QC2006", "1-1-2020");};
        Runnable task5 = () -> {onStore.returnItem("ONU2002", "QC2006", "1-1-2020");};
        Runnable task6 = () -> {bcStore.returnItem("BCU2002", "QC2006", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = qcStore.listItemAvailability("QCM9999"); // QC2006 qty = 97

        printInventory(result);
        printResult("TC-6", result.contains("QC2006\tTC-6\tqty: 97"));
    }

    private static void test7() {
        onStore.addItem("ONM9999", "ON2007", "TC-7-0", 100, 100);
        onStore.addItem("ONM9999", "ON2107", "TC-7-1", 100, 100);

        qcStore.purchaseItem("QCU1001", "ON2007", "1-1-2020");
        qcStore.purchaseItem("QCU1002", "ON2007", "1-1-2020");
        onStore.purchaseItem("ONU1001", "ON2007", "1-1-2020");
        onStore.purchaseItem("ONU1002", "ON2007", "1-1-2020");
        bcStore.purchaseItem("BCU1001", "ON2007", "1-1-2020");
        bcStore.purchaseItem("BCU1002", "ON2007", "1-1-2020");

        Runnable task1 = () -> {qcStore.exchangeItem("QCU1001", "ON2107", "ON2007", "1-1-2020");};
        Runnable task2 = () -> {qcStore.exchangeItem("QCU1002", "ON2107", "ON2007", "1-1-2020");};
        Runnable task3 = () -> {onStore.exchangeItem("ONU1001", "ON2107","ON2007", "1-1-2020");};
        Runnable task4 = () -> {onStore.exchangeItem("ONU1002", "ON2107", "ON2007", "1-1-2020");};
        Runnable task5 = () -> {bcStore.exchangeItem("BCU1001", "ON2107","ON2007", "1-1-2020");};
        Runnable task6 = () -> {bcStore.exchangeItem("BCU1002", "ON2107","ON2007", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = onStore.listItemAvailability("ONM9999"); // ON2007 qty = 100, ON2107 qty = 94

        printInventory(result);
        printResult("TC-7", result.contains("ON2007\tTC-7-0\tqty: 100") && result.contains("ON2107\tTC-7-1\tqty: 94"));
    }

    private static void test8() {
        onStore.addItem("ONM9999", "ON2008", "TC-8-0", 100, 100);
        onStore.addItem("ONM9999", "ON2108", "TC-8-1", 100, 100);

        qcStore.purchaseItem("QCU2002", "ON2008", "1-1-2020");
        onStore.purchaseItem("ONU2002", "ON2008", "1-1-2020");
        bcStore.purchaseItem("BCU2002", "ON2008", "1-1-2020");

        Runnable task1 = () -> {qcStore.purchaseItem("QCU2001", "ON2108", "1-1-2020");};
        Runnable task2 = () -> {onStore.purchaseItem("ONU2001", "ON2108", "1-1-2020");};
        Runnable task3 = () -> {bcStore.purchaseItem("BCU2001", "ON2108", "1-1-2020");};
        Runnable task4 = () -> {qcStore.exchangeItem("QCU2002", "ON2108", "ON2008", "1-1-2020");};
        Runnable task5 = () -> {onStore.exchangeItem("ONU2002", "ON2108","ON2008", "1-1-2020");};
        Runnable task6 = () -> {bcStore.exchangeItem("BCU2002", "ON2108","ON2008", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = onStore.listItemAvailability("ONM9999"); // ON2008 qty = 100, ON2108 qty = 94

        printInventory(result);
        printResult("TC-8", result.contains("ON2008\tTC-8-0\tqty: 100") && result.contains("ON2108\tTC-8-1\tqty: 94"));
    }

    private static void test9() {
        bcStore.addItem("BCM9999", "BC2009", "TC-9-0", 100, 100);
        bcStore.addItem("BCM9999", "BC2109", "TC-9-1", 100, 100);

        qcStore.purchaseItem("QCU1001", "BC2009", "1-1-2020");
        qcStore.purchaseItem("QCU1002", "BC2009", "1-1-2020");
        onStore.purchaseItem("ONU1001", "BC2009", "1-1-2020");
        onStore.purchaseItem("ONU1002", "BC2009", "1-1-2020");
        bcStore.purchaseItem("BCU1001", "BC2009", "1-1-2020");
        bcStore.purchaseItem("BCU1002", "BC2009", "1-1-2020");

        Runnable task1 = () -> {qcStore.returnItem("QCU1001", "BC2009", "1-1-2020");};
        Runnable task2 = () -> {onStore.returnItem("ONU1001", "BC2009", "1-1-2020");};
        Runnable task3 = () -> {bcStore.returnItem("BCU1001", "BC2009", "1-1-2020");};
        Runnable task4 = () -> {qcStore.exchangeItem("QCU1002", "BC2109", "BC2009", "1-1-2020");};
        Runnable task5 = () -> {onStore.exchangeItem("ONU1002", "BC2109","BC2009", "1-1-2020");};
        Runnable task6 = () -> {bcStore.exchangeItem("BCU1002", "BC2109","BC2009", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = bcStore.listItemAvailability("BCM9999"); // BC2009 qty = 100, BC2109 qty = 97

        printInventory(result);
        printResult("TC-9", result.contains("BC2009\tTC-9-0\tqty: 100") && result.contains("BC2109\tTC-9-1\tqty: 97"));
    }

    private static void test10() {
        bcStore.addItem("BCM9999", "BC2010", "TC-10-0", 100, 100);
        bcStore.addItem("BCM9999", "BC2110", "TC-10-1", 100, 100);

        onStore.purchaseItem("ONU2001", "BC2010", "1-1-2020");
        onStore.purchaseItem("ONU2002", "BC2010", "1-1-2020");
        bcStore.purchaseItem("BCU2001", "BC2010", "1-1-2020");
        bcStore.purchaseItem("BCU2002", "BC2010", "1-1-2020");

        Runnable task1 = () -> {qcStore.purchaseItem("QCU2001", "BC2110", "1-1-2020");};
        Runnable task2 = () -> {qcStore.purchaseItem("QCU2002", "BC2110", "1-1-2020");};
        Runnable task3 = () -> {onStore.returnItem("ONU2001", "BC2010", "1-1-2020");};
        Runnable task4 = () -> {onStore.returnItem("ONU2002", "BC2010", "1-1-2020");};
        Runnable task5 = () -> {bcStore.exchangeItem("BCU2001", "BC2110","BC2010", "1-1-2020");};
        Runnable task6 = () -> {bcStore.exchangeItem("BCU2002", "BC2110","BC2010", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = bcStore.listItemAvailability("BCM9999"); // BC2010 qty = 100, BC2110 qty = 96

        printInventory(result);
        printResult("TC-10", result.contains("BC2010\tTC-10-0\tqty: 100") && result.contains("BC2110\tTC-10-1\tqty: 96"));
    }

    // endregion customer only

    // region customers and managers

    private static void test11() {
        qcStore.addItem("QCM9999", "QC2011", "TC-11", 100, 100);

        Runnable task1 = () -> {qcStore.addItem("QCM1001", "QC2011", "TC-11", 15, 100);};
        Runnable task2 = () -> {qcStore.removeItem("QCM1002", "QC2011", 5);};
        Runnable task3 = () -> {qcStore.purchaseItem("QCU3001", "QC2011", "1-1-2020");};
        Runnable task4 = () -> {onStore.purchaseItem("ONU3001", "QC2011", "1-1-2020");};
        Runnable task5 = () -> {bcStore.purchaseItem("BCU3001", "QC2011", "1-1-2020");};
        Runnable task6 = () -> {};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = qcStore.listItemAvailability("QCM9999"); // QC2011 qty = 107

        printInventory(result);
        printResult("TC-11", result.contains("QC2011\tTC-11\tqty: 107"));
    }

    private static void test12() {
        onStore.addItem("ONM9999", "ON2012", "TC-12", 100, 100);

        qcStore.purchaseItem("QCU3001", "ON2012", "1-1-2020");
        onStore.purchaseItem("ONU3001", "ON2012", "1-1-2020");
        bcStore.purchaseItem("BCU3001", "ON2012", "1-1-2020");

        Runnable task1 = () -> {onStore.addItem("ONM1001", "ON2012", "TC-12", 15, 100);};
        Runnable task2 = () -> {onStore.removeItem("ONM1002", "ON2012", 5);};
        Runnable task3 = () -> {qcStore.returnItem("QCU3001", "ON2012", "1-1-2020");};
        Runnable task4 = () -> {onStore.returnItem("ONU3001", "ON2012", "1-1-2020");};
        Runnable task5 = () -> {bcStore.returnItem("BCU3001", "ON2012", "1-1-2020");};
        Runnable task6 = () -> {};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = onStore.listItemAvailability("ONM9999"); // ON2012 qty = 110

        printInventory(result);
        printResult("TC-12", result.contains("ON2012\tTC-12\tqty: 110"));
    }

    private static void test13() {
        onStore.addItem("ONM9999", "ON2013", "TC-13-0", 100, 100);
        onStore.addItem("ONM9999", "ON2113", "TC-13-1", 100, 100);

        qcStore.purchaseItem("QCU3001", "ON2013", "1-1-2020");
        onStore.purchaseItem("ONU3001", "ON2013", "1-1-2020");
        bcStore.purchaseItem("BCU3001", "ON2013", "1-1-2020");

        Runnable task1 = () -> {onStore.addItem("ONM1001", "ON2013", "TC-13-0", 15, 100);};
        Runnable task2 = () -> {onStore.removeItem("ONM1002", "ON2013", 5);};
        Runnable task3 = () -> {onStore.addItem("ONM1003", "ON2113", "TC-13-1", 15, 100);};
        Runnable task4 = () -> {qcStore.exchangeItem("QCU3001", "ON2113", "ON2013", "1-1-2020");};
        Runnable task5 = () -> {onStore.exchangeItem("ONU3001", "ON2113", "ON2013", "1-1-2020");};
        Runnable task6 = () -> {bcStore.exchangeItem("BCU3001", "ON2113", "ON2013", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = onStore.listItemAvailability("ONM9999"); // ON2013 qty = 110, ON2113 qty = 112

        printInventory(result);
        printResult("TC-13", result.contains("ON2013\tTC-13-0\tqty: 110") && result.contains("ON2113\tTC-13-1\tqty: 112"));
    }

    private static void test14() {
        bcStore.addItem("BCM9999", "BC2014", "TC-14-0", 100, 100);
        bcStore.addItem("BCM9999", "BC2114", "TC-14-1", 100, 100);

        onStore.purchaseItem("ONU3001", "BC2014", "1-1-2020");
        bcStore.purchaseItem("BCU3001", "BC2014", "1-1-2020");

        Runnable task1 = () -> {bcStore.addItem("BCM1001", "BC2014", "TC-14-0", 15, 100);};
        Runnable task2 = () -> {bcStore.removeItem("BCM1002", "BC2014", 5);};
        Runnable task3 = () -> {bcStore.addItem("BCM1003", "BC2114", "TC-14-1", 15, 100);};
        Runnable task4 = () -> {qcStore.purchaseItem("QCU3001", "BC2114", "1-1-2020");};
        Runnable task5 = () -> {onStore.returnItem("ONU3001", "BC2014", "1-1-2020");};
        Runnable task6 = () -> {bcStore.exchangeItem("BCU3001", "BC2114", "BC2014", "1-1-2020");};

        runTasks(task1, task2, task3, task4, task5, task6);

        String result = bcStore.listItemAvailability("BCM9999"); // BC2014 qty = 110, BC2114 qty = 113

        printInventory(result);
        printResult("TC-14", result.contains("BC2014\tTC-14-0\tqty: 110") && result.contains("BC2114\tTC-14-1\tqty: 113"));
    }

    // endregion customers and managers

    private static void runTasks(Runnable task1, Runnable task2, Runnable task3, Runnable task4, Runnable task5, Runnable task6) {
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        Thread thread3 = new Thread(task3);
        Thread thread4 = new Thread(task4);
        Thread thread5 = new Thread(task5);
        Thread thread6 = new Thread(task6);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
            thread5.join();
            thread6.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printInventory(String inventory) {
        if (verbose) {
            System.out.println();
            System.out.println("===========================================================================");
            System.out.println(inventory);
        }
    }

    private static void printResult(String testCase, boolean condition) {
        if (condition) {
            System.out.println(testCase + " PASS");
        } else {
            System.out.println(testCase + " FAIL");
        }
    }
}
