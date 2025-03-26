package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.Collection;
import java.util.List;

public class TaskWithSkills {
    private int taskId;
    private String title;
    private String description;
    private int totalNeeded;
    private int totalAccepted;
    private int totalRegistered;
    private Collection<GetSkillDto> skills;

    public TaskWithSkills(TaskDto task, int totalAccepted, int totalRegistered, Collection<GetSkillDto> skills) {
        setTaskId(task.getTaskId());
        setTitle(task.getTitle());
        setDescription(task.getDescription());
        setTotalNeeded(task.getTotalNeeded());
        setTotalAccepted(totalAccepted);
        setTotalRegistered(totalRegistered);
        setSkills(skills);
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalNeeded() {
        return totalNeeded;
    }

    public void setTotalNeeded(int totalNeeded) {
        this.totalNeeded = totalNeeded;
    }

    public int getTotalAccepted() {
        return totalAccepted;
    }

    public void setTotalAccepted(int totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    public int getTotalRegistered() {
        return totalRegistered;
    }

    public void setTotalRegistered(int totalRegistered) {
        this.totalRegistered = totalRegistered;
    }

    public Collection<GetSkillDto> getSkills() {
        return skills;
    }

    public void setSkills(Collection<GetSkillDto> skills) {
        this.skills = skills;
    }
}
