package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.exceptions.DatabaseInsertException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.IProjectDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class ProjectDao implements IProjectDao {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ProjectDto> mapper = (resultSet, rowNum) -> new ProjectDto(
            resultSet.getInt("projectId"),
            resultSet.getString("title"),
            resultSet.getString("description"),
            new FileDto(Optional.ofNullable(resultSet.getString("photo")))
    );

    @Autowired
    public ProjectDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Boolean> checkIfProjectExists(int projectId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Projects WHERE projectId = ?", Integer.class, projectId) > 0);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> checkIfProjectNameIsTaken(String title, int supervisorId) {
        String sql = """            
                SELECT COUNT(*)            
                FROM Projects            
                INNER JOIN Supervisors ON Projects.userId = Supervisors.userId            
                INNER JOIN Business ON Supervisors.businessId = Business.businessId            
                WHERE Projects.title = ? AND Supervisors.userId = ?            
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class, title, supervisorId) > 0);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<ProjectDto> getAll() {
        return jdbcTemplate.query("SELECT projectId, title, description, imagePath AS photo FROM Projects", mapper);
    }

    @Override
    public Optional<ProjectDto> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT projectId, title, description, imagePath AS photo FROM Projects WHERE projectId = ?", mapper, id));
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Project id niet gevonden");
        }
    }

    @Override
    public boolean add(ProjectDto item) {
        throw new NotImplementedException();
    }

    @Override
    public boolean delete(Integer id) {
        throw new NotImplementedException();
    }

    @Override
    public boolean update(Integer id, ProjectDto item) {
        throw new NotImplementedException();
    }

    @Override
    public Integer getLastInsertedId() {
        try {
            return jdbcTemplate.queryForObject("SELECT MAX(projectId) FROM Projects", Integer.class);
        } catch (EmptyResultDataAccessException ignored) {
            return 0;
        }
    }

    @Override
    public Collection<ProjectDto> getAllByBusinessId(int businessId) {
        return jdbcTemplate.query("""
                SELECT projectId, P.title, P.description, P.imagePath AS photo 
                FROM Projects AS P
                INNER JOIN Supervisors AS S
                    ON S.userId = P.userId
                INNER JOIN Business AS B 
                    ON B.businessId = S.businessId 
                WHERE B.businessId = ?
                """, mapper, businessId);
    }



    public int storeProject(CreateProjectDto project, int supervisorId) {
        int updatedRows = jdbcTemplate.update(
                "INSERT INTO Projects (userId, title, description, imagePath) VALUES (?, ?, ?, ?)",
                supervisorId,
                project.getTitle().orElse(null),
                project.getDescription().orElse(null),
                project.getImagePath().path().orElse(null)
        );

        if (updatedRows == 0) {
            throw new DatabaseInsertException("Aanmaken van het project is mislukt, probeer later opnieuw");
        }

        Integer lastInsertedId = getLastInsertedId();
        if (lastInsertedId != 0) {
            return lastInsertedId;
        }
        throw new DatabaseInsertException("Aanmaken van het project is mislukt, probeer later opnieuw");
    }

    public List<ProjectDto> getBySupervisorId(int userId) {
        String sql = "SELECT projectId, P.title, P.description, P.imagePath AS photo FROM Projects AS P WHERE userId = ?";
        return jdbcTemplate.query(sql, mapper, userId);
    }

    public List<ProjectDto> getByBusinessId(int businessId) {
        return jdbcTemplate.query("""
                SELECT projectId, P.title, P.description, P.imagePath AS photo
                FROM Projects AS P
                INNER JOIN supervisors
                    ON P.userid = supervisors.userid
                WHERE supervisors.businessid = ?
                """, mapper, businessId);
    }
}