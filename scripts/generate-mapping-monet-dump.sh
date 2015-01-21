#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} generate_mapping --r2rml -o mapping_monet_dump.ttl -b http://data.linkedeodata.eu/CWI-Workshop-Data -u monetdb -p monetdb jdbc:monetdb://localhost/monet-dump
