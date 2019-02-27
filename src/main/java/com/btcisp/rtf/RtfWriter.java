/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.btcisp.sasshiato.SasshiatoSP;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.SasshiatoVersion;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.pdf.PdfWriter;

/**
 * note restricted chars:
 * {, \ users need to use \\ and \{
 * @author owner
 *
 */
public class RtfWriter {
	private static String pageParagraphSeparator = "{\\fs0\\pard\\par}";
	public static String getPageParagraphSeparator(){
		return pageParagraphSeparator;
	}
	public static void setPageParagraphSeparator(String sep){
		pageParagraphSeparator = sep;
	}
	ByteArrayOutputStream tmp_out;
	private PrintStream out =null;
	private PrintStream permanent_out =null;
	boolean appendable = false;
    int fontId;
 	public RtfWriter(PrintStream out_) throws Exception{
		permanent_out = out_;
		tmp_out = new ByteArrayOutputStream();
		out = new PrintStream(tmp_out);
	}
	
 	public void openForSecondaryPages(RtfDocFormat setup, boolean appendable){
 		this.fontId = setup.getFontId();
 		this.appendable = appendable;
 	}
	public Map openWithAppend(String fileName, RtfDocFormat setup, boolean appendable, boolean useSectionBreaks, boolean pageBreakNeedsParagraph) throws Exception{
		//System.out.println("got " + useSectionBreaks);
		this.appendable = appendable;
		this.fontId = setup.getFontId();
		FileReader in0 = null;
		BufferedReader in = null;
		Map info = new HashMap();
		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Open with append on file: " + fileName);

		try{
			in0 = new FileReader(fileName);
			in = new BufferedReader(in0);
			long lineno = 0;
			for(String line=in.readLine(); line!=null; line=in.readLine()){
				lineno++;
				if(line.startsWith("{\\*\\eofrrginfo ")){ 
					if(line.indexOf("appendable") ==-1){
						throw new Exception("File " + fileName + " was not marked for appending");
					} else {
						//collect info
						line = line.substring("{\\*\\eofrrginfo ".length());
						StringTokenizer st = new StringTokenizer(line, " ");
						while(st.hasMoreTokens()){
							String token = st.nextToken();
							if(token.equals("appendable")) {
							
							} else {
								StringTokenizer st2 = new StringTokenizer(token, "=");
								if(st2.countTokens()==2){
									String key = st2.nextToken();
									String val = st2.nextToken();
									//SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Previous File Info: " + key + "=" + val);
									info.put(key, val);
								}
							}
						}
						break;
					}
				} else {
					permanent_out.println(line);
					permanent_out.flush();
				}
			}
			if(lineno==0) SasshiatoTrace.logWarning("Open with append: " + fileName + " had no lines!");
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Open with append: " + fileName + " read no of lines=" + lineno);
		}finally{
			if(in!=null) in.close();
			if(in0!=null) in0.close();
		}
		
		if(useSectionBreaks)
			permanent_out.println("\\pard\\par\\sectd\\sect"); //do not follow page font adjustements
		else 
			pageBreak(pageBreakNeedsParagraph);
		return info;
	}
	
	public static void populateAndPrintPageSizingSyntax(RtfDocFormat setup, PrintStream out1) throws Exception{
		if(RtfDocFormat.PAPER_SIZE_LETTER.equalsIgnoreCase(setup.getPaperSize())){
			if(RtfDocFormat.ORINET_L.equalsIgnoreCase(setup.getOrient())){
				out1.print("\\paperw15820\\paperh12240");
				setup.setDerivedHeight(12240);
				setup.setDerivedWidth(15820);
				open_setMarigings(setup, out1);
				out1.print("\\landscape");
			} else if (RtfDocFormat.ORIENT_P.equalsIgnoreCase(setup.getOrient())){
				out1.print("\\paperw12240\\paperh15820");
				setup.setDerivedHeight(15820);
				setup.setDerivedWidth(12240);
				open_setMarigings(setup, out1);
			} else 
				throw new Exception ("Invalid orientation: " + setup.getOrient());
		} else if(RtfDocFormat.PAPER_SIZE_A4.equalsIgnoreCase(setup.getPaperSize())){
			if(RtfDocFormat.ORINET_L.equalsIgnoreCase(setup.getOrient())){
				out1.print("\\paperw16834\\paperh11909");
				setup.setDerivedHeight(11909);
				setup.setDerivedWidth( 16834);
				open_setMarigings(setup, out1);
				out1.print("\\landscape");				
			} else if (RtfDocFormat.ORIENT_P.equalsIgnoreCase(setup.getOrient())){
				out1.print("\\paperw11909\\paperh16834");
				setup.setDerivedHeight(16834);
				setup.setDerivedWidth( 11909);
				open_setMarigings(setup, out1);				
			} else 
				throw new Exception ("Invalid orientation: " + setup.getOrient());			
		} else {
			throw new Exception ("Unsupported paper size: " + setup.getPaperSize());		
		}		
	}
	
