Sasshiato: Java PDF and RTF rendering companion program for RRG
-----------------------------------------------------------------

Sasshiato can generate PDF and RTF files from XML outputs generated from SAS.
It is a standalone project but it has been designed to work well with RRG macro reporting system
(https://github.com/ipeszek/RRG).

Sasshiato has been around since 2009. This is open source version of this program. 
Open source sasshiato starts with version 4.0 reserving <= 3.x for previous, not open
sourced versions of the product.

Setup (installation)
--------------------

These instructions assume POSIX environment (Linux, Mac).  
Windows setup instructions are very similar. 

You need to have JDK installed and JAVA_HOME properly configured for
`gradle` to work correctly. 
Chose a parent folder you want to install sasshiato into. 
Here we will call it `someFolder`. Open terminal in that folder. 
Clone this repo and run gradle jar task:

```
someFolder> git clone https://github.com/ipeszek/sasshiato.git
someFolder> cd sasshiato
sasshiato> ./gradlew
sasshiato> ./gradlew jar
```

If you trying to just install sasshiato for use with RRG you can just go to __With RRG__ section.

The rest of this section explains what has just happened. This should create the folder: 
```
sasshiato/build/libs
```
with all jars needed to run sasshiato.


To test that it runs using provided input xml files execute this:
```
sasshiato>  sh ./sasshiato.sh ./test_inputs/run.props
```
This should create test_outputs folder with an rtf, pdf and a log file.


With RRG
--------

Configure SASSHIATO_HOME in RRG to where this project was cloned,
i.e. `someFolder/sasshiato` (see the corresponding setup instructions). 


Documentation
-------------
Here somewhat outdated but much more complete documents:
* [SasshiatoCrashGuide](docs/SasshiatoCrashGuide.pdf)
* [SasshiatoDocumentation](docs/SasshiatoDocumentation.pdf)


_The following notes are incomplete but can provide some additional useful information:_ 

Configuration
-------------
Default global configuration is defined in [sasshiato.props](src/main/resources/sasshiato.props).
These can be changed/overridden in the input XML document
using `<__sprops></__sprops>` tag in the `<__datatype>RINFO </__datatype>` record as a comma delimited `name = value` list.
See example input XML in included `/test_inputs` folder.

Requesting file generation
--------------------------
A Java property file similar to [run.props](test_inputs/run.props)
needs to be passed to the sasshiato (see sasshiato.sh or sasshiato.bat).
It contains input and output location info as well as optional watermark file.

Watermark images
-----------------
(TODO -- needs better documentation, has not been tested recently)

defined as 
```
watermark_file=<fully qualified image file>
```
in the input java properties file.

It is possible to use text or rtf file (text encoded images are supported for rtf outputs only). 
To create rft wartermark, save rft file and clean it up removing `\header` or `\headerr` constructs.

All other extensions are treated as images: can be jpeg, png, wmf, bmp.
no resizing is done for rtf/txt files with images.


Other -D parameters
-------------------
* `-Dpdf_stamp_chunk_size` (defaulted to 1000).


Input XML Spec
--------------
See work in progress doc [InputXmlNotes.md](docs/InputXmlNotes.md)


