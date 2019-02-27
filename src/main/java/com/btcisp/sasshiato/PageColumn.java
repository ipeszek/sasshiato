/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

public class PageColumn {
private int columnID;
private float columnWidth;
private float beforeStreatchColumnWidth;
private boolean stretchAllowed;

public int getColumnID() {
	return columnID;
}

//This is initial width after PageGroup contains final adjusted widths.
public float getColumnWidth() {
	return columnWidth;
}

public boolean isStretchAllowed(){
	return stretchAllowed;
}
public PageColumn(int columnID, float columnWidth, boolean stretchAllowed) {
	super();
	this.columnID = columnID;
	this.columnWidth = columnWidth;
	this.beforeStreatchColumnWidth = columnWidth;
	this.stretchAllowed = stretchAllowed;
}
public PageColumn(int columnID, float columnWidth, float beforeStreatchColumnWidth , boolean stretchAllowed) {
	super();
	this.columnID = columnID;
	this.columnWidth = columnWidth;
	this.beforeStreatchColumnWidth = beforeStreatchColumnWidth;
	this.stretchAllowed = stretchAllowed;
}

public float getNumericColumnPadding(){
	if(beforeStreatchColumnWidth < columnWidth)
       return (columnWidth- beforeStreatchColumnWidth) /2;
	else 
		return 0;
}
public String toString(){
	return "C:" + columnID + " [" + columnWidth + "]";
}
}