	public void open(RtfDocFormat setup, boolean appendable) throws Exception{
		this.appendable = appendable;
		this.fontId = setup.getFontId();
		out.println("{\\rtf1\\ansi\\deff0\\deflang1033{\\fonttbl{\\f0\\froman Times New Roman;}{\\f1\\froman\\fcharset2\\fprq2 Symbol;}{\\f2\\fswiss Arial;}{\\f3\\fmodern Courier New;}{\\f4\\fmodern\\fcharset0\\fprq1 SAS Monospace;}{\\f5\\fswiss Arial Narrow;}}");
		out.println("{\\colortbl");
		out.println(";");
		out.println("\\red255\\green255\\blue255;");
		out.println("\\red255\\green0\\blue0;");
		out.println("\\red0\\green255\\blue0;");
		out.println("\\red0\\green0\\blue255;");
		out.println("}");
        if(setup.isIncludeHeaderStylesheet()){
        	out.println("{\\stylesheet");
        	out.println("{\\s0 Normal;}");
        	out.println("{\\s1 \\widctlpar{\\*\\soutlvl0} heading 1;}");
        	out.println("{\\s2 \\widctlpar{\\*\\soutlvl1} heading 2;}");
        	out.println("{\\s3 \\widctlpar{\\*\\soutlvl2} heading 3;}");
        	out.println("{\\s4 \\widctlpar{\\*\\soutlvl3} heading 4;}");
        	out.println("{\\s5 \\widctlpar{\\*\\soutlvl4} heading 5;}");
        	out.println("{\\s6 \\widctlpar{\\*\\soutlvl5} heading 6;}");
        	out.println("{\\s7 \\widctlpar{\\*\\soutlvl6} heading 7;}");
            out.println("}");
        }
		//out.println("{\\rtf\\ansi\\deff0 {\\fonttbl {\\f0 "+ setup.getFont() + ";}}");
		//todo info group
		//page size etc.
		populateAndPrintPageSizingSyntax(setup, out);
		out.println();
		//language and standard headings
		if(RtfDocFormat.COMPATIBILITY_NONE.equals(setup.getCompatibility())) 
			out.println("\\nocompatoptions");
		int fs = setup.getFontSizeRTF();
		out.println("\\deflang1033\\plain\\fs" + fs + "\\f" + setup.getFontId() + " ");
		//out.println();
		//TODO this makes it sasshiato dependent
		SasshiatoSP sp = new SasshiatoSP();
		Properties spprops = sp.getSeProperties();
        if(spprops==null) {
        	SasshiatoTrace.logError("Missing internal configuration");
        	return;
        }
        String compName = spprops.getProperty("companyName", "btcisp.com");
        String compShortName = spprops.getProperty("companyShortName", "btcisp");
        out.println("{\\info");
        out.println("{\\author " + compName + "}");
        if(!"btcisp.com".equals(compName)) out.println("{\\company " + compName+ "}");
        SimpleDateFormat format = new SimpleDateFormat("yyyy'\\mo'M'\\dy'd'\\hr'h'\\min'm");
        
        out.println("{\\creatim\\yr" + format.format(new Date()) + "}");
        String lic_2= "";
        if(!"btcisp.com".equals(compName))
        	lic_2= " licensed to " + compShortName;
        else 
        	lic_2 = " this software is the property of Izabella Peszek";
        out.println("{\\doccomm " + "Generated by Sasshiato " + SasshiatoVersion.ver +  " (by btcisp.com)" + lic_2+ "}");     
        out.println("}");
        
        out.println("{\\*\\generationrrginfo ");
        out.println("" + "Generated by Sasshiato " + SasshiatoVersion.ver +  " (by btcisp.com)" + lic_2+ "");     
         out.println("}");
        out.println("{\\*\\docrrginfo docstart}");
		flush();
	}
	
