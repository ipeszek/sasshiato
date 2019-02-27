/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

public class Column extends ColumnGroup {
	public Column(int columnNumber, float actualWidth) {
		super();
		this.setStartingColumn(columnNumber);
		this.setEndingColumn(columnNumber);
		//this.setMinimumWidth(0);
		this.setActualWidth(actualWidth);	
		
	}




	
}