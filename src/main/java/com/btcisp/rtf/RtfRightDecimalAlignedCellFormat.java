/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

public class RtfRightDecimalAlignedCellFormat extends RtfParagraphFormat {
	int tabPositionRight;
	int tabPositionDec;
	public void configure(int tabPositionRight, int tabPositonDec, int spaceB, int spaceA, int leading, String ffInstructions){
		super.configure(null, paddingLeft, paddingRight, spaceB, spaceA, leading, -99, ffInstructions);
		this.tabPositionRight = tabPositionRight;
		this.tabPositionDec = tabPositonDec;
	}
	public int getTabPositionDec() {
		return tabPositionDec;
	}
	public void setTabPositionDec(int tabPositionDec) {
		this.tabPositionDec = tabPositionDec;
	}
	public int getTabPositionRight() {
		return tabPositionRight;
	}
	public void setTabPositionRight(int tabPositionRight) {
		this.tabPositionRight = tabPositionRight;
	}
	
	
}
