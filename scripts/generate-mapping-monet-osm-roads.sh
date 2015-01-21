#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} generate_mapping --r2rml -o mapping_osm_roads_monet.ttl -b http://data.linkedeodata.eu/osm-roads -u monetdb -p monetdb jdbc:monetdb://localhost/osm-roads
