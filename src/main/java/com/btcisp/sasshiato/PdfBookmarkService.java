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
import java.util.StringTokenizer;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Chunk;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfWriter;

//TODO usage of this class is not ready for multithreaded execution of several documents
public class PdfBookmarkService {

	public void handleBookmarks(PdfWriter writer, RcdInfo info,
			ReportSetup setup, ReportLaF laf) throws InvalidStylingSyntaxException {
		if (writer.getCurrentPageNumber() < 1) {
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,
					"Cannot Bookmark Document has no pages");
			return;
		}
		PdfContentByte content = writer.getDirectContent();
		PdfOutline root = content.getRootOutline();
		PdfOutline parent = root;
		final PdfDestination DESTINATION = new PdfDestination(PdfDestination.FIT);
				//new PdfDestination(PdfDestination.XYZ, 0f, 0f, 1f);
		PdfAction action = PdfAction.gotoLocalPage(1, DESTINATION, writer);
		String bookmarks = setup.getBookmarksPdf();
		Map levels = parseBookmarkLevels(bookmarks);
		int max_level = getMaxLevel(levels, setup);

		if (max_level > 0) {
			String[] titles = new String[max_level];
			int used_max_level = -1;
			for (int i = 1; i <= max_level; i++) {
				String level = "bk" + i;
				String what = (String) levels.get(level);
				String title = "";
				if (what != null) {
					title = getTitle(what, info, setup, laf);
				}
				if(!StringUtil.isEmpty(title)) {
					titles[i-1] =title;
					used_max_level = i;
				}
			}
			
			for (int i = 1; i <= used_max_level; i++) {
				String title = "";
				if (titles[i-1] != null) {
					title = titles[i-1];
				}
				Paragraph p = null;
				try {
					p = PdfPrinterUtil.getSelf().formatText_singleLine(
							StyledText.withConfiguredStyle(sanitizeTitle(title, laf), "title", laf), laf, "title");
				} catch (Exception e) {
					SasshiatoTrace.logError(
							"Pdf Bookmarking error in rendering title: "
									+ title, e);
					p = new Paragraph(sanitizeTitle(title, laf));
				}
				PdfOutline outline = new PdfOutline(parent, action, p);
				parent = outline;
			}
		}
	}

	private Map parseBookmarkLevels(String bookmarks) {
		Map result = new HashMap();
		StringTokenizer st = new StringTokenizer(bookmarks, ",");
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(word, "=");
			if (st2.countTokens() != 2) {
				SasshiatoTrace.logError("__bookmarks missconfigured: " + word);
			}
			String key = st2.nextToken().trim();
			String value = st2.nextToken().trim();
			result.put(key, value);
		}
		return result;
	}

	private int getMaxLevel(Map levels, ReportSetup setup) {
		if (levels == null)
			return -1;
		for (int i = 7; i > 0; i--) {
			if (levels.containsKey("bk" + i))
				return i;
		}
		SasshiatoTrace.logError("__bookmarks missconfigured: "
				+ setup.getBookmarksPdf());
		return -1;
	}

	private String sanitizeTitle(String title, ReportLaF laf) {
		title = StringUtil
				.replace(title, laf.getDoubleEscapeCharString(), "\n");
		String ESCAPE = laf.getEscapeCharString();
		sanitizeTitle(title, ESCAPE + "t");
		sanitizeTitle(title, ESCAPE + "i");
		return title;
	}

	private String sanitizeTitle(String title, String specialChar) {

		while (title.contains(specialChar)) {
			int inx = title.indexOf(specialChar);
			int inx2 = title.indexOf(" ", inx);
			title = title.substring(0, inx) + title.substring(inx2);
		}
		return title;
	}


	private String getTitle(String what, RcdInfo info, ReportSetup setup, ReportLaF laf) throws InvalidStylingSyntaxException{
		what = what.toLowerCase();
		// range situation
		StringTokenizer st = new StringTokenizer(what, "-");
		Map titleMap = new HashMap();
		StyledText[] titles = info.getTitles(laf);
		for (int i = 0; i < titles.length; i++) {
			titleMap.put("__title" + (i+1), StringUtil.nonEmpty(titles[i].getText()));
		}

		if (st.countTokens() == 2) {
			String start = st.nextToken();
			String end = st.nextToken();
			int starti = extractTitleNumber(start);
			int endi = extractTitleNumber(end);
			if (starti == -1 || endi == -1 || starti > endi) {
				SasshiatoTrace.logError("__bookmarks missconfigured: " + what);
				return "ERROR";
			}
			String result = "";
			for (int i = starti; i <= endi; i++) {
				String key = "__title" + i;
				if (i < endi)
					result = result + titleMap.get(key) + "\n";
				else
					result = result + titleMap.get(key);
			}
			return result;
		} else if (st.countTokens() == 1) {
			String result = (String) titleMap.get("__" + what);
			if (result == null) {
				SasshiatoTrace.logError("__bookmarks missconfigured: " + what);
				return "ERROR";
			}
			return result;
		} else {
			SasshiatoTrace.logError("__bookmarks missconfigured: " + what);
			return "ERROR";
		}
	}

	private int extractTitleNumber(String what) {
		if (!what.startsWith("title"))
			return -1;
		String num = what.substring("title".length());
		return Integer.parseInt(num);
	}
}
