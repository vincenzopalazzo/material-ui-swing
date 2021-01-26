/**
 * MIT License
 * <p>
 * Copyright (c) 2018-2020 atharva washimkar, Vincenzo Palazzo vincenzopalazzo1996@gmail.com
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package mdlaf.utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author https://github.com/vincenzopalazzo
 */
public class MaterialDrawingUtils {

    public static final boolean MAC_USE_QUARTZ = Boolean.getBoolean( "apple.awt.graphics.UseQuartz" );
    private static HashMap<RenderingHints.Key, Object> defaultHints;

    static {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.xrender", "true");
        defaultHints = new HashMap<>();
        Object value = MAC_USE_QUARTZ ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE;
        defaultHints.put(RenderingHints.KEY_STROKE_CONTROL, value);
        defaultHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        defaultHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
        defaultHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        defaultHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        defaultHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    }

    /**
     * The documentation https://docs.oracle.com/javase/tutorial/2d/text/renderinghints.html
     */
    public static Graphics getAliasedGraphics(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        String screenConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getIDstring();
        String currentDesktop = String.format("awt.font.desktophints.%s", screenConf);
        Map<RenderingHints.Key, Object> renderHints =
                (Map<RenderingHints.Key, Object>) Toolkit.getDefaultToolkit().getDesktopProperty(currentDesktop);
        Map<RenderingHints.Key, Object> hints = putMissedHints(renderHints);
        g2d.addRenderingHints(hints);
        return g2d;
    }

    public static void drawCircle(Graphics g, int x, int y, int radius, Color color) {
        g = getAliasedGraphics(g);
        Color shadowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 65);
        g.setColor(shadowColor);
        g.fillOval(x, y, radius * 2, radius * 2);
    }

    public static void drawString(JComponent c, Graphics g, String text, Rectangle textRect, int mnemonicIndex, int shiftOffset, Color color) {
        g = MaterialDrawingUtils.getAliasedGraphics(g);
        FontMetrics fm = g.getFontMetrics(c.getFont());

        g.setColor(color);
        BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                textRect.x + shiftOffset,
                textRect.y + fm.getAscent() + shiftOffset);
    }

    private static Map<RenderingHints.Key, Object> putMissedHints(Map<RenderingHints.Key, Object> hints) {
        if (hints == null) {
            return defaultHints;
        }
        for (Map.Entry<RenderingHints.Key, Object> entry : defaultHints.entrySet()) {
            if (!hints.containsKey(entry.getKey())) {
                hints.put(entry.getKey(), entry.getValue());
            }
        }
        return hints;
    }
}
