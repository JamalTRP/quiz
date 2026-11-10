#!/usr/bin/env bash
# Shell script to compile and run the Java Swing Quiz Application
echo "Compiling Java Swing Quiz Application..."
mkdir -p bin
javac -cp ".:lib/*" -d bin src/db/*.java src/model/*.java src/dao/*.java src/ui/*.java src/Main.java

if [ $? -eq 0 ]; then
    echo "Compilation successful. Launching Quiz Application..."
    java -cp "bin:lib/*" Main
else
    echo "Compilation failed. Ensure Java 17+ is installed and Connector JAR is in lib/."
fi
