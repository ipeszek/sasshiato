/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import com.lowagie.text.SplitCharacter;
import com.lowagie.text.pdf.PdfChunk;

public class SplitCharacter_Std implements SplitCharacter {

	String splitChars = null;
	public SplitCharacter_Std(ReportLaF laf){
		splitChars = laf.getSplitChars();
	}
	public boolean isSplitCharacter(int start, int current, int end,
			  char[] cc, PdfChunk[] ck) {
			    char c;
			    //String msg= "";
			    if (ck == null) {
			      c = cc[current];
			      //msg = "isspit:" + current+":" + c;
			    } else {
			      c = (char) ck[Math.min(current, ck.length - 1)]
			        .getUnicodeEquivalent(cc[current]);
			      //msg = "isspit2:" + current+":" + c;
			    }
			    boolean res  =splitChars.indexOf(c) !=-1;
			    //if(res) System.out.println(msg +":line=" + new String(cc));			    
			    return res;
			    //return (c == '/' || c == ' ' || c == '-' || c==',');	//make sure this list matches TableWidthCalculator LW logic
    }

}
