/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

public class RtfParagraphFormat {
	public static final String ALIGN_LEFT = "\\ql";
	public static final String ALIGN_RIGHT = "\\qr";
	public static final String ALIGN_CENTER = "\\qc";
	int paddingLeft  = 0;
	int paddingRight = 0;
	String align;
	int spaceBefore = -99;
	int spaceAfter  = -99;
	int leading = -99;
	int indentSize = 100;
	String barinstructions = "";
	String ffInstructions;
	String extraInstructions = "";
	String style= null; //used for bookmarking
	
	public void configure(String align, int paddingLeft, int paddingRight, int spaceB, int spaceA, int leading, int indentSize, String affInstructions){
		this.align= align;
		this.paddingLeft = paddingLeft;
		this.paddingRight =paddingRight;
		this.spaceAfter = spaceA;
		this.spaceBefore = spaceB;
		this.leading = leading;
		this.ffInstructions = affInstructions;
		if(indentSize!=-99) this.indentSize =indentSize;
	}
	

	public void setExtraInstructions(String inst){
		extraInstructions = inst;
	}
	public String getExtraInstructions(){
		return extraInstructions;
	}
	public void setBarAbove(){
		barinstructions = "\\brdrt\\brdrs";
	}
	
	public void unsetBarAbove(){
		barinstructions = "";
	}
	
	public int getIndentSize() {
		return indentSize;
	}

	public String getBarInstructions(){
		return barinstructions;
	}

	public void setIndentSize(int indentSize) {
		this.indentSize = indentSize;
	}


	public String getFFInstrutions(){
		return null; //font face has been moved to chunks
	}
	
	public int getLeading() {
		return leading;
	}


	public void setLeading(int leading) {
		this.leading = leading;
	}


	public int getSpaceAfter() {
		return spaceAfter;
	}

	public void setSpaceAfter(int spaceAfter) {
		this.spaceAfter = spaceAfter;
	}

	public int getSpaceBefore() {
		return spaceBefore;
	}

	public void setSpaceBefore(int spaceBefore) {
		this.spaceBefore = spaceBefore;
	}

	public String getAlign() {
		return align;
	}
	public void setAlign(String align) {
		this.align = align;
	}
	public int getPaddingLeft() {
		return paddingLeft;
	}
	public void setPaddingLeft(int paddingLeft) {
		this.paddingLeft = paddingLeft;
	}
	public int getPaddingRight() {
		return paddingRight;
	}
	public void setPaddingRight(int paddingRight) {
		this.paddingRight = paddingRight;
	}


	public String getStyle() {
		return style;
	}


	public void setStyle(String style) {
		this.style = style;
	}
	
}
