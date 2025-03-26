package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.BusinessDto;
import com.han.pwac.pinguins.backend.domain.DTO.FileDto;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.IBusinessDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class BusinessDao implements IBusinessDao {
    private final JdbcTemplate jdbcTemplate;
    private final String DEFAULT_PROFILE_PICTURE = "/han_logo.png";

    protected static final RowMapper<BusinessDto> mapper = (resultSet, rowNum) -> new BusinessDto(
            resultSet.getInt("businessId"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            new FileDto(Optional.ofNullable(resultSet.getString("photo"))),
            resultSet.getString("location")
    );

    @Autowired
    public BusinessDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<BusinessDto> getAll() {
        return jdbcTemplate.query(
                "SELECT businessId, name, description, imagePath as photo, location FROM Business",
                mapper
        );
    }

    @Override
    public Optional<BusinessDto> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT businessId, name, description, imagePath as photo, location FROM Business WHERE businessId = ?",
                    mapper,
                    id
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean add(BusinessDto item) {
        return jdbcTemplate.update("""
                        INSERT INTO Business (name, description, imagePath, location)
                        VALUES (?, '', ?, '')
                        """,
                item.name(), DEFAULT_PROFILE_PICTURE) != 0;
    }

    @Override
    public boolean delete(Integer id) {
        throw new NotImplementedException();
    }

    @Override
    public boolean update(Integer id, BusinessDto item) {
        return jdbcTemplate.update("""
                        UPDATE Business
                        SET name = ?, description = ?, imagePath = ?, location = ?
                        WHERE businessId = ?
                        """,
                item.name(), item.description(), item.photo().path().orElse(null), item.location(), id) != 0;
    }

    @Override
    public Integer getLastInsertedId() {
        return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
    }

    @Override
    public Optional<BusinessDto> getByProjectId(int projectId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    SELECT B.businessId, B.name, B.description, B.imagePath as photo, location
                    FROM Business AS B
                    INNER JOIN Supervisors AS S 
                        ON B.businessId = S.businessId
                    INNER JOIN Projects AS P 
                        ON P.userId = S.userId
                    WHERE P.projectId = ?
                    """, mapper, projectId));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public boolean addUserToBusiness(Integer businessId, Integer userId) {
        jdbcTemplate.update("DELETE FROM Students where userId = ?", userId);
        return jdbcTemplate.update("INSERT INTO Supervisors (businessId, userId) VALUES (?, ?)", businessId, userId) > 0;
    }
}
