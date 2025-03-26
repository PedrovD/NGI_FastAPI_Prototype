package com.han.pwac.pinguins.backend.domain;

import java.sql.Timestamp;

public class InviteKey {

    private String key;
    private Integer businessId = null;
    private Timestamp dateTime;

    public InviteKey() {}
    public InviteKey(String key, Integer businessId, Timestamp dateTime) {
        this.key = key;
        this.businessId = businessId;
        this.dateTime = dateTime;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

}
