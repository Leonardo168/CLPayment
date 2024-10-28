package com.CLPayment.enums;

public enum TransactionStatus {
    pending,
    approved,
    authorized,
    in_process,
    in_mediation,
    rejected,
    cancelled,
    refunded,
    charged_back;
    
    public static TransactionStatus fromString(String status) {
	return TransactionStatus.valueOf(status);
    }
}
