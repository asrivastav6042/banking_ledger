package com.bank.banking_core.constants;

public final class AppConstants {
    private AppConstants() {}

    public static final String TOPIC_BANKING_TRANSACTIONS = "banking.transactions";
    public static final String LOCK_ACCOUNT_PREFIX = "lock:account:";
    public static final long LOCK_WAIT_TIME_SECONDS = 5;
    public static final long LOCK_LEASE_TIME_SECONDS = 10;
}
