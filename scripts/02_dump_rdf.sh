#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -cp ${CP} eu.linkedeodata.geotriples.TempLayer dump_rdf -sh ~/00_demo/Demobetrieb_TFB_Polygon.shp                         -b http://www.linkedeodata.eu/demotrieb  -o demotrieb.nt -s 4326 demotrieb.ttl
java -cp ${CP} eu.linkedeodata.geotriples.TempLayer dump_rdf -sh ~/00_demo/waterways.shp                                       -b http://www.linkedeodata.eu/waterways  -o waterways.nt -s 4326 waterways.ttl
#java -cp ${CP} eu.linkedeodata.geotriples.TempLayer dump_rdf -sh ~/00_demo/Natura2000_Spatial_Public_End2010_LAEA_Shape_DE.shp -b http://natura.linkedeodata.eu/ -o natura.nt    -s 4326 natura.ttl


ls *.nt
