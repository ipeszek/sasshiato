/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.utils.ConversionUtil;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

/**
 * Documentation of sprops parameter:
 * title_al - title align L/R/C center is default
 * splitchars - string consisting of additional split characters, /-, and [SPACE] are always included.
 * col_sp - extra spacing between columns (def 12f)
 * headu_sp - def 2f head uderline spacing (open space between lines)
 * foot_pos - def B (R/B) running or bottom position of footer
 * super_rs - def 3f superscript raise
 * super_fs - def bodyfontsize-2 superscript font size
 * shead_pd - def 4f system header padding
 * sfoot_pd - def 0f system footer padding
 * body_pd - def 2d after body padding
 * title_pd - def 4f after title padding
 * head_pd - def 4f header bottom padding
 * foot_pd - def 4f before footnote padding
 * foot_ftsp - def 4f separation between FT footers, 4pts is always added to this value
 * <name>_ld - (def 1.2 times fontsize) leading, name=sfoot/foot/head/shead/title/body
 * 
 * rtfpl_<what> - hf <default>, t - place in rtf header/footer or as part of the table
 *              where <what> = shead, sfoot, foot
 * rtf_extlns - (def 2) extra lines used for PDF-RTF compatibility if systemHeaders are placed in table or footers/system
 * footers are placed in table.
*  esc_char - default to /, escape char for things like /tN, /i, /s# and /#, has to be a single char
*              
 * date_fmt defaulted to ddMMMyyyy HH:mm,  any format supported by java.text.SimpleDateFormat
 * data_fmt_uc  true/false defaulted to true, forces date to be upper case.
  */
public class ReportLaF {
	public static final String FONT_STYLE_NORMAL = "N";
	public static final String FONT_STYLE_DEFAULT = "_";
	public static final int COLUMN_SEPARATION_USE_DEFAULT = -1;
	
	public static final String STYLE_TYPE_BODY = "body";
	public static final String STYLE_TYPE_HEADER = "header";
	public static final String STYLE_TYPE_SYS_HEADER = "systemHeader";
	public static final String STYLE_TYPE_FOOTNOTE = "footnote";
	public static final String STYLE_TYPE_SYS_FOOTNOTE = "systemFootnote";
	public static final String STYLE_TYPE_TITLE = "title";
	
	//private static final String COLUMN_SEPARATION_DEFAULT_S = "12f";
	//currently system supports one base font needed for all for calculations. 
	//calculations are done for the body part only, so logically 
	//baseFont==bodyBaseFont.  In the future this should be relaxed
	BaseFont baseFont;
	
	Font symbolFont;
	Font symbolSystemFootnoteFont;
	Font bodyFont;
	Font headerFont;
	Font superscriptFont;
	Font titleFont;
	Font footnoteFont;
	Font systemHeaderFont;
	Font systemFootnoteFont;
	float fontSize = 8;
	Properties props = null;
	float indentSize;
	int indentSizeCh;
	private String splitChars = " ";
	//private char[] rtfSpitChars = null;
	float sfootfontsize;
	int def_leading;
	float colseparation;
	//public ReportLaF() throws Exception{
	//	props = new Properties();
	//}
	float[] dist2nextOverrides;
	float[] decimalOffsets;
	boolean[] has2nextOverrides;
 	RcdInfo myInfo;
 	boolean inText = false;
	boolean headerUnderline_useSepAdj;
	boolean _applySpecialCharReplacementsToNumericCell;
	int fontFamily;
	float paddingAdjustement;
	float emptyLinePDFBottomPadding;
	boolean printEmptyHeaders;
	boolean tabularTitles;

