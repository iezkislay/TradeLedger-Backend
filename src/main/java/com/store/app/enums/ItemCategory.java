package com.store.app.enums;

public enum ItemCategory {
    CPVC("CPVC"),
    UPVC("UPVC"),
    SWR("SWR"),
    GI("GI"),
    BORING("BR"),
    CP_ITEMS("CP"),
    OTHERS("OTR");

    private final String prefix;

    ItemCategory(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
