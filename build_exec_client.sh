#!/bin/bash

set -e
./gradlew jar
java -cp ./build/libs/kogaproject-v1.1.jar com.test.koga.ktcpsocketlib.LauncherClient
