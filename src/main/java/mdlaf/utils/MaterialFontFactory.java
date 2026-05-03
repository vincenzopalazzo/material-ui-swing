/*
 * MIT License
 *
 * Copyright (c) 2018-2021 Vincenzo Palazzo vincenzopalazzodev@gmail.com
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

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.plaf.FontUIResource;

/**
 * This class managed the font inside the library and inside the Look and Feel, in fact this class
 * uses a flyweight pattern to minimized the font instance created from the library. In fact on a
 * lot of components you have only 4 instance of font in a normal use case.
 *
 * @author https://github.com/vincenzopalazzo
 */
public class MaterialFontFactory {

  public static final MaterialTypeFont REGULAR = MaterialTypeFont.REGULAR;
  public static final MaterialTypeFont BOLD = MaterialTypeFont.BOLD;
  public static final MaterialTypeFont ITALIC = MaterialTypeFont.ITALIC;
  public static final MaterialTypeFont MEDIUM = MaterialTypeFont.MEDIUM;

  private static MaterialFontFactory SINGLETON;

  public static MaterialFontFactory getInstance() {
    if (SINGLETON == null) {
      SINGLETON = new MaterialFontFactory();
    }
    return SINGLETON;
  }

  /**
   * @deprecated this method will be removed in the version 1.2, this method is not really util fot
   *     the library.
   */
  @Deprecated
  public static Font fontUtilsDisplayable(String textDisplayable, Font withFont) {
    if (textDisplayable == null || withFont == null) {
      throw new IllegalArgumentException(
          "Argument at the fontUtilsDisplayable function are/is null");
    }

    if (withFont.canDisplayUpTo(textDisplayable) < 0) {
      return withFont;
    }

    return new FontUIResource(Font.SANS_SERIF, withFont.getStyle(), withFont.getSize());
  }

  /**
   * The path font was load from a proprieties file. This can permit the user to change the font
   * also with a proprieties file see The file inside resources/config/fonts.properties
   */
  protected Properties properties = new Properties();

  /**
   * Cache of bare typefaces parsed from each TTF resource, keyed by the source identifier (font
   * path or stream identity). Size and per-load attributes (kerning) are NOT baked into these
   * Fonts; they are derived on every {@link #getFont} call so display-scale changes are picked up
   * the next time the font is requested. See {@link #invalidateScaleCache()} for how external
   * code can drop everything when the JRE reports a display change.
   */
  protected Map<String, Font> typefaceCache = new HashMap<>();

  /**
   * @deprecated kept only for binary compatibility with subclasses that may have read this field.
   *     The factory no longer caches sized {@link FontUIResource} instances because doing so pins
   *     font metrics to the scale that was active when the entry was first created.
   */
  @Deprecated protected Map<String, FontUIResource> cacheFont = new HashMap<>();

  protected float defaultSize = 14f;
  protected boolean withPersonalSettings = true;

  private MaterialFontFactory() {
    try {
      loadOsProprieties();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method load the font from a String path, in common this method should be use to load the
   * personal font in a personal location.
   */
  public FontUIResource getFontWithPath(String path) {
    return this.getFontWithPath(path, this.withPersonalSettings);
  }

  /**
   * This method load the font from a input resource, in common this method should be use to load
   * the personal font in a personal resource.
   */
  public FontUIResource getFontWithStream(InputStream stream) {
    return this.getFontWithStream(stream, this.withPersonalSettings);
  }

  /**
   * This method load the library default font, at this moment this is a Noto Sans font, you can
   * load 4 different dimension of font, @see MaterialTypeFont
   */
  public FontUIResource getFont(MaterialTypeFont typeFont) {
    return this.getFont(typeFont, this.withPersonalSettings);
  }

  /**
   * This method load the library default font, at this moment this is a Noto Sans font, you can
   * load 4 different dimension of font, @see MaterialTypeFont
   *
   * <p>In addition, this method have the boolean (by default this propriety is true) called
   * withPersonalSettings to jump the personal font setting. This is util when you have other
   * library that work with font and you want take the control on your code
   */
  public FontUIResource getFont(MaterialTypeFont typeFont, boolean withPersonalSettings) {
    if (typeFont == null) {
      throw new IllegalArgumentException("\n- Parameter type font null.\n");
    }
    String path = properties.getProperty(typeFont.toString());
    return getFontWithPath(path, withPersonalSettings);
  }

  /**
   * This method load the font from a String path, in common this method should be use to load the
   * personal font in a personal location.
   *
   * <p>In addition, this method have the boolean (by default this propriety is true) called
   * withPersonalSettings to jump the personal font setting. This is util when you have other
   * library that work with font and you want take the control on your code
   */
  public FontUIResource getFontWithPath(String path, boolean withPersonalSettings) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("\n- The path to load personal fort is null or empty");
    }
    Font typeface = typefaceCache.get(path);
    if (typeface == null) {
      InputStream stream = getClass().getResourceAsStream(path);
      typeface = loadTypeface(stream);
      typefaceCache.put(path, typeface);
    }
    return deriveSizedFont(typeface, withPersonalSettings);
  }

