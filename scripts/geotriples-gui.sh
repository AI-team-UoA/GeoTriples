#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

java -Xmx1024m -classpath "${CP}" eu.linkedeodata.geotriples.gui.GeoTriples  $@
