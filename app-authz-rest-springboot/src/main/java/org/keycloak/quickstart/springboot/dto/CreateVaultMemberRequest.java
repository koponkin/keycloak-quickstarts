package org.keycloak.quickstart.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateVaultMemberRequest {

    @JsonProperty("user_id")
    private String userId;
    private String role;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
