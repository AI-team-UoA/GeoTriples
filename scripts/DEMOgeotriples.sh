#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh
demoargs=generate_mapping -o natura.ttl -b http://natura.org/ natura-de-2000/Natura2000_Spatial_Public_End2010_LAEA_Shape_DE.shp
java -jar ${GEOTRIPLESJAR} $@
