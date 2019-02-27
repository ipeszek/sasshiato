/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato.rtf;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.rtf.RtfDocFormat;
import com.btcisp.rtf.RtfParagraphFormat;
import com.btcisp.rtf.RtfRowFormat;
import com.btcisp.rtf.RtfWriter;
import com.btcisp.rtf.Sasshiato2RTFUtil;
import com.btcisp.sasshiato.DocumentHolder;
import com.btcisp.sasshiato.HeaderRow;
import com.btcisp.sasshiato.HeaderTablet;
import com.btcisp.sasshiato.PageGroup;
import com.btcisp.sasshiato.PdfPrinterUtil;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.ReportPad;
import com.btcisp.sasshiato.ReportPadListener;
import com.btcisp.sasshiato.ReportSetup;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.sasshiato.TableWidths;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class ReportPadListener_RtfSinglePage implements ReportPadListener {
    RtfWriter writer;
    ReportSetup reportSetup;
    RcdInfo info;
    ReportLaF laf;
    PageGroup pageGroup;
    TableWidths tableWidths;
    boolean ownsDocumentClosing;
    boolean ownsDocumentOpening;
    DocumentHolder docHolder;
	private static int totPages = 0;
	private File backgroundImage = null;
    Sasshiato2RTFUtil convUtil = Sasshiato2RTFUtil.getSelf();
 	public ReportPadListener_RtfSinglePage(String baseFilePath, PrintStream out_, ReportSetup reportSetup, RcdInfo info,  ReportLaF laf, PageGroup pageGroup, TableWidths tableWidths, boolean ownsDocumentOpening, boolean ownDocumentClosing, DocumentHolder holder, File backgroundImage) throws Exception{
		this.backgroundImage = backgroundImage;
		writer = new RtfWriter(out_);
		docHolder = holder;
		this.ownsDocumentOpening= ownsDocumentOpening;
		RtfDocFormat dformat = convUtil.convert(info, reportSetup, laf);
		if(ownsDocumentOpening) {
			if(reportSetup.isAppend()) {
				Map infoM = writer.openWithAppend(baseFilePath + ".rtftmp", dformat, reportSetup.isAllowFutureAppend(), laf.useSectionBreaksWhenAppending(), !laf.isRtfPageStartsWithParagraph());
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Appending to rtf document " + infoM);
				String totPagesS = (String) infoM.get("rtf.totPages");
				try{
				totPages = Integer.parseInt(totPagesS);
				}catch(Exception e){
					SasshiatoTrace.logError("Error parsing rtf.totPages from previous document, read:" + totPagesS);
					throw e;
				}
				File fx = new File(baseFilePath + ".rtftmp");
				if(fx.delete())
					SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + baseFilePath + ".rtftmp" + " deleted for cleanup");
				else 
					SasshiatoTrace.logError("Cannot delete file " + fx.getAbsolutePath());
				
			} else {
				writer.open(dformat, reportSetup.isAllowFutureAppend());
				totPages = 0;
			}
		} else {
			writer.openForSecondaryPages(dformat, reportSetup.isAllowFutureAppend());
		}
		this.reportSetup = reportSetup;
		this.info = info;
		this.laf = laf;
		this.pageGroup = pageGroup;
		this.tableWidths = tableWidths;
		this.ownsDocumentClosing = ownDocumentClosing;
	}
	
	public PageGroup getPageGroup(){
		return pageGroup;
	}

	StyledText[] current_footnotes =null;
	public void onFootnotesPrep(StyledText[] footnotes,  Map props) throws Exception {
		current_footnotes = footnotes;
		if(this.ownsDocumentOpening && (laf.isSystemHeaderInRtfHeader() || backgroundImage !=null) ){
			writer.beginHeader(convUtil.convert2FontSize(laf.getFontSize("systemHeader")));
			if(laf.isSystemHeaderInRtfHeader()) RtfPrinterUtil.getSelf().printSystemHeaders(writer, laf, info, reportSetup);
			if(backgroundImage !=null) RtfPrinterUtil.getSelf().printBackgroundImage(writer, laf, reportSetup, backgroundImage);
			writer.endHeader();
		} 
		if(this.ownsDocumentOpening && (laf.isFootnotesInRtfFooter() || laf.isSystemFooterInRtfFooter())) {
			writer.beginFooter(convUtil.convert2FontSize(laf.getFontSize("footnote")));
			if(laf.isFootnotesInRtfFooter()){
				RtfPrinterUtil.getSelf().printFootnotes(writer, footnotes, laf, info, reportSetup, 0, laf.isSeparatorAboveFootnotes());
			}
			if(laf.isSystemFooterInRtfFooter()){
				writer.beginNewFontSize(convUtil.convert2FontSize(laf.getFontSize("systemFootnote")));
				RtfPrinterUtil.getSelf().printSystemFootnotes(writer, laf, info, reportSetup, 0, footnotes.length==0 && laf.isSeparatorAboveFootnotes());
				writer.endNewFontSize();
			}
			writer.endFooter();
		}

	}

	public void onFootnotesWrite(Map props) throws Exception {

	}


	public void onHeaderWrite(RcdInfo info, Map props) throws Exception{
		HeaderTablet tablet = (HeaderTablet) props.get(this.pageGroup);
		if(tablet==null) throw new Exception("System error, missing header tablet in RTF Report Pad Listener");
		RtfPrinterUtil.getSelf().printHeaderRows(writer, tablet, pageGroup, laf, reportSetup);
	}

	public void onLineWrite(StyledText txt, int indent, boolean spanWholeTable,
			Map props) throws Exception {
 
		//System.out.println("writing line "+ txt);
		float indentf = indent * laf.getIndentSize();
		if(spanWholeTable){
			   RtfRowFormat tformat = new RtfRowFormat();  
			   tformat.configure(new int[]{convUtil.convert2Twips(pageGroup.calculateTotalWidth())}, RtfRowFormat.TABLE_ALIGN_CENTER, null,  RtfRowFormat.BORDER_NONE, false);
			   RtfParagraphFormat cFormat= new RtfParagraphFormat();
			   cFormat.configure(RtfParagraphFormat.ALIGN_LEFT, convUtil.convert2Twips(indentf), 0, 0, 0, convUtil.convert2Twips(laf.getParagraphLeading("body")), convUtil.convert2Twips(laf.getIndentSize()), convUtil.convertFont2FFInstructions(laf.getFont("body")));
			   writer.beginRow(tformat);
			   RtfPrinterUtil.getSelf().formatTextAsCell(writer, txt, laf, reportSetup, "body", cFormat, pageGroup.calculateTotalWidth());
			   writer.endRow(false);
		} else {
			   RtfRowFormat tformat = new RtfRowFormat();  
			   tformat.configure(convUtil.convert2Twips(pageGroup.getColumnWidths(), 0), RtfRowFormat.TABLE_ALIGN_CENTER, null,  RtfRowFormat.BORDER_NONE, false);
			   RtfParagraphFormat cFormat= new RtfParagraphFormat();
			   cFormat.configure(RtfParagraphFormat.ALIGN_LEFT, convUtil.convert2Twips(indentf), 0, 0, 0, convUtil.convert2Twips(laf.getParagraphLeading("body")), convUtil.convert2Twips(laf.getIndentSize()), convUtil.convertFont2FFInstructions(laf.getFont("body")));
			   writer.beginRow(tformat);
			   RtfPrinterUtil.getSelf().formatTextAsCell(writer, txt, laf, reportSetup, "body", cFormat, pageGroup.getColumnWidths()[0]);
			   writer.endRow(true);
			
		}

	}

	public void onRowWrite(RcdRow row,  int hint, Map props) throws Exception{
		if(!row.getTCol(laf).isEmpty()){			
			   //System.out.println("wrting tcol " + row.getTCol());
			   RtfRowFormat tformat = new RtfRowFormat();  
			   tformat.configure(new int[]{convUtil.convert2Twips(pageGroup.calculateTotalWidth())}, RtfRowFormat.TABLE_ALIGN_CENTER, null,  RtfRowFormat.BORDER_NONE, false);
			   RtfParagraphFormat cFormat= new RtfParagraphFormat();
			   cFormat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, 0, convUtil.convert2Twips(laf.getParagraphLeading("body")), convUtil.convert2Twips(laf.getIndentSize()), convUtil.convertFont2FFInstructions(laf.getFont("body")));
			   writer.beginRow(tformat);
			   RtfPrinterUtil.getSelf().formatTextAsCell(writer, row.getTCol(laf), laf, reportSetup, "body", cFormat, pageGroup.calculateTotalWidth());
			   writer.endRow(false);
		}
		//System.out.println("Writing row " + row.getCols().get("__col_0"));
		RtfPrinterUtil.getSelf().printBodyRows(writer, row, 
				pageGroup,
				laf, reportSetup, 
				tableWidths,
				hint
				);

	}

	private StyledText[] titles;
	private Map oldTitleIndexes;
	public void onTitlesPrep(StyledText[] titles, Map oldTitleIndexes,
			Map props) throws Exception {
		this.titles = titles;
		this.oldTitleIndexes = oldTitleIndexes;

	}

	public void onTitlesWrite(boolean firstTime, boolean firstPage, Map props) throws Exception{
		if(laf.isSystemHeaderPartOfTable() ){
			RtfPrinterUtil.getSelf().printSystemHeaders(writer, laf, info, reportSetup);
		} 
	  	
//	       boolean scaleDown = false;
//	       float scaleDownTo = 0;
//	       if(!pageGroup.isStretchRequired()) {
//	    	   float pageGroupWidth= pageGroup.calculateTotalWidth();
//	    	   if(pageGroupWidth < reportSetup.getTableWidth()){
//	    		   scaleDown =true;
//	    		   scaleDownTo = pageGroupWidth;
//	    	   }
//	       }

	      boolean secondPageTitle = !firstPage && !StringUtil.isEmpty(info.getTitle1Cont()) && titles[0] != null;
	      if(!secondPageTitle)  {
		       RtfPrinterUtil.getSelf().printTiltesRows(writer, titles, oldTitleIndexes, info, reportSetup, laf,  reportSetup.getTableWidth(), firstTime, props);	    	  
	      } else {
	    	  StyledText[] ajustedTitles = new StyledText[titles.length];
	    	  ajustedTitles[0] = titles[0].appendUsingLastCharacterStyle(" " + info.getTitle1Cont(), ReportLaF.STYLE_TYPE_TITLE, laf);
	    	  for (int i = 1; i < ajustedTitles.length; i++) {
					  ajustedTitles[i] = titles[i];				
    		  }
	    	  RtfPrinterUtil.getSelf().printTiltesRows(writer, ajustedTitles, oldTitleIndexes, info, reportSetup, laf,  reportSetup.getTableWidth(), firstTime, props);	
	      }
	}

	
	public void onFinalizePage(boolean isDocumentClosing, Map props)
	throws Exception {
		String reason = "";
		if(props!=null) {
			reason = (String) props.get("finalize_reason");
		}
		if(laf.isInText() && StringUtil.areEqual(reason, ReportPad.FINALIZE_PAGE_REASON_NORMAL)){
			return;
		}
		//System.out.println("finalize called " + reason + "," + isDocumentClosing);
		totPages ++;
		docHolder.getStratchBoard().put("rtf.totPages", totPages+ "");
		//footnotes if requested in the same table
		if(laf.isFootnotesPartOfTable()|| laf.isSystemFooterPartOfTable()){
			float extraTopPadding = 0;
			if(props!=null && props.get(this.pageGroup) !=null){
				Float fep = (Float)props.get(this.pageGroup);
				if(fep!=null){
					extraTopPadding = fep.floatValue();
				}
			}
			SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "RTF footnote padding used:" + extraTopPadding);
			boolean needsBar = laf.isSeparatorAboveFootnotes();
			if(laf.isFootnotesPartOfTable()){
				if(RtfPrinterUtil.getSelf().printFootnotes(writer, current_footnotes, laf, info, reportSetup, extraTopPadding, needsBar)){
					extraTopPadding = 0;
					needsBar = false;
				}
			}
			if(laf.isSystemFooterPartOfTable()){
				writer.beginNewFontSize(convUtil.convert2FontSize(laf.getFontSize("systemFootnote")));
				RtfPrinterUtil.getSelf().printSystemFootnotes(writer, laf, info, reportSetup, extraTopPadding, needsBar);
				writer.endNewFontSize();
			}
		}
		
		if(isDocumentClosing && ownsDocumentClosing) {
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Closing rft document with " + docHolder.getStratchBoard());
			writer.close(docHolder.getStratchBoard());
		}
		else {
			writer.pageBreak(!laf.isRtfPageStartsWithParagraph());
			writer.flush();
			//System.out.println("page break inserted");
		} 
		
	}

}
