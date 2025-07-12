import java.sql.*;
import java.util.Scanner;

public class ATMDatabaseSimulator {

    // Update DB details here
    static final String DB_URL = "jdbc:mysql://localhost:3306/atm_db";
    static final String USER = "root";         // your MySQL username
    static final String PASS = "GokulMysql@2004";     // your MySQL password

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("===== Welcome to ATM =====");

            System.out.print("Enter your account number: ");
            int accNo = sc.nextInt();
            sc.nextLine(); // clear buffer

            System.out.print("Enter your PIN: ");
            String pin = sc.nextLine();

            if (!verifyLogin(conn, accNo, pin)) {
                System.out.println("❌ Invalid account number or PIN.");
                return;
            }

            int choice;
            do {
                System.out.println("\n===== ATM Menu =====");
                System.out.println("1. Check Balance");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");
                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        checkBalance(conn, accNo);
                        break;
                    case 2:
                        deposit(conn, accNo, sc);
                        break;
                    case 3:
                        withdraw(conn, accNo, sc);
                        break;
                    case 4:
                        System.out.println("👋 Thank you for using ATM.");
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }

            } while (choice != 4);

        } catch (SQLException e) {
            System.out.println("❌ Database error.");
            e.printStackTrace();
        }
    }

    static boolean verifyLogin(Connection conn, int accNo, String pin) throws SQLException {
        String sql = "SELECT * FROM users WHERE account_no = ? AND pin = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accNo);
            stmt.setString(2, pin);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // login successful if user exists
        }
    }

    static void checkBalance(Connection conn, int accNo) throws SQLException {
        String sql = "SELECT balance FROM users WHERE account_no = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.printf("💰 Current balance: ₹%.2f\n", balance);
            }
        }
    }

    static void deposit(Connection conn, int accNo, Scanner sc) throws SQLException {
        System.out.print("Enter amount to deposit: ₹");
        double amount = sc.nextDouble();
        if (amount <= 0) {
            System.out.println("❌ Invalid deposit amount.");
            return;
        }

        String sql = "UPDATE users SET balance = balance + ? WHERE account_no = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accNo);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Deposit successful.");
            }
        }
    }

    static void withdraw(Connection conn, int accNo, Scanner sc) throws SQLException {
        System.out.print("Enter amount to withdraw: ₹");
        double amount = sc.nextDouble();
        if (amount <= 0) {
            System.out.println("❌ Invalid withdrawal amount.");
            return;
        }

        // Check current balance first
        String checkSql = "SELECT balance FROM users WHERE account_no = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, accNo);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (currentBalance < amount) {
                    System.out.println("❌ Insufficient balance.");
                    return;
                }
            }
        }

        // Deduct the balance
        String withdrawSql = "UPDATE users SET balance = balance - ? WHERE account_no = ?";
        try (PreparedStatement withdrawStmt = conn.prepareStatement(withdrawSql)) {
            withdrawStmt.setDouble(1, amount);
            withdrawStmt.setInt(2, accNo);
            int rows = withdrawStmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Withdrawal successful.");
            }
        }
    }
}
