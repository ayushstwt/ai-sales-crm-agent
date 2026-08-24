package com.ayshriv.salescrm.lead.entity;

public enum LeadStatus {
    NEW,
    CONTACTED,
    QUALIFIED,
    CONVERTED,
    LOST;

    public boolean canTransitionTo(LeadStatus newStatus) {
        if (this == newStatus) {
            return true;
        }
        return switch (this) {
            case NEW -> newStatus == CONTACTED || newStatus == QUALIFIED || newStatus == LOST;
            case CONTACTED -> newStatus == QUALIFIED || newStatus == LOST;
            case QUALIFIED -> newStatus == CONVERTED || newStatus == LOST;
            case CONVERTED, LOST -> false; // Terminal states
        };
    }
}
