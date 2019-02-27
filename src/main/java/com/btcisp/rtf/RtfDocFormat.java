/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

public class RtfDocFormat {
	public static final String COMPATIBILITY_NONE= "None";
	public static final String PAPER_SIZE_LETTER= "letter";
	public static final String PAPER_SIZE_A4 = "A4";
	public static final String ORIENT_P = "P";
	public static final String ORINET_L = "L";
	
	int fontSizeRTF = 20;
	int leftMRTF = 1800;
	int rightMRTF = 1800;
	int topMRTF = 1440;
	int bottomMRTF = 1440;
	int fontid = 0;
	int topparagraphleadning;
	String orient = "P";
	String paperSize = "letter";
	private String compatibility=COMPATIBILITY_NONE;
	
	int derivedHeight;
	int derivedWidth;
	boolean includeHeaderStylesheet = false;
	
	
	public RtfDocFormat() {
		super();
	}
	public boolean isIncludeHeaderStylesheet() {
		return includeHeaderStylesheet;
	}
	public void setIncludeHeaderStylesheet(boolean includeHeaderStylesheet) {
		this.includeHeaderStylesheet = includeHeaderStylesheet;
	}
	public int getDerivedHeight() {
		return derivedHeight;
	}
	public void setDerivedHeight(int derivedHeight) {
		this.derivedHeight = derivedHeight;
	}
	public int getDerivedWidth() {
		return derivedWidth;
	}
	public void setDerivedWidth(int derivedWidth) {
		this.derivedWidth = derivedWidth;
	}
	public String getCompatibility() {
		return compatibility;
	}
	public void setCompatibility(String compatibility) {
		this.compatibility = compatibility;
	}
	public int getTopparagraphleadning() {
		return topparagraphleadning;
	}
	public void setTopparagraphleadning(int topparagraphleadning) {
		this.topparagraphleadning = topparagraphleadning;
	}
	public int getFontSizeRTF(){
		return fontSizeRTF;
	}
	public int getFontId() {
		return fontid;
	}
	public String getOrient() {
		return orient;
	}
	public String getPaperSize() {
		return paperSize;
	}
	public int getBottomMRTF() {
		return bottomMRTF;
	}
	public int getLeftMRTF() {
		return leftMRTF;
	}
	public int getRightMRTF() {
		return rightMRTF;
	}
	public int getTopMRTF() {
		return topMRTF;
	}
	public void setBottomMRTF(int bottomMRTF) {
		this.bottomMRTF = bottomMRTF;
	}
	public void setFontId(int fontid) {
		this.fontid = fontid;
	}
	public void setFontSizeRTF(int fontSizeRTF) {
		this.fontSizeRTF = fontSizeRTF;
	}
	public void setLeftMRTF(int leftMRTF) {
		this.leftMRTF = leftMRTF;
	}
	public void setOrient(String orient) {
		this.orient = orient;
	}
	public void setPaperSize(String paperSize) {
		this.paperSize = paperSize;
	}
	public void setRightMRTF(int rightMRTF) {
		this.rightMRTF = rightMRTF;
	}
	public void setTopMRTF(int topMRTF) {
		this.topMRTF = topMRTF;
	}
	
}
