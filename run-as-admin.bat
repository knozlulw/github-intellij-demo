@echo off
:: Run GameBoost as Administrator — use this during development
:: Requires Java 17+ and Maven to be on PATH

powershell -Command "Start-Process cmd -ArgumentList '/c cd /d %~dp0 && mvn javafx:run' -Verb RunAs"
