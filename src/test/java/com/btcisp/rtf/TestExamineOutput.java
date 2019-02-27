package com.btcisp.rtf;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class TestExamineOutput {

    @Test
	public void testUnderline() throws Exception{
		RtfDocFormat setup = new RtfDocFormat();
		setup.orient=RtfDocFormat.ORINET_L;
		setup.paperSize = RtfDocFormat.PAPER_SIZE_A4;
		FileOutputStream fos = new FileOutputStream("./test_outputs/test2.rtf");
		PrintStream ps  = new PrintStream(fos);
		RtfWriter w = new RtfWriter(ps);
		w.open(setup, false);
        RtfRowFormat rf = new RtfRowFormat();
        rf.configure(new int[]{2000,100,2000,100,2000,100,2000}, RtfRowFormat.TABLE_ALIGN_CENTER, RtfRowFormat.ROW_ALIGN_CENTER, null, false);
        rf.setBorders(new String[] {RtfRowFormat.BORDER_BOTTOM, "", RtfRowFormat.BORDER_BOTTOM, "", RtfRowFormat.BORDER_BOTTOM, "", RtfRowFormat.BORDER_BOTTOM});
        w.beginRow(rf);
        RtfParagraphFormat pf =new RtfParagraphFormat();
        pf.configure(RtfParagraphFormat.ALIGN_CENTER, 0, 0, 0,0, 0, 100, null);
        w.addCell(new String[]{"test1"}, new int[]{0}, pf);
        w.addCell(new String[]{""}, new int[]{0}, pf);
        w.addCell(new String[]{"test2"}, new int[]{0}, pf);
        w.addCell(new String[]{""}, new int[]{0}, pf);
        w.addCell(new String[]{"test3"}, new int[]{0}, pf);
        w.addCell(new String[]{""}, new int[]{0}, pf);
        w.addCell(new String[]{"test4"}, new int[]{0}, pf);
        w.endRow(false);
        w.close(null);
        ps.close();
        fos.close();

	}

}
