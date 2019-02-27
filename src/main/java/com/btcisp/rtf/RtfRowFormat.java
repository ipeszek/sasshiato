/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.utils.StringUtil;

public class RtfRowFormat {
   public static final String ROW_ALIGN_TOP = "\\clvertalt";
   public static final String ROW_ALIGN_BOTTOM = "\\clvertalb";
   public static final String ROW_ALIGN_CENTER = "\\clvertalc";
   public static final String TABLE_ALIGN_RIGHT = "\\trqr";
   public static final String TABLE_ALIGN_LEFT = "\\trql";
   public static final String TABLE_ALIGN_CENTER = "\\trqc";
  public static final String BORDER_TOP = "\\clbrdrt";
   public static final String BORDER_BOTTOM = "\\clbrdrb";
   public static final String BORDER_LEFT = "\\clbrdrl";
   public static final String BORDER_RIGHT = "\\clbrdrr";
   public static final String BORDER_NONE = "";
  int[] rightreach;
   String tableAlign= "";
   String[] aligns;
   String[] borders;
   int cellgap = 0;
   int borderwidth=10;
   int height = -99;
   String partialBorders; //example "0,1-2"
   boolean ispartialborder;
   int[] colwidths;
   boolean isRepeatable;
   
   boolean usePartialBorderSizing=false;
   int[] partialBordersSizeLeft;
   int[] partialBorderSizeRight;
   
   
public String[] getAligns() {
	return aligns;
}
public void setAligns(String[] aligns) {
	this.aligns = aligns;
}
public int[] getRigthReachCoordinates() {
	return rightreach;
}
public int[] getColWidths(){
	return colwidths;
}
public void configure(int[] colwidths) {
	this.colwidths= colwidths;
	this.rightreach = new int[colwidths.length];
	int total = 0;
	for (int i = 0; i < colwidths.length; i++) {
		total = total + colwidths[i];
		rightreach[i] = total;
	}
}
public void configure(int[] colwidths, String tableAlign, String align, String border, boolean isRepeatable) {
	configure(colwidths);
	if(tableAlign==null) tableAlign="";
	this.tableAlign = tableAlign;
	if(!StringUtil.isEmpty(align)){
		aligns = new String[colwidths.length];
		for (int i = 0; i < aligns.length; i++) {
			 aligns[i] = align;
		}
	}
	if(!StringUtil.isEmpty(border)){
		borders = new String[colwidths.length];
		for (int i = 0; i < borders.length; i++) {
			borders[i] = border;
		}
	} else {
		borders=null;
	}
	this.isRepeatable = isRepeatable;
	
}

public boolean isRepeatable(){
	return isRepeatable;
}

public String getTableAlign() {
	return tableAlign;
}
public void setTableAlign(String tableAlign) {
	this.tableAlign = tableAlign;
}
public boolean hasAligns(){
	return aligns !=null;
}

public boolean hasBorders(){
	return borders !=null;
}
public int getCellgap() {
	return cellgap;
}
public void setCellgap(int cellgap) {
	this.cellgap = cellgap;
}
public String[] getBorders() {
	return borders;
}
public void setBorders(String[] borders) {
	this.borders = borders;
}

public void setBorder(int col, String border) {
	if(this.borders == null) {
		borders = new String[this.colwidths.length];
		for (int i = 0; i < borders.length; i++) {
		   borders[i] = BORDER_NONE;			
		}
	}
	if(col < borders.length) {
		borders[col] = border;		
	} else {
		SasshiatoTrace.logError("Attempt to set border " + border + " on a non-existing column " + col + ", expecting " + borders.length + " columns");
	}
}
public int getBorderwidth() {
	return borderwidth;
}
public void setBorderwidth(int borderwidth) {
	this.borderwidth = borderwidth;
}
public int getHeight() {
	return height;
}
public void setHeight(int height) {
	this.height = height;
}
public String getPartialBorders() {
	return partialBorders;
}
private void setPartialBorders(String partialBorders) {
	this.partialBorders = partialBorders;
}
public void setPartialBorders(boolean partial){
	ispartialborder = partial;
}
public boolean isPartialBorder(){
	return ispartialborder;
}
public int[] getPartialBorderSizeRight() {
	return partialBorderSizeRight;
}
public void setPartialBorderSizeRight(int[] partialBorderSizeRight) {
	this.partialBorderSizeRight = partialBorderSizeRight;
}
public int[] getPartialBordersSizeLeft() {
	return partialBordersSizeLeft;
}
public void setPartialBordersSizeLeft(int[] partialBordersSizeLeft) {
	this.partialBordersSizeLeft = partialBordersSizeLeft;
}
public boolean usePartialBorderSizing() {
	return usePartialBorderSizing;
}
public void setUsePartialBorderSizing(boolean usePartialBorderSizing) {
	this.usePartialBorderSizing = usePartialBorderSizing;
}


}
