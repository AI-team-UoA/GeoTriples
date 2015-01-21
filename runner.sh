#!/bin/bash

mvn package
cd target
java -jar geotriples-1.0-SNAPSHOT.one-jar.jar
cd ..
