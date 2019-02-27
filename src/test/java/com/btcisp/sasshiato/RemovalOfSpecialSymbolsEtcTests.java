package com.btcisp.sasshiato;

import static org.junit.Assert.*;

import org.junit.Test;

import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.RrgPrinterUtil;

public class RemovalOfSpecialSymbolsEtcTests {

	@Test
	public void test() {
		String testS = "abc ~{super a} cde /s#le fgh /#13 /#35 ijk";
		RrgPrinterUtil util = RrgPrinterUtil.getSelf();
		String result = util.redoForWidthCalculation("/", testS);
		System.out.println(result);
		assertEquals("abc a cde Wfgh /#13 Wijk", result);
	}
	
	@Test
	public void test2() {
		String testS = "/#13 /#27 /#33 /#13 ";
		RrgPrinterUtil util = RrgPrinterUtil.getSelf();
		String result = util.replaceSpecialChars(testS, "/", null);
		System.out.println(result);
	
	}

}
