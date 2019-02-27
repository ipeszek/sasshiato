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

public interface ReportPadListener {
	  
		public abstract void onHeaderWrite(RcdInfo info, Map props)throws Exception;

		public abstract void onTitlesPrep(StyledText[] titles, Map oldTitleIndexes,  Map props) throws Exception;

		public abstract void onTitlesWrite(boolean fistTime, boolean firstPage, Map props)throws Exception;


		public abstract void onFootnotesPrep(StyledText[] footnotes,  Map props) throws Exception;
		public abstract void onFootnotesWrite(Map props) throws Exception;
		
		public abstract void onLineWrite(StyledText txt, int indent, boolean spanWholeTable, Map props) throws Exception;
		public abstract void onRowWrite(RcdRow row, int hint, Map props)throws Exception;
		
		public abstract void onFinalizePage(boolean isDocumentClosing, Map props)
				throws Exception;
 
}
