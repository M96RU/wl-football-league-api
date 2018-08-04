#!/usr/bin/env bash
docker exec -i -t  wlfootballleagueapi_db_1 sh -c 'mysql -p"$MYSQL_ROOT_PASSWORD" wlceligue'