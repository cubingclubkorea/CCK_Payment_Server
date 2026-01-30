package com.cubingclub.payment.Registration.Entity;

public enum PaymentStatus {
    PENDING, PAID, CANCELLED, REFUNDED;

    public boolean canMoveTo(PaymentStatus target) {
        return switch (this) {
            case PENDING -> target == PAID || target == CANCELLED;
            case PAID -> target == CANCELLED;
            case CANCELLED -> target == REFUNDED;
            case REFUNDED -> false;
        };
    };
}
