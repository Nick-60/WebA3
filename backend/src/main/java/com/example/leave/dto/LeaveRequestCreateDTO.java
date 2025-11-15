package com.example.leave.dto;

import com.example.leave.model.LeaveType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class LeaveRequestCreateDTO {
    @JsonProperty("leave_type")
    private LeaveType leaveType;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    private String comment;

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