	private void open_setMarigings(RtfDocFormat setup){
		out.println("\\margl" + setup.getLeftMRTF() + "\\margr" + setup.getRightMRTF() + "\\margt" + setup.getTopMRTF() + "\\margb" + setup.getBottomMRTF());
	}
	private static void open_setMarigings(RtfDocFormat setup, PrintStream out1){
		out1.print("\\margl" + setup.getLeftMRTF() + "\\margr" + setup.getRightMRTF() + "\\margt" + setup.getTopMRTF() + "\\margb" + setup.getBottomMRTF());
	}
	
	public void close(Map info) throws Exception{
		String endinst = appendable?"appendable": "final";
		String extraInfo = "";
		if(info!=null){
			Iterator keys = info.keySet().iterator();
			while(keys.hasNext()){
				String key = (String) keys.next();
				if(key.startsWith("rtf."))
					extraInfo = extraInfo + " " + key + "=" + info.get(key); 
			}
		}
		out.println("{\\*\\eofrrginfo " + endinst + extraInfo + " }}");
		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Closed document with:" +  endinst + extraInfo);
		this.flush();
		out.close();
		tmp_out.close();
	}
	
	public void addParagraph(String[] text, int[] indents, RtfParagraphFormat setup) throws Exception{
		addParagraphImpl(text, indents, setup, false);
	}

	private void addParagraphImpl(String[] text, int[] indents, RtfParagraphFormat setup, boolean forCell) throws Exception{
		if(text==null || text.length ==0) {
			//out.print("\\pard");
			//return;
			//TODO if trying to print empty this code executes, it maybe better to force it as one element array
			text = new String[] {""};
			indents = new int[] {0};
		}
		for (int i = 0; i < text.length; i++) {
			String line = text[i];
			if(!forCell || setup.getStyle()==null || i==0) out.print("\\pard");
			if(forCell) out.print("\\intbl");
			int totIndSize = setup.getPaddingLeft() + (indents[i] * setup.getIndentSize());
			if(totIndSize<0) totIndSize =0;
			if(setup.getAlign()!=null) {
				out.print(setup.getAlign());
			} 
			if(setup.getAlign()!=null && forCell) {
				if(RtfParagraphFormat.ALIGN_LEFT.equals(setup.getAlign())){
					out.print("\\li" + totIndSize);
				} else if(RtfParagraphFormat.ALIGN_RIGHT.equals(setup.getAlign())){	
					out.print("\\ri" + setup.getPaddingRight());
				}
			} else {
				out.print("\\li" + totIndSize);
				out.print("\\ri" + setup.getPaddingRight());
			}
			if(i == text.length -1 && setup.getSpaceAfter() !=-99){
				out.print("\\sa" + setup.getSpaceAfter());
			}
			if(i==0){
				int topSpace = 0;
				boolean applyTopSpace = false;
				if(setup.getSpaceBefore() !=-99) {
					topSpace= setup.getSpaceBefore();
					applyTopSpace = true;
				}
				//if(setup.getLeading() !=-99) {
				//	topSpace = topSpace + setup.getLeading();
				//	applyTopSpace = true;
				//}
				//leading is applied to top.
				if(applyTopSpace) out.print("\\sb" + topSpace);
			} 
            if (setup.getLeading() !=-99){
				out.print("\\sl" + setup.getLeading());
			}
			
            out.print(setup.getBarInstructions());
            if(i==0 && setup.getStyle()!=null){
            	out.println("{" + setup.getStyle());
            }
    		out.print(setup.getExtraInstructions());
   		    String ffInstructions = setup.getFFInstrutions();
    		if(ffInstructions!=null){
    			out.print("{" +  ffInstructions);
    			out.print(" ");
    		}
    		out.print(line);
    		if(ffInstructions!=null){
    			out.println("}");
    		}
			if(!forCell || i< text.length -1) {
				if(!forCell || setup.getStyle()==null) out.print("\\par ");
				else out.print("\\line ");
			}
			if(!forCell && i == text.length -1&& setup.getStyle()!=null){
				out.println("}");
			}
			out.println();
		}
	}

	public void addDirectyRtfSyntaxLine(String syntax){
		out.println(syntax);
	}
	
	public PrintStream startDirectContentMode() throws Exception{
		flush();
		return permanent_out;
	}
	
	public void stopDirectContentMode() throws Exception{
		
	}
	
