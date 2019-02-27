/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.utils.sasxml;

import java.io.FileInputStream;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.btcisp.utils.sasxml.SasXmlObservation;



public class SasXmlParser {
	
    private String fileName = null;
    SasXmlObservationFactory factory = null;
	private XMLStreamReader xmlr = null;
	private FileInputStream fis = null;
	private boolean eof  =false;
	private boolean inTable=false;
	private boolean inRecord=false;
	private String tableName= null;
     
    public SasXmlParser(){
    }
    
	public SasXmlParser(String afileName, SasXmlObservationFactory afactory) throws Exception{
		openDocument(afileName); 
        setSasXmlObservationFactory(afactory);
    } 
	
	public void openDocument(String afileName) throws Exception{
        fileName = afileName;
		XMLInputFactory inputFactory= XMLInputFactory.newInstance();
        fis = new FileInputStream(afileName);
    	xmlr = inputFactory.createXMLStreamReader(afileName,fis); 	
    }
    
	public void setSasXmlObservationFactory(SasXmlObservationFactory afactory){
    	factory = afactory;
    }
    
    public void closeDocument(){
    	if(xmlr!=null) try{ xmlr.close(); xmlr=null;} catch(Exception e){e.printStackTrace();}
    	if(fis!=null)  try{ fis.close(); fis=null;} catch(Exception e){e.printStackTrace();}
    	eof  =false;
    	inTable=false;
    	inRecord=false;
    	tableName= null;
   }
    
    private SasXmlObservation createObservation() throws Exception{
    	return factory.createObservation();
    }
    public SasXmlObservation nextObservation() throws Exception{
 	   int eventType = xmlr.getEventType();
 	   String currentVariableName = null;
	   String currentVariableText = null;
 	   String lastVariableName = null;
	   String lastVariableText = null;
	   SasXmlObservation line = createObservation();
	   boolean dataFound=false;
       while(true) {   	    
   	        switch(eventType){
	        	    case XMLStreamConstants.START_DOCUMENT:
	        	    	break;
	   	        	case XMLStreamConstants.END_DOCUMENT:
	   	        		eof=true;
	   	        	    break;
	   	        	case XMLStreamConstants.START_ELEMENT:
	   	        		currentVariableText =null;
	   	        		currentVariableName = null;
	   	        		if("TABLE".equals(xmlr.getLocalName())){
	   	        			inTable=true;
	   	        		} else if (inTable && !inRecord){
	   	        		    inRecord=true;
	   	        			if(tableName==null) tableName= xmlr.getLocalName();
	   	        		} else {
	   	        			//record reading
	   	        			currentVariableName= xmlr.getLocalName();
	   	        		}
	   	        		break;
	   	        	case XMLStreamConstants.CHARACTERS:
	   	        		if(currentVariableName!=null) 
	   	        			currentVariableText = xmlr.getText();
	   	        		break;
	   	        	case XMLStreamConstants.END_ELEMENT:
	   	        		String locName = xmlr.getLocalName();
	   	        		if("TABLE".equals(locName)){
	   	        			inTable=false;
	   	        		} else if (inTable && tableName!=null && tableName.equals(locName)){
	   	        		    inRecord=false;
	   	        		    break;
	   	        		} else if(currentVariableName!=null && currentVariableName.equals(locName)){
	   	        			//record reading
	   	        			line.populateVariableValue(currentVariableName, currentVariableText);
	   	        			dataFound=true;
	   	        			lastVariableName = currentVariableName;
	   	        			lastVariableText = currentVariableText;
	   	        			currentVariableText=null;
	   	        			currentVariableName=null;
	   	        		}
	   	        		break;
	   	        }
        	
	        	if (eof || (!inRecord && dataFound)) break;
	        	try{
	        	eventType = xmlr.next();  
	        	}catch(Exception e){
	        		Location loc = null;
	        		try{
	        			loc=xmlr.getLocation();
	        		}catch(Exception ex){}
	        		//e.printStackTrace();
	        		throw new SasParserException("Error when parsing xml, parsed so far: " + line + ", parsing:" + currentVariableName + "=" + currentVariableText + ", prev:" + lastVariableName + "=" + lastVariableText + " reproted loc=" + loc, e);
	        	}
	        } 
       if (dataFound) return line;
       else return null;
    }
    
    
    
 
    
    
    public final static String getEventTypeString(int eventType) {
        switch (eventType) {
            case XMLEvent.START_ELEMENT:
                return "START_ELEMENT";
            case XMLEvent.END_ELEMENT:
                return "END_ELEMENT";
            case XMLEvent.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLEvent.CHARACTERS:
                return "CHARACTERS";
            case XMLEvent.COMMENT:
                return "COMMENT";
            case XMLEvent.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLEvent.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLEvent.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLEvent.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLEvent.DTD:
                return "DTD";
            case XMLEvent.CDATA:
                return "CDATA";
            case XMLEvent.SPACE:
                return "SPACE";
        }
        return "UNKNOWN_EVENT_TYPE , " + eventType;
    }

}
