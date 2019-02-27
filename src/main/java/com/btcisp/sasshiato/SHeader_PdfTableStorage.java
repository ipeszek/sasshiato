/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class SHeader_PdfTableStorage {
	private static SHeader_PdfTableStorage self = new SHeader_PdfTableStorage();
	
	public static SHeader_PdfTableStorage getSelf(){
		return self;
	}
	
	PdfPTable emptyHeaderTable;
		
	private StyledText firstRun_HeaderTextPrep(StyledText txt){
		return txt.replace("_PAGE_", "Page 2000000 of 2000000");
	}
	
	public float measureSHeaderTableLength(RcdInfo info, ReportLaF laf, ReportSetup reportSetup) throws Exception{
	    Document tmp = DocumentFactory.getSelf().createITextDocument(reportSetup);
	    OutputStream os= new ByteArrayOutputStream();
	    //OutputStream os= new FileOutputStream("I:/test.pdf");
	    	//new FileOutputStream("I:/test.pdf");//new ByteArrayOutputStream();
	    PdfWriter w = PdfWriter.getInstance(tmp, os);
	    tmp.open();

		float[] percentSystemWitdhs = laf.getSystemHeaderColumnPercentWidths();
		float[] absoluteWidths = new float[percentSystemWitdhs.length];
		float runningTotal = 0;
		for (int i = 0; i < absoluteWidths.length; i++) {
			if(i<absoluteWidths.length-1){
				absoluteWidths[i] = percentSystemWitdhs[i] * reportSetup.getTableWidth();
				runningTotal = runningTotal + absoluteWidths[i];	
			} else{
				absoluteWidths[i]  = reportSetup.getTableWidth() - runningTotal;
			}
		}
		PdfPTable shtable = new PdfPTable(absoluteWidths.length);
		shtable.setTotalWidth(absoluteWidths);
		shtable.setWidthPercentage(100f);

		StyledText shead_l = info.getShead_l(laf);
		StyledText shead_m = info.getShead_m(laf);
		StyledText shead_r = info.getShead_r(laf);
		//if(shead_m==null) shead_m= "test";
		//int shtable_nocolumns = shtable.getNumberOfColumns();
		if(shead_r.isEmpty() && shead_m.isEmpty()){
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
			cell_l.setColspan(3);
			cell_l.setPaddingTop(0f);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingBottom(laf.getSystemHeaderPadding());
			cell_l.setPaddingLeft(0f);
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_l);
		}
		else if(shead_m.isEmpty()){
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
			cell_l.setColspan(2);
			cell_l.setPaddingTop(0f);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingBottom(laf.getSystemHeaderPadding());
			cell_l.setPaddingLeft(0f);
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_l);

			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_r), laf, "systemHeader", 0, Element.ALIGN_RIGHT);
			//	cell.setBorder(PdfPCell.TOP);
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			cell.setPaddingTop(0f);
			cell.setPaddingBottom(laf.getSystemHeaderPadding());
			cell.setPaddingRight(0f);
			shtable.addCell(cell);	
