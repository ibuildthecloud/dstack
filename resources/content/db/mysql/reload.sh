#!/bin/bash

cd $(dirname $0)

mysqldump -u root cattle > cattle_dump_$(date '+%s').sql
mysql -u root < reload_db.sql
