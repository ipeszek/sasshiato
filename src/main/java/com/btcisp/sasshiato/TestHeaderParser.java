/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;

public class TestHeaderParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
    String[] header = new String[] {"A","B","B","C","C","C","C"};
    		 
     int counter = 0;
     int startc=0;
     int endc=0;
     
     ArrayList newHeaderText = new ArrayList();
     ArrayList startCols = new ArrayList();
     ArrayList endCols = new ArrayList();
     
     String s = header[counter];
     counter++;
     while(counter< header.length) {
    	 
    	 System.out.println(" "+counter);
    	 String s1 = header[counter];
    	 if (s1.equals(s)){
    		 endc++;
    	 }else {
    		 newHeaderText.add(s);
    		 System.out.println("added "+s + " counter= "+counter + " start, end: " + startc + ","+endc);
    		 startCols.add(new Integer(startc));
    		 endCols.add(new Integer(endc));
    		 startc=counter;
    		 endc=counter;
    		 s = s1;
    	 }
    
    	 counter++;
    	 if (counter==header.length){
    		 newHeaderText.add(s);
    		 startCols.add(new Integer(startc));
    		 endCols.add(new Integer(endc));
    	 }	 
     }
     System.out.println("done");
	 for (int i = 0; i < header.length; i++) {
		 System.out.print(" " + header[i]);
		
	}
	 System.out.println("" );
     for (int j = 0; j < newHeaderText.size(); j++) {
	
     System.out.println("" + (String) newHeaderText.get(j) + " cols " +(Integer) startCols.get(j)
    		+ " to " + (Integer) endCols.get(j));
     }
	}

}
