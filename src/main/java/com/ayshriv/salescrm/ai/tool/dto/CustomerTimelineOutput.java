package com.ayshriv.salescrm.ai.tool.dto;

import com.ayshriv.salescrm.activity.dto.TimelineItemDto;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomerTimelineOutput implements Serializable {

    private int count;
    private List<TimelineItemDto> timeline = new ArrayList<>();
    private String message;

    public CustomerTimelineOutput() {
    }

    public CustomerTimelineOutput(int count, List<TimelineItemDto> timeline, String message) {
        this.count = count;
        this.timeline = timeline != null ? timeline : new ArrayList<>();
        this.message = message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<TimelineItemDto> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineItemDto> timeline) {
        this.timeline = timeline != null ? timeline : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}