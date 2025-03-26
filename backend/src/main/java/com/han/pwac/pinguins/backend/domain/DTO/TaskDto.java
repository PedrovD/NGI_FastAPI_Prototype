package com.han.pwac.pinguins.backend.domain.DTO;

import org.hibernate.validator.constraints.Length;

public class TaskDto {
    private int taskId;
    private int projectId;
    @Length(max=50)
    private String title;
    private String description;
    private int totalNeeded;

    public TaskDto(int taskId, int projectId, String title, String description, int totalNeeded) {
        setTaskId(taskId);
        setProjectId(projectId);
        setTitle(title);
        setDescription(description);
        setTotalNeeded(totalNeeded);
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
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
}
