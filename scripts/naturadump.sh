#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -jar ${GEOTRIPLESJAR} dump_rdf -sh natura-de-2000/Natura2000_Spatial_Public_End2010_LAEA_Shape_DE.shp -b http://data.linkedeodata.eu/naturade2000 -o natura.nt -s 4326 mapping.ttl
