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
	switch (status) {
	case "EM_PROCESSAMENTO":
            return in_process;
        case "REALIZADO":
            return approved;
        case "NAO_REALIZADO":
            return rejected;
	default:
	    try {
                return TransactionStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido: " + status);
            }
	}
    }
}