	private RtfRowFormat reconfigureForPartialBorders(RtfRowFormat rsetup){
		if(!rsetup.usePartialBorderSizing()){
			RtfRowFormat newFormat = new RtfRowFormat();
			int[] colwidths = new int[2* rsetup.getColWidths().length -1];
			   String[] aligns = new String[2* rsetup.getColWidths().length -1];
			   String[] borders = new String[2* rsetup.getColWidths().length -1];;
	
			for (int i = 0; i < rsetup.getColWidths().length; i++) {
			  colwidths[2* i] = rsetup.getColWidths()[i] - 100;
			  if(i==0 || i==rsetup.getColWidths().length-1) colwidths[2* i] = rsetup.getColWidths()[i] -50;
			  aligns[2*i] = rsetup.getAligns()[i];
			  borders[2*i] = rsetup.getBorders()[i];
			}
			
			for (int i = 0; i < colwidths.length; i++) {
				 if(colwidths[i] <=0) colwidths[i] = 100;
				 if(aligns[i] ==null) aligns[i] = "";
				 if(borders[i]==null) {
					 borders[i]= "";
					 if(i>0 && borders[i-1].indexOf(RtfRowFormat.BORDER_TOP) !=-1) {
						 borders[i] = RtfRowFormat.BORDER_TOP;
					 }
				 }
			}
			newFormat.configure(colwidths, rsetup.getTableAlign(), null,null, rsetup.isRepeatable());
			newFormat.setAligns(aligns);
			newFormat.setBorders(borders);
			newFormat.setBorderwidth(rsetup.getBorderwidth());
			newFormat.setCellgap(rsetup.getCellgap());
			newFormat.setHeight(rsetup.getHeight());
			newFormat.setPartialBorders(rsetup.isPartialBorder());
			return newFormat;
		} else {
			RtfRowFormat newFormat = new RtfRowFormat();
			int[] colwidths = new int[2* rsetup.getColWidths().length -1];
			String[] aligns = new String[2* rsetup.getColWidths().length -1];
			String[] borders = new String[2* rsetup.getColWidths().length -1];
			int[] leftSizing = rsetup.getPartialBordersSizeLeft();
			leftSizing[0] = 0;
			int[] rightSizing = rsetup.getPartialBorderSizeRight();
			rightSizing[rightSizing.length-1] = 0;
			
			for (int i = 0; i < rsetup.getColWidths().length; i++) {
			  colwidths[2* i] = rsetup.getColWidths()[i] - leftSizing[i] - rightSizing[i];
			  if(2*i +1 < colwidths.length) {
				  if(i + 1<leftSizing.length)
					  colwidths[2* i +1] = leftSizing[i + 1] + rightSizing[i];
				  else 
					  colwidths[2* i +1] = rightSizing[i];
			  }
			  if(i==0) colwidths[2* i] = rsetup.getColWidths()[i] - rightSizing[i];
			  //if(i==rsetup.getCallWidths().length-1) colwidths[2* i] = rsetup.getCallWidths()[i] - leftSizing[i];
			  aligns[2*i] = rsetup.getAligns()[i];
			  borders[2*i] = rsetup.getBorders()[i];
			}
			
			for (int i = 0; i < colwidths.length; i++) {
				 //if(colwidths[i] <=0) colwidths[i] = leftSizing[i] + rightSizing[i];
				//System.out.println("test " + i + "," + colwidths[i]);
				 if(aligns[i] ==null) aligns[i] = "";
				 if(borders[i]==null) {
					 borders[i]= "";
					 if(i>0 && borders[i-1].indexOf(RtfRowFormat.BORDER_TOP) !=-1) {
						 borders[i] = RtfRowFormat.BORDER_TOP;
					 }
				 }
			}
			newFormat.configure(colwidths, rsetup.getTableAlign(), null,null, rsetup.isRepeatable());
			newFormat.setAligns(aligns);
			newFormat.setBorders(borders);
			newFormat.setBorderwidth(rsetup.getBorderwidth());
			newFormat.setCellgap(rsetup.getCellgap());
			newFormat.setHeight(rsetup.getHeight());
			newFormat.setPartialBorders(rsetup.isPartialBorder());
			return newFormat;
			
		}
	}
	
	
	RtfRowFormat current_row;
	int cellcount = 0;
	public void beginRow(RtfRowFormat rsetup) throws Exception{
		if(rsetup.isPartialBorder()) {
			rsetup = reconfigureForPartialBorders(rsetup);
		}
		current_row= rsetup;
		cellcount = -1;
		int[] widths = rsetup.getRigthReachCoordinates();
		out.print("\\trowd\\trkeep" + (rsetup.isRepeatable()?"\\trhdr":"") + rsetup.getTableAlign() + "\\trtleft0\\trgaph" + rsetup.getCellgap());
		if(rsetup.getHeight() != -99) {
			out.print("\\trrh" + rsetup.getHeight());
		}
		for (int i = 0; i < widths.length; i++) {
			if(rsetup.hasAligns()){
				out.print(rsetup.getAligns()[i]);
			}
			if(rsetup.hasBorders()){
				StringTokenizer st = new StringTokenizer(rsetup.getBorders()[i], "\\");
				while(st.hasMoreTokens()){
					out.print("\\" + st.nextToken());
					out.print("\\brdrw" + rsetup.getBorderwidth() + "\\brdrs ");
				}
			}
			out.print("\\cellx" + widths[i]);		
		}
		out.println("");
		
	}
	
