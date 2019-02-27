package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.btcisp.sasshiato.Pair;
import com.btcisp.sasshiato.StyledChunk;
import com.btcisp.sasshiato.StyledText;
import com.btcisp.sasshiato.StyledTextUtil;
import com.btcisp.utils.StringUtil;

public class StyleTextUtilTests {
	
	@Test
	public void testBecauseItBroke1() throws Exception{
		String s= "EGFR/t0(mL/min/1.73 m~{super 2})";
		StyledTextUtil util = StyledTextUtil.getTestInstance("/");
		
		ArrayList<String> empty = new ArrayList<String>();
		ArrayList<String> bfOnly = new ArrayList<String>();
		
		String input = s;
		
		StyledText stext = util.parseToStyledText(empty, input, false);
        System.out.println( stext);
	}
	@Test
	public void testSanity0() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		ArrayList<String> empty = new ArrayList<String>();
		ArrayList<String> bfOnly = new ArrayList<String>();
		
		String input = "";
		
		StyledText stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(stext.isEmpty());

		stext = util.parseToStyledText(bfOnly, input, false);
		Assert.assertTrue(stext.isEmpty());
		
	}

	@Test
	public void testSanity1() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		ArrayList<String> empty = new ArrayList<String>();
		
		String input = "{\\it This is italic {\\bf Bold italic \\bf} only italic \\it}";
		
		StyledText stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "This is italic Bold italic only italic"));
		StyledChunk[] chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold italic"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__it__\\} only italic"));
		for(int i = 0; i< "This is italic ".length(); i++){
			Assert.assertEquals(stext.getChunkAt(i), chunks[0]);
		}
        for(int i = "This is italic ".length(); i< "This is italic Bold italic".length(); i++){
			Assert.assertEquals(stext.getChunkAt(i), chunks[1]);			
		}
        for(int i = "This is italic Bold italic".length(); i< "This is italic Bold italic only italic".length(); i++){
			Assert.assertEquals(stext.getChunkAt(i), chunks[2]);			
		}
		
		input = "{\\it This is italic{\\bf Bold italic \\bf}only italic \\it}";
		
		stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "This is italicBold italiconly italic"));
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__it__\\}This is italic"));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold italic"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__it__\\}only italic"));
		
		input = "Normal {\\it\\bf Bold italic \\bf\\it} again normal";
		
		stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "Normal Bold italic again normal"));
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "Normal "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold italic"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), " again normal"));
	}
	
	
	//TODO add \\tN or \\iN instructions
	@Test
	public void testSanity2() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		ArrayList<String> empty = new ArrayList<String>();
		ArrayList<String> bfOnly = new ArrayList<String>();
		bfOnly.add(StyledChunk.STYLE_INSTRUCTION_BF);
		
		String input = "{\\it This is italic {\\bf Bold \\t2 italic \\bf} only italic \\it}";
		
		StyledText stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "This is italic Bold  italic only italic"));
		StyledChunk[] chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 4);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold "));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__+t2;bf;it__\\} italic")); //t2 does not have trailing blank
		Assert.assertTrue(StringUtil.areEqual(chunks[3].toString(), "{\\__it__\\} only italic"));
		
		input = "\\i2{\\it This is italic {\\bf Bold \\t2 italic \\bf} only italic \\it}";
		
		stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "This is italic Bold  italic only italic"));
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 4);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__+i2;it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold "));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__+t2;bf;it__\\} italic")); //t2 does not have trailing blank
		Assert.assertTrue(StringUtil.areEqual(chunks[3].toString(), "{\\__it__\\} only italic"));

		input = "\\i2{\\it This is italic {\\bf Bold\\t2italic \\bf} only italic \\it}";
		
		stext = util.parseToStyledText(bfOnly, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "This is italic Bolditalic only italic"));
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 4);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__+i2;bf;it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__+t2;bf;it__\\}italic")); //t2 does not have trailing blank
		Assert.assertTrue(StringUtil.areEqual(chunks[3].toString(), "{\\__bf;it__\\} only italic"));

		input = "\\i2Line 1\\t2Line 2\\t3Line 3";
		
		stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "Line 1Line 2Line 3"));
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__+i2__\\}Line 1"));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__+t2__\\}Line 2"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__+t3__\\}Line 3")); //t2 does not have trailing blank

		input = "\\i2Line 1\\t2Line 2\\t3Line 3";
		
		stext = util.parseToStyledText(empty, input, false);
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__+i2__\\}Line 1"));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__+t2__\\}Line 2"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__+t3__\\}Line 3")); //t2 does not have trailing blank

		input = "\\i2Line 1\\t2Line 2\\t3Line 3";
		
		stext = util.parseToStyledText(bfOnly, input, false);
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__+i2;bf__\\}Line 1"));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__+t2;bf__\\}Line 2"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__+t3;bf__\\}Line 3")); //t2 does not have trailing blank

		
		input = "~{super b} /ftr Included the following drug classes (ATC Level 4): oOrganic nitrates; dDigitalis glycosides; oOther cardiac preparations; aAntiarrhythmics, Class III; oOther vasodilators used in cardiac diseases; aAdrenergic and dopaminergic agents;, and aAntiarrhythmics, Class IC.\\t0";
		
		stext = util.parseToStyledText(bfOnly, input, false);
		chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 2);
		Assert.assertTrue(chunks[1].getStyles().get(0).contains("t0"));

	}

	//this was not working
	@Test 
	public void testWhenStartsWithNewLine() throws Exception{
		ArrayList<String> empty = new ArrayList<String>();
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		
		String txt = "//Illustrates applying cutoff-limit for percentages, and Preferred Term sorted by decreasing frequency in selected column";
		StyledText stext = util.parseToStyledText(empty, txt, false);
		StyledChunk[] chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 1);
		Assert.assertTrue(chunks[0].getStyles().get(0).equals("+t0"));
		
		List<StyledText> lines = util.groupIntoLines(stext);
		Assert.assertTrue(lines.size() == 2);
	}

	@Test
	public void testExceptions() {
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		ArrayList<String> empty = new ArrayList<String>();
		
		String input = "{\\it This is italic {\\bf Bold italic \\it} only italic \\bf}";
		String msg = null;
		try{
			StyledText stext = util.parseToStyledText(empty, input, false);
		} catch(Exception e){
			System.out.println(e.getMessage());
			msg = e.getMessage();
		}
		Assert.assertTrue(msg != null);
		
		input = "Normal {\\it\\bf Bold italic \\ef\\it} again normal";
		msg = null;
		try{
			StyledText stext = util.parseToStyledText(empty, input, false);
		} catch(Exception e){
			System.out.println(e.getMessage());
			msg = e.getMessage();
		}
		Assert.assertTrue(msg != null);

	}
	
	
	@Test
	public void testSplit() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		ArrayList<String> empty = new ArrayList<String>();
		
		String input = "{\\it This is italic {\\bf Bold.italic \\bf} only)italic \\it}";
		StyledText stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "This is italic Bold.italic only)italic"));
		StyledChunk[] chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold.italic"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__it__\\} only)italic"));
		
		Pair<StyledText, StyledText> split = util.split(stext, ".,)%:", false);
		StyledText left = split.left;
		StyledText right = split.right;
		
		Assert.assertTrue(StringUtil.areEqual(left.getText(), "This is italic Bold"));
		chunks = left.getChunks();
		Assert.assertTrue( chunks.length == 2);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold"));
		
		Assert.assertTrue(StringUtil.areEqual(right.getText(), ".italic only)italic"));
		
		//degenerate case
		input = "";
		stext = util.parseToStyledText(empty, input, false);
		split = util.split(stext, ".,)%:", false);
		left = split.left;
		right = split.right;
		//Assert.assertTrue(left.getChunks().length == 1);
		//Assert.assertTrue(right.getChunks().length == 0);
		Assert.assertTrue(left.isEmpty());
		Assert.assertTrue(right.isEmpty());

		
	}
	
	
	@Test
	public void testSplit2() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		ArrayList<String> empty = new ArrayList<String>();
		
		String input = ",";
		StyledText stext = util.parseToStyledText(empty, input, false);
		
		Pair<StyledText, StyledText>  split = util.split(stext, ",", false);
		StyledText left = split.left;
		Assert.assertTrue(split.left.isEmpty());
		Assert.assertTrue(split.right.getText().equals(stext.getText()));
		
		
		input = ",x,";
		stext = util.parseToStyledText(empty, input, false);
		List<StyledText> splits = util.splitIntoList(stext, ",", false, true);
		Assert.assertTrue(splits.size() == 3);
		
	}
	
	@Test
	public void testSplitAtPurePosition() throws Exception{
		StyledTextUtil util = StyledTextUtil.getTestInstance();
		ArrayList<String> empty = new ArrayList<String>();
		
		String input = "{\\it This is italic {\\bf Bold.italic \\bf} only)italic \\it}";
		StyledText stext = util.parseToStyledText(empty, input, false);
		Assert.assertTrue(StringUtil.areEqual(stext.getText(), "This is italic Bold.italic only)italic"));
		StyledChunk[] chunks = stext.getChunks();
		Assert.assertTrue( chunks.length == 3);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold.italic"));
		Assert.assertTrue(StringUtil.areEqual(chunks[2].toString(), "{\\__it__\\} only)italic"));
		
		Pair<StyledText, StyledText> split = util.splitAtPureTextPosition(stext, stext.getText().indexOf("."), false);
		StyledText left = split.left;
		StyledText right = split.right;
		
		Assert.assertTrue(StringUtil.areEqual(left.getText(), "This is italic Bold"));
		chunks = left.getChunks();
		Assert.assertTrue( chunks.length == 2);
		Assert.assertTrue(StringUtil.areEqual(chunks[0].toString(), "{\\__it__\\}This is italic "));
		Assert.assertTrue(StringUtil.areEqual(chunks[1].toString(), "{\\__bf;it__\\}Bold"));
		
		Assert.assertTrue(StringUtil.areEqual(right.getText(), ".italic only)italic"));
		
		
	}
}
