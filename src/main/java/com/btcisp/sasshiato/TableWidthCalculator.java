/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

public class TableWidthCalculator {
	private static final int CELL_PADDING_ADJUSTMENT =4; //0;//4; //these are important adjustment to apparent internal cell spacing in PDF
	private static final int ONE_SIDED_CELL_PADDING_AJDUSTEMENT=2; //0;//2; //these are important adjustment to apparent internal cell spacing in PDF
	boolean calcPreferredLegths;
	float fontsize = 0;
	Font font_tn;
	//BaseFont bf;
	ReportLaF laf;
	RrgPrinterUtil printerUtil = RrgPrinterUtil.getSelf();
	float[] preferredWidths = null;
	float[] maxWidths = null;
	//float[] maxWidthsB4Decimal = null;
	float[] maxWidthsAfterDecimal = null; //this one is calcuated including extra space required if D and RD are used together, (includes RD to the right).
	    
	float[] maxWidths1 = null;
	float[] maxWidthsB4Decimal1 = null; //is used for both D and R in RD calculations.
	float[] maxWidthsAfterDecimal1 = null;

	float[] maxWidths2 = null;
	float[] maxWidthsB4Decimal2 = null;
	float[] maxWidthsAfterDecimal2 = null;
	float[] paddingAdjustements = null;

	boolean calcCharacterSizes = false;
	int[] maxWidthsCh =null;
	int[] maxWidthsAfterDecimalCh = null;
	int[] maxWidths1Ch = null;
	int[] maxWidthsB4Decimal1Ch = null; //is used for both D and R in RD calculations.
	int[] maxWidthsAfterDecimal1Ch = null;

	int[] maxWidths2Ch = null;
	int[] maxWidthsB4Decimal2Ch = null;
	int[] maxWidthsAfterDecimal2Ch = null;

	int numberOfHeaderColumns;
	String[] widthCalculationInstructions;

	Map actualWidthsInfo = new HashMap();
	Map maxWidthsInfo = new HashMap();
	Map maxWidthsB4Decimal1Info = new HashMap();
	Map maxWidths1Info= new HashMap();
	Map maxWidths2Info = new HashMap();
	Map maxWidthsB4Decimal2Info = new HashMap();
	Map maxWidthsAfterDecimal2Info = new HashMap();
	Map maxWidthsAfterDecimalInfo = new HashMap();
	Map maxWidthsAfterDecimal1Info = new HashMap();
	Map preferredWidthInfo = new HashMap();
	
	public TableWidthCalculator(ReportSetup setup){
		numberOfHeaderColumns = setup.getNumberOfHeaderColumnsI();
		widthCalculationInstructions = setup.getWidthCalculationInstructions();
		calcPreferredLegths = setup.applyTextWeightedStretching();
		calcCharacterSizes = setup.isGenerateSimpleSizingInfo();
	}
	
	public float[] getMaxWidths1() {
		return maxWidths1;
	}

	public float[] getMaxWidthsB4Decimal1() {
		return maxWidthsB4Decimal1;
	}

	public float[] getMaxWidthsAfterDecimal1() {
		return maxWidthsAfterDecimal1;
	}

	public float[] getMaxWidths2() {
		return maxWidths2;
	}

	public float[] getMaxWidthsB4Decimal2() {
		return maxWidthsB4Decimal2;
	}

	public float[] getMaxWidthsAfterDecimal2() {
		return maxWidthsAfterDecimal2;
	}

	public float getFontsize() {
		return fontsize;
	}

	public Font getFont_tn() {
		return font_tn;
	}


	public float[] getMaxWidths() {
		return maxWidths;
	}

	//public float[] getMaxWidthsB4Decimal() {
	//	return maxWidthsB4Decimal;
	//}

	public float[] getMaxWidthsAfterDecimal() {
		return maxWidthsAfterDecimal;
	}

