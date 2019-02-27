#!/bin/bash
# This scrip expects fully qualified location of sprop.props file passed as $1
#

scriptdir="$(dirname "$0")"
cd "$scriptdir"

java -cp build/libs/sasshiato-4.0.jar:build/libs/itext-2.1.4.jar:build/libs/itext-rtf-2.1.4.jar -Dcprops=$1 com.btcisp.sasshiato.SasshiatoMain

