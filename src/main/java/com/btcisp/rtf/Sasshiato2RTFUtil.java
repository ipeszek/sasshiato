/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

import java.util.List;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.sasshiato.RcdConstants;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.ReportSetup;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.StyledChunk;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;

public class Sasshiato2RTFUtil {
	private static Sasshiato2RTFUtil self = new Sasshiato2RTFUtil();
	
	public static Sasshiato2RTFUtil getSelf() {
		return self;
	}
	
	public String convertFont2FFInstructions(Font f){
		int style = f.getStyle();
		switch(style){
		case Font.NORMAL: return null;
		case Font.BOLD: return "\\b";
		case Font.ITALIC: return "\\i";
		case Font.BOLDITALIC: return "\\b\\i";
		default: return null;
		}
	}

	public String convertBorderInstructions(String initialInst, int border){
		StringBuffer sb = new StringBuffer();
		if(initialInst != null)
			sb.append(initialInst);
		if((border & Rectangle.BOTTOM) >0 && initialInst.indexOf(RtfRowFormat.BORDER_BOTTOM) == -1)
			sb.append(RtfRowFormat.BORDER_BOTTOM);
		if((border & Rectangle.TOP) >0 && initialInst.indexOf(RtfRowFormat.BORDER_TOP) == -1)
			sb.append(RtfRowFormat.BORDER_TOP);
		if((border & Rectangle.RIGHT) >0 && initialInst.indexOf(RtfRowFormat.BORDER_RIGHT) == -1)
			sb.append(RtfRowFormat.BORDER_RIGHT);
		if((border & Rectangle.LEFT) >0 && initialInst.indexOf(RtfRowFormat.BORDER_LEFT) == -1)
			sb.append(RtfRowFormat.BORDER_LEFT);
		return sb.toString();
	}
	
	public String[] convertAlignments(String[] rrgaligns) throws Exception {
		String[] result = new String[rrgaligns.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = convertAlignment(rrgaligns[i]);
		}
		return result;
	}
	public String convertAlignment(String rrgalign) throws Exception{
		String align = null;
		   if("L".equals(rrgalign)){
			   align = RtfParagraphFormat.ALIGN_LEFT;
		   } else if ("R".equals(rrgalign)){
			   align = RtfParagraphFormat.ALIGN_RIGHT;		   
		   } else if ("C".equals(rrgalign)){
			   align = RtfParagraphFormat.ALIGN_CENTER;
		   } else {
			   align="";
		   }
		   return align;
	}
	public RtfDocFormat convert(RcdInfo info, ReportSetup reportSetup, ReportLaF laf) throws Exception{
		RtfDocFormat result = new RtfDocFormat();
		//font
		int fontid = convert2FontId(info.getFont());
		result.setFontId(fontid);
		result.setFontSizeRTF(convert2FontSize(info.getFontsize()));
		//paper sizes
		result.setOrient(reportSetup.getOrientationI());
		result.setPaperSize(reportSetup.getPapersizeI());
		//margins
		result.setTopMRTF(convert2Twips(reportSetup.getTopMargin()));
		result.setBottomMRTF(convert2Twips(reportSetup.getBottomMargin()));
		result.setLeftMRTF(convert2Twips(reportSetup.getLeftMargin()));
		result.setRightMRTF(convert2Twips(reportSetup.getRightMargin()));
		result.setTopparagraphleadning(convert2Twips(laf.getParagraphLeading("body")));
		result.setIncludeHeaderStylesheet(reportSetup.isBookmarksEnabled());
		return result;
	}
	
	public int convert2FontId(String font) throws Exception{
		int fontid = 0;
		if(StringUtil.isEmpty(font)) font= "timesroman";

		if("courier".equalsIgnoreCase(font)){
			fontid= 3;
		}else if ("symbol".equalsIgnoreCase(font)){
			fontid= 1;
		}else if ("helvetica".equalsIgnoreCase(font)){ //arial
			fontid= 2;
		}else if ("arialnarrow".equalsIgnoreCase(font)){
			fontid= 5;
		} else if("timesroman".equalsIgnoreCase(font)){
			fontid= 0;
		} else if("SASMonospace".equalsIgnoreCase(font)){
			fontid= 4;
		} else {
			throw new Exception("Unsupported font requested " + font + " TIMESROMAN will be used.\n Supported fonts are: TIMESROMAN, COURIER");
		}
        return fontid;
	}
	public int convert2Twips(float points){
		//return 20 * (int) points;
		return (int) (20f * points);
	}
	public int[] convert2Twips(float[] points, float forceTotPoints){
		float totPoints=0;
		int totTwips =0;
		int[] result = new int[points.length];
		for (int i = 0; i < result.length; i++) {
			totPoints= totPoints + points[i];
			result[i] = convert2Twips(points[i]);
			totTwips= totTwips + result[i];
		}
		if(forceTotPoints >0) totPoints = forceTotPoints;
		int calTotTwips = convert2Twips(totPoints);
		//System.out.println("totPoints=" + totPoints + ",totTwips=" + totTwips);
		if(result.length>0){
			result[0] = result[0] - totTwips + calTotTwips;
		}
		return result;
	}
	
	public int convert2FontSize(float fontsize){
		return 2* ((int) fontsize);
	}
	public String convertText(String txt) {
		return txt;
	}
	public String[] convertText(String[] txt){
		return txt;
	}
}
