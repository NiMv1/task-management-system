@echo off
chcp 65001 >nul
:: Запуск PowerShell скрипта (без проблем с кодировкой)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0START_PROJECT.ps1"
