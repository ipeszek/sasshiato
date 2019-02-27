/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato.simple_info;

import java.util.Map;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.sasshiato.HeaderTablet;
import com.btcisp.sasshiato.PageGroup;
import com.btcisp.sasshiato.ReportPadListener;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.StyledText;

public class ReportPadListener_SimpleInfoGenerator implements ReportPadListener {

	InfoGenerator_Simple generator;
	public ReportPadListener_SimpleInfoGenerator(InfoGenerator_Simple generator){
		this.generator = generator;
	}
	public void onFinalizePage(boolean isDocumentClosing, Map props)
			throws Exception {

	}

	public void onFootnotesPrep(StyledText[] footnotes, Map props) throws Exception {

	}

	public void onFootnotesWrite(Map props) throws Exception {

	}

	public void onHeaderWrite(RcdInfo info, Map props) throws Exception {
		PageGroup pageGroup = generator.getPageGroup();
		if(pageGroup==null) {
			SasshiatoTrace.logError("Invalid use of ReportPad Listener, no pageGroups defined");
			return;
		}
		HeaderTablet tablet = (HeaderTablet) props.get(pageGroup);
		try{
		generator.generateHeaderInfo(tablet);
		}catch(Exception e){
			SasshiatoTrace.logError("Simple Info Generation Error", e);
		}

		//generator.generateHeaderSplitPointsInfo(tablet);
	}

	public void onLineWrite(StyledText txt, int indent, boolean spanWholeTable,
			Map props) throws Exception {

	}

	public void onRowWrite(RcdRow row, int hint, Map props) throws Exception {

	}

	public void onTitlesPrep(StyledText[] titles, Map oldTitleIndexes, Map props) throws Exception {

	}

	public void onTitlesWrite(boolean fistTime, boolean firstPage, Map props) throws Exception {
		// TODO Auto-generated method stub

	}

}
