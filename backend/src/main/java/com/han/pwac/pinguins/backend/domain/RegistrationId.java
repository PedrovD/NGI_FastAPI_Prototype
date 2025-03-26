package com.han.pwac.pinguins.backend.domain;

import java.util.Objects;

public class RegistrationId {
    public final int userId;
    public final int taskId;

    public RegistrationId(int userId, int taskId) {
        this.userId = userId;
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationId that = (RegistrationId) o;
        return userId == that.userId && taskId == that.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, taskId);
    }
}
