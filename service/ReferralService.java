package com.hms.service;

import com.hms.model.Referral;
import com.hms.util.CSVHandler;
import java.util.ArrayList;
import java.util.List;

public class ReferralService {
    // 1. Static Instance
    private static ReferralService instance;
    private List<Referral> referrals;

    // 2. Private Constructor (prevent direct instantiation)
    private ReferralService() {
        this.referrals = new ArrayList<>();
    }

    // 3. Public Static Accessor (Global Access Point)
    public static synchronized ReferralService getInstance() {
        if (instance == null) {
            instance = new ReferralService();
        }
        return instance;
    }

    public void loadData(List<String[]> data) {
        referrals.clear();
        for (String[] row : data) {
            if (row.length >= 10) {
                referrals.add(new Referral(row));
            }
        }
    }

    public void createReferral(Referral r) {
        referrals.add(r);
        CSVHandler.appendToCSV("data/referrals.csv", r.toCSV());
        System.out.println("Singleton: Referral created " + r.getReferralId());
    }
    
    public void deleteReferral(String id) {
        referrals.removeIf(r -> r.getReferralId().equals(id));
        CSVHandler.deleteRecord("data/referrals.csv", 0, id);
    }
    
    public void updateReferral(Referral r) {
        deleteReferral(r.getReferralId());
        createReferral(r);
    }

    public List<Referral> getAllReferrals() { return referrals; }
}