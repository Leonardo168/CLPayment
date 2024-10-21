package webClient.mercadoPago.teste.enums;

public enum TransactionType {
    CREATE_TOURNAMENT(false),
    JOIN_TOURNAMENT(false),
    BUY_TICKETS(true),
    SELL_TICKETS(false),
    WIN_TOURNAMENT(true),
    REFUND_TOURNAMENT(true);

    public boolean plus;

    private TransactionType(boolean plus) {
        this.plus = plus;
    }
}