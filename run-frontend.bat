@echo off
title TaskForge Frontend
cd /d "%~dp0frontend"
"C:\Users\grego\.m2\wrapper\dists\apache-maven-3.9.15\9925cc1d\bin\mvn.cmd" javafx:run
pause
