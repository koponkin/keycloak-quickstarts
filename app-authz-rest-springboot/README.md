### Run Keycloak
docker-compose -f docker-compose-keycloak-local.yml up

### Import the realm
config/quickstart-realm.json

### Add users
1. Create a `user` role in Realm roles
2. Create users
   1. igor with password `igor`
   2. jdou with password `jdou`
   3. aclice with password `alice`
3. Add `user` role  to `Assigned roles` on the `Role mappings` tab.


### Run the app
mvn spring-boot:run

