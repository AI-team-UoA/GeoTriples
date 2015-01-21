#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} generate_mapping --r2rml -o mapping_demobetrieb_monet.ttl -b http://data.linkedeodata.eu/demobetrieb -u monetdb -p monetdb jdbc:monetdb://localhost/demobetrieb