	public void init(ReportLaF laf,  int numberOfColumns){
		this.laf = laf;
		fontsize= laf.getFontSize();
		font_tn =  laf.getBodyFont();
		
		maxWidths = new float[numberOfColumns];
		preferredWidths = new float[numberOfColumns];
		//maxWidthsB4Decimal = new float[numberOfColumns];
		maxWidthsAfterDecimal = new float[numberOfColumns];
		maxWidths1 = new float[numberOfColumns];
		maxWidthsB4Decimal1 = new float[numberOfColumns];
		maxWidthsAfterDecimal1 = new float[numberOfColumns];
		maxWidths2 = new float[numberOfColumns];
		maxWidthsB4Decimal2 = new float[numberOfColumns];
		maxWidthsAfterDecimal2 = new float[numberOfColumns];
		paddingAdjustements = new float[numberOfColumns];

		if(calcCharacterSizes){
			maxWidthsCh = new int[numberOfColumns];
			//maxWidthsB4Decimal = new float[numberOfColumns];
			maxWidthsAfterDecimalCh = new int[numberOfColumns];
			maxWidths1Ch = new int[numberOfColumns];
			maxWidthsB4Decimal1Ch = new int[numberOfColumns];
			maxWidthsAfterDecimal1Ch = new int[numberOfColumns];
			maxWidths2Ch = new int[numberOfColumns];
			maxWidthsB4Decimal2Ch = new int[numberOfColumns];
			maxWidthsAfterDecimal2Ch = new int[numberOfColumns];
		
		}
		for (int i = 0; i < numberOfColumns; i++) {
			maxWidths[i] = 0;
			maxWidthsAfterDecimal[i] = 0;
			//maxWidthsB4Decimal[i] = 0;
			maxWidths1[i] = 0;
			maxWidthsAfterDecimal1[i] = 0;
			maxWidthsB4Decimal1[i] = 0;
			maxWidths2[i] = 0;
			maxWidthsAfterDecimal2[i] = 0;
			maxWidthsB4Decimal2[i] = 0;
			paddingAdjustements[i] = 0;
		}
	}
	
