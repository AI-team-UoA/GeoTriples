#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh
java -jar ${GEOTRIPLESJAR} generate_mapping --r2rml -o mapping_all_postgis.ttl -b http://data.linkedeodata.eu/CWI-Workshop-Data -u kostis -p p1r3as jdbc:postgresql:leo
