/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;

public class ReportPad_SinglePage implements ReportPad {
    
   ReportSetup reportSetup;
   PdfContentByte rawContentHandle;
   PageGroup pageGroup;
   ReportLaF laf;
   TableWidths tableWidths;
   DocumentHolder docHolder;
   
   HeaderTablet headerTablet;
   ColumnText testWriteSpace;
   ColumnText realWriteSpace;
   PdfPTable ttable; //title;
   PdfPTable ttable_cont; //title_cont;
   PdfPTable htable; //headers;
   PdfPTable shtable; //system headers;
   PdfPTable btable; //body;
   PdfPTable ftable;
   PdfPTable sftable;
   private float bottomEmptySpaceLeft = -1;
   
   public ReportPad_SinglePage(PageGroup pageGroup,  ReportSetup reportSetup, ReportLaF laf, PdfContentByte rawContentHandle, TableWidths tableWidths, DocumentHolder holder){
	   this.pageGroup = pageGroup;
	   this.reportSetup = reportSetup;
	   this.rawContentHandle = rawContentHandle;
	   this.laf  = laf;
	   this.tableWidths = tableWidths;
	   this.docHolder = holder;
	   _init();
   }
   private void _init(){
	   headerTablet = new HeaderTablet();
   }
 
   public HeaderTablet getHeaderTablet(){
	   return headerTablet;
   }
   
   public PageGroup getPageGroup(){
	   return pageGroup;
   }
	public void calculateBottomEmptySpace() {
		//System.out.println("bookmark yline=" + getWriteSpace(MODE_WRITE_TO_TEST).getYLine());

		bottomEmptySpaceLeft = getWriteSpace(MODE_WRITE_TO_TEST).getYLine() - reportSetup.getBottomMargin();
		//System.out.println("Empty Space left on page " + bottomEmptySpaceLeft);
	}
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#headerStorageClear()
 */
public void headerStorageClear() {
	   headerTablet.clearHeaderRows();
	   htable= null;
   }
   
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#headerStorageAdd(com.btcisp.rrg.rcdparser.RcdRow)
 */
public void headerStorageAdd(RcdRow line) throws Exception{
	  int[] colIDs = pageGroup.getColumnIDs();
	  headerTablet.addHeaderRow(line, colIDs, laf);
   }
   
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#headerPrep()
 */
public void headerPrep(RcdInfo info) throws Exception{
		headerPrep_init();
		
		PdfPrinterUtil.getSelf().printHeaderRows(
				headerTablet, htable, pageGroup, laf, reportSetup);		   
   }
   
