/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.HashMap;
import java.util.Map;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.lowagie.text.Document;
 
public class ReportPad_VertSplitMultiPage implements ReportPad{
	ReportPad_SinglePage[] delegates;
	ReportPadListener[] listeners;
	boolean fistTimeTitlesWrite =true;
	public ReportPad_VertSplitMultiPage(ReportPad_SinglePage[] delegates, ReportPadListener[] listeners) {
		this.delegates = delegates;
		this.listeners = listeners;
	}
	
	public void calculateBottomEmptySpace() {
		for (int i = 0; i < delegates.length; i++) {
		  delegates[i].calculateBottomEmptySpace();			
		}		
	}
	public boolean canFit() throws Exception {
		boolean canFit = true;
		for (int i = 0; i < delegates.length; i++) {
			canFit = canFit && delegates[i].canFit();			
		}
		return canFit;
	}

	public void finalizePage(Document document, boolean startNewPage, Map props) throws Exception {
	    if(props==null) props = new HashMap();
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].finalizePage(document, startNewPage || (i < delegates.length-1), props);			
		}	
		for (int i = 0; i < listeners.length; i++) {
			//boolean closeDocument = !startNewPage && i==listeners.length - 1;
			listeners[i].onFinalizePage(!startNewPage, props);
		}
	}

	public void footnotesPrep(StyledText[] footnotes, ReportLaF laf, RcdInfo info, Map context) throws Exception{
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].footnotesPrep(footnotes, laf, info, context);
		}
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onFootnotesPrep(footnotes,  null);
		}

	}

	public void footnotesWrite(boolean mode) throws Exception{
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].footnotesWrite(mode);
		}	
		if(mode==ReportPad.MODE_WRITE_TO_REAL){
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].onFootnotesWrite(null);
			}
		}
	}

	public void headerPrep(RcdInfo info) throws Exception {
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].headerPrep(info);
		}			
	}

	public void headerStorageAdd(RcdRow line) throws Exception {
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].headerStorageAdd(line);
		}			
	}

	public void headerStorageClear() {
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].headerStorageClear();
		}			
	}

	public void headerWrite(boolean mode, RcdInfo info) throws Exception{
		Map headers = new HashMap();
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].headerWrite(mode, info);
			headers.put(delegates[i].getPageGroup(), delegates[i].getHeaderTablet());
		}	
		if(mode==ReportPad.MODE_WRITE_TO_REAL){
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].onHeaderWrite(info, headers);
			}
		}
	}

	public void headerWriteB(RcdInfo info) throws Exception{
		Map headers = new HashMap();
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].headerWriteB(info);
			headers.put(delegates[i].getPageGroup(), delegates[i].getHeaderTablet());
		}	
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onHeaderWrite(info, headers);
		}
	}

	public void initWriteSpace() {
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].initWriteSpace();
		}			
	}

	public void lineWrite(boolean mode, StyledText txt, int indent, boolean spanWholeTable, Map props) throws Exception{
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].lineWrite(mode, txt, indent,spanWholeTable, props);   
		}			
		if(mode==ReportPad.MODE_WRITE_TO_REAL){
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].onLineWrite(txt, indent, spanWholeTable, props);
			}
		}
	}

	public void lineWriteB(StyledText txt, int indent, boolean spanWholeTable, Map props) throws Exception{
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].lineWriteB(txt, indent,spanWholeTable, props);
		}			
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onLineWrite(txt, indent, spanWholeTable, props);
		}
	}

	public void rowPrep(RcdRow row) throws Exception {
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].rowPrep(row);
		}			
	}

	public void rowWrite(boolean mode, RcdRow row, int hint) throws Exception{
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].rowWrite(mode, row, hint);
		}		
		if(mode==ReportPad.MODE_WRITE_TO_REAL){
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].onRowWrite(row, hint, null);
			}
		}
	}

	public void tiltesPrep(StyledText[] titles, Map oldTitleIndexes, RcdInfo info,  ReportLaF laf, Map context) throws Exception {
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].tiltesPrep(titles, oldTitleIndexes, info, laf, context);
		}		
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onTitlesPrep(titles, oldTitleIndexes, null);
		}
	}

	public void titlesWrite(boolean mode, boolean firstPage) throws Exception{
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].titlesWrite(mode, firstPage);
		}	
		if(mode==ReportPad.MODE_WRITE_TO_REAL){
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].onTitlesWrite(fistTimeTitlesWrite, firstPage, null);
			}
			fistTimeTitlesWrite = false;
		}
	}

	public void titlesWriteB(boolean firstPage) throws Exception{
		for (int i = 0; i < delegates.length; i++) {
			delegates[i].titlesWriteB(firstPage);
		}	
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onTitlesWrite(fistTimeTitlesWrite, firstPage, null);
		}
		fistTimeTitlesWrite = false;
	}

}
