@echo off
title TaskForge Backend
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
if not exist "C:\taskforge\uploads" mkdir "C:\taskforge\uploads"
cd /d "%~dp0backend"
"C:\Users\grego\.m2\wrapper\dists\apache-maven-3.9.15\9925cc1d\bin\mvn.cmd" spring-boot:run
pause
