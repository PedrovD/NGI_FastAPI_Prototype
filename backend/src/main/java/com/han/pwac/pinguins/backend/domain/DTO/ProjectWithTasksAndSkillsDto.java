package com.han.pwac.pinguins.backend.domain.DTO;

import java.util.List;

public class ProjectWithTasksAndSkillsDto {
    private int projectId;
    private String title;
    private String description;
    private FileDto image;
    private List<TaskWithSkills> tasks;

    public ProjectWithTasksAndSkillsDto(List<TaskWithSkills> tasks, FileDto image, String description, String title, int projectId) {
        this.tasks = tasks;
        this.image = image;
        this.description = description;
        this.title = title;
        this.projectId = projectId;
    }

    public ProjectWithTasksAndSkillsDto() {
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

    public FileDto getImage() {
        return image;
    }

    public void setImage(FileDto image) {
        this.image = image;
    }

    public List<TaskWithSkills> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskWithSkills> tasks) {
        this.tasks = tasks;
    }

}
