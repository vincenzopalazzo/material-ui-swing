package mdlaf.shadows;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.ConstructorProperties;
import java.util.Objects;

import javax.swing.JPopupMenu;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;

import mdlaf.utils.MaterialConstants;

public class TextFieldBorder extends AbstractBorder {
    private static final long serialVersionUID = 1L;
    protected int left, right, top, bottom;
    protected int arch = 12; // default value
    protected float withBorder = 1.2f;

    /**
     * Creates an empty border with the specified insets.
     * 
     * @param top    the top inset of the border
     * @param left   the left inset of the border
     * @param bottom the bottom inset of the border
     * @param right  the right inset of the border
     */
    public TextFieldBorder(int top, int left, int bottom, int right) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    /**
     * Creates an empty border with the specified insets.
     * 
     * @param borderInsets the insets of the border
     */
    @ConstructorProperties({ "borderInsets" })
    public TextFieldBorder(Insets borderInsets) {
        this.top = borderInsets.top;
        this.right = borderInsets.right;
        this.bottom = borderInsets.bottom;
        this.left = borderInsets.left;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (c instanceof JTextComponent) {
            JTextComponent tf = (JTextComponent) c;
            TextUI ui = tf.getUI();
            if (ui instanceof TextFieldStyle) {
            	TextFieldStyle mui = (TextFieldStyle) ui;
                MaterialConstants.TextComponent textFieldStyle = mui.getTextFieldStyle();
                if (textFieldStyle == null)
                    return;

                if (textFieldStyle == MaterialConstants.TextComponent.TEXT_FIELD_STYLE_NONE
                        || textFieldStyle == MaterialConstants.TextComponent.TEXT_FIELD_STYLE_LINE)
                    return;

                Color disabledBackground = mui.getDisabledBackground();
                Color colorLine = mui.getColorLine();
                // Color colorLineActive = mui.colorLineActive();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setStroke(new BasicStroke(withBorder));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (textFieldStyle == MaterialConstants.TextComponent.TEXT_FIELD_STYLE_OUTLINE) {
                    int r = arch;
                    int w = width - 1;
                    int h = height - 1;

                    Area round = new Area(new RoundRectangle2D.Float(x, y, w, h, r, r));
                    if (c instanceof JPopupMenu) {
                        g2.setPaint(c.getBackground());
                        g2.fill(round);
                    } else {
                        Container parent = c.getParent();
                        if (Objects.nonNull(parent)) {
                            g2.setPaint(parent.getBackground());
                            Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
                            corner.subtract(round);
                            g2.fill(corner);
                        }
                    }
                    if (tf.isEnabled())
                        g2.setPaint(colorLine);
                    else
                        g2.setPaint(disabledBackground);
                    g2.draw(round);
                } else if (textFieldStyle == MaterialConstants.TextComponent.TEXT_FIELD_STYLE_BORDER_LINE) {
                    if (tf.isEnabled())
                        g2.setPaint(colorLine);
                    else
                        g2.setPaint(disabledBackground);
                    g2.drawRect(x, y + height - 1, width, 1);
                }

                g2.dispose();
            }
        }
    }

    /**
     * Reinitialize the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = left;
        insets.top = top;
        insets.right = right;
        insets.bottom = bottom;
        if (c != null && c instanceof JTextComponent) {
            Insets margin = null;
            if (c instanceof JTextComponent) {
                margin = ((JTextComponent) c).getMargin();
            }
            if (margin != null) {
                insets.top += margin.top;
                insets.left += margin.left;
                insets.bottom += margin.bottom;
                insets.right += margin.right;
            }
        }

        return insets;
    }
    
    /**
     * Returns the insets of the border.
     * @since 1.3
     */
    public Insets getBorderInsets() {
        return new Insets(top, left, bottom, right);
    }
}
