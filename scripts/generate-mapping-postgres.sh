#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} generate_mapping --r2rml -o mapping_postgis.ttl -b http://data.linkedeodata.eu/demobetrieb -u postgres -p postgres jdbc:postgresql:demobetrieb
