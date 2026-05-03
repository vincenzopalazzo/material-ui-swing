package unittest;

import static java.awt.GraphicsEnvironment.isHeadless;
import static org.junit.Assume.assumeFalse;

import java.awt.*;
import java.lang.reflect.Field;
import javax.swing.plaf.FontUIResource;
import junit.framework.TestCase;
import mdlaf.utils.MaterialFontFactory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MaterialFontFactoryTest {

  private static final String PATH = "/fonts/noto-sans/";
  private static final String BOLD_NAME = "NotoSansDisplay-Bold.ttf";
  private static final String REGULAR_NAME = "NotoSansDisplay-Regular.ttf";
  private static final String MEDIUM_NAME = "NotoSansDisplay-Medium.ttf";
  private static final String ITALIC_NAME = "NotoSansDisplay-Italic.ttf";

  @BeforeClass
  public static void beforeClass() throws Exception {
    assumeFalse("Font tests can't run in headless env.", isHeadless());
  }

  @After
  public void resetDefaultSize() throws Exception {
    setDefaultFontSize(MaterialFontFactory.getInstance(), 14f);
  }

  @Test
  public void testWithString() {
    Font fontOne = MaterialFontFactory.getInstance().getFontWithPath(PATH + BOLD_NAME);
    Font fontTwo = MaterialFontFactory.getInstance().getFontWithPath(PATH + BOLD_NAME);
    TestCase.assertNotNull(fontOne);
    TestCase.assertNotNull(fontTwo);
    TestCase.assertEquals(fontOne, fontTwo);
    TestCase.assertTrue(fontOne instanceof FontUIResource);
    TestCase.assertTrue(fontTwo instanceof FontUIResource);
  }

  @Test
  public void testWithInputStream() {
    Font fontOne =
        MaterialFontFactory.getInstance()
            .getFontWithStream(getClass().getResourceAsStream(PATH + BOLD_NAME));
    Font fontTwo =
        MaterialFontFactory.getInstance()
            .getFontWithStream(getClass().getResourceAsStream(PATH + BOLD_NAME));
    TestCase.assertNotNull(fontOne);
    TestCase.assertNotNull(fontTwo);
    TestCase.assertEquals(fontOne, fontTwo);
    TestCase.assertTrue(fontOne instanceof FontUIResource);
    TestCase.assertTrue(fontTwo instanceof FontUIResource);
  }

  @Test
  public void testWithDefaultCall() {
    Font fontOne = MaterialFontFactory.getInstance().getFont(MaterialFontFactory.BOLD);
    Font fontTwo = MaterialFontFactory.getInstance().getFont(MaterialFontFactory.BOLD);
    TestCase.assertNotNull(fontOne);
    TestCase.assertNotNull(fontTwo);
    TestCase.assertEquals(fontOne, fontTwo);
    TestCase.assertTrue(fontOne instanceof FontUIResource);
    TestCase.assertTrue(fontTwo instanceof FontUIResource);
  }

  @Test
  public void testFontAttributesAreRecomputedForEachLoad() throws Exception {
    MaterialFontFactory fontFactory = MaterialFontFactory.getInstance();

    setDefaultFontSize(fontFactory, 18f);
    Font largerFont =
        fontFactory.getFontWithStream(getClass().getResourceAsStream(PATH + REGULAR_NAME));

    setDefaultFontSize(fontFactory, 14f);
    Font defaultFont =
        fontFactory.getFontWithStream(getClass().getResourceAsStream(PATH + REGULAR_NAME));

    TestCase.assertEquals(18f, largerFont.getSize2D());
    TestCase.assertEquals(14f, defaultFont.getSize2D());
  }

  /**
   * A second {@link MaterialFontFactory#getFontWithPath} call for the same resource path must reuse
   * the parsed typeface but still return a freshly sized {@link FontUIResource} that reflects the
   * current {@code defaultSize}. This is the scale-on-demand contract: typeface caching is fine,
   * size pinning is not.
   */
  @Test
  public void testTypefaceIsReusedButSizeFollowsDefaultSize() throws Exception {
    MaterialFontFactory fontFactory = MaterialFontFactory.getInstance();

    setDefaultFontSize(fontFactory, 14f);
    Font initial = fontFactory.getFontWithPath(PATH + REGULAR_NAME);

    setDefaultFontSize(fontFactory, 22f);
    Font afterSizeChange = fontFactory.getFontWithPath(PATH + REGULAR_NAME);

    TestCase.assertEquals(14f, initial.getSize2D());
    TestCase.assertEquals(22f, afterSizeChange.getSize2D());
    TestCase.assertEquals(initial.getFontName(), afterSizeChange.getFontName());
  }

  /**
   * After {@link MaterialFontFactory#invalidateScaleCache()} the typeface cache is empty, so the
   * next path-based load re-parses the TTF. We can't observe parsing directly, but a successful
   * load with the expected size verifies the path still works after invalidation.
   */
  @Test
  public void testInvalidateScaleCacheForcesReparseAndStillReturnsCorrectSize() throws Exception {
    MaterialFontFactory fontFactory = MaterialFontFactory.getInstance();

    fontFactory.getFontWithPath(PATH + REGULAR_NAME);
    fontFactory.invalidateScaleCache();

    setDefaultFontSize(fontFactory, 17f);
    Font reloaded = fontFactory.getFontWithPath(PATH + REGULAR_NAME);
    TestCase.assertEquals(17f, reloaded.getSize2D());
  }

  private void setDefaultFontSize(MaterialFontFactory fontFactory, float size) throws Exception {
    Field defaultSize = MaterialFontFactory.class.getDeclaredField("defaultSize");
    defaultSize.setAccessible(true);
    defaultSize.set(fontFactory, size);
  }
}
