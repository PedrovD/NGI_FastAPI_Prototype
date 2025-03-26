package com.han.pwac.pinguins.backend.repository;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillWithDescriptionDto;
import com.han.pwac.pinguins.backend.exceptions.NotImplementedException;
import com.han.pwac.pinguins.backend.repository.contract.ISkillDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class SkillDao implements ISkillDao {
    private final JdbcTemplate jdbcTemplate;

    protected static final RowMapper<GetSkillDto> mapper = (resultSet, rowNum) -> new GetSkillDto(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getBoolean("isPending")
    );

    protected static final RowMapper<GetSkillWithDescriptionDto> skillDescriptionMapper = (resultSet, rowNum) -> new GetSkillWithDescriptionDto(
            mapper.mapRow(resultSet, rowNum),
            resultSet.getString("description")
    );

    @Autowired
    public SkillDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<GetSkillDto> getAll() {
        return jdbcTemplate.query("SELECT skillId as id, name, isPending FROM Skills", mapper);
    }

    @Override
    public Optional<GetSkillDto> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT skillId as id, name, isPending FROM Skills WHERE skillId = ?", mapper, id));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<GetSkillDto> getTaskSkills(int taskId) {
        return jdbcTemplate.query("""
                        SELECT s.skillId as id, s.name, s.isPending
                        FROM TasksSkills ts
                        INNER JOIN Skills s ON ts.skillId = s.skillId
                        WHERE ts.taskId = ?
                        """,
                mapper,
                taskId
        );
    }

    public void addTaskSkill(int taskId, int skillId) {
        String sql = "INSERT INTO TasksSkills (taskId, skillId) VALUES (?, ?)";
        jdbcTemplate.update(sql, taskId, skillId);
    }

    public boolean existsTaskSkill(Integer taskId, Integer skillId) {
        String sql = "SELECT COUNT(*) > 0 FROM tasksSkills WHERE taskId = ? AND skillId = ?";
        return jdbcTemplate.queryForObject(sql, Boolean.class, taskId, skillId);
    }

    public void removeSkillsFromTask(Integer taskId) {
        String sql = "DELETE FROM TasksSkills WHERE taskId = ?";
        jdbcTemplate.update(sql, taskId);
    }

    @Override
    public boolean add(GetSkillDto item) {
        String sql = "INSERT INTO Skills (name) VALUES (?)";
        return jdbcTemplate.update(sql, item.name()) != 0;
    }

    @Override
    public boolean delete(Integer id) {
        throw new NotImplementedException();
    }

    @Override
    public boolean update(Integer id, GetSkillDto item) {
        throw new NotImplementedException();
    }

    @Override
    public Integer getLastInsertedId() {
        try {
            return jdbcTemplate.queryForObject("SELECT MAX(skillId) FROM Skills", Integer.class);
        } catch (EmptyResultDataAccessException ignored) {
            return 0;
        }
    }

    @Override
    public List<GetSkillDto> getTopByProjectId(int projectId) {
        return jdbcTemplate.query("""
                SELECT S.skillId as id, name, isPending, COUNT(*) AS count
                FROM Skills AS S
                INNER JOIN TasksSkills AS TS
                    ON TS.skillId = S.skillId
                INNER JOIN Tasks AS T 
                    ON T.taskId = TS.taskId
                WHERE T.projectId = ?
                GROUP BY S.skillId, name
                ORDER BY count DESC
                """, mapper, projectId);
    }

    @Override
    public Collection<GetSkillWithDescriptionDto> getAllForStudent(int userId) {
        return jdbcTemplate.query("""
                SELECT S.skillId as id, name, isPending, SS.description
                FROM Skills AS S 
                INNER JOIN StudentsSkills AS SS 
                    ON SS.skillId = S.skillId
                WHERE SS.userId = ?                
                """, skillDescriptionMapper, userId);
    }

    public void addSkillToStudent(Integer userId, Integer skillId) {
        String sql = "INSERT INTO StudentsSkills (userId, skillId) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, skillId);
    }

    /**
     * Removes existing rows that violate the PK and replaces them with the new deiscriptoion if so
     *
     * @param userId
     * @param item
     */
    public void editSkillDescription(int userId, GetSkillWithDescriptionDto item) {
        String sql = "INSERT INTO StudentsSkills (userId, skillId, description) VALUES (?, ?, ?)" +
                "ON CONFLICT (userId, skillId) DO UPDATE SET description = ?";
        jdbcTemplate.update(sql, userId, item.skill().skillId(), item.description(), item.description());
    }

    public void removeSkillFromStudent(Integer userId, Integer skillId) {
        String sql = "DELETE FROM StudentsSkills WHERE userId = ? AND skillId = ?";
        jdbcTemplate.update(sql, userId, skillId);
    }

    public void updateSkillName(int skillId, String skillName) {
        String sql = "UPDATE Skills SET name = ? WHERE skillId = ?";
        jdbcTemplate.update(sql, skillName, skillId);
    }

    public void acceptSkill(int skillId) {
        String sql = "UPDATE Skills SET isPending = false WHERE skillId = ?";
        jdbcTemplate.update(sql, skillId);
    }

    public void deleteSkill(int skillId) {
        String sql = "DELETE FROM Skills WHERE skillId = ?";
        jdbcTemplate.update(sql, skillId);
    }

    public void deleteSkillFromAllTasks(int skillId) {
        String sql = "DELETE FROM TasksSkills WHERE skillId = ?";
        jdbcTemplate.update(sql, skillId);
    }

    public void deleteSkillFromAllStudents(int skillId) {
        String sql = "DELETE FROM StudentsSkills WHERE skillId = ?";
        jdbcTemplate.update(sql, skillId);
    }

    @Override
    public Collection<GetSkillDto> getAllSkillsCreatedAfter(Timestamp date) {
        return jdbcTemplate.query("""
                SELECT skillid AS id, name, ispending
                FROM skills
                WHERE createdat > ? AND ispending = false
                """, mapper, date);
    }

    @Override
    public List<GetSkillDto> getTopSkillsForBusiness(int businessId, int top) {
        return jdbcTemplate.query("""
                SELECT skills.skillid AS id, name, ispending
                FROM skills
                INNER JOIN tasksskills AS TS
                    ON TS.skillId = skills.skillid
                INNER JOIN tasks AS T
                    ON T.taskid = TS.taskid
                INNER JOIN projects AS P
                    ON P.projectid = T.projectid
                INNER JOIN supervisors AS S
                    ON P.userid = S.userid
                WHERE S.businessid = ?
                GROUP BY skills.skillid, name, ispending
                ORDER BY COUNT(skills.skillid) DESC
                LIMIT ?
                """, mapper, businessId, top);
    }

    @Override
    public List<GetSkillDto> getAllForTask(int taskId) {
        return jdbcTemplate.query("""
                SELECT skills.skillid AS id, name, ispending
                FROM skills
                INNER JOIN tasksskills AS TS
                    ON TS.skillId = skills.skillid
                WHERE TS.taskId = ?
                """, mapper, taskId);
    }
}