	private void addCellComplete(){
		if(current_row.isPartialBorder()){
			if(this.cellcount < current_row.getRigthReachCoordinates().length -2){
				out.print("\\pard");
				out.print("\\intbl");		
				out.print(" ");
				out.print("\\cell ");
				out.println();	
				cellcount ++;
			}
		}
		cellcount ++;
	}
	public void addCell(String[] text, int[] indents, RtfParagraphFormat setup) throws Exception {
		this.addParagraphImpl(text, indents, setup, true);
		if(setup.getStyle()!=null){
			out.println("\\cell} ");
		} else {
			out.print("\\cell ");
		}
		out.println();	
		addCellComplete();
	}
	
	public void addCellWithDecimalAlignment(String txt, RtfDecimalAlignedCellFormat setup) throws Exception {
        out.print("\\pard");
		out.print("\\intbl");		
		out.print("\\ql");
		
		if(setup.getSpaceAfter() !=-99){
			out.print("\\sa" + setup.getSpaceAfter());
		}
			int topSpace = 0;
			boolean applyTopSpace = false;
			if(setup.getSpaceBefore() !=-99) {
				topSpace= setup.getSpaceBefore();
				applyTopSpace = true;
			}
			//leading is applied to top.
			if(applyTopSpace) out.print("\\sb" + topSpace);
	        if (setup.getLeading() !=-99){
					out.print("\\sl" + setup.getLeading());
		    }

	    out.print("\\tqdec\\tx" + setup.getTabPosition());
		String line = txt.trim();
		if(line.length()==0) line = " ";
		out.print(setup.getExtraInstructions());
		String ffInstructions = setup.getFFInstrutions();
		if(ffInstructions!=null){
			out.print("{" +  ffInstructions);
		}
	    out.print(" ");	
		out.print(line);
		if(ffInstructions!=null){
			out.println("}");
		}
		out.print("\\cell ");
		out.println();
		addCellComplete();
	}
	
	public void addCellWithRightDecimalAlignment(String txt1, String txt2, RtfRightDecimalAlignedCellFormat setup) throws Exception {
		if(txt1==null) txt1=" ";
		if(txt2==null) txt2=" ";
		if(txt1.length()>1) txt1 = StringUtil.leftTrim(txt1);
		if(txt2.length()>1) txt2 = txt2.trim();
		
        out.print("\\pard");
		out.print("\\intbl");		
		out.print("\\ql");
		
		if(setup.getSpaceAfter() !=-99){
			out.print("\\sa" + setup.getSpaceAfter());
		}
			int topSpace = 0;
			boolean applyTopSpace = false;
			if(setup.getSpaceBefore() !=-99) {
				topSpace= setup.getSpaceBefore();
				applyTopSpace = true;
			}
			//leading is applied to top.
			if(applyTopSpace) out.print("\\sb" + topSpace);
	        if (setup.getLeading() !=-99){
					out.print("\\sl" + setup.getLeading());
		    }


		out.print("\\tqr\\tx" + setup.getTabPositionRight());
	    out.print("\\tqdec\\tx" + setup.getTabPositionDec());
		
	    //extra space does not change anything here if there are { } but is not needed, only needed if there is no { }
	    String extraSpace1 = txt1.startsWith("{")?"": " ";
	    String extraSpace2 = txt2.startsWith("{")?"": " ";
		String line = "\\tab" + extraSpace1 + txt1 + "\\tab" + extraSpace2 + txt2;
		out.print(setup.getExtraInstructions());
		String ffInstructions = setup.getFFInstrutions();
		if(ffInstructions!=null){
			out.print("{" +  ffInstructions);
		}
		out.print(line);
		if(ffInstructions!=null){
			out.println("}");
		}
		out.print("\\cell ");
		out.println();
		addCellComplete();
	}
	
