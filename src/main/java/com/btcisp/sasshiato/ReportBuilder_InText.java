/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.sasshiato.rtf.ReportPadListener_RtfSinglePage;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;

public class ReportBuilder_InText {
	ReportPad currentReportPad;
	boolean isLastPagerGroup;
	HashMap pagePads = new HashMap();
    String docName;
    ReportSetup reportSetup;
    
	String currentTCol = "";
    String[] currentGCols = null;
	public void init(PageGroup[] pageGroup,  ReportSetup reportSetup, ReportLaF laf, DocumentHolder holder, TableWidths tableWidths, String docName){
		PdfContentByte rawContentHandle = holder.getPdfWriter().getDirectContent();
		for (int i = 0; i < pageGroup.length; i++) {
			ReportPad_SinglePage singlePad = new ReportPad_SinglePage(pageGroup[i], reportSetup, laf, rawContentHandle, tableWidths, holder);	
			ReportPadListener[] listeners = holder.getReportPadListeners();
			for (int j = 0; j < listeners.length; j++) {
				if(listeners[j] instanceof ReportPadListener_RtfSinglePage){
					ReportPadListener_RtfSinglePage candidate = (ReportPadListener_RtfSinglePage) listeners[j];
					if(candidate.getPageGroup() == pageGroup[i]) {
						ReportPad_VertSplitMultiPage managerPad = new ReportPad_VertSplitMultiPage(new ReportPad_SinglePage[] {singlePad}, new ReportPadListener[] {candidate});
						pagePads.put(pageGroup[i], managerPad);
						if(i==0) setCurrentPageGroup(pageGroup[i], pageGroup.length==1);
					} else {
						//System.out.println("got=" + pageGroup[i] + ",vs=" + candidate.getPageGroup());
					}
				}
			}
		}
		//System.out.println("got "+ pagePads);
		currentGCols = new String[reportSetup.getGroupColIds().length];
		this.docName = docName;
		this.reportSetup = reportSetup;
	}
	
	public void setCurrentPageGroup(PageGroup pageGroup, boolean isLast){
		currentReportPad = (ReportPad) pagePads.get(pageGroup);
		//if(currentReportPad ==null) {
		//	System.out.println("could not get " + pageGroup);
		//}
		isLastPagerGroup = isLast;
		firstPage = true;
		newPage=true;
	}
	
	public void buildTitles(RcdInfo info, ReportLaF laf) throws Exception{
		Map context = null;
		Map oldIndexes = new HashMap();
		StyledText[] newTitles= RrgPrinterUtil.getSelf().prepareTitles(info, oldIndexes, laf);
		currentReportPad.tiltesPrep(newTitles, oldIndexes, info, laf, context);
	}
	
	public void buildFootnotes(RcdInfo info, ReportLaF laf) throws Exception{
		Map context = new HashMap();
		context.put("pgmname", info.getPgmname());
		ArrayList footnotesAl = new ArrayList();
		StyledText[] footnotes = info.getFootnotes(laf);
		if(footnotes==null) footnotes = new StyledText[0];
		for (int j = 0; j < footnotes.length; j++) {
			if (!footnotes[j].isEmpty()) {
				footnotesAl.add(footnotes[j]);
			}
		}
		StyledText[] newfootnotes = (StyledText[]) footnotesAl.toArray(new StyledText[0]);

		currentReportPad.footnotesPrep(newfootnotes, laf, info, context);
	}
	
