#!/bin/bash

# Generate db from sql and save it to shared assets
cat advices.sql | sqlite3 common/src/main/assets/advices.db