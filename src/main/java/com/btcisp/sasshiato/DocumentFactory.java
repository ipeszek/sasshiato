/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.sasshiato.rtf.ReportPadListener_RtfSinglePage;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

public class DocumentFactory {
	static DocumentFactory self = new DocumentFactory();
	public static DocumentFactory getSelf(){
		return self;
	}
	public DocumentHolder createDocument(ReportSetup reportSetup, RcdInfo info, ReportLaF laf, PageGroup[] groups, TableWidths tableWidths, String debugInfo, String pdfPath, String basePath, boolean isEmptyTable, File watermark, ReportPadListener[] specialListeners) throws Exception{
		DocumentHolder holder = new DocumentHolder();
		holder.setDebugInfo(debugInfo);
		holder.setDocument(createITextDocument(reportSetup));
		if(reportSetup.isPdfOut()) holder.setPdfOutStream( new FileOutputStream(pdfPath), false);
		else holder.setPdfOutStream( new ByteArrayOutputStream(), true);
		holder.setPdfWriter(PdfWriter.getInstance(holder.getDocument(),
				holder.getPdfOutStream()));
		String documentsInfo = "";
		if(reportSetup.isPdfOut())
			documentsInfo = documentsInfo + "," + basePath + ".pdf";
		if(reportSetup.isRtfOut())
			documentsInfo = documentsInfo + "," + basePath + ".rtf";
		if(documentsInfo.startsWith(",")) documentsInfo = documentsInfo.substring(1);
		holder.setDocumentsInfo(documentsInfo);
		ArrayList listenerA = new ArrayList();
		if(reportSetup.isRtfOut()){
			File fosF = new File(basePath + ".rtf");
			if(fosF.exists())
			{
				if(reportSetup.isAppend()){
					File fx = new File(basePath + ".rtftmp");
					if(fx.exists()) {
						SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + basePath + ".rtftmp " + "deleted as preparation step");
						if(!fx.delete()) {
							throw new Exception("Cannot delete file " +  fx.getAbsolutePath());
						}
					}
					if(!fosF.renameTo(new File(basePath + ".rtftmp"))){
						throw new Exception("Cannot rename file " +  basePath + ".rtf" + " to " + basePath + ".rtftmp");
					}
					SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + basePath + ".rtf" + " renamed to " + basePath + ".rtftmp");
				} else {
					fosF.delete();
				}
			}
			FileOutputStream fos = new FileOutputStream(basePath + ".rtf");
			PrintStream rtfout  = new PrintStream(fos);
			holder.setOtherOutputStreams(new OutputStream[]{fos});
			holder.setOtherPrintStreams(new PrintStream[] {rtfout});
			holder.setOtherStreamTypes(new String[]{"rtf"});
			if(!isEmptyTable) {
				//ReportPadListener_RtfSinglePage[] listeners = new ReportPadListener_RtfSinglePage[groups.length];
				for (int i = 0; i < groups.length; i++) {
					listenerA.add( new ReportPadListener_RtfSinglePage(basePath, rtfout, reportSetup, info, laf, groups[i], tableWidths, i==0, i==groups.length-1, holder, watermark));			
				}
				//holder.setReportPadListeners(listeners);
			} else {
				//holder.setReportPadListeners(new ReportPadListener[0]);				
			}
		} else {
			//holder.setReportPadListeners(new ReportPadListener[0]);
		}
		if(specialListeners !=null){
			for (int i = 0; i < specialListeners.length; i++) {
				listenerA.add(specialListeners[i]);
			}
		}
		holder.setReportPadListeners((ReportPadListener[]) listenerA.toArray(new ReportPadListener[0]));
		return holder;
	}
	public Document createITextDocument(ReportSetup reportSetup){
		Document document = null;
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO,"INFO: requested paper size is " + reportSetup.getPapersizeI() + ", orientation=" +reportSetup.getOrientationI());
		if ("LETTER".equalsIgnoreCase(reportSetup.getPapersizeI())) {
			if ("P".equalsIgnoreCase(reportSetup.getOrientationI())) {
				document = new Document(PageSize.LETTER, reportSetup
						.getLeftMargin(), reportSetup.getRightMargin(),
						reportSetup.getTopMargin(), reportSetup
								.getBottomMargin());
			} else {
				document = new Document(new Rectangle(PageSize.LETTER
						.rotate().getWidth(), PageSize.LETTER.rotate()
						.getHeight()), reportSetup.getLeftMargin(),
						reportSetup.getRightMargin(), reportSetup
								.getTopMargin(), reportSetup
								.getBottomMargin());

			}
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: Creating Document with LETTER format size=" + document.getPageSize());

		} else {
			if ("P".equalsIgnoreCase(reportSetup.getOrientationI())) {
				document = new Document(PageSize.A4, reportSetup
						.getLeftMargin(), reportSetup.getRightMargin(),
						reportSetup.getTopMargin(), reportSetup
								.getBottomMargin());
			} else {
				document = new Document(new Rectangle(PageSize.A4.rotate()
						.getWidth(), PageSize.A4.rotate().getHeight()),
						reportSetup.getLeftMargin(), reportSetup
								.getRightMargin(), reportSetup
								.getTopMargin(), reportSetup
								.getBottomMargin());
			}
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: Creating Document with A4 format size=" + document.getPageSize());
		}
		return document;
	}
}
