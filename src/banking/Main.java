package banking;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static int balance = 0;
    public static boolean login = true;
    public static boolean exit = true;
    public static String url;
    public static int id = 1;
    public static String cardNumber2;
    public static int addIncome;


    public static void main(String[] args) {

        String fileName = args[1];
        Scanner scan = new Scanner(System.in);
        createNewDatabase(fileName);
        createNewTable();


        while (exit) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            int input = scan.nextInt();
            switch (input) {
                case 1:
                    System.out.println("Your card has been created");
                    System.out.println("Your card number:");
                    String cardNumber = generateCardNumber();
                    System.out.println(cardNumber);
                    System.out.println("Your card PIN:");
                    String cardPin = generatePin();
                    System.out.println(cardPin);
                    InsertApp app = new InsertApp();
                    // insert into database
                    app.insert(id, cardNumber, cardPin, balance);
                    break;
                case 2:
                    System.out.println("Enter your card number:");
                    cardNumber2 = scan.next();
                    System.out.println("Enter your pin:");
                    String pin2 = scan.next();
                    SelectApp app2 = new SelectApp();
                    if (pin2.equals(app2.selectPin(cardNumber2))) {
                        System.out.println("You have successfully logged in");
                        accountMenu();
                    } else System.out.println("Wrong card number or PIN!");
                    break;
                case 0:
                    exit = false;
                    System.out.println("Bye!");
            }
        }
    }

    public static void accountMenu() {

        while (login != false) {
            Scanner scan = new Scanner(System.in);

            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");


            int input = scan.nextInt();
            UpdateApp app2 = new UpdateApp();
            SelectApp app = new SelectApp();
            switch (input) {
                case 1:
                    System.out.println("Balance: " + app.selectBalance(cardNumber2));
                    break;
                case 2:
                    System.out.println("Enter income: ");
                    addIncome = scan.nextInt();
                    app2.updateBalance(addIncome, cardNumber2);
                    System.out.println("Income was added");
                    break;
                case 3:
                    System.out.println("Enter card number: ");
                    String transferToCardNumber = scan.next();
                    if (!checkLuhn(transferToCardNumber)){
                        System.out.println("Probably you made mistake in the card number. Please try again!");
                        break;
                    }
                    else if (transferToCardNumber == cardNumber2) {
                        System.out.println("You can't transfer money to the same account!");
                        break;
                    }
                    else if (app.selectCardNumber(transferToCardNumber)) {
                        System.out.println("Enter how much money you want to transfer: ");
                        int transferAmount = scan.nextInt();
                        if (transferAmount > app.selectBalance(cardNumber2)) {
                            System.out.println("Not enough money");
                        } else {
                            //transfer from
                            app2.updateBalance(-transferAmount, cardNumber2);

                            //transfer to
                            app2.updateBalance(transferAmount, transferToCardNumber);
                        }
                    }
                    else {
                        System.out.println("Such a card does not exist");
                    }
                    break;

                case 4:
                    DeleteApp app3 = new DeleteApp();
                    app3.deleteCard(cardNumber2);
                    System.out.println("The account has been closed");
                    break;
                case 5:
                    login = false;
                    break;
                case 0:
                    login = false;
                    exit = false;
                    break;
            }
        }


    }

    public static String generateCardNumber() {

        Random random = new Random();

        ArrayList<Integer> array = new ArrayList<Integer>();
        array.add(0, 4);
        array.add(1, 0);
        array.add(1, 0);
        array.add(1, 0);
        array.add(1, 0);
        array.add(1, 0);

        for (int i = 0; i < 9; i++)
            array.add(random.nextInt(9));

        //calculate the sum of digits using Luhn`s algorithm
        int sum = 0;
        for (int i = 0; i < array.size(); i += 2) {
            int n = array.get(i) * 2;
            if (n > 9) {
                n = n - 9;
            }
            sum += n;
        }
        for (int i = 1; i < array.size(); i += 2) {
            int m = array.get(i);
            sum += m;
        }
        //Get last check sum digit of the card.
        int checkSumInt = 0;
        if (sum % 10 != 0) {
            checkSumInt = 10 - (sum % 10);
        }
        array.add(checkSumInt);

        Long result = 0L;
        for (int temp2 = 0; temp2 < array.size(); temp2++) {
            result *= 10;
            result += array.get(temp2);
        }

        return Long.toString(result);


    }

    static boolean checkLuhn(String cardNo) {
        int nDigits = cardNo.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--)
        {

            int d = cardNo.charAt(i) - '0';

            if (isSecond == true)
                d = d * 2;

            // We add two digits to handle
            // cases that make two digits
            // after doubling
            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }

    public static String generatePin() {

        Random random = new Random();

        int pin = random.nextInt(10000 - 1000 + 1) + 1000;
        return Integer.toString(pin);

    }

    public static void createNewDatabase(String fileName) {

        url = "jdbc:sqlite:" + fileName;


        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTable() {

        System.out.println(url);

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + "	id INTEGER,\n"
                + "	number TEXT,\n"
                + "	pin TEXT,\n"
                + "	balance INTEGER DEFAULT 0\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static class InsertApp {

        /**
         * Connect to the card.db database
         *
         * @return the Connection object
         */
        private Connection connect() {
            // SQLite connection string

            Connection conn = null;
            try {
                conn = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return conn;
        }

        /**
         * Insert a new row into the card table
         */
        public void insert(int id, String number, String pin, int balance) {
            String sql = "INSERT INTO card(id, number, pin, balance) VALUES(?,?,?,?)";

            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, number);
                pstmt.setString(3, pin);
                pstmt.setInt(4, balance);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }


    }

    public static class SelectApp {

        /**
         * Connect to the database
         *
         * @return the Connection object
         */
        private Connection connect() {
            // SQLite connection string

            Connection conn = null;
            try {
                conn = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return conn;
        }


        /**
         * return the pin for the cardNumber from the db.
         */
        public String selectPin(String cardNumber2) {
            String pin = "";
            String sql = "SELECT pin FROM card WHERE number = '" + cardNumber2 + "'";

            try (Connection conn = this.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                // loop through the result set
                while (rs.next()) {
                    pin = rs.getString("pin");

                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return pin;
        }

        public int selectBalance(String cardNumber2) {
            int balance2 = 0;

            String sql = "SELECT balance FROM card WHERE number = '" + cardNumber2 + "'";

            try (Connection conn = this.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                // loop through the result set
                while (rs.next()) {
                    balance2 = rs.getInt("balance");

                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return balance2;

        }
        public boolean selectCardNumber(String cardNumber2) {
            boolean isInDb = false;
            String cardSearch = "";
            String sql = "SELECT number FROM card WHERE number = '" + cardNumber2 + "'";

            try (Connection conn = this.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                // loop through the result set
                while (rs.next()) {
                    cardSearch = rs.getString("number");

                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            if (cardSearch.equals(cardNumber2)){
                isInDb = true;
            }
            return isInDb;


        }
    }

    public static class UpdateApp {

        /**
         * Connect to the test.db database
         *
         * @return the Connection object
         */
        private Connection connect() {

            Connection conn = null;
            try {
                conn = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return conn;
        }

        /**
         * Update balance of a card specified by the cardNumber
         */
        public void updateBalance(int addIncome, String cardNumber2) {
            String sql = "UPDATE card SET balance = balance + ? WHERE number = " + cardNumber2;
            System.out.println( balance);


            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // set the corresponding param

                pstmt.setInt(1, addIncome);
                // update
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        }


    }

    public static class DeleteApp {

        /**
         * Connect to the card.db database
         *
         * @return the Connection object
         */
        private Connection connect() {

            Connection conn = null;
            try {
                conn = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return conn;
        }

        /**
         * Delete a card specified by the number
         */
        public void deleteCard(String cardNumber) {
            String sql = "DELETE FROM card WHERE number = ?";

            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // set the corresponding param
                pstmt.setString(1, cardNumber);
                // execute the delete statement
                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}