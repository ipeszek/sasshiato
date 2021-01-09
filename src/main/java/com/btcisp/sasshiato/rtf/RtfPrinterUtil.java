/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato.rtf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.rtf.RtfDecimalAlignedCellFormat;
import com.btcisp.rtf.RtfImage2;
import com.btcisp.rtf.RtfParagraphFormat;
import com.btcisp.rtf.RtfRightDecimalAlignedCellFormat;
import com.btcisp.rtf.RtfRowFormat;
import com.btcisp.rtf.RtfWriter;
import com.btcisp.rtf.Sasshiato2RTFUtil;
import com.btcisp.sasshiato.HeaderRow;
import com.btcisp.sasshiato.HeaderTablet;
import com.btcisp.sasshiato.ImageUtil;
import com.btcisp.sasshiato.PageColumn;
import com.btcisp.sasshiato.PageGroup;
import com.btcisp.sasshiato.Pair;
import com.btcisp.sasshiato.PdfPrinterUtil;
import com.btcisp.sasshiato.RcdConstants;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.ReportPad;
import com.btcisp.sasshiato.ReportSetup;
import com.btcisp.sasshiato.RrgPrinterUtil;
import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.sasshiato.SplitCharacter_Std;
import com.btcisp.sasshiato.StyledChunk;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.sasshiato.StyledTextSizingUtil;
import com.btcisp.sasshiato.StyledTextUtil;
import com.btcisp.sasshiato.TableWidths;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.rtf.document.RtfDocument;

public class RtfPrinterUtil {
	private static RtfPrinterUtil self = new RtfPrinterUtil();

	public static RtfPrinterUtil getSelf() {
		return self;
	}

	Sasshiato2RTFUtil convUtil = Sasshiato2RTFUtil.getSelf();

	private int extractTitleNumber(String what) {
		if (!what.startsWith("title"))
			return -1;
		String num = what.substring("title".length());
		return Integer.parseInt(num);
	}

	private Map parseBookmarkInfo(String bookmarks) {
		Map result = new HashMap();
		StringTokenizer st = new StringTokenizer(bookmarks, ",");
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(word, "=");
			if (st2.countTokens() != 2) {
				SasshiatoTrace.logError("__bookmarks missconfigured: " + word);
			}
			String value = st2.nextToken().trim();
			String keys = st2.nextToken().trim();
			StringTokenizer st3 = new StringTokenizer(keys, "-");
			if (st3.countTokens() == 1) {
				result.put(keys, value);
			} else if (st3.countTokens() == 2) {
				String start = st3.nextToken();
				String end = st3.nextToken();
				int starti = extractTitleNumber(start);
				int endi = extractTitleNumber(end);
				if (starti == -1 || endi == -1 || starti > endi) {
					SasshiatoTrace.logError("__bookmarks missconfigured: "
							+ keys);
				}
				for (int i = starti; i <= endi; i++) {
					String key = "title" + i;
					result.put(key, value);
				}
			} else {
				SasshiatoTrace.logError("__bookmarks missconfigured: "
						+ bookmarks);
			}
		}
		return result;
	}

	public void printTiltesRows(RtfWriter writer, StyledText[] titles,
			Map oldTitleIndexes, RcdInfo info, ReportSetup reportSetup,
			ReportLaF laf, float tableWidth,
			boolean firstTimeTitles, Map context) throws Exception {
		
		if (info.isTitleStyle_withTab()) {
			printTiltesRows_withTab(writer, titles, oldTitleIndexes, info,
					reportSetup, laf, tableWidth, firstTimeTitles, context);
		} else {
			printTiltesRows_STD(writer, titles, oldTitleIndexes, info,
					reportSetup, laf, tableWidth, firstTimeTitles, context);
		}
	}
	
	private void printTiltesRows_STD(RtfWriter writer, StyledText[] titles,
			Map oldTitleIndexes, RcdInfo info, ReportSetup reportSetup,
			ReportLaF laf, float tableWidth,
			boolean firstTimeTitles, Map context) throws Exception {

		if (titles == null)
			titles = new StyledText[0];
		String align = convUtil.convertAlignment(laf.getTitleAlign());
		float widthF = tableWidth;

		int width = convUtil.convert2Twips(widthF);

		int padding = (int) (convUtil
				.convert2Twips(reportSetup.getTableWidth()) - width) / 2;
		padding = 0;// the table is of reduced size
		RtfParagraphFormat pformat = new RtfParagraphFormat();
		pformat.configure(align, padding, padding, 0, 0, convUtil
				.convert2Twips(laf.getParagraphLeading("title")), convUtil
				.convert2Twips(laf.getIndentSize()), convUtil
				.convertFont2FFInstructions(laf.getFont("title")));

		RtfRowFormat tformat = new RtfRowFormat();
		tformat.configure(new int[] { width }, RtfRowFormat.TABLE_ALIGN_CENTER,
				null, RtfRowFormat.BORDER_NONE, true);
		Map bookmarkInfo = new HashMap();
		if (firstTimeTitles && reportSetup.isBookmarksEnabled()
				&& reportSetup.getBookmarksRtf() != null) {
			bookmarkInfo = parseBookmarkInfo(reportSetup.getBookmarksRtf());
			SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL,
					"RTF: Bookmarking titles indexMap:" + oldTitleIndexes
							+ "\r\n" + "bookmarks=" + bookmarkInfo);
		}

		for (int j = 0; j < titles.length; j++) {
			pformat.setStyle(null);
			StyledText ts = (StyledText) titles[j];
			if (j == titles.length - 1)
				pformat.setSpaceAfter(convUtil.convert2Twips(laf
						.getAfterTitlePadding()));
			else
				pformat.setSpaceAfter(0);
			if (firstTimeTitles && reportSetup.isBookmarksEnabled()
					&& reportSetup.getBookmarksRtf() != null) {
				Integer oldIndx = (Integer) oldTitleIndexes.get(Integer
						.valueOf(j));
				if (oldIndx != null) {
					String key = "title" + oldIndx.intValue();
					String bookmark = (String) bookmarkInfo.get(key);
					if (bookmark != null) {
						if (bookmark.startsWith("bk")) {
							Integer.parseInt(bookmark.substring(2));
							String style = "\\s" + bookmark.substring(2);
							pformat.setStyle(style);
						} else {
							pformat.setStyle(null);
							SasshiatoTrace.logError("Misconfigured bookmarks "
									+ reportSetup.getBookmarksRtf());
						}
					} else {
						pformat.setStyle(null);
					}
				}
			}
			if (laf.isTitlesPartOfTable()) {
				writer.beginRow(tformat);
				formatTextAsCell(writer, ts, laf, reportSetup, "title",
						pformat, widthF);
				writer.endRow(false);
			} else
				formatTextAsParagraph(writer, ts, laf, reportSetup, "title",
						pformat, widthF);
		}
	}

	//it is not working because RftWriter.pageBreak needs paragraph=true
	//
