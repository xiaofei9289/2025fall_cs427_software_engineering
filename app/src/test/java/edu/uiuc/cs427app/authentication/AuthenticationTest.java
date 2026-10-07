package edu.uiuc.cs427app;

import org.junit.Before;
import org.junit.Test;

import edu.uiuc.cs427app.data.entity.User;

import static org.junit.Assert.*;
import static edu.uiuc.cs427app.viewmodel.LoginViewModel.validateCredentials;

/**
 * Comprehensive tests for authentication functionality.
 * Tests User entity, validation logic, and integration flows.
 */
public class AuthenticationTest {

    @Before
    public void setUp() {
    }

    // ==================== User Entity Tests ====================

    @Test
    public void testUserCreation() {
        User user = new User("NewUser", "mypassword", "dark");
        
        assertEquals("newuser", user.getUsername());
        assertNotNull(user.getPasswordHash());
        assertNotNull(user.getCreatedAt());
        assertEquals("dark", user.getThemePreference());
        assertTrue(user.validatePassword("mypassword"));
        assertFalse(user.validatePassword("wrongpassword"));
    }

    @Test
    public void testPasswordHashing() {
        User user1 = new User("user1", "password123", "light");
        User user2 = new User("user2", "password123", "light");
        
        assertNotEquals(user1.getPasswordHash(), user2.getPasswordHash());
        
        assertTrue(user1.validatePassword("password123"));
        assertTrue(user2.validatePassword("password123"));
        
        assertFalse(user1.validatePassword("wrong"));
        assertFalse(user2.validatePassword("wrong"));
    }

    @Test
    public void testUserThemeManagement() {
        User user = new User("testuser", "password", "blue");
        
        user.setTheme("dark");
        assertEquals("dark", user.getThemePreference());
        
        user.setTheme("blue");
        assertEquals("blue", user.getThemePreference());
        
        user.setTheme("invalid");
        assertEquals("blue", user.getThemePreference());
        
        user.setTheme(null);
        assertEquals("blue", user.getThemePreference());
    }

    // ==================== Validation Logic Tests ====================

    @Test
    public void testValidateCredentials_ValidInputs() {
        assertTrue(validateCredentials("testuser", "password123"));
        assertTrue(validateCredentials("user123", "password"));
        assertTrue(validateCredentials("abc", "password"));
        assertTrue(validateCredentials("testuser123", "password123"));
    }

    @Test
    public void testValidateCredentials_InvalidUsername() {
        assertFalse(validateCredentials("ab", "password123"));
        
        assertFalse(validateCredentials("thisusernameistoolong", "password123"));
        
        assertFalse(validateCredentials("test@user", "password123"));
        assertFalse(validateCredentials("test-user", "password123"));
        assertFalse(validateCredentials("test user", "password123"));
        
        assertFalse(validateCredentials("", "password123"));
        assertFalse(validateCredentials(null, "password123"));
    }

    @Test
    public void testValidateCredentials_InvalidPassword() {
        assertFalse(validateCredentials("testuser", "12345"));
        
        assertFalse(validateCredentials("testuser", ""));
        assertFalse(validateCredentials("testuser", null));
    }

    @Test
    public void testValidateCredentials_EdgeCases() {
        assertTrue(validateCredentials("abc", "123456"));
        
        assertTrue(validateCredentials("abcdefghijklmnopqrst", "123456"));
        
        assertFalse(validateCredentials("ab", "123456"));
        
        assertFalse(validateCredentials("abc", "12345"));
    }

    // ==================== Error Handling Tests ====================

    @Test
    public void testNullHandling() {
        assertFalse(validateCredentials(null, null));
        assertFalse(validateCredentials("test", null));
        assertFalse(validateCredentials(null, "test"));
    }

    @Test
    public void testEmptyStringHandling() {
        assertFalse(validateCredentials("", ""));
        assertFalse(validateCredentials("test", ""));
        assertFalse(validateCredentials("", "test"));
    }

    @Test
    public void testWhitespaceHandling() {
        assertTrue(validateCredentials("  testuser  ", "  password123  "));
        assertFalse(validateCredentials("  ab  ", "  password123  "));
    }

    // ==================== Security Tests ====================

