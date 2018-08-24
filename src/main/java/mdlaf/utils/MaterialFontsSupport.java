package mdlaf.utils;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MaterialFontsSupport{

    private static final Map<TextAttribute, Object> fontSettings = new HashMap<TextAttribute, Object>();
    public static final Font BLACK = loadFont ("/fonts/NotoSansArabic/NotoSansArabic-Black.ttf");
    //public static final Font BLACK_ITALIC = loadFont ("/fonts/NotoSansArabic/Roboto-BlackItalic.ttf");
    public static final Font BOLD = loadFont ("/fonts/NotoSansArabic/NotoSansArabic-Bold.ttf");
    //public static final Font BOLD_ITALIC = loadFont ("/fonts/NotoSansArabic/Roboto-BoldItalic.ttf");
    //public static final Font ITALIC = loadFont ("/fonts/NotoSansArabic/Roboto-Italic.ttf");
    public static final Font LIGHT = loadFont ("/fonts/NotoSansArabic/NotoSansArabic-CondensedLight.ttf");
   // public static final Font LIGHT_ITALIC = loadFont ("/fonts/NotoSansArabic/Roboto-LightItalic.ttf");
    public static final Font MEDIUM = loadFont ("/fonts/NotoSansArabic/NotoSansArabic-Medium.ttf");
    //public static final Font MEDIUM_ITALIC = loadFont ("/fonts/NotoSansArabic/Roboto-MediumItalic.ttf");
    public static final Font REGULAR = loadFont ("/fonts/NotoSansArabic/NotoSansArabic-Regular.ttf");
    public static final Font THIN = loadFont ("/fonts/NotoSansArabic/NotoSansArabic-Thin.ttf");
   // public static final Font THIN_ITALIC = loadFont ("/fonts/NotoSansArabic/Roboto-ThinItalic.ttf");

    private static Font loadFont (String fontPath) {
        if (fontSettings.isEmpty ()) {
            fontSettings.put (TextAttribute.SIZE, 14f);
            fontSettings.put (TextAttribute.KERNING, TextAttribute.KERNING_ON);
        }

        try (InputStream inputStream = MaterialFonts.class.getResourceAsStream (fontPath)) {
            return Font.createFont (Font.TRUETYPE_FONT, inputStream).deriveFont (fontSettings);
        }
        catch (IOException | FontFormatException e) {
            e.printStackTrace ();
            throw new RuntimeException ("Font " + fontPath + " wasn't loaded");
        }
    }
}
