package com.hms.service;

import com.hms.model.Referral;

public class ExternalHospitalService {
    // mock implementation of external API call
    public void sendReferralData(Referral r) {
        System.out.println("[EXTERNAL HOSPITAL SYSTEM] Sending Referral ID " + r.getReferralId() + 
                           " to Hospital Specialist " + r.getReferralId());
        System.out.println("[EXTERNAL HOSPITAL SYSTEM] Uploading clinical summary: " + r.getReferralId());
    }
}