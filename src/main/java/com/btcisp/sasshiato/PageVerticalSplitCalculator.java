/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;

public class PageVerticalSplitCalculator {
	class FloatHolder{
		FloatHolder(float el){
			element = el;
		}
		float element;
	}
	private float[] actualWidths;
	private float[] preferredWidths;
	private int[] breaksAlowedAt;
	//private boolean[] isBasedOnMinimumWidth;
	private float tableWidth;
	private int numberOfColumns;
	private int numberOfHeaderColumns;
	private TableWidths tableWidths;
	
	private ArrayList initialPageGroups = new ArrayList();
	//private ArrayList finalPageGroups= new ArrayList();
	private ArrayList allColumns = new ArrayList();
	private ColumnGroup headerGroup = new ColumnGroup();
	private ReportSetup reportSetup ;
	boolean[] stretch_flags;

	
	private void initializeColumns(){
		//creates ArrayList allColumns  consisting of all columns;
		for (int i = 0; i < numberOfColumns; i++) {
			Column col = new Column(i,  actualWidths[i]);
			allColumns.add(col);
		}
		
	}

	private void calcHeaderColumns(){
		// sets properties of headerGroup ;
		
		headerGroup.setStartingColumn(0);
		headerGroup.setEndingColumn(numberOfHeaderColumns- 1);
		float width = 0;
		for (int j = 0; j < numberOfHeaderColumns; j++) {
			Column c = (Column) allColumns.get(j);

			width = width + c.getActualWidth();
		}
		headerGroup.setActualWidth(width);
		
	}

	private void calcInitialPages() throws Exception{
		float max_group_size = tableWidth-headerGroup.getActualWidth();
        if(max_group_size<0) {
        	throw new Exception("Combined width of header columns " + headerGroup.getActualWidth() + " exceeds table width " + tableWidth);
        }
		for (int i = numberOfHeaderColumns; i < numberOfColumns; i++) {
			Column c = (Column) allColumns.get(i);
				if(c.getActualWidth() > max_group_size) {
		        	throw new Exception("Combined width of header columns " + headerGroup.getActualWidth() +" + width of column " + i + "(" +c.getActualWidth() + ") exceeds table width " + tableWidth);					
				}
		}
		// populates initialPageGroups ArrayList with initial "page groups" assignments;
		// one per each element in breaksAllowedAt;
		// if breaksAllowedAt is null then each non-header column represents initial "page group";
		// later, some of initial page groups may be placed together on a page;
		if (breaksAlowedAt == null || breaksAlowedAt.length==0) {
			
			for (int i = numberOfHeaderColumns; i <= numberOfColumns-1; i++) {
				ColumnGroup cg = new ColumnGroup();
				cg.setStartingColumn(i);
				cg.setEndingColumn(i);
				float width = 0;
				Column c = (Column) allColumns.get(i);
					width = width + c.getActualWidth();
				cg.setActualWidth(width);
				initialPageGroups.add(cg);
			}

		} else {
			int startingC = numberOfHeaderColumns; //initialize to firt column after header column
			for (int i = 0; i <=  breaksAlowedAt.length; i++) {
				int nextAllowedBreak = -1;
				if(i==breaksAlowedAt.length)
					nextAllowedBreak = this.numberOfColumns; //set it to last column index +1 to mimic next break logic for last column group
				else 
					nextAllowedBreak =breaksAlowedAt[i];
				if(startingC > nextAllowedBreak - 1) continue; //skip records pertaining to header or duplicate records
				ColumnGroup cg = new ColumnGroup();
				cg.setStartingColumn(startingC);
				cg.setEndingColumn(nextAllowedBreak - 1);
				float width = 0;
				for (int j = startingC; j < nextAllowedBreak; j++) {
					Column c = (Column) allColumns.get(j);
						width = width + c.getActualWidth();
				}
				cg.setActualWidth(width); //actual width is reliable for calculation
				if(width > max_group_size) {
					//spit just created group into single colum groups
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: requested breakatok columns " + cg.getStartingColumn() + " and " + (cg.getEndingColumn() +1) + " cannot be met (resulting width would be " + cg.getActualWidth() + "), the column group is being split");
					for (int k = cg.getStartingColumn(); k <= cg.getEndingColumn(); k++) {
						ColumnGroup ng = new ColumnGroup();
						ng.setStartingColumn(k);
						ng.setEndingColumn(k);
						float nwidth = 0;
						Column c = (Column) allColumns.get(k);
							nwidth = nwidth + c.getActualWidth();
						ng.setActualWidth(nwidth);
						initialPageGroups.add(ng);
					}				
				} else {
					initialPageGroups.add(cg);
				}
				//startingC=i;
				startingC = nextAllowedBreak;

			}
			
		}
	}
	

