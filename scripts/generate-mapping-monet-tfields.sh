#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} generate_mapping --r2rml -o mapping_tfields_monet.ttl -b http://data.linkedeodata.eu/tfields -u monetdb -p monetdb jdbc:monetdb://localhost/tfields
