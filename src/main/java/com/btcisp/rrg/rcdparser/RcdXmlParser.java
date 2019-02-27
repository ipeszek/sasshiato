/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rrg.rcdparser;

import java.io.FileInputStream;


import com.btcisp.utils.sasxml.SasParserException;
import com.btcisp.utils.sasxml.SasXmlObservation;
import com.btcisp.utils.sasxml.SasXmlObservationFactory;
import com.btcisp.utils.sasxml.SasXmlParser;



public class RcdXmlParser {
	
    private String fileName = null;
    RcdInfo rcdInfo = null;
    SasXmlParser xmlUtil = new SasXmlParser();
    
    public RcdXmlParser(String afileName){
    	xmlUtil.setSasXmlObservationFactory(new SasXmlObservationFactory(){
			public SasXmlObservation createObservation() throws Exception {
				return new RcdRow();
			}   		
    	});
    	fileName = afileName;
    }
    
    public RcdInfo getRcdInfo(){
    	return rcdInfo;
    }
    
    public void open() throws Exception{
    	xmlUtil.openDocument(fileName);
    	RcdRow lastRow=null;
    	try{
		for(RcdRow row = (RcdRow) xmlUtil.nextObservation(); row!=null; row = (RcdRow) xmlUtil.nextObservation()){
			if("RINFO".equals(row.getDatatype())){
				rcdInfo = new RcdInfo();
				rcdInfo.loadMap(row.getOther());
				break;
			} else {
				lastRow = row;
			}
		}
    	}catch(Exception e){
    		throw new SasParserException("XML parsing error (re-run with debug=101 for more info), error during scan for RCD info, last successfully parsed row=" + lastRow, e);
    	}
		xmlUtil.closeDocument();
		xmlUtil.openDocument(fileName);
    }
    
    public void reopen() throws Exception{
    	xmlUtil.openDocument(fileName);
    }  
    
    public RcdRow nextRow() throws Exception{
    	RcdRow row= (RcdRow) xmlUtil.nextObservation();
    	if(row!=null && "RINFO".equals(row.getDatatype()))
    		row=(RcdRow) xmlUtil.nextObservation();
    	return row;
    }
    
    public void close(){
    	xmlUtil.closeDocument();
    }
    

}
