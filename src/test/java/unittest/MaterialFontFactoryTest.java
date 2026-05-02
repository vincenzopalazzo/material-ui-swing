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
  private static final String BOLD_NAME = "NotoSans-Bold.ttf";
  private static final String REGULAR_NAME = "NotoSans-Regular.ttf";
  private static final String MEDIUM_NAME = "NotoSans-Medium.ttf";
  private static final String ITALIC_NAME = "NotoSans-Italic.ttf";

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

  private void setDefaultFontSize(MaterialFontFactory fontFactory, float size) throws Exception {
    Field defaultSize = MaterialFontFactory.class.getDeclaredField("defaultSize");
    defaultSize.setAccessible(true);
    defaultSize.set(fontFactory, size);
  }
}
