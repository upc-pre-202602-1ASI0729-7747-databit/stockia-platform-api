package com.databit.stockia.platform.shared.application.transaction;

import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Marks the current transaction for rollback when a command returns an expected failure after
 * having written something (e.g. a sale whose stock ran out inside the same transaction).
 */
public final class TransactionRollback {
    private TransactionRollback() {
    }

    public static void markRollbackOnly() {
        if (TransactionSynchronizationManager.isActualTransactionActive())
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
}