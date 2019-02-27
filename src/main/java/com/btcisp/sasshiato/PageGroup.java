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

public class PageGroup {
private ArrayList  allColumns;
private int pageNumber;
private float[] columnWidths; //same as allColumns.get(i).getWidth()
private Map columnId2IndexMap = new HashMap();

public int[] getColumnIDs() {
	PageColumn[] result= (PageColumn[]) allColumns.toArray(new PageColumn[0]);
	int[] res = new int[result.length];
	for (int i = 0; i < result.length; i++) {
		res[i]=result[i].getColumnID();
	}
	return res;
}

public boolean isStretchRequired(){
	boolean allowed = false;
	for (int i = 0; i < allColumns.size(); i++) {
		PageColumn sc = (PageColumn) allColumns.get(i);
		if(sc.isStretchAllowed()) allowed = true;
	}
	return allowed;
}

public int calculateColumnSpan(int startColId, int endColId) {
	Integer indxStart = (Integer) columnId2IndexMap.get(new Integer(startColId));
	if(indxStart==null) {
		SasshiatoTrace.logError("Attempt to calculate column span starting with non-existing column " + startColId + " on " + this);
		return -1;
	}
	Integer indxEnd = (Integer) columnId2IndexMap.get(new Integer(endColId));
	if(indxEnd==null) {
		SasshiatoTrace.logError("Attempt to calculate column span ending with non-existing column " + endColId + " on " + this);
		return -1;
	}
	int span = indxEnd.intValue()  - indxStart.intValue() + 1;
	return span;
}

public float calculateTotalWidth(int startColId, int endColId){
	Integer indxStart = (Integer) columnId2IndexMap.get(new Integer(startColId));
	if(indxStart==null) {
		SasshiatoTrace.logError("Attempt to calculate column span starting with non-existing column " + startColId + " on " + this);
		return -1;
	}
	Integer indxEnd = (Integer) columnId2IndexMap.get(new Integer(endColId));
	if(indxEnd==null) {
		SasshiatoTrace.logError("Attempt to calculate column span ending with non-existing column " + endColId + " on " + this);
		return -1;
	}
	float width = 0;
	for(int i=indxStart.intValue(); i<=indxEnd.intValue(); i++){
		width = width + this.columnWidths[i];
	}
	return width;
}
public float calculateTotalWidth(){
	float w = 0f;
	for (int i = 0; i < columnWidths.length; i++) {
		w = w+columnWidths[i];
	}
	return w;
}
public float calculateTotalWidth(float forcedWidth){
	if(forcedWidth>0) return forcedWidth;
	else return calculateTotalWidth();
}
public PageColumn[] getAllColumns() {
	PageColumn[] result= (PageColumn[]) allColumns.toArray(new PageColumn[0]);
	return result;
}


public int getPageNumber() {
	return pageNumber;
}

public float[] getColumnWidths() {
	return columnWidths;
}

public PageGroup(ArrayList allColumns, int pageNumber) {
	super();
	this.allColumns = allColumns;
	this.pageNumber = pageNumber;
	columnWidths= new float[allColumns.size()];
	for (int i = 0; i < allColumns.size(); i++) {
		PageColumn sc =(PageColumn)allColumns.get(i); 
		columnWidths[i]=sc.getColumnWidth();
		columnId2IndexMap.put(new Integer(sc.getColumnID()), new Integer(i));
	}
}
float forcedTotWidth = 0f;
public void setForcedTotalWidth(float width){
	this.forcedTotWidth =width;
}
public float getForcedTotWidth(){
	return forcedTotWidth;
}
public boolean isLastColumnOnPage(int onPageColId){
	return allColumns.size() -1==onPageColId;
}
public int map2OnPageColumnNumber(int columnId){
	Integer indx = (Integer) columnId2IndexMap.get(new Integer(columnId));
	if(indx==null) {
		SasshiatoTrace.logError("Attempt to map " + columnId + " which is not on " + this);
		return -1;
	}
	return indx.intValue();

}

public PageColumn getPageColumnWithAbsColumnId(int columnId) {
	int inx = map2OnPageColumnNumber(columnId);
	if(inx!=-1) {
		return (PageColumn) allColumns.get(inx);
	} else{
		return null;
	}
}
public String toString(){
	String s = "";
	s = "PageGroup[pn="+pageNumber +"{\n";
	float w = 0f;
	for (int i = 0; i < allColumns.size(); i++) {
		PageColumn sc = (PageColumn) allColumns.get(i);
		s = s + " C_"+sc.getColumnID()+"(w="+sc.getColumnWidth() + "),";
		w = w+sc.getColumnWidth();
	}
	s = s + "Tot width="+w+ "}";
	return s;
}

public void adjustToStetch(ReportSetup reportSetup) {
    float pageGroupWidth= calculateTotalWidth();
    float ratio = reportSetup.getTableWidth() / pageGroupWidth;
    if(ratio < 1)
    	ratio = 1f; //only scale up

	if(!isStretchRequired()){
		if(pageGroupWidth < reportSetup.getTableWidth()){
  	    	 //DO NOT SCALE UP EVEN IF IT IS POSSIBLE, mimics PDF code
  	    	 ratio = 1f;
  		}
	}
	 float totalWidth = 0;
	 for (int j = 0; j < columnWidths.length; j++) {
		 columnWidths[j] = ratio * columnWidths[j];
		 totalWidth += columnWidths[j];
	 }

	 if(totalWidth> 0 && totalWidth > reportSetup.getTableWidth()){
		 columnWidths[0] += reportSetup.getTableWidth() - totalWidth; 
	 }
}
}
