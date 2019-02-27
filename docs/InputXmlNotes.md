Sasshiato interface documentation 
---------------------------------
(This is a rough cut, more details are TODO)

 
special formatting:
-------------------
* `\,{,}` are restricted characters.  
* `/` is restricted escape character by default but can be changed.  
* `~{super` is restricted word  
(see below how to print restricted characters).


Escaped formats:
----------------

NOTE the escape character `/` (shown) can be replaced using `esc_char` sprops property.  
 * `/s#<number>`  or `/s#le, /s#ge, /s#pm`  - case sensitive, symbol font characters.  Has to be ended with SPACE  
 * `/#<number>`  ability to pass ASCI deciml encoding as a char. Has to be ended with SPACE
 * `//` - line break
 * `/t<number>` - line break with tab
 * `/i<number>` - initial tab (only at the begining of txt
 
How to force print special characters:
 to print `/` either use `/#47` or change escape to a different character
 
 * `~{super text}` - puts text in supperscript. Text cannot contain ~{super other than to indicate 
  superscript.
 
 * `- \` is restricted for future use advanced rft syntax and is not supported (unless you use double slash \\)
     to print \ use /#92 you can also use \\
 * `{}` are restricted for future (currently not suppored) use of advanced RTF syntax. 
     to print { and } use /#123 and /#125

RINFO record
-------------
XML needs to have one row marked with `<__datatype>RINFO</__datatype>`
This record contains document wide metadata. 
Some of it is described here:

Bookmark tags:
-------------
* `<__bookmarks_pdf/>` syntax: comma delimited set of `bkN=titleX` or `bkN=titleX-titleY`
ex:
```
bk1=title1,bk2=title3-title5
```

* `<__bookmarks_rtf/>` has the same syntax.

However, Both work differently. rtf does not support titles bookmarked in reversed order (no checks performed in sashiato) (pdf does). 
Providing range or creates a bookmark with concatenated text in PDF, in RTF it simply assings the same style
to all titles in the range.

__dest
------

`__dest = rtf||pdf` (comma or space delimited) if blank or contains app or csr both pdf and rtf are generated

__colwidths
-----------

__colwidths = space delimited list of colwidths 
     A - auto
     N - no wrapping, 
     NH - no wrapping including header,
     LW - longest word, 
     LWH - longest word with header included,
     <number> - fixed number in points, 1in=72 points
     <number>in - fixed number in inches
     <number>cm - fixed number in cm
     if not all columns are provided with instuctions the following defaults are set:
        if no instructions are provided 72 points is set for __col_0 if it is a header column othersize 15 points is set
           and N is set for all remaining columns.
        if only first column is provided all other columns are assumed as N.
        if more than one column is provided the last __colwidths instruction is reapeated for remaining columns
 
Other tags
----------
        
__stretch = space delimited list of stretch instructions (Y|N)
     if fewer instructions are povided than column names N is assumed for the remaining columns

__sfoot_fs = system footer font size

__dist2next = space delimited set of distances in points.  D for default. Default is col_sp is __sprops.

__doffsets = space delimited list of floats (with optional units of measure- in, cm, ch - no space between number and UOM)
             shifts positioning of decimal cells or RD-aligned cells left or right.  This functionality can be very data dependent
             and should be used with care (table should be reviewed when data changes).

__layouttype values STD (default) or INTEXT

__title1_cont = use to specify verbiage appended to repeated title1
tabular titles can be use /ftl and /ftr instructions


NON RINFO records
-----------------

__label_cont = use to specify verbiage appended to repeated titles, tcols, __col_0
__cellborders = pass space delimited list of per-cell instructions. Each instruction can contain letters T, B, R, L, use N for no borders.
                example:  'T RL'   configures frist cell with top border, 2nd with right and left, all other cells will have no border.
                does not work with spanning cells, tcols,
                Cell borders work on header rows, but only R and L (T, B are ingnored) and if they are passed 
                RTF will not use spacing between not border separated header cells.
                
__span_0 with value all,  appears to be undocumented feature which causes __col_0 to span whole row.