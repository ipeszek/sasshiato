/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;

public class TxTableFormat {
	int numberOfColumns;
	String[] aligments;
	String[] widthInstructions;
	
	public void init(int anumberOfColumns){
		numberOfColumns = anumberOfColumns;
		aligments = new String[anumberOfColumns];
		widthInstructions = new String[anumberOfColumns];
		
		//set defaults
		for (int i = 0; i < aligments.length; i++) {
			if(i==0) aligments[i] = "L";
			else if(i==aligments.length-1) aligments[i] = "R";
			else aligments[i] = "C";
		}
		float size = 100f / (float) numberOfColumns;
		for (int i = 0; i < widthInstructions.length; i++) {
			if(i==0) aligments[i] = size + "%";
			else if(i==aligments.length-1) aligments[i] = "R";
			else aligments[i] = size + "%";			
		}
	}

	public void setAlignment(int column, String align){
		aligments[column] = align;
	}
	
	public void setWidthInstruction(int column, String instruction){
		widthInstructions[column] = instruction;
	}
	
	public int getNumberOfColumns() {
		return numberOfColumns;
	}
	public String getAlignment(int column){
		return aligments[column];
	}
	public String getWidthInstruction(int column){
		return widthInstructions[column];
	}
	
	public String toString(){
		StringBuffer res = new StringBuffer();
		res.append("TxFormat[").append(numberOfColumns).append("]").append("\r\n");
		for (int i = 0; i < widthInstructions.length; i++) {
			res.append(i).append("=").append("a[").append(aligments[i]).append("] ").append("w[").append(widthInstructions[i]).append("]\r\n");		
		}
		return res.toString();
	}
}