	public ReportLaF(RcdInfo info, int numberOfColumns) throws Exception{
		myInfo = info;
		props = info.getSystemProps();
		//load props from jar/file
		

		float fontsize = info.getFontsize();
		fontFamily = Font.TIMES_ROMAN;
		String fontS = info.getFont();
		if(fontS!=null){
			fontS = fontS.toLowerCase();
			if("courier".equalsIgnoreCase(info.getFont())){
				fontFamily= Font.COURIER;
			} else if("helvetica".equalsIgnoreCase(info.getFont())){
				fontFamily = Font.HELVETICA;
			} else if("timesroman".equalsIgnoreCase(info.getFont())){
				fontFamily= Font.TIMES_ROMAN;
			} else {
				SasshiatoTrace.logError("Unsupported font requested " + fontS + " TIMESROMAN will be used.\n Supported fonts are: TIMESROMAN, HELVETICA, COURIER");
			}
		}
		Font font_tn = new Font(fontFamily, fontsize);
		
		sfootfontsize = info.getSfoot_fs();
		if(sfootfontsize == 0f){
			sfootfontsize = 8;
		}
		Font font_tn8 = new Font(fontFamily, sfootfontsize);
		
		BaseFont bf = font_tn.getCalculatedBaseFont(false);
		
		paddingAdjustement = bf.getDescentPoint("Pykjhg", fontSize);
		paddingAdjustement = (float) Math.ceil(Math.abs(paddingAdjustement));
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Using PDF Padding Adjustement of " + paddingAdjustement);
		
		symbolFont = new Font(Font.SYMBOL, fontsize);
		symbolSystemFootnoteFont = new Font(Font.SYMBOL, sfootfontsize);
		ReportLaF laf = this;
		baseFont = bf;
		bodyFont = createFont(STYLE_TYPE_BODY, null, font_tn);
		systemHeaderFont = createFont(STYLE_TYPE_SYS_HEADER,  null, bodyFont); //backward compatibility body font was used for default before
		footnoteFont = createFont(STYLE_TYPE_FOOTNOTE,  null, font_tn);
		titleFont = createFont(STYLE_TYPE_TITLE,  null, font_tn);
		headerFont = createFont(STYLE_TYPE_HEADER,  null, font_tn);
		systemFootnoteFont  = createFont(STYLE_TYPE_SYS_FOOTNOTE,  null, font_tn8); 
		laf.setFontSize(info.getFontsize());
		
		float requestedIS = 1f;
		
		if(!StringUtil.isEmpty(info.getIndentSize())){
			requestedIS =Float.parseFloat(info.getIndentSize());
		} 
		if(fontFamily==Font.COURIER){
			laf.indentSize = requestedIS * bf.getWidthPoint('a', fontsize);
		} else {
			laf.indentSize = requestedIS * 5f;
		}
	    SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Using indent size=" + laf.indentSize);
		
		//String splitCharsl= props.getProperty("splitchars", "");
		//splitChars = splitCharsl + "/-, ";
		String splitCharsl= props.getProperty("splitchars", "/-,");
		if(splitCharsl.indexOf(",")==-1) splitCharsl = splitCharsl + ",";
		if(splitCharsl.indexOf(" ")==-1) splitCharsl = splitCharsl + " ";
		splitChars = splitCharsl;
		//String rtfSpitCharsS = splitChars;
		//rtfSpitCharsS = StringUtil.replace(rtfSpitCharsS, " ", "");
		//rtfSpitCharsS = StringUtil.replace(rtfSpitCharsS, "-", "");
		//rtfSpitChars = new char[rtfSpitCharsS.length()];
		//for (int i = 0; i < rtfSpitChars.length; i++) {
		//	rtfSpitChars[i] = rtfSpitCharsS.charAt(i);
		//}
		
		//TODO this property is not documented - it should not be set unless problems are found
		float defpadd =  fontsize * .22f;
		if(defpadd <2f) defpadd = 2f;
		String emptyLinePDFBottomPaddingS = props.getProperty("elinePdfPadding", defpadd + "");
		emptyLinePDFBottomPadding = Float.parseFloat(emptyLinePDFBottomPaddingS);

		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: SplitChars used: [" + splitChars + "]");
		def_leading = (int) Math.ceil(1.2f *fontsize);
		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: Default leading is set to " + def_leading);
		
		int def_speparation = (int) Math.ceil(bf.getWidthPoint("w", fontsize));
		String colsepS = props.getProperty("col_sp", def_speparation + "");
		colseparation = ConversionUtil.getSelf().parseFloatWithPoints(colsepS, getDefaultCharWidth());//Float.parseFloat(props.getProperty("col_sp", "12f"));
		int min_colseparation = (int) Math.ceil(bf.getWidthPoint("-", fontsize)/2);
		//System.out.println("min col sep=" + min_colseparation);
		//TODO
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: column separation set to " +colseparation);
		if(min_colseparation +1  > colseparation){
			//colseparation= min_colseparation +1;
			//SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: column separation set to " +colseparation + " which is the minimum allowed value for the specified font");
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: column separation set to " +colseparation + " which is less than the minimum recommened value for the specified font: " + (min_colseparation +1));
			
		} else if (colseparation < 2) {
			SasshiatoTrace.logWarning("Separation value " + colseparation + " is below recommended range. This value should not be less than 2. The recommended value range for specified font is >=" + (min_colseparation +1));
		}
		
		String layouttS = info.getLayoutType();
		if("intext".equalsIgnoreCase(layouttS)){
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "In-Text layout requested");
			inText = true;
		} else if (!StringUtil.isEmpty(layouttS)&& !layouttS.equalsIgnoreCase("std")){
			SasshiatoTrace.logWarning("Invalid layout type requested " + layouttS + " std layout type will be used");
		}
		ArrayList dist2nextA = new ArrayList();
		String dist2next = info.getDist2Next();
		
