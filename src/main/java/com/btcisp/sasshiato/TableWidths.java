/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

public class TableWidths {
	//float fforcedPadding[] ; //padding that has to be added because static column size was requested
	private float originalWidths[] ; //widths used in vertical page break and streaching calcuation
	private float actualWidths[] ; //widths used in vertical page break and streaching calcuation
	private float actualWidths1[] ; //used for RD alignment for the R part, not includes extra padding from F, streach, etc.
	private float actualWidths2[] ; //used for RD alignment for the D part
	private float extraWidths[]; //D or RD padding that has to be added because static column size was requested
	                     //or because non-numeric widths are pushing the size up
	private float preferredWidths[]; //D or RD padding that has to be added because static column size was requested
	
	private float[] maxWidths = null; //not used
	private float[] maxWidthsB4Decimal = null; //used to calc padding to get decimal alignment
	private float[] maxWidthsB4Decimal1 = null; //used to calc padding to get decimal alignment
	private float[] maxWidthsB4Decimal2 = null; //used to calc padding to get decimal alignment
	private float[] maxWidthsAfterDecimal = null;	 //not used
	private float[] includedCellPaddingAdjustements = null;
	private int[] actualWidthsInChars = null;
	//private float[] offsets = null;
	
	public TableWidths() {
		super();
	}
	

	public int[] getActualWidthsInChars() {
		return actualWidthsInChars;
	}


	public void setActualWidthsInChars(int[] actualWidthsInChars) {
		this.actualWidthsInChars = actualWidthsInChars;
	}


	public float[] getIncludedCellPaddingAdjustements() {
		return includedCellPaddingAdjustements;
	}


	public void setIncludedCellPaddingAdjustements(
			float[] includedCellPaddingAdjustements) {
		this.includedCellPaddingAdjustements = includedCellPaddingAdjustements;
	}


	public float[] getOriginalWidths() {
		return originalWidths;
	}


	public void setOriginalWidths(float[] originalWidths) {
		this.originalWidths = originalWidths;
	}


	//public float[] getFForcedPadding() {
	//	return fforcedPadding;
	//}
	public float[] getActualWidths() {
		return actualWidths;
	}
	public void setActualWidths(float[] actualWidths) {
		this.actualWidths = actualWidths;
	}
	public float[] getActualWidths1() {
		return actualWidths1;
	}
	public void setActualWidths1(float[] actualWidths1) {
		this.actualWidths1 = actualWidths1;
	}
	public float[] getActualWidths2() {
		return actualWidths2;
	}
	public void setActualWidths2(float[] actualWidths2) {
		this.actualWidths2 = actualWidths2;
	}
	public float[] getExtraWidths() {
		return extraWidths;
	}
	public void setExtraWidths(float[] extraWidths) {
		this.extraWidths = extraWidths;
	}
	//private float[] getMaxWidths() {
	//	return maxWidths;
	//}
	public void setMaxWidths(float[] maxWidths) {
		this.maxWidths = maxWidths;
	}
	public float[] getMaxWidthsB4Decimal() {
		return maxWidthsB4Decimal;
	}
	public void setMaxWidthsB4Decimal(float[] maxWidthsB4Decimal) {
		this.maxWidthsB4Decimal = maxWidthsB4Decimal;
	}
	public float[] getMaxWidthsB4Decimal1() {
		return maxWidthsB4Decimal1;
	}
	public void setMaxWidthsB4Decimal1(float[] maxWidthsB4Decimal1) {
		this.maxWidthsB4Decimal1 = maxWidthsB4Decimal1;
	}
	public float[] getMaxWidthsB4Decimal2() {
		return maxWidthsB4Decimal2;
	}
	public void setMaxWidthsB4Decimal2(float[] maxWidthsB4Decimal2) {
		this.maxWidthsB4Decimal2 = maxWidthsB4Decimal2;
	}
	//private float[] getMaxWidthsAfterDecimal() {
	//	return maxWidthsAfterDecimal;
	//}
	public void setMaxWidthsAfterDecimal(float[] maxWidthsAfterDecimal) {
		this.maxWidthsAfterDecimal = maxWidthsAfterDecimal;
	}

	public float[] getMaxWidthsAfterDecimal() {
		return maxWidthsAfterDecimal;
	}
	public float[] getPreferredWidths() {
		return preferredWidths;
	}


	public void setPreferredWidths(float[] preferredWidths) {
		this.preferredWidths = preferredWidths;
	}


	//public float[] getOffsets() {
	//	return offsets;
	//}


	//public void setOffsets(float[] offsets) {
	//	this.offsets = offsets;
	//}

}
