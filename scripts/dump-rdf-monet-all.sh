#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o all_monet.nt -b http://data.linkedeodata.eu/CWI-Workshop-data -u monetdb -p monetdb -jdbc jdbc:monetdb://localhost/leo mapping_all_monet.ttl
