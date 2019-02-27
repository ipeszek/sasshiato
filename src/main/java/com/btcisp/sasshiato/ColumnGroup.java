/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

public class ColumnGroup {
	private int startingColumn = -1;
	private int endingColumn = -1;
	//private float minimumWidth;
	private float actualWidth;
	static final int UNDEFINED_COLUMN= -1;
//	private float[] columnwidths;

/*
	public float[] getColumnwidths() {
		return columnwidths;
	}

	public void setColumnwidths(float[] columnwidths) {
		this.columnwidths = columnwidths;
	}

	public float[] getColumnHwidths() {
		return columnHwidths;
	}

	public void setColumnHwidths(float[] columnHwidths) {
		this.columnHwidths = columnHwidths;
	}

	public int getStartingHColumn() {
		return startingHColumn;
	}

	public void setStartingHColumn(int startingHColumn) {
		this.startingHColumn = startingHColumn;
	}

	public int getEndingHColumn() {
		return endingHColumn;
	}

	public void setEndingHColumn(int endingHColumn) {
		this.endingHColumn = endingHColumn;
	}
*/
	public int getStartingColumn() {
		return startingColumn;
	}

	public void setStartingColumn(int startingColumn) {
		this.startingColumn = startingColumn;
	}

	public int getEndingColumn() {
		return endingColumn;
	}

	public void setEndingColumn(int endingColumn) {
		this.endingColumn = endingColumn;
	}

	//public float getMinimumWidth() {
	//	return minimumWidth;
	//}

	//public void setMinimumWidth(float minimumWidth) {
	//	this.minimumWidth = minimumWidth;
	//}

	public float getActualWidth() {
		return actualWidth;
	}

	public void setActualWidth(float actualWidth) {
		this.actualWidth = actualWidth;
	}

	}
