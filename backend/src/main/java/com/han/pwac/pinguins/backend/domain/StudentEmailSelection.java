package com.han.pwac.pinguins.backend.domain;

public enum StudentEmailSelection {
    REGISTERED,
    ACCEPTED,
    REJECTED;

    public int getFlagValue() {
        return 1 << ordinal();
    }
}
