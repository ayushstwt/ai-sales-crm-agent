package com.ayshriv.salescrm.deal.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.deal.dto.DealCreateRequest;
import com.ayshriv.salescrm.deal.dto.DealMoveStageRequest;
import com.ayshriv.salescrm.deal.dto.DealSearchRequest;
import com.ayshriv.salescrm.deal.dto.DealUpdateRequest;

public interface DealService {

    ApiStatus listDeals(DealSearchRequest request);

    ApiStatus viewDeal(Long id);

    ApiStatus createDeal(DealCreateRequest request);

    ApiStatus editDeal(Long id, DealUpdateRequest request);

    ApiStatus deleteDeal(Long id);

    ApiStatus moveStage(Long id, DealMoveStageRequest request);
}
