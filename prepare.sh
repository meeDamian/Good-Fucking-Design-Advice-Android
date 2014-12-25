#!/bin/bash

# Generate db from sql and save it to assets
cat advices.sql | sqlite3 mobile/src/main/assets/advices.db

# Copy db to wearable
cp mobile/src/main/assets/advices.db wear/src/main/assets/advices.db