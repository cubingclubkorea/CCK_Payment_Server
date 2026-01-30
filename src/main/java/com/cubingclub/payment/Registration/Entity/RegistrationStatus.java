package com.cubingclub.payment.Registration.Entity;

public enum RegistrationStatus
{
    PENDING, CONFIRMED, CANCELLED;

    public boolean canMoveTo(RegistrationStatus target) {
        return switch (this) {
            case PENDING -> target == CONFIRMED || target == CANCELLED;
            case CONFIRMED -> target == CANCELLED;
            case CANCELLED -> false;
        };
    }
}
