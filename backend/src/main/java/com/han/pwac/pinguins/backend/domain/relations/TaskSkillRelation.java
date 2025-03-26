package com.han.pwac.pinguins.backend.domain.relations;

public class TaskSkillRelation {
    private int taskId;
    private int skillId;

    public TaskSkillRelation() {
    }

    public TaskSkillRelation(int taskId, int skillId) {
        this.taskId = taskId;
        this.skillId = skillId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }
}
