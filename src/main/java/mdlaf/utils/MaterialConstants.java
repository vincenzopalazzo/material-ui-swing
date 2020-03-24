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
