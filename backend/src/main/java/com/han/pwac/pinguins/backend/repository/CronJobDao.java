package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;

@Repository
public class CronJobDao implements ICronJobDao {
    private final JdbcTemplate template;

    public CronJobDao(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public boolean create(int emailsSent) {
        return template.update("INSERT INTO CronJobRuns (emailssent) VALUES (?)", emailsSent) != 0;
    }

    @Override
    public Timestamp getPreviousRunDate() {
        Timestamp date = template.queryForObject("SELECT MAX(runDate) FROM CronJobRuns", Timestamp.class);
        if (date == null) {
            return new Timestamp(0);
        }
        return date;
    }
}