	RcdRow lastHeaderLine = null;
	public void inspect(RcdRow line) throws Exception{
	     inspect(line, false);
	}
	public void inspect(RcdRow line, boolean processHeader) throws Exception{
		//processHeader - decides if header lines are stored or processed
		//span whole row
		boolean columnSpanWholeRow = false;
		String col0 = line.getSpan0();
		if(!StringUtil.isEmpty(col0)){
			if(col0.trim().equalsIgnoreCase("all")){
				return; //no calulations for spanning all row columns
			} else {
				throw new Exception("Unsupported __span_0 value, this value is ingored at row rowid=" + line.getRowid());
			}
		}
		//end span whole row

		if(!processHeader){
			if("HEAD".equals(line.getDatatype())) {
				lastHeaderLine = line;
				return;
			} else if("TBODY".equals(line.getDatatype())){
				if(lastHeaderLine!=null) {
					inspect(lastHeaderLine, true);
					lastHeaderLine = null;
				}		
			}
		}

		//Map allCols = line.getCols();
		String styletype = "body";
		StyledTextSizingUtil sizingUtil = StyledTextSizingUtil.self;
		StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(laf);
		StringTokenizer st = new StringTokenizer(line.getAlign(), " ");
		int colsize= line.getColSize();
		for (int i = 0; i < colsize; i++) {
			float currentWidth = 0;
			float currentWidthB4Dec = 0;
			float currentWidthAfterDec = 0;
			float currentWidth1 = 0;
			float currentWidthB4Dec1 = 0;
			float currentWidthAfterDec1 = 0;
			float currentWidth2 = 0;
			float currentWidthB4Dec2 = 0;
			float currentWidthAfterDec2 = 0;
			float currentPaddingAdjustement=0;
			float currentPreferredWidth=0;
			StyledText longestPreferred = StyledText.EMPTY;

			int currentWidthCh = 0;
			int currentWidthB4DecCh = 0;
			int currentWidthAfterDecCh = 0;
			int currentWidth1Ch = 0;
			int currentWidthB4Dec1Ch = 0;
			int currentWidthAfterDec1Ch = 0;
			int currentWidth2Ch = 0;
			int currentWidthB4Dec2Ch = 0;
			int currentWidthAfterDec2Ch = 0;

			String align = null;
			if(st.hasMoreTokens()) 
				align = st.nextToken().toUpperCase(); 
			else 
				align = "L";
			
			StyledText curcol = line.getColumAsStyledText(i, styletype, laf);
			List<StyledText> curcolAsLines = line.getColumAsStyledTextLines(i, styletype, laf);
			StyledText longestNonsplitable = curcol;
			String longestNonsplitableInfo = "";
			int charLength=-1;
			String widthCalcMethod = widthCalculationInstructions[i];
			String textB4dot = "";
			
			//if(!StringUtil.isEmpty(curcol) && curcol.indexOf("1.732") != -1) {
			//	System.out.println(i + " calc (" + curcol + ") align="+ align);
			//}
			if("HEAD".equals(line.getDatatype())) styletype = "header";

			if(!"HEAD".equals(line.getDatatype()) && widthCalcMethod.endsWith("H"))
					widthCalcMethod = widthCalcMethod.substring(0, widthCalcMethod.length()-1);
			if (curcol.isEmpty()) continue;
			
			if (widthCalcMethod !=null && widthCalcMethod.startsWith("F") && (align ==null || align.indexOf('D') ==-1)) {
				//fixed widths are force-applied at the end
				//if fixed width is takeon on a numeric column a max of calculated values and requiested fixed value is used.
			} else	if ("HEAD".equals(line.getDatatype()) || ("LW".equals(widthCalcMethod) && (align ==null || align.indexOf('D') ==-1))) {
				//apply logic of non-wrapping words for all header columns and all non-numeric columns if word wrap is not allowed
				//if("HEAD".equals(line.getDatatype()))
				//		System.out.println("dd");
				if("HEAD".equals(line.getDatatype()) && !widthCalcMethod.endsWith("H")){
					
				} else {
				    float cw=0f;
				    int cwCh = 0;
				    float cline=0f;
					for (int j = 0; j < curcolAsLines.size(); j++) {
						StyledText stline = curcolAsLines.get(j);
						if(calcPreferredLegths) {
							float cline_ = sizingUtil.calcLineSize(stline, styletype, laf);
							if(cline_ > cline){
								longestPreferred = stline;
								cline = cline_;
							}
						}
						//c == '/' || c == ' ' || c == '-' || c==','
						
						List<StyledText> splits = styledTextUtil.splitIntoList(stline, laf.getSplitChars(), true, true);

						for(int x = 0; x< splits.size(); x++){
							StyledText s0 = splits.get(x);
							if(!s0.isEmpty()){
								float cw0 =  sizingUtil.calcLineSize(s0, styletype, laf);
								if (cw0>cw) {
									cw=cw0;
									longestNonsplitable = s0;
									if(calcCharacterSizes) {
										cwCh = sizingUtil.calcCharacterSize(s0, laf);							 
									}
								}															
							}
						}
					}
					if(i==0) {
						int indentlv = line.getIndentlev();
						cw = cw + (indentlv * laf.getIndentSize());
						if(calcCharacterSizes){
							cwCh = cwCh + (indentlv * laf.getIndentSizeCh());
						}
						if(calcPreferredLegths) cline = cline + (indentlv * laf.getIndentSize());
					}
					currentWidth = cw +CELL_PADDING_ADJUSTMENT; //add 4 to accomodate for cell internal spacing
					currentWidthCh = cwCh;

					if(calcPreferredLegths) {
						currentPreferredWidth = cline + CELL_PADDING_ADJUSTMENT;
						//System.out.println(i + " got " + currentPreferredWidth + ":" + longestPreferred);
					}
					currentPaddingAdjustement = CELL_PADDING_ADJUSTMENT;
					//currentWidthB4Dec=cw;
					//currentWidthAfterDec=0;
				}

			} else if (align !=null && align.indexOf('D') !=-1){
				//TODO this assumes that curcol is a single line!!
				currentWidth = sizingUtil.calcLineSize(curcol, styletype, laf);
				if(calcCharacterSizes)
					currentWidthCh = sizingUtil.calcCharacterSize(curcol, laf);
				
				//curcol = curcol.trim();
	
                if ("D".equals(align)) {
					currentWidthB4Dec=currentWidth;
					if(calcCharacterSizes)
						currentWidthB4DecCh= currentWidthCh;
					if(!curcol.isEmpty()){
						Pair<StyledText, StyledText> split = styledTextUtil.split(curcol, ".,)%: ", false);
						StyledText s0 = split.left;
						currentWidthB4Dec = sizingUtil.calcLineSize(s0, styletype, laf);
						if(calcCharacterSizes)
							currentWidthB4DecCh= sizingUtil.calcCharacterSize(s0, laf);
					}
					currentWidthAfterDec = currentWidth- currentWidthB4Dec;
					if(calcCharacterSizes)
						currentWidthAfterDecCh = currentWidthCh - currentWidthB4DecCh;

					//if(!StringUtil.isEmpty(curcol) && curcol.indexOf("1.732") != -1) {
					//	System.out.println(i + " calc (" + curcol + ") D"+ (currentWidth) +"," +currentWidthB4Dec+","+currentWidthAfterDec);
					//}

				} else if ("RD".equals(align)) {
					Pair<StyledText, StyledText> split = styledTextUtil.split(curcol, " ", false);
					StyledText tmpword1 = split.left.appendToLastChunk(" ", "body", laf);
					StyledText tmpword2 = split.right.leftTrim();
					
					tmpword2 = tmpword2.trim();
					currentWidth1 = sizingUtil.calcLineSize(tmpword1, styletype, laf);
					currentWidth2 = sizingUtil.calcLineSize(tmpword2, styletype, laf);
					currentWidthB4Dec1 = currentWidth1;
					currentWidthB4Dec2 = currentWidth2;
					if(calcCharacterSizes){
						currentWidth1Ch = sizingUtil.calcCharacterSize(tmpword1, laf);
						currentWidth2Ch = sizingUtil.calcCharacterSize(tmpword2, laf);
						currentWidthB4Dec1Ch = currentWidth1Ch;
						currentWidthB4Dec2Ch = currentWidth2Ch;						
					}
					//trim after to make sure currentWidth1 includes space
					tmpword1 = tmpword1.trim();
					
					if(!tmpword1.isEmpty()){
						Pair<StyledText, StyledText> split2 = styledTextUtil.split(tmpword1, ".,)%:", false);
						StyledText s0 = split2.left;
						
						currentWidthB4Dec1 = sizingUtil.calcLineSize(s0, styletype, laf) ;
						if(calcCharacterSizes)
							currentWidthB4Dec1Ch = sizingUtil.calcCharacterSize(s0, laf);
						
					}
					currentWidthAfterDec1 = currentWidth1- currentWidthB4Dec1;
					if(calcCharacterSizes)
						currentWidthAfterDec1Ch = currentWidth1Ch - currentWidthB4Dec1Ch;
						

					if(!tmpword2.isEmpty()){
						Pair<StyledText, StyledText> split2 = styledTextUtil.split(tmpword2,".,)%:", false);
						StyledText s0 = split2.left;
						currentWidthB4Dec2 = sizingUtil.calcLineSize(s0, styletype, laf) ;
						if(calcCharacterSizes)
							currentWidthB4Dec2Ch = sizingUtil.calcCharacterSize(s0, laf);
						
					}
					currentWidthAfterDec2 = currentWidth2- currentWidthB4Dec2;					
					if(calcCharacterSizes)
						currentWidthAfterDec2Ch = currentWidth2Ch - currentWidthB4Dec2Ch;
				}
				
			} else if ("N".equals(widthCalcMethod)) {
				float cw=0f;
				int cwCh=0;
				List<StyledText> adjustedLines = new ArrayList<StyledText>();
				adjustedLines.addAll(curcolAsLines);
				
				if(adjustedLines.size()>0 ) {
					if(!"L".equals(align)){
						adjustedLines.set(0, adjustedLines.get(0).trim());
					} else {
						adjustedLines.set(0, adjustedLines.get(0).rightTrim());
					}
				}

				int totInd= 0;
				for (int j = 0; j < adjustedLines.size(); j++) {
					StyledText thisLine = adjustedLines.get(j);
					float lineSize = sizingUtil.calcLineSize(thisLine, styletype, laf);
					int lineCharSize = sizingUtil.calcCharacterSize(thisLine, laf);
					if(j==0) {
						cw = lineSize;
					    longestNonsplitable = thisLine; 
					    totInd = thisLine.getIndent(); //TODO need a method on StyledText
					    if(calcCharacterSizes)
					    	cwCh = lineCharSize;
					}
					else if (cw < lineSize) {
						cw = lineSize;
						longestNonsplitable = thisLine;  
						totInd = thisLine.getIndent();
					    if(calcCharacterSizes)
					    	cwCh = lineCharSize;
					}
				}
				if(i==0) {
					int indentlv = line.getIndentlev();
					cw = cw + (indentlv * laf.getIndentSize());
					totInd = totInd + indentlv;
				    if(calcCharacterSizes)
				    	cwCh = cwCh + (indentlv * laf.getIndentSizeCh());
				}
				currentWidth = cw +CELL_PADDING_ADJUSTMENT; //add 4 to accomodate for cell padding in non D aligned cells
			    if(calcCharacterSizes)
			    	currentWidthCh = cwCh;
				currentPaddingAdjustement = CELL_PADDING_ADJUSTMENT;
				charLength = longestNonsplitable.getText().length();
				longestNonsplitableInfo = "ind(" + totInd + ")";
				//currentWidthB4Dec=cw;
				//currentWidthAfterDec=0;
			} else {
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "WARNING: Invalid width calculation instruction " + widthCalcMethod + " check requested __colwidths values");
			}

			/*
			if(currentWidthB4Dec2>0)currentWidthB4Dec2=currentWidthB4Dec2+2;
			if(currentWidthB4Dec1>0)currentWidthB4Dec1=currentWidthB4Dec1+2;
			if(currentWidthB4Dec>0)currentWidthB4Dec=currentWidthB4Dec+2;
			if(currentWidthAfterDec1>0)currentWidthAfterDec1=currentWidthAfterDec1+2;
			if(currentWidthAfterDec2>0)currentWidthAfterDec2=currentWidthAfterDec2+2;
			if(currentWidthAfterDec>0)currentWidthAfterDec=currentWidthAfterDec+2;
            */
			
			if (maxWidths.length > 0) {
				String longestNonsplitableText = longestNonsplitable.getText();
				longestNonsplitableInfo += longestNonsplitableText;
				if(charLength ==-1) charLength = longestNonsplitableText.length();
				longestNonsplitableInfo= longestNonsplitableInfo + "[";// + "charlength=" + charLength ;
				if (maxWidths[i] < currentWidth) {
					maxWidths[i] = currentWidth;
					String charInfo = "";
				    if(calcCharacterSizes) {
				    	maxWidthsCh[i] = currentWidthCh;
				    	charInfo = "(" +currentWidthCh + "ch)";
				    }
					paddingAdjustements[i]=currentPaddingAdjustement;				    	
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
						maxWidthsInfo.put("" + i, longestNonsplitableInfo + "rowid=" + line.getRowid() + " width=" + currentWidth + charInfo +  "]");
				}
		
				//if (maxWidthsB4Decimal[i] < currentWidthB4Dec) {
				//	maxWidthsB4Decimal[i] = currentWidthB4Dec;
				//}
				if (maxWidthsB4Decimal1[i] < currentWidthB4Dec) {
					maxWidthsB4Decimal1[i] = currentWidthB4Dec;
					String charInfo = "";
				    if(calcCharacterSizes) {
				    	maxWidthsB4Decimal1Ch[i] = currentWidthB4DecCh;
				    	charInfo = "(" +currentWidthB4DecCh + "ch)";
				    }
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
					 maxWidthsB4Decimal1Info.put("" + i, longestNonsplitableInfo + " rowid=" + line.getRowid() + " width=" + currentWidth + " B4Dec1="+currentWidthB4Dec+ charInfo+"]");
				}
				
				if (maxWidthsB4Decimal1[i] < currentWidthB4Dec1) {
					maxWidthsB4Decimal1[i] = currentWidthB4Dec1;
					String charInfo = "";
				    if(calcCharacterSizes) {
						maxWidthsB4Decimal1Ch[i] = currentWidthB4Dec1Ch;
				    	charInfo = "(" +currentWidthB4Dec1Ch + "ch)";
				    }
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
					 maxWidthsB4Decimal1Info.put("" + i, longestNonsplitableInfo + " rowid=" + line.getRowid() + " width=" + currentWidth + " B4Dec1_="+currentWidthB4Dec1+ charInfo+ "]");
				}
				if (maxWidthsB4Decimal2[i] < currentWidthB4Dec2) {
					maxWidthsB4Decimal2[i] = currentWidthB4Dec2;
					String charInfo = "";
				    if(calcCharacterSizes) {
						maxWidthsB4Decimal2Ch[i] = currentWidthB4Dec2Ch;
				    	charInfo = "(" +currentWidthB4Dec2Ch + "ch)";
				    }
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
					 maxWidthsB4Decimal2Info.put("" + i, longestNonsplitableInfo + " rowid=" + line.getRowid() + " width=" + currentWidth + " B4Dec2_="+currentWidthB4Dec2+ charInfo+ "]");
				}
				
				if (maxWidthsAfterDecimal[i] < currentWidthAfterDec) {
					maxWidthsAfterDecimal[i] = currentWidthAfterDec;
					String charInfo = "";
				    if(calcCharacterSizes){
						maxWidthsAfterDecimalCh[i] = currentWidthAfterDecCh;
				    	charInfo = "(" +currentWidthAfterDecCh + "ch)";
				    }
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
					 maxWidthsAfterDecimalInfo.put("" + i, longestNonsplitableInfo + " rowid=" + line.getRowid() + " width=" + currentWidth + " AfterDec="+ currentWidthAfterDec + charInfo+ "]");
				}
				
				if (maxWidthsAfterDecimal[i] < currentWidthAfterDec1 + currentWidth2) {
					maxWidthsAfterDecimal[i] = currentWidthAfterDec1 + currentWidth2;
					String charInfo = "";
				    if(calcCharacterSizes) {
						maxWidthsAfterDecimalCh[i] = currentWidthAfterDec1Ch + currentWidth2Ch;
				    	charInfo = "(" +(currentWidthAfterDec1Ch + currentWidth2Ch) + "ch)";
				    }
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
					 maxWidthsAfterDecimalInfo.put("" + i, longestNonsplitableInfo + " rowid=" + line.getRowid() + " width=" + currentWidth + " AfterDec_="+ currentWidthAfterDec1 + currentWidth2 + charInfo+ "]");
				} //includes RD calculation in D positioning.
				
				if (maxWidthsAfterDecimal1[i] < currentWidthAfterDec1) {
					maxWidthsAfterDecimal1[i] = currentWidthAfterDec1;
					String charInfo = "";
				    if(calcCharacterSizes) {
						maxWidthsAfterDecimal1Ch[i] = currentWidthAfterDec1Ch;
				    	charInfo = "(" +currentWidthAfterDec1Ch + "ch)";
				    }
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
					 maxWidthsAfterDecimal1Info.put("" + i, longestNonsplitableInfo + " rowid=" + line.getRowid() + " width=" + currentWidth +  " AfterDec1="+ currentWidthAfterDec1 + charInfo+ "]");
				}
				if (maxWidthsAfterDecimal2[i] < currentWidthAfterDec2) {
					maxWidthsAfterDecimal2[i] = currentWidthAfterDec2;
					String charInfo = "";
				    if(calcCharacterSizes) {
						maxWidthsAfterDecimal2Ch[i] = currentWidthAfterDec2Ch;
				    	charInfo = "(" +currentWidthAfterDec2Ch + "ch)";
				    }
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
					 maxWidthsAfterDecimal2Info.put("" + i, longestNonsplitableInfo + " rowid=" + line.getRowid() + " width=" + currentWidth + " AfterDec2="+ currentWidthAfterDec2 + charInfo+ "]");
				}
				if (preferredWidths[i] < currentPreferredWidth) {
					preferredWidths[i] = currentPreferredWidth;
					preferredWidthInfo.put("" + i, longestPreferred + " rowid=" + line.getRowid() + " preferred width=" + currentPreferredWidth );
				}
			}
		}
		
	}

	
	private float[] getActualWidths(float[] extrapadding){
		float actualWidths[] = new float[maxWidths.length];
		for (int i = 0; i < actualWidths.length; i++) {
			float  max_dec1, max_dec2, max_dec, max_all_dec;
			actualWidths[i]=maxWidths[i];
			if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
				actualWidthsInfo.put("" + i, "size=" + actualWidths[i]+ " max_width:"+ maxWidthsInfo.get("" + i));
			max_dec1 = maxWidthsB4Decimal1[i]+ maxWidthsAfterDecimal1[i];// + ONE_SIDED_CELL_PADDING_AJDUSTEMENT; //+2;
			max_dec2 = maxWidthsB4Decimal2[i]+ maxWidthsAfterDecimal2[i] + ONE_SIDED_CELL_PADDING_AJDUSTEMENT; //+2;
			//tmp3 = maxWidthsB4Decimal[i]+ maxWidthsAfterDecimal[i]; //+2;
			max_dec = maxWidthsB4Decimal1[i]+ maxWidthsAfterDecimal[i]; //+2;
			
			//potenial issue - no space between numbers in RD alignment (we are simply adding)
			//THIS IS NO ISSUE AS THERE IS NO TRIMMING AND EMPTY SPACE IN TEXT IS COUNTED AS AFTER DECIMAL

			max_all_dec = max_dec >=max_dec1 + max_dec2 ? max_dec: max_dec1 + max_dec2;
			float max_all_dec_padding_adjustement = max_dec >=max_dec1 + max_dec2 ? 0: ONE_SIDED_CELL_PADDING_AJDUSTEMENT;
			if (actualWidths[i]<max_all_dec) {
				actualWidths[i]=max_all_dec;
				paddingAdjustements[i] = max_all_dec_padding_adjustement;
				if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED)){
					if(max_dec >=max_dec1 + max_dec2) {
						actualWidthsInfo.put("" + i, "size=" + actualWidths[i] + " maxB4Dec1:"+ maxWidthsB4Decimal1Info.get("" + i)+ " + maxAfterDec:" + maxWidthsAfterDecimalInfo.get("" + i));	
					} else {
						actualWidthsInfo.put("" + i, "size=" + actualWidths[i] + " maxB4Dec1:"+ maxWidthsB4Decimal1Info.get("" + i)+ " + maxAfterDec1:" + maxWidthsAfterDecimal1Info.get("" + i) + 
								                                              " + maxB4Dec2:" + maxWidthsB4Decimal2Info.get("" + i) + " + maxAfterDec2:" + maxWidthsAfterDecimal2Info.get("" + i));									
					}
				}
			}
			String widthInstruction = this.widthCalculationInstructions[i];
			if(widthInstruction.startsWith("F")){
				//NOTE, we are doning max here to accomodate for situation where RD or D alignment numeric columns have
				// been used with fixed width setup.  Unless this happens the actualWidths should be 0 if F is used.
				widthInstruction = widthInstruction.substring(1);
				float min_width = 0f;
				try{
					min_width = Float.parseFloat(widthInstruction);
				}catch(Exception e){	
					if(i<this.numberOfHeaderColumns) min_width = 72f;
					else min_width = 15f;
					SasshiatoTrace.logError("Invalid __colwidth instruction " + widthInstruction + " width for column " + i + " will be defaulted to " + min_width);
				}
				//System.out.println("act=" + actualWidths[i] + ", reg=" + min_width );
				//System.out.println("" + actualWidthsInfo.get("" + i));
				if(actualWidths[i] < min_width) {
					if(maxWidths[i]>0) SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: column " + i + " has requested or defaulted __colwidths width request: " + widthInstruction + " the calculated width of " + actualWidths[i] + " will be streached accordingly");
					float padding = 0;
					if(actualWidths[i] >0){
						padding = (min_width - actualWidths[i])/2;
					}
					actualWidths[i] = min_width;
					paddingAdjustements[i] = 0;
					try{
						if(Font.COURIER == laf.getFont("body").getFamily()){
							float fitNumber = actualWidths[i]/laf.getDefaultCharWidth();
							float remainder = actualWidths[i]- (float) Math.floor(fitNumber) * laf.getDefaultCharWidth();
							paddingAdjustements[i] = remainder;
							//System.out.println("here" + fitNumber+"," +(float) Math.floor(fitNumber * laf.getDefaultCharWidth())+"," + remainder);
						}
					}catch(Exception e){ SasshiatoTrace.logError("Unexpected error in widths calculation", e);}
					if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED))
						actualWidthsInfo.put("" + i, "size =" + actualWidths[i] +  " Per __colwidths request");
					extrapadding[i] = padding;
				} else {
					if(actualWidths[i] >ONE_SIDED_CELL_PADDING_AJDUSTEMENT) {
						SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "WARNING: column " + i + " has requested or defaulted __colwidths width of: " + widthInstruction + " points yet it contains numeric D or RD aligned data with calculated size of " + actualWidths[i] + " points, the requested width will not be honored");
					}
				}
			} else {
				//calculate extras based on numeric and total widths:
				if(max_all_dec + 15f<actualWidths[i]){
					extrapadding[i] = actualWidths[i]- max_all_dec-15f;
				}
			}
		}
		if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_REQUIRED)) {
			StringBuffer trb = new StringBuffer();
			StringBuffer paddingTxt = new StringBuffer();
			StringBuffer preferredW = new StringBuffer();
			for (int i = 0; i < actualWidths.length; i++) {
				trb.append("col_").append(i).append(":").append(actualWidthsInfo.get("" +i)).append("\n");
				paddingTxt.append("c_").append(i).append("=").append(paddingAdjustements[i]).append(" ");
				if(calcPreferredLegths && preferredWidthInfo.get("" + i) !=null) {
					preferredW.append("c_").append(i).append("=").append(preferredWidthInfo.get("" + i)).append("\n");
				}
			}
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Width calcuation details (char widths shown without indentation): \n" + trb + "\nCell padding adjustements="+ paddingTxt, false);
			if(calcPreferredLegths) SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Preferred Width details: \n" + preferredW, false);
			
		}
		return actualWidths;
	}

	private int[] getActualWidthsCh(){
		int actualWidths[] = new int[maxWidths.length];
		for (int i = 0; i < actualWidths.length; i++) {
			int  max_dec1, max_dec2, max_dec, max_all_dec;
			actualWidths[i]=maxWidthsCh[i];
			//if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_INFO))
			//	actualWidthsInfo.put("" + i, "size=" + actualWidths[i]+ " max_width:"+ maxWidthsInfo.get("" + i));
			max_dec1 = maxWidthsB4Decimal1Ch[i]+ maxWidthsAfterDecimal1Ch[i];
			max_dec2 = maxWidthsB4Decimal2Ch[i]+ maxWidthsAfterDecimal2Ch[i];
			//tmp3 = maxWidthsB4Decimal[i]+ maxWidthsAfterDecimal[i]; //+2;
			max_dec = maxWidthsB4Decimal1Ch[i]+ maxWidthsAfterDecimalCh[i]; //+2;
			//max_all_dec = max_dec >=max_dec1 + max_dec2 + 1? max_dec: max_dec1 + max_dec2 +1; //add one space of separation between decimal sets
			max_all_dec = max_dec >=max_dec1 + max_dec2? max_dec: max_dec1 + max_dec2; 
			
			if (actualWidths[i]<max_all_dec) {
				actualWidths[i]=max_all_dec;
				//if(max_all_dec == max_dec1 + max_dec2 + 1) {
				//	SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Character width calculation for Column " + i + ": adding extra one char separation between number groups for RD alignment");
				//}
				/*if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_INFO)){
					if(max_dec >=max_dec1 + max_dec2) {
						actualWidthsInfo.put("" + i, "size=" + actualWidths[i] + " maxB4Dec1:"+ maxWidthsB4Decimal1Info.get("" + i)+ " + maxAfterDec:" + maxWidthsAfterDecimalInfo.get("" + i));	
					} else {
						actualWidthsInfo.put("" + i, "size=" + actualWidths[i] + " maxB4Dec1:"+ maxWidthsB4Decimal1Info.get("" + i)+ " + maxAfterDec1:" + maxWidthsAfterDecimal1Info.get("" + i) + 
								                                              " + maxB4Dec2:" + maxWidthsB4Decimal2Info.get("" + i) + " + maxAfterDec2:" + maxWidthsAfterDecimal2Info.get("" + i));									
					}
				}*/
			}
			String widthInstruction = this.widthCalculationInstructions[i];
			if(widthInstruction.startsWith("F")){
				//NOTE, we are doning max here to accomodate for situation where RD or D alignment numeric columns have
				// been used with fixed width setup.  Unless this happens the actualWidths should be 0 if F is used.
				widthInstruction = widthInstruction.substring(1);
				float min_widthF = 0f;
				try{
					min_widthF = Float.parseFloat(widthInstruction);
				}catch(Exception e){	
					if(i<this.numberOfHeaderColumns) min_widthF = 72f;
					else min_widthF = 15f;
					//SasshiatoTrace.logError("Invalid __colwidth instruction " + widthInstruction + " width for column " + i + " will be defaulted to " + min_width);
				}
				int min_width = Math.round(min_widthF/laf.getDefaultCharWidth());
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: column " + i + " has requested or defaulted width " + widthInstruction +" in points=" + min_widthF + ", which translates to " + min_width + " characters");
				//System.out.println("act=" + actualWidths[i] + ", reg=" + min_width );
				//System.out.println("" + actualWidthsInfo.get("" + i));
				if(actualWidths[i] < min_width) {
					if(maxWidths[i]>0) SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: column " + i + " has requested or defaulted __colwidths width request: " + widthInstruction + " the calculated width of " + actualWidths[i] + " chars and will be streached per requested width instruction");
					actualWidths[i] = min_width;
				} else {
					SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "WARNING: column " + i + " has requested or defaulted __colwidths width request: " + widthInstruction + " yet it contains numeric D or RD aligned data with calculated size of " + actualWidths[i] + ", the requested width will not be honored");
				}
			} 
		}
		
		/*
		if(SasshiatoTrace.isTracing(SasshiatoTrace.LEV_INFO)) {
			StringBuffer trb = new StringBuffer();
			for (int i = 0; i < actualWidths.length; i++) {
				trb.append("col_").append(i).append(":").append(actualWidthsInfo.get("" +i)).append("\n");
			}
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Width calculation details in characters (indentation not shown): \n" + trb , false);			
		}
		*/
		return actualWidths;
	}

	private float[] getActualWidths1(float[] fforcedpadding){
		float actualWidths1[] = new float[maxWidths.length];
		
		for (int i = 0; i < actualWidths1.length; i++) {
			actualWidths1[i] = maxWidthsB4Decimal1[i]+ maxWidthsAfterDecimal1[i]; //+ONE_SIDED_CELL_PADDING_AJDUSTEMENT;
		}
		return actualWidths1;
	}
	
	private float[] getActualWidths2(float[] fforcedpadding){
		float actualWidths2[] = new float[maxWidths.length];
		for (int i = 0; i < actualWidths2.length; i++) {
			actualWidths2[i] = maxWidthsB4Decimal2[i]+ maxWidthsAfterDecimal2[i]+ONE_SIDED_CELL_PADDING_AJDUSTEMENT;
		}
		return actualWidths2;
	}	
	
	public TableWidths getTableWidths(){
		
		TableWidths result = new TableWidths();
		float[] padding= new float[maxWidths.length];
		//note getActualWidths has side effect of correcting paddingAdjustemnts
		float[] actualWidths = this.getActualWidths(padding);
		float[] originalWidths = new float[actualWidths.length];
		for (int i = 0; i < actualWidths.length; i++) {
			//boolean isMiddle = i==0;
			originalWidths[i] = actualWidths[i];
			actualWidths[i] = actualWidths[i] + RrgPrinterUtil.getSelf().getColumnTotalSeparation(i, laf);			
		}
		String overrideMessage = "";
		if(laf.hasColumnSeparationOverrides()) 
			overrideMessage = " with overrides: " + laf.getColumnSeparationOverrideInfo();
		
		SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "INFO: Column separation of " + (2* laf.getColumnSeparation()) + " is added" + overrideMessage);
		if(calcCharacterSizes){
			int[] actualWidthsCh = getActualWidthsCh();
			result.setActualWidthsInChars(actualWidthsCh);
		}
		result.setOriginalWidths(originalWidths);
		result.setIncludedCellPaddingAdjustements(this.paddingAdjustements);
		result.setActualWidths(actualWidths);
		result.setExtraWidths(padding);
		result.setActualWidths1(this.getActualWidths1(padding));
		result.setActualWidths2(this.getActualWidths2(padding));
		result.setPreferredWidths(this.preferredWidths);
		
		//float[] extraWidths = new float[actualWidths.length];
		//for (int i = 0; i < extraWidths.length; i++) {
		//	extraWidths[i] = (actualWidths[i]
		//			- this.getMaxWidthsB4Decimal1()[i] - this
		//			.getMaxWidthsAfterDecimal()[i]) / 2; //note getMaxWidthsB4Decimal changed to getMaxWidthsB4Decimal1
		//}
		
		//result.extraWidths = extraWidths;

		result.setMaxWidths(this.getMaxWidths());
		result.setMaxWidthsB4Decimal(this.getMaxWidthsB4Decimal1()); //MaxWidthsB4Decimal1 contain both RD and D calcuation
		result.setMaxWidthsB4Decimal1(this.getMaxWidthsB4Decimal1());
		result.setMaxWidthsB4Decimal2(this.getMaxWidthsB4Decimal2());
		result.setMaxWidthsAfterDecimal(this.getMaxWidthsAfterDecimal());
		return result;
	}

}
