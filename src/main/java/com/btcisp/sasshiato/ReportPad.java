/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.Map;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.lowagie.text.Document;

public interface ReportPad {
	  public static final boolean MODE_WRITE_TO_TEST=false;
	  public static final boolean MODE_WRITE_TO_REAL=true;
	  
	  public static final int HINT_FIRST_ROW_ON_PAGE =1;
	  public static final int HINT_LAST_ROW_ON_PAGE =99;
	  public static final int HINT_SINGLE_ROW_ON_PAGE =100;
	  public static final int HINT_UNKNOWN =0;
	  
	  public static final String FINALIZE_PAGE_REASON_NEW_HEADER="newheader";
	  public static final String FINALIZE_PAGE_REASON_NEXT_PAGEGROUP="nexpagegroup";
	  public static final String FINALIZE_PAGE_REASON_NORMAL="normal";
	  public static final String FINALIZE_PAGE_REASON_END="end";
	  
		public abstract void headerStorageClear();

		public abstract void headerStorageAdd(RcdRow line) throws Exception;

		public abstract void headerPrep(RcdInfo info) throws Exception;

		public abstract void headerWrite(boolean mode, RcdInfo info) throws Exception;

		public abstract void headerWriteB(RcdInfo info) throws Exception;

		public abstract void tiltesPrep(StyledText[] titles, Map oldTitleIndexes, RcdInfo info, ReportLaF laf, Map context) throws Exception;

		public abstract void titlesWrite(boolean mode, boolean firstPage) throws Exception;

		public abstract void titlesWriteB(boolean firstPage) throws Exception;

		public abstract void footnotesPrep(StyledText[] footnotes, ReportLaF laf, RcdInfo info, Map context) throws Exception;

		public abstract void footnotesWrite(boolean mode) throws Exception;
		public abstract void lineWriteB(StyledText txt, int indent, boolean spanWholeTable, Map props) throws Exception;
		public abstract void lineWrite(boolean mode, StyledText txt, int indent, boolean spanWholeTable, Map props) throws Exception;
		public abstract void rowPrep(RcdRow row) throws Exception;
		public abstract void rowWrite(boolean mode, RcdRow row, int hint) throws Exception;
		public abstract boolean canFit() throws Exception;
		public abstract void finalizePage(Document document, boolean startNewPage, Map props)
				throws Exception;
		public abstract void initWriteSpace();
		
		public abstract void calculateBottomEmptySpace() ;
}
