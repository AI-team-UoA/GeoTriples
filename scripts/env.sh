#!/bin/bash

export BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export GEOTRIPLESJAR=`ls -1 ${BASEDIR}/../target/geotriples-*.one-jar.jar`
CP=
for f in $(ls -1 ${BASEDIR}/../target/*.jar);
do
	CP="${f}:${CP}";
done
export CP

if [[ ! -e "${GEOTRIPLESJAR}" ]] ;
then
	echo "Could not find the compiled jar files of GeoTriples. Please compile GeoTriples before invoking this script.";
	exit 0;
fi

