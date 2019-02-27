/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;

public class RCDStringUtls {
	static RCDStringUtls self = new RCDStringUtls();

	public static RCDStringUtls getSelf() {
		return self;
	}

	public String[] tokenize(String text, String delimiterText ){
		ArrayList res = new ArrayList();
		if (text==null) {
			return null;
		}
		while(text.length()>0){
		   int index0 = text.indexOf(delimiterText);
		   if (index0==0){
			   String t0="";
			   res.add(t0);
			   text = text.substring(delimiterText.length());
		   } else if (index0>-1){
			   String t0 = text.substring(0, index0);
			   res.add(t0);
			   text = text.substring(index0+delimiterText.length());
		   } else{
			   res.add(text);
			   text="";
		   }
		}
		if (res!=null){
		String[] result = (String[]) res.toArray(new String[0]);
		return result;
		} else return null;
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
    String s = "/t0MedDRA System Organ Class~{super a} /t1 Preferred Term/t2 CTC Grade";
     
     String[] s2 = RCDStringUtls.getSelf().tokenize(s,"/t");
     int x=s2.length;
	}

}
