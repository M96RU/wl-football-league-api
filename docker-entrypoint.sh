#!/bin/bash -x
set -eo pipefail

#mysql -proot -P3306 -h fifadb < /initdb.sql

cd /usr/src/app
mvn spring-boot:run
