@echo off
title TaskForge Frontend
cd /d "%~dp0frontend"
"C:\Users\MP2GH\.m2\wrapper\dists\apache-maven-3.9.14-bin\1cb7fhup6b5n3bed6kckbrnspv\apache-maven-3.9.14\bin\mvn.cmd" javafx:run
pause
