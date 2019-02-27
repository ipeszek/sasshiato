/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;

import com.btcisp.rrg.rcdparser.RcdRow;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;

public class HeaderRow {
	private ArrayList headerCells;
	private ArrayList startColumns;
	private ArrayList endColumns;
	private ArrayList alignments;
	private ArrayList<Integer> borders;
	private float rowId;

	public HeaderRow(ArrayList<StyledText> headerCells, ArrayList startColumns,
			ArrayList endColumns, ArrayList alignments, ArrayList<Integer> borders, float rowId) {
		super();
		this.headerCells = headerCells;
		this.startColumns = startColumns;
		this.endColumns = endColumns;
		this.alignments = alignments;
		this.rowId = rowId;
		this.borders = borders;
	}

	public float getRowId(){
		return rowId;
	}
	public StyledText[] getHeaderCells() {
		StyledText[] result = (StyledText[]) headerCells.toArray(new StyledText[0]);
		return result;
	}

	public String[] getAlignments() {
		String[] result = (String[]) alignments.toArray(new String[0]);
		for (int i = 0; i < result.length; i++) {
			if (!"R".equals(result[i]) && !"L".equals(result[i])) result[i]="C";
		}
		return result;
	}
	
	public int[] getAlignmentsInt() {
		String[] result = (String[]) alignments.toArray(new String[0]);
		int[] result2 = new int[result.length];
		for (int i = 0; i < result.length; i++) {
			if ("R".equals(result[i])) result2[i]=PdfPCell.ALIGN_RIGHT;
			else if ("L".equals(result[i])) result2[i]=PdfPCell.ALIGN_LEFT;
			else result2[i]=PdfPCell.ALIGN_CENTER;
		}
		return result2;
	}

	public int[] getStartColumns() {
		Integer[] result = (Integer[]) startColumns.toArray(new Integer[0]);
		int[] result2 = new int[result.length];
		for (int i = 0; i < result.length; i++) {
			result2[i]=result[i].intValue();
		}
		return result2;
	}

	public int[] getEndColumns() {
		Integer[] result = (Integer[]) endColumns.toArray(new Integer[0]);
		int[] result2 = new int[result.length];
		for (int i = 0; i < result.length; i++) {
			result2[i]=result[i].intValue();
		}
		return result2;

	}
	public int[] getBorderInstructions() {
		Integer[] result = (Integer[]) borders.toArray(new Integer[0]);
		int[] result2 = new int[result.length];
		for (int i = 0; i < result.length; i++) {
			result2[i]=result[i].intValue();
		}
		return result2;

	}

	//sets both left and right border info
	public static int figureOutBorderInstructions( RcdRow row,  int startColumn, int endColum) {
		int leftBorder = Rectangle.LEFT & row.getColumnBorderInstructions(startColumn);
		if(leftBorder == 0 && startColumn > 0) {
			if((Rectangle.RIGHT & row.getColumnBorderInstructions(startColumn - 1)) != 0){
				leftBorder = Rectangle.LEFT;
			}
		}
		int rightBorder = Rectangle.RIGHT & row.getColumnBorderInstructions(endColum);
		if(rightBorder == 0 && row.getColSize() > endColum + 1) {
			if((Rectangle.LEFT & row.getColumnBorderInstructions(endColum + 1)) != 0) {
				rightBorder = Rectangle.RIGHT;
			}
		}
		int res = leftBorder | rightBorder;
		return res;
	}
}
