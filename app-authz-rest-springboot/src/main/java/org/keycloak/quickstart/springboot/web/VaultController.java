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

import org.keycloak.KeycloakPrincipal;
import org.keycloak.TokenVerifier;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.resource.PermissionResource;
import org.keycloak.authorization.client.resource.PolicyResource;
import org.keycloak.authorization.client.resource.ProtectedResource;
import org.keycloak.common.VerificationException;
import org.keycloak.quickstart.springboot.dto.CreateVaultRequest;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.keycloak.representations.idm.authorization.UmaPermissionRepresentation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@RestController
public class VaultController {


    public static final String VAULT_ADMIN_SCOPE = "vault:admin";
    public static final String VAULT_WRITE_SCOPE = "vault:write";
    public static final String VAULT_READ_SCOPE = "vault:read";

    @RequestMapping(value = "/vaults", method = RequestMethod.POST)
    public String createVault(
            Principal principal,
            @RequestBody CreateVaultRequest vault
    ) {
        // https://www.keycloak.org/docs/latest/authorization_services/#creating-a-resource-using-the-protection-api

        AuthzClient authzClient = AuthzClient.create();
        // create a new resource representation with the information we want
        ResourceRepresentation newResource = new ResourceRepresentation();

        String resourceName = "Vault:" + vault.getId();
        newResource.setName(resourceName);
        newResource.setType("/vaults");

        ScopeRepresentation adminScope = new ScopeRepresentation(VAULT_ADMIN_SCOPE);
        ScopeRepresentation writeScope = new ScopeRepresentation(VAULT_WRITE_SCOPE);
        ScopeRepresentation readScope = new ScopeRepresentation(VAULT_READ_SCOPE);
        newResource.addScope(adminScope);
        newResource.addScope(writeScope);
        newResource.addScope(readScope);
        Set<String> uris = new HashSet<>();
        uris.add("/vaults/" + vault.getId());
        uris.add("/vaults/" + vault.getId() + "/members");
        uris.add("/vaults/" + vault.getId() + "/members/{memberId}");
        newResource.setUris(uris);
        newResource.setOwnerManagedAccess(true);

        ProtectedResource resourceClient = authzClient.protection().resource();
        ResourceRepresentation existingResource = resourceClient.findByName(newResource.getName());

        if (existingResource != null) {
            resourceClient.delete(existingResource.getId());
        }

        ResourceRepresentation resource = resourceClient.create(newResource);
        PolicyResource policyClient = authzClient.protection().policy(resource.getId());

        Set<String> adminScopes = new HashSet<>();
        adminScopes.add(VAULT_ADMIN_SCOPE);
        adminScopes.add(VAULT_READ_SCOPE);
        adminScopes.add(VAULT_WRITE_SCOPE);

        // Add admin policy and permission
        UmaPermissionRepresentation umaPermissionRepresentation = new UmaPermissionRepresentation();
        umaPermissionRepresentation.setName(resourceName + " - admin users");
        umaPermissionRepresentation.addUser(principal.getName());
        umaPermissionRepresentation.setScopes(adminScopes);
        policyClient.create(umaPermissionRepresentation);

        Set<String> writeScopes = new HashSet<>();
        writeScopes.add(VAULT_READ_SCOPE);
        writeScopes.add(VAULT_WRITE_SCOPE);

        // Add write policy and permission
        umaPermissionRepresentation = new UmaPermissionRepresentation();
        umaPermissionRepresentation.setName(resourceName + " - write users");
        umaPermissionRepresentation.setScopes(writeScopes);
        policyClient.create(umaPermissionRepresentation);

        // Add write policy and permission
        umaPermissionRepresentation = new UmaPermissionRepresentation();
        umaPermissionRepresentation.setName(resourceName + " - read users");
        umaPermissionRepresentation.setScopes(Collections.singleton(VAULT_READ_SCOPE));
        policyClient.create(umaPermissionRepresentation);

        System.out.println(resource);
        return createResponse(resource.getName());
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public String handleVaultRequest(KeycloakPrincipal principal) throws VerificationException {
        AuthzClient authzClient = AuthzClient.create();

        AuthorizationRequest request = new AuthorizationRequest();
        request.addPermission(null, VAULT_READ_SCOPE);
        AuthorizationResponse authorizationResponse = authzClient.authorization(principal.getKeycloakSecurityContext().getTokenString())
                .authorize(request);


        AccessToken token = TokenVerifier.create(authorizationResponse.getToken(), AccessToken.class).getToken();
        List<String> vaults = token.getAuthorization().getPermissions().stream()
                .filter(it -> !it.getResourceName().contains("endpoint")) // filter /vaults resource
                .map(Permission::getResourceId)
                .collect(Collectors.toList());

        return "Found:" + vaults;
    }

    @RequestMapping(value = "/vaults/{vaultId}", method = RequestMethod.GET)
    public String handleVaultRequest(@PathVariable("vaultId") String vaultId) {
        return createResponse(vaultId);
    }

    @RequestMapping(value = "/vaults/{vaultId}", method = RequestMethod.PUT)
    public String putVault(@PathVariable("vaultId") String vaultId) {
        return createResponse(vaultId);
    }

    @RequestMapping(value = "/vaults/{vaultId}", method = RequestMethod.DELETE)
    public String deleteVault(@PathVariable("vaultId") String vaultId) {
        AuthzClient authzClient = AuthzClient.create();
        ProtectedResource resourceClient = authzClient.protection().resource();
        String[] resources = resourceClient.find(null, vaultId, null, null, null, null, false, null, null);
        if (resources.length == 0) {
            throw new RuntimeException("Not found");
        }

        if (resources.length > 1) {
            throw new RuntimeException("More than 1 resource was found");
        }

        resourceClient.delete(resources[0]);

        return "Deleted:" + vaultId;
    }


    private String createResponse(String vaultId) {
        return "Access Granted to " + vaultId;
    }
}
