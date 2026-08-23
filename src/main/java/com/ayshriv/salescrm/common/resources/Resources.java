package com.ayshriv.salescrm.common.resources;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.http.converter.json.MappingJacksonValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Resources {

    public static final String FILTER_NAME = "apiStatusFilter";

    private Resources() {
        // Utility class
    }

    public static ApiStatus setStatus(String type, String text, String entity) {
        ApiStatus status = new ApiStatus();
        status.setStatusType(type);
        if (text != null && entity != null) {
            status.setText(text.replace("|ENTITY|", entity));
        } else {
            status.setText(text);
        }
        return status;
    }

    public static MappingJacksonValue formatedResponse(Object obj, Set<String> properties) {
        MappingJacksonValue mapping = new MappingJacksonValue(obj);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        if (properties != null && !properties.isEmpty()) {
            filterProvider.addFilter(FILTER_NAME, SimpleBeanPropertyFilter.filterOutAllExcept(properties));
        } else {
            filterProvider.addFilter(FILTER_NAME, SimpleBeanPropertyFilter.serializeAll());
        }
        mapping.setFilters(filterProvider);
        return mapping;
    }

    public static MappingJacksonValue formatedResponse(Object obj, String... properties) {
        Set<String> propertySet = (properties != null && properties.length > 0) 
                ? new HashSet<>(Arrays.asList(properties)) 
                : new HashSet<>();
        return formatedResponse(obj, propertySet);
    }

    public static <T extends BaseSearchRequest> T getDefaultRequest(T request) {
        if (request == null) {
            request = (T) new BaseSearchRequest();
        }
        if (request.getPageNumber() == null || request.getPageNumber() <= 0) {
            request.setPageNumber(1);
        }
        if (request.getPageSize() == null || request.getPageSize() <= 0) {
            request.setPageSize(10);
        }
        if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy("id");
        }
        if (request.getOrderDir() == null || request.getOrderDir().trim().isEmpty()) {
            request.setOrderDir("ASC");
        }
        return request;
    }
}
