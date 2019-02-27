/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;

public class NonAsciiScan {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
		String fileName = System.getProperty("file");
		NonAsciiScan me = new NonAsciiScan();
		ArrayList ampers = new ArrayList();
		String[] res = me.findNonAscii(fileName, ampers, System.out);
		if(res.length==0)
			System.out.println("No non-ASCII characters found");
		//else {
		//	StringBuffer sb = new StringBuffer();
		//	for (int i = 0; i < res.length; i++) {
		//		sb.append(res[i]).append("\n");
		//	}
		//	System.out.println("List of lines with non-ASCII characters:\n" + sb);

		//}
		if(ampers.size()==0){
			System.out.println("No & characters found");
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < ampers.size(); i++) {
				sb.append(ampers.get(i)).append("\n");
			}
			System.out.println("List of lines with & characters:\n" + sb);
		
		}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public String[] findNonAscii(String fileName, ArrayList ampersChars, PrintStream out) throws Exception{
		ArrayList al = new ArrayList();
		FileReader reader =null;
		BufferedReader breader = null;
		try{
			reader = new FileReader(fileName);
			breader = new BufferedReader(reader);
			
			String line = "";
			int lineNo=0;
			while(line !=null) {
			     byte bytearray []  = line.getBytes();
			     CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
			     try {
			       CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
			       r.toString();
			     }
			     catch(CharacterCodingException e) {
			       String msg = "Line " + lineNo + " (original txt with non-ASCI chars):" + line;
			       out.println(msg);
			       al.add(msg);
			       //replace non-asci with X
			       String repl_line = line.replaceAll("[^\\p{ASCII}]", "X");
			       String ms2 = "Line " + lineNo + " (     non-ASCI replaced with 'X' ):" + repl_line;
			       out.println(ms2);
			     }
			     if(line.indexOf("&")!= -1){
				       String msg = "Line " + lineNo + ":" + line;
				       // interrupt the processing
				       ampersChars.add(msg);
		    	 
			     }
					line = breader.readLine();
					lineNo++;
	
			}
		
			String[] result = (String[]) al.toArray(new String[0]);
			return result;
		}finally {
			if(breader !=null) breader.close();
			if(reader!=null) reader.close();
			
		}
	}
}
