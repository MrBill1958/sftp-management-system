package com.nearstar.sftpmanager.model.enums;

public enum Status {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    TESTING("Testing"),
    ERROR("Error"),
    DISABLED("Disabled");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}