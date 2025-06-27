package main.java;

import main.java.frontpage.DishTrackerApp;

/**
 * @author Justin Myers
 * This acts as a fake main to call the real main. Needed for creating an application.
 */
public class FakeMain {
    public static void main(String[] args) {
        DishTrackerApp.main((args));
    }
}
