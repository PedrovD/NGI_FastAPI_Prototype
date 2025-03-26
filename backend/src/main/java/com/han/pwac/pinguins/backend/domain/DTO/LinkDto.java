package com.han.pwac.pinguins.backend.domain.DTO;

import java.sql.Timestamp;

public class LinkDto {
    private String link;
    private Timestamp timestamp;
    public LinkDto(String link, Timestamp timestamp) {
        this.link = link;
        this.timestamp = timestamp;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
