package com.hms.service;

import com.hms.model.Staff;
import java.util.List;

public class AuthenticationService {
    public boolean login(String userId, String password, List<Staff> staffList) {
        // simplified login for demo: skipping password check
        // treating existence of Staff ID as successful auth
        for (Staff s : staffList) {
            if (s.getStaffId().trim().equalsIgnoreCase(userId.trim())) {
                // TODO: integrate real password hashing later
                System.out.println("User " + s.getName() + " logged in via Auth Service.");
                return true;
            }
        }
        return false;
    }

    public void logout(String username) {
        System.out.println("User " + username + " logged out.");
    }
}