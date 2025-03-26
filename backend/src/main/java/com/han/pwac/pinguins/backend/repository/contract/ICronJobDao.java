package com.han.pwac.pinguins.backend.repository.contract;

import java.sql.Timestamp;

public interface ICronJobDao {
    boolean create(int emailsSent);

    Timestamp getPreviousRunDate();
}
