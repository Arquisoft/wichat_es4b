# wichat_es4b

[![Deploy on release](https://github.com/Arquisoft/wichat_es4b/actions/workflows/release.yml/badge.svg)](https://github.com/Arquisoft/wichat_es4b/actions/workflows/release.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Arquisoft_wichat_es4b&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Arquisoft_wichat_es4b)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Arquisoft_wichat_es4b&metric=coverage)](https://sonarcloud.io/summary/overall?id=Arquisoft_wichat_es4b)

## Contributors:

| Contributor | Profile                                                                                                                                                        |
| ------------- |----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Alfredo Jirout Cid  | <a href="https://github.com/UO288443"><img src="https://img.shields.io/badge/UO288443-Alfredo Jirout Cid-red"></a>                                             |
| Manuel García Baldo  | <a href="https://github.com/manugbd"2><img src="https://img.shields.io/badge/manugbd-Manuel García Baldo-purple"></a>                                          |
| Miguel Olamendi Alonso  | <a href="https://github.com/uo285032"><img src="https://img.shields.io/badge/uo285032-Miguel Olamendi Alonso-green"></a>                                       |
| Nicolas Guerbartchouk Pérez  | <a href="https://github.com/NicolasGuerbartchoukPerez"><img src="https://img.shields.io/badge/NicolasGuerbartchoukPerez-Nicolas Guerbartchouk Pérez-blue"></a> |

As a basis, we have used last year's project [wiq_es04b](https://github.com/Arquisoft/wiq_es04b).

### Local deployment instructions:

#### Without docker (slower):

1. Fist you have to clone the repository using a CMD and the following command: `git clone https://github.com/Arquisoft/wichat_es4b.git` or using an IDE with Git integration or any other app of your preference.

2. Then you have to execute the [runServer.bat](https://github.com/Arquisoft/wichat_es4b/blob/master/database/hsqldb/bin/runServer.bat) to start the local database.

3. With the database initialized you have to open a CMD in the project root directory and execute the following command `mvnw spring-boot:run`, to start the application.

4. When the application is started the web app uses the port 3000. You can access the app through any web client using the following URL: http://localhost:3000/.

5. If you wish to execute the tests you have to open a CMD in the project root directory (you could use the same you used before), you have to execute `set EXCLUDE_JUNIT=true` if you also want to execute the E2E tests. Then to execute the tests you have to use the following command: `mvnw org.jacoco:jacoco-maven-plugin:prepare-agent verify`.

6. If you want to obtain the report you have to torn off the app and in the same CMD as before execute the following command: `mvnw org.jacoco:jacoco-maven-plugin:report`.

#### With docker (faster):

> #### *Disclaimer: This method is faster but it is not recommended for development because it is harder to debug and to see the logs and it is harder to execute the tests.*

1. First you need to have installed [docker](https://www.docker.com/#build) and docker [compose](https://docs.docker.com/compose/install/).
2. Then you have to clone the repository using a CMD and the following command: `git clone https://github.com/Arquisoft/wichat_es4b.git` or using an IDE with Git integration or any other app of your preference.
3. Then you have to open a CMD in the project root directory and execute the following command: `docker-compose up`. This is going to deploy the docker image that is in our repository. This docker will contain the app, a MySql database, Graphana and Prometheus. The app will be available in the port 443 https.