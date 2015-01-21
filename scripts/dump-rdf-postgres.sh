#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o demobetrieb_postgis.nt -b http://data.linkedeodata.eu/demobetrieb -u postgres -p postgres -jdbc jdbc:postgresql:demobetrieb mapping_postgis.ttl