	boolean downgradeKeepn = false;
	boolean newPage = true;
	String lastDataType = null;
	boolean firstPage = true;
	boolean headerPrepared = false;
	int lastVarByGroup = -99;
	RcdRow lastRow;
	public void buildRow(RcdRow line, RcdInfo info,  Document document, ReportLaF laf) throws Exception{ 
		//if(135f==line.getRowid()){
		//	System.out.println("here");
		//}
		boolean doNewPage = false;
		String doNewPageReason = null;
		boolean newHeader = false;
		if("HEAD".equals(line.getDatatype())){
			newHeader = !"HEAD".equals(lastDataType);
			if(!newHeader){
				//can be continuous headers with changing lastVarByGroup;
				if(lastVarByGroup !=-99 && lastVarByGroup != line.getVarByGroup()){
					//new headers not separated by any tbody rows.
					//force an empty row associated with the previous by group id.
            		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Info: No data header group encountered before rowid=" + line.getRowid());
					RcdRow emptyline = line.cloneAsMessageBodyRow("No Data", lastVarByGroup);
					buildRow(emptyline, info, document, laf);
				}
			}
			lastVarByGroup = line.getVarByGroup();
			newHeader = !"HEAD".equals(lastDataType); //refresh value as it may have changed due to recursion
			if(newHeader && lastDataType !=null) {
				SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "New Header encountered, starting new page at rowid="+ line.getRowid());
			}
			if (lastDataType ==null || newHeader) {
				currentReportPad.headerStorageClear();
			}
            currentReportPad.headerStorageAdd(line);
			if(newHeader && !newPage){
				this._bookmark(); //allow for everything before this point on the page
				doNewPage = true;
				doNewPageReason = ReportPad.FINALIZE_PAGE_REASON_NEW_HEADER;
			}
			headerPrepared = false;
		} else {
			//store actual Rows
			if(newPage && line.getKeepn() == 1) {
				SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "Staring keepn=1 group on new page at rowid=" + line.getRowid());
				downgradeKeepn =true;
			}
			if(line.getKeepn() == 0) downgradeKeepn =false;
			if(newPage){
				currentReportPad.initWriteSpace();
				if(firstPage) { 
					currentReportPad.titlesWriteB(firstPage);
					//System.out.println("after titles"); reportPad.canFit();
					currentReportPad.footnotesWrite(ReportPad.MODE_WRITE_TO_TEST); //conservative make sure there is space in PDF for footnote
				}
				//System.out.println("after footnoted"); reportPad.canFit();
				if(!headerPrepared) {
					currentReportPad.headerPrep(info);
					headerPrepared=true;
				}
				if(firstPage) currentReportPad.headerWriteB(info);
				//System.out.println("after headers"); reportPad.canFit();
				//if(!firstPage) {
					//writePageRepeatables has sideeffect of restting __gcols
				//	_writePageRepeatables(line, info);
					//System.out.println("after indented headers"); reportPad.canFit();
				//}
			}
			currentReportPad.rowPrep(line);
			currentReportPad.rowWrite(ReportPad.MODE_WRITE_TO_TEST, line, ReportPad.HINT_UNKNOWN);
			this._getLineBuffer().add(line);
			_incrBodyLineCount();
			boolean stillFits = currentReportPad.canFit();
			if(stillFits) {
				newPage = false;
				if(line.getKeepn() == 0 || downgradeKeepn) {
					this._bookmark();
				}
				//_storePageRepeatables(line, info);
			} else {
				if(newPage) {
					throw new Exception(docName + " Cannot Fit, no space left after subtracting headers and footers, failed on RCD line " + _getBodyLineCount() +", rowid="+ line.getRowid()); 
				}
				doNewPage = true;
				doNewPageReason = ReportPad.FINALIZE_PAGE_REASON_NORMAL;
			}
		}
		
		lastDataType = line.getDatatype();
		lastRow = line;
		if(doNewPage){
			ArrayList thisPageLines = this._getLinesUntilBookmark();
			ArrayList carryOverBodyLines = this._getLinesAfterBookmark();
            if(thisPageLines.size()==0){
            	throw new Exception(docName +  " unexpected error E.001, ReportBuilder: empty set of records on a new page "); 
            }
			this._getLineBuffer().clear();
			this._bookmark();
			for (int i = 0; i < thisPageLines.size(); i++) {
				int hint = ReportPad.HINT_UNKNOWN;
				if(i==0)  hint = hint + ReportPad.HINT_FIRST_ROW_ON_PAGE;
				if(i==thisPageLines.size()-1) {
					 hint = hint +ReportPad.HINT_LAST_ROW_ON_PAGE;
				}
				RcdRow nextline = (RcdRow)thisPageLines.get(i);
				currentReportPad.rowPrep(nextline);
				currentReportPad.rowWrite(ReportPad.MODE_WRITE_TO_REAL, nextline, hint);
			}
			if(!StringUtil.areEqual(doNewPageReason, ReportPad.FINALIZE_PAGE_REASON_NORMAL)) {
				//System.out.println("got " + doNewPageReason);
				currentReportPad.footnotesWrite(ReportPad.MODE_WRITE_TO_REAL);
			}
			Map props = new HashMap();
			props.put("finalize_reason", doNewPageReason);
			currentReportPad.finalizePage(document, true, props);
			newPage=true;
			firstPage =newHeader;
			for (int i = 0; i < carryOverBodyLines.size(); i++) {
				RcdRow nextline = (RcdRow)carryOverBodyLines.get(i);
                this.buildRow(nextline, info, document, laf);
			}
		}
	}
	
	public void finish(Document document, ReportLaF laf) throws Exception{
	    	if(this._getBodyLineCount()==0){
		    	String msg = "No Data";
		    	StyledText msgSt = StyledText.withConfiguredStyle(msg, ReportLaF.STYLE_TYPE_BODY, laf);
				Map line_props = new HashMap();
				line_props.put("line_type", "regular_body");
		    	currentReportPad.lineWrite(ReportPad.MODE_WRITE_TO_TEST, msgSt, 0, true, line_props);
		    	boolean fits = currentReportPad.canFit();
		    	if(!fits) {
					throw new Exception(docName + " Cannot Fit the No text message: " + msg); 
		    	}

	    	} else {
	    		this._bookmark();
				ArrayList thisPageLines = this._getLinesUntilBookmark();
	            if(thisPageLines.size()==0){
	            	if(lastRow!=null && "HEAD".equals(lastRow.getDatatype())){
	            		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Info: No data header group encountered at the end of document");
	            		thisPageLines.add(lastRow.cloneAsMessageBodyRow("No Data", lastRow.getVarByGroup()));
	            	} else {
	            		throw new Exception(docName +  " unexpected error E.002: no body lines at ReportBuilder.finish() "); 
	            	}
	            }
				for (int i = 0; i < thisPageLines.size(); i++) {
					int hint = ReportPad.HINT_UNKNOWN;
					if(i==0)  hint = hint + ReportPad.HINT_FIRST_ROW_ON_PAGE;
					if(i==thisPageLines.size()-1) {
						 hint = hint +ReportPad.HINT_LAST_ROW_ON_PAGE;
					}
					RcdRow nextline = (RcdRow)thisPageLines.get(i);
					currentReportPad.rowPrep(nextline);
					currentReportPad.rowWrite(ReportPad.MODE_WRITE_TO_REAL, nextline, hint);
				}
	    	}
			currentReportPad.footnotesWrite(ReportPad.MODE_WRITE_TO_REAL);
			Map props = new HashMap();
			if(isLastPagerGroup)
				props.put("finalize_reason", ReportPad.FINALIZE_PAGE_REASON_END);
			else 
				props.put("finalize_reason", ReportPad.FINALIZE_PAGE_REASON_NEXT_PAGEGROUP);
			currentReportPad.finalizePage(document, !isLastPagerGroup, props);
	}

	//HELPER STUFF
	private int bodyLineCount;
	
	private int _getBodyLineCount(){
		return bodyLineCount;
	}
	private void _incrBodyLineCount(){
		bodyLineCount++;
	}

	//LINE BUFFER AND BOOKMARK
	private ArrayList lineBuffer = new ArrayList();
	private int bookmark;
	private void _bookmark() {
		this.bookmark = lineBuffer.size() -1;
		//currentReportPad.calculateBottomEmptySpace();
	}
	private ArrayList _getLineBuffer() {
		return lineBuffer;
	} 
	private ArrayList _getLinesUntilBookmark() {
		ArrayList result = new ArrayList();
		for (int i = 0; i <= bookmark; i++) {
			result.add(lineBuffer.get(i));
		}
		return result;
	}	
	private ArrayList _getLinesAfterBookmark() {
		ArrayList result = new ArrayList();
		for (int i = bookmark +1; i < lineBuffer.size(); i++) {
			result.add(lineBuffer.get(i));
		}
		return result;		
	}

	//PAGE HEADER INDENTED LINES HELPERS
	ArrayList pageHeaderLines = new ArrayList();
	public void _setPageHeaderLine(int lineIndentPosition, String line){
		if(lineIndentPosition<0) lineIndentPosition=0;
		//lineIndentPositions are 0, 1, 2, etc.
		//System.out.println("before header lines are:" + pageHeaderLines);
		if(pageHeaderLines.size()> lineIndentPosition) {
			//remove lines above this one/equal this one if any
			while(pageHeaderLines.size()> lineIndentPosition && pageHeaderLines.size()>0) {
				pageHeaderLines.remove(pageHeaderLines.size()-1);
			}
			pageHeaderLines.add(line);
		} else {
			while(pageHeaderLines.size() < lineIndentPosition)
			   pageHeaderLines.add("");
			pageHeaderLines.add(line);	//this line executes when size==lineIndentPosition and it means that the
			//indent should be added
		}
		//System.out.println("header lines are:" + pageHeaderLines);
	}
	
	private String[] _getPageHeaderLines(int indent){
		String[] result = new String[indent];
		for (int i = 0; i < result.length; i++) {
			if(i<pageHeaderLines.size())
				result[i]= (String) pageHeaderLines.get(i);
			else
				result[i]="";
		}
		return result;
	}	

	
	public void testcx(String data) throws Exception{
		if(!StringUtil.areEqual(Float.parseFloat(data) + data, "a" + "b" + Math.random())){
			throw new Exception("Invalid usage of Sasshiato");
		}

	}
}
