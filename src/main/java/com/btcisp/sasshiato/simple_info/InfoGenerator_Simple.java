/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato.simple_info;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rtf.RtfDocFormat;
import com.btcisp.rtf.RtfParagraphFormat;
import com.btcisp.rtf.RtfRowFormat;
import com.btcisp.rtf.RtfWriter;
import com.btcisp.rtf.Sasshiato2RTFUtil;
import com.btcisp.sasshiato.HeaderRow;
import com.btcisp.sasshiato.HeaderTablet;
import com.btcisp.sasshiato.PageColumn;
import com.btcisp.sasshiato.PageGroup;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.ReportSetup;
import com.btcisp.sasshiato.RrgPrinterUtil;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.sasshiato.StyledTextUtil;
import com.btcisp.sasshiato.TableWidths;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;


public class InfoGenerator_Simple {
	//TODO this class is not thread safe
	

	
	ReportLaF laf;
	ReportSetup reportSetup;  
	String filenmbase;
	TableWidths tableWidths;
	PageGroup[] pageGroups;
	boolean isEmptyTable = false;
	
	public boolean init(ReportLaF laf, ReportSetup reportSetup,  String filenmbase, TableWidths tableWidths, PageGroup[] pageGroups){
		this.laf = laf;
		this.reportSetup = reportSetup;
		this.filenmbase = filenmbase;
		this.tableWidths = tableWidths;
		this.pageGroups = pageGroups;
		if(pageGroups!=null && pageGroups.length > 1) {
			SasshiatoTrace.logError("Cannot generate simple size info with vertical page split");
			return false;
		}
		Font ft = laf.getBodyFont();
		if(ft.getFamily() != Font.COURIER) {
			SasshiatoTrace.logError("Cannot generate simple size info with font different than courier");
			return false;
		}
	
		return true;
	}
	public boolean initForEmptyTable(ReportLaF laf, ReportSetup reportSetup,  String filenmbase){
		this.laf = laf;
		this.reportSetup = reportSetup;
		this.filenmbase = filenmbase;
		this.isEmptyTable = true;
		Font ft = laf.getBodyFont();
		if(ft.getFamily() != Font.COURIER) {
			SasshiatoTrace.logError("Cannot generate simple size info with font different than courier");
			return false;
		}
		return true;
	}
	