	private void headerPrep_init() throws Exception{
//  Scaling logic is now part of pageGroup setup
		
//        boolean scaleDown = false;
//        if(!pageGroup.isStretchRequired()) {
//  	     float pageGroupWidth= pageGroup.calculateTotalWidth();
//   	     if(pageGroupWidth < reportSetup.getTableWidth()){
//   		   scaleDown =true;
//   		 }
//       }

			htable = new PdfPTable(
					pageGroup.getColumnWidths().length);
			htable.setTotalWidth(pageGroup.getColumnWidths());
//			if(scaleDown){
				htable.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
				htable.setLockedWidth(true);
//			} else {
//				htable.setWidthPercentage(100f);
//			}
			
	}
	

   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#headerWrite(boolean)
 */
public void headerWrite(boolean mode, RcdInfo info){
	   getWriteSpace(mode).addElement(htable);
   }
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#headerWriteB()
 */
public void headerWriteB(RcdInfo info){
	headerWrite(ReportPad.MODE_WRITE_TO_TEST, info);
	headerWrite(ReportPad.MODE_WRITE_TO_REAL, info);   
		//System.out.println("after headerWriteB " +" yline=" + getWriteSpace(ReportPad.MODE_WRITE_TO_TEST).getYLine());
 } 

private void printSystemHeaderRows(RcdInfo info) throws Exception{
   shtable = SHeader_PdfTableStorage.getSelf().buildSTable_FirstRun(info, laf, reportSetup);
}


   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#tiltesPrep(java.lang.String[], java.util.Map)
 */
public void tiltesPrep(StyledText[] titles, Map oldTitleIndexes, RcdInfo info, ReportLaF laf, Map context) throws Exception{
       this.printSystemHeaderRows(info);
	
//       boolean scaleDown = false;
//       float scaleDownTo = 0;
//       if(!pageGroup.isStretchRequired()) {
//    	   float pageGroupWidth= pageGroup.calculateTotalWidth();
//    	   if(pageGroupWidth < reportSetup.getTableWidth()){
//    		   scaleDown =true;
//    		   scaleDownTo = pageGroupWidth;
//    	   }
//       }
      ttable = PdfPrinterUtil.getSelf().printTiltesRows(titles, info, reportSetup, laf, pageGroup.calculateTotalWidth(), context);
      if(!StringUtil.isEmpty(info.getTitle1Cont()) && titles[0] != null){
    	  StyledText[] ajustedTitles = new StyledText[titles.length];
    	  ajustedTitles[0] = titles[0].appendUsingLastCharacterStyle(" " + info.getTitle1Cont(), ReportLaF.STYLE_TYPE_TITLE, laf);
    	  for (int i = 1; i < ajustedTitles.length; i++) {
				  ajustedTitles[i] = titles[i];				
		  }
    	  ttable_cont = PdfPrinterUtil.getSelf().printTiltesRows(ajustedTitles, info, reportSetup, laf, pageGroup.calculateTotalWidth(), context);
      } else {
    	  ttable_cont = ttable;
      }
   }

   
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#titlesWrite(boolean)
 */
public void titlesWrite(boolean mode, boolean firstPage){
	   //System.out.println("Before shtable " + getWriteSpace(mode).getYLine());
	   getWriteSpace(mode).addElement(shtable);
	   //System.out.println("After shtable " + getWriteSpace(mode).getYLine());
	   if(firstPage)
		   getWriteSpace(mode).addElement(ttable);
	   else
		   getWriteSpace(mode).addElement(ttable_cont); 
	   //System.out.println("After table " + getWriteSpace(mode).getYLine());
   }
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#titlesWriteB()
 */
public void titlesWriteB(boolean firstPage){
	titlesWrite(ReportPad.MODE_WRITE_TO_TEST, firstPage);
	titlesWrite(ReportPad.MODE_WRITE_TO_REAL, firstPage);
	   
		//System.out.println("after titlesWriteB mode=" +" yline=" + getWriteSpace(ReportPad.MODE_WRITE_TO_TEST).getYLine());

   }
   
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#footenotesPrep(java.lang.String[], java.util.Map)
 */
public void footnotesPrep(StyledText[] footnotes, ReportLaF laf, RcdInfo info, Map context) throws Exception{
	   if(footnotes==null) footnotes = new StyledText[0];


		//top bar and after body padding moved to footnotesWrite method
		/*
		//creates narrow cell spacing out the body from the footer
		PdfPCell topcell = new PdfPCell();
		//topcell.setPadding(laf.getAfterBodyPadding());
		topcell.setFixedHeight(laf.getAfterBodyPadding());
		topcell.setBorder(PdfPCell.BOTTOM);
		ftable.addCell(topcell);
		*/
		if(info.isFooterStyle_withFT()) {
			footnotesPrep_withFT(footnotes, laf);
		} else {
			footnotesPrep_std(footnotes);
		}

		
		sftable = PdfPrinterUtil.getSelf().printSystemFootnotes(laf, info, reportSetup);
		

   }
   
private void footnotesPrep_std(StyledText[] footnotes) throws Exception {
	ftable = new PdfPTable(1);
	ftable.setTotalWidth(reportSetup.getTableWidth());
	ftable.setWidthPercentage(100f);
	boolean hasFootnotes = false;
	for (int j = 0; j < footnotes.length; j++) {
		StyledText ts = (StyledText) footnotes[j];
		if (!ts.isEmpty()) {
			hasFootnotes = true;
			//String ts = StringUtil.replace(footnotes[j],
			//		"//", "\n");
			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(ts, laf, "footnote", 0, Element.ALIGN_LEFT);
			if(j == 0 && laf.isSeparatorAboveFootnotes())
				cell.setBorder(PdfPCell.TOP);
			else
				cell.setBorder(PdfPCell.NO_BORDER);
			if (j == 0) {
				//cell.setBorder(PdfPCell.TOP);
				cell.setPaddingTop(laf.getBeforeFootnotePadding());
			} else  {
				cell.setPaddingTop(0f);
			}
			cell.setPaddingLeft(0f);

			ftable.addCell(cell);
		}
	}
	//note std has to exectute on empty footnote
	if(!hasFootnotes && laf.isSeparatorAboveFootnotes()){
		PdfPCell topcell = new PdfPCell();
		//topcell.setPadding(laf.getAfterBodyPadding());
		topcell.setFixedHeight(laf.getBeforeFootnotePadding() + laf.getPdfPaddingAdjustement());
		topcell.setBorder(PdfPCell.TOP);
		ftable.addCell(topcell);
	}
}


private void footnotesPrep_withFT(StyledText[] footnotes, ReportLaF laf) throws Exception {
	ftable = new PdfPTable(2);
	// calc column widths
	float maxwidth = RrgPrinterUtil.getSelf().maxSpecialFooterTitleSize(footnotes, laf, ReportLaF.STYLE_TYPE_FOOTNOTE);
	float firstColSize = maxwidth + laf.getFTSeparation() + 4; //4 added for minimum separaration and safety (parentese problem is 2 should be added.
	ftable.setTotalWidth(new float[] {firstColSize, reportSetup.getTableWidth() - firstColSize});
	ftable.setWidthPercentage(100f);
	StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(laf);
	StyledTextSizingUtil sizingUtil = StyledTextSizingUtil.self;
	for (int j = 0; j < footnotes.length; j++) {
		StyledText ts = (StyledText) footnotes[j];
		if (!ts.isEmpty()) {
			//String ts = StringUtil.replace(footnotes[j],
			//		"//", "\n");
			String tsTxt = ts.getText();
			int idxftl = tsTxt.indexOf(RcdConstants.LEFT_FOOTNOTE_TAB);
			int idxftr = tsTxt.indexOf(RcdConstants.RIGHT_FOOTNOTE_TAB);
			if(idxftl !=-1 || idxftr!=-1){
				int align = idxftl !=-1 ? Element.ALIGN_LEFT: Element.ALIGN_RIGHT;
				String delim = idxftl !=-1 ? RcdConstants.LEFT_FOOTNOTE_TAB: RcdConstants.RIGHT_FOOTNOTE_TAB;
				int inx = idxftl !=-1 ? idxftl: idxftr;
				Pair<StyledText, StyledText> split = StyledTextUtil.splitAtPureTextPosition(ts, inx, false);
				StyledText footL= split.left;
				StyledText footR = split.right;
				footR = footR.substring(delim.length());
				footR = footR.trim();
				if(align==Element.ALIGN_LEFT) footL=footL.rightTrim();
				else if(align==Element.ALIGN_RIGHT) footL = footL.leftTrim();

				float indent =0;
				if(align==Element.ALIGN_RIGHT){
					float size = sizingUtil.calcLineSize(footL, "footnote", laf);//laf.getBaseFont("footnote").getWidthPoint(footL, laf.getFontSize("footnote"));
					indent = maxwidth - size;
				}
				//System.out.println("printing ["+footL + "], indent=" + indent);
				PdfPCell celll = PdfPrinterUtil.getSelf().formatTextAsCell(footL, laf, "footnote", indent, Element.ALIGN_LEFT);
				if(j == 0 && laf.isSeparatorAboveFootnotes())
					celll.setBorder(PdfPCell.TOP);
				else
					celll.setBorder(PdfPCell.NO_BORDER);
				if (j == 0) {
					//cell.setBorder(PdfPCell.TOP);
					celll.setPaddingTop(laf.getBeforeFootnotePadding());
				} else  {
					celll.setPaddingTop(0f);
				}
				celll.setPaddingLeft(0f); //text had conservative 4 points added so padding will be visible
				//if(align==Element.ALIGN_RIGHT) 
				//	celll.setPaddingRight(laf.getFTSeparation() + 4);
				//else 
				celll.setPaddingRight(0f);
				ftable.addCell(celll);
				PdfPCell cellr = PdfPrinterUtil.getSelf().formatTextAsCell(footR, laf, "footnote", 0, Element.ALIGN_LEFT);
				if(j == 0 && laf.isSeparatorAboveFootnotes())
					cellr.setBorder(PdfPCell.TOP);
				else
					cellr.setBorder(PdfPCell.NO_BORDER);
				if (j == 0) {
					//cell.setBorder(PdfPCell.TOP);
					cellr.setPaddingTop(laf.getBeforeFootnotePadding());
				} else  {
					cellr.setPaddingTop(0f);
				}
				cellr.setPaddingRight(0f); //text in celll had conservative 4 points added so padding will be visible
				cellr.setPaddingLeft(0f);
				ftable.addCell(cellr);			
				//System.out.println("After FootnoteLR:" + footR + ",size=" + ftable.getTotalHeight());
			} else {
				PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(ts, laf, "footnote", 0, Element.ALIGN_LEFT);
				cell.setBorder(PdfPCell.NO_BORDER);
				cell.setColspan(2);
				if (j == 0) {
					if(laf.isSeparatorAboveFootnotes()) cell.setBorder(PdfPCell.TOP);
					cell.setPaddingTop(laf.getBeforeFootnotePadding());
				} else  {
					cell.setPaddingTop(0f);
				}
				cell.setPaddingLeft(0f);
				cell.setPaddingRight(0f);
				ftable.addCell(cell);
				//System.out.println("After FootnoteC:" + ts+ ",size=" + ftable.getTotalHeight());
			}

		}
	}

}
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#footnotesWrite(boolean)
 */
public void footnotesWrite(boolean mode) throws Exception{
	//scaling is now part of PageGroup
//    boolean scaleDown = false;
//    float scaleDownTo = 0;
//    if(!pageGroup.isStretchRequired()) {
//	    float pageGroupWidth= pageGroup.calculateTotalWidth();
//	    if(pageGroupWidth < reportSetup.getTableWidth()){
//		   scaleDown =true;
//		   scaleDownTo = pageGroupWidth;
//		 }
//    }
    
	PdfPTable emptyT = new PdfPTable(1);
//	if(!scaleDown){
//		emptyT.setTotalWidth(reportSetup.getTableWidth());
//		emptyT.setWidthPercentage(100f);
//	} else {
		emptyT.setTotalWidth(pageGroup.calculateTotalWidth());
		emptyT.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
		emptyT.setLockedWidth(true);
//	}
	//creates narrow cell spacing out the body from the footer
	PdfPCell topcell = new PdfPCell();
	//topcell.setPadding(laf.getAfterBodyPadding());
	if(mode == MODE_WRITE_TO_TEST && reportSetup.isRtfOut() && laf.getPdfExtraLinesForRtfCompatibility()>0 ) {
		float extraLinesSize = (laf.getParagraphLeading("body")) * laf.getPdfExtraLinesForRtfCompatibility();
		topcell.setFixedHeight(laf.getAfterBodyPadding() + laf.getPdfPaddingAdjustement() + extraLinesSize);
		SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "PDF bottom space increased by (extra lines for rft compatibility=)" + extraLinesSize + "+(after body padding=)" + laf.getAfterBodyPadding() + "+(pdf padding adj=)" + laf.getPdfPaddingAdjustement()+" for compatibility with RTF");		
	} else {
		SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "PDF bottom space increased by (after body padding=)" + laf.getAfterBodyPadding() + "+(pdf padding adj=)" + laf.getPdfPaddingAdjustement());		
		topcell.setFixedHeight(laf.getAfterBodyPadding() + laf.getPdfPaddingAdjustement());
	}
	if(laf.isSeparatorBelowTable())
		topcell.setBorder(PdfPCell.BOTTOM);
	else
		topcell.setBorder(PdfPCell.NO_BORDER);
	emptyT.addCell(topcell);

	/*
	   if(laf.isBottomFooter() && mode==MODE_WRITE_TO_REAL && this.bottomEmptySpaceLeft>0){
			PdfPCell ecell = new PdfPCell();
			ecell.setFixedHeight(this.bottomEmptySpaceLeft);
			ecell.setBorder(PdfPCell.NO_BORDER);
			emptyT.addCell(ecell);
			System.out.println("before footnote padding added: " + this.bottomEmptySpaceLeft);
			
	   }
	   */

	getWriteSpace(mode).addElement(emptyT);	 
	
	//System.out.println("total hight of footer table = "+ ftable.getTotalHeight());
	//System.out.println("total hight of sfooter table = "+ sftable.getTotalHeight());
	if(mode==MODE_WRITE_TO_TEST || !laf.isBottomFooter()){
		getWriteSpace(mode).addElement(ftable);		
		getWriteSpace(mode).addElement(sftable);		
	} else {
		//MOVED TO FINIALIZE TO DEAL WITH vertical splits
		//getWriteSpace(mode).go();
		//getWriteSpace(mode).setYLine(reportSetup.getBottomMargin() + ftable.getTotalHeight());
		//getWriteSpace(mode).addElement(ftable);		
	}
	//System.out.println("after footnotesWrite mode=" + mode + " yline=" + getWriteSpace(mode).getYLine());
   }

   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#lineWriteB(java.lang.String, int)
 */
public void lineWriteB(StyledText txt, int indent, boolean spanWholeTable, Map props) throws Exception{
	   lineWrite(ReportPad.MODE_WRITE_TO_TEST, txt, indent, spanWholeTable, props);
	   lineWrite(ReportPad.MODE_WRITE_TO_REAL, txt, indent, spanWholeTable, props);
   }
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#lineWrite(boolean, java.lang.String, int)
 */
public void lineWrite(boolean mode, StyledText txt, int indent, boolean spanWholeTable, Map props) throws Exception{
			PdfPTable ltable = new PdfPTable(
				pageGroup.getColumnWidths().length);
			 ltable.setTotalWidth(pageGroup.getColumnWidths());

			 //scale down is now part of PageGroup logic
//		        boolean scaleDown = false;
//		        if(!pageGroup.isStretchRequired()) {
//		 	      float pageGroupWidth= pageGroup.calculateTotalWidth();
//		 	      if(pageGroupWidth < reportSetup.getTableWidth()){
//		 		   scaleDown =true;
//		 		  }
//		        }
//				if(scaleDown){
					ltable.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
					ltable.setLockedWidth(true);
//				} else {
//					ltable.setWidthPercentage(100f);
//				}

     	  PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(txt, laf, "body", indent * laf.getIndentSize(), Element.ALIGN_LEFT);
    	  cell.setBorder(PdfPCell.NO_BORDER);
		  cell.setPaddingLeft(0f);
    	  if(spanWholeTable) {
    		  cell.setColspan(ltable.getNumberOfColumns());
        	  ltable.addCell(cell);    		  
    	  } else {
        	  ltable.addCell(cell);
	    	  for(int i=1; i<ltable.getNumberOfColumns(); i++){
	    		  PdfPCell empt= PdfPrinterUtil.getSelf().formatEmptyParagraphAsCell2(laf, "body");
	    		  empt.setBorder(PdfPCell.NO_BORDER);
	    		  ltable.addCell(empt);
	    	  }
    	  }
    	  getWriteSpace(mode).addElement(ltable);	
      
   }
   
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#rowPrep(com.btcisp.rrg.rcdparser.RcdRow)
 */
   public void rowPrep(RcdRow row) throws Exception{
	   rowPrep_init();
	   rowPrep_impl(row);
   }
	
   private void rowPrep_impl(RcdRow line) throws Exception{
		if(!line.getTCol(laf).isEmpty()){
			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(line.getTCol(laf), laf, "body", 0, Element.ALIGN_LEFT);
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setColspan(btable.getNumberOfColumns());
			btable.addCell(cell);
		}
		PdfPrinterUtil.getSelf().printBodyRows(line, 
				pageGroup,
				btable, 
				laf, 
				tableWidths
				);
		// System.out.println("added row " + rowcount +
		// " to table");	
   }
   
   private void rowPrep_init() throws Exception{
	 btable = new PdfPTable(
		pageGroup.getColumnWidths().length);
	 btable.setTotalWidth(pageGroup.getColumnWidths());

	 //scaling is now part of PageGroup logic
//        boolean scaleDown = false;
//        if(!pageGroup.isStretchRequired()) {
// 	      float pageGroupWidth= pageGroup.calculateTotalWidth();
// 	      if(pageGroupWidth < reportSetup.getTableWidth()){
// 		   scaleDown =true;
// 		  }
//        }
//		if(scaleDown){
			btable.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
			btable.setLockedWidth(true);
//		} else {
//			btable.setWidthPercentage(100f);
//		}
   }
   
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#rowWrite(boolean)
 */
public void rowWrite(boolean mode, RcdRow row, int hint){
	   getWriteSpace(mode).addElement(btable);	   
   }
   
	/* (non-Javadoc)
	 * @see com.btcisp.sasshiato.ReportPadI#canFit()
	 */
	public boolean canFit() throws Exception{
		int status = getWriteSpace(ReportPad.MODE_WRITE_TO_TEST).go(true);
		//System.out.println("CanFit is at " + getWriteSpace(ReportPad.MODE_WRITE_TO_TEST).getYLine());
		return !ColumnText.hasMoreText(status);
	}
	
	/* (non-Javadoc)
	 * @see com.btcisp.sasshiato.ReportPadI#finalizePage(com.lowagie.text.Document, boolean)
	 */
	public void finalizePage(Document document, boolean startNewPage, Map props) throws Exception{
		//System.out.println("finalize page" + getWriteSpace(ReportPad.MODE_WRITE_TO_REAL).getYLine());
		

			if(laf.isBottomFooter()){
				//footer placing with setYLine requires a go() this has to be done in the finalizePage to avoid introducing incorrect behavior with vertical page splits
				
				getWriteSpace(ReportPad.MODE_WRITE_TO_REAL).go();
				float currentYLine = getWriteSpace(ReportPad.MODE_WRITE_TO_REAL).getYLine();
				float y = reportSetup.getBottomMargin() + ftable.getTotalHeight() + sftable.getTotalHeight();
				getWriteSpace(ReportPad.MODE_WRITE_TO_REAL).setYLine(y);
				float padd = currentYLine - (reportSetup.getBottomMargin() + ftable.getTotalHeight() + sftable.getTotalHeight());
				float extraLinesSize = (laf.getParagraphLeading("body")) * laf.getPdfExtraLinesForRtfCompatibility();
				float diff = padd - extraLinesSize;
				SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "INFO: Footer placement at: " + y + ", space to table=" + padd + ", if adjused for RTF=" + diff);
				if(reportSetup.isRtfOut() && diff<0) SasshiatoTrace.logError("Unexpected condition: footnote padding(" + padd + ") < rtf compatibility space (" + extraLinesSize +")");
				if(props!=null) props.put(this.pageGroup, new Float(diff));
				//System.out.println("calculated footnote padding=" + diff);
				getWriteSpace(ReportPad.MODE_WRITE_TO_REAL).addElement(ftable);		
				getWriteSpace(ReportPad.MODE_WRITE_TO_REAL).addElement(sftable);		
			}
			getWriteSpace(ReportPad.MODE_WRITE_TO_REAL).go();	
			if(!reportSetup.isPdfOut()) {
				docHolder.clearTemporaryStorage();
			}

		if(startNewPage) document.newPage();
		SasshiatoTrace.displayProgress("Document Preparation",  SasshiatoTrace.PROGRESS_PAGE);

	}
   
   /* (non-Javadoc)
 * @see com.btcisp.sasshiato.ReportPadI#initWriteSpace()
 */
public void initWriteSpace(){
	   this.bottomEmptySpaceLeft = -1;
	   _initRealWriteSpace();
	   _initTestWriteSpace();
   }
   
   private void _initTestWriteSpace(){
	   testWriteSpace = new ColumnText(rawContentHandle);
	   _initWriteSpace(testWriteSpace);
   }
   
   private void _initRealWriteSpace(){
	   realWriteSpace = new ColumnText(rawContentHandle);
	   _initWriteSpace(realWriteSpace);
   }
   
   private void _initWriteSpace(ColumnText ct){
		ct.setSimpleColumn(reportSetup.getLeftMargin(), reportSetup
				.getBottomMargin(), reportSetup.getLeftMargin()
				+ reportSetup.getTableWidth(), reportSetup.getBottomMargin()
				+ reportSetup.getTableHeight(), 18, Element.ALIGN_CENTER);
   }
   
   private ColumnText getWriteSpace(boolean mode){
	   if(mode==ReportPad.MODE_WRITE_TO_REAL) return realWriteSpace;
	   else return testWriteSpace;
   }
}