	public PageVerticalSplitCalculator(ReportSetup reportSetup, TableWidths widths) {
		super();
		this.actualWidths = widths.getActualWidths();
		this.preferredWidths = widths.getPreferredWidths();
		this.breaksAlowedAt = reportSetup.getBreaksAlowedAt();
		this.tableWidth = reportSetup.getTableWidth();
		this.numberOfColumns = reportSetup.getNumberOfColumnsI();
		this.numberOfHeaderColumns = reportSetup.getNumberOfHeaderColumnsI();
		this.tableWidths = widths;
		this.reportSetup = reportSetup;
		this.stretch_flags =reportSetup.getStretchFlags();
	}	
	

	//throws exception if initial column groups are too big fit on a page;
	private void checkIfFits()throws Exception{
		// returns false if initial column groups are too big fit on a page;
		for (int i = 0; i < initialPageGroups.size(); i++) {
			ColumnGroup cg = (ColumnGroup) initialPageGroups.get(i);
			float cw = cg.getActualWidth();
			float cw0 = headerGroup.getActualWidth();
			if (cw+cw0>tableWidth){
				throw new Exception("Initial columns and headers do not fit on the page, required width " + cw + "+" + cw0 + " exceeds page width of " +tableWidth );
			}
		}
	}
	
	private final static boolean AFTER_TEXT_WEIGHTED_STRETCH_APPLY_PROPORTIONAL=false; //not configurable at this moment, if true proportional post stretch is applied, otherwise even amount is applied.
	private PageGroup[] recalculatePages(){
		// creates final page groupings;
		
		//int newstartcol=numberOfHeaderColumns;
		//int newendcol=numberOfColumns;
		float spaceleft = tableWidth-headerGroup.getActualWidth();
		
		ColumnGroup workingPage = new ColumnGroup();
		workingPage.setStartingColumn(numberOfHeaderColumns);
		workingPage.setActualWidth(0);

		int groupid=0;
		
		ArrayList finalPageGroups= new ArrayList();
		while(groupid<initialPageGroups.size()){
			ColumnGroup c = (ColumnGroup) initialPageGroups.get(groupid);
			float width = c.getActualWidth();
			
			if (width<spaceleft){
				workingPage.setEndingColumn(c.getEndingColumn());
				float oldWidth = workingPage.getActualWidth();
				workingPage.setActualWidth(oldWidth + width); //only minimum width is reliable for calculation (set correctly in calcInitial)
				spaceleft = spaceleft-width;
				groupid++;
				continue;
			}
			else {	
				if(workingPage.getEndingColumn() != ColumnGroup.UNDEFINED_COLUMN){
					//previous group has fitted on the page
					//store previous group and reset from the begining
					finalPageGroups.add(workingPage);
					workingPage = new ColumnGroup();
					workingPage.setStartingColumn(c.getStartingColumn()); 
					workingPage.setActualWidth(0);					
					spaceleft = tableWidth-headerGroup.getActualWidth();
					continue;
				} else {
					//single group does not want to fit on one page, THIS SHOULD NEVER HAPPEN
					if(workingPage.getStartingColumn() != c.getStartingColumn()){
						SasshiatoTrace.logError("Unexpected behavior in page splitting logic: column group starting column=" + c.getStartingColumn() + ", ending column=" + c.getStartingColumn() + " NOT FITTING ON ONE PAGE");
					}
					
					//check another assertion
					if(workingPage.getStartingColumn() != c.getStartingColumn()){
						SasshiatoTrace.logError("Unexpected behavior in page splitting logic: working column group starting column=" + workingPage.getStartingColumn() + "!= not fitting column group page starting column=" + c.getStartingColumn());
					}
				}
			}
			
		}
		if(!finalPageGroups.contains(workingPage) && workingPage.getEndingColumn() != ColumnGroup.UNDEFINED_COLUMN) 
			finalPageGroups.add(workingPage);
		
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: Total table width is " + tableWidth);
		
		PageGroup[] pageGroup = new PageGroup[finalPageGroups.size()];
		// adjust widths inside each page group;
		float fixedHWidths = 0f;
		float scalableHWidths=0f;
		int scalableColumnCount = 0;
		for (int i = 0; i < numberOfHeaderColumns; i++) {
			if(!stretch_flags[i]){
				fixedHWidths = fixedHWidths+actualWidths[i];
			} 
			else {
				scalableHWidths = scalableHWidths+actualWidths[i];
				scalableColumnCount ++;
			}
		}
		for (int i = 0; i < finalPageGroups.size(); i++) {
			ColumnGroup cg = (ColumnGroup) finalPageGroups.get(i);
			ArrayList pageColumns = new ArrayList();

			float fixedWidths = fixedHWidths;
			float scalableWidths = scalableHWidths;
			
			for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
				if(!stretch_flags[j]){
					fixedWidths = fixedWidths+actualWidths[j];
				} else {
					scalableWidths = scalableWidths+actualWidths[j];
					scalableColumnCount ++;
				}
			}
			
			boolean add_with_current_sizes = false;
			if(tableWidth -1 > fixedWidths + scalableWidths && scalableWidths>0) {
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: fixedWidths= " + fixedWidths + ", scalableWidths=" +scalableWidths);
				float diff = tableWidth - 1 - fixedWidths - scalableWidths; //subtracting 1 prevents floating point errors with new table size over what allowed
				
				FloatHolder swh = new FloatHolder(scalableWidths);
				FloatHolder ash = new FloatHolder(diff);
				float[] modifiedWidths = applyTextWeightedStretching(swh, ash);
				scalableWidths = swh.element;
			    diff = ash.element;
			    
				float factor = (scalableWidths + diff)/(scalableWidths);
				float evenStretch = diff/scalableColumnCount;
				for (int j = 0; j < numberOfHeaderColumns; j++) {
					float finalWidth=0f;
					if(stretch_flags[j]) {
						if(AFTER_TEXT_WEIGHTED_STRETCH_APPLY_PROPORTIONAL)
							finalWidth=modifiedWidths[j]*factor;
						else 
							finalWidth=modifiedWidths[j] + evenStretch;
						SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: header col " + j + " stretched from " + actualWidths[j]+ " to " + finalWidth);
					} else {
						finalWidth=modifiedWidths[j];						
					}
					PageColumn sc = new PageColumn(j, finalWidth, actualWidths[j], stretch_flags[j]);
					pageColumns.add(sc);					
				}
					
				for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
					float finalWidth=0f;
					if(stretch_flags[j]) {
						if(AFTER_TEXT_WEIGHTED_STRETCH_APPLY_PROPORTIONAL)
							finalWidth=modifiedWidths[j]*factor;
						else 
							finalWidth=modifiedWidths[j] + evenStretch;
						SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: col " + j + " stretched from " + actualWidths[j]+ " to " + finalWidth);
					} else {
						finalWidth=modifiedWidths[j];						
					}
					PageColumn sc = new PageColumn(j, finalWidth, actualWidths[j], stretch_flags[j]);
					pageColumns.add(sc);					
				}
				

			} else if (tableWidth < fixedWidths + scalableWidths) {
				//shrinking
				SasshiatoTrace.logError("Unexpected width condition tableWidth( "+ tableWidth +") <fixedWidths(" +fixedWidths + ") + scalableWidths (" + scalableWidths + "), table formatting may be incorrect");
				add_with_current_sizes = true;
				//keep current sizes
				/*
				if(scalableWidths >0) {
				float factor = (tableWidth-fixedWidths)/scalableWidths;
				for (int j = 0; j < numberOfHeaderColumns; j++) {
					float finalWidth=0f;
					if(isBasedOnMinimumWidth[j]){
						finalWidth=actualWidths[j]*factor;
					} else finalWidth=actualWidths[j];
					SingleColumn sc = new SingleColumn(j, finalWidth);
					pageColumns.add(sc);
				}
				for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
					float finalWidth=0f;
					if(isBasedOnMinimumWidth[j]){
						finalWidth=actualWidths[j]*factor;
					} else finalWidth=actualWidths[j];
					SingleColumn sc = new SingleColumn(j, finalWidth);
					pageColumns.add(sc);
				}
				}
				*/
			} else {
				add_with_current_sizes = true;
			}
			if(add_with_current_sizes){
				//keep current sizes
				for (int j = 0; j < numberOfHeaderColumns; j++) {
					float finalWidth=actualWidths[j];
					PageColumn sc = new PageColumn(j, finalWidth, false);
					pageColumns.add(sc);
				}
				for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
					float finalWidth=actualWidths[j];
					PageColumn sc = new PageColumn(j, finalWidth, false);
					pageColumns.add(sc);
				}
			}
			/*
			float factor = (tableWidth-fixedWidths)/scalableWidths;
			
			if (factor<1){
				for (int j = 0; j < numberOfHeaderColumns; j++) {
					float finalWidth=0f;
					if(wrapAllowed[j]==true){
						finalWidth=actualWidths[j]*factor;
					} else finalWidth=actualWidths[j];
					SingleColumn sc = new SingleColumn(j, finalWidth);
					pageColumns.add(sc);
				}
				for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
					float finalWidth=0f;
					if(wrapAllowed[j]==true){
						finalWidth=actualWidths[j]*factor;
					} else finalWidth=actualWidths[j];
					SingleColumn sc = new SingleColumn(j, finalWidth);
					pageColumns.add(sc);
				}
				
				
			} else{
				factor = tableWidth/(fixedWidths+scalableWidths);
				for (int j = 0; j < numberOfHeaderColumns; j++) {
					float finalWidth=0f;
					finalWidth=actualWidths[j]*factor;
					SingleColumn sc = new SingleColumn(j, finalWidth);
					pageColumns.add(sc);					
				}
				
				for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
					float finalWidth=0f;
					finalWidth=actualWidths[j]*factor;
					SingleColumn sc = new SingleColumn(j, finalWidth);
					pageColumns.add(sc);
				}
				
			
			}
			*/
			PageGroup pg = new PageGroup(pageColumns, i);
			if(scalableWidths >0) pg.setForcedTotalWidth(tableWidth); 
			pageGroup[i]=pg;
			float totWidth=pg.calculateTotalWidth();
		    if(tableWidth < totWidth) {
		    	SasshiatoTrace.logError("Unexpected error, calculated page group width exceeds table width of " + tableWidth + " page group:" + pg);
		    }
		}
		
		if(pageGroup.length==1){
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: No vertical page split is required, following page group will be used:" +
			"\n"+pageGroup[0]);
		} else if(pageGroup.length>1){
			StringBuffer msg= new StringBuffer();
			msg.append("INFO: Vertical page split into "+ pageGroup.length + " pages will be used:");
			for (int i = 0; i < pageGroup.length; i++) {
				PageGroup group = pageGroup[i];
				msg.append("\nGROUP[" + i +"]" + group);
			}
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, msg.toString());			
		} else {
			SasshiatoTrace.logError("Unexpected no page groups have been calculated");
		}
		return pageGroup;
	}
	
	private float[] applyTextWeightedStretching(FloatHolder scalableWidthH, FloatHolder availableSpaceH)  {
		if(!reportSetup.applyTextWeightedStretching()) 
			return actualWidths;
		else {
			float availableSpace = availableSpaceH.element;
			float[] sfactor = new float[actualWidths.length];
			float sumx = 0;
			for (int i = 0; i < actualWidths.length; i++) {
				if(stretch_flags[i] && preferredWidths[i] > actualWidths[i] & actualWidths[i]>0){
					sfactor[i] = preferredWidths[i] - actualWidths[i];
					sumx = sumx + sfactor[i];
				} else {
					sfactor[i] = 0f;
				}
			}
			float[] newWidths = new float[actualWidths.length];
			for (int i = 0; i < newWidths.length; i++) {
				if(sfactor[i]==0f) newWidths[i]= actualWidths[i];
				else {
					newWidths[i]= Math.min(preferredWidths[i], actualWidths[i] + availableSpace * (sfactor[i] / sumx));
					scalableWidthH.element = scalableWidthH.element + newWidths[i] - actualWidths[i];
					availableSpaceH.element = availableSpaceH.element - newWidths[i] + actualWidths[i];
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Column " + i + " text weighted pre-stretching from " + actualWidths[i] + " to " + newWidths[i] +", preferred=" + preferredWidths[i]);
				}
			}
			return newWidths;
		}
	}
	/*
	private PageGroup[] recalculatePages_OLD(){
		// creates final page groupings;
		
		int newstartcol=numberOfHeaderColumns;
		int newendcol=numberOfColumns;
		float spaceleft = tableWidth-headerGroup.getActualWidth();
		
		
		for (int i = 0; i < initialPageGroups.size(); i++) {
			ColumnGroup c = (ColumnGroup) initialPageGroups.get(i);
			float width = c.getActualWidth();
			int startcol = c.getStartingColumn();
			int endcol = c.getEndingColumn();
			if (width<spaceleft){			
				newendcol = endcol;
				spaceleft = spaceleft-width;
			}
			else {
				
				ColumnGroup cg = new ColumnGroup();
				cg.setStartingColumn(newstartcol);
				cg.setEndingColumn(newendcol);
				finalPageGroups.add(cg);
				
				newstartcol=startcol;
				newendcol=endcol;
				spaceleft = tableWidth-headerGroup.getActualWidth()-width;
				
			}
			if (i==initialPageGroups.size()-1){
				ColumnGroup cg = new ColumnGroup();
				cg.setStartingColumn(newstartcol);
				cg.setEndingColumn(newendcol);
				finalPageGroups.add(cg);
				
			}
		}
		PageGroup[] pageGroup = new PageGroup[finalPageGroups.size()];
		// adjust widths inside each page group;
		float fixedHWidths = 0f;
		float scalableHWidths=0f;
		for (int i = 0; i < numberOfHeaderColumns; i++) {
			if(stretch_flags[i]==false){
				fixedHWidths = fixedHWidths+actualWidths[i];
			} else scalableHWidths = scalableHWidths+actualWidths[i];
		}
		for (int i = 0; i < finalPageGroups.size(); i++) {
			ColumnGroup cg = (ColumnGroup) finalPageGroups.get(i);
			ArrayList pageColumns = new ArrayList();

			float fixedWidths = fixedHWidths;
			float scalableWidths = scalableHWidths;
			
			for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
				if(stretch_flags[j]==false){
					fixedWidths = fixedWidths+actualWidths[j];
				} else scalableWidths = scalableWidths+actualWidths[j];
			}
			float factor = (tableWidth-fixedWidths)/scalableWidths;
			
			if (factor<1){
				for (int j = 0; j < numberOfHeaderColumns; j++) {
					float finalWidth=0f;
					if(stretch_flags[j]==true){
						finalWidth=actualWidths[j]*factor;
					} else finalWidth=actualWidths[j];
					PageColumn sc = new PageColumn(j, finalWidth, false);
					pageColumns.add(sc);
				}
				for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
					float finalWidth=0f;
					if(stretch_flags[j]==true){
						finalWidth=actualWidths[j]*factor;
					} else finalWidth=actualWidths[j];
					PageColumn sc = new PageColumn(j, finalWidth, false);
					pageColumns.add(sc);
				}
				
				
			} else{
				factor = tableWidth/(fixedWidths+scalableWidths);
				for (int j = 0; j < numberOfHeaderColumns; j++) {
					float finalWidth=0f;
					finalWidth=actualWidths[j]*factor;
					PageColumn sc = new PageColumn(j, finalWidth, false);
					pageColumns.add(sc);					
				}
				
				for (int j = cg.getStartingColumn(); j <= cg.getEndingColumn(); j++) {
					float finalWidth=0f;
					finalWidth=actualWidths[j]*factor;
					PageColumn sc = new PageColumn(j, finalWidth, false);
					pageColumns.add(sc);
				}
				
			
			}
			PageGroup pg = new PageGroup(pageColumns, i);
			pageGroup[i]=pg;

		}
		return pageGroup;
	}
	*/
		

	
	public PageGroup[] calculatePageAssignment() throws Exception{
		initializeColumns();
		calcHeaderColumns();
		calcInitialPages();
		checkIfFits();
		PageGroup[] pageGroup = recalculatePages();
		return pageGroup;
	}
	
}
