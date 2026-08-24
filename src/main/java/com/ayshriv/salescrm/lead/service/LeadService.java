package com.ayshriv.salescrm.lead.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.lead.dto.LeadConvertRequest;
import com.ayshriv.salescrm.lead.dto.LeadCreateRequest;
import com.ayshriv.salescrm.lead.dto.LeadSearchRequest;
import com.ayshriv.salescrm.lead.dto.LeadUpdateRequest;

public interface LeadService {

    ApiStatus listLeads(LeadSearchRequest request);

    ApiStatus viewLead(Long id);

    ApiStatus createLead(LeadCreateRequest request);

    ApiStatus editLead(Long id, LeadUpdateRequest request);

    ApiStatus deleteLead(Long id);

    ApiStatus convertLead(Long id, LeadConvertRequest request);
}
