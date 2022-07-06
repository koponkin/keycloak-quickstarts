/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.keycloak.quickstart.springboot.web;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.resource.PolicyResource;
import org.keycloak.quickstart.springboot.dto.CreateVaultMemberRequest;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.UmaPermissionRepresentation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@RestController
public class VaultMembersController {


    public static final String VAULT_ADMIN_SCOPE = "vault:admin";

    @RequestMapping(value = "/vaults/{vaultId}/members", method = RequestMethod.GET)
    public String getMembers(@PathVariable String vaultId) {
        AuthzClient authzClient = AuthzClient.create();
        ResourceRepresentation resource = authzClient.protection().resource().findByUri("/vaults/"+ vaultId).get(0);
        PolicyResource policyClient = authzClient.protection().policy(resource.getId());
        List<UmaPermissionRepresentation> policies = policyClient.find(null, null, null, null);

        Map<String, Set<String>> usersGrouped = new HashMap<>();
        policies.forEach(policy -> {
            Set<String> users = policy.getUsers() == null ? new HashSet<>() : policy.getUsers();
            usersGrouped.put(policy.getName(), users);
        });
        return usersGrouped.toString();
    }

    @RequestMapping(value = "/vaults/{vaultId}/members", method = RequestMethod.POST)
    public String addMember(@PathVariable String vaultId, @RequestBody CreateVaultMemberRequest request) {
        AuthzClient authzClient = AuthzClient.create();
        ResourceRepresentation resource = authzClient.protection().resource().findByUri("/vaults/"+ vaultId).get(0);
        PolicyResource policyClient = authzClient.protection().policy(resource.getId());
        List<UmaPermissionRepresentation> policies = policyClient.find(null, null, null, null);

        Set<UmaPermissionRepresentation> oldPolicies = policies.stream()
                .filter(policy ->
                        policy.getUsers() != null
                                && policy.getUsers().contains(request.getUserId())
                ).peek(policy -> policy.removeUser(request.getUserId()))
                .collect(Collectors.toSet());

        Set<UmaPermissionRepresentation> newPolicies = policies.stream()
                .filter(policy ->
                        policy.getName().contains(request.getRole())
                ).peek(policy -> policy.addUser(request.getUserId()))
                .collect(Collectors.toSet());

        oldPolicies.addAll(newPolicies);
        oldPolicies.forEach(policyClient::update);
        return "Updated";
    }

    @RequestMapping(value = "/vaults/{vaultId}/members/{userId}", method = RequestMethod.DELETE)
    public String addMember(@PathVariable String vaultId, @PathVariable String userId) {
        AuthzClient authzClient = AuthzClient.create();
        ResourceRepresentation resource = authzClient.protection().resource().findByUri("/vaults/"+ vaultId).get(0);
        PolicyResource policyClient = authzClient.protection().policy(resource.getId());
        List<UmaPermissionRepresentation> policies = policyClient.find(null, null, null, null);

        policies.stream()
                .filter(policy ->
                        policy.getUsers() != null
                                && policy.getUsers().contains(userId)
                ).peek(policy -> policy.removeUser(userId))
                .forEach(policyClient::update);

        return "Deleted";
    }

}
