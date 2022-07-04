DO
$do$
declare
arow record;
    tenant_id varchar(14);
    resource_id uuid;
    policy_id uuid;
	permission_id uuid;
    resource_srv text;
    role_name text;
    user_name text;
BEGIN
	resource_srv = '3766a5ad-d742-476e-8a3c-b24814588abd';
FOR i IN 1..320000 LOOP
	  FOR arow IN
SELECT 'tnt' || substr(md5(random()::text), 0, 12) as id
    LOOP
      tenant_id := arow.id;
END LOOP;
      resource_id := uuid_generate_v4();
		-- add resource
insert into resource_server_resource (id, name, display_name, type, owner, resource_server_id, owner_managed_access) values
    (resource_id, 'Vault:' || tenant_id, 'Vault:' || tenant_id, '/vaults', resource_srv, resource_srv, 'true');
-- add uri
insert into resource_uris (resource_id, value) values (resource_id, '/vaults/' || tenant_id);
-- add scopes
FOR arow IN
select * from resource_server_scope where name in ('vault:read', 'vault:write', 'vault:admin')
    LOOP
insert into resource_scope (resource_id, scope_id) values (resource_id, arow.id);
END LOOP;

      -- ===================================== Add READ permissions
      role_name = 'read';
	  user_name = 'alice';

      	-- Add policy
      policy_id = uuid_generate_v4();
insert into resource_server_policy (id, name, type, resource_server_id, owner, decision_strategy, logic) values
    (policy_id, tenant_id || ' - policy ' || role_name, 'user', resource_srv, resource_srv, 1, 0);


-- Assign a user on the policy
FOR arow IN
select id from user_entity where realm_id = 'spring-boot-quickstart' and username = user_name
    LOOP
insert into policy_config (policy_id, name, value) values
    (policy_id, 'users', '["' || arow.id || '"]');
END LOOP;

		-- Add permission
      permission_id = uuid_generate_v4();
insert into resource_server_policy (id, name, type, resource_server_id, owner, decision_strategy, logic) values
    (permission_id, tenant_id || ' - permission ' || role_name, 'uma', resource_srv, resource_srv, 1, 0);

-- Add scope policy
FOR arow IN
select * from resource_server_scope where name in ('vault:read')
    LOOP
insert into scope_policy (scope_id, policy_id) values (arow.id, permission_id);
END LOOP;

	  	-- Link policy <-> permission
insert into associated_policy (policy_id, associated_policy_id) values
    (permission_id, policy_id);

-- Link permission <-> resource

insert into resource_policy (resource_id, policy_id) values (resource_id, permission_id);



-- ===================================== Add write permissions
role_name = 'write';
      user_name = 'jdoe';

          -- Add policy
      policy_id = uuid_generate_v4();
insert into resource_server_policy (id, name, type, resource_server_id, owner, decision_strategy, logic) values
    (policy_id, tenant_id || ' - policy ' || role_name, 'user', resource_srv, resource_srv, 1, 0);

-- Assign a user on the policy
FOR arow IN
select id from user_entity where realm_id = 'spring-boot-quickstart' and username = user_name
    LOOP
insert into policy_config (policy_id, name, value) values
    (policy_id, 'users', '["' || arow.id || '"]');
END LOOP;

          -- Add permission
      permission_id = uuid_generate_v4();
insert into resource_server_policy (id, name, type, resource_server_id, owner, decision_strategy, logic) values
    (permission_id, tenant_id || ' - permission ' || role_name, 'uma', resource_srv, resource_srv, 1, 0);

-- Add scope policy
FOR arow IN
select * from resource_server_scope where name in ('vault:read', 'vault:write')
    LOOP
insert into scope_policy (scope_id, policy_id) values (arow.id, permission_id);
END LOOP;

          -- Link policy <-> permission
insert into associated_policy (policy_id, associated_policy_id) values
    (permission_id, policy_id);

-- Link permission <-> resource

insert into resource_policy (resource_id, policy_id) values (resource_id, permission_id);


-- ===================================== Add admin permissions
role_name = 'admin';
      user_name = 'igor';

          -- Add policy
      policy_id = uuid_generate_v4();
insert into resource_server_policy (id, name, type, resource_server_id, owner, decision_strategy, logic) values
    (policy_id, tenant_id || ' - policy ' || role_name, 'user', resource_srv, resource_srv, 1, 0);

-- Assign a user on the policy
FOR arow IN
select id from user_entity where realm_id = 'spring-boot-quickstart' and username = user_name
    LOOP
insert into policy_config (policy_id, name, value) values
    (policy_id, 'users', '["' || arow.id || '"]');
END LOOP;

          -- Add permission
      permission_id = uuid_generate_v4();
insert into resource_server_policy (id, name, type, resource_server_id, owner, decision_strategy, logic) values
    (permission_id, tenant_id || ' - permission ' || role_name, 'uma', resource_srv, resource_srv, 1, 0);

-- Add scope policy
FOR arow IN
select * from resource_server_scope where name in ('vault:read', 'vault:write', 'vault:admin')
    LOOP
insert into scope_policy (scope_id, policy_id) values (arow.id, permission_id);
END LOOP;


          -- Link policy <-> permission
insert into associated_policy (policy_id, associated_policy_id) values
    (permission_id, policy_id);

-- Link permission <-> resource

insert into resource_policy (resource_id, policy_id) values (resource_id, permission_id);




END LOOP;
END
$do$;