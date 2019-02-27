/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;


public class HeaderTablet {
     private ArrayList<HeaderRow> headerRows = new ArrayList<HeaderRow>();
     //ArrayList intitialFullHeader = new ArrayList();
     //ArrayList intitialFullHeaderAlignments = new ArrayList();
     private boolean _requiresVarByGroupHeading;
     private StyledText _currentVarByGroupHeading;
     private int _currentVarByGroupId = -1;
         
     public boolean requiresVarByGroupHeading(){
    	 return _requiresVarByGroupHeading;
     }
     public StyledText getCurrentVarByGroupHeading(){
    	 return _currentVarByGroupHeading;
     }
     public int getCurrentVarByGroupId(){
    	 return _currentVarByGroupId;
     }
     public HeaderRow[] getHeaderRows(){
    	 HeaderRow[] result= (HeaderRow[]) headerRows.toArray(new HeaderRow[0]);
    	 return result;
     }
     
     
     /*
     public void inspect(RcdRow row){
    	 if ("HEAD".equals(row.getDatatype())) 
    		 //addHeaderRowOld(row); 
    		 addHeaderRowOld(row, );
    	 
     }
     */
     public void clearHeaderRows(){
    	 headerRows.clear();
    	 lastHr = null;
     }
     

     HeaderRow lastHr = null;
     public void addHeaderRow( RcdRow row, int[] colids, ReportLaF laf) throws InvalidStylingSyntaxException{
 	    // need array of cells (text) , and alignments;
 	    //int numberOfColumns = row.getColSize();
    	if(row.getVarByGroup()>-1) {
    		_requiresVarByGroupHeading= true;
    		_currentVarByGroupId = row.getVarByGroup();
    		_currentVarByGroupHeading = row.getVarByLab(laf);
    	}
    	int numberOfColumns = colids.length; 
 	    //Map allCols= row.getCols();
    	StyledText[] header = new StyledText[numberOfColumns];
		for (int j = 0; j < numberOfColumns; j++) {
			header[j] = row.getColumAsStyledText(colids[j], ReportLaF.STYLE_TYPE_HEADER, laf);
		}
		int counter = 0;
		int startc = 0;
		int endc = 0;

		ArrayList<StyledText> newHeaderText = new ArrayList<StyledText>();
		ArrayList startCols = new ArrayList();
		ArrayList endCols = new ArrayList();
		ArrayList alignH = new ArrayList();
		ArrayList<Integer> borders = new ArrayList<Integer>();
		String[] allAligns = row.getAllAligns();
		//


		//
		StyledText s = header[counter];
		counter++;
		while (counter < header.length) {
			int starcid = colids[startc];
			int lastInx= -1;
			int lastEndCId = -1;
			if(lastHr!=null) {
				int[] lastStarts = lastHr.getStartColumns();
				for(int i=0; i<lastStarts.length; i++){
					int ls = lastStarts[i];
					if(ls <= starcid) lastInx = i;
					else
						break;
				}
				if(lastInx != -1){
					lastEndCId = lastHr.getEndColumns()[lastInx];
				}
			}

			StyledText s1 = header[counter];
			if(s1==null) {
				SasshiatoTrace.logError( "Unexpected null header record, err:HeaderTablet:001 - please contact BTCISP if you see this message, counter= " + counter + ",row=" + row);
				s1=StyledText.EMPTY;
			}
			if (s1.equalsAsText(s) && (lastEndCId ==-1 || colids[endc+1] <= lastEndCId)) {
				endc++;
			} else {
				int endcid = colids[endc];
				newHeaderText.add(s);
				startCols.add(new Integer(starcid));
				endCols.add(new Integer(endcid));
				int borderInst = HeaderRow.figureOutBorderInstructions(row, starcid, endcid);
				borders.add(borderInst);

				//if (startc!=endc){
				//	alignH.add(new String("C"));
				//}
				//else {
				if(allAligns.length>startc)
					alignH.add(allAligns[colids[startc]]);
				else {
					SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Defaulting missing alignment for header text " + s);
					alignH.add("C");
				}
				//}
				startc = counter;
				endc = counter;
				s = s1;
			}
			counter++;
		}
		if (counter == header.length) {
			newHeaderText.add(s);
			startCols.add(new Integer(colids[startc]));
			endCols.add(new Integer(colids[endc]));
			int borderInst = HeaderRow.figureOutBorderInstructions(row, colids[startc], colids[endc]);
			borders.add(borderInst);
			//if (startc!=endc){
			//	alignH.add(new String("C"));
			//}
			//else {
			if(allAligns.length>startc)
				alignH.add(allAligns[colids[startc]]);
			else  {
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Defaulting missing alignment for header text " + s);
				alignH.add("C");
			}
			//}								
		}

		
		HeaderRow hr = new HeaderRow(newHeaderText, startCols, endCols, alignH, borders, row.getRowid());
		lastHr = hr;
        headerRows.add(hr);

  }
     
  
}
