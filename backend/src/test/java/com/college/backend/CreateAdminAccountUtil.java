package com.college.backend;

import com.college.backend.dao.UserDAO;
import com.college.backend.entity.User;
import com.college.backend.util.PasswordHashService;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Temporary utility to safely initialize the local Admin account
 * using the project's existing UserDAO and PasswordHashService.
 *
 * This utility:
 * 1. Supports standard terminal input via System.in, ensuring compatibility
 *    with IDEs and integrated terminals.
 * 2. Asks for password confirmation and rejects mismatches.
 * 3. Enforces pre-existence checks to never overwrite or duplicate records.
 * 4. Generates an authentic BCrypt hash via PasswordHashService.
 * 5. Verifies authentication against the database post-creation.
 * 6. Never prints or stores plaintext passwords or hashes.
 */
public class CreateAdminAccountUtil {

    private static final String ADMIN_NAME = "System Administrator";
    private static final String ADMIN_EMAIL = "admin@college.edu";
    private static final String ADMIN_ROLE = User.ROLE_ADMIN;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" College Placement - Local Admin Account Creator ");
        System.out.println("=================================================");
        System.out.println("Target account:");
        System.out.println("  Name : " + ADMIN_NAME);
        System.out.println("  Email: " + ADMIN_EMAIL);
        System.out.println("  Role : " + ADMIN_ROLE);
        System.out.println("-------------------------------------------------");

        UserDAO userDAO = new UserDAO();

        // 1. Check if admin account already exists
        try {
            if (userDAO.existsByEmail(ADMIN_EMAIL)) {
                System.out.println("ABORT: User with email '" + ADMIN_EMAIL + "' already exists in the database.");
                System.out.println("No changes were made to existing records.");
                return;
            }
        } catch (Exception e) {
            System.err.println("ERROR: Database connectivity failure while checking existing accounts: " + e.getMessage());
            return;
        }

        try {
            // 2. Read password interactively from standard input (compatible with integrated terminals)
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter password for " + ADMIN_EMAIL + ": ");
            System.out.flush();
            String plainPassword = reader.readLine();
            if (plainPassword == null || plainPassword.trim().isEmpty()) {
                System.err.println("ERROR: Password cannot be empty.");
                return;
            }

            // 3. Confirm password
            System.out.print("Confirm password: ");
            System.out.flush();
            String confirmPassword = reader.readLine();
            if (confirmPassword == null || !plainPassword.equals(confirmPassword)) {
                System.err.println("ERROR: Passwords do not match. Aborting.");
                return;
            }

            // 4. Hash password with existing PasswordHashService
            String passwordHash = PasswordHashService.hashPassword(plainPassword);

            // 5. Construct Admin User entity
            User adminUser = new User(ADMIN_NAME, ADMIN_EMAIL, passwordHash, ADMIN_ROLE);

            // 6. Insert into database using existing UserDAO
            boolean created = userDAO.createUser(adminUser);
            if (!created) {
                System.err.println("ERROR: Failed to insert admin user into database.");
                return;
            }

            System.out.println("SUCCESS: Admin account record created with ID: " + adminUser.getId());

            // 7. Verify login using existing authentication code
            User retrievedUser = userDAO.findByEmail(ADMIN_EMAIL);
            if (retrievedUser == null) {
                System.err.println("WARNING: Verification failed - could not retrieve inserted user.");
                return;
            }

            boolean authValid = PasswordHashService.verifyPassword(plainPassword, retrievedUser.getPasswordHash());
            if (authValid && ADMIN_ROLE.equals(retrievedUser.getRole())) {
                System.out.println("SUCCESS: Verified authentication and role '" + retrievedUser.getRole() + "' with existing BCrypt service!");
                System.out.println("-------------------------------------------------");
                System.out.println("You can now log in at: http://localhost:8080/backend/login");
                System.out.println("  Email: " + ADMIN_EMAIL);
                System.out.println("=================================================");
            } else {
                System.err.println("WARNING: Verification failed during password authentication check.");
            }

        } catch (Exception e) {
            System.err.println("ERROR: An error occurred while creating the account: " + e.getMessage());
        }
    }
}
