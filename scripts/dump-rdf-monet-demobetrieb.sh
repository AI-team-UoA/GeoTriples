#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -o demobetrieb_monet.nt -b http://data.linkedeodata.eu/demobetrieb -u monetdb -p monetdb -jdbc jdbc:monetdb://localhost/demobetrieb mapping_demobetrieb_monet.ttl
