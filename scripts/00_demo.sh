#!/bin/bash

function pause(){
   echo -e "\n\n"
   read -p "$*"
}

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${BASEDIR}/env.sh

rm *.ttl *.nt 2>/dev/null

check=`ps aux|grep 'apache-tomcat'`

if [[ -z "${check}" ]] ; then
	/home/kostis/apps/apache-tomcat/bin/shutdown.sh
	sleep 5
fi

dropdb leo-demo
#createdb leo-demo
#psql -c "CREATE EXTENSION postgis;" leo-demo

rm -rf /tmp/leo-demo
clear;

pause "Press ENTER to generate all mappings."
./01_generate_mappings.sh

pause "Press ENTER to generate RDF documents from input shape files."
./02_dump_rdf.sh

pause "Press ENTER to store the generated data to Strabon."
mkdir /tmp/leo-demo
cat *.nt > /tmp/leo-demo/data.nt
cd /tmp/leo-demo
/home/kostis/dev/StrabonLoader/filler /tmp/leo-demo/data.nt ''
/home/kostis/dev/StrabonLoader/LoadingScripts/postgis/import leo-demo

echo -e "\n\nPress ENTER to start Strabon endpoint"
/home/kostis/apps/apache-tomcat/bin/startup.sh

pause "Open your browser at http://localhost:8080/strabon/"
