/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

public class Pair<L, R> {
	public final L left;
	public final R right;
	
	public Pair(L left, R right){
		this.left= left;
		this.right=right;
	}
	
	public L getLeft() {
		return left;
	}
	public R getRight() {
		return right;
	}
}
