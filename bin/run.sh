#!/usr/bin/env bash
cp -f ../target/cards-1.0-SNAPSHOT-jar-with-dependencies.jar cards.jar
java -jar cards.jar $1 7070