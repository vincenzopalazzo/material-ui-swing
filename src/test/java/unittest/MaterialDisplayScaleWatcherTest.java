package unittest;

import static java.awt.GraphicsEnvironment.isHeadless;
import static org.junit.Assume.assumeFalse;

import java.awt.Font;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import junit.framework.TestCase;
import mdlaf.MaterialLookAndFeel;
import mdlaf.utils.MaterialDisplayScaleWatcher;
import mdlaf.utils.MaterialFontFactory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MaterialDisplayScaleWatcherTest {

  private static final String PATH = "/fonts/noto-sans/";
  private static final String REGULAR_NAME = "NotoSansDisplay-Regular.ttf";

  @BeforeClass
  public static void beforeClass() throws Exception {
    assumeFalse("Display scale watcher tests need a graphics env.", isHeadless());
  }

  @After
  public void tearDown() throws Exception {
    MaterialDisplayScaleWatcher.uninstall();
    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
  }

  @Test
  public void testInstallAndUninstallAreIdempotent() {
    MaterialDisplayScaleWatcher.install();
    MaterialDisplayScaleWatcher.install();
    MaterialDisplayScaleWatcher.uninstall();
    MaterialDisplayScaleWatcher.uninstall();
  }

  /**
   * After calling the package-private {@code refreshNow} with no Material L&F installed, the
   * factory's typeface cache must still be cleared. This proves invalidation runs before any
   * L&F-specific work, so non-Material consumers don't pay for the watcher.
   */
  @Test
  public void testRefreshNowInvalidatesFontCacheEvenWithoutMaterialLaf() throws Exception {
    MaterialFontFactory factory = MaterialFontFactory.getInstance();
    factory.getFontWithPath(PATH + REGULAR_NAME); // populate typeface cache

    Map<String, Font> typefaces = readTypefaceCache(factory);
    TestCase.assertFalse("typeface cache should have an entry", typefaces.isEmpty());

    invokeRefreshNow(null);

    typefaces = readTypefaceCache(factory);
    TestCase.assertTrue("typeface cache should be empty after refreshNow", typefaces.isEmpty());
  }

  /**
   * With the Material L&F installed, refreshNow should re-derive the theme's fonts via
   * {@link MaterialFontFactory#invalidateScaleCache()} + theme.refreshFonts(). After the call the
   * cache is empty (refresh path completed) and the theme's regular font is non-null.
   */
  @Test
  public void testRefreshNowRebakesThemeFontsWithMaterialLafInstalled()
      throws UnsupportedLookAndFeelException, Exception {
    UIManager.setLookAndFeel(new MaterialLookAndFeel());

    MaterialFontFactory factory = MaterialFontFactory.getInstance();
    factory.getFontWithPath(PATH + REGULAR_NAME);

    invokeRefreshNow(null);

    TestCase.assertTrue(UIManager.getLookAndFeel() instanceof MaterialLookAndFeel);
    TestCase.assertNotNull(UIManager.getFont("Label.font"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Font> readTypefaceCache(MaterialFontFactory factory) throws Exception {
    Field f = MaterialFontFactory.class.getDeclaredField("typefaceCache");
    f.setAccessible(true);
    return (Map<String, Font>) f.get(factory);
  }

  private static void invokeRefreshNow(Object window) throws Exception {
    Method m =
        MaterialDisplayScaleWatcher.class.getDeclaredMethod("refreshNow", java.awt.Window.class);
    m.setAccessible(true);
    m.invoke(null, window);
  }
}
