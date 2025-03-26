package com.han.pwac.pinguins.backend.domain.DTO;

public class SupervisorDto {
    private int supervisorId;
    private int businessId;

    public int getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(int supervisorId) {
        this.supervisorId = supervisorId;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }
}
