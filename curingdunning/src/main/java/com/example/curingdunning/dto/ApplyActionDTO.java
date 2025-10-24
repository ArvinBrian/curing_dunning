package com.example.curingdunning.dto;

import com.example.curingdunning.entity.ActionType;

import lombok.Data;

@Data
public class ApplyActionDTO {
    private Long eventId;
    private ActionType actionType;  // Must be ActionType, not Long

    // getters and setters
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
}


