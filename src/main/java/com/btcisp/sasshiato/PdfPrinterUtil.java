/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class PdfPrinterUtil {
	static PdfPrinterUtil self = new PdfPrinterUtil();

	public static PdfPrinterUtil getSelf() {
		return self;
	}

	private SplitCharacter_Std splitCharFunction = null;

	public PdfPTable printTiltesRows(StyledText[] titles, RcdInfo info,
			ReportSetup reportSetup, ReportLaF laf,
			float tableWidth, Map context) throws Exception {
		
		if (info.isTitleStyle_withTab()) {
			return printTiltesRows_withTab(titles, info,
			reportSetup, laf, tableWidth, context);
		} else {
			return printTiltesRows_STD(titles, info,
					reportSetup, laf, tableWidth, context);
		}
	}
	
	private PdfPTable printTiltesRows_STD(StyledText[] titles, RcdInfo info,
			ReportSetup reportSetup, ReportLaF laf,
			float tableWidth, Map context) throws Exception {

		if (titles == null)
			titles = new StyledText[0];
		int align = PdfPTable.ALIGN_CENTER;
		if ("L".equals(laf.getTitleAlign())) {
			align = PdfPTable.ALIGN_LEFT;
		} else if ("R".equals(laf.getTitleAlign())) {
			align = PdfPTable.ALIGN_RIGHT;
		} else if ("C".equals(laf.getTitleAlign())) {
			align = PdfPTable.ALIGN_CENTER;
		} else {
			throw new Exception("Invalid alignment requested:"
					+ laf.getTitleAlign());
		}
		PdfPTable ttable = new PdfPTable(1);
		ttable.setTotalWidth(tableWidth);
		ttable.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
		ttable.setLockedWidth(true);
		PdfPCell cell;

		for (int j = 0; j < titles.length; j++) {
			StyledText ts = (StyledText) titles[j];
			// String ts = StringUtil.replace(titles[j], "//",
			// "\n");
			cell = PdfPrinterUtil.getSelf().formatTextAsCell(ts, laf, "title",
					0f, align);
			// cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			if (j < titles.length - 1) {
				cell.setPaddingBottom(0f);
			} else {
				cell.setPaddingBottom(0f);
				// cell.setPaddingBottom(laf.getAfterTitlePadding()); //done
				// when printing headers
			}
			if (align == PdfPTable.ALIGN_LEFT) {
				cell.setPaddingLeft(0f);
			} else if (align == PdfPTable.ALIGN_RIGHT) {
				cell.setPaddingRight(0f);
			}
			cell.setPaddingTop(0f);
			cell.setBorder(PdfPCell.NO_BORDER);
			ttable.addCell(cell);
		}
		return ttable;
	}

	
	public PdfPTable printTiltesRows_withTab(StyledText[] titles, RcdInfo info,
			ReportSetup reportSetup, ReportLaF laf,
			float tableWidth, Map context) throws Exception {

		PdfPTable tttable = new PdfPTable(2);
		// calc column widths
		float maxwidth = RrgPrinterUtil.getSelf().maxSpecialFooterTitleSize(titles, laf, ReportLaF.STYLE_TYPE_TITLE);
		float firstColSize = maxwidth + laf.getTabularTitleSeparation() + 4; //4 added for minimum separaration and safety (parentese problem is 2 should be added.
		tttable.setTotalWidth(new float[] {firstColSize, reportSetup.getTableWidth() - firstColSize});
		tttable.setWidthPercentage(100f);
		StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(laf);
		StyledTextSizingUtil sizingUtil = StyledTextSizingUtil.self;
		for (int j = 0; j < titles.length; j++) {
			StyledText ts = (StyledText) titles[j];
			if (!ts.isEmpty()) {
				//String ts = StringUtil.replace(footnotes[j],
				//		"//", "\n");
				String tsTxt = ts.getText();
				int idxftl = tsTxt.indexOf(RcdConstants.LEFT_TITLE_TAB);
				int idxftr = tsTxt.indexOf(RcdConstants.RIGHT_TITLE_TAB);
				if(idxftl !=-1 || idxftr!=-1){
					int align = idxftl !=-1 ? Element.ALIGN_LEFT: Element.ALIGN_RIGHT;
					String delim = idxftl !=-1 ? RcdConstants.LEFT_TITLE_TAB: RcdConstants.RIGHT_TITLE_TAB;
					int inx = idxftl !=-1 ? idxftl: idxftr;
					Pair<StyledText, StyledText> split = StyledTextUtil.splitAtPureTextPosition(ts, inx, false);
					StyledText footL= split.left;
					StyledText footR = split.right;
					footR = footR.substring(delim.length());
					footR = footR.trim();
					if(align==Element.ALIGN_LEFT) footL=footL.rightTrim();
					else if(align==Element.ALIGN_RIGHT) footL = footL.leftTrim();

					float indent =0;
					if(align==Element.ALIGN_RIGHT){
						float size = sizingUtil.calcLineSize(footL, ReportLaF.STYLE_TYPE_TITLE, laf);//laf.getBaseFont("footnote").getWidthPoint(footL, laf.getFontSize("footnote"));
						indent = maxwidth - size;
					}
					//System.out.println("printing ["+footL + "], indent=" + indent);
					PdfPCell celll = PdfPrinterUtil.getSelf().formatTextAsCell(footL, laf, ReportLaF.STYLE_TYPE_TITLE, indent, Element.ALIGN_LEFT);
					
					celll.setBorder(PdfPCell.NO_BORDER);
					celll.setPaddingTop(0f);
					celll.setPaddingLeft(0f); //text had conservative 4 points added so padding will be visible
					celll.setPaddingRight(0f);
					tttable.addCell(celll);
					PdfPCell cellr = PdfPrinterUtil.getSelf().formatTextAsCell(footR, laf, ReportLaF.STYLE_TYPE_TITLE, 0, Element.ALIGN_LEFT);
					cellr.setBorder(PdfPCell.NO_BORDER);
					cellr.setPaddingTop(0f);
					cellr.setPaddingRight(0f); //text in celll had conservative 4 points added so padding will be visible
					cellr.setPaddingLeft(0f);
					tttable.addCell(cellr);			
				} else {
					PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(ts, laf, ReportLaF.STYLE_TYPE_TITLE, 0, Element.ALIGN_LEFT);
					cell.setBorder(PdfPCell.NO_BORDER);
					cell.setColspan(2);
					if (j == 0) {
						if(laf.isSeparatorAboveFootnotes()) cell.setBorder(PdfPCell.TOP);
						cell.setPaddingTop(laf.getBeforeFootnotePadding());
					} else  {
						cell.setPaddingTop(0f);
					}
					cell.setPaddingLeft(0f);
					cell.setPaddingRight(0f);
					tttable.addCell(cell);
					//System.out.println("After FootnoteC:" + ts+ ",size=" + ftable.getTotalHeight());
				}
			}
		}
		return tttable;
	}
	
	public void printHeaderRows(HeaderTablet tablet, PdfPTable table,
			PageGroup pageGroup, ReportLaF raf, ReportSetup setup) throws Exception {
		Font font = raf.getBodyFont();

		boolean hasRegularHeaders = raf.printEmptyHeaders() || !RrgPrinterUtil.getSelf().isEmptyHeader(tablet);
		float titlepadding = raf.getAfterTitlePadding()
				+ raf.getPdfPaddingAdjustement();
		if(!hasRegularHeaders && setup.isRtfOut()) titlepadding = titlepadding + 4; //adjust for RTF compatibility. RTF prints thin row with very small font
		// System.out.println("got " + titlepadding);
		StyledText vbgheanding = tablet.getCurrentVarByGroupHeading();
		if (tablet.requiresVarByGroupHeading()
				&& vbgheanding != null && !vbgheanding.isEmpty()) {
			PdfPCell varbycell = formatTextAsCell(tablet
					.getCurrentVarByGroupHeading(), raf, "title", 0,
					Element.ALIGN_LEFT);
			varbycell.setBorder(PdfPCell.BOTTOM);
			int totalColSpan = pageGroup.getAllColumns().length;
			varbycell.setColspan(totalColSpan);
			varbycell.setPaddingBottom(raf.getHeaderBottomPadding()
					+ raf.getPdfPaddingAdjustement());
			varbycell.setPaddingTop(titlepadding);
			varbycell.setPaddingLeft(0f);
			table.addCell(varbycell);
		} else {
			// add empty cell with title padding;
			PdfPCell topcell = new PdfPCell();
			// topcell.setPadding(laf.getAfterBodyPadding());
			topcell.setFixedHeight(titlepadding);
			topcell.setBorder(PdfPCell.BOTTOM);
			int totalColSpan = pageGroup.getAllColumns().length;
			topcell.setColspan(totalColSpan);
			table.addCell(topcell);
		}

		HeaderRow[] rows = tablet.getHeaderRows();

		if (hasRegularHeaders) {
			for (int i = 0; i < rows.length; i++) {
				HeaderRow hr = (HeaderRow) rows[i];
				int[] starts = hr.getStartColumns();
				int[] ends = hr.getEndColumns();
				StyledText[] cells = hr.getHeaderCells();
				int[] borders = hr.getBorderInstructions();
//				for (int j = 0; j < cells.length; j++) {
//					System.out.println("got " + cells[j] + borders[j]);
//				}
				int[] aligns = hr.getAlignmentsInt();
				ArrayList bottomBorders = new ArrayList();

				for (int j = 0; j < cells.length; j++) {
					boolean lastCellInRow = (j == cells.length -1);
					// int colspan = ends[j] - starts[j] + 1;
					boolean hasSeparationAdjR = raf
							.hasColumnSeparationAdjustmentRight(starts[j]);
					boolean hasSeparationAdjL = raf
							.hasColumnSeparationAdjustmentLeft(ends[j]);
					float separationAdjR = raf
							.getColumnSeparationAdjustmentRight(ends[j]);
					float separationAdjL = raf
							.getColumnSeparationAdjustmentLeft(starts[j]);
					float columnSeparation = raf.getColumnSeparation();
					String debugInfo = "Columns from " + starts[j] + " to "
							+ ends[j];
					// System.out.println("TTT:" + debugInfo + ","+
					// separationAdjL + "," + separationAdjR);

					int colspan = pageGroup.calculateColumnSpan(starts[j],
							ends[j]);
					float totalwith = pageGroup.calculateTotalWidth(starts[j],
							ends[j]);
					if (colspan < 0 || totalwith < 0)
						throw new Exception(
								"ERROR: Unexpected Error, please please file issue with https://github.com/ipeszek/sasshiato, colspan="
										+ colspan + ", totalwidth=" + totalwith);
					StyledText celltext = cells[j];
					PdfPCell cell = null;
					int leftRightBorder = borders[j];
					int singleVerticalBorder = leftRightBorder;
					if(!lastCellInRow)
						singleVerticalBorder = singleVerticalBorder & Rectangle.LEFT; //to not duplicate left-right borders print only left borders
					if (!celltext.isEmpty()) {
						int alignment = aligns[j];
						// if(colspan > 1) alignment = Element.ALIGN_CENTER;
						// //RP verify with Iza

						int pageColId = pageGroup
								.map2OnPageColumnNumber(starts[j]);
						if (alignment == Element.ALIGN_LEFT) {
							if (pageColId > 0)
								cell = formatTextAsCell(celltext, raf,
										"header", columnSeparation
												+ separationAdjL, alignment);
							else {
								cell = formatTextAsCell(celltext, raf,
										"header", 0, alignment);
								cell.setPaddingLeft(0f);
							}
							cell.setPaddingRight(0f);
							// if(hasSeparationAdjR &&
							// !pageGroup.isLastColumnOnPage(pageColId)){
							// cell.setPaddingRight(columnSeparation+
							// separationAdjR);
							// }
						} else {
							cell = formatTextAsCell(celltext, raf, "header", 0,
									alignment);

							if (alignment == Element.ALIGN_RIGHT) {
								cell.setPaddingLeft(0);

								if (pageGroup.isLastColumnOnPage(pageColId)) {
									cell.setPaddingRight(0f);
								} else {
									if (hasSeparationAdjR) {
										cell.setPaddingRight(columnSeparation
												+ separationAdjR); // keep the
																	// default
																	// padding
																	// of 2
									} else {
										if (raf.getColumnSeparation() > 0)
											cell.setPaddingRight(raf
													.getColumnSeparation() + 2); // keep
																					// the
																					// default
																					// padding
																					// of 2
									}
								}
								// if(alignment ==Element.ALIGN_RIGHT &&
								// hasSeparationAdjR &&
								// !pageGroup.isLastColumnOnPage(pageColId)){
								// cell.setPaddingRight(columnSeparation+
								// separationAdjR);
								// System.out.println("header: right padding " +
								// cell.getPaddingRight() + "columnSeparation="
								// + columnSeparation + ",separationAdjR=" +
								// separationAdjR);
								// }
							}
						}
						if (i == rows.length - 1) {
							// border cell done on top
							// if (i==0)
							// cell.setBorder(PdfPCell.BOTTOM + PdfPCell.TOP);
							// else
							cell.setBorder(PdfPCell.BOTTOM | singleVerticalBorder);
						} else {
							// border cell done on top
							// if (i==0)
							// cell.setBorder(PdfPCell.TOP);
							// else
							cell.setBorder(singleVerticalBorder);
							// create partial bottom-border cells
							// float percentage = 95f;
							// if(alignment != Element.ALIGN_CENTER) percentage
							// = 97f;
							PdfPCell bottom_border = formatPartialBorderCell(
									colspan, totalwith, alignment,
									separationAdjL, separationAdjR, leftRightBorder, singleVerticalBorder, raf,
									debugInfo);
							bottomBorders.add(bottom_border);
						}
						cell.setPaddingBottom(raf.getHeaderBottomPadding()
								+ raf.getPdfPaddingAdjustement());
					} else {
						cell = new PdfPCell();
						cell.addElement(formatEmptyParagraph(font));
						if (i == rows.length - 1) {
							cell.setBorder(PdfPCell.BOTTOM | singleVerticalBorder);
						} else {
							cell.setBorder(singleVerticalBorder);
						}

						if (i < rows.length - 1) {
							// adds empty cell to bottom border cells.
							PdfPCell bcell = new PdfPCell();
							bcell.setColspan(colspan);
							bcell.setBorder(singleVerticalBorder);
							bcell.setPadding(0f);
							bottomBorders.add(bcell);
						}
					}
					// common logic
					cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
					cell.setColspan(colspan);
					cell.setPaddingTop(0f); // added to apply consistend
											// vertical padding
					if (i < rows.length - 1)
						cell.setPaddingBottom(0f); // added to apply consistend
													// vertical padding
					else if (cell.getPaddingBottom() < 2f)
						cell.setPaddingBottom(2f); // last row logic

					// System.out.println("cell " + i + "/" + j + " bottom " +
					// cell.getPaddingBottom());

					table.addCell(cell);
				}
				// bottom boarders are underlines which do not touch.
				for (int j = 0; j < bottomBorders.size(); j++) {
					PdfPCell cell = (PdfPCell) bottomBorders.get(j);
					table.addCell(cell);
				}
			}
		} else {
			
		}

	}


	//leftRightBorder tells this method what borders are used, useVerticalBorder tells it which border to print
	private PdfPCell formatPartialBorderCell(int rowspan, float width,
			int align, float separationAdjL, float separationAdjR, int leftRightBorder, int useVerticalBorder,
			ReportLaF laf, String debugInfo) throws Exception {
		
		boolean hasRightBorder = (leftRightBorder & Rectangle.RIGHT) > 0;
		boolean hasLeftBorder = (leftRightBorder & Rectangle.LEFT) > 0;
		boolean noReduction = hasRightBorder && hasLeftBorder;
		if(hasRightBorder || hasLeftBorder){
			if(noReduction)
				align = Element.ALIGN_CENTER;
			else if(hasRightBorder)
				align = Element.ALIGN_RIGHT;
			else if(hasLeftBorder)
				align = Element.ALIGN_LEFT;
		}
		if(hasRightBorder){
			separationAdjR = 0f;
		}
		if(hasLeftBorder){
			separationAdjL = 0f;
		}
		boolean useSeparationForUnderline = laf
				.headerUnderline_useSeparationAdjustement();
		float defaultSpacing = laf.getHeaderUnderlineSpacing();
		// System.out.println("Underline: " + debugInfo + "," +
		// useSeparationForUnderline + "," + separationAdjL + ","+
		// separationAdjR);
		if (useSeparationForUnderline && separationAdjL <= 0
				&& separationAdjR <= 0)
			useSeparationForUnderline = false;
		if (useSeparationForUnderline
				&& width <= Math.max(separationAdjL, defaultSpacing)
						+ Math.max(separationAdjR, defaultSpacing)) {
			SasshiatoTrace
					.log(
							SasshiatoTrace.LEV_REQUIRED,
							debugInfo
									+ ": header underlining in pdf cannot fit using distance to next, col_width="
									+ width + ", separationAdjR="
									+ separationAdjR + ",separationAdjL="
									+ separationAdjL
									+ ", default underlining will be used");
			useSeparationForUnderline = false;
		}
		if (!useSeparationForUnderline) {
			PdfPCell bcell = new PdfPCell();
			bcell.setColspan(rowspan);
			bcell.setBorder(useVerticalBorder);
			bcell.setPadding(0f);
			bcell.setHorizontalAlignment(align);
			// cell3.setFixedHeight(1f);
			// cell3.setBorder(PdfPCell.BOTTOM);
			PdfPTable nested1 = new PdfPTable(1);
			// nested1.setWidthPercentage(percentage);
			float widthReduction = 2 * defaultSpacing;
			if (Element.ALIGN_CENTER != align)
				widthReduction = defaultSpacing;
			if(noReduction)
				widthReduction = 0f;
			float newWidth = width - widthReduction;
			if (newWidth <= 0)
				newWidth = width / 2;
			nested1.setTotalWidth(newWidth);
			nested1.setLockedWidth(true);
			PdfPCell celln1 = new PdfPCell();
			celln1.setFixedHeight(0f);
			celln1.setBorder(PdfPCell.BOTTOM);
			nested1.addCell(celln1);
			bcell.addElement(nested1);
			return bcell;
		} else {
			if (!hasLeftBorder && separationAdjL < defaultSpacing)
				separationAdjL = defaultSpacing;
			if (!hasRightBorder && separationAdjR < defaultSpacing)
				separationAdjR = defaultSpacing;
			PdfPCell bcell = new PdfPCell();
			bcell.setColspan(rowspan);
			bcell.setBorder(leftRightBorder);
			bcell.setPadding(0f);
			bcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			// cell3.setFixedHeight(1f);
			// cell3.setBorder(PdfPCell.BOTTOM);
			PdfPTable nested1 = new PdfPTable(3);
			// nested1.setWidthPercentage(percentage);
			float widthReduction = separationAdjL + separationAdjR;
			float newWidth = width - widthReduction;
			nested1.setTotalWidth(new float[] { separationAdjL, newWidth,
					separationAdjR });
			// System.out.println("Underline: " + debugInfo + "," +
			// separationAdjL + ","+ newWidth + "," + separationAdjR);
			nested1.setLockedWidth(true);

			PdfPCell celln0 = new PdfPCell();
			celln0.setFixedHeight(0f);
			celln0.setBorder(PdfPCell.NO_BORDER);
			nested1.addCell(celln0);

			PdfPCell celln1 = new PdfPCell();
			celln1.setFixedHeight(0f);
			celln1.setBorder(PdfPCell.BOTTOM);
			celln1.setHorizontalAlignment(align);
			nested1.addCell(celln1);

			PdfPCell celln2 = new PdfPCell();
			celln2.setFixedHeight(0f);
			celln2.setBorder(PdfPCell.NO_BORDER);
			nested1.addCell(celln2);

			bcell.addElement(nested1);
			return bcell;
		}
	}

	public void printHeaderRows_OLD(HeaderTablet tablet, PdfPTable table,
			ReportLaF raf) throws Exception {
		Font font = raf.getBodyFont();
		HeaderRow[] rows = tablet.getHeaderRows();
		for (int i = 0; i < rows.length; i++) {
			HeaderRow hr = (HeaderRow) rows[i];
			int[] starts = hr.getStartColumns();
			int[] ends = hr.getEndColumns();
			StyledText[] cells = hr.getHeaderCells();
			int[] aligns = hr.getAlignmentsInt();

			for (int j = 0; j < cells.length; j++) {
				int colspan = ends[j] - starts[j] + 1;
				StyledText celltext = cells[j];
				PdfPCell cell = null;
				if (!celltext.isEmpty()) {
					int alignment = aligns[j];
					if (colspan > 1)
						alignment = Element.ALIGN_CENTER; // RP verify with
															// Iza
					cell = formatTextAsCell(celltext, raf, "header", 0,
							alignment);
					if (i == 0)
						cell.setBorder(PdfPCell.BOTTOM + PdfPCell.TOP);
					else {
						cell.setBorder(PdfPCell.BOTTOM);

					}
					cell.setPaddingBottom(raf.getHeaderBottomPadding());
				} else {
					cell = new PdfPCell();
					cell.addElement(formatEmptyParagraph(font));
					if (i == 0)
						cell.setBorder(PdfPCell.TOP);
					else
						cell.setBorder(PdfPCell.NO_BORDER);
				}
				cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
				cell.setColspan(colspan);
				table.addCell(cell);
			}
		}

	}

	public PdfPCell printCenteredCell(StyledText text, ReportLaF raf,
			String styletype) throws Exception {
		PdfPCell cell;
		cell = formatTextAsCell(text, raf, styletype, 0, Element.ALIGN_CENTER);
		// cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(PdfPCell.NO_BORDER);
		return cell;

	}

	public PdfPCell printLeftAlignedCell(float indent, StyledText text,
			ReportLaF raf, String styletype) throws Exception {
		PdfPCell cell;
		cell = formatTextAsCell(text, raf, styletype, indent,
				Element.ALIGN_LEFT);
		// cell.setIndent(indent);
		// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setBorder(PdfPCell.NO_BORDER);
		return cell;

	}

	public PdfPCell printRightAlignedCell(StyledText text, ReportLaF raf,
			String styletype) throws Exception {
		PdfPCell cell;
		cell = formatTextAsCell(text, raf, styletype, 0f, Element.ALIGN_RIGHT);
		// cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setBorder(PdfPCell.NO_BORDER);
		// if(raf.getColumnSeparation() > 0 )
		// cell.setPaddingRight(raf.getColumnSeparation() + 2); //keep the
		// default padding of 2
		return cell;

	}

	private PdfPCell printDecimalAlignedCell(StyledText text, float w, Font font,
			float fontsize, ReportLaF raf, float totSize, String debugInfo)
			throws Exception {
		
		StyledTextSizingUtil sizingUtil = StyledTextSizingUtil.self;
		StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(raf);
		String styletype = ReportLaF.STYLE_TYPE_BODY;
		
		// text = StringUtil.replace(text, "//", "\n");
		text = text.trim();// StringUtil.leftTrim(text);
		
		PdfPCell cell;
		float currentWidthB4Dec, currentWidth;
		// String textB4dot;
		currentWidth = sizingUtil.calcLineSize(text, styletype, raf);
		// int dotat = text.indexOf(".");
		currentWidthB4Dec = currentWidth;
		if (!text.isEmpty()) {
			Pair<StyledText, StyledText> split2 = styledTextUtil.split(text, ".,)%: ", false);
			StyledText s0 = split2.left;
			currentWidthB4Dec =  sizingUtil.calcLineSize(s0, styletype, raf) ;
		}
		/*
		 * int dotat = s0.length();
		 * 
		 * if (dotat == -1) { currentWidthB4Dec = currentWidth; } else {
		 * textB4dot = text.substring(0, dotat); currentWidthB4Dec =
		 * bf.getWidthPoint(textB4dot, fontsize); }
		 */
		float indentD = w - currentWidthB4Dec;

		if (indentD + currentWidth + 1 > totSize) {
			SasshiatoTrace
					.logWarning(" POSSIBLE DATA TRUNCATION IN PDF "
							+ debugInfo
							+ ", Txt=["
							+ text
							+ "] may wrap or be indented to far indent="
							+ indentD
							+ ", txtWidth="
							+ currentWidth
							+ ", colSize="
							+ totSize
							+ ", calcWidthB4Dot="
							+ w
							+ " this problem can be caused by use of column spacing < 2 points");
			cell = formatTextAsCell(text, raf, "body", 0, Element.ALIGN_RIGHT);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setPadding(0f);
			cell.setBorder(PdfPCell.NO_BORDER);
			return cell;
		}

		cell = formatTextAsCell(text, raf, "body", 0, Element.ALIGN_LEFT);
		// p.setIndentationLeft(indentD);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		// cell.setIndent(indentD);
		cell.setPaddingLeft(indentD);
		cell.setPaddingRight(0);

		/*
		 * cell = new PdfPCell(new Paragraph(text, raf.getBodyFont()));
		 * cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		 * cell.setPaddingLeft(indentD);
		 */
		/*
		 * cell = new PdfPCell(); cell.addElement(new Paragraph(text,
		 * raf.getBodyFont())); cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		 * cell.setPaddingLeft(indentD);
		 */
		cell.setBorder(PdfPCell.NO_BORDER);
		return cell;
	}

	public void printBodyRows(RcdRow row, PageGroup pageGroup, PdfPTable table,
			ReportLaF raf, TableWidths widths) throws Exception {

		StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(raf);
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
		float[] offsets = raf.getDecimalOffsets();
		float[] maxWidthB4Dec = widths.getMaxWidthsB4Decimal();
		float[] maxWidthB4Dec1 = widths.getMaxWidthsB4Decimal1();
		float[] maxWidthB4Dec2 = widths.getMaxWidthsB4Decimal2();
		float[] maxWidthsAfterDecimal = widths.getMaxWidthsAfterDecimal();
		// float[] actualWidth_notused = widths.getActualWidths();
		float[] actualWidth1 = widths.getActualWidths1();
		float[] actualWidth2 = widths.getActualWidths2();
		float[] extra_num_padding = widths.getExtraWidths();
		Font font = raf.getBodyFont();
		int[] colids = pageGroup.getColumnIDs();
		PageColumn[] cols = pageGroup.getAllColumns();
		float[] colWiths = pageGroup.getColumnWidths();
		// float[] colWidths = pageGroup.getColumnWidths();
		float indent = row.getIndentlev() * raf.getIndentSize();
		String suffix = row.getSuffix();
		String[] aligns = row.getAllAligns();
        String styleType = ReportLaF.STYLE_TYPE_BODY;
        
		for (int i = 0; i < colids.length; i++) {
			if (i > 0) {
				indent = 0;
				if (columnSpanWholeRow)
					break;
			}
			int j = colids[i];
			// System.out.println("got " + j + " and " + offsets.length);
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

			PageColumn column = cols[i];
			float colWidth = colWiths[i];
			StyledText celltext = row.getColumAsStyledText(j, styleType, raf);
			PdfPCell cell = null;


			String cellAlignment = "C";
			if (aligns.length > j)
				cellAlignment = aligns[j];
			if (celltext == null) {
				cell = formatTextAsCell(celltext, raf, "body");
				cell.setBorder(row.getColumnBorderInstructions(j));
			} else {
				if ("C".equals(cellAlignment))
					cell = printCenteredCell(celltext, raf, "body");
				else if ("R".equals(cellAlignment)) {
					cell = printRightAlignedCell(celltext, raf, "body");
					if (i == colids.length - 1) {
						cell.setPaddingRight(0f);
					} else {
						if (hasSeparationAdjR) {
							cell.setPaddingRight(columnSeparation
									+ separationAdjR); // keep the default
														// padding of 2
							// System.out.println("body: right padding " +
							// cell.getPaddingRight() + "columnSeparation=" +
							// columnSeparation + ",separationAdjR=" +
							// separationAdjR);
						} else {
							if (raf.getColumnSeparation() > 0)
								cell
										.setPaddingRight(raf
												.getColumnSeparation() + 2); // keep
																				// the
																				// default
																				// padding
																				// of 2
						}
					}
					// if(i>0 && hasSeparationAdjL){
					// cell.setPaddingLeft(0f);
					// }
					cell.setPaddingLeft(0f);

				} else if ("L".equals(cellAlignment)) {
					if (i > 0)
						cell = printLeftAlignedCell(columnSeparation
								+ separationAdjL + indent, celltext, raf,
								"body");
					else {
						cell = printLeftAlignedCell(indent, celltext, raf,
								"body");
					}
					// if(i<colids.length -1 && hasSeparationAdjR){
					// cell.setPaddingRight(columnSeparation + separationAdjR);
					// }
					cell.setPaddingRight(0f);
				} else if ("RD".equals(cellAlignment)) {
					float exta_padding = column.getNumericColumnPadding();
					float extraWidth = extra_num_padding[j];
					// the first cell uses 2 points from full allowence for
					// separation
					// todo move out for performance
					if (offset > 0) {
						if (offset
								+ columnSeparation
								+ separationAdjL
								+ Math
										.min(columnSeparation + separationAdjR,
												2) + extraWidth + exta_padding
								+ actualWidth1[j] + actualWidth2[j] > colWidth) {
							float maxallowed = column.getColumnWidth()
									- (columnSeparation
											+ separationAdjL
											+ Math.min(columnSeparation
													+ separationAdjR, 2)
											+ extraWidth + exta_padding
											+ actualWidth1[j] + actualWidth2[j]);
							SasshiatoTrace
									.logWarning("offset "
											+ offset
											+ "pt for column "
											+ j
											+ " moves RD aligned text outside cell boundary and is ingnored, this report allows max of "
											+ maxallowed
											+ "pt for RD-aligned cells (this maybe data dependent)");// +
																										// actualWidth1[j]
																										// +
																										// ","+
																										// actualWidth2[j]);
							offset = 0;
							offsets[j] = 0;
						}
					} else if (offset < 0) {
						if (offset + columnSeparation + separationAdjL
								+ extraWidth + exta_padding < 0) {
							float maxallowed = columnSeparation
									+ separationAdjL + extraWidth
									+ exta_padding;
							SasshiatoTrace
									.logWarning("offset "
											+ offset
											+ "pt for column "
											+ j
											+ " moves RD aligned text outside cell boundary and is ingnored, this report allows min of "
											+ (-maxallowed)
											+ "pt for RD-aligned cells (this maybe data dependent)");
							offset = 0;
							offsets[j] = 0;
						}
					}
					float[] mv = {
							offset
									+ columnSeparation
									+ separationAdjL
									+ Math.min(columnSeparation
											+ separationAdjR, 2) + extraWidth
									+ exta_padding + actualWidth1[j],
							actualWidth2[j] };
					// float[] mv = { columnSeparation +separationAdjL +
					// extraWidth + exta_padding + actualWidth1[j],
					// actualWidth2[j] };

					// if(!StringUtil.isEmpty(celltext) &&
					// celltext.indexOf("1.732") != -1) {
					// System.out.println(j + " (" + celltext + ") RD"+
					// (columnSeparation + extraWidth + exta_padding +
					// actualWidth1[j] +", " + actualWidth2[j] + ",xx=" +
					// extraWidth+ ", tot_col=" + column.getColumnWidth()));
					// }


					Pair<StyledText, StyledText> split = styledTextUtil.split(celltext, " ", false);
					StyledText celltext1 = split.left.appendToLastChunk(" ", "body", raf);
					StyledText celltext2 = split.right.leftTrim();
   
					PdfPCell cell1 = printDecimalAlignedCell(celltext1, offset
							+ columnSeparation + separationAdjL + extraWidth
							+ exta_padding + maxWidthB4Dec1[j], font, raf
							.getFontSize("body"), raf, mv[0], "RD-formatted "
							+ column + " ,R-cell (w=" + mv[0] + "), rowid="
							+ row.getRowid());
					PdfPCell cell2 = printDecimalAlignedCell(celltext2,
							maxWidthB4Dec2[j], font, raf.getFontSize("body"),
							raf, mv[1], "RD-formatted " + column
									+ " ,D-cell (w=" + mv[1] + "), rowid="
									+ row.getRowid());
					PdfPTable smalltable = new PdfPTable(mv);
					smalltable.setTotalWidth(mv[0] + mv[1]);
					// System.out.println(j + " RD tot width=" + (mv[0] +
					// mv[1]));
					// float[] twidths = table.getAbsoluteWidths();
					// System.out.println(j + "tot col widht=" + twidths[j]);
					// smalltable.setTotalWidth(colWidths[i]);
					smalltable.setLockedWidth(true);
					// smalltable.setWidthPercentage(100f);
					smalltable.addCell(cell1);
					smalltable.addCell(cell2);
					smalltable.setHorizontalAlignment(Element.ALIGN_LEFT);
					/*
					 * try { doc.add(smalltable); } catch (Exception ex) {
					 * ex.printStackTrace(); }
					 */
					cell = new PdfPCell();
					cell.addElement(smalltable);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.setPadding(0f);
					// if((j==2) && !StringUtil.isEmpty(celltext)) {
					// System.out.println(j + " (" + celltext + ") RD"+
					// (columnSeparation + maxWidthB4Dec1[j]+ extraWidth +
					// exta_padding) +"," + maxWidthB4Dec2[j] + "," + mv[0] +
					// ","+ mv[1]);
					// }

				} else if ("D".equals(cellAlignment)) {
					float extraWidth = extra_num_padding[j];
					float exta_padding = column.getNumericColumnPadding();
					// System.out.println("colsize=" + column.getColumnWidth() +
					// " stretch padding=" + exta_padding);
					// extraWidth=(colWidths[i]-actualWidth[j])/2;
					if (offset > 0) {
						if (offset
								+ columnSeparation
								+ separationAdjL
								+ Math
										.min(columnSeparation + separationAdjR,
												2) + extraWidth + exta_padding
								+ maxWidthB4Dec[j] + maxWidthsAfterDecimal[j] > column
								.getColumnWidth()) {
							float maxallowed = column.getColumnWidth()
									- (columnSeparation
											+ separationAdjL
											+ Math.min(columnSeparation
													+ separationAdjR, 2)
											+ extraWidth + exta_padding
											+ maxWidthB4Dec[j] + maxWidthsAfterDecimal[j]);
							SasshiatoTrace
									.logWarning("offset "
											+ offset
											+ " for column "
											+ j
											+ " moves D-aligned text outside cell boundary and is ingnored, this report allows max of "
											+ maxallowed
											+ "pt for D-aligned cells (this maybe data dependent)");
							offset = 0;
							offsets[j] = 0;
						}
					} else if (offset < 0) {
						if (offset + columnSeparation + separationAdjL
								+ extraWidth + exta_padding < 0) {
							float maxallowed = columnSeparation
									+ separationAdjL + extraWidth
									+ exta_padding;
							SasshiatoTrace
									.logWarning("offset "
											+ offset
											+ "pt for column "
											+ j
											+ " moves D-aligned text outside cell boundary and is ingnored, this report allows min of "
											+ (-maxallowed)
											+ "pt for D-aligned cells (this maybe data dependent)");
							offset = 0;
							offsets[j] = 0;
						}
					}

					float width_2_decimal = offset + columnSeparation
							+ separationAdjL + maxWidthB4Dec[j] + extraWidth
							+ exta_padding;
					// total_padding = 0;
					cell = printDecimalAlignedCell(celltext, width_2_decimal,
							font, raf.getFontSize("body"), raf, column
									.getColumnWidth(), "D-formatted " + column
									+ ", rowid=" + row.getRowid());
					
					
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
					cell = printCenteredCell(celltext, raf, "body");
				}
				cell.setBorder(row.getColumnBorderInstructions(j));
			}
			if (columnSpanWholeRow){
				cell.setColspan(colids.length);
				//table borders?  TODO
			}
			table.addCell(cell);
		}
		if ("~-2n".equals(suffix)
				|| raf.getDoubleEscapeCharString().equals(suffix)) {
			PdfPCell emptycell = new PdfPCell(formatEmptyParagraph(font));
			emptycell.setColspan(colids.length);
			emptycell.setBorder(PdfPCell.NO_BORDER);
			emptycell.setPaddingBottom(raf.getEmptyLinePDFBottomPadding());

			emptycell.setPaddingTop(0f);
			table.addCell(emptycell);
			//table borders?  TODO
		}

	}

	
	
	
	public Paragraph[] formatText(StyledText txt, ReportLaF laf, String styletype)
			throws Exception {

		return formatText(txt, laf, styletype, 0, -99);
	}

	/**
	 * this method returns array of Paragraphs, some maybe for blank string.
	 * Current version of ITEXT will not print them (no new lines)
	 * 
	 */
	public Paragraph[] formatText(StyledText txt, ReportLaF laf, String styletype,
			float indent, int align) throws Exception {

		// if(txt==null) txt= "";
		// txt = StringUtil.replace(txt, "//", "/t0");
		// txt = StringUtil.replace(txt, "\n", "/t0");
		// txt = StringUtil.replace(txt, "\r", "");
		// String[] ss = RCDStringUtls.getSelf().tokenize(txt,
		// "/t");
		

		List<StyledText> lines = StyledTextUtil.getInstance(laf).groupIntoLines(txt);
		if (lines.size() > 0) {
			if (align != Element.ALIGN_LEFT) {
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
		
		Paragraph[] result = new Paragraph[lines.size()];

		float indentSize = laf.getIndentSize();
		for (int k = 0; k < lines.size(); k++) {
			// note first token will never be after /t so should not be
			// indented, also first token can be empty and
			// then it should be skipped.
			StyledText s = lines.get(k);
			float indentD = s.getIndent();

			result[k] = formatText_singleLine(s, laf, styletype);
			if (align != -99)
				result[k].setAlignment(align);
			if (indent + (indentD * indentSize) > 0)
				result[k].setIndentationLeft(indent + (indentD * indentSize));
		}

		return result;
	}

	public Paragraph formatText_singleLine(StyledText txt, ReportLaF laf,
			String styletype) throws Exception {
		if (splitCharFunction == null) {
			splitCharFunction = new SplitCharacter_Std(laf);
		}

		String plainText = txt.getText();
		
		if (plainText.indexOf("_PAGE_") != -1) {
			SasshiatoTrace
					.logError("The _PAGE_ keyword is supported only in system header, it is ignored for "
							+ styletype + " in the text: " + txt);
		}
		if (plainText.indexOf("_DATE_") != -1) {
			txt = txt.replace("_DATE_", RrgPrinterUtil.getSelf()
					.getDateReplacement(laf));
		}
		if (plainText.indexOf("\\\\") != -1) {
			txt = txt.replace("\\\\", "\\");
		}
		
		Paragraph result = new Paragraph();
		StyledChunk[] chunks = txt.getChunks();
		for (int i = 0; i < chunks.length; i++) {
			StyledChunk styledChunk = chunks[i];
			String chunkText = styledChunk.getText();
			if("".equals(chunkText) && styledChunk.getIndentInstruction() != null){
				//new line encountered
				chunkText = " ";
			}
			String styleOverride = styledChunk.getEncodedNoIndentingStyles();
			Font f = laf.getFont(styletype, styleOverride);
			
			int ssidx_start = chunkText.toLowerCase().indexOf("~{super");
			result.setLeading(laf.getParagraphLeading(styletype));
			while (ssidx_start != -1) {
				// parse superscript text
				int ssidx_stop = chunkText.indexOf("}", ssidx_start);
				if (ssidx_stop == -1) {
					SasshiatoTrace
							.logError("Malformed superscript instruction found in line:"
									+ txt);
					formatPartialLine(chunkText, result, laf, f, styletype);
					return result;
				} else {
					String txt_before = chunkText.substring(0, ssidx_start);
					String txt_after = chunkText.substring(ssidx_stop + 1);
					String superscript = chunkText.substring(ssidx_start
							+ "~{super".length(), ssidx_stop);
					superscript = superscript.trim();
					formatPartialLine(txt_before, result, laf, f, styletype);
					Chunk superscript_chunk = new Chunk(" " + superscript, laf
							.getSuperscriptFont(styletype));
					float superscriptRaise = laf.getSuperscriptRaise(styletype);
					superscript_chunk.setTextRise(superscriptRaise);
					result.add(superscript_chunk);
					chunkText = txt_after;
				}
				ssidx_start = chunkText.toLowerCase().indexOf("~{super");
			}

			if (chunkText.length() > 0) {
				formatPartialLine(chunkText, result, laf, f, styletype);
			}
			
		}
		
		return result;
	}

	private void formatPartialLine(String txt, Paragraph paragraph,
			ReportLaF laf, Font f, String styletype) throws Exception {
		// replace /# with corresponding characters
		txt = RrgPrinterUtil.getSelf().replaceSpecialChars(txt, laf);

		String ESCAPE = laf.getEscapeCharString();
		String prefix = ESCAPE + "s#";
		if (txt.indexOf(prefix) == -1) {
			Chunk ch = new Chunk(txt, f);
			ch.setSplitCharacter(this.splitCharFunction);
			paragraph.add(ch);
			return;
		}

		int ssidx_start = txt.indexOf(prefix);
		while (ssidx_start != -1) {
			// parse superscript text
			int ssidx_stop = txt.indexOf(" ", ssidx_start);
			if (ssidx_stop == -1) {
				// automatic trimming may impact this calculation
				ssidx_stop = txt.length();
			}
			if (ssidx_stop == -1) {
				SasshiatoTrace.logError("Malformed " + prefix
						+ " instruction found in line:" + txt);
				Chunk ch = new Chunk(txt, f);
				ch.setSplitCharacter(this.splitCharFunction);
				paragraph.add(ch);
				return;
			} else {
				String txt_before = txt.substring(0, ssidx_start);
				String txt_after = "";
				if (ssidx_stop + 1 < txt.length())
					txt_after = txt.substring(ssidx_stop + 1);
				String scommand = txt.substring(ssidx_start + prefix.length(),
						ssidx_stop);
				scommand = scommand.trim();
				int it = 0;
				try {
					it = Integer.parseInt(scommand);
				} catch (NumberFormatException e) {
					it = RrgPrinterUtil.getSelf().convertSymbolHintToAsciiCode(
							scommand);
				}

				Chunk text_before_chunk = new Chunk(txt_before, f);
				text_before_chunk.setSplitCharacter(this.splitCharFunction);
				paragraph.add(text_before_chunk);
				Chunk special_chunk = new Chunk("" + (char) it, laf
						.getSymbolFont(styletype));
				paragraph.add(special_chunk);
				txt = txt_after;
			}
			ssidx_start = txt.indexOf(prefix);
		}

		if (txt.length() > 0) {
			Chunk final_ch = new Chunk(txt, f);
			final_ch.setSplitCharacter(this.splitCharFunction);
			paragraph.add(final_ch);
		}
	}

	public PdfPCell formatTextAsCell(StyledText txt, ReportLaF laf,
			String styletype, float indent, int align) throws Exception {
		
		Paragraph[] p = formatText(txt, laf, styletype, indent, align);
		PdfPCell result = new PdfPCell();
		for (int i = 0; i < p.length; i++) {
			result.addElement(p[i]);
		}
		result.setPaddingBottom(0f);
		result.setPaddingTop(0f);
		return result;
	}

	public PdfPCell formatTextAsCell(StyledText txt, ReportLaF laf, String styletype)
			throws Exception {
		return formatTextAsCell(txt, laf, styletype, 0, -99);
	}

	// for some reason attempt to print " " in a cell makes it higher
	public PdfPCell formatEmptyParagraphAsCell2(ReportLaF laf, String styletype)
			throws Exception {
		PdfPCell result = new PdfPCell();
		result.setPadding(0f);
		return result;
	}

	public PdfPCell formatEmptyParagraphAsCell(ReportLaF laf, String styletype)
			throws Exception {
		PdfPCell result = new PdfPCell();
		result.addElement(formatEmptyParagraph(laf.getFont(styletype)));
		return result;
	}

	public Paragraph formatEmptyParagraph(Font font) {
		return new Paragraph(" ", font);
	}

	public PdfPTable printSystemFootnotes(ReportLaF laf, RcdInfo info,
			ReportSetup reportSetup) throws Exception {

		float[] percentSystemWitdhs = laf
				.getSystemFootnoteColumnPercentWidths();
		int sftable_nocolumns = percentSystemWitdhs.length;
		PdfPTable sftable = new PdfPTable(sftable_nocolumns);

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
		sftable.setTotalWidth(absoluteWidths);
		sftable.setWidthPercentage(100f);

		// String pgmfoot = null;
		// if(context!=null && context.get("pgmname")!=null)
		// pgmfoot = "Program: " + context.get("pgmname") + " " + datestr;
		// else
		// pgmfoot = "Generated on " + datestr;
		StyledText sfoot_l = info.getSfoot_l(laf);
		StyledText sfoot_m = info.getSfoot_m(laf);
		StyledText sfoot_r = info.getSfoot_r(laf);
		if (sfoot_m.isEmpty() && sfoot_r.isEmpty()) {
			if(sfoot_l.isEmpty()){
				PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(PdfPCell.NO_BORDER);
                emptyCell.setFixedHeight(0);
			} else {
				PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(
						sfoot_l, laf, "systemFootnote", 0, Element.ALIGN_LEFT);
				cell_l.setColspan(3);
				cell_l.setBorder(PdfPCell.NO_BORDER);
				cell_l.setPaddingTop(laf.getSystemFooterPadding());
				cell_l.setPaddingLeft(0f);
				cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
				sftable.addCell(cell_l);
			}
		} else if (sfoot_m.isEmpty()) {
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(
					sfoot_l, laf, "systemFootnote", 0, Element.ALIGN_LEFT);
			cell_l.setColspan(2);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingLeft(0f);
			cell_l.setPaddingTop(laf.getSystemFooterPadding());
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			sftable.addCell(cell_l);

			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(sfoot_r,
					laf, "systemFootnote", 0, Element.ALIGN_RIGHT);
			// cell.setBorder(PdfPCell.TOP);
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			cell.setPaddingTop(laf.getSystemFooterPadding());
			cell.setPaddingRight(0f);
			sftable.addCell(cell);
		}
		// else if(StringUtil.isEmpty(sfoot_r)){
		// PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(sfoot_l,
		// laf, "systemFootnote", 0, Element.ALIGN_LEFT);
		// cell_l.setColspan(1);
		// cell_l.setBorder(PdfPCell.NO_BORDER);
		// cell_l.setPaddingTop(laf.getSystemFooterPadding());
		// cell_l.setPaddingLeft(0f);
		// cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
		// sftable.addCell(cell_l);
		//
		// PdfPCell cell_m = PdfPrinterUtil.getSelf().formatTextAsCell(sfoot_m,
		// laf, "systemFootnote", 0, Element.ALIGN_CENTER);
		// cell_m.setBorder(PdfPCell.NO_BORDER);
		// cell_m.setColspan(2);
		// cell_m.setPaddingTop(laf.getSystemFooterPadding());
		// cell_m.setVerticalAlignment(PdfPCell.ALIGN_TOP);
		// sftable.addCell(cell_m);
		// }
		else {
			PdfPCell cell_l = PdfPrinterUtil.getSelf().formatTextAsCell(
					sfoot_l, laf, "systemFootnote", 0, Element.ALIGN_LEFT);
			cell_l.setColspan(1);
			cell_l.setBorder(PdfPCell.NO_BORDER);
			cell_l.setPaddingTop(laf.getSystemFooterPadding());
			cell_l.setPaddingLeft(0f);
			cell_l.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			sftable.addCell(cell_l);

			PdfPCell cell_m = PdfPrinterUtil.getSelf().formatTextAsCell(
					sfoot_m, laf, "systemFootnote", 0, Element.ALIGN_CENTER);
			cell_m.setBorder(PdfPCell.NO_BORDER);
			cell_m.setColspan(1);
			cell_m.setPaddingTop(laf.getSystemFooterPadding());
			cell_m.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			sftable.addCell(cell_m);

			PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(sfoot_r,
					laf, "systemFootnote", 0, Element.ALIGN_RIGHT);
			// cell.setBorder(PdfPCell.TOP);
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
			cell.setPaddingTop(laf.getSystemFooterPadding());
			cell.setPaddingRight(0f);
			sftable.addCell(cell);

		}

		return sftable;
	}
}
