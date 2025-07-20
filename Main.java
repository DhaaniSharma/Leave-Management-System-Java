import java.awt.Desktop;
import java.io.*;
import java.util.*;

class User {
    String username, password;
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class Employee extends User {
    public Employee(String username, String password) {
        super(username, password);
    }

    public void applyLeave() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter leave reason: ");
        String reason = sc.nextLine();
        System.out.print("Starting date (YYYY-MM-DD): ");
        String from = sc.nextLine();
        System.out.print("Ending date (YYYY-MM-DD): ");
        String to = sc.nextLine();

        BufferedWriter bw = new BufferedWriter(new FileWriter("leaveRequests.txt", true));
        bw.write(this.username + "," + this.password + "," + from + "," + to + "," + reason + "====PENDING!====\n");
        bw.close();

        System.out.println("Leave applied successfully!\n");
    }

    public void viewMyLeaves() throws IOException {
        Scanner sc = new Scanner(System.in);
        File file = new File("leaveRequests.txt");
        if (!file.exists()) {
            System.out.println("No leave records found.");
            return;
        }

        boolean found = false;
        BufferedReader br = new BufferedReader(new FileReader(file));
        System.out.print("Enter your password to view leave status: ");
        String enteredPassword = sc.nextLine();

        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 2 && parts[0].equals(this.username) && parts[1].equals(enteredPassword)) {
                found = true;
                break;
            }
        }
        br.close();

        if (!found) {
            System.out.println("Incorrect password or no matching leave record.\n");
            return;
        }

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        } else {
            System.out.println("Cannot open file automatically. Please open 'leaveRequests.txt' manually.");
        }
    }
}

class Admin extends User {
    public Admin(String username) {
        super(username, "ADMIN1234");
    }

    public void reviewLeaves() throws IOException {
        File file = new File("leaveRequests.txt");
        if (!file.exists()) {
            System.out.println("No leave records found.");
            return;
        }

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(file);
                System.out.println("Please review and edit the leaveRequests.txt file. Press ENTER when done...");
                Scanner sc = new Scanner(System.in);
                sc.nextLine();
                System.out.println("Review completed.\n");
            } catch (IOException e) {
                System.out.println("Error opening file.");
            }
        } else {
            System.out.println("Desktop not supported. Please open 'leaveRequests.txt' manually and press ENTER when done...");
            Scanner sc = new Scanner(System.in);
            sc.nextLine();
        }
    }
}

public class Main {
    static final String USER_FILE = "users.txt";
    static final String ADMIN_PASSWORD = "ADMIN1234";

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to Leave Management System\n");

        while (true) {
            System.out.println("Select user type:");
            System.out.println("1. Admin");
            System.out.println("2. Employee");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            String userType = sc.nextLine();

            switch (userType) {
                case "1": // Admin
                    System.out.print("Are you an existing admin? (yes/no): ");
                    String existingAdmin = sc.nextLine().trim().toLowerCase();
                    String adminUsername = "";

                    if (existingAdmin.equals("yes")) {
                        showUsers("ADMIN");
                        System.out.print("Enter admin username: ");
                        adminUsername = sc.nextLine();
                        System.out.print("Enter admin password: ");
                        String adminPass = sc.nextLine();

                        if (!adminPass.equals(ADMIN_PASSWORD)) {
                            System.out.println("Invalid admin password.\n");
                            break;
                        }
                    } else {
                        System.out.print("Create new admin username: ");
                        adminUsername = sc.nextLine();
                        saveUser("ADMIN", adminUsername, "");
                        System.out.println("Admin registered! Use password 'ADMIN1234' to login next time.\n");
                    }

                    Admin admin = new Admin(adminUsername);
                    while (true) {
                        System.out.println("\nAdmin Panel");
                        System.out.println("1. Review Leave Requests");
                        System.out.println("2. Logout");
                        System.out.print("Enter choice: ");
                        String choice = sc.nextLine();
                        if (choice.equals("1")) admin.reviewLeaves();
                        else break;
                    }
                    break;

                case "2": // Employee
                    System.out.print("Are you an existing employee? (yes/no): ");
                    String existingEmp = sc.nextLine().trim().toLowerCase();
                    String empUsername = "", empPassword = "";

                    if (existingEmp.equals("yes")) {
                        showUsers("EMPLOYEE");
                        System.out.print("Enter employee username: ");
                        empUsername = sc.nextLine();
                        System.out.print("Enter password: ");
                        empPassword = sc.nextLine();

                        if (!validateUser("EMPLOYEE", empUsername, empPassword)) {
                            System.out.println("Invalid username or password.\n");
                            break;
                        }
                    } else {
                        System.out.print("Create new employee username: ");
                        empUsername = sc.nextLine();
                        System.out.print("Create password: ");
                        empPassword = sc.nextLine();
                        saveUser("EMPLOYEE", empUsername, empPassword);
                        System.out.println("Employee registered successfully!\n");
                    }

                    Employee emp = new Employee(empUsername, empPassword);
                    while (true) {
                        System.out.println("\nEmployee Panel");
                        System.out.println("1. Apply for Leave");
                        System.out.println("2. View Leave Status");
                        System.out.println("3. Logout");
                        System.out.print("Enter choice: ");
                        String choice = sc.nextLine();
                        if (choice.equals("1")) emp.applyLeave();
                        else if (choice.equals("2")) emp.viewMyLeaves();
                        else break;
                    }
                    break;

                case "3":
                    System.out.println("Exiting system. Goodbye!");
                    return;

                default:
                    System.out.println("Invalid choice. Try again.\n");
            }
        }
    }

    // Save new user
    public static void saveUser(String role, String username, String password) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true));
        bw.write(role + "," + username + "," + password + "\n");
        bw.close();
    }

    // Display usernames by role
    public static void showUsers(String role) throws IOException {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            System.out.println("No users found yet.\n");
            return;
        }

        System.out.println("List of " + role + "s:");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts[0].equals(role)) {
                System.out.println("- " + parts[1]);
            }
        }
        br.close();
    }

    // Validate username-password
    public static boolean validateUser(String role, String username, String password) throws IOException {
        File file = new File(USER_FILE);
        if (!file.exists()) return false;

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 3 && parts[0].equals(role) && parts[1].equals(username) && parts[2].equals(password)) {
                br.close();
                return true;
            }
        }
        br.close();
        return false;
    }
}
