#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o tfields_monet.nt -b http://data.linkedeodata.eu/tfields -u monetdb -p monetdb -jdbc jdbc:monetdb://localhost/tfields mapping_tfields_monet.ttl
