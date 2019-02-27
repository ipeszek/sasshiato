/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rrg.rcdparser;

import java.util.Map;
import java.util.Set;

public class TestXmlParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 RcdXmlParser p = null;
		 try{
			p = new RcdXmlParser("D:/sas_stuff/Work/xmlfiles/ae.xml");
			p.open();
			System.out.println("INFO " + p.getRcdInfo());
			for(RcdRow line = p.nextRow(); line!=null; line = p.nextRow()){
				System.out.println(" " + line);
				Map allCols = line.getCols();
				for(int i =0; i< line.getColSize(); i++){
					System.out.println("__col_" + i +"=" + allCols.get("__col_" + i));
				}
			}
            p.close();
			System.out.println("SECOND SCAN");
            p.reopen();
			System.out.println("INFO " + p.getRcdInfo());
			for(RcdRow line = p.nextRow(); line!=null; line = p.nextRow()){
				System.out.println(" " + line);
			}            
		 }catch(Exception e){
			e.printStackTrace();
		 }finally{
			if(p!=null) p.close();
		 }

	}

}
