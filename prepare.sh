#!/bin/bash

# 1. sort by line hash (deterministic)
# 2. append with current line number
# 3. sort back by IDs
# 4. turn into SQL
# 5. replace last comma with a semicolon
# 6. save output to tmp file
cat rawQuotes.txt \
    | gsort -R --random-source=/dev/zero \
    | awk '{printf("%s, %d\n", $0,NR)}' \
    | gsort -h \
    | awk '{ printf("(%s),\n", $0 ) }' \
    | sed '$s/,$/;/' \
    > tmp

# replace quotes section in actual .sql file
lead='^-- START_QUOTES$'
tail='^-- END_QUOTES$'
sed -i '' -e "/$lead/,/$tail/{ /$lead/{p; r tmp
}; /$tail/p; d
}" advices.sql

# clean up
rm tmp

# Generate db from sql and save it to shared assets
cat advices.sql | sqlite3 common/src/main/assets/databases/advices.db