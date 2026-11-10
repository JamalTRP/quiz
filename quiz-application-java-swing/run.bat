@echo off
REM Batch script to compile and run the Java Swing Quiz Application on Windows
echo Compiling Java Swing Quiz Application...
if not exist bin mkdir bin
javac -cp ".;lib/*" -d bin src\db\*.java src\model\*.java src\dao\*.java src\ui\*.java src\Main.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful! Launching Quiz Application...
    java -cp "bin;lib/*" Main
) else (
    echo Compilation failed. Please check for errors above.
    pause
)
