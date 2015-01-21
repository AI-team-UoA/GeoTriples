#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o osm_roads_monet.nt -b http://data.linkedeodata.eu/osm-roads -u monetdb -p monetdb -jdbc jdbc:monetdb://localhost/osm-roads mapping_osm_roads_monet.ttl
