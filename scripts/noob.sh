#Author: Dimitrianos Savva dimis@di.uoa.gr
#Project: GeoTriples
#Description: Step-by-step run GeoTriples (people without experience on using it)

read -r -p '--> Select mode: Generate Mapping,Dump RDF [g/d]:' mode
#Generate mapping stuff
if [[ $mode =~ ^([gG][eE][nN][eE][rR][aA][tT][eE]|[gG])$ ]];then 
echo 'Info: Entering GenerateMapping mode...';echo;mode='generate'
#Dump rdf stuff
elif [[ $mode =~ ^([dD][uU][mM][pP]|[dD])$ ]];then
echo 'Info: Entering DumpRDF mode...';echo;mode='dump'
#Give the input mapping file
read -e -p '--> Give the path of input mapping file:' mapping
else
echo 'Error: Bad input. Should be `g` for Generate Mapping or `d` for Dump RDF'
exit
fi

read -r -p '--> Select datasource type: Shapefile,Database [s/d]:' datatype
while [[ ! $datatype =~ ^([sS][hH][aA][pP][eE][fF][iI][lL][eE]|[sS])$ ]] && [[ ! $datatype =~ ^([dD][aA][tT][aA][bB][aA][sS][eE]|[dD])$ ]]; do
read -r -p 'Error: Bad input. Should be `shapefile` or `database`. Give it again' datatype
done

if [[ $datatype =~ ^([sS][hH][aA][pP][eE][fF][iI][lL][eE]|[sS])$ ]];then
datatype='shapefile'
else
datatype='database'
fi


#Give the url of datasource
if [ "$datatype" = 'shapefile' ]; then
echo '--> Give the path for Shapefile'
else
echo '--> Give the jdbc url of the Database (e.g jdbc:monetdb://localhost:50000/demobjohn)'
fi
read -e url

echo 'Info: The datasource is described by: '$url

#Give other properties for database
if [ "$datatype" = 'database' ]; then
echo '--> Provide the connection properties' 
echo '--> Give the user'
read user
echo '--> Give the password'
read password
fi

echo '--> Give the path of the output file'
read -e output

#Give the base iri
echo '--> Give the base iri (e.g http://linkedeodata.eu/talking-fields)'
read baseiri

if [ "$mode" = 'generate' ];then
#Generate mapping stuff
if [ "$datatype" = 'shapefile' ]; then
command='generate_mapping -b '$baseiri' -o '$output' '$url
else
#Dump rdf stuff
command='generate_mapping -u '$user' -p '$password' -b '$baseiri' -o '$output' --r2rml '$url
fi
else
command='dump_rdf -b '$baseiri' -o '$output
if [ "$datatype" = 'shapefile' ]; then
command=$command' -sh '
else
command=$command' -u '$user' -p '$password
command=$command' -jdbc '
fi
command=$command' '$url' '$mapping
fi

echo '--> Ok you costructed the command:'
echo 'java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar '$command
read -r -p '--> Execute it? [y/N]' answer
if [[ $answer =~ ^([yY][eE][sS]|[yY])$ ]]; then
java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar $command
if [ "$mode" = 'generate' ];then
#Generate mapping stuff
read -r -p '--> Would you like to dump the source data into RDF? [y/N]' answer
echo answer
if [[ $answer =~ ^([yY][eE][sS]|[yY])$ ]]; then
mapping=$output
echo '--> Give the path of the output file'
read -e output

command='dump_rdf -b '$baseiri' -o '$output
if [ "$datatype" = 'shapefile' ]; then
command=$command' -sh '
else
command=$command' -u '$user' -p '$password
command=$command' -jdbc '
fi
command=$command' '$url' '$mapping

echo '--> Ok you costructed the command:'
echo 'java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar '$command
read -r -p '--> Execute it? [y/N]' answer
if [[ $answer =~ ^([yY][eE][sS]|[yY])$ ]]; then
java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar $command
fi

fi
fi
fi


