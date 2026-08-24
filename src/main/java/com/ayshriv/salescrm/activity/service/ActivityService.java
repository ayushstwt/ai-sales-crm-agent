package com.ayshriv.salescrm.activity.service;

import com.ayshriv.salescrm.activity.dto.ActivityCreateRequest;
import com.ayshriv.salescrm.activity.dto.ActivitySearchRequest;
import com.ayshriv.salescrm.activity.dto.TimelineItemDto;
import com.ayshriv.salescrm.common.resources.ApiStatus;

import java.util.List;

public interface ActivityService {

    ApiStatus listActivities(ActivitySearchRequest request);

    ApiStatus viewActivity(Long id);

    ApiStatus createActivity(ActivityCreateRequest request);

    ApiStatus deleteActivity(Long id);

    List<TimelineItemDto> getCustomerTimeline(Long leadId, Long contactId, Long companyId, Long dealId);
}