	public PageGroup getPageGroup() {
		if(pageGroups.length !=1) return null;
		else return pageGroups[0];
	}
	private void generateSizingInfo3(ReportSetup reportSetup, RcdInfo info,  ReportLaF laf) throws Exception {
		Sasshiato2RTFUtil convUtil = Sasshiato2RTFUtil.getSelf();
		RtfDocFormat dformat = convUtil.convert(info, reportSetup, laf);
		
		ByteArrayOutputStream os= new ByteArrayOutputStream();
		PrintStream out = new PrintStream(os);
		RtfWriter.populateAndPrintPageSizingSyntax(dformat, out);
		String syntax = os.toString();
		os.reset();
			int leadingI=convUtil.convert2Twips(laf.getParagraphLeading("body"));
		//chars per line:
		BaseFont bf = laf.getBaseFont("body");
		float charSize= bf.getWidthPoint("1", laf.getFontSize("body"));
		int charSizeI = convUtil.convert2Twips(charSize);
	    int pageW = dformat.getDerivedWidth() - dformat.getLeftMRTF() - dformat.getRightMRTF();
	    float charPerPage = (float) Math.floor((float) (pageW / (charSizeI)));
	    int charPerPageI = (int) charPerPage;
	    //lines per page:
	    int pageH = dformat.getDerivedHeight() - dformat.getBottomMRTF() - dformat.getTopMRTF();
	    float linesPerPage = (float) Math.floor((float) (pageH/leadingI));
	    int linesPerPegeI = (int) linesPerPage;
	    
		PrintStream outf =null;
		FileOutputStream fos = null;
		File f = new File(filenmbase + "_info3.txt");
		if(f.exists() && !f.delete()){
			throw new Exception("Cannot delete old " + f.getAbsolutePath());
		}
		try{
			fos = new FileOutputStream(f);
			outf  = new PrintStream(fos);
			outf.println(leadingI + " " + charPerPageI + " " + linesPerPegeI +  " " + syntax);
		}finally{
			if(outf!=null) outf.close();
			if(fos!=null) fos.close();
		}
	}
	public void generateSizingInfo(ReportSetup reportSetup, RcdInfo info,  ReportLaF laf) throws Exception {
		generateSizingInfo3(reportSetup, info, laf);
		PrintStream out =null;
		FileOutputStream fos = null;
		File f = new File(filenmbase + "_info.txt");
		if(f.exists() && !f.delete()){
			throw new Exception("Cannot delete old " + f.getAbsolutePath());
		}
		
		if(pageGroups!=null && pageGroups.length > 1) {
			SasshiatoTrace.logError("Cannot generate simple size info with vertical page split");
			return;
		}
		
		//Font ft = laf.getBodyFont();
		
		//BaseFont bf = laf.getBaseFont("body");
		float fontsize = laf.getFontSize("body");
		try{
			fos = new FileOutputStream(f);
			out  = new PrintStream(fos);
			if(tableWidths ==null){
				out.println("c0,0");
			} else {
				//float columnSeparation = laf.getColumnSeparation();
				//float[] extra_num_padding = tableWidths.getExtraWidths();
				int[] act_widths_ch = tableWidths.getActualWidthsInChars();
				//float[] padd_adj = tableWidths.getIncludedCellPaddingAdjustements();
				//float[] maxWidthB4Dec= tableWidths.getMaxWidthsB4Decimal();
				
				String logTxt = "Simple size info generator column char sizes:";
				for (int i = 0; i < act_widths_ch.length; i++) {
					//int totw = conv2charcount(orig_widths[i] - padd_adj[i], bf, fontsize);
					out.println("c"+i+"," +act_widths_ch[i] );
					logTxt = logTxt + " c"+i+"=" +act_widths_ch[i];
				}
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, logTxt);
				/*
				for (int i = 0; i < actual_widths.length; i++) {
					float separationAdjL = laf.getColumnSeparationAdjustmentLeft(i);
					PageColumn pc = pageGroups[0].getPageColumnWithAbsColumnId(i);
					int totw = conv2charcount(pc.getColumnWidth(), bf, fontsize);
					int paddl = conv2charcount(columnSeparation + separationAdjL, bf, fontsize);
					int decplacement = conv2charcount(maxWidthB4Dec[i] + columnSeparation + separationAdjL + extra_num_padding[i] + pc.getNumericColumnPadding(), bf, fontsize);
					out.println("c"+i+"," +totw + "," + paddl + "," + decplacement );
				}
				*/
				//int[] colids = pageGroup.getColumnIDs();				
			}
		}finally{
			if(out!=null) out.close();
			if(fos!=null) fos.close();
		}
		
	}
	
	float singlewidth = -1;
	private int conv2charcount(float width, BaseFont bf, float fontSize){
		if(singlewidth ==-1){
			singlewidth = bf.getWidthPoint("0", fontSize);
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Simple size info generator uses char width:" + singlewidth);
		}
		
		//System.out.println("got=" + width + "/" + singlewidth + "=" + width/singlewidth +"=" + (int) Math.round(width/singlewidth));
		return (int) Math.round(width/singlewidth);
	}
		
	boolean headerSaved = false;
	public void generateHeaderInfo(HeaderTablet tablet) throws Exception{
		if(isEmptyTable) {
			this.startHeaderInfo();
			this.endHeaderInfo();
			headerSaved = true;
			return;
		}
		if(headerSaved) return;
		if(pageGroups!=null && pageGroups.length > 1) {
			SasshiatoTrace.logError("Cannot generate simple size info with vertical page split");
			return;
		}
		PageGroup pageGroup = pageGroups[0];
	      
//		if(tablet.requiresVarByGroupHeading() && !StringUtil.isEmpty(tablet.getCurrentVarByGroupHeading())){
//			RtfRowFormat tformat = new RtfRowFormat();
//			tformat.configure(new int[] {convUtil.convert2Twips(pageGroup.calculateTotalWidth())}, RtfRowFormat.TABLE_ALIGN_CENTER, RtfRowFormat.ROW_ALIGN_BOTTOM, RtfRowFormat.BORDER_NONE);
//			RtfParagraphFormat cformat = new RtfParagraphFormat();
//			cformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, convUtil.convert2Twips(raf.getHeaderBottomPadding()), convUtil.convert2Twips(raf.getParagraphLeading("title")), convUtil.convert2Twips(raf.getIndentSize()));
//			writer.beginRow(tformat);
//			formatTextAsCell(writer, tablet.getCurrentVarByGroupHeading(), raf, reportSetup, "header", cformat, pageGroup.calculateTotalWidth());
//			writer.endRow(false);
//		} else {
//			//title padding added when printing titles.
//		}

		HeaderRow[] rows = tablet.getHeaderRows();
		ProcReportHeaderNode root =null;
		this.startHeaderInfo();
		if(1==1){
			for (int row = 0; row < rows.length; row++) {
				HeaderRow hr = (HeaderRow) rows[row];
				
				int[] starts = hr.getStartColumns();
				int[] ends = hr.getEndColumns();
				StyledText[] cells = hr.getHeaderCells();
				if(root==null) {
					root = new ProcReportHeaderNode();
					root.initAsRoot(0, ends[cells.length -1]);
				}
	//			String[] rrgaligns = hr.getAlignments();
	//			String[] aligns = convUtil.convertAlignments(rrgaligns);
				for (int col = 0; col < cells.length; col++) {
					StyledText celltext = cells[col];
					if(row==rows.length-1 && starts[col] == ends[col]) continue;
					
					if(!celltext.isEmpty()){
						ProcReportHeaderNode node = new ProcReportHeaderNode();
						node.setText(celltext.getText());
						node.setStartColumn(starts[col]);
						node.setEndColumn(ends[col]);
						node.setRowId(hr.getRowId());
						node.setIsLastRow(row==rows.length-1);
						root.insertNode(node);
					}
					
				}
			}
			StringBuffer syntax= new StringBuffer();
			StringBuffer syntax2= new StringBuffer();
			root.generateProcReportSyntax(syntax, syntax2);
			out2.println(syntax.toString());
			String secLine = syntax2.toString();
			if(!StringUtil.isEmpty(secLine)){
				out2.println(secLine);
			}
		} 
		//else {
		//	StringBuffer syntax = new StringBuffer();
		//	int[] colids = pageGroup.getColumnIDs();
		//	for (int i = 0; i < colids.length; i++) {
		//		syntax.append("__col_" + i + " ");
		//	}
		//	out2.println(syntax.toString());			
		//}
		this.endHeaderInfo();
		headerSaved = true;
	}
	public void generateHeaderSplitPointsInfo(HeaderTablet tablet) throws Exception{
		if(isEmptyTable) {
			this.startHeaderInfo();
			this.endHeaderInfo();
			headerSaved = true;
			return;
		}
		if(headerSaved) return;
		if(pageGroups!=null && pageGroups.length > 1) {
			SasshiatoTrace.logError("Cannot generate simple size info with vertical page split");
			return;
		}
		PageGroup pageGroup = pageGroups[0];
	      
//		if(tablet.requiresVarByGroupHeading() && !StringUtil.isEmpty(tablet.getCurrentVarByGroupHeading())){
//			RtfRowFormat tformat = new RtfRowFormat();
//			tformat.configure(new int[] {convUtil.convert2Twips(pageGroup.calculateTotalWidth())}, RtfRowFormat.TABLE_ALIGN_CENTER, RtfRowFormat.ROW_ALIGN_BOTTOM, RtfRowFormat.BORDER_NONE);
//			RtfParagraphFormat cformat = new RtfParagraphFormat();
//			cformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, convUtil.convert2Twips(raf.getHeaderBottomPadding()), convUtil.convert2Twips(raf.getParagraphLeading("title")), convUtil.convert2Twips(raf.getIndentSize()));
//			writer.beginRow(tformat);
//			formatTextAsCell(writer, tablet.getCurrentVarByGroupHeading(), raf, reportSetup, "header", cformat, pageGroup.calculateTotalWidth());
//			writer.endRow(false);
//		} else {
//			//title padding added when printing titles.
//		}

		HeaderRow[] rows = tablet.getHeaderRows();
		
		this.startHeaderInfo();
		for (int row = 0; row < rows.length; row++) {
			HeaderRow hr = (HeaderRow) rows[row];
			
			int[] starts = hr.getStartColumns();
			int[] ends = hr.getEndColumns();
			StyledText[] cells = hr.getHeaderCells();
//			String[] rrgaligns = hr.getAlignments();
//			String[] aligns = convUtil.convertAlignments(rrgaligns);
			float[] widths = new float[cells.length];

			for(int j = 0; j < cells.length; j++){
				widths[j] = pageGroup.calculateTotalWidth(starts[j], ends[j]);
			}
			char ESCAPE = laf.getEscapeChar();
			for (int col = 0; col < cells.length; col++) {
				StyledText celltext = cells[col];
				if(celltext.isEmpty()) celltext = StyledText.withNoStyle(" ");
				
				List<StyledText> lines = StyledTextUtil.getTestInstance().groupIntoLines(celltext);
				StringBuffer newTxt = new StringBuffer();
				int lastIndent = 0;
				for (int i = 0; i < lines.size(); i++) {
					StyledText line = lines.get(i);
					int indent = line.getIndent();
					if(i==0){
						if(indent >0 ) newTxt.append(ESCAPE).append('i').append(indent);
					}
					else {
						int incrIndent =indent - lastIndent;
						if(incrIndent>0)
							newTxt.append(ESCAPE).append('t').append(incrIndent);
						else 
							newTxt.append(ESCAPE).append(ESCAPE);
					}
					newTxt.append(line.getText());
					lastIndent = indent;
				}
				
			
				//OLD CODE
//				String[] ss  = RrgPrinterUtil.getSelf().lineSpitText_Prep(celltext, laf);
//				int[] indents = new int[ss.length];
//				ss = RrgPrinterUtil.getSelf().lineSplitText_Perf(ss, indents, laf, false);
//				
//				
//				for (int k = 0; k < ss.length; k++) {
//					ss[k] = RrgPrinterUtil.getSelf().adjustForProcRtfAsciLineBreaks(ss[k], widths[col] -(indents[k] * laf.getIndentSize()), laf, "header", '|');
//					if(k==0) {
//						if(indents[k] > 0) newTxt.append(ESCAPE).append('i').append(indents[k]);
//					}
//					else {
//						int incrIndent =indents[k] - indents[k-1];
//						if(incrIndent>0)
//							newTxt.append(ESCAPE).append('t').append(indents[k] - indents[k-1]);
//						else 
//							newTxt.append(ESCAPE).append(ESCAPE);
//					}
//					newTxt.append(ss[k]);
//				}
				String modcelltext = newTxt.toString();
				SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "header txt=" + celltext);
				SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "gen size i=" + modcelltext);
				for(int k=starts[col]; k<=ends[col]; k++){
					this.generateHeaderSplitPointsInfo((int) hr.getRowId(), k, modcelltext);
				}
			}
		}
		this.endHeaderInfo();
		headerSaved = true;
	}
	
	PrintStream out2 =null;
	FileOutputStream fos2 = null;
	private void startHeaderInfo() throws Exception{
		File f = new File(filenmbase + "_info2.txt");
		if(f.exists() && !f.delete()){
			throw new Exception("Cannot delete old " + f.getAbsolutePath());
		}
		fos2 = new FileOutputStream(f);
		out2  = new PrintStream(fos2);
	}
	
	private void generateHeaderSplitPointsInfo(int rowId, int columnId, String txt) throws Exception{
		String line = StringUtil.leftPadWithBlanks("" + rowId, 4) + " " + StringUtil.leftPadWithBlanks("" + columnId, 3) + StringUtil.fill(' ', 21) + txt;
		out2.println(line);
	}
	

	private void endHeaderInfo() throws Exception {
		if(out2!=null) out2.close();
		if(fos2!=null) fos2.close();
	}
}
