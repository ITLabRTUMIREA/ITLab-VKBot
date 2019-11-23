@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  vk_bot startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and VK_BOT_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\vk_bot-0.0.1.jar;%APP_HOME%\lib\ktor-server-netty-1.2.2.jar;%APP_HOME%\lib\ktor-server-host-common-1.2.2.jar;%APP_HOME%\lib\ktor-gson-1.2.2.jar;%APP_HOME%\lib\ktor-server-core-1.2.2.jar;%APP_HOME%\lib\ktor-http-cio-jvm-1.2.2.jar;%APP_HOME%\lib\ktor-http-jvm-1.2.2.jar;%APP_HOME%\lib\ktor-network-1.2.2.jar;%APP_HOME%\lib\ktor-utils-jvm-1.2.2.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.3.41.jar;%APP_HOME%\lib\logback-classic-1.3.0-alpha5.jar;%APP_HOME%\lib\jsoup-1.12.1.jar;%APP_HOME%\lib\hibernate-entitymanager-5.4.1.Final.jar;%APP_HOME%\lib\hibernate-core-5.4.1.Final.jar;%APP_HOME%\lib\postgresql-42.2.8.jre7.jar;%APP_HOME%\lib\mail-1.5.0-b01.jar;%APP_HOME%\lib\fuel-gson-2.1.0.jar;%APP_HOME%\lib\fuel-2.1.0.jar;%APP_HOME%\lib\slf4j-simple-1.7.29.jar;%APP_HOME%\lib\sdk-0.5.12.jar;%APP_HOME%\lib\unirest-java-2.3.14.jar;%APP_HOME%\lib\jedis-3.1.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.3.41.jar;%APP_HOME%\lib\result-2.2.0.jar;%APP_HOME%\lib\kotlinx-coroutines-io-jvm-0.1.10.jar;%APP_HOME%\lib\kotlinx-io-jvm-0.1.10.jar;%APP_HOME%\lib\atomicfu-0.12.9.jar;%APP_HOME%\lib\kotlinx-coroutines-jdk8-1.2.2.jar;%APP_HOME%\lib\kotlinx-coroutines-core-1.2.2.jar;%APP_HOME%\lib\kotlin-reflect-1.3.40.jar;%APP_HOME%\lib\kotlin-stdlib-1.3.41.jar;%APP_HOME%\lib\logback-core-1.3.0-alpha5.jar;%APP_HOME%\lib\slf4j-api-2.0.0-alpha1.jar;%APP_HOME%\lib\javax.mail-1.6.2.jar;%APP_HOME%\lib\checker-framework-1.7.0.jar;%APP_HOME%\lib\hibernate-commons-annotations-5.1.0.Final.jar;%APP_HOME%\lib\jboss-logging-3.3.2.Final.jar;%APP_HOME%\lib\javax.persistence-api-2.2.jar;%APP_HOME%\lib\javassist-3.24.0-GA.jar;%APP_HOME%\lib\byte-buddy-1.9.5.jar;%APP_HOME%\lib\antlr-2.7.7.jar;%APP_HOME%\lib\jboss-transaction-api_1.2_spec-1.1.1.Final.jar;%APP_HOME%\lib\jandex-2.0.5.Final.jar;%APP_HOME%\lib\classmate-1.3.4.jar;%APP_HOME%\lib\jaxb-runtime-2.3.1.jar;%APP_HOME%\lib\jaxb-api-2.3.1.jar;%APP_HOME%\lib\javax.activation-api-1.2.0.jar;%APP_HOME%\lib\dom4j-2.1.1.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\gson-2.8.5.jar;%APP_HOME%\lib\config-1.3.1.jar;%APP_HOME%\lib\netty-codec-http2-4.1.36.Final.jar;%APP_HOME%\lib\alpn-api-1.1.3.v20160715.jar;%APP_HOME%\lib\netty-transport-native-kqueue-4.1.36.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.36.Final.jar;%APP_HOME%\lib\commons-collections4-4.1.jar;%APP_HOME%\lib\commons-io-2.5.jar;%APP_HOME%\lib\httpmime-4.5.9.jar;%APP_HOME%\lib\httpclient-4.5.9.jar;%APP_HOME%\lib\commons-lang3-3.6.jar;%APP_HOME%\lib\httpasyncclient-4.1.4.jar;%APP_HOME%\lib\json-20180813.jar;%APP_HOME%\lib\commons-pool2-2.6.2.jar;%APP_HOME%\lib\kotlinx-coroutines-io-0.1.10.jar;%APP_HOME%\lib\kotlinx-io-0.1.10.jar;%APP_HOME%\lib\kotlinx-coroutines-core-common-1.2.2.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.3.41.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\txw2-2.3.1.jar;%APP_HOME%\lib\istack-commons-runtime-3.0.7.jar;%APP_HOME%\lib\stax-ex-1.8.jar;%APP_HOME%\lib\FastInfoset-1.2.15.jar;%APP_HOME%\lib\atomicfu-common-0.12.9.jar;%APP_HOME%\lib\netty-codec-http-4.1.36.Final.jar;%APP_HOME%\lib\netty-handler-4.1.36.Final.jar;%APP_HOME%\lib\netty-codec-4.1.36.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.36.Final.jar;%APP_HOME%\lib\netty-transport-4.1.36.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.36.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.36.Final.jar;%APP_HOME%\lib\netty-common-4.1.36.Final.jar;%APP_HOME%\lib\httpcore-4.4.11.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.11.jar;%APP_HOME%\lib\httpcore-nio-4.4.10.jar

@rem Execute vk_bot
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %VK_BOT_OPTS%  -classpath "%CLASSPATH%" MainKt %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable VK_BOT_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%VK_BOT_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
