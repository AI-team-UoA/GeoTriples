#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o schalli_monet.nt -b http://data.linkedeodata.eu/schalli -u monetdb -p monetdb -jdbc jdbc:monetdb://localhost/schalli mapping_schalli_monet.ttl
