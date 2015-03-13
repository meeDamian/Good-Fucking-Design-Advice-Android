#!/bin/bash

# 1. sort by line hash
# 2. append with current line number
# 3. sort back by IDs
# 4. turn into SQL
# 5. replace last comma with a semicolon
# 6. save output to tmp file
cat rawQuotes.txt \
    | gsort -R \
    | awk '{printf("%s, %d\n", $0,NR)}' \
    | gsort -h \
    | awk '{ printf("(%s),\n", $0 ) }' \
    | sed '$s/,$/;/' \
    > tmp

# replace quotes section in actual .sql file
awk '                                                                                                                                     ✭ ✈ ✱
    BEGIN       {p=1}
    /^-- START/   {print;system("cat tmp");p=0}
    /^-- END/     {p=1}
    p' advices.sql > advices.sql

# clean up
rm tmp

# Generate db from sql and save it to shared assets
cat advices.sql | sqlite3 common/src/main/assets/databases/advices.db