//TODO bookmarks not done, bookmarks to work may need paragraph with Tab, not table
// bookmarks are simply a style in RTF
	private void printTiltesRows_withTab(RtfWriter writer, StyledText[] titles,
			Map oldTitleIndexes, RcdInfo info, ReportSetup reportSetup,
			ReportLaF laf, float tableWidth,
			boolean firstTimeTitles, Map context) throws Exception {
		float firstColSize = RrgPrinterUtil.getSelf().maxSpecialFooterTitleSize(
				titles, laf, ReportLaF.STYLE_TYPE_TITLE);
		float separation = laf.getTabularTitleSeparation();
		firstColSize = firstColSize + 4; // 2 added
																	// for
																	// standard
																	// left
																	// padding
																	// and 2 as
																	// safety
		boolean result = false;
		StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(laf);
		for (int j = 0; j < titles.length; j++) {
			RtfRowFormat rf = new RtfRowFormat();
			StyledText ts = (StyledText) titles[j];
			if (!ts.isEmpty()) {
				result = true;
				// String ts = StringUtil.replace(footnotes[j],
				// "//", "\n");
				String tsTxt = ts.getText();
				int idxftl = tsTxt.indexOf(RcdConstants.LEFT_TITLE_TAB);
				int idxftr = tsTxt.indexOf(RcdConstants.RIGHT_TITLE_TAB);
				if (idxftl != -1 || idxftr != -1) {
					String align = idxftl != -1 ? RtfParagraphFormat.ALIGN_LEFT
							: RtfParagraphFormat.ALIGN_RIGHT;
					String delim = idxftl != -1 ? RcdConstants.LEFT_TITLE_TAB
							: RcdConstants.RIGHT_TITLE_TAB;
					int inx = idxftl !=-1 ? idxftl: idxftr;
					
					Pair<StyledText, StyledText> split = StyledTextUtil.splitAtPureTextPosition(ts, inx, false);
					StyledText footL= split.left;
					StyledText footR = split.right;
					footR = footR.substring(delim.length());
					footR = footR.trim();

					RtfParagraphFormat pf = new RtfParagraphFormat();
					pf.configure(align, 0, 0, 0, 0, convUtil.convert2Twips(laf
							.getParagraphLeading(ReportLaF.STYLE_TYPE_TITLE)), convUtil
							.convert2Twips(laf.getIndentSize()),
							convUtil.convertFont2FFInstructions(laf
									.getFont(ReportLaF.STYLE_TYPE_TITLE)));
					if (j == titles.length - 1)
						pf.setSpaceAfter(convUtil.convert2Twips(laf
								.getAfterTitlePadding()));
					else
						pf.setSpaceAfter(0);

					pf.setSpaceBefore(0);
					pf.unsetBarAbove();
					float padding = 0;
					if (RtfParagraphFormat.ALIGN_RIGHT.equals(align)) {
						padding = laf.getFTSeparation();// + 2; 2pt padding moved to right padding
						// pf.setPaddingRight(convUtil.convert2Twips(padding));
						int[] colwidths = convUtil
								.convert2Twips(new float[] {
										firstColSize - padding,
										padding + separation,
										reportSetup.getTableWidth()
												- firstColSize - separation}, 0);
						rf.configure(colwidths,
								RtfRowFormat.TABLE_ALIGN_CENTER, null,
								RtfRowFormat.BORDER_NONE, false);
					} else {
						int[] colwidths = convUtil
								.convert2Twips(new float[] {
										firstColSize + separation,
										reportSetup.getTableWidth()
												- firstColSize -separation}, 0);
						rf.configure(colwidths,
								RtfRowFormat.TABLE_ALIGN_CENTER, null,
								RtfRowFormat.BORDER_NONE, false);
					}
					writer.beginRow(rf);
					pf.setPaddingRight(convUtil.convert2Twips(2)); //RTF can truncate last character
					formatTextAsCell(writer, footL, laf, reportSetup,
							ReportLaF.STYLE_TYPE_TITLE, pf, firstColSize - padding);
					if (RtfParagraphFormat.ALIGN_RIGHT.equals(align))
						formatTextAsCell(writer, StyledText.withConfiguredStyle(" ", ReportLaF.STYLE_TYPE_TITLE, laf), laf, reportSetup,
								ReportLaF.STYLE_TYPE_TITLE, pf, 0);
					pf.setAlign(RtfParagraphFormat.ALIGN_LEFT);
					pf.setPaddingRight(0);
					formatTextAsCell(writer, footR, laf, reportSetup,
							ReportLaF.STYLE_TYPE_TITLE, pf, reportSetup.getTableWidth()
									- firstColSize - separation);
					writer.endRow(false);
				} else {
					RtfRowFormat rf0 = new RtfRowFormat();
					rf0.configure(new int[] { convUtil
							.convert2Twips(reportSetup.getTableWidth()) },
							RtfRowFormat.TABLE_ALIGN_CENTER, null,
							RtfRowFormat.BORDER_NONE, false);

					RtfParagraphFormat pf = new RtfParagraphFormat();
					pf.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, 0,
							0, convUtil
									.convert2Twips(laf.getIndentSize()),
							convUtil.convertFont2FFInstructions(laf
									.getFont(ReportLaF.STYLE_TYPE_TITLE)));
					pf.setSpaceBefore(0);
					pf.unsetBarAbove();
					writer.beginRow(rf0);
					formatTextAsCell(writer, ts, laf, reportSetup, ReportLaF.STYLE_TYPE_TITLE,
							pf, 0);
					writer.endRow(false);
				}

			}
		}

	}

	// assumes all paddings are 0;
	public void formatTextAsCell(RtfWriter writer, StyledText txt, ReportLaF laf,
			ReportSetup setup, String styletype, RtfParagraphFormat pformat,
			float max_size) throws Exception {
		formatText_Impl(writer, txt, laf, setup, styletype, pformat, max_size,
				true);

	}

	public void formatTextAsParagraph(RtfWriter writer, StyledText txt,
			ReportLaF laf, ReportSetup setup, String styletype,
			RtfParagraphFormat pformat, float max_size) throws Exception {
		formatText_Impl(writer, txt, laf, setup, styletype, pformat, max_size,
				false);

	}

	private void formatText_Impl(RtfWriter writer, StyledText stxt, ReportLaF laf,
			ReportSetup setup, String styletype, RtfParagraphFormat pformat,
			float max_size, boolean asCell) throws Exception {
				
		if (max_size == 0)
			max_size = setup.getTableWidth();
		//TODO split into lines first
		List<StyledText> lines = StyledTextUtil.getTestInstance().groupIntoLines(stxt);
		if (lines.size() > 0) {
			if (pformat.getAlign() != RtfParagraphFormat.ALIGN_LEFT) {
				lines.set(0,  lines.get(0).trim());
			} else {
				lines.set(0,  lines.get(0).rightTrim());
			}
			for (int i = 1; i < lines.size(); i++) {
				StyledText stext = lines.get(i);
				
				if (stext.getText().startsWith(" "))
					lines.set(i,  lines.get(i).substring(1));; // remove single blank after /tN
			}
		}
		
		String[] ss = new String[lines.size()];
		int[] indents = new int[lines.size()];
		for (int k = 0; k < lines.size(); k++) {
			// note first token will never be after /t so should not be
			// indented, also first token can be empty and
			// then it should be skipped.

			StyledText line = lines.get(k);
			StyledText adjustedLine= StyledTextSizingUtil.self.adjustForRtfLineBreaks(max_size, line, styletype, laf);
			ss[k] = formatText_singleLine(writer, adjustedLine, laf, setup, styletype,
					true);
			indents[k] = adjustedLine.getIndent();
		}
		if (asCell)
			writer.addCell(ss, indents, pformat);
		else
			writer.addParagraph(ss, indents, pformat);


//		String txt = stxt.getText();
//		String[] ss = RrgPrinterUtil.getSelf().lineSpitText_Prep(txt, laf);
//		int[] indents = new int[ss.length];
//		ss = RrgPrinterUtil.getSelf()
//				.lineSplitText_Perf(ss, indents, laf, true);
//		if (ss.length > 0) {
//			if (pformat.getAlign() != RtfParagraphFormat.ALIGN_LEFT) {
//				ss[0] = ss[0].trim();
//			} else {
//				ss[0] = StringUtil.rightTrim(ss[0]);
//			}
//			for (int i = 1; i < ss.length; i++) {
//				if (ss[i] != null && ss[i].startsWith(" "))
//					ss[i] = ss[i].substring(1); // remove single blank after /tN
//			}
//		}
//		if (max_size == 0)
//			max_size = setup.getTableWidth();
//		if (setup.isRtfSoftLineBreakCalculated()) {
//			//todo use StyledTextSizingUtil instead
//			ss = RrgPrinterUtil.getSelf().adjustForRtfLineBreaking(ss, indents,
//					max_size, laf, styletype);
//		}
//		for (int k = 0; k < ss.length; k++) {
//			// note first token will never be after /t so should not be
//			// indented, also first token can be empty and
//			// then it should be skipped.
//			String s = ss[k];
//			if ("".equals(s))
//				s = " ";
//
//			ss[k] = formatText_singleLine(writer, s, laf, setup, styletype,
//					true);
//		}
//		if (asCell)
//			writer.addCell(ss, indents, pformat);
//		else
//			writer.addParagraph(ss, indents, pformat);

	}

	public String formatText_singleLine(RtfWriter writerNotUsed, StyledText txt,
			ReportLaF laf, ReportSetup setup, String styletype, boolean fromCell)
			throws Exception {
		return formatText_singleLine(txt, laf, setup, styletype,
				null, fromCell);
	}

	public String formatText_singleLine(StyledText txt,
			ReportLaF laf, ReportSetup setup, String styletype, String align,
			boolean fromCellNotUsed) throws Exception {

		if (txt == null)
			return "";

		String plainText = txt.getText();
		if (plainText.indexOf("_PAGE_") != -1) {
			if (!"systemHeader".equals(styletype)) {
				// make sure the _PAGE_ logic is outside of this method (when
				// doing system headers).
				SasshiatoTrace
						.logWarning("The _PAGE_ keyword is not supported for "
								+ styletype
								+ " only in system header, it is not supported for "
								+ styletype + " in the text: " + txt);
			}
			String pagestr = null;

			if (laf.isSystemHeaderInRtfHeader()) {
				if (!setup.isAppend() && setup.isAllowFutureAppend()) {
					long currtime = System.currentTimeMillis();
					pagestr = "{Page }{\\field{\\*\\fldinst {\\insrsid"
							+ currtime
							+ " PAGE }}{\\fldrslt {\\lang1024\\langfe1024\\noproof\\insrsid79999999999 2}}}"
							+ "{ of }{\\field{\\*\\fldinst {\\insrsid"
							+ currtime
							+ " NUMPAGES }}{\\fldrslt {\\lang1024\\langfe1024\\noproof\\insrsid79999999999 2}}}";

				} else {
					// long currtime = System.currentTimeMillis();
					// pagestr=
					// "{Page }{\\field{\\*\\fldinst {\\insrsid" +currtime + "
					// PAGE }}{\\fldrslt
					// {\\lang1024\\langfe1024\\noproof\\insrsid79999999999
					// 2}}}"+
					// "{ of }{\\field{\\*\\fldinst {\\insrsid" +currtime + "
					// NUMPAGES }}{\\fldrslt
					// {\\lang1024\\langfe1024\\noproof\\insrsid79999999999
					// 2}}}";
					pagestr = "{Page }{\\field{\\*\\fldinst { PAGE }}}{ of }{\\field{\\*\\fldinst { NUMPAGES }} {\\fldrslt {\\lang1024\\langfe1024\\noproof }}}";

				}
			} else {
				pagestr = "_PAGE_";
			}
			txt = txt.replace("_PAGE_", pagestr);
		}

		if (plainText.indexOf("_DATE_") != -1) {
			txt = txt.replace("_DATE_", RrgPrinterUtil.getSelf()
					.getDateReplacement(laf));
		}
		int ssidx_start = plainText.toLowerCase().indexOf("~{super");

		if (ssidx_start != -1) {
			txt = txt.replace("~{super", "{\\super");
		}
		StringBuffer newtxt = new StringBuffer();
		StyledChunk[] chunks = txt.getChunks();
		for (int i = 0; i < chunks.length; i++) {
			StyledChunk chunk = chunks[i];
			String chunkInRtf = formatText_singleChunk(chunk, laf, setup, styletype, align, fromCellNotUsed);
			newtxt.append(chunkInRtf);
		}
		return newtxt.toString();
	}

	public String formatText_singleChunk(StyledChunk chunk,
			ReportLaF laf, ReportSetup setup, String styletype, String align,
			boolean fromCellNotUsed) throws Exception {
		
		StringBuffer newtxt = new StringBuffer();
		String txt = chunk.getText();
		
//		if(txt.indexOf("Regulatory") != -1){
//			System.out.println("here");
//		}
		String ffInstructions = chunk.getRftFFInstructions();
		newtxt.append("{" +  ffInstructions); //always wrap chunk in {}
		if(!StringUtil.isEmpty(ffInstructions)){
			newtxt.append(" "); //important to separate instruction from text
		}
//		if(!StringUtil.isEmpty(ffInstructions)){
//			newtxt.append("{" +  ffInstructions);
//		} else {
//			newtxt.append(" "); //needed to separate text from instructions
//		}
		
		String splitChars = ""; // laf.getSplitChars(); //TODO no more RTF
		// hypenation
		if (setup.isRtfSoftLineBreakHyphenated()) {
			splitChars = laf.getSplitChars();
		}
		if (align != null && align.toUpperCase().indexOf("D") != -1) { 
			// this allows the method to be called for D or RD cells, in this case hypenation
		    //does not happen
			splitChars = "";
		}

		int i = 0;

		char ESCAPE = laf.getEscapeChar();
		while (i < txt.length()) {
			char c = txt.charAt(i);
			// if(c == '\\') {
			// newtxt.append(c); //double all forward slashes
			// }
			if (c != ' '
					&& c != '-'
					&& splitChars.length() > 0
					&& RrgPrinterUtil.getSelf().isSplitCharacter_RTFSyntax(c,
							i, txt, laf, splitChars)) {
				newtxt.append(c).append("{\\cf1\\fs0\\-}");
				i++;
			} else if (i < txt.length() - 3 && c == ESCAPE
					&& txt.charAt(i + 1) == 's' && txt.charAt(i + 2) == '#') {
				int cmdend = txt.indexOf(' ', i + 2);
				if (cmdend == -1)
					cmdend = txt.length();
				if (cmdend != -1) {
					String after_p = txt.substring(i + 3, cmdend);
					int it = 0;
					try {
						it = Integer.parseInt(after_p);
					} catch (NumberFormatException e) {
						it = RrgPrinterUtil.getSelf()
								.convertSymbolHintToAsciiCode(after_p);
					}

					if (it == -1) {
						SasshiatoTrace.logError("Invalid " + ESCAPE
								+ "s command, txt=" + txt);
						i++;
					} else {
						newtxt.append("{\\f1 \\'" + Integer.toHexString(it)
								+ "}");
						i = cmdend + 1;
					}
				} else {
					SasshiatoTrace
							.logError("Invalid "
									+ ESCAPE
									+ "s command, expecting BLANK to end the /s instruction, txt="
									+ txt);
					i++;
				}
			} else if (i < txt.length() - 2 && c == ESCAPE
					&& txt.charAt(i + 1) == '#') {
				int cmdend = txt.indexOf(' ', i + 1);
				if (cmdend == -1)
					cmdend = txt.length();
				if (cmdend != -1) {
					String after_p = txt.substring(i + 2, cmdend);
					int it = -1;
					try {
						it = Integer.parseInt(after_p);
					} catch (NumberFormatException e) {

					}

					if (it == -1) {
						SasshiatoTrace.logError("Invalid " + ESCAPE
								+ "# command, txt=" + txt);
						i++;
					} else {
						newtxt.append("{\\'" + Integer.toHexString(it) + "}");
						i = cmdend + 1;
					}
				} else {
					SasshiatoTrace
							.logError("Invalid "
									+ ESCAPE
									+ "# command, expecting BLANK to end the /s instruction, txt="
									+ txt);
					i++;
				}
			} else {
				newtxt.append(c);
				i++;
			}
		}
		
//		if(!StringUtil.isEmpty(ffInstructions)){
//			newtxt.append("}");
//		}
		newtxt.append("}"); //always wrap chunk in {}
		return newtxt.toString();
	}
	
	public String formatText_singleLineSimple(StyledText txt,
			ReportLaF laf, ReportSetup setup) throws Exception {

		if (txt == null)
			return "";

		StringBuffer newtxt = new StringBuffer();
		StyledChunk[] chunks = txt.getChunks();
		for (int i = 0; i < chunks.length; i++) {
			StyledChunk chunk = chunks[i];
			String chunkInRtf = formatText_singleChunkSimple(chunk, laf, setup);
			newtxt.append(chunkInRtf);
		}
		return newtxt.toString();
	}
	
	public String formatText_singleChunkSimple(StyledChunk chunk,
			ReportLaF laf, ReportSetup setup) throws Exception {
		
		StringBuffer newtxt = new StringBuffer();
		String txt = chunk.getText();
		
		String ffInstructions = chunk.getRftFFInstructions();
		newtxt.append("{" +  ffInstructions); //always wrap chunk in {}
		if(!StringUtil.isEmpty(ffInstructions)){
			newtxt.append(" "); //important to separate instruction from text
		}
		
//		if(!StringUtil.isEmpty(ffInstructions)){
//			newtxt.append("{" +  ffInstructions);
//		} else {
//			newtxt.append(" ");
//		}
		
		
		newtxt.append(txt);
		
//		if(!StringUtil.isEmpty(ffInstructions)){
//			newtxt.append("}");
//		}
		newtxt.append("}"); //always wrap chunk in {}

		return newtxt.toString();
	}
	
	
	public void printHeaderRows(RtfWriter writer, HeaderTablet tablet,
			PageGroup pageGroup, ReportLaF raf, ReportSetup reportSetup)
			throws Exception {

		StyledText currentVbgh = tablet.getCurrentVarByGroupHeading();
		if (tablet.requiresVarByGroupHeading()
				&& currentVbgh != null && !currentVbgh.isEmpty()) {
			RtfRowFormat tformat = new RtfRowFormat();
			tformat.configure(new int[] { convUtil.convert2Twips(pageGroup
					.calculateTotalWidth(pageGroup.getForcedTotWidth())) },
					RtfRowFormat.TABLE_ALIGN_CENTER,
					RtfRowFormat.ROW_ALIGN_BOTTOM, RtfRowFormat.BORDER_NONE,
					true);
			RtfParagraphFormat cformat = new RtfParagraphFormat();
			cformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, convUtil
					.convert2Twips(raf.getHeaderBottomPadding()), convUtil
					.convert2Twips(raf.getParagraphLeading("title")), convUtil
					.convert2Twips(raf.getIndentSize()), convUtil
					.convertFont2FFInstructions(raf.getFont("title")));
			writer.beginRow(tformat);
			formatTextAsCell(writer, tablet.getCurrentVarByGroupHeading(), raf,
					reportSetup, "title", cformat, pageGroup
							.calculateTotalWidth());
			writer.endRow(false);
		} else {
			// title padding added when printing titles.
		}

		HeaderRow[] rows = tablet.getHeaderRows();

		if (raf.printEmptyHeaders()
				|| !RrgPrinterUtil.getSelf().isEmptyHeader(tablet)) {

			for (int i = 0; i < rows.length; i++) {
				HeaderRow hr = (HeaderRow) rows[i];
				int[] starts = hr.getStartColumns();
				int[] ends = hr.getEndColumns();
				StyledText[] cells = hr.getHeaderCells();
				int[] hborders = hr.getBorderInstructions();
				String[] rrgaligns = hr.getAlignments();
				String[] aligns = convUtil.convertAlignments(rrgaligns);
				float[] widths = new float[cells.length];

//		        boolean scaleDown = false;
//		        
//		        float pageGroupWidth= pageGroup.calculateTotalWidth();
//		        float ratio = reportSetup.getTableWidth() / pageGroupWidth;
//		        if(ratio < 1)
//		        	ratio = 1f; //only scale up
//		        if(!pageGroup.isStretchRequired()) {
//		   	      if(pageGroupWidth < reportSetup.getTableWidth()){
//		   	    	 //DO NOT SCALE UP EVEN IF IT IS POSSIBLE, mimics PDF code
//		   	    	 ratio = 1f;
//		   		  }
//		        }
//		        
//		        float totalWidth = 0;
				for (int j = 0; j < cells.length; j++) {
					//multiplication by ratio moved out
					widths[j] = pageGroup.calculateTotalWidth(starts[j],
							ends[j]);
//					totalWidth += widths[j];
				}

//				if(totalWidth> 0 && totalWidth > reportSetup.getTableWidth()){
//					widths[0] += reportSetup.getTableWidth() - totalWidth; 
//				}
				
				
				RtfRowFormat tformat = new RtfRowFormat();
				// todo border vs borders, should be borders all the time.
				// if i=0 partail cells should have top border;
				String[] border = new String[cells.length];
				boolean rowHasVerticalBorders = false;
				for (int j = 0; j < border.length; j++) {
					boolean lastCellInRow = (j == cells.length -1);
					int leftRightBorder = hborders[j];
					int singleVerticalBorder = leftRightBorder;  //TODO debug this 
					if(!lastCellInRow) {
						singleVerticalBorder = singleVerticalBorder & Rectangle.LEFT; //to not duplicate left-right borders print only left borders
					}
					border[j] = convUtil.convertBorderInstructions("", singleVerticalBorder);
					
					if(!rowHasVerticalBorders && singleVerticalBorder > 0) {
						rowHasVerticalBorders = true;
					}
				}
				if (i == 0) {
					for (int j = 0; j < border.length; j++) {
						border[j] = border[j] + RtfRowFormat.BORDER_TOP;
					}
				}
				if (i == rows.length - 1) {
					for (int j = 0; j < border.length; j++) {
						border[j] = border[j] + RtfRowFormat.BORDER_BOTTOM;
					}
				}
				tformat.configure(convUtil.convert2Twips(widths, pageGroup
						.getForcedTotWidth()), RtfRowFormat.TABLE_ALIGN_CENTER,
						RtfRowFormat.ROW_ALIGN_BOTTOM,
						RtfRowFormat.BORDER_NONE, true);
				if (i < rows.length - 1) {
					if(!rowHasVerticalBorders)
						tformat.setPartialBorders(true);
					for (int j = 0; j < border.length; j++) {
						if (!cells[j].isEmpty())
							border[j] = border[j] + RtfRowFormat.BORDER_BOTTOM;
						else
							border[j] = border[j] + RtfRowFormat.BORDER_NONE;
					}
				}
				if (raf.headerUnderline_useSeparationAdjustement()) {
					int[] adjLeft = new int[cells.length];
					int[] adjRigth = new int[cells.length];
					boolean hasSeparationAdj = false;
					for (int j = 0; j < adjRigth.length; j++) {
						int leftRightBorder = hborders[j];
						boolean hasRightBorder = (leftRightBorder & Rectangle.RIGHT) > 0;
						boolean hasLeftBorder = (leftRightBorder & Rectangle.LEFT) > 0;
					    float separationAdjR = raf
								.getColumnSeparationAdjustmentRight(ends[j]);
						float separationAdjL = raf
								.getColumnSeparationAdjustmentLeft(starts[j]);
						if(hasRightBorder){
							separationAdjR = 0f;
						}
						if(hasLeftBorder){
							separationAdjL = 0f;
						}
						if (separationAdjL > raf.getHeaderUnderlineSpacing()
								|| separationAdjR > raf
										.getHeaderUnderlineSpacing())
							hasSeparationAdj = true;
						adjLeft[j] = convUtil.convert2Twips(separationAdjL);
						adjRigth[j] = convUtil.convert2Twips(separationAdjR);
						if (adjLeft[j] < 50)
							adjLeft[j] = 50;
						if (adjRigth[j] < 50)
							adjRigth[j] = 50;
					}
					adjLeft[0] = 0;
					adjRigth[adjRigth.length - 1] = 0;
					if (hasSeparationAdj) {
						tformat.setUsePartialBorderSizing(true);
						tformat.setPartialBorderSizeRight(adjRigth);
						tformat.setPartialBordersSizeLeft(adjLeft);
					}
				}
				tformat.setBorders(border);
				writer.beginRow(tformat);
				for (int j = 0; j < cells.length; j++) {
					// int colspan = ends[j] - starts[j] + 1;
					int colspan = pageGroup.calculateColumnSpan(starts[j],
							ends[j]);
					if (colspan < 0)
						throw new Exception(
								"ERROR: Unexpected Error, please please file issue with https://github.com/ipeszek/sasshiato, colspan="
										+ colspan);
					StyledText celltext = cells[j];
					boolean hasSeparationAdjR = raf
							.hasColumnSeparationAdjustmentRight(starts[j]);
					boolean hasSeparationAdjL = raf
							.hasColumnSeparationAdjustmentLeft(ends[j]);
					float separationAdjR = raf
							.getColumnSeparationAdjustmentRight(starts[j]);
					float separationAdjL = raf
							.getColumnSeparationAdjustmentLeft(ends[j]);
					float columnSeparation = raf.getColumnSeparation();

					if (celltext.isEmpty())
						celltext = StyledText.withConfiguredStyle(" ", ReportLaF.STYLE_TYPE_HEADER, raf);
					if (1 == 1) {
						String alignment = aligns[j];
						// if(colspan > 1) alignment =
						// RtfParagraphFormat.ALIGN_CENTER; //RP verify with Iza
						// System.out.println("aling=" + alignment + ", header="
						// + celltext);
						int pageColId = pageGroup
								.map2OnPageColumnNumber(starts[j]);
						float indentLeft = 0f;
						float indentRight = 0f;
						if (RtfParagraphFormat.ALIGN_LEFT.equals(alignment)) {
							if (pageColId > 0)
								indentLeft = columnSeparation + separationAdjL;
						} else if (RtfParagraphFormat.ALIGN_RIGHT
								.equals(alignment)) {
							if (j < cells.length - 1)
								indentRight = columnSeparation + separationAdjR;
						}

						RtfParagraphFormat cformat = new RtfParagraphFormat();
						int spaceB = 0;
						int spaceA = convUtil.convert2Twips(raf
								.getHeaderBottomPadding());
						cformat.configure(alignment, convUtil
								.convert2Twips(indentLeft), convUtil
								.convert2Twips(indentRight), spaceB, spaceA,
								convUtil.convert2Twips(raf
										.getParagraphLeading("header")),
								convUtil.convert2Twips(raf.getIndentSize()),
								convUtil.convertFont2FFInstructions(raf
										.getFont("header")));
						formatTextAsCell(writer, celltext, raf, reportSetup,
								"header", cformat, widths[j]);
					}
				}
				writer.endRow(false);
			}
		} else {
			//insert simple underline, make it as thin as possible
			RtfRowFormat tformat = new RtfRowFormat();
			int[] colwidths = new int[] { convUtil.convert2Twips(reportSetup
					.getTableWidth()) };
			tformat.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
					RtfRowFormat.ROW_ALIGN_BOTTOM,
					RtfRowFormat.BORDER_BOTTOM, false);
			
			RtfParagraphFormat cformat = new RtfParagraphFormat();
			int spaceB = 0;
			int spaceA = 0;
			cformat.configure(RtfParagraphFormat.ALIGN_CENTER, 0, 0, spaceB, spaceA,
					0,
					0,
					null);
			writer.beginRow(tformat);
			//this inserts simple underline
			formatTextAsCell(writer, StyledText.withNoStyle("\\fs0"), raf, reportSetup, "header",
					cformat, colwidths[0]);
			writer.endRow(false);
			writer.writeSpecial("\\fs" + convUtil.convert2FontSize(raf.getFontSize()));


		}

	}

	public void printBodyRows(RtfWriter writer, RcdRow row,
			PageGroup pageGroup, ReportLaF raf, ReportSetup reportSetup,
			TableWidths widths, int hint) throws Exception {

		// float[] maxWidth = widths.getMaxWidths();
		// span whole row
		boolean columnSpanWholeRow = false;
		String col0 = row.getSpan0();
		if (!StringUtil.isEmpty(col0)) {
			if (col0.trim().equalsIgnoreCase("all")) {
				columnSpanWholeRow = true;
			} else {
				SasshiatoTrace
						.logError("Unsupported __span_0 value, this value is ingored at row rowid="
								+ row.getRowid());
			}
		}
		// end span whole row
		float columnSeparation = raf.getColumnSeparation();
		float[] offsets = raf.getDecimalOffsets();// rr
		float[] maxWidthB4Dec = widths.getMaxWidthsB4Decimal();
		float[] maxWidthB4Dec1 = widths.getMaxWidthsB4Decimal1();
		float[] maxWidthB4Dec2 = widths.getMaxWidthsB4Decimal2();
		// float[] maxWidthsAfterDecimal = widths.getMaxWidthsAfterDecimal();
		// float[] actualWidth_notused = widths.getActualWidths();
		float[] actualWidth1 = widths.getActualWidths1();
		float[] actualWidth2 = widths.getActualWidths2();
		float[] extra_num_padding = widths.getExtraWidths();
		Font font = raf.getBodyFont();
		int[] colids = pageGroup.getColumnIDs();
		PageColumn[] cols = pageGroup.getAllColumns();
		// float[] colWidths = pageGroup.getColumnWidths();
		//Map allcols = row.getCols();
		float indent = row.getIndentlev() * raf.getIndentSize();
		String suffix = row.getSuffix();
		String[] aligns = row.getAllAligns();
		String[] alignsrtf = convUtil.convertAlignments(aligns);
		RtfRowFormat rformat = new RtfRowFormat();
		int spaceB = 0;
		int spaceA = 0;
		String border = RtfRowFormat.BORDER_NONE; // todo add broder to the
													// lastone
		String suffix_border = RtfRowFormat.BORDER_NONE; // todo add broder
															// to the lastone
		if ((hint == ReportPad.HINT_LAST_ROW_ON_PAGE || hint == ReportPad.HINT_SINGLE_ROW_ON_PAGE)
				&& raf.isSeparatorBelowTable()) {
			// System.out.println("got in rows");
			if (("~-2n".equals(suffix) || raf.getDoubleEscapeCharString()
					.equals(suffix))) {
				suffix_border = RtfRowFormat.BORDER_BOTTOM;
			} else {
				border = RtfRowFormat.BORDER_BOTTOM;
			}
			spaceA = convUtil.convert2Twips(raf.getAfterBodyPadding());
		}
		if (columnSpanWholeRow) {
			//TODO table borders
			rformat.configure(new int[] { convUtil.convert2Twips(pageGroup
					.calculateTotalWidth()) }, RtfRowFormat.TABLE_ALIGN_CENTER,
					null, border, false);
		} else {
			rformat.configure(convUtil.convert2Twips(pageGroup
					.getColumnWidths(), pageGroup.getForcedTotWidth()),
					RtfRowFormat.TABLE_ALIGN_CENTER, null, border, false);
			for (int i = 0; i < colids.length; i++) {
				int j = colids[i];
				int cellBorders = row.getColumnBorderInstructions(j);
				String inst = convUtil.convertBorderInstructions(border, cellBorders);
				if(!StringUtil.areEqual(inst, border)){
					rformat.setBorder(i, inst); //rformat is per page use i not global j
				}
			}
		}
		writer.beginRow(rformat);
		for (int i = 0; i < colids.length; i++) {
			if (i > 0) {
				indent = 0;
				if (columnSpanWholeRow)
					break;
			}
			String extraInstructions = "";
			if (i == 0
					&& raf.isInText()
					&& (row.getKeepn() == 1 || "~-2n".equals(suffix) || raf
							.getDoubleEscapeCharString().equals(suffix))) {
				extraInstructions = "\\keepn";
			}

			int j = colids[i];
			float offset = offsets[j];
			boolean hasSeparationAdjR = raf
					.hasColumnSeparationAdjustmentRight(j);
			boolean hasSeparationAdjL = raf
					.hasColumnSeparationAdjustmentLeft(j);
			float separationAdjR = raf.getColumnSeparationAdjustmentRight(j);
			float separationAdjL = raf.getColumnSeparationAdjustmentLeft(j);
			if (columnSpanWholeRow && i == 0 && j != 0)
				throw new Exception(
						"Unsupported use of __span_0, each page group needs to contain __col_0");
			float cwidth = 0;
			if (!columnSpanWholeRow) {
				cwidth = pageGroup.getColumnWidths()[i];
			} else {
				cwidth = pageGroup.calculateTotalWidth();
			}
			PageColumn column = cols[i];
			StyledText celltext = row.getColumAsStyledText(j, ReportLaF.STYLE_TYPE_BODY, raf);
			PdfPCell cell = null;

			String cellAlignment = "C";
			String cellAlignmentrtf = convUtil.convertAlignment(cellAlignment);
			if (aligns.length > j) {
				cellAlignment = aligns[j];
				cellAlignmentrtf = alignsrtf[j];
			}
			RtfParagraphFormat pformat = new RtfParagraphFormat();
			int indentLeft = 0;
			int indentRight = 0;
			// todo fix pdf right formatting
			if ("R".equals(cellAlignment)) {
				if (i < cols.length - 1) {
					indentRight = convUtil.convert2Twips(columnSeparation
							+ separationAdjR);
				}
			} else if ("L".equals(cellAlignment)) {
				if (i > 0) {
					indentLeft = convUtil.convert2Twips(columnSeparation
							+ separationAdjL + indent);
				} else {
					indentLeft = convUtil.convert2Twips(indent);
				}
			}
			pformat.configure(cellAlignmentrtf, indentLeft, indentRight,
					spaceB, spaceA, convUtil.convert2Twips(raf
							.getParagraphLeading("body")), convUtil
							.convert2Twips(raf.getIndentSize()), convUtil
							.convertFont2FFInstructions(raf.getFont("body")));
			pformat.setExtraInstructions(extraInstructions);

			if (celltext == null || celltext.isEmpty()) {
				formatTextAsCell(writer, StyledText.EMPTY, raf, reportSetup, "body", pformat,
						cwidth);
			} else {
				if ("C".equals(cellAlignment) || "R".equals(cellAlignment)
						|| "L".equals(cellAlignment)) {
					formatTextAsCell(writer, celltext, raf, reportSetup,
							"body", pformat, cwidth);
				} else if ("RD".equals(cellAlignment)) {
					float exta_padding = column.getNumericColumnPadding();
					float extraWidth = extra_num_padding[j];
					float[] mv = {
							offset + columnSeparation + separationAdjL
									+ extraWidth + exta_padding
									+ maxWidthB4Dec[j], maxWidthB4Dec2[j] };

					// if(!StringUtil.isEmpty(celltext) &&
					// celltext.indexOf("1.732") != -1) {
					// System.out.println(j + " (" + celltext + ") RD"+
					// (columnSeparation + extraWidth + exta_padding +
					// actualWidth1[j] +", " + actualWidth2[j] + ",xx=" +
					// extraWidth+ ", tot_col=" + column.getColumnWidth()));
					// }

					StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(raf);
					Pair<StyledText, StyledText> split = styledTextUtil.split(celltext, " ", false);
					//for RFT alignment to work space needs to be moved to first cell.
					StyledText celltext1 = split.left.appendToLastChunk(" ", "body", raf);
					StyledText celltext2 = split.right.leftTrim();
					
					if (mv[1] == 0) {
						if (!celltext2.isEmpty()) {
							SasshiatoTrace
									.logError("unexpected condition in RD alignment calculation, widthB4Dec2=0 and column "
											+ j
											+ " has 2 words:"
											+ celltext
											+ ", row=" + row);
						}
						RtfDecimalAlignedCellFormat dformat = new RtfDecimalAlignedCellFormat();
						dformat.configure(convUtil.convert2Twips(mv[0]),
								spaceB, spaceA, convUtil.convert2Twips(raf
										.getParagraphLeading("body")), convUtil
										.convertFont2FFInstructions(raf
												.getFont("body")));
						
						String cellRftText;
						if (raf.applySpecialCharReplacementsToNumericCells()) {
							cellRftText = formatText_singleLine(celltext, raf,
									reportSetup, "body", "D", true);
						} else {
							cellRftText = formatText_singleLineSimple(celltext, raf,
									reportSetup);
						}
						
						writer.addCellWithDecimalAlignment(cellRftText, dformat);
					} else {
						float secondtab = offset + columnSeparation
								+ separationAdjL + extraWidth + exta_padding
								+ actualWidth1[j] + mv[1];
						RtfRightDecimalAlignedCellFormat rdf = new RtfRightDecimalAlignedCellFormat();
						rdf.configure(convUtil.convert2Twips(mv[0]), convUtil
								.convert2Twips(secondtab), spaceB, spaceA,
								convUtil.convert2Twips(raf
										.getParagraphLeading("body")), convUtil
										.convertFont2FFInstructions(raf
												.getFont("body")));
						rdf.setExtraInstructions(extraInstructions);
						
						String cellRftText1;
						if (raf.applySpecialCharReplacementsToNumericCells()) {
							cellRftText1 = formatText_singleLine(celltext1, raf,
									reportSetup, "body", "D", true);
						} else {
							cellRftText1 = formatText_singleLineSimple(celltext1, raf,
									reportSetup);
						}
						String cellRftText2;
						if (raf.applySpecialCharReplacementsToNumericCells()) {
							cellRftText2 = formatText_singleLine(celltext2, raf,
									reportSetup, "body", "D", true);
						} else {
							cellRftText2 = formatText_singleLineSimple(celltext2, raf,
									reportSetup);
						}

						writer.addCellWithRightDecimalAlignment(cellRftText1,
								cellRftText2, rdf);
					}

				} else if ("D".equals(cellAlignment)) {
					float extraWidth = extra_num_padding[j];
					float exta_padding = column.getNumericColumnPadding();
					// System.out.println("colsize=" + column.getColumnWidth() +
					// " stretch padding=" + exta_padding);
					// extraWidth=(colWidths[i]-actualWidth[j])/2;
					float width_2_decimal = offset + columnSeparation
							+ separationAdjL + maxWidthB4Dec[j] + extraWidth
							+ exta_padding;
					// total_padding = 0;
					RtfDecimalAlignedCellFormat dformat = new RtfDecimalAlignedCellFormat();
					dformat.configure(convUtil.convert2Twips(width_2_decimal),
							spaceB, spaceA, convUtil.convert2Twips(raf
									.getParagraphLeading("body")), convUtil
									.convertFont2FFInstructions(raf
											.getFont("body")));
					dformat.setExtraInstructions(extraInstructions);
					
					String cellRftText;
					if (raf.applySpecialCharReplacementsToNumericCells()) {
						cellRftText = formatText_singleLine(celltext, raf,
								reportSetup, "body", "D", true);
					} else {
						cellRftText = formatText_singleLineSimple(celltext, raf,
								reportSetup);
					}

					writer.addCellWithDecimalAlignment(cellRftText, dformat);
					// if(!StringUtil.isEmpty(celltext) &&
					// celltext.indexOf("1.732") != -1) {
					// if((j==2) && !StringUtil.isEmpty(celltext)) {
					// System.out.println(j + " (" + celltext + ") D"+
					// (width_2_decimal) +"," +maxWidthB4Dec[j]
					// +","+extraWidth+"," +exta_padding + ", aw=" +
					// table.getAbsoluteWidths()[j] + ",col=" +
					// column.getColumnWidth());
					// }

				} else {
					SasshiatoTrace.logError("Not supported alignment "
							+ cellAlignment + " requested for rowid="
							+ row.getRowid());
					formatTextAsCell(writer, celltext, raf, reportSetup,
							"body", pformat, 0);
				}

			}

		}
		writer.endRow(false);

		if ("~-2n".equals(suffix)
				|| raf.getDoubleEscapeCharString().equals(suffix)) {
			
			//TODO table borders
			RtfRowFormat tformat = new RtfRowFormat();
			tformat
					.configure(new int[] { convUtil
							.convert2Twips(pageGroup
									.calculateTotalWidth(pageGroup
											.getForcedTotWidth())) },
							RtfRowFormat.TABLE_ALIGN_CENTER, null,
							suffix_border, false);
			RtfParagraphFormat cFormat = new RtfParagraphFormat();
			cFormat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, 0,
					convUtil.convert2Twips(raf.getParagraphLeading("body")),
					convUtil.convert2Twips(raf.getIndentSize()), convUtil
							.convertFont2FFInstructions(raf.getFont("body")));
			writer.beginRow(tformat);
			RtfPrinterUtil.getSelf().formatTextAsCell(writer, StyledText.withConfiguredStyle(" ", "body", raf), raf,
					reportSetup, "body", cFormat,
					pageGroup.calculateTotalWidth());
			writer.endRow(false);
		}

	}

	public boolean printFootnotes(RtfWriter writer, StyledText[] footnotes,
			ReportLaF laf, RcdInfo info, ReportSetup reportSetup,
			float extraTopPadding, boolean barAbove) throws Exception {
		if (info.isFooterStyle_withFT()) {
			return printFootnotes_withFT(writer, footnotes, laf, info,
					reportSetup, extraTopPadding, barAbove);
		} else {
			return printFootnotes_STD(writer, footnotes, laf, info,
					reportSetup, extraTopPadding, barAbove);
		}
	}

	private boolean printFootnotes_STD(RtfWriter writer, StyledText[] footnotes,
			ReportLaF laf, RcdInfo info, ReportSetup reportSetup,
			float extraTopPadding, boolean barAbove) throws Exception {
		if (footnotes == null)
			footnotes = new StyledText[0];
		boolean result = false;
		String align = RtfParagraphFormat.ALIGN_LEFT;
		int width = convUtil.convert2Twips(reportSetup.getTableWidth());
		RtfParagraphFormat pformat = new RtfParagraphFormat();
		pformat.configure(align, 0, 0, 0, 0, convUtil.convert2Twips(laf
				.getParagraphLeading("footnote")), convUtil.convert2Twips(laf
				.getIndentSize()), convUtil.convertFont2FFInstructions(laf
				.getFont("footnote")));

		int[] colwidths = new int[] { width };

		for (int j = 0; j < footnotes.length; j++) {
			RtfRowFormat rf = new RtfRowFormat();
			rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER, null,
					RtfRowFormat.BORDER_NONE, false);
			result = true;
			StyledText ts = (StyledText) footnotes[j];
			if (j == 0) {
				pformat.setSpaceBefore(convUtil.convert2Twips(extraTopPadding)
						+ convUtil
								.convert2Twips(laf.getBeforeFootnotePadding()));
				if (barAbove)
					pformat.setBarAbove();
			} else {
				pformat.setSpaceBefore(0);
				pformat.unsetBarAbove();
			}
			if (laf.isFootnotesPartOfTable()) {
				writer.beginRow(rf);
				formatTextAsCell(writer, ts, laf, reportSetup, "footnote",
						pformat, 0);
				writer.endRow(false);
			} else
				formatTextAsParagraph(writer, ts, laf, reportSetup, "footnote",
						pformat, 0);
		}
		return result;
	}

	// TODO try striking above first text if extraTopPadding >0
	private boolean printFootnotes_withFT(RtfWriter writer, StyledText[] footnotes,
			ReportLaF laf, RcdInfo info, ReportSetup reportSetup,
			float extraTopPadding, boolean barAbove) throws Exception {
		float firstColSize = RrgPrinterUtil.getSelf().maxSpecialFooterTitleSize(
				footnotes, laf, ReportLaF.STYLE_TYPE_FOOTNOTE);
		float separation = laf.getFTSeparation();
		firstColSize = firstColSize  + 4; // 2 added
																	// for
																	// standard
																	// left
																	// padding
																	// and 2 as
																	// safety
		boolean result = false;
		StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(laf);
		for (int j = 0; j < footnotes.length; j++) {
			RtfRowFormat rf = new RtfRowFormat();
			StyledText ts = (StyledText) footnotes[j];
			if (!ts.isEmpty()) {
				result = true;
				// String ts = StringUtil.replace(footnotes[j],
				// "//", "\n");
				String tsTxt = ts.getText();
				int idxftl = tsTxt.indexOf(RcdConstants.LEFT_FOOTNOTE_TAB);
				int idxftr = tsTxt.indexOf(RcdConstants.RIGHT_FOOTNOTE_TAB);
				if (idxftl != -1 || idxftr != -1) {
					String align = idxftl != -1 ? RtfParagraphFormat.ALIGN_LEFT
							: RtfParagraphFormat.ALIGN_RIGHT;
					String delim = idxftl != -1 ? RcdConstants.LEFT_FOOTNOTE_TAB
							: RcdConstants.RIGHT_FOOTNOTE_TAB;
					int inx = idxftl !=-1 ? idxftl: idxftr;
					
					Pair<StyledText, StyledText> split = StyledTextUtil.splitAtPureTextPosition(ts, inx, false);
					StyledText footL= split.left;
					StyledText footR = split.right;
					footR = footR.substring(delim.length());
					footR = footR.trim();

					RtfParagraphFormat pf = new RtfParagraphFormat();
					pf.configure(align, 0, 0, 0, 0, convUtil.convert2Twips(laf
							.getParagraphLeading("footnote")), convUtil
							.convert2Twips(laf.getIndentSize()),
							convUtil.convertFont2FFInstructions(laf
									.getFont("footnote")));
					if (j == 0) {
						// cell.setBorder(PdfPCell.TOP);
						// System.out.println("TOT padding "+ (
						// convUtil.convert2Twips(extraTopPadding) +
						// convUtil.convert2Twips(laf.getBeforeFootnotePadding())));
						pf.setSpaceBefore(convUtil
								.convert2Twips(extraTopPadding)
								+ convUtil.convert2Twips(laf
										.getBeforeFootnotePadding()));
						if (barAbove)
							pf.setBarAbove();
					} else {
						pf.setSpaceBefore(0);
						pf.unsetBarAbove();
					}
					float padding = 0;
					if (RtfParagraphFormat.ALIGN_RIGHT.equals(align)) {
						padding = laf.getFTSeparation();// + 2; 2pt padding moved to right padding
						// pf.setPaddingRight(convUtil.convert2Twips(padding));
						int[] colwidths = convUtil
								.convert2Twips(new float[] {
										firstColSize - padding,
										padding + separation,
										reportSetup.getTableWidth()
												- firstColSize - separation}, 0);
						rf.configure(colwidths,
								RtfRowFormat.TABLE_ALIGN_CENTER, null,
								RtfRowFormat.BORDER_NONE, false);
					} else {
						int[] colwidths = convUtil
								.convert2Twips(new float[] {
										firstColSize + separation,
										reportSetup.getTableWidth()
												- firstColSize -separation}, 0);
						rf.configure(colwidths,
								RtfRowFormat.TABLE_ALIGN_CENTER, null,
								RtfRowFormat.BORDER_NONE, false);
					}
					writer.beginRow(rf);
					pf.setPaddingRight(convUtil.convert2Twips(2)); //RTF can truncate last character
					formatTextAsCell(writer, footL, laf, reportSetup,
							"footnote", pf, firstColSize - padding);
					if (RtfParagraphFormat.ALIGN_RIGHT.equals(align))
						formatTextAsCell(writer, StyledText.withConfiguredStyle(" ", "footnote", laf), laf, reportSetup,
								"footnote", pf, 0);
					pf.setAlign(RtfParagraphFormat.ALIGN_LEFT);
					pf.setPaddingRight(0);
					formatTextAsCell(writer, footR, laf, reportSetup,
							"footnote", pf, reportSetup.getTableWidth()
									- firstColSize - separation);
					writer.endRow(false);
				} else {
					RtfRowFormat rf0 = new RtfRowFormat();
					rf0.configure(new int[] { convUtil
							.convert2Twips(reportSetup.getTableWidth()) },
							RtfRowFormat.TABLE_ALIGN_CENTER, null,
							RtfRowFormat.BORDER_NONE, false);

					RtfParagraphFormat pf = new RtfParagraphFormat();
					pf.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, 0,
							convUtil.convert2Twips(laf
									.getParagraphLeading("footnote")), convUtil
									.convert2Twips(laf.getIndentSize()),
							convUtil.convertFont2FFInstructions(laf
									.getFont("footnote")));
					if (j == 0) {
						// cell.setBorder(PdfPCell.TOP);
						pf.setSpaceBefore(convUtil
								.convert2Twips(extraTopPadding)
								+ convUtil.convert2Twips(laf
										.getBeforeFootnotePadding()));
						if (barAbove)
							pf.setBarAbove();
					} else {
						pf.setSpaceBefore(0);
						pf.unsetBarAbove();
					}
					writer.beginRow(rf0);
					formatTextAsCell(writer, ts, laf, reportSetup, "footnote",
							pf, 0);
					writer.endRow(false);
				}

			}
		}
		return result;
	}

	// TODO try striking above first text if extraTopPadding >0
	public void printSystemFootnotes(RtfWriter writer, ReportLaF laf,
			RcdInfo info, ReportSetup reportSetup, float extraTopPadding,
			boolean barAbove) throws Exception {
		float[] percentSystemWitdhs = laf
				.getSystemFootnoteColumnPercentWidths();
		int sftable_nocolumns = percentSystemWitdhs.length;

		float[] absoluteWidths = new float[percentSystemWitdhs.length];
		float runningTotal = 0;
		for (int i = 0; i < absoluteWidths.length; i++) {
			if (i < absoluteWidths.length - 1) {
				absoluteWidths[i] = percentSystemWitdhs[i]
						* reportSetup.getTableWidth();
				runningTotal = runningTotal + absoluteWidths[i];
			} else {
				absoluteWidths[i] = reportSetup.getTableWidth() - runningTotal;
			}
		}

		// String pgmfoot = null;
		// if(context!=null && context.get("pgmname")!=null)
		// pgmfoot = "Program: " + context.get("pgmname") + " " + datestr;
		// else
		// pgmfoot = "Generated on " + datestr;
		StyledText sfoot_l = info.getSfoot_l(laf);
		StyledText sfoot_m = info.getSfoot_m(laf);
		StyledText sfoot_r = info.getSfoot_r(laf);
		String border = RtfRowFormat.BORDER_NONE;
		if (sfoot_m.isEmpty() && sfoot_r.isEmpty()) {
			if (sfoot_l.isEmpty()){
				//nothing to do
			} else {
				RtfRowFormat rf = new RtfRowFormat();
				int[] colwidths = new int[] { convUtil.convert2Twips(reportSetup
						.getTableWidth()) };
				rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
						RtfRowFormat.ROW_ALIGN_TOP, border, false);
				writer.beginRow(rf);
				RtfParagraphFormat pformat = new RtfParagraphFormat();
				pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, convUtil
						.convert2Twips(extraTopPadding)
						+ convUtil.convert2Twips(laf.getSystemFooterPadding()), 0,
						convUtil.convert2Twips(laf
								.getParagraphLeading("systemFootnote")), convUtil
								.convert2Twips(laf.getIndentSize()), convUtil
								.convertFont2FFInstructions(laf
										.getFont("systemFootnote")));
				if (barAbove)
					pformat.setBarAbove();
				formatTextAsCell(writer, sfoot_l, laf, reportSetup,
						"systemFootnote", pformat, 0);
				writer.endRow(false);
			}
		} else if (sfoot_m.isEmpty()) {
			RtfRowFormat rf = new RtfRowFormat();
			// int[] colwidths = new
			// int[]{convUtil.convert2Twips(absoluteWidths[0] +
			// absoluteWidths[1]), convUtil.convert2Twips(absoluteWidths[2])};
			int[] colwidths = convUtil.convert2Twips(new float[] {
					absoluteWidths[0] + absoluteWidths[1], absoluteWidths[2] },
					0);
			rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
					RtfRowFormat.ROW_ALIGN_TOP, border, false);
			writer.beginRow(rf);
			RtfParagraphFormat pformat = new RtfParagraphFormat();
			pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, convUtil
					.convert2Twips(extraTopPadding)
					+ convUtil.convert2Twips(laf.getSystemFooterPadding()), 0,
					convUtil.convert2Twips(laf
							.getParagraphLeading("systemFootnote")), convUtil
							.convert2Twips(laf.getIndentSize()), convUtil
							.convertFont2FFInstructions(laf
									.getFont("systemFootnote")));
			if (barAbove)
				pformat.setBarAbove();
			formatTextAsCell(writer, sfoot_l, laf, reportSetup,
					"systemFootnote", pformat, absoluteWidths[0]
							+ absoluteWidths[1]);
			pformat.setAlign(RtfParagraphFormat.ALIGN_RIGHT);
			formatTextAsCell(writer, sfoot_r, laf, reportSetup,
					"systemFootnote", pformat, absoluteWidths[2]);
			writer.endRow(false);
		}
		// else if(StringUtil.isEmpty(sfoot_r)){
		// RtfRowFormat rf = new RtfRowFormat();
		// int[] colwidths = convUtil.convert2Twips(new
		// float[]{absoluteWidths[0] , absoluteWidths[2]+ absoluteWidths[1]});
		// rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
		// RtfRowFormat.ROW_ALIGN_TOP, border);
		// writer.beginRow(rf);
		// RtfParagraphFormat pformat = new RtfParagraphFormat();
		// pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0,
		// convUtil.convert2Twips(extraTopPadding) +
		// convUtil.convert2Twips(laf.getSystemFooterPadding()), 0,
		// convUtil.convert2Twips(laf.getParagraphLeading("systemFootnote")),
		// convUtil.convert2Twips(laf.getIndentSize()),
		// convUtil.convertFont2FFInstructions(laf.getFont("systemFootnote")));
		// if(barAbove) pformat.setBarAbove();
		// formatTextAsCell(writer, sfoot_l, laf, reportSetup, "systemFootnote",
		// pformat, absoluteWidths[0]);
		// pformat.setAlign(RtfParagraphFormat.ALIGN_CENTER);
		// formatTextAsCell(writer, sfoot_m, laf, reportSetup, "systemFootnote",
		// pformat, absoluteWidths[2]+ absoluteWidths[1]);
		// writer.endRow(false);
		// }
		else {
			RtfRowFormat rf = new RtfRowFormat();
			// int[] colwidths = new
			// int[]{convUtil.convert2Twips(absoluteWidths[0]),
			// convUtil.convert2Twips(absoluteWidths[2]+ absoluteWidths[1])};
			int[] colwidths = convUtil.convert2Twips(new float[] {
					absoluteWidths[0], absoluteWidths[1], absoluteWidths[2] },
					0);
			rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
					RtfRowFormat.ROW_ALIGN_TOP, border, false);
			writer.beginRow(rf);
			RtfParagraphFormat pformat = new RtfParagraphFormat();
			pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, convUtil
					.convert2Twips(extraTopPadding)
					+ convUtil.convert2Twips(laf.getSystemFooterPadding()), 0,
					convUtil.convert2Twips(laf
							.getParagraphLeading("systemFootnote")), convUtil
							.convert2Twips(laf.getIndentSize()), convUtil
							.convertFont2FFInstructions(laf
									.getFont("systemFootnote")));
			if (barAbove)
				pformat.setBarAbove();
			formatTextAsCell(writer, sfoot_l, laf, reportSetup,
					"systemFootnote", pformat, absoluteWidths[0]);
			pformat.setAlign(RtfParagraphFormat.ALIGN_CENTER);
			formatTextAsCell(writer, sfoot_m, laf, reportSetup,
					"systemFootnote", pformat, absoluteWidths[1]);
			pformat.setAlign(RtfParagraphFormat.ALIGN_RIGHT);
			formatTextAsCell(writer, sfoot_r, laf, reportSetup,
					"systemFootnote", pformat, absoluteWidths[2]);
			writer.endRow(false);
		}
	}

	public void printSystemHeaders(RtfWriter writer, ReportLaF laf,
			RcdInfo info, ReportSetup reportSetup) throws Exception {
		float[] percentSystemWitdhs = laf.getSystemHeaderColumnPercentWidths();
		int sftable_nocolumns = percentSystemWitdhs.length;

		float[] absoluteWidths = new float[percentSystemWitdhs.length];
		float runningTotal = 0;
		for (int i = 0; i < absoluteWidths.length; i++) {
			if (i < absoluteWidths.length - 1) {
				absoluteWidths[i] = percentSystemWitdhs[i]
						* reportSetup.getTableWidth();
				runningTotal = runningTotal + absoluteWidths[i];
			} else {
				absoluteWidths[i] = reportSetup.getTableWidth() - runningTotal;
			}
		}

		// String pgmfoot = null;
		// if(context!=null && context.get("pgmname")!=null)
		// pgmfoot = "Program: " + context.get("pgmname") + " " + datestr;
		// else
		// pgmfoot = "Generated on " + datestr;
		StyledText shead_l = info.getShead_l(laf);
		StyledText shead_m = info.getShead_m(laf);
		StyledText shead_r = info.getShead_r(laf);
		String border = RtfRowFormat.BORDER_NONE;
		boolean repeatable = laf.isSystemHeaderPartOfTable();
		if (shead_m.isEmpty() && shead_r.isEmpty()) {
			RtfRowFormat rf = new RtfRowFormat();
			int[] colwidths = new int[] { convUtil.convert2Twips(reportSetup
					.getTableWidth()) };
			rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
					RtfRowFormat.ROW_ALIGN_TOP, border, repeatable);
			writer.beginRow(rf);
			RtfParagraphFormat pformat = new RtfParagraphFormat();
			pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, convUtil
					.convert2Twips(laf.getSystemHeaderPadding()), convUtil
					.convert2Twips(laf.getParagraphLeading("systemHeader")),
					convUtil.convert2Twips(laf.getIndentSize()), convUtil
							.convertFont2FFInstructions(laf
									.getFont("systemHeader")));
			formatTextAsCell(writer, shead_l, laf, reportSetup, "systemHeader",
					pformat, 0);
			writer.endRow(false);
		} else if (shead_m.isEmpty()) {
			RtfRowFormat rf = new RtfRowFormat();
			// int[] colwidths = new
			// int[]{convUtil.convert2Twips(absoluteWidths[0] +
			// absoluteWidths[1]), convUtil.convert2Twips(absoluteWidths[2])};
			int[] colwidths = convUtil.convert2Twips(new float[] {
					absoluteWidths[0] + absoluteWidths[1], absoluteWidths[2] },
					0);
			rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
					RtfRowFormat.ROW_ALIGN_TOP, border, repeatable);
			writer.beginRow(rf);
			RtfParagraphFormat pformat = new RtfParagraphFormat();
			pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, convUtil
					.convert2Twips(laf.getSystemHeaderPadding()), convUtil
					.convert2Twips(laf.getParagraphLeading("systemHeader")),
					convUtil.convert2Twips(laf.getIndentSize()), convUtil
							.convertFont2FFInstructions(laf
									.getFont("systemHeader")));
			formatTextAsCell(writer, shead_l, laf, reportSetup, "systemHeader",
					pformat, absoluteWidths[0] + absoluteWidths[1]);
			pformat.setAlign(RtfParagraphFormat.ALIGN_RIGHT);
			formatTextAsCell(writer, shead_r, laf, reportSetup, "systemHeader",
					pformat, absoluteWidths[2]);
			writer.endRow(false);
		}
		// else if(StringUtil.isEmpty(shead_r)){
		// RtfRowFormat rf = new RtfRowFormat();
		// int[] colwidths = convUtil.convert2Twips(new float[]
		// {absoluteWidths[0], absoluteWidths[1] + absoluteWidths[2]});
		// rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
		// RtfRowFormat.ROW_ALIGN_TOP, border);
		// writer.beginRow(rf);
		// RtfParagraphFormat pformat = new RtfParagraphFormat();
		// pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0,
		// convUtil.convert2Twips(laf.getSystemHeaderPadding()),
		// convUtil.convert2Twips(laf.getParagraphLeading("systemHeader")),
		// convUtil.convert2Twips(laf.getIndentSize()),
		// convUtil.convertFont2FFInstructions(laf.getFont("systemHeader")));
		// formatTextAsCell(writer, shead_l, laf, reportSetup, "systemHeader",
		// pformat, absoluteWidths[0]);
		// pformat.setAlign(RtfParagraphFormat.ALIGN_CENTER);
		// formatTextAsCell(writer, shead_m, laf, reportSetup, "systemHeader",
		// pformat, absoluteWidths[1] + absoluteWidths[2]);
		// writer.endRow(false);
		// }
		else {
			RtfRowFormat rf = new RtfRowFormat();
			// int[] colwidths = new
			// int[]{convUtil.convert2Twips(absoluteWidths[0]),
			// convUtil.convert2Twips(absoluteWidths[2]+ absoluteWidths[1])};
			int[] colwidths = convUtil.convert2Twips(new float[] {
					absoluteWidths[0], absoluteWidths[1], absoluteWidths[2] },
					0);
			rf.configure(colwidths, RtfRowFormat.TABLE_ALIGN_CENTER,
					RtfRowFormat.ROW_ALIGN_TOP, border, repeatable);
			writer.beginRow(rf);
			RtfParagraphFormat pformat = new RtfParagraphFormat();
			pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, convUtil
					.convert2Twips(laf.getSystemHeaderPadding()), convUtil
					.convert2Twips(laf.getParagraphLeading("systemHeader")),
					convUtil.convert2Twips(laf.getIndentSize()), convUtil
							.convertFont2FFInstructions(laf
									.getFont("systemHeader")));
			formatTextAsCell(writer, shead_l, laf, reportSetup, "systemHeader",
					pformat, absoluteWidths[0]);
			pformat.setAlign(RtfParagraphFormat.ALIGN_CENTER);
			formatTextAsCell(writer, shead_m, laf, reportSetup, "systemHeader",
					pformat, absoluteWidths[1]);
			pformat.setAlign(RtfParagraphFormat.ALIGN_RIGHT);
			formatTextAsCell(writer, shead_r, laf, reportSetup, "systemHeader",
					pformat, absoluteWidths[2]);
			writer.endRow(false);
		}

	}

	public void printBackgroundImage(RtfWriter writer, ReportLaF laf,
			ReportSetup reportSetup, File backgroundImage) throws Exception {
		String name = backgroundImage.getName().toLowerCase();
		if (name.endsWith("rtf") || name.endsWith(".txt")) {
			printBackgroundImage_FromRtf(writer, laf, reportSetup,
					backgroundImage);
		} else {
			printBackgroundImage_FromImage(writer, laf, reportSetup,
					backgroundImage);
		}
	}

	private void printBackgroundImage_FromRtf(RtfWriter writer, ReportLaF laf,
			ReportSetup reportSetup, File backgroundImage) throws Exception {
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(backgroundImage);
			br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				writer.addDirectyRtfSyntaxLine(line);
				line = br.readLine();
			}
		} finally {
			if (fr != null)
				fr.close();
			if (br != null)
				br.close();
		}
	}

	private void printBackgroundImage_FromImage(RtfWriter writer,
			ReportLaF laf, ReportSetup reportSetup, File backgroundImage)
			throws Exception {
		Image img = ImageUtil.getSelf().getImage(backgroundImage, reportSetup);

		float xpos = img.getAbsoluteX() - reportSetup.getLeftMargin(); // adjust
																		// back
																		// marginins
		float ypos = img.getAbsoluteY() - reportSetup.getBottomMargin();
		float width = img.getScaledWidth();
		float height = img.getScaledHeight();
		String instr = "{{{\\shp{\\*\\shpinst\\shpleft"
				+ convUtil.convert2Twips(xpos)
				+ "\\shptop"
				+ convUtil.convert2Twips(ypos)
				+ "\\shpright"
				+ convUtil.convert2Twips(xpos + width)
				+ "\\shpbottom"
				+ convUtil.convert2Twips(ypos + height)
				+ "\\shpfhdr0\\shpbxcolumn\\shpbxignore\\shpbypara\\shpbyignore\\shpwr3\\shpwrk0\\shpfblwtxt1\\shpz0\\shplid2049{\\sp{\\sn shapeType}{\\sv 75}}{\\sp{\\sn fFlipH}"
				+ "{\\sv 0}}{\\sp{\\sn fFlipV}{\\sv 0}}{\\sp{\\sn pib}{\\sv "
				+ "{\\pict\\picwgoal" + convUtil.convert2Twips(width)
				+ "\\pichgoal" + convUtil.convert2Twips(width);
		writer.addDirectyRtfSyntaxLine(instr);

		RtfDocument doc = new RtfDocument();
		doc.getDocumentSettings().setImageWrittenAsBinary(false);
		RtfImage2 rtfImage = new RtfImage2(doc, img);

		PrintStream fos = writer.startDirectContentMode();
		rtfImage.writeContent(fos);
		writer.stopDirectContentMode();

		writer.addDirectyRtfSyntaxLine("}}}}}}}");
	}

}
