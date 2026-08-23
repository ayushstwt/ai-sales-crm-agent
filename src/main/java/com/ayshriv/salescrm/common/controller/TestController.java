package com.ayshriv.salescrm.common.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.Resources;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/filter-test")
    public MappingJacksonValue testFiltering() {
        ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, "Test Entity");
        status.setToken("secret-token-that-should-be-filtered-out");
        status.setTotal(100L);

        // Return only statusType and text, filtering out token and total
        return Resources.formatedResponse(status, "statusType", "text");
    }

    @PostMapping("/filter-test")
    public MappingJacksonValue testRequestBody(@RequestBody Object body) {
        ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, "Test Entity");
        return Resources.formatedResponse(status, "statusType", "text");
    }
}
