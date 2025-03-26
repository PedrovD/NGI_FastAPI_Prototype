package com.han.pwac.pinguins.backend.repository.contract;

import com.han.pwac.pinguins.backend.domain.DTO.GetSkillDto;
import com.han.pwac.pinguins.backend.domain.DTO.GetSkillWithDescriptionDto;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface ISkillDao extends IBaseDao<GetSkillDto> {
    List<GetSkillDto> getTopByProjectId(int projectId);

    Collection<GetSkillWithDescriptionDto> getAllForStudent(int userId);

    void addSkillToStudent(Integer userId, Integer skillId);

    void editSkillDescription(int userId, GetSkillWithDescriptionDto item);

    void removeSkillFromStudent(Integer userId, Integer skillId);

    Collection<GetSkillDto> getAllSkillsCreatedAfter(Timestamp date);

    List<GetSkillDto> getTopSkillsForBusiness(int businessId, int top);

    List<GetSkillDto> getAllForTask(int taskId);
}
