/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.HashMap;
import java.util.StringTokenizer;

import com.btcisp.utils.StringUtil;

public class TxTableFormatParser {
	
	private static TxTableFormatParser self = new TxTableFormatParser();
	public static TxTableFormatParser getSelf() {
		return self;
	}
	
	HashMap txFormatsS = new HashMap();
	HashMap txFormats = new HashMap();
	
	public void load(String allFormats) throws Exception {
		StringTokenizer st = new StringTokenizer(allFormats, "{}");
		while(st.hasMoreTokens()) {
			String name = st.nextToken().trim();
			if(st.hasMoreTokens()) {
				String format = st.nextToken();
				loadFormat(name, format);
			}
			else {
				loadFormat(name, "");
			}
		}
	}
	
	public void loadFormat(String name, String format) throws Exception{
		txFormatsS.put(name, format);
		calculate(name);
	}

	private void calculate(String name) throws Exception{
		String format = (String) txFormatsS.get(name);
		if(format ==null) throw new Exception("Tx Calcuation Error: No format set for "  + name);
		
		format = StringUtil.replace(format, "/tx", "/"); //temporary mapping
		StringTokenizer st = new StringTokenizer(" " + format + " ", "/");
		int columns= st.countTokens();
		TxTableFormat tformat = new TxTableFormat();
		tformat.init(columns);
		int i=0;
		while(st.hasMoreTokens()){
			String instruction = st.nextToken();
			String w = parseInstruction(instruction, "w=");
			String a = parseInstruction(instruction, "a=");
			if(w!=null) {
				tformat.setWidthInstruction(i, w);
			}
			if(a!=null) {
				tformat.setAlignment(i, a);
			}
			i++;
		}
		txFormats.put(name, tformat);
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Tx format for " + name + " is " + tformat);
		
		
	}
	
	
	
	private String parseInstruction(String s, String instTemplate) throws Exception{
		int inx = s.indexOf(instTemplate);
		if(inx==-1) return null;
		else{
			String inst = s.substring(inx+instTemplate.length());
			inst = inst.trim();
			int inx2=inst.indexOf(" ");
			if(inx2==-1) {
				return inst.toUpperCase();
			} else {
				return inst.substring(0, inx2).toUpperCase();
			}
		}	
	}
	public TxTableFormat getFormat(String name){
		return (TxTableFormat) txFormats.get(name);
	}


}
