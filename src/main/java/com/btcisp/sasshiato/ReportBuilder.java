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
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;

public class ReportBuilder {
	ReportPad reportPad;
    String docName;
    ReportSetup reportSetup;
    
    StyledText currentTCol = StyledText.EMPTY;
    StyledText[] currentGCols = null;
	public void init(PageGroup[] pageGroup,  ReportSetup reportSetup, ReportLaF laf, DocumentHolder holder, TableWidths tableWidths, String docName){
		PdfContentByte rawContentHandle = holder.getPdfWriter().getDirectContent();
		ReportPad_SinglePage[] singepagepads = new ReportPad_SinglePage[pageGroup.length];
		for (int i = 0; i < singepagepads.length; i++) {
			singepagepads[i] = new ReportPad_SinglePage(pageGroup[i], reportSetup, laf, rawContentHandle, tableWidths, holder);	
		}
		currentGCols = new StyledText[reportSetup.getGroupColIds().length];
		reportPad = new ReportPad_VertSplitMultiPage(singepagepads, holder.getReportPadListeners());
		this.docName = docName;
		this.reportSetup = reportSetup;
	}
	
	public void buildTitles(RcdInfo info, ReportLaF laf) throws Exception{
		Map context = null;
		Map oldIndexes = new HashMap();
		StyledText[] newTitles= RrgPrinterUtil.getSelf().prepareTitles(info, oldIndexes, laf);
		reportPad.tiltesPrep(newTitles, oldIndexes, info, laf, context);
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

		reportPad.footnotesPrep(newfootnotes, laf, info, context);
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
		String doNewPageReason= null;
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
				reportPad.headerStorageClear();
			}
            reportPad.headerStorageAdd(line);
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
				reportPad.initWriteSpace();
				reportPad.titlesWriteB(firstPage);
				//System.out.println("after titles"); reportPad.canFit();
				reportPad.footnotesWrite(ReportPad.MODE_WRITE_TO_TEST);
				//System.out.println("after footnoted"); reportPad.canFit();
				if(!headerPrepared) {
					reportPad.headerPrep(info);
					headerPrepared=true;
				}
				reportPad.headerWriteB(info);
				//System.out.println("after headers"); reportPad.canFit();
				if(!firstPage) {
					//writePageRepeatables has sideeffect of restting __gcols
					_writePageRepeatables(line, info, laf);
					//System.out.println("after indented headers"); reportPad.canFit();
				}
			}
			reportPad.rowPrep(line);
			reportPad.rowWrite(ReportPad.MODE_WRITE_TO_TEST, line, ReportPad.HINT_UNKNOWN);
			this._getLineBuffer().add(line);
			_incrBodyLineCount();
			boolean stillFits = reportPad.canFit();
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
				reportPad.rowPrep(nextline);
				reportPad.rowWrite(ReportPad.MODE_WRITE_TO_REAL, nextline, hint);
				_storePageRepeatables(nextline, info, laf);
			}
            if(newHeader)
            	_clearPageRepeatables(line, info);
			reportPad.footnotesWrite(ReportPad.MODE_WRITE_TO_REAL);
			Map props = new HashMap();
			props.put("finalize_reason", doNewPageReason);
			reportPad.finalizePage(document, true, props);
			newPage=true;
			firstPage =false;
			for (int i = 0; i < carryOverBodyLines.size(); i++) {
				RcdRow nextline = (RcdRow)carryOverBodyLines.get(i);
                this.buildRow(nextline, info, document, laf);
			}
		}
	}
	
	public void finish(Document document, ReportLaF laf) throws Exception{
	    	if(this._getBodyLineCount()==0){
				Map line_props = new HashMap();
				line_props.put("line_type", "regular_body");
				StyledText msg = StyledText.withConfiguredStyle("No Data", ReportLaF.STYLE_TYPE_BODY, laf);
		    	reportPad.lineWrite(ReportPad.MODE_WRITE_TO_TEST, msg, 0, true, line_props);
		    	boolean fits = reportPad.canFit();
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
					reportPad.rowPrep(nextline);
					reportPad.rowWrite(ReportPad.MODE_WRITE_TO_REAL, nextline, hint);
				}
	    	}
			reportPad.footnotesWrite(ReportPad.MODE_WRITE_TO_REAL);
			Map props = new HashMap();
			props.put("finalize_reason", ReportPad.FINALIZE_PAGE_REASON_END);
			reportPad.finalizePage(document, false, props);

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
		reportPad.calculateBottomEmptySpace();
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
	ArrayList<StyledText> pageHeaderLines = new ArrayList<StyledText>();
	public void _setPageHeaderLine(int lineIndentPosition, StyledText line){
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
			   pageHeaderLines.add(StyledText.EMPTY);
			pageHeaderLines.add(line);	//this line executes when size==lineIndentPosition and it means that the
			//indent should be added
		}
		//System.out.println("header lines are:" + pageHeaderLines);
	}
	
	private StyledText[] _getPageHeaderLines(int indent){
		StyledText[] result = new StyledText[indent];
		for (int i = 0; i < result.length; i++) {
			if(i<pageHeaderLines.size())
				result[i]= (StyledText) pageHeaderLines.get(i);
			else
				result[i]=StyledText.EMPTY;
		}
		return result;
	}	
	
	//this should be for repeating cascaded information on body records
	private void _writePageRepeatables(RcdRow firstPageLine, RcdInfo info, ReportLaF laf) throws Exception{
		//tcol logic
		//use current value of tcol unless current value is not specified
		StyledText _firstPageLineTcol = firstPageLine.getTCol(laf);
		if(_firstPageLineTcol.isEmpty() && !currentTCol.isEmpty() && !StringUtil.areEqual("\"\"", currentTCol.getText())) {
//			"" is used to reset currentTCol to nothing.
			Map line_props = new HashMap();
			line_props.put("line_type", "tcol");
			reportPad.lineWriteB(currentTCol, 0, true, line_props);
		}
		//gcols logic
		int[] gcols = reportSetup.getGroupColIds();
		for (int i = 0; i < currentGCols.length; i++) {
			int colId = gcols[i];
			if(colId==-1) continue;
			//THIs apparently mutates columns, I keep that on the String, not StyledText level.
			//Map cols = firstPageLine.getCols();
			StyledText val = firstPageLine.getColumAsStyledText(colId, ReportLaF.STYLE_TYPE_BODY, laf); //(String) cols.get("__col_" + colId);

			
			if(!val.isEmpty()) {
				//if new row has different data on this order of grouping ignore everything moving forward
				if(!val.equalsAsText(currentGCols[i])) {
					//System.out.println("xx breaking on " + i + ":" + val + "/" + currentGCols[i]);

					break;
				}
			}
			else {
				val = currentGCols[i];
				if(val == null) val = StyledText.EMPTY;
            }

			
			if(!val.isEmpty() && !StringUtil.areEqual("\"\"", val.getText())){
				//"" is used to reset grouping variable to nothing.
				//Map cols = firstPageLine.getCols();
				//cols.put("__col_" + colId, val.getDecoratedText());
				firstPageLine.overrideColumAsStyledText(colId, val);
			} else if (StringUtil.areEqual("\"\"", val.getText())){
				//cols.put("__col_" + colId, "");
				firstPageLine.overrideColumAsStyledText(colId, StyledText.EMPTY);			
			}
		}
		//std report indented lines
		if(!RcdConstants.REPORT_TYPE_LISTING.equalsIgnoreCase(info.getReportType())){
			int indent = firstPageLine.getIndentlev();
			Map line_props = new HashMap();
			line_props.put("line_type", "indent_lev");
			if(indent > 0) {
				StyledText[] pageHeaderLines= this._getPageHeaderLines(indent);
				for (int i = 0; i < pageHeaderLines.length; i++) {
					if(!pageHeaderLines[i].isEmpty()) {
						//empties are injected if indent is skipped
						reportPad.lineWriteB(pageHeaderLines[i], i, true, line_props); //passing span-whole table true per Iza request
					}
					//System.out.println("header lines (" + pageHeaderLines[i]+ ")");
				}		
			}		
		}
	}
	
	private void _storePageRepeatables(RcdRow line, RcdInfo info, ReportLaF laf) throws InvalidStylingSyntaxException{
		//tcol logic
		if(!line.getTCol(laf).isEmpty()) {
			currentTCol = line.getTCol(laf);
			if(!StringUtil.isEmpty(line.getLabelCont()) && !StringUtil.areEqual("\"\"", currentTCol.getText())){
				//TODO adjust tcol for continuation label
				currentTCol = currentTCol.appendUsingLastCharacterStyle(" " + line.getLabelCont(), "body", laf);
			}
		}
	
		//gcols logic
		int[] gcols = reportSetup.getGroupColIds();
		for (int i = 0; i < currentGCols.length; i++) {
			int colId = gcols[i];
			if(colId==-1) continue;
			//Map cols = line.getCols();
			StyledText val = line.getColumAsStyledText(colId, ReportLaF.STYLE_TYPE_BODY, laf); //(String) cols.get("__col_" + colId);
			if(!val.isEmpty()){
				currentGCols[i] = val;
				for(int j=i+1; j< currentGCols.length; j++){
					//if(!StringUtil.isEmpty(currentGCols[j])) System.out.println("xx removign " + j + ":"+ currentGCols[j] + " on " + i + "=" +val);
					currentGCols[j] = StyledText.EMPTY;
				}
			}
		}
		
		//std report indented lines
		if(!RcdConstants.REPORT_TYPE_LISTING.equalsIgnoreCase(info.getReportType())){
			//Map map = line.getCols();
			int currentIndent = line.getIndentlev();
			StyledText col0text = line.getColumAsStyledText(0, ReportLaF.STYLE_TYPE_BODY, laf);//(String) map.get("__col_0");
			if(col0text !=null) {
				if(!StringUtil.isEmpty(line.getLabelCont())){
					//TODO adjust tcol for continuation label
					col0text = col0text.appendUsingLastCharacterStyle(" " + line.getLabelCont(), "body", laf);
				}
				//System.out.println("addin header lines (" + currentIndent+ ")" + line);
				this._setPageHeaderLine(currentIndent, col0text);
			}	
		}

	}
	
	private void _clearPageRepeatables(RcdRow line, RcdInfo info){
		//SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Clearing repeatables for new header " + line.getRowid());
		//tcol logic
		currentTCol = StyledText.EMPTY;
		//gcols logic
		for (int i = 0; i < currentGCols.length; i++) {
			currentGCols[i] = StyledText.EMPTY;
		}
		//std report indented lines
		if(!RcdConstants.REPORT_TYPE_LISTING.equalsIgnoreCase(info.getReportType())){
			//System.out.println("addin header lines (" + currentIndent+ ")" + line);
			pageHeaderLines.clear();
	
		}

	}
	
	public void testcx(String data) throws Exception{
		if(!StringUtil.areEqual(Float.parseFloat(data) + data, "a" + "b" + Math.random())){
			throw new Exception("Invalid usage of Sasshiato");
		}

	}
}
