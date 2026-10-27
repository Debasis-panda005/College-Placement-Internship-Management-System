package com.college.backend;

import com.college.backend.dao.UserDAO;
import com.college.backend.entity.User;
import com.college.backend.util.PasswordHashService;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utility to safely initialize a local Placement Coordinator account
 * using the project's existing UserDAO and PasswordHashService.
 *
 * This utility:
 * 1. Supports standard terminal input via System.in, ensuring compatibility
 *    with IDEs and integrated terminals.
 * 2. Prompts for password confirmation and rejects mismatches.
 * 3. Enforces pre-existence checks to prevent overwriting or duplicate records.
 * 4. Generates an authentic BCrypt hash via PasswordHashService (work factor 10).
 * 5. Inserts strictly into the 'users' table (does NOT touch the 'students' table).
 * 6. Verifies authentication against the database post-creation.
 * 7. Never prints, logs, or stores plaintext passwords or hashes.
 */
public class CreateCoordinatorAccountUtil {

    private static final String COORDINATOR_NAME = "Placement Coordinator";
    private static final String COORDINATOR_EMAIL = "coordinator@college.edu";
    private static final String COORDINATOR_ROLE = User.ROLE_COORDINATOR;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" College Placement - Coordinator Account Creator ");
        System.out.println("=================================================");
        System.out.println("Target account:");
        System.out.println("  Name : " + COORDINATOR_NAME);
        System.out.println("  Email: " + COORDINATOR_EMAIL);
        System.out.println("  Role : " + COORDINATOR_ROLE);
        System.out.println("-------------------------------------------------");

        UserDAO userDAO = new UserDAO();

        // 1. Check if coordinator account already exists
        try {
            if (userDAO.existsByEmail(COORDINATOR_EMAIL)) {
                System.out.println("ABORT: User with email '" + COORDINATOR_EMAIL + "' already exists in the database.");
                System.out.println("No changes were made to existing records.");
                return;
            }
        } catch (Exception e) {
            System.err.println("ERROR: Database connectivity failure while checking existing accounts: " + e.getMessage());
            return;
        }

        try {
            // 2. Read password interactively from standard input
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter password for " + COORDINATOR_EMAIL + ": ");
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

            // 4. Hash password with existing PasswordHashService (BCrypt)
            String passwordHash = PasswordHashService.hashPassword(plainPassword);

            // 5. Construct Coordinator User entity
            User coordinatorUser = new User(COORDINATOR_NAME, COORDINATOR_EMAIL, passwordHash, COORDINATOR_ROLE);

            // 6. Insert into database using existing UserDAO (users table only)
            boolean created = userDAO.createUser(coordinatorUser);
            if (!created) {
                System.err.println("ERROR: Failed to insert coordinator user into database.");
                return;
            }

            System.out.println("SUCCESS: Coordinator account record created with ID: " + coordinatorUser.getId());

            // 7. Verify login using existing authentication code
            User retrievedUser = userDAO.findByEmail(COORDINATOR_EMAIL);
            if (retrievedUser == null) {
                System.err.println("WARNING: Verification failed - could not retrieve inserted user.");
                return;
            }

            boolean authValid = PasswordHashService.verifyPassword(plainPassword, retrievedUser.getPasswordHash());
            if (authValid && COORDINATOR_ROLE.equals(retrievedUser.getRole())) {
                System.out.println("SUCCESS: Verified authentication and role '" + retrievedUser.getRole() + "' with existing BCrypt service!");
                System.out.println("-------------------------------------------------");
                System.out.println("You can now log in at: http://localhost:8080/backend/login");
                System.out.println("  Email: " + COORDINATOR_EMAIL);
                System.out.println("=================================================");
            } else {
                System.err.println("WARNING: Verification failed during password authentication check.");
            }

        } catch (Exception e) {
            System.err.println("ERROR: An error occurred while creating the account: " + e.getMessage());
        }
    }
}
