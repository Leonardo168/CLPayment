package com.CLPayment.enums;

public enum TransactionType {
    CREATE_TOURNAMENT(false),
    JOIN_TOURNAMENT(false),
    BUY_CHIPS(true),
    SELL_CHIPS(false),
    WIN_TOURNAMENT(true),
    REFUND_TOURNAMENT(true);

    public boolean plus;

    private TransactionType(boolean plus) {
        this.plus = plus;
    }
}