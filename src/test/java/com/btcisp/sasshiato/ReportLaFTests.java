package com.btcisp.sasshiato;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.sasshiato.ReportLaF;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

public class ReportLaFTests {

	@Test
	public void testFonts() throws Exception {
		RcdInfo info = new RcdInfo();
		Properties overrides = new Properties();
		//overrides.setProperty("__font", "courier");
		overrides.setProperty("title_ff", "xit"); //use less than perfect syntax
		overrides.setProperty("foot_ff", "xbf");
		overrides.setProperty("sfoot_ff", "xitxbf");
		info.setTestOverrides(overrides);
		ReportLaF laf = new ReportLaF(info, 2);
		
		Font f = laf.getFont("body", "");
		Assert.assertEquals(f.getFamily(), Font.TIMES_ROMAN);
		Assert.assertTrue(f.getStyle()== Font.NORMAL || f.getStyle() == -1);
		
		BaseFont bf = laf.getBaseFont("body", "");
		String[][] fn = bf.getFullFontName();
		
		f = laf.getFont("title", "");
		Assert.assertEquals(f.getFamily(), Font.TIMES_ROMAN);
		Assert.assertEquals(f.getStyle(), Font.ITALIC);
		fn = bf.getFullFontName();
		
		f = laf.getFont("footnote", "");
		Assert.assertEquals(f.getFamily(), Font.TIMES_ROMAN);
		Assert.assertEquals(f.getStyle(), Font.BOLD);
		fn = bf.getFullFontName();
		
		f = laf.getFont("systemFootnote", "");
		Assert.assertEquals(f.getFamily(), Font.TIMES_ROMAN);
		Assert.assertEquals(f.getStyle(), Font.BOLDITALIC);
		
		//FF is fully defined by override, body is only used for size		
		f = laf.getFont("body", "it");
		Assert.assertEquals(f.getFamily(), Font.TIMES_ROMAN);
		Assert.assertEquals(f.getStyle(), Font.ITALIC);

		f = laf.getFont("body", "bf");
		Assert.assertEquals(f.getFamily(), Font.TIMES_ROMAN);
		Assert.assertEquals(f.getStyle(), Font.BOLD);

		f = laf.getFont("body", "bf_it");
		Assert.assertEquals(f.getFamily(), Font.TIMES_ROMAN);
		Assert.assertEquals(f.getStyle(), Font.BOLDITALIC);

	}
}
