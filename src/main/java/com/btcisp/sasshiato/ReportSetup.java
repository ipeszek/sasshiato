/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.utils.ConversionUtil;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

/**
 * sprops:
 * append - true/false default false
 * appendable - true/false default false

 * @author owner
 *
 */
public class ReportSetup {
	// minwidths are generated based on colwidths;
	// actualWidths are calculated based on Recordset;
	// breaksAlowedAt is a function of breakOKat;
	// wrapAllowed is generated based on colwidths;
	// tableWidth and tableHeight is a function of papersize and orientation;
	// numberOfColumns is determined from Recordset;
	// numberOfHeaderColumns is read from Recordset;
	// orientation is read from Recordset;
	// papersize is read from Recordset;
	// colwidths is read from Recordset;
	// breakOKat is read from Recordset;
	// margins are (for now) hard-coded;

	//private float[] minWidths;

	private int[] breaksAlowedAt;
	private String[] widthCalculationInstructions;
	private float tableWidth;
	private float tableHeight;

	private int numberOfColumnsI;
	
	private int numberOfHeaderColumnsI;
	private String orientationI;
	private String papersizeI;
	private String colwidthsI;
	private String breakOKatI;
	private boolean[] stretchFlag ;

	
	private float leftMargin;
	private float rightMargin;
	private float topMargin;
	private float bottomMargin;
	private int[] groupColIds = new int[0];
	private boolean allowFutureAppend = false;
	private boolean append = false;
	boolean isPdfOut;
	boolean isRtfOut;
	boolean isRftSoftLineBreakCalced = true;
	boolean isRtfSoftLineBreakHyphen = false;
	boolean isGenerateSimpleSizingInfo = false;
	boolean applyTextWeightedStretch = true;
	
	private String bookmarks_rtf;
	private String bookmarks_pdf;
	private boolean bookmarksEnabled=true;
	
	public boolean applyTextWeightedStretching(){
		return applyTextWeightedStretch;
	}
	
	public boolean isGenerateSimpleSizingInfo(){
		return isGenerateSimpleSizingInfo; 
	}
	public boolean isRtfSoftLineBreakCalculated(){
		return isRftSoftLineBreakCalced;
	}
	public boolean isRtfSoftLineBreakHyphenated(){
		return isRtfSoftLineBreakHyphen;
	}
	public boolean isAllowFutureAppend() {
		return allowFutureAppend;
	}
	public boolean isAppend(){
		return append;
	}
	
	public int[] getGroupColIds(){
		return groupColIds;
	}
	public int getNumberOfColumnsI() {
		return numberOfColumnsI;
	}

	public int getNumberOfHeaderColumnsI() {
		return numberOfHeaderColumnsI;
	}

	public String getOrientationI() {
		return orientationI;
	}

	public String getPapersizeI() {
		return papersizeI;
	}

	public String getColwidthsI() {
		return colwidthsI;
	}

	public String getBreakOKatI() {
		return breakOKatI;
	}

	public float getLeftMargin() {
		return leftMargin;
	}

	public float getRightMargin() {
		return rightMargin;
	}

	public float getTopMargin() {
		return topMargin;
	}

	public float getBottomMargin() {
		return bottomMargin;
	}



	

	public int[] getBreaksAlowedAt() {
		return breaksAlowedAt;
	}

	public void setBreaksAlowedAt(int[] breaksAlowedAt) {
		this.breaksAlowedAt = breaksAlowedAt;
	}

	public String[] getWidthCalculationInstructions() {
		return widthCalculationInstructions;
	}

	public boolean[] getStretchFlags(){
	
		return stretchFlag;
	}

	public float getTableWidth() {
		return tableWidth;
	}

	public void setTableWidth(float tableWidth) {
		this.tableWidth = tableWidth;
	}
	public ReportSetup(float[] actualWidths, int numberOfColumns,
		 RcdInfo info) {
	
	}



