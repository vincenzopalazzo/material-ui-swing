/*
 * MIT License
 *
 * Copyright (c) 2019-2020 Vincent Palazzo vincenzopalazzodev@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package mdlaf.utils;

public class MaterialConstants {

	public enum TextComponent {
		/*
		 * Style
		 */
		TEXT_FIELD_STYLE_NONE(0), 
		TEXT_FIELD_STYLE_LINE(1), 
		TEXT_FIELD_STYLE_OUTLINE(2), // only with TextFieldBorder
		TEXT_FIELD_STYLE_BORDER_LINE(3),// only with TextFieldBorder

		;

		private final int code;

		TextComponent(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}
}
