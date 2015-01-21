#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o all_postgis.nt -b http://data.linkedeodata.eu/CWI-Workshop-data -u kostis -p p1r3as -jdbc jdbc:postgresql:leo mapping_all_postgis.ttl