	public ReportSetup(int numberOfColumnsI,
			int numberOfHeaderColumnsI, RcdInfo info, ReportLaF laf, boolean isEmpty, Properties ssprops) {
		super();
		this.numberOfColumnsI = numberOfColumnsI;
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: table total column count is " + numberOfColumnsI);
		this.numberOfHeaderColumnsI = numberOfHeaderColumnsI;
		this.orientationI = info.getOrient();
		if(this.orientationI==null) this.orientationI = "L";
		this.papersizeI = info.getPapersize();
		this.colwidthsI = info.getColwidths();
		this.breakOKatI = info.getBreakOkAt();
		String stretchFlagS = info.getStretch();
		this.stretchFlag = new boolean[numberOfColumnsI];
		if(StringUtil.isEmpty(stretchFlagS)) stretchFlagS = "Y"; //default only first column to streachable
		StringTokenizer stx = new StringTokenizer(stretchFlagS, " ");
		int count =0;
		while(stx.hasMoreTokens() && count < stretchFlag.length){
			String nextFlag = stx.nextToken();
			stretchFlag[count] = "Y".equals(nextFlag.toUpperCase());
			count ++;
		}
		StringBuffer sb0 = new StringBuffer();
		for (int i = 0; i < stretchFlag.length; i++) {
			sb0.append(i + "=" + stretchFlag[i] + ",");
		}
	    SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: Requested/defaulted streach configuration is: " + sb0);


		// set margins;
		//  margins = {72f,72f,72f,72f};
	    
		topMargin=72f;
		rightMargin=72f;
		bottomMargin=72f;
		leftMargin=72f;
	    String marigins = info.getMargins();

	    if(marigins !=null){
	    	StringTokenizer stm = new StringTokenizer(marigins, " ");
	    	try{
		    	if(stm.hasMoreTokens()){
		    		String token = stm.nextToken();
		    		topMargin = (int) Math.ceil(ConversionUtil.getSelf().parseFloatWithPoints(token, -1f));
		    	}
		    	if(stm.hasMoreTokens()){
		    		String token = stm.nextToken();
		    		rightMargin = (int) Math.ceil(ConversionUtil.getSelf().parseFloatWithPoints(token, -1f));    		
		    	}
		    	if(stm.hasMoreTokens()){
		    		String token = stm.nextToken();
		    		bottomMargin =  (int) Math.ceil(ConversionUtil.getSelf().parseFloatWithPoints(token, -1f));    		
		    	}
		    	if(stm.hasMoreTokens()){
		    		String token = stm.nextToken();
		    		leftMargin = (int) Math.ceil(ConversionUtil.getSelf().parseFloatWithPoints(token, -1f));    		
		    	}
	    	}catch(Exception e){
	    		SasshiatoTrace.logError("Invalid __margins configuration in RcdInfo", e);
	    	}
	    }
	    	
	    SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: Requested/defaulted margins are (in points): T=" + topMargin +" ,R=" + rightMargin + " ,B=" + bottomMargin + ",L" + leftMargin);
	    	
        // top, bottom, left, right;
		
		// set table width and height;
		if ("LETTER".equalsIgnoreCase(papersizeI)) {
			if ("P".equalsIgnoreCase(orientationI)) {
				tableWidth = 72f * 8.5f;
				tableHeight = 72f * 11f;
			} else {
				tableWidth = 72f * 11f;
				tableHeight = 72f * 8.5f;
			}
		} else {
			if ("P".equalsIgnoreCase(orientationI)) {
				tableWidth = 72f * 8.27f;
				tableHeight = 72f * 11.69f;
			} else {
				tableWidth = 72f * 11.69f;
				tableHeight = 72f * 8.27f;
			}
		}
		tableWidth = tableWidth - leftMargin-rightMargin;
		tableHeight = tableHeight - topMargin-bottomMargin;

		// parse colwiths - set minwidths, wrapAllowed, ;
		int counter = 0;

		widthCalculationInstructions = new String[numberOfColumnsI];
		if(widthCalculationInstructions.length >0) {
			if (StringUtil.isEmpty(colwidthsI)) {
				
				if(numberOfHeaderColumnsI >0)
					widthCalculationInstructions[0] = "F72";
				else 
					widthCalculationInstructions[0] = "F15";
				
				for (int i = 1; i < widthCalculationInstructions.length; i++) {
					widthCalculationInstructions[i] = "N";
				}
	
			} else {
				StringTokenizer st = new StringTokenizer(colwidthsI, " ");
				counter = -1;
				while (st.hasMoreTokens() && counter + 1 <widthCalculationInstructions.length) {
					counter = counter + 1;
					String s = st.nextToken();
					if("N".equalsIgnoreCase(s) || "LW".equalsIgnoreCase(s) || "NH".equalsIgnoreCase(s)|| "LWH".equalsIgnoreCase(s)){
						widthCalculationInstructions[counter] = s;
					} else if ("A".equalsIgnoreCase(s)){
						if(numberOfHeaderColumnsI >counter)
							widthCalculationInstructions[counter] = "F72";
						else 
							widthCalculationInstructions[counter] = "F15";					
					} else {
						try {
							float reqSize = ConversionUtil.getSelf().parseFloatWithPoints(s, laf.getDefaultCharWidth());
							widthCalculationInstructions[counter] = "F" + reqSize;					
						} catch (Exception ex) {
							// do nothing;
							SasshiatoTrace.logWarning("Invalid data for __colwidths for datatype=RINFO: " + colwidthsI + " instruction " + s + " replaced with " + "N", ex);							
							
							widthCalculationInstructions[counter] = "N";
						}												
					}
				}
				if(counter < widthCalculationInstructions.length -1){
					String repeatinst = "N";
					if(counter > 0)
					    repeatinst = widthCalculationInstructions[counter];
					//this handles empty colwiths or single column colwith
						for (int i = counter +1 ; i < widthCalculationInstructions.length; i++) {
						widthCalculationInstructions[i] = repeatinst;
					}
				}			    
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < widthCalculationInstructions.length; i++) {
			sb.append(i + "=" +widthCalculationInstructions[i] + ",");
		}
	    SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: Requested/defaulted input call widths are (numeric entries are in points): " + sb );

		StringTokenizer st;
		// parse breakokAt;
		if (breakOKatI == null) {

		} else {
			st = new StringTokenizer(breakOKatI, " ");
			int numofb = st.countTokens();
			breaksAlowedAt = new int[numofb];
			counter = 0;
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				try {
					breaksAlowedAt[counter] = (int) Float.parseFloat(s);
					counter = counter + 1;
				} catch (NumberFormatException ex) {
					// do nothing;
					SasshiatoTrace.logError("Unexpected breakatok value for datatype=RINFO:" + breakOKatI);
					ex.printStackTrace();
				}
			}
		}
    
   
		if(!StringUtil.isEmpty(info.getGroupColumns())) {
			String[] ids = StringUtil.separateDelimitedText(info.getGroupColumns(), " ", true);
			groupColIds = new int[ids.length];
			for (int i = 0; i < ids.length; i++) {
				groupColIds[i] = Integer.parseInt(ids[i]);
				if(groupColIds[i] >= this.numberOfColumnsI) {
					if(!isEmpty) SasshiatoTrace.logError("Requested (RINFO) __gcols value of " + groupColIds[i] + " exceeds requested column cout of " + this.numberOfColumnsI);
					groupColIds[i] = -1;
				}
			}
		}
		
		Properties props = info.getSystemProps();
		String appendS= props.getProperty("append", "false");
		append = Boolean.valueOf(appendS).booleanValue();
		
		String applyTextWeightedStretchS = props.getProperty("stretch_tw", "true");
		applyTextWeightedStretch = Boolean.valueOf(applyTextWeightedStretchS).booleanValue();
		
		String allowFutureAppendS= props.getProperty("appendable", "false");
		allowFutureAppend = Boolean.valueOf(allowFutureAppendS).booleanValue();
		
		String dest = info.getDest();
		if(StringUtil.isEmpty(dest) || dest.toLowerCase().indexOf("app")!=-1 || dest.toLowerCase().indexOf("csr") !=-1){
			dest = "pdf,rtf";
		}
		isPdfOut = dest.toLowerCase().indexOf("pdf") !=-1;
		isRtfOut = dest.toLowerCase().indexOf("rtf") !=-1;
		
		if(isPdfOut && ssprops.getProperty("outputs", "pdf").indexOf("pdf") ==-1){
			SasshiatoTrace.logWarning("Use of pdf output direction is not licensed");
			isPdfOut = false;
		}
		if(isRtfOut && ssprops.getProperty("outputs", "pdf").indexOf("rtf") ==-1){
			SasshiatoTrace.logWarning("Use of rtf output direction is not licensed");
			isRtfOut = false;
		}
	    String expDate = ssprops.getProperty("expirationDate", "2000-01-01");
	    if(expDate.indexOf(",")!=-1){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    	StringTokenizer stexp = new StringTokenizer(expDate, ",");
	    	while(stexp.hasMoreTokens()){
	    		String nexted = stexp.nextToken();
	    		if(nexted.indexOf(":")!=-1){
		    	    String expDate0 = nexted.substring(nexted.indexOf(":")+1);
		    	    //System.out.println("got "+ expDate0);
		    	    try{
			    		Date end = sdf.parse(expDate0);
			    		Date now = new Date();	
			    		if(isPdfOut && nexted.startsWith("pdf:") && end.before(now)){
			    			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Use of pdf output direction license expired");
			    			isPdfOut = false;
		    			
			    		} 
			    		if(isRtfOut && nexted.startsWith("rtf:") && end.before(now)){
			    			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Use of rtf output direction license expired");
			    			isRtfOut = false;	    			
			    		}
		    	    }catch(Exception e) {
		    	    	SasshiatoTrace.logError("Invalid sashiato license file");
		    	    }
	    		}
	    	}
	    }

		String rtfLineSplitMethod = props.getProperty("rtf_linesplit", "calc");
		isRftSoftLineBreakCalced ="calc".equalsIgnoreCase(rtfLineSplitMethod);
		isRtfSoftLineBreakHyphen="hyphen".equalsIgnoreCase(rtfLineSplitMethod);
		if(!isRftSoftLineBreakCalced && !isRtfSoftLineBreakHyphenated() && !"none".equalsIgnoreCase(rtfLineSplitMethod)) {
			SasshiatoTrace.logError("Invalid __sprops rtf_linesplit value " + rtfLineSplitMethod);
		}
		
		String sizingInfoS = props.getProperty("gen_size_info", "none");
		isGenerateSimpleSizingInfo = false;
		if ("simple".equalsIgnoreCase(sizingInfoS)) {
			if(laf.getFontFamily()!=Font.COURIER){
				SasshiatoTrace.logError("__sprops gen_size_info value of " + sizingInfoS + " can only be used with COURIER font");
			} else {
				int requestedIS = 1;
				
				if(!StringUtil.isEmpty(info.getIndentSize())){
					try{
						requestedIS =Integer.parseInt(info.getIndentSize());
					}catch(Exception e){
						SasshiatoTrace.logError("Non integer Indent Size cannot be used with __sprops gen_size_info value of " + sizingInfoS + " the generated sizing information may be incorrect");						
					}
				} 
				laf.setIndentSizeCh(requestedIS);
			
				isGenerateSimpleSizingInfo = true;
			}
		} else if(!"none".equalsIgnoreCase(sizingInfoS)) {
			SasshiatoTrace.logError("Invalid __sprops gen_size_info value " + sizingInfoS);
		}
		bookmarksEnabled = Boolean.valueOf(props.getProperty("bookmarks_enabled", "true")).booleanValue();
		bookmarks_pdf = info.getBookmarksPdf();
		bookmarks_rtf = info.getBookmarksRtf();

		
		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Output direction is pdf=" + isPdfOut + ",rtf=" + isRtfOut);
	}

	public float getTableHeight() {
		return tableHeight;
	}

	public void setTableHeight(float tableHeight) {
		this.tableHeight = tableHeight;
	}
	
	public boolean isPdfOut(){
		return isPdfOut || (isRtfOut && SasshiatoTrace.isTracing(SasshiatoTrace.LEV_ALL_EXTRA_DEBUG));
	}
	
	public boolean isRtfOut(){
		return isRtfOut;
	}

	public String getBookmarksPdf(){
		return bookmarks_pdf;
	}
	public String getBookmarksRtf(){
		return bookmarks_rtf;
	}
	
	public boolean isBookmarksEnabled(){
		return bookmarksEnabled;
	}
	
}
