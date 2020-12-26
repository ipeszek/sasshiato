/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *  
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rrg.rcdparser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.btcisp.sasshiato.HeaderRow;
import com.btcisp.sasshiato.InvalidStylingSyntaxException;
import com.btcisp.sasshiato.RcdConstants;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.sasshiato.StyledTextUtil;
import com.btcisp.utils.StringUtil;

public class RcdInfo {
	String __dest, __filename, __path, __version, __orient, __colwidths, __breakOkAt,
	__title1, __title1_cont, __title2, __title3, __title4, __title5, __title6, __footnot1,
	__footnot2, __footnot3, __footnot4, __footnot5, __footnot6, __footnot7, __footnot8, 
    __footnot9, __footnot10, __footnot11, __footnot12, __footnot13, __footnot14,
	__systitle, __pgmname, __shead_l, __shead_m,__shead_r, __sfoot_l, __sfoot_m,__sfoot_r, __sprops, __bookmarks_rtf,__bookmarks_pdf, 
	__stretch, __font, __nodatamsg, __indentsize, __margins , __rtype, __gcols, __dist2next, __layouttype, __doffsets;
	float __fontsize, __sfoot_fs;
	String __papersize = "LETTER";
	int __lastcheadid = 0;
	boolean isFootWithFT = false;
	boolean isTitleWithTab = false;
	Properties __systemProperties;
	
	public void loadMap(Map map) throws Exception{
		__dest = StringUtil.trim((String) map.get("__dest"));
		//__dest="rtf";
		__filename= StringUtil.trim((String)map.get("__filename"));
		//__filename="alltables";
		__papersize = StringUtil.trim((String)map.get("__papersize"));
		if(StringUtil.isEmpty(__papersize)) __papersize = "LETTER";
		__pgmname= StringUtil.trim((String)map.get("__pgmname"));
		String fontsize=StringUtil.trim((String) map.get("__fontsize"));
		if (StringUtil.isEmpty(fontsize)) fontsize="10";
		__fontsize = Float.parseFloat(fontsize);
		__path = StringUtil.trim((String)map.get("__path"));
		__version = (String)map.get("__version");
		__orient = StringUtil.trim((String)map.get("__orient"));
		if(StringUtil.isEmpty(__orient)) __orient = "L";
		if(__orient != null) __orient = __orient.toUpperCase();
		__colwidths = StringUtil.trim((String) map.get("__colwidths"));
		if(__colwidths!=null) __colwidths = __colwidths.toUpperCase();
		__breakOkAt = StringUtil.trim((String) map.get("__breakokat"));
		__title1 =  checkTitle((String) map.get("__title1"));
		__title1_cont = (String) map.get("__title1_cont");
		__title2 = checkTitle((String) map.get("__title2"));
		__title3 = checkTitle((String) map.get("__title3"));
		__title4 = checkTitle((String) map.get("__title4"));
		__title5 = checkTitle((String) map.get("__title5"));
		__title6 = checkTitle((String) map.get("__title6"));
		__footnot1 = checkFootnote((String) map.get("__footnot1"));
		__footnot2 = checkFootnote((String) map.get("__footnot2"));
		__footnot3 = checkFootnote((String) map.get("__footnot3"));
		__footnot4 = checkFootnote((String) map.get("__footnot4"));
		__footnot5 = checkFootnote((String) map.get("__footnot5"));
		__footnot6 = checkFootnote((String) map.get("__footnot6"));
		__footnot7 = checkFootnote((String) map.get("__footnot7"));
		__footnot8 = checkFootnote((String) map.get("__footnot8"));
        __footnot9 = checkFootnote((String) map.get("__footnot9"));
        __footnot10 = checkFootnote((String) map.get("__footnot10"));
        __footnot11 = checkFootnote((String) map.get("__footnot11"));
        __footnot12 = checkFootnote((String) map.get("__footnot12"));
        __footnot13 = checkFootnote((String) map.get("__footnot13"));
        __footnot14 = checkFootnote((String) map.get("__footnot14"));
        __systitle = (String) map.get("__systitle");
		__stretch = (String) map.get("__stretch");
		//System.out.println("stretch is "+ __stretch);
		__sprops = (String) map.get("__sprops");
		__bookmarks_rtf = (String) map.get("__bookmarks_rtf");
		__bookmarks_pdf = (String) map.get("__bookmarks_pdf");
			
		__shead_l = (String) map.get("__shead_l");
		__shead_m = (String) map.get("__shead_m");
		__shead_r = (String) map.get("__shead_r");
		__sfoot_l = (String) map.get("__sfoot_l");
		__sfoot_m = (String) map.get("__sfoot_m");
		__sfoot_r = (String) map.get("__sfoot_r");
		String _sfoot_fontsize=StringUtil.trim((String) map.get("__sfoot_fs"));
		if (StringUtil.isEmpty(_sfoot_fontsize)) _sfoot_fontsize="8";
		__sfoot_fs = Float.parseFloat(_sfoot_fontsize);
		
		//auto fill in
	    //if(__sfoot_l ==null &&  !StringUtil.isEmpty(__pgmname)) 
	    //	__sfoot_l = "Program: " + __pgmname;

	    __font = StringUtil.trim((String) map.get("__font"));
	    __nodatamsg = StringUtil.trim((String) map.get("__nodatamsg"));
	   __indentsize = StringUtil.trim((String) map.get("__indentsize"));
	   __margins = StringUtil.trim((String) map.get("__margins"));
	   
	   String __lastcheadids = StringUtil.trim((String) map.get("__lastcheadid"));
	   if(!StringUtil.isEmpty(__lastcheadids))
		   __lastcheadid= Integer.parseInt(__lastcheadids);
	   
	   __rtype = StringUtil.trim((String) map.get("__rtype"));
	   __gcols = StringUtil.trim((String) map.get("__gcols"));
	   __dist2next=  StringUtil.trim((String) map.get("__dist2next"));
	   __doffsets=  StringUtil.trim((String) map.get("__doffsets"));
	   __layouttype = StringUtil.trim((String) map.get("__layouttype"));
	   __systemProperties = new Properties();
		//load props from jar/file
	    InputStream is0 = getClass().getClassLoader().getResourceAsStream("sasshiato.props");
	    if(is0!=null){
		    __systemProperties.load(is0);
		    is0.close();
		    SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "Default Properties loaded " + __systemProperties);
	    }
		
		if(!StringUtil.isEmpty(__sprops)) {
			String sysProps = __sprops;
			sysProps = StringUtil.replace(sysProps, ",", "\r\n");
			InputStream is = new ByteArrayInputStream(sysProps.getBytes());
			__systemProperties.load(is);
			is.close();	
			
			Enumeration keys = __systemProperties.propertyNames();
			while(keys.hasMoreElements()){
				String key = (String) keys.nextElement();
				String value = __systemProperties.getProperty(key);
				if(value==null) value = "";
				if(!value.equals(value.trim())){
					SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Adjusting system property for ["+ key+ "] from [" +value + "] to [" + value.trim()+"]");
					__systemProperties.setProperty(key, value.trim());
				}
				//this should never execute, keys should be trimmed when loading
				//if it is there maybe a mismatch with previously loaded props
				if(key!=null && !key.equals(key.trim())){
					SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Adjusting system property key from ["+ key+"]" + "to [" + key.trim()+"]");
					__systemProperties.remove(key);
					__systemProperties.put(key.trim(), value);
				}
			}
		    SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Requested Properties loaded " + __systemProperties);
		}

	}
	
	private String checkFootnote(String footnote){
		if(!StringUtil.isEmpty(footnote)){
			if(footnote.indexOf(RcdConstants.LEFT_FOOTNOTE_TAB)!=-1 || footnote.indexOf(RcdConstants.RIGHT_FOOTNOTE_TAB)!=-1){
				isFootWithFT = true;
			}
		}
		return footnote;
	}
	
	private String checkTitle(String title){
		if(!StringUtil.isEmpty(title)){
			if(title.indexOf(RcdConstants.LEFT_TITLE_TAB)!=-1 || title.indexOf(RcdConstants.RIGHT_TITLE_TAB)!=-1){
				isTitleWithTab = true;
			}
		}
		return title;
	}
	public String getDoffsets(){
		return __doffsets;
	}
	public String getLayoutType(){
		return __layouttype;
	}
	public String getDist2Next(){
		 return __dist2next;
	}	
	public String getReportType(){
		return __rtype;
	}
	public String getGroupColumns() {
		return __gcols;
	}
	
	public boolean isTitleStyle_withTab() {
		return isTitleWithTab;
	}
	public boolean isFooterStyle_withFT(){
		return isFootWithFT;
	}
	public String getMargins(){
		return __margins;
	}
	public String getIndentSize(){
		return __indentsize;
	}
	public String getNodatamsg(){
		return __nodatamsg;
	}
	public String getFont(){
		return __font;
	}
	public String getStretch(){
		return __stretch;
	}
	public String getDest() {
		return __dest;
	}

	public String getFilename() {
		return __filename;
	}

	public String getPgmname() {
		if (__pgmname==null) __pgmname=__filename;
		return __pgmname;
	}

	public String getPath() {
		return __path;
	}

	public String getVersion() {
		return __version;
	}

	public String getOrient() {
		return __orient;
	}

	public String getColwidths() {
		return __colwidths;
	}

	public String getBreakOkAt() {
		return __breakOkAt;
	}


	public StyledText[] getTitles(ReportLaF laf) throws InvalidStylingSyntaxException{
		String[] result= new String[] {
				__title1,
				__title2,
				__title3,
				__title4,
				__title5,
				__title6
		};
		StyledText[] res = new StyledText[result.length];
		for (int i = 0; i < result.length; i++) {
			String txt = result[i];
			if(txt == null)
				res[i] = StyledText.EMPTY;
			else
				res[i] = StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_TITLE), txt, true);
		}
		return res;	
	}
	
	
	public String getTitle1Cont(){
		return this.__title1_cont;
	}

	public StyledText[] getFootnotes(ReportLaF laf) throws InvalidStylingSyntaxException {
		ArrayList<String> foots = new ArrayList<String>();
		if (__footnot1!=null) foots.add(__footnot1);
		if (__footnot2!=null) foots.add(__footnot2);
		if (__footnot3!=null) foots.add(__footnot3);
		if (__footnot4!=null) foots.add(__footnot4);
		if (__footnot5!=null) foots.add(__footnot5);
		if (__footnot6!=null) foots.add(__footnot6);
		if (__footnot7!=null) foots.add(__footnot7);
		if (__footnot8!=null) foots.add(__footnot8);
        if (__footnot9!=null) foots.add(__footnot9);
        if (__footnot10!=null) foots.add(__footnot10);
        if (__footnot11!=null) foots.add(__footnot11);
        if (__footnot12!=null) foots.add(__footnot12);
        if (__footnot13!=null) foots.add(__footnot13);
        if (__footnot14!=null) foots.add(__footnot14);
		
		ArrayList<StyledText> res = new ArrayList<StyledText>();
		for (int i = 0; i < foots.size(); i++) {
			String foot =  foots.get(i);
			if(foot == null)
				res.add(StyledText.EMPTY);
			else 
				res.add(StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_FOOTNOTE), foot, true));
		}
		StyledText[] result= (StyledText[]) res.toArray(new StyledText[0]);
		
		return result;	
	}

	

	public String getSystitle() {
		return __systitle;
	}

	
	

	public String toString(){
		return "RcdInfo(__filename=" + __filename + ",__dest=" + __dest +
		",__version=" + __version +",__orient=" + __orient+",__papersize="+__papersize+",__colwidths=" + __colwidths+",__breakOkAt=" +__breakOkAt +
		",__stretch=" +__stretch +",__sprops=" +__sprops +  "__bookmarks_pdf=" + __bookmarks_pdf +  "__bookmarks_rtf=" + __bookmarks_rtf + ")";		
	}

	
	public float getFontsize() {
		return __fontsize;
	}

	public String getPapersize() {
		return __papersize;
	}

	public int getLastcheadid() {
		return __lastcheadid;
	}

	public float getSfoot_fs() {
		return __sfoot_fs;
	}

	public void setSfoot_fs(float __sfoot_fs) {
		this.__sfoot_fs = __sfoot_fs;
	}

	public StyledText getSfoot_l(ReportLaF laf) throws InvalidStylingSyntaxException  {
		if(__sfoot_l == null)
			return StyledText.EMPTY;
		else {
			return StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_SYS_FOOTNOTE), __sfoot_l, true);
		}
	}

	public void setSfoot_l(String __sfoot_l) {
		this.__sfoot_l = __sfoot_l;
	}

	public StyledText getSfoot_m(ReportLaF laf) throws InvalidStylingSyntaxException {
		if(__sfoot_m == null)
			return StyledText.EMPTY;
		else {
			return StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_SYS_FOOTNOTE), __sfoot_m, true);
		}
	}
	public StyledText getSfoot_r(ReportLaF laf) throws InvalidStylingSyntaxException{
		if(__sfoot_r == null)
			return StyledText.EMPTY;
		else {
			return StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_SYS_FOOTNOTE), __sfoot_r, true);
		}
	}

	public void setSfoot_m(String __sfoot_m) {
		this.__sfoot_m = __sfoot_m;
	}

	public StyledText getShead_l(ReportLaF laf) throws InvalidStylingSyntaxException{
		if(__shead_l == null)
			return StyledText.EMPTY;
		else {
			return StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_SYS_HEADER), __shead_l, true);
		}
	}

	public void setShead_l(String __shead_l) {
		this.__shead_l = __shead_l;
	}

	public StyledText getShead_m(ReportLaF laf)  throws InvalidStylingSyntaxException{
		if(__shead_m == null)
			return StyledText.EMPTY;
		else {
			return StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_SYS_HEADER), __shead_m, true);
		}
	}

	public StyledText getShead_r(ReportLaF laf) throws InvalidStylingSyntaxException{
		if(__shead_r == null)
			return StyledText.EMPTY;
		else {
			return StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_SYS_HEADER), __shead_r, true);
		}
	}
	public void setShead_m(String __shead_m) {
		this.__shead_m = __shead_m;
	}
	
	public Properties getSystemProps(){
		return __systemProperties;
	}
	
	public void setTestOverrides(Properties p) {
		__systemProperties = p;
		__fontsize = 8;
		if(p.containsKey("__font")){
			__font = p.getProperty("__font");
		}
	}
	public String getBookmarksPdf(){
		return __bookmarks_pdf;
	}
	
	public String getBookmarksRtf(){
		return __bookmarks_rtf;
	}
}
