#!/usr/bin/env bash
rm -f /tmp/dump.sql
docker exec wlfootballleagueapi_db_1 sh -c 'exec mysqldump --databases wlceligue -uroot -p"$MYSQL_ROOT_PASSWORD"' > /tmp/dump.sql
echo "" | mutt -a /tmp/dump.sql -s "FIFA Dump" -- johann.vanackere@equensworldline.com, manuel.russo@equensworldline.com