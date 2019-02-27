/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rrg.rcdparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.btcisp.sasshiato.InvalidStylingSyntaxException;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.sasshiato.StyledTextUtil;
import com.btcisp.utils.StringUtil;
import com.btcisp.utils.sasxml.SasXmlObservation;
import com.lowagie.text.Rectangle;

public class RcdRow implements SasXmlObservation{
	   static final String[] rinfonamesA = new String[] {"__dest", "__filename", "__pgmname",
		   "__fontsize", "__orient ", "__path", "__version", "__colwidths", "__breakokat", "__systitle",
		   "__shead_l","__shead_r", "__shead_m", "__sfoot_l", "__sfoot_m","__sfoot_r", "__sprops", "__bookmarks_rtf","__bookmarks_pdf", "__stretch",
		   "__sfoot_fs", "__font", "__nodatamsg", "__indentsize", "__margins", "__lastcheadid", "__rtype", "__gcols", "__dist2next", "__layouttype",
		   "__papersize", "__orient", "__doffsets"};
	   static final List rinfonames = Arrays.asList(rinfonamesA);
	   
	   private String __datatype, __align, __suffix, __varbylab, __tcol, __label_cont, __span_0,  __cellborders;
	   private int  __blockid, __keepn, __indentlev;
	   int __varbygrp = -1;
	   private float __rowid ;
	   private Map __col = new HashMap();
	   private Map __other = new HashMap();

	   public RcdRow cloneAsMessageBodyRow(String msg, int byGroupId){
		    RcdRow row = new RcdRow();
		    row.__datatype = "TBODY";
		    row.__align = __align;
		    row.__cellborders = __cellborders;
		    row.__suffix = "";
		    row.__varbygrp = byGroupId;
		    row.__varbylab = "";
		    row.__tcol = "";
		    row.__label_cont = "";
		    row.__blockid = -99;
		    row.__keepn = 0;
		    row.__indentlev = 0;
		    row.__rowid = __rowid;
		    row.__col.putAll(__col);
		    Iterator it = row.__col.keySet().iterator();
		    while(it.hasNext()){
		    	String key = (String) it.next();
		        if("__col_0".equals(key)){
		        	row.__col.put(key, msg);
		        } else {
		        	row.__col.put(key, "");
		        }
		    }
		    return row;
	   }
   public  void populateVariableValue(String name, String value){
	   name=name.toLowerCase();
	   //if(value!=null) value=value.trim();
	   if("__datatype".equalsIgnoreCase(name) && value!=null){
		   value=value.trim();
		   __datatype= value;
		   if(__datatype !=null) __datatype = __datatype.toUpperCase();
	   }else if("__align".equalsIgnoreCase(name) && value!=null){
		   value=value.trim();
		   __align=value;
		   if(__align !=null) __align = __align.toUpperCase();
	   }else if("__cellborders".equalsIgnoreCase(name) && value!=null){
		   value=value.trim();
		   __cellborders=value;
		   if(__cellborders !=null) __cellborders = __cellborders.toUpperCase();
	   } else if("__suffix".equalsIgnoreCase(name) && value!=null){
		   value=value.trim();
		   __suffix=value;
	   }else if("__rowid".equalsIgnoreCase(name) && !StringUtil.isEmpty(value) ){
		   value=value.trim();
		   __rowid=Float.parseFloat(value);
	   }else if("__blockid".equalsIgnoreCase(name) && value!=null ){
		   value=value.trim();
		   __blockid=Integer.parseInt(value);
	   }else if("__keepn".equalsIgnoreCase(name) && !StringUtil.isEmpty(value)){
		   value=value.trim();
		   __keepn=Integer.parseInt(value);
	   }else if("__indentlev".equalsIgnoreCase(name) && !StringUtil.isEmpty(value) ){
		   //__indentlev=Float.parseFloat(value);
		   value=value.trim();
		  __indentlev=Integer.parseInt(value);
		  if(__indentlev >=100) {
			  SasshiatoTrace.logError("Indent Level >=100 encountered (indentLev=" + __indentlev  + " for rowid=" + __rowid + " this indent will be not used (set to 0)");
			  __indentlev=0;
		  }
	   }else if("__varbylab".equalsIgnoreCase(name) && !StringUtil.isEmpty(value) ){
		   __varbylab=value;
	   }else if("__varbygrp".equalsIgnoreCase(name) && !StringUtil.isEmpty(value) ){
		   value=value.trim();
		   __varbygrp = Integer.parseInt(value);
	   }else if("__tcol".equalsIgnoreCase(name) && !StringUtil.isEmpty(value) ){
		   __tcol = value;
	   }else if("__label_cont".equalsIgnoreCase(name) && !StringUtil.isEmpty(value) ){
		   __label_cont = value.trim();
	   }else if("__span_0".equalsIgnoreCase(name) && !StringUtil.isEmpty(value) ){
		   __span_0 = value;
	   } else if(name.startsWith("__col_")){
     	   __col.put(name, value);
	   } else if((__datatype==null || "RINFO".equalsIgnoreCase(__datatype)) && 
	             value!=null && 
	             (name.startsWith("__tit") || name.startsWith("__foot") || name.startsWith("__sfoot") || name.startsWith("__shead"))  || rinfonames.contains(name)){
		   __other.put(name, value);
       } 
	
   }
   
 
   public String toString(){
	   return "__rowid=" + __rowid + " __datatype=" + __datatype + " __col=" + __col + " __varbygrp=" + __varbygrp;
   }
   public String getSpan0(){
	   return __span_0;
   }
  
