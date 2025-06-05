@echo off

setlocal EnableDelayedExpansion

set THIS_SCRIPT=%~n0
set THIS_SCRIPT_DIR=%~dp0
set LIB_DIR=%THIS_SCRIPT_DIR%..\lib\
set JAR_NAME=%THIS_SCRIPT%.jar
set BUILD_LIB_DIR=%THIS_SCRIPT_DIR%..\..\..\..\build\libs\

rem Find the the jar.
for %%d in ("%THIS_SCRIPT_DIR%" "%LIB_DIR%" "%BUILD_LIB_DIR%") do (
    set MAYBE_EXISTS_JAR=%%~d%JAR_NAME%
    if exist "!MAYBE_EXISTS_JAR!" (
        if defined DEBUG echo JAR exists: !MAYBE_EXISTS_JAR!
	    set JAR=!MAYBE_EXISTS_JAR!
	    goto :runJar
	) else (
        if defined DEBUG echo JAR does not exist: !MAYBE_EXISTS_JAR!
    )
)
echo No %JAR_NAME% file found
goto :EOF

:runJar
if defined DEBUG echo Running %JAR%
java -jar "%JAR%" %*

endlocal