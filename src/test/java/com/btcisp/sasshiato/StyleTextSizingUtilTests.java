package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.sasshiato.StyledTextSizingUtil;
import com.btcisp.sasshiato.StyledTextUtil;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Font;

public class StyleTextSizingUtilTests {
	@Test
	public void testSanity0() throws Exception{
		RcdInfo info = new RcdInfo();
		info.setTestOverrides(new Properties());
		ReportLaF laf = new ReportLaF(info, 2);
		StyledTextUtil util = StyledTextUtil.getTestInstance();
        StyledTextSizingUtil sutil = StyledTextSizingUtil.self;
        
        ArrayList<String> empty = new ArrayList<String>();
		
		String input = "test long {\\bf sentence _DATE_ /#22  \\bf} "; //TODO needs 2 blanks one for /#22 termination, the other for \\bf}
		
		StyledText stext = util.parseToStyledText(empty, input, false);
        float res = sutil.calcLineSize(stext, "body", laf);
        System.out.println( "test width " + res);
		
        input = "/#22"; //TODO needs 2 blanks one for /#22 termination, the other for \\bf}
		
		stext = util.parseToStyledText(empty, input, false);
        res = sutil.calcLineSize(stext, "body", laf);
        System.out.println( "test width " + res);
	}
	
	@Test
	public void testSanity() throws Exception{
		RcdInfo info = new RcdInfo();
		info.setTestOverrides(new Properties());
		ReportLaF laf = new ReportLaF(info, 2);
		Assert.assertEquals(laf.getBodyFont().getFamily(), Font.TIMES_ROMAN);
		StyledTextUtil util = StyledTextUtil.getTestInstance();
        StyledTextSizingUtil sutil = StyledTextSizingUtil.self;
        
        ArrayList<String> empty = new ArrayList<String>();
		
        String input01 = "test long 1 ";
        String input02 = "test long 2";
		String input0 = input01 + input02; //TODO needs 2 blanks one for /#22 termination, the other for \\bf}
		
		StyledText stext0 = util.parseToStyledText(empty, input0, false);
        float size0 = sutil.calcLineSize(stext0, "body", laf);

		StyledText stext01 = util.parseToStyledText(empty, input01, false);
        float size01 = sutil.calcLineSize(stext01, "body", laf);

		StyledText stext02 = util.parseToStyledText(empty, input02, false);
        float size02 = sutil.calcLineSize(stext02, "body", laf);

        Assert.assertTrue(Math.abs(size01 + size02 - size0) < 0.001f ); //there maybe float errors here
       
        String input11 = "{\\bf test long 1  \\bf}";
        String input12 = "{\\it test long 2 \\it}";
		String input1 = input11 + input12; //TODO needs 2 blanks one for /#22 termination, the other for \\bf}
		
		StyledText stext1 = util.parseToStyledText(empty, input1, false);
        float size1 = sutil.calcLineSize(stext1, "body", laf);

		StyledText stext11 = util.parseToStyledText(empty, input11, false);
        float size11 = sutil.calcLineSize(stext11, "body", laf);

		StyledText stext12 = util.parseToStyledText(empty, input12, false);
        float size12 = sutil.calcLineSize(stext12, "body", laf);

        Assert.assertTrue(Math.abs(size11 + size12 - size1) < 0.001f ); //there maybe float errors here
        
        Assert.assertTrue(Math.abs(size1 - size0) > 0.001f ); //TIMES_ROMAN is default font is not fix size
 	}

	
	@Test
	//same as sanity but with courier font
	public void testSanity1() throws Exception{
		RcdInfo info = new RcdInfo();
		//"courier"
		Properties overrides = new Properties();
		overrides.setProperty("__font", "courier");
		info.setTestOverrides(overrides);
		ReportLaF laf = new ReportLaF(info, 2);
		Assert.assertEquals(laf.getBodyFont().getFamily(), Font.COURIER);
		StyledTextUtil util = StyledTextUtil.getTestInstance();
        StyledTextSizingUtil sutil = StyledTextSizingUtil.self;
        
        ArrayList<String> empty = new ArrayList<String>();
		
        String input01 = "test long 1 ";
        String input02 = "test long 2";
		String input0 = input01 + input02; //TODO needs 2 blanks one for /#22 termination, the other for \\bf}
		
		StyledText stext0 = util.parseToStyledText(empty, input0, false);
        float size0 = sutil.calcLineSize(stext0, "body", laf);

		StyledText stext01 = util.parseToStyledText(empty, input01, false);
        float size01 = sutil.calcLineSize(stext01, "body", laf);

		StyledText stext02 = util.parseToStyledText(empty, input02, false);
        float size02 = sutil.calcLineSize(stext02, "body", laf);

        Assert.assertTrue(Math.abs(size01 + size02 - size0) < 0.001f ); //there maybe float errors here
        
        String input11 = "{\\bf test long 1  \\bf}";
        String input12 = "{\\it test long 2 \\it}";
		String input1 = input11 + input12; //TODO needs 2 blanks one for /#22 termination, the other for \\bf}
		
		StyledText stext1 = util.parseToStyledText(empty, input1, false);
        float size1 = sutil.calcLineSize(stext1, "body", laf);

		StyledText stext11 = util.parseToStyledText(empty, input11, false);
        float size11 = sutil.calcLineSize(stext11, "body", laf);

		StyledText stext12 = util.parseToStyledText(empty, input12, false);
        float size12 = sutil.calcLineSize(stext12, "body", laf);

        Assert.assertTrue(Math.abs(size11 + size12 - size1) < 0.001f ); //there maybe float errors here
         
        Assert.assertTrue(Math.abs(size1 - size0) < 0.001f ); //COURIER is fix size
 	}

	@Test
	public void testAdjustForRtfLineBreaks() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		StyledTextSizingUtil sutil = StyledTextSizingUtil.self;

		RcdInfo info = new RcdInfo();
		info.setTestOverrides(new Properties());
		ReportLaF laf = new ReportLaF(info, 2);
		String text = "one two three four five six seven";
		StyledText stext = StyledText.withConfiguredStyle(text, "body", laf);
		float size = sutil.calcLineSize(StyledText.withConfiguredStyle(" seven", "body", laf), "body", laf); //longest word size
		System.out.println( "test width " + sutil.calcCharacterSize(stext, laf));
		
		StyledText adjusted;
		adjusted = sutil.adjustForRtfLineBreaks(size , stext, "body", laf);
		Assert.assertTrue(StringUtil.areEqual(adjusted.getText(), "one two three four five six seven")); //there is nothing to split
		
		text = "one two three four five six se,ven";
		stext = StyledText.withConfiguredStyle(text, "body", laf);
		size = sutil.calcLineSize(StyledText.withConfiguredStyle(text, "body", laf), "body", laf); 
		adjusted = sutil.adjustForRtfLineBreaks(size -1, stext, "body", laf); 
		Assert.assertTrue(StringUtil.areEqual(adjusted.getText(), "one two three four five six se, ven"));
		
		text = "one two three four five six se,ven two three four five six se,ven";
		stext = StyledText.withConfiguredStyle(text, "body", laf);
		adjusted = sutil.adjustForRtfLineBreaks(size -1, stext, "body", laf); 
		Assert.assertTrue(StringUtil.areEqual(adjusted.getText(), "one two three four five six se, ven two three four five six se, ven"));

	}
	//TODO calcCharacterSize test

	@Test
	public void testCharSize() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		StyledTextSizingUtil sutil = StyledTextSizingUtil.self;

		RcdInfo info = new RcdInfo();
		info.setTestOverrides(new Properties());
		ReportLaF laf = new ReportLaF(info, 2);
		String text = "one two three four five six seven";
		StyledText stext = StyledText.withConfiguredStyle(text, "body", laf);
		int size = sutil.calcCharacterSize(stext, laf); //longest word size
		int length = text.length();
		Assert.assertEquals(size,length);
		
		text = "one {\bf two three four five \bf} six seven";
		stext = StyledText.withConfiguredStyle(text, "body", laf);
		size = sutil.calcCharacterSize(stext, laf); //longest word size
		length = text.length();
		Assert.assertEquals(size,length);
		

	}
}