  /**
   * This method load the font from a input resource, in common this method should be use to load
   * the personal font in a personal resource. In addition, this method have the boolean (by default
   * this propriety is true) called withPersonalSettings to jump the personal font setting. This is
   * util when you have other library that work with font and you want take the control on your code
   */
  public FontUIResource getFontWithStream(InputStream stream, boolean withPersonalSettings) {
    if (stream == null) {
      throw new IllegalArgumentException("\n- The stream to load personal fort is null");
    }
    return deriveSizedFont(loadTypeface(stream), withPersonalSettings);
  }

  /**
   * Drop every cached typeface so the next {@link #getFont} call re-parses the TTF. Call this
   * when the system reports a display scale change so any sized Fonts handed out previously can
   * be re-derived from a freshly loaded typeface; it complements the per-call size derivation in
   * {@link #deriveSizedFont}.
   */
  public void invalidateScaleCache() {
    typefaceCache.clear();
    cacheFont.clear();
  }

  /**
   * Parse a TTF stream into a bare {@link Font}. Intentionally does NOT apply size or kerning,
   * so the same typeface can be re-used to build differently sized Fonts when the display scale
   * changes.
   */
  private Font loadTypeface(InputStream inputStream) {
    try {
      return Font.createFont(Font.TRUETYPE_FONT, inputStream);
    } catch (IOException | FontFormatException e) {
      e.printStackTrace();
      throw new RuntimeException("Font " + inputStream + " wasn't loaded");
    }
  }

  /**
   * Take a bare typeface and produce a {@link FontUIResource} sized for the current scale. The
   * size is recomputed on every call so cross-display moves see a fresh value the next time the
   * factory is asked for a font.
   */
  private FontUIResource deriveSizedFont(Font typeface, boolean withPersonalSettings) {
    float size =
        withPersonalSettings ? this.doOptimizingDimensionFont(this.defaultSize) : this.defaultSize;
    Font sized = typeface.deriveFont(size);
    if (withPersonalSettings) {
      sized = sized.deriveFont(getFontSettings());
    }
    return new FontUIResource(sized);
  }

  private static Map<TextAttribute, Object> getFontSettings() {
    Map<TextAttribute, Object> settings = new HashMap<>();
    settings.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    return settings;
  }

  /**
   * In this method optimizing the font dimension with the display resolution This method to
   * calculate the font dimension is bad, We now but the JDK 8 display the font very bad and at the
   * moment with the JDK 8 the dimension of the font is calculate with the screen resolution and
   * with the JDK9+ the font set with the defaultSize @see defaultSize
   *
   * @param dimension is the dimension font that you want optimizing
   */
  public float doOptimizingDimensionFont(float dimension) {
    if (defaultSize <= 0) {
      throw new IllegalArgumentException("\n- The dimension should be positive (>= 0)");
    }
    if (Utils.isJavaVersionUnderJava9()) {
      float dimensionOptimized =
          11f * Math.min(Toolkit.getDefaultToolkit().getScreenResolution(), 96) / 72;
      if (dimensionOptimized <= (dimension - 3)) {
        // ON OSX with display 4k in some cases the font dimension is equal to 5.
        return (11f * 96) / 72;
      }
      return dimensionOptimized;
    }
    return dimension;
  }

  /**
   * This method was to change the font based on the operating system because there was a bug for
   * the well-known font, it was tinted pixeled, but now this problem has been solved and therefore
   * there is no need to carry around Many dependencies, native fonts are removed from the project
   * but this method remains for furious purposes
   *
   * @throws IOException
   */
  private void loadOsProprieties() throws IOException {
    properties.load(getClass().getResourceAsStream("/config/fonts.properties"));
  }

  /**
   * Enum class that. This constant is to set the library font inside the material type as, REGULAR,
   * BOLD, ITALIC, MEDIUM
   */
  protected enum MaterialTypeFont {
    REGULAR("REGULAR"),
    BOLD("BOLD"),
    ITALIC("ITALIC"),
    MEDIUM("MEDIUM");

    private String type;

    MaterialTypeFont(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return type;
    }
  }
}
