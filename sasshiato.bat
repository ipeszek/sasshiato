REM 
REM Note: %~dp0 resolves full path to where this script resides 
REM 

java -cp %~dp0\build\libs\sasshiato-4.0.jar;%~dp0\build\libs\itext-2.1.4.jar;%~dp0\build\libs\itext-rtf-2.1.4.jar -Dcprops=%1 com.btcisp.sasshiato.SasshiatoMain
