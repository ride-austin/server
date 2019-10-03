# Ride-austin server application

## How to prepare env

### Prerequisites

* Install JDK 1.8+ http://www.oracle.com/technetwork/java/javase/downloads/index.html
* Install Maven https://maven.apache.org/download.cgi https://maven.apache.org/install.html
* Install MySQL server https://dev.mysql.com/downloads/mysql/
* Install Redis server https://redis.io/download

### Create database

* `mysql -u root -p`
* `> create database ride_db`
* `> create user 'rideaustin'@'localhost' identified by 'test123'`
* `> grant all on ride_db.* to 'rideaustin'@'localhost'`
* Obtain mysql dump from RC server
* `mysql -u rideaustin -p ride_db < dump.sql`
* Add in `/etc/mysql/my.cnf` in `[mysqld]` configuration: `lower_case_table_names = 1`, then restart mysql service

### Assembly and run (in repo directory):

* `$ cd PATH-TO-REPO-DIRECTORY`
* `$ mvn clean package -DskipTests=true && mvn cargo:run -f app/pom.xml`
* `localhost:8080` is ready

## dev set-up

`$ mvn dependency:sources` to download all sources`

## Integration tests execution (from IntelliJ IDEA)
docker container for mysql
```text
docker run --name mysql-4-test \
-e MYSQL_ROOT_PASSWORD=toor \
-e MYSQL_DATABASE=ride_db \
-e MYSQL_USER=test \
-e MYSQL_PASSWORD=test123 \
-p 3307:3306 -d mysql --lower_case_table_names=1
```

docker container for redis
```text
docker run --name redis-4-test -p 6378:6379 -d redis
```

## Logging

* [logback.xml](app/src/main/resources/logback.xml) - default Spring logger
* [logback-custom.xml](app/src/main/resources/logback-custom.xml) - Spring logger that is using on the `aws`
* [logging.properies](.ebextensions/files/logging.properties) - Tomcat JULI logger that is using on the `aws
