/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

public class RtfDecimalAlignedCellFormat extends RtfParagraphFormat {
	int tabPosition;
	public void configure(int tabPosition, int spaceB, int spaceA, int leading, String ffInstructions){
		super.configure(null, -99, -99, spaceB, spaceA, leading, -99, ffInstructions);
		this.tabPosition = tabPosition;
	}
	public int getTabPosition() {
		return tabPosition;
	}
	public void setTabPosition(int tabPosition) {
		this.tabPosition = tabPosition;
	}

	
}
