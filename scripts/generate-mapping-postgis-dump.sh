#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh
java -jar ${GEOTRIPLESJAR} generate_mapping --r2rml -o mapping_postgis_dump.ttl -b http://data.linkedeodata.eu/CWI-Workshop-Data -u postgres -p postgres jdbc:postgresql:postgres-dump
