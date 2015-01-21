#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o postgis_dump.nt -b http://data.linkedeodata.eu/CWI-Workshop-data -u postgres -p postgres -jdbc jdbc:postgresql:postgres-dump mapping_postgis_dump.ttl