	public void addCell(String txt, RtfCellFormat cellsetup) throws Exception {
		out.print("\\pard");
		out.print("\\intbl");		
		out.print(" ");
		out.print(txt);
		out.print("\\cell ");
		out.println();
		addCellComplete();
	}
	
	public void endRow(boolean selfComplete) throws Exception{
		if(selfComplete){
			for(int i=cellcount+1; i<current_row.getRigthReachCoordinates().length; i++){
				out.print("\\pard");
				out.print("\\intbl");		
				out.print(" ");
				out.print("\\cell ");
				out.println();					
			}
		}
		out.println("\\row");
		//parital borders
		int spacing = 80;
		if(!StringUtil.isEmpty(current_row.getPartialBorders())){
			out.println("\\pard");
			String[] ranges = new String[0];
			if(StringUtil.areEqual("all", current_row.getPartialBorders())){
				ranges = new String[current_row.getRigthReachCoordinates().length];
				for (int i = 0; i < ranges.length; i++) {
					ranges[i] = "" + i;
				}
			} else {
				ranges = StringUtil.separateDelimitedText(current_row.getPartialBorders(), ",", true);
			}
			if(ranges.length >0){
				for (int i = 0; i < ranges.length; i++) {
					String range = ranges[i];
					StringTokenizer str = new StringTokenizer(range, "-");
					int count = str.countTokens();
					int firstCol=-1;
					int lastCol=-1;
					if(count == 2){
						firstCol = Integer.parseInt(str.nextToken());
						lastCol = Integer.parseInt(str.nextToken());
					} else if (count ==1){
						firstCol = Integer.parseInt(range);
						lastCol = firstCol;
					} else {
						throw new Exception("Invalid partial broder request " + current_row.getPartialBorders());
					}
					if(firstCol < 0 || lastCol >= current_row.rightreach.length) {
						throw new Exception("Partial broder request " + current_row.getPartialBorders() + " is invalid or out of range");				
					}
					int firstIdx = 0;
					if(firstCol>0) 
						firstIdx = current_row.getRigthReachCoordinates()[firstCol-1];
					int lastIdx = current_row.getRigthReachCoordinates()[lastCol];
					
					if(lastIdx-firstIdx< 2* spacing){
						spacing = (int) lastIdx - firstIdx/8;
					}
					lastIdx = lastIdx-spacing;
					firstIdx = firstIdx + spacing;
				    String line_inst= "{\\*\\do \\dobxcolumn \\dobypara \\dodhgt" + lastIdx + " " +  
                     "\\dpline \\dpptx"+firstIdx + " \\dppty0 \\dpptx" + lastIdx + " " +
                     "\\dppty0 \\dpx"+firstIdx + " \\dpy0 \\dpxsize"+(lastIdx-firstIdx) + " " +
                     "\\dpysize0 \\dplinew15" +
                     "\\dplinecor0 \\dplinecog0 \\dplinecob0}";
				    out.println(line_inst);
				}
			}
		}
	}
	
	public void pageBreak(boolean needsParagraph){
		if(needsParagraph){
			out.println("\\page" + getPageParagraphSeparator());
		} else {
			out.println("\\page");			
		}
	}

	public void beginHeader(int fontSize){
		out.println("{\\header {");
		out.println("\\f" + fontId);
		if(fontSize!=-99){
			out.print("\\fs"+ fontSize);
		}
	}
	public void endHeader(){
		out.println("}}");
	}
	public void beginNewFontSize(int fontSize){
		out.print("{\\fs"+ fontSize);
	}
	public void endNewFontSize(){
		out.println("}");
	}
	
	public void beginFooter(int fontSize){
		out.println("{\\footer {");
		out.println("\\f" + fontId);
		if(fontSize!=-99){
			out.print("\\fs"+ fontSize);
		}
	}
	public void endFooter(){
		out.println("}}");
	}
	public void flush() throws Exception{
		out.flush();
		String page= tmp_out.toString();
		permanent_out.print(page);
		permanent_out.flush();
		tmp_out.reset();
	}
	
	public void writeSpecial(String instructions){
		out.println(instructions);
	}
}
