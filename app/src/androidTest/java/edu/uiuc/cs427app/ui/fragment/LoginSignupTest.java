package edu.uiuc.cs427app.ui.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import edu.uiuc.cs427app.MainActivity;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.data.AppDatabase;
import edu.uiuc.cs427app.data.entity.User;
import edu.uiuc.cs427app.data.dao.UserDao;

import org.hamcrest.CoreMatchers;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LoginSignupTest {

        private Context context;
        private AppDatabase db;
        private UserDao userDao;

        @Before
        public void setUp() {
                context = ApplicationProvider.getApplicationContext();

                // Clear SharedPreferences (last logged-in user)
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                .edit().clear().commit();

                // Use test DB and clear it
                db = AppDatabase.getInstance(context);
                userDao = db.userDao();
                userDao.deleteAll();
        }

        @After
        public void tearDown() {
                // Clear DB again
                userDao.deleteAll();
        }

        @Test
        public void testLoginFailure() {
                // Launch activity after setUp() clears SharedPreferences
                ActivityScenario.launch(MainActivity.class);

                onView(withId(R.id.etUsername)).perform(typeText("wronguser"), closeSoftKeyboard());
                onView(withId(R.id.etPassword)).perform(typeText("wrongpass"), closeSoftKeyboard());
                onView(withId(R.id.btnLogin)).perform(click());

                // Wait briefly for UI update
                try {
                        Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // Check that login fields are still visible (login failed)
                onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
                onView(withId(R.id.etPassword)).check(matches(isDisplayed()));

                try {
                        Thread.sleep(2500);
                } catch (InterruptedException e) {
                }
        }

        @Test
        public void testLoginSuccess() {
                // Insert user into DB
                User user = new User("testuser", "testpass", "");
                userDao.insert(user);

                // Launch activity after setUp() clears SharedPreferences
                ActivityScenario.launch(MainActivity.class);

                onView(withId(R.id.etUsername)).perform(typeText("testuser"), closeSoftKeyboard());
                onView(withId(R.id.etPassword)).perform(typeText("testpass"), closeSoftKeyboard());
                onView(withId(R.id.btnLogin)).perform(click());

                // Wait for CityListFragment to load
                try {
                        Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // Check that CityListFragment's welcome text is displayed
                onView(withId(R.id.tvWelcome)).check(matches(withText(CoreMatchers.containsString("testuser"))));
                onView(withId(R.id.tvEmptyState)).check(matches(isDisplayed()));

                // Check that user is created in database
                User createdUser = userDao.getUserByUsername("testuser");
                assertNotNull("User should be created in database", createdUser);
                assertEquals("Username should match", "testuser", createdUser.getUsername());

                try {
                        Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
        }

        @Test
        public void testSignupFlow() {
                // Launch activity after setUp() clears SharedPreferences
                ActivityScenario.launch(MainActivity.class);

                // Wait for fragment to fully load
                try {
                        Thread.sleep(2000);
                } catch (InterruptedException e) {
                }

                onView(withId(R.id.btnSignUp)).perform(click());

                onView(withId(R.id.etUsername)).perform(typeText("newuser"), closeSoftKeyboard());
                onView(withId(R.id.etPassword)).perform(typeText("newpass"), closeSoftKeyboard());

                onView(withId(R.id.btnSignUpConfirm)).perform(click());

                // Wait for CityListFragment to load
                try {
                        Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // Verify CityListFragment is displayed
                onView(withId(R.id.tvWelcome)).check(matches(withText(CoreMatchers.containsString("newuser"))));
                onView(withId(R.id.tvEmptyState)).check(matches(isDisplayed()));

                // Check that user is created in database
                User createdUser = userDao.getUserByUsername("newuser");
                assertNotNull("User should be created in database", createdUser);
                assertEquals("Username should match", "newuser", createdUser.getUsername());

                try {
                        Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
        }
}