//		}
//		else if(StringUtil.isEmpty(shead_r)){
//			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
//			cell_l.setColspan(1);
//			cell_l.setPaddingTop(0f);
//			cell_l.setBorder(PdfPCell.NO_BORDER);
//			cell_l.setPaddingBottom(laf.getSystemHeaderPadding());
//			cell_l.setPaddingLeft(0f);
//			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
//			shtable.addCell(cell_l);
//
//			PdfPCell cell_m = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_m), laf, "systemHeader", 0, Element.ALIGN_CENTER);
//			cell_l.setColspan(2);
//			cell_m.setBorder(PdfPCell.NO_BORDER);
//			cell_m.setPaddingTop(0f);
//			cell_m.setPaddingBottom(laf.getSystemHeaderPadding());
//			cell_m.setVerticalAlignment(PdfPCell.ALIGN_TOP);
//			shtable.addCell(cell_m);
//
		} else {
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
			cell_l.setPaddingTop(0f);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingBottom(laf.getSystemHeaderPadding());
			cell_l.setPaddingLeft(0f);
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_l);

			PdfPCell cell_m = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_m), laf, "systemHeader", 0, Element.ALIGN_CENTER);
			cell_m.setBorder(PdfPCell.NO_BORDER);
			cell_m.setPaddingTop(0f);
			cell_m.setPaddingBottom(laf.getSystemHeaderPadding());
			cell_m.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_m);

			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(firstRun_HeaderTextPrep(shead_r), laf, "systemHeader", 0, Element.ALIGN_RIGHT);
			//	cell.setBorder(PdfPCell.TOP);
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			cell.setPaddingTop(0f);
			cell.setPaddingRight(0f);
			cell.setPaddingBottom(laf.getSystemHeaderPadding());
			shtable.addCell(cell);	
		}

		
		float beforeTable = w.getVerticalPosition(false);
		
		tmp.add(shtable);
		float tableBottom = w.getVerticalPosition(false);
		float result = beforeTable- tableBottom;
		tmp.close();
		w.close();
		os.close();
		return result;
	}
	
	public PdfPTable buildSTable_FirstRun(RcdInfo info, ReportLaF laf, ReportSetup reportSetup) throws Exception{
		if(emptyHeaderTable ==null) {
			float neededSpace = measureSHeaderTableLength(info, laf, reportSetup);
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: Calculated vertical space for system header is " + neededSpace);
			emptyHeaderTable = new PdfPTable(1);
			emptyHeaderTable.setTotalWidth(reportSetup.getTableWidth());
			emptyHeaderTable.setWidthPercentage(100f);
			PdfPCell cell = new PdfPCell();
			cell.setFixedHeight(neededSpace);
			cell.setBorder(PdfPCell.NO_BORDER);
			emptyHeaderTable.addCell(cell);
		}
		return emptyHeaderTable;
	}
	
	private StyledText secondRun_HeaderTextPrep(int pageNum, int pageTot, StyledText txt){
		return txt.replace("_PAGE_", "Page " + pageNum +" of " + pageTot);
	}	
	
	public PdfPTable buildSTable_Stamping(int pageNumber, int totalPages, ReportLaF laf, RcdInfo rcdInfo, ReportSetup reportSetup) throws Exception{
		
		float[] percentSystemWitdhs = laf.getSystemHeaderColumnPercentWidths();
		float[] absoluteWidths = new float[percentSystemWitdhs.length];
		float runningTotal = 0;
		for (int i = 0; i < absoluteWidths.length; i++) {
			if(i<absoluteWidths.length-1){
				absoluteWidths[i] = percentSystemWitdhs[i] * reportSetup.getTableWidth();
				runningTotal = runningTotal + absoluteWidths[i];	
			} else{
				absoluteWidths[i]  = reportSetup.getTableWidth() - runningTotal;
			}
		}
		PdfPTable shtable = new PdfPTable(absoluteWidths.length);
		shtable.setTotalWidth(absoluteWidths);
		shtable.setWidthPercentage(100f);

		StyledText shead_l = rcdInfo.getShead_l(laf);
		StyledText shead_m = rcdInfo.getShead_m(laf);
		StyledText shead_r = rcdInfo.getShead_r(laf);
		//if(shead_m==null) shead_m= "test";
		//int shtable_nocolumns = shtable.getNumberOfColumns();
		if(shead_r.isEmpty()&& shead_m.isEmpty()){
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
			cell_l.setColspan(3);
			cell_l.setPaddingTop(0f);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingBottom(laf.getSystemHeaderPadding()); //appears no need for adjustement
			cell_l.setPaddingLeft(0f);
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_l);
		}
		else if(shead_m.isEmpty()){
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
			cell_l.setColspan(2);
			cell_l.setPaddingTop(0f);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingBottom(laf.getSystemHeaderPadding()); //appears no need for adjustement
			cell_l.setPaddingLeft(0f);
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_l);

			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_r), laf, "systemHeader", 0, Element.ALIGN_RIGHT);
			//	cell.setBorder(PdfPCell.TOP);
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			cell.setPaddingTop(0f);
			cell.setPaddingBottom(laf.getSystemHeaderPadding());
			cell.setPaddingRight(0f);
			shtable.addCell(cell);	
//		}
//		else if(StringUtil.isEmpty(shead_r)){
//			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
//			cell_l.setColspan(1);
//			cell_l.setPaddingTop(0f);
//			cell_l.setBorder(PdfPCell.NO_BORDER);
//			cell_l.setPaddingBottom(laf.getSystemHeaderPadding());
//			cell_l.setPaddingLeft(0f);
//			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
//			shtable.addCell(cell_l);
//
//			PdfPCell cell_m = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_m), laf, "systemHeader", 0, Element.ALIGN_CENTER);
//			cell_l.setColspan(2);
//			cell_m.setBorder(PdfPCell.NO_BORDER);
//			cell_m.setPaddingTop(0f);
//			cell_m.setPaddingBottom(laf.getSystemHeaderPadding());
//			cell_m.setVerticalAlignment(PdfPCell.ALIGN_TOP);
//			shtable.addCell(cell_m);
//
		} else {
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_l), laf, "systemHeader", 0, Element.ALIGN_LEFT);
			cell_l.setPaddingTop(0f);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingBottom(laf.getSystemHeaderPadding());
			cell_l.setPaddingLeft(0f);
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_l);

			PdfPCell cell_m = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_m), laf, "systemHeader", 0, Element.ALIGN_CENTER);
			cell_m.setBorder(PdfPCell.NO_BORDER);
			cell_m.setPaddingTop(0f);
			cell_m.setPaddingBottom(laf.getSystemHeaderPadding());
			cell_m.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			shtable.addCell(cell_m);

			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(secondRun_HeaderTextPrep(pageNumber, totalPages, shead_r), laf, "systemHeader", 0, Element.ALIGN_RIGHT);
			//	cell.setBorder(PdfPCell.TOP);
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			cell.setPaddingTop(0f);
			cell.setPaddingRight(0f);
			cell.setPaddingBottom(laf.getSystemHeaderPadding());
			shtable.addCell(cell);	
		}
		return shtable;
	}
}
