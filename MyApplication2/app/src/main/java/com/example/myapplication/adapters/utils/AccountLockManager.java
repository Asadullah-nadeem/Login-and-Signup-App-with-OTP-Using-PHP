package com.example.myapplication.adapters.utils;

import java.util.Calendar;
import java.util.Date;

public class AccountLockManager {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    public static void handleFailedLogin(SharedPreferencesHelper prefs) {
        int attempts = prefs.getLoginAttempts() + 1;
        prefs.updateLoginAttempts(attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, LOCK_DURATION_MINUTES);
            prefs.setAccountLock(calendar.getTime());
        }
    }

    public static boolean isAccountLocked(SharedPreferencesHelper prefs) {
        Date lockTime = prefs.getAccountLockTime();
        return lockTime != null && new Date().before(lockTime);
    }
}