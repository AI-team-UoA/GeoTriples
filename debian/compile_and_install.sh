#!/bin/sh

mvn clean install
sudo dpkg -i target/geotriples_1.0~SNAPSHOT_all.deb
echo "To run the GeoTriples engine, type geotriples on the cmd"
