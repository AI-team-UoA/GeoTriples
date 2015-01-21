#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -cp ${CP} eu.linkedeodata.geotriples.TempLayer generate_mapping  -o demotrieb.ttl -b http://www.linkedeodata.eu/demotrieb ~/00_demo/Demobetrieb_TFB_Polygon.shp
java -cp ${CP} eu.linkedeodata.geotriples.TempLayer generate_mapping  -o waterways.ttl -b http://www.linkedeodata.eu/waterways ~/00_demo/waterways.shp
#java -cp ${CP} eu.linkedeodata.geotriples.TempLayer generate_mapping  -o natura.ttl  -b http://data.linkedeodata.eu/naturade2000  ~/00_demo/Natura2000_Spatial_Public_End2010_LAEA_Shape_DE.shp

ls *.ttl
