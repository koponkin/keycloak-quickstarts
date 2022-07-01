### Run Keycloak
docker-compose -f docker-compose-keycloak-local.yml up

### Import the realm
config/quickstart-realm.json

### Add `User` role to the `app-authz-rest-springboot` on the `scope` tab

### Run the app
mvn spring-boot:run

### Configure postman
Import requests from /postman/Keycloak Authorization demo.postman_collection.json 
