/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato.simple_info;

import java.util.ArrayList;

public class ProcReportHeaderNode {
	String text;
	boolean leaf;
	boolean root;
	int startColumn;
	int endColumn;
	float rowId;
	boolean isLastRow;
	
	ArrayList children = new ArrayList();
	public ArrayList getChildren() {
		return children;
	}
	private void setChildren(ArrayList children) {
		this.children = children;
	}
	public int getEndColumn() {
		return endColumn;
	}
	public void setEndColumn(int endColumn) {
		this.endColumn = endColumn;
	}
	public boolean isLeaf() {
		return leaf;
	}
	public boolean isLastRow() {
		return isLastRow;
	}
	public void setIsLastRow(boolean lastRow){
		isLastRow = lastRow;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	public int getStartColumn() {
		return startColumn;
	}
	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	public float getRowId() {
		return rowId;
	}
	public void setRowId(float rowId) {
		this.rowId = rowId;
	}
	public synchronized void initAsRoot(int firstColId, int lastColId){
		this.startColumn = firstColId;
		this.endColumn = lastColId;
		this.leaf = false;
		this.root = true;
	}
	public synchronized void insertNode(ProcReportHeaderNode node) throws Exception{
		if(node.endColumn > this.endColumn || node.startColumn < this.startColumn )
			throw new Exception("Headers not compatible with Proc Report, Cannot insert " + node + " into " + this);
		if(children.size()==0) {
			//System.out.println("inserting " + node + " into empty " + this);
			children.add(node);
		} else {
			ProcReportHeaderNode node1= new ProcReportHeaderNode();
			node1.initAsRoot(-1, -1);
			boolean inserted = false;
			for (int i = 0; i < children.size(); i++) {
				ProcReportHeaderNode node2= (ProcReportHeaderNode) children.get(i);
				if(node2!=null){
					if(node2.endColumn >= node.endColumn && node2.startColumn <=node.startColumn){
						//System.out.println("redirecting to child " + node2 + " to insert " + node);
						node2.insertNode(node);
						inserted = true;
						break;
					} else if(node1.endColumn <node.startColumn && node2.startColumn >node.startColumn) {
						//insert in-between
						children.add(i, node);
						//System.out.println("inserting " + node + " into " + this);
						inserted = true;
						break;
					} else if(node1.endColumn < node.startColumn && i==children.size()-1){
						//insert at the end
						//System.out.println("inserting " + node + " into end of " + this);
						children.add(node);
						inserted = true;
						break;
					}
					//System.out.println("have tried node=" + node2 + " of " + this);
					node1=node2;
				}
			}
			if(!inserted) {
				throw new Exception("Headers not compatible with Proc Reprot, Could not insert " + node);
			}
		}		
	}
	
	public synchronized void generateProcReportSyntax(StringBuffer syntax, StringBuffer syntax2ndLine){
		if(!root){
			syntax.append(" (");
		}
		String underline= "|__";
		if(isLastRow) underline = "";
		if(text!=null){
			syntax.append("\"" + rowId + "__col_" + startColumn +  underline + "\"");
		}
		int column = startColumn;
		while(column <= endColumn){
			ProcReportHeaderNode node = findNode(column);
			if(node==null){
				//System.out.println("p: did not found node "  + " for " + column + " in " + this);
				syntax.append(" ");
				syntax.append("__col_" + column);
				column++;
			}
			else {
				//System.out.println("p: found node "  + node  + " for " + column + " in " + this);
				node.generateProcReportSyntax(syntax, syntax2ndLine);
				column = node.endColumn+ 1;
			}
		}
		if(!root){
			syntax.append(")");
		}
		if(isLastRow) {
			for(int i=startColumn; i<=endColumn; i++){
				syntax2ndLine.append("__col_" + i + "=\'\';");
			}
		}

	}
	
	private ProcReportHeaderNode findNode(int column){
		for (int i = 0; i < children.size(); i++) {
			ProcReportHeaderNode node2= (ProcReportHeaderNode) children.get(i);
			if(node2!=null){
				if(node2.startColumn <=column && node2.endColumn >=column)
					return node2;
			}
		}
		return null;
	}
	public String toString(){
		if(root) return "<root," + startColumn + "," + endColumn + ">";
		else return "<node: "+ text + "," + startColumn + "," + endColumn + ">";
	}
}