    @Test
    public void testPasswordSecurity() {
        User user = new User("testuser", "password123", "light");
        
        assertNotEquals("password123", user.getPasswordHash());
        
        assertTrue(user.getPasswordHash().length() > 50);
        
        assertTrue(user.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    public void testUsernameCaseInsensitivity() {
        User user1 = new User("TestUser", "password123", "light");
        User user2 = new User("testuser", "password456", "light");
        User user3 = new User("TESTUSER", "password789", "light");
        
        assertEquals("testuser", user1.getUsername());
        assertEquals("testuser", user2.getUsername());
        assertEquals("testuser", user3.getUsername());
        
        assertTrue(user1.validatePassword("password123"));
        assertTrue(user2.validatePassword("password456"));
        assertTrue(user3.validatePassword("password789"));
    }

    // ==================== Integration Tests ====================

    @Test
    public void testCompleteSignupFlow() {
        String username = "newuser";
        String password = "password123";
        
        User newUser = new User(username, password, "light");
        assertNotNull("User should be created", newUser);
        assertEquals(username, newUser.getUsername());
        assertTrue("Password should be valid", newUser.validatePassword(password));
        
        assertTrue("Credentials should be valid", validateCredentials(username, password));
    }

    @Test
    public void testCompleteLoginFlow() {
        String username = "testuser";
        String password = "password123";
        
        User user = new User(username, password, "light");
        assertNotNull("User should be created", user);
        
        assertTrue("Password should be valid", user.validatePassword(password));
        assertFalse("Wrong password should fail", user.validatePassword("wrongpassword"));
    }

    @Test
    public void testDuplicateUsernameHandling() {
        String username = "duplicateuser";
        String password1 = "password123";
        String password2 = "password456";
        
        User user1 = new User(username, password1, "light");
        assertNotNull("First user should be created", user1);
        
        User user2 = new User(username, password2, "dark");
        assertNotNull("Second user should be created", user2);
        
        assertEquals("Both users should have same username", user1.getUsername(), user2.getUsername());
        assertNotEquals("Users should have different password hashes", 
            user1.getPasswordHash(), user2.getPasswordHash());
        
        assertTrue("First user password should work", user1.validatePassword(password1));
        assertTrue("Second user password should work", user2.validatePassword(password2));
        
        assertFalse("First user should not validate second password", user1.validatePassword(password2));
        assertFalse("Second user should not validate first password", user2.validatePassword(password1));
    }

    @Test
    public void testPasswordComplexity() {
        String username = "passwordtest";
        
        String[] validPasswords = {"123456", "password", "abcdef", "123456789"};
        String[] invalidPasswords = {"12345", "abc", "", "123"};
        
        for (String password : validPasswords) {
            User user = new User(username + password.length(), password, "light");
            assertNotNull("User with password '" + password + "' should be created", user);
            assertTrue("Password '" + password + "' should be valid", user.validatePassword(password));
        }
        
        for (String password : invalidPasswords) {
            User user = new User(username + password.length(), password, "light");
            assertNotNull("User with password '" + password + "' should be created", user);
        }
    }

    @Test
    public void testUserThemePersistence() {
        String username = "themeuser";
        String password = "password123";
        
        User user = new User(username, password, "light");
        assertNotNull("User should be created", user);
        
        user.setTheme("dark");
        assertEquals("Theme should be set to dark", "dark", user.getThemePreference());
        
        user.setTheme("blue");
        assertEquals("Theme should be set to blue", "blue", user.getThemePreference());
        
        user.setTheme("invalid");
        assertEquals("Theme should remain blue", "blue", user.getThemePreference());
    }

    @Test
    public void testUserCreationWithThemeDescription() {
        User user1 = new User("user1", "password123", "dark");
        assertEquals("dark", user1.getThemePreference());
        
        User user2 = new User("user2", "password456", "blue");
        assertEquals("blue", user2.getThemePreference());
        
        User user3 = new User("user3", "password789", "light");
        assertEquals("light", user3.getThemePreference());
        
        assertTrue(user1.validatePassword("password123"));
        assertTrue(user2.validatePassword("password456"));
        assertTrue(user3.validatePassword("password789"));
    }
}