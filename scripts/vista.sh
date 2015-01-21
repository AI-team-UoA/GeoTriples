#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

#java -cp ${CP} eu.linkedeodata.geotriples.TempLayer generate_mapping  -o demotrieb.ttl -b http://www.linkedeodata.eu/demotrieb ~/00_demo/Demobetrieb_TFB_Polygon.shp

java -cp ${CP} eu.linkedeodata.geotriples.TempLayer dump_rdf -sh /home/kostis/00_vista_demo/lsg_b_fl.shp -b http://www.vista.de/protectedareas/lsg  -o /home/kostis/00_vista_demo/lsg.nt  /home/kostis/00_vista_demo/lsgmapping.ttl