		String _applySpecialCharReplacementsToNumericCellS = props.getProperty("body_ft_dec_cells", "true");
		_applySpecialCharReplacementsToNumericCell = Boolean.valueOf(_applySpecialCharReplacementsToNumericCellS).booleanValue();
		String headerUnderline_useSepAdjS = props.getProperty("headu_use_d2next", "false");
		headerUnderline_useSepAdj = Boolean.valueOf(headerUnderline_useSepAdjS).booleanValue();
		
		String printEmptyHeadersS = props.getProperty("print_empty_headers", "false");
		printEmptyHeaders = Boolean.valueOf(printEmptyHeadersS).booleanValue();
		
		boolean hasSome2nextOverrides =false;
		if(!StringUtil.isEmpty(dist2next)){
			StringTokenizer stn = new StringTokenizer(dist2next);
			while(stn.hasMoreTokens()){
				String distover = stn.nextToken();
				dist2nextA.add(distover);
			}
			this.dist2nextOverrides = new float[dist2nextA.size()];
			this.has2nextOverrides = new boolean[dist2nextA.size()];
			for (int i = 0; i < dist2nextOverrides.length; i++) {
				String distover = (String) dist2nextA.get(i);
				if("D".equalsIgnoreCase(distover)) {
						dist2nextOverrides[i] = COLUMN_SEPARATION_USE_DEFAULT;
						has2nextOverrides[i] = false;
				} else {
					try{
						dist2nextOverrides[i] = ConversionUtil.getSelf().parseFloatWithPoints(distover, getDefaultCharWidth()); //Float.parseFloat(distover);
						has2nextOverrides[i] = true;
						hasSome2nextOverrides = true;
					}catch(Exception e){
						SasshiatoTrace.logError("Invalid __dist2next value of " + distover);
					}
				}
			}
		}
		decimalOffsets = new float[numberOfColumns];
		int iter = 0;
		String offsets = info.getDoffsets();
		if(offsets==null) offsets = "";
		StringTokenizer stoff = new StringTokenizer(offsets);
		String msg = "";
		while(stoff.hasMoreTokens()){
			String tok = stoff.nextToken();;
			decimalOffsets[iter] = ConversionUtil.getSelf().parseFloatWithPoints(tok, getDefaultCharWidth());
			msg = msg + "\n" + "offset[" + iter + "]=" + decimalOffsets[iter];
			iter++;
		}
		if(StringUtil.isEmpty(msg)){
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "No Decimal Offsets");			
		} else
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Decimal Offsets" + msg);
		if(headerUnderline_useSepAdj && !hasSome2nextOverrides) {
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "No explicit __dist2next overrides specified, header underlines will use default spacing, headu_use_d2next=true is ignored");
			headerUnderline_useSepAdj = false;
		}
		
		tabularTitles = info.isTitleStyle_withTab();
		//charStrict= "true".equalsIgnoreCase(this.props.getProperty("char_strict", "false"));

		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Approximate Title Line Height=" + (getParagraphLeading(STYLE_TYPE_TITLE)));
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Approximate Header Line Height=" + (getParagraphLeading(STYLE_TYPE_HEADER) + getHeaderBottomPadding()));
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Approximate Body Line Height=" + (getParagraphLeading(STYLE_TYPE_BODY)));
	}

	//returns font styles following the StyledChunk conventions
	public List<String> getFontStylesForStyleType(String styletype){
		//throw new RuntimeException("not done");
		String fontface = props.getProperty(this.styletype2propname(styletype)+ "_ff");
		if(fontface == null){
			return StyledChunk.EMPTY_STYLES;
		} else {
			ArrayList<String> res = new ArrayList<String>();
			for(int i=0; i< StyledTextUtil.VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ.length; i++){
				String style = StyledTextUtil.VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ[i];
				if(fontface.indexOf(style) != -1)
					res.add(style);
			}
			return res;
		}
	}
	
	
	
	HashMap baseFonts = new HashMap();
	HashMap allFonts = new HashMap();
	//creates font for styletype and props unless styleOverride is selected.
	//props are used if oneOffFontFace is not passed to figure out default configuration.
	//this has side-effects, configurations are stored as default configurations or overriding configurations
	//"_" is treated as default font face so oneOffFontFace=="_" is like null
	private Font createFont(String styletype, String oneOffFontFace, Font defaultFont){
		String fontface = null;
		if(StringUtil.isEmpty(oneOffFontFace))
			fontface = props.getProperty(this.styletype2propname(styletype)+ "_ff");
		else 
			fontface = oneOffFontFace;
		if(StringUtil.isEmpty(fontface)) 
			return defaultFont;
		else {
			Font result = (Font) allFonts.get(styletype + "_" + fontface);
			if(result == null){
				//TODO needs underline handling
				if(fontface.toLowerCase().indexOf("bf") !=-1 && fontface.toLowerCase().indexOf("it")!=-1){
					result = new Font(defaultFont.getFamily(), defaultFont.getSize(), Font.BOLDITALIC);
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Using BOLDITALIC font face for style type="+ styletype);
				} else if(fontface.toLowerCase().indexOf("bf") !=-1){
					result = new Font(defaultFont.getFamily(), defaultFont.getSize(), Font.BOLD);
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Using BOLD font face for style type="+ styletype);
				} else if(fontface.toLowerCase().indexOf("it") != -1){
					result = new Font(defaultFont.getFamily(), defaultFont.getSize(), Font.ITALIC);
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Using ITALIC font face for style type="+ styletype);
				} else {
					result = defaultFont;
				}
				allFonts.put(styletype + "_" + fontface, result);
			}
			BaseFont bf = result.getCalculatedBaseFont(false);
			if(StringUtil.isEmpty(oneOffFontFace))
				baseFonts.put(styletype, bf);							
			else
				baseFonts.put(fontface, bf);
			return result;
		}
	}
	
	public boolean printEmptyHeaders(){
		return printEmptyHeaders;
	}
	//used to match RFT padding for empty lines
	public float getEmptyLinePDFBottomPadding() {
		return emptyLinePDFBottomPadding;
	}
	public int getFontFamily(){
		return fontFamily;
	}
	public float[] getDecimalOffsets(){
		return decimalOffsets;
	}
	public boolean applySpecialCharReplacementsToNumericCells(){
		return _applySpecialCharReplacementsToNumericCell;
	}
	
	public boolean headerUnderline_useSeparationAdjustement(){
		return headerUnderline_useSepAdj;
	}
	//boolean charStrict = false;
	//public boolean isCharacterStrict(){
	//	return charStrict;
	//}
	private float defCharWidth=-1;
	public float getDefaultCharWidth(){
		if(defCharWidth ==-1){
			BaseFont bf = getBaseFont(STYLE_TYPE_BODY);
			defCharWidth= bf.getWidthPoint("a", getFontSize(STYLE_TYPE_BODY));
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "The default char width is " + defCharWidth);
		}
		return defCharWidth;
	}
	char ESCAPE = 'A';
	public char getEscapeChar(){
		if(ESCAPE =='A'){
			//TODO verify that valid escape char is used
			ESCAPE = getEscapeCharString().charAt(0);
		} 
		return ESCAPE;
	}
	public String getEscapeCharString(){
		return props.getProperty("esc_char", "/").trim();
	}
	String doubleExc = null;
	public String getDoubleEscapeCharString(){
		if(doubleExc==null) {
			doubleExc= getEscapeCharString() + getEscapeCharString();
		}
		return doubleExc;
	}
	public Font getSymbolFont(String styletype) throws Exception{
		if("title".equals(styletype))
			return symbolFont;
		else if (STYLE_TYPE_BODY.equals(styletype) || STYLE_TYPE_HEADER.equals(styletype) ||STYLE_TYPE_SYS_HEADER.equals(styletype))
			return symbolFont;
		else if (STYLE_TYPE_FOOTNOTE.equals(styletype))
			return symbolFont;
		else if (STYLE_TYPE_SYS_FOOTNOTE.equals(styletype))
			return symbolSystemFootnoteFont;
		else
			throw new Exception("Invalid style type for symbol font " + styletype);

	}
	
	public boolean isSeparatorBelowTable() {
		if(isInText()) return false;
		String placement = props.getProperty("bseparator", "t");
		return placement.equalsIgnoreCase("t");
	}
	
	public boolean isSeparatorAboveFootnotes() {
		String placement = props.getProperty("bseparator", "t");
		if(isInText()) return !placement.equalsIgnoreCase("n");
		return placement.equalsIgnoreCase("f");
	}
	
	public boolean isSystemHeaderInRtfHeader(){
		String placement =props.getProperty("rtfpl_shead", "hf");
		return placement.equalsIgnoreCase("hf");
	}
	public boolean isSystemFooterInRtfFooter(){
		String placement =props.getProperty("rtfpl_sfoot", "hf");
		return placement.equalsIgnoreCase("hf");
	}
	public boolean isFootnotesInRtfFooter() {
		String placement =props.getProperty("rtfpl_foot", "hf");
		return placement.equalsIgnoreCase("hf");
	}

	public boolean isSystemHeaderPartOfTable(){
		String placement =props.getProperty("rtfpl_shead", "hf");
		return placement.equalsIgnoreCase("t");
	}
	public boolean isSystemFooterPartOfTable(){
		String placement =props.getProperty("rtfpl_sfoot", "hf");
		return placement.equalsIgnoreCase("t");
	}
	public boolean isFootnotesPartOfTable() {
		String placement =props.getProperty("rtfpl_foot", "hf");
		return placement.equalsIgnoreCase("t");
	}
	public boolean isRestampingRequired() {
		//anything part of table needs to be potentally restamped to add empty par at the end.
		return isSystemHeaderPartOfTable() || isSystemFooterPartOfTable() || isFootnotesPartOfTable();
	}
	public boolean isRftCombinable(){
		String s = props.getProperty("rtf_combinable", "false");
		return Boolean.valueOf(s).booleanValue();
	}
	public boolean isTitlesPartOfTable() {
		//titles should be in table if systemheader is
		String placement =props.getProperty("rtfpl_title");
		if(placement==null) 
			return isSystemHeaderPartOfTable();
		else if(placement.equalsIgnoreCase("t")) {
			return true;
		} else if(placement.equalsIgnoreCase("hf")){
			return false;
		} else if(placement.equalsIgnoreCase("p")){
			return false;
		} else {
			SasshiatoTrace.logError("Invalid property rtfpl_title=" + placement);
			return isSystemHeaderPartOfTable();
		}
	}
	
	public boolean isInText(){
		return this.inText;
	}
	public int getPdfExtraLinesForRtfCompatibility(){
	   if(isSystemHeaderPartOfTable() || isSystemFooterPartOfTable() || isFootnotesPartOfTable()) {
		   String el = props.getProperty("rtf_extlns", "2");
		   int exl = Integer.parseInt(el);
		   return exl;
	   } else 
		   return 0;
	}
	
	//returns true if page starts with paragraph because of how titles etc are printed
	public boolean isRtfPageStartsWithParagraph() {
		//titles should be in table if systemheader is
		return !isSystemHeaderPartOfTable() && !isTitlesPartOfTable() && !tabularTitles;
	}
	public boolean useSectionBreaksWhenAppending(){
		ReportLaF laf = this;
		//System.out.println("got2" + laf.isSystemFooterInRtfFooter());
		return laf.isSystemFooterInRtfFooter() || laf.isSystemHeaderInRtfHeader() || laf.isFootnotesInRtfFooter();
	}
	public String getSplitChars(){
    	return splitChars;
    }

	public float getTabularTitleSeparation(){
		String s = props.getProperty("title_tbsp", "30f");
		return Float.parseFloat(s);
	}

	public float getFTSeparation(){
		String s = props.getProperty("foot_ftsp", "4f");
		return Float.parseFloat(s);
	}
	public String getDateFormat(){
		return props.getProperty("date_fmt", "ddMMMyyyy HH:mm");
	}
	public boolean isDateUpperCase(){
		String prs= props.getProperty("date_fmt_uc", "true");
		return Boolean.valueOf(prs).booleanValue();
	}
	public float getColumnSeparation(){
		return colseparation;
	}
	public float getColumnSeparationOverrideToNext(int column){
		if(dist2nextOverrides==null) return COLUMN_SEPARATION_USE_DEFAULT;
		if(dist2nextOverrides.length > column){
			return dist2nextOverrides[column];
		}else {
			if(!disterrorissued) SasshiatoTrace.logError("__dist2next does not include all columns");
			disterrorissued = true;
			return COLUMN_SEPARATION_USE_DEFAULT;
		}			
	}
	//0 if not adjusted
	boolean disterrorissued = false;
	public float getColumnSeparationAdjustmentLeft(int column){
		if(dist2nextOverrides==null) return 0;
		if(column > 0) {
			column = column -1;
			if(column<0) return 0;
			if(dist2nextOverrides.length > column){
				if(dist2nextOverrides[column]!=COLUMN_SEPARATION_USE_DEFAULT)
					return dist2nextOverrides[column] - colseparation;
				else return 0;
			}else {
				if(!disterrorissued) SasshiatoTrace.logError("__dist2next does not include all columns");
				disterrorissued = true;
				return 0;
			}			
		} else {
			return 0;
		}		
	}
	//0 if not adjusted
	public float getColumnSeparationAdjustmentRight(int column){
		//if(column==3) System.out.println("Test:" + dist2nextOverrides[3] + "," + colseparation);
		if(dist2nextOverrides==null) return 0;
		if(dist2nextOverrides.length > column){
			if(dist2nextOverrides[column]!=COLUMN_SEPARATION_USE_DEFAULT) {
				//System.out.println("Test: returning " + (dist2nextOverrides[column] - colseparation));
				return dist2nextOverrides[column] - colseparation;
			} else 
				return 0;
		}else {
			if(!disterrorissued) SasshiatoTrace.logError("__dist2next does not include all columns");
			disterrorissued = true;
			return 0;
		}		
	}
	public boolean hasColumnSeparationAdjustmentRight(int column){
		if(has2nextOverrides ==null) return false;
		if(has2nextOverrides.length > column){
			return has2nextOverrides[column];
		}else {
			if(!disterrorissued) SasshiatoTrace.logError("__dist2next does not include all columns");
			disterrorissued = true;
			return false;
		}
	}
	public boolean hasColumnSeparationAdjustmentLeft(int column){
		if(has2nextOverrides ==null) return false;
		if(column > 0) {
			column = column -1;
			if(has2nextOverrides.length > column){
				return has2nextOverrides[column];
			}else {
				if(!disterrorissued) SasshiatoTrace.logError("__dist2next does not include all columns");
				disterrorissued = true;
				return false;
			}			
		} else {
			return false;
		}
	}
	public boolean hasColumnSeparationOverrides(){
		return !StringUtil.isEmpty(myInfo.getDist2Next());
	}
	public String getColumnSeparationOverrideInfo(){
		return myInfo.getDist2Next();
	}
	public void setIndentSizeCh(int size){
		indentSizeCh = size;
	}
	public int getIndentSizeCh(){
		return indentSizeCh;
	}
	public float getIndentSize(){
		return indentSize;
	}
	public float[] getSystemFootnoteColumnPercentWidths(){
		return new float[]{ .33f, .34f, .33f};
	}
	
	public float[] getSystemHeaderColumnPercentWidths(){
		return new float[]{ .33f, .34f, .33f};
	}
	
	private String styletype2propname(String styletype){
		if(STYLE_TYPE_SYS_FOOTNOTE.equals(styletype))
			return "sfoot";
		else if (STYLE_TYPE_FOOTNOTE.equals(styletype) )
			return "foot";
		else if (STYLE_TYPE_HEADER.equals(styletype))
			return "head";
		else if (STYLE_TYPE_SYS_HEADER.equals(styletype))
			return "shead";
		else return styletype; //title, body
	}
	private String propname2styletype(String propname){
		if("sfoot".equals(propname))
			return STYLE_TYPE_SYS_FOOTNOTE;
		else if ("foot".equals(propname) )
			return STYLE_TYPE_FOOTNOTE;
		else if ("head".equals(propname))
			return STYLE_TYPE_HEADER;
		else if ("shead".equals(propname))
			return STYLE_TYPE_SYS_HEADER;
		else return propname; //title, body
	}	
	public float getPdfPaddingAdjustement(){
		return paddingAdjustement;
		
	}
	public String getTitleAlign(){
		return (props.getProperty("title_al", "C")).trim().toUpperCase();
	}
	public boolean isBottomFooter(){
		if(isInText()) return false;
		return StringUtil.areEqual("B", props.getProperty("foot_pos", "B").trim());
	}
	public float getSystemHeaderPadding(){
		return Float.parseFloat(props.getProperty("shead_pd", "4f"));
	}
	public float getSystemFooterPadding(){
		return Float.parseFloat(props.getProperty("sfoot_pd", "0f"));
	}
	public float getHeaderUnderlineSpacing(){
		return Float.parseFloat(props.getProperty("headu_sp", "2f"));
	}
	public float getAfterBodyPadding(){
		return Float.parseFloat(props.getProperty("body_pd", "2f"));
	}
	
	public float getAfterTitlePadding(){
		return Float.parseFloat(props.getProperty("title_pd", "4f"));
	}
	public float getHeaderBottomPadding(){
		return Float.parseFloat(props.getProperty("head_pd", "4f"));
	}	
	public float getBeforeFootnotePadding() {
		return Float.parseFloat(props.getProperty("foot_pd", "4f"));
	}
	public float getSuperscriptRaise(String stypetype) throws Exception{
		return Float.parseFloat(props.getProperty("super_rs", "3f"));
	}
	public float getParagraphLeading(String styletype){
		//float fontsize = getFontSize(styletype);
		
		//int def_leading = (int) Math.ceil(1.2f *fontsize);
		//if("header".equals(styletype)) def_leading = 12;
		//else if("title".equals(styletype)) def_leading = 12;
		String propNm= styletype2propname(styletype);
		return Float.parseFloat(props.getProperty(propNm + "_ld", def_leading + "f"));
	}
	public Font getSuperscriptFont(String stypetype) throws Exception{
		if(superscriptFont!=null) return superscriptFont;
		else {
			float fontsize = Float.parseFloat(props.getProperty("super_fs", "" + (bodyFont.getSize() -2)));
			superscriptFont = new Font(bodyFont.getFamily(), fontsize);
		}
		return superscriptFont;
	}
	public float getSuperscriptFontSize(String stypetype) throws Exception{
		if(superscriptFont!=null) return superscriptFont.getSize();
		else {
			float fontsize = Float.parseFloat(props.getProperty("super_fs", "" + (bodyFont.getSize() -2)));
			return fontsize;
		}
	}
	public Font getFont(String styletype) throws Exception{
		if(STYLE_TYPE_TITLE.equals(styletype))
			return titleFont;
		else if (STYLE_TYPE_HEADER.equals(styletype)) {
			return headerFont;
		}
		else if (STYLE_TYPE_SYS_HEADER.equals(styletype))
			return systemHeaderFont;
		else if (STYLE_TYPE_BODY.equals(styletype))
			return bodyFont;
		else if (STYLE_TYPE_FOOTNOTE.equals(styletype))
			return footnoteFont;
		else if (STYLE_TYPE_SYS_FOOTNOTE.equals(styletype))
			return systemFootnoteFont;
		else
			throw new Exception("Invalid style type " + styletype);
	}
	

	public Font getFont(String styletype, String oneOffFontFace) throws Exception {
		if(StringUtil.isEmpty(oneOffFontFace) || StringUtil.areEqual(FONT_STYLE_DEFAULT, oneOffFontFace)){
			return getFont(styletype);
		} else {
			Font f = (Font) this.allFonts.get(styletype + "_" + oneOffFontFace);
			
			if(f==null){
				Font defaultF = null;
				try{
					defaultF = getFont(styletype);
				}catch(Exception e) {
					SasshiatoTrace.logError("No Font for " + styletype);
					defaultF = this.bodyFont;
				}
				createFont(styletype, oneOffFontFace, defaultF);
				f = (Font) this.allFonts.get(styletype + "_" + oneOffFontFace);
			}
			return f;
		}
	}
	public float getFontSize(String styletype) {
		if("systemFootnote".equals(styletype)) return sfootfontsize;
		return fontSize;
	}	
	public float getFontSize() {
		return fontSize;
	}
	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}
	
	@Deprecated
	public Font getBodyFont() {
		return bodyFont;
	}
	
	@Deprecated
	public void setBodyFont(Font bodyFont) {
		this.bodyFont = bodyFont;
	}
	@Deprecated
	public Font getTitleFont() {
		return titleFont;
	}
	@Deprecated
	public Font getFootnoteFont() {
		return footnoteFont;
	}
	@Deprecated
	public Font getSystemFootnoteFont() {
		return systemFootnoteFont;
	}

	//TODO make this private
	public BaseFont getBaseFont(String styletype) {
		BaseFont f = (BaseFont) this.baseFonts.get(styletype);
		if(f==null)
			return baseFont;
		else 
			return f;
	}
	
	//TODO change how oneOffFontFaceOverride are stored, no need to use styletype
	//fontStyleOverride == fontFace
	public BaseFont getBaseFont(String styletype, String oneOffFontFaceOverride){
		if(StringUtil.isEmpty(oneOffFontFaceOverride)){
			return getBaseFont(styletype);
		} else {
			BaseFont f = (BaseFont) this.baseFonts.get(oneOffFontFaceOverride);
			
			if(f==null){
				Font defaultF = null;
				try{
					defaultF = getFont(styletype);
				}catch(Exception e) {
					SasshiatoTrace.logError("No Font for " + styletype);
					defaultF = this.bodyFont;
				}
				createFont(styletype, oneOffFontFaceOverride, defaultF);
				f = (BaseFont) this.baseFonts.get(oneOffFontFaceOverride);
			}
			return f;
		}
	}

	
	/*
	public String toString(){
		StringBuffer res = new StringBuffer();
		res.append("ReportLaF:");
		res.append("=").append(get)
		return res.toString();
	}
	*/
	
}
