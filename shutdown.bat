@echo off
setlocal

:: URL сервера
set "URL=http://localhost:8005/actuator/shutdown"

:: Выполняем PowerShell команду для POST-запроса
powershell.exe -Command ^
"Invoke-RestMethod -Method Post -Uri '%URL%' -Headers @{'Content-Type'='application/json'}"


pause