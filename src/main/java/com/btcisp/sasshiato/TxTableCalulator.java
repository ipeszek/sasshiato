/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

public class TxTableCalulator {
	private static TxTableCalulator self = new TxTableCalulator();
	public static TxTableCalulator getSelf(){
		return self;
	}
	
	public float[] calculateTableWidths(TxTableFormat format, float totWidth, String[] artifacts){
		return null;
	}
}
