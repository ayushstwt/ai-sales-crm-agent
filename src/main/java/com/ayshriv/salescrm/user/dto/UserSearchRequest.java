package com.ayshriv.salescrm.user.dto;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;

public class UserSearchRequest extends BaseSearchRequest {

    private String email;
    private String name;
    private String userType;
    private Long organizationId;

    public UserSearchRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