   public StyledText getTCol(ReportLaF laf) throws InvalidStylingSyntaxException{
		if(__tcol == null || StringUtil.isEmpty(__tcol))
			return StyledText.EMPTY;
		else {
			return StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_BODY), __tcol, true);
		}
   }
   
   public String getLabelCont(){
	   return this.__label_cont;
   }
   public int getVarByGroup(){
	   return __varbygrp;
   }
   
   StyledText __varbylabSt;
   public StyledText getVarByLab(ReportLaF laf) throws InvalidStylingSyntaxException{
	   if(__varbylab == null){
		   return StyledText.EMPTY;
	   }
	   if(__varbylab != null && __varbylabSt == null){
		   __varbylabSt = StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(ReportLaF.STYLE_TYPE_TITLE), __varbylab, true);
	   }
	   return __varbylabSt;
   }
   
   public int getColSize(){

		return __col.size();
   }
   
	public String getDatatype() {
		return __datatype;
	}
	
	
	public String getAlign() {
		return __align;
	}

	public int getBlockid() {
		return __blockid;
	}

	public int getIndentlev() {
		return __indentlev;
	}

	public int getKeepn() {
		return __keepn;
	}

	public float getRowid() {
		return __rowid;
	}

	public String getSuffix() {
		return __suffix;
	}

	public Map getCols() {
		return __col;
	}
	
	Map<String, StyledText> __styledTextCol = new HashMap<String, StyledText>();
	public StyledText getColumAsStyledText(int cellInx, String styletype, ReportLaF laf) throws InvalidStylingSyntaxException{
		String key = "__col_" + cellInx;
		if(__styledTextCol.get(key) == null){
			String curcol = (String) __col.get("__col_" + cellInx);

			if(curcol == null)
				curcol = "";
			StyledText stext = StyledTextUtil.getInstance(laf).parseToStyledText(laf.getFontStylesForStyleType(styletype), curcol, true);
			__styledTextCol.put(key, stext);
		} 
		
		return __styledTextCol.get(key);
	}
	
	public void overrideColumAsStyledText(int i, StyledText newText) {
		String key = "__col_" + i;
		__styledTextCol.put(key, newText);
	}
	
	
	Map<String, List<StyledText>> __styledTextLinesCol = new HashMap<String, List<StyledText>>();
	public List<StyledText> getColumAsStyledTextLines(int cellInx, String styletype, ReportLaF laf) throws InvalidStylingSyntaxException{
		String key = "__col_" + cellInx;
		if(__styledTextLinesCol.get(key) == null){
			StyledText stext = getColumAsStyledText(cellInx, styletype, laf);
			List<StyledText> res = StyledTextUtil.getInstance(laf).groupIntoLines(stext);
			__styledTextLinesCol.put(key, res);
		} 
		
		return __styledTextLinesCol.get(key);
	}
	
	public Map getOther() {
		return __other;
	}  
	
	ArrayList allAligns = null;
	public String[] getAllAligns(){
		if(allAligns==null) {
			allAligns = new ArrayList();
			StringTokenizer stalign = new StringTokenizer(this.getAlign(), " ");
			while(stalign.hasMoreTokens()){
				allAligns.add(stalign.nextToken());
			}
			if(this.getColSize() > allAligns.size()){
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: Row " + this.getRowid() + " does not provide all __align values, colsize=" + this.getColSize() + ", align count=" + allAligns.size());
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Sasshiato will default missing align values to L");
				for(int i=allAligns.size(); i< this.getColSize(); i++) {
					allAligns.add("L");
				}
			}
		}
		return (String[]) allAligns.toArray(new String[0]);
	}
	
	ArrayList<Integer> allBorderInstructions = null;
	private void parseBorderInstructions() {
		allBorderInstructions = new ArrayList<Integer>();
		if(__cellborders == null)
			return;
			
		StringTokenizer st = new StringTokenizer(__cellborders, " ");
		while(st.hasMoreTokens()){
			String inst = st.nextToken();
			int parsedInst = 0;
			if(inst.indexOf('T') != -1){
				parsedInst = parsedInst | Rectangle.TOP;
			}
			if(inst.indexOf('B') != -1){
				parsedInst = parsedInst | Rectangle.BOTTOM;
			}
			if(inst.indexOf('R') != -1){
				parsedInst = parsedInst | Rectangle.RIGHT;
			}
			if(inst.indexOf('L') != -1){
				parsedInst = parsedInst | Rectangle.LEFT;
			}
			allBorderInstructions.add(parsedInst);
		}
	}
	
	//returns border instruction based on itext Rectangle 
	public int getColumnBorderInstructions(int cellIndx){
		if(allBorderInstructions == null){
			parseBorderInstructions();
		}
		
		if(cellIndx >= allBorderInstructions.size()){
			return Rectangle.NO_BORDER;
		} else {
			return allBorderInstructions.get(cellIndx);
		}
	}

}
