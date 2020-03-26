/**
 * MIT License
 *
 * Copyright (c) 2018-2020 atharva washimkar, Vincenzo Palazzo vincenzopalazzo1996@gmail.com
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
package mdlaf.components.formattertextfield;

import mdlaf.shadows.TextFieldStyle;
import mdlaf.utils.MaterialConstants;
import mdlaf.utils.MaterialDrawingUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author https://github.com/vincenzopalazzo
 */
public class MaterialFormattedTextFieldUI extends BasicFormattedTextFieldUI implements TextFieldStyle{
	
	protected static final String ProprietyPrefix = "FormattedTextField";
	
    protected static final String PROPERTY_LINE_COLOR = "lineColor";
    protected static final String PROPERTY_SELECTION_COLOR = "selectionColor";
    protected static final String PROPERTY_SELECTION_TEXT_COLOR = "selectedTextColor";
    protected static final String PROPERTY_ENABLED_COMPONENT = "enabled";
    protected static final String PROPERTY_ANCESTOR = "ancestor";

	protected MaterialConstants.TextComponent textFieldStyle;
	protected boolean focused;
	protected JTextComponent textComponent;
	protected Color background;
	protected Color foreground;
	protected Color activeBackground;
	protected Color activeForeground;
	protected Color inactiveBackground;
	protected Color inactiveForeground;
	protected Color disabledBackground;
	protected Color disabledForeground;
	protected Color colorLineInactive;
    protected Color colorLineActive;
    protected Color colorLine;
    protected FocusListener focusListenerColorLine;
    protected PropertyChangeListener propertyChangeListener;
    protected PropertyChangeSupport propertyChangeSupport;
	
    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
    public static ComponentUI createUI(JComponent c) {
        return new MaterialFormattedTextFieldUI();
    }
    
    public MaterialFormattedTextFieldUI() {
    	this((MaterialConstants.TextComponent)UIManager.get(ProprietyPrefix + "[Line].styleType"));
    }
    
    public MaterialFormattedTextFieldUI(MaterialConstants.TextComponent textFieldStyle) {
    	super();
    	if(textFieldStyle == null)
    		 textFieldStyle = MaterialConstants.TextComponent.TEXT_FIELD_STYLE_LINE;
    	this.textFieldStyle = textFieldStyle;
    	this.focusListenerColorLine = new FocusListenerColorLine();
        this.propertyChangeListener = new MaterialPropertyChangeListener();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    @Override
    protected String getPropertyPrefix() {
        return ProprietyPrefix;
    }
    
    protected void logicForChangeColorOnFocus(JComponent component, Color background, Color foreground) {
        if (background == null || foreground == null) {
            return;
        }
        JTextComponent componentText = (JTextField) component;
        componentText.setSelectedTextColor(foreground);
        componentText.setSelectionColor(background);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JFormattedTextField formattedTextField = (JFormattedTextField) c;
        //formattedTextField.setSelectionColor(UIManager.getColor(getPropertyPrefix() + ".selectionBackground"));
        //formattedTextField.setSelectedTextColor(UIManager.getColor(getPropertyPrefix() + ".selectionForeground"));
        formattedTextField.setBackground(UIManager.getColor(getPropertyPrefix() + ".inactiveBackground"));
        //formattedTextField.setForeground(UIManager.getColor(getPropertyPrefix() + ".inactiveForeground"));
        //formattedTextField.setFont(UIManager.getFont(getPropertyPrefix() + ".font"));
        //formattedTextField.setBorder(UIManager.getBorder(getPropertyPrefix() + ".border"));
        installMyDefaults(c);
    }
    
    protected void installMyDefaults(JComponent component) {
    	textComponent = (JTextComponent) component;
        this.background = UIManager.getColor(getPropertyPrefix() + ".background");
        this.foreground = UIManager.getColor(getPropertyPrefix() + ".foreground");
        this.activeBackground = UIManager.getColor(getPropertyPrefix() + ".selectionBackground");
        this.activeForeground = UIManager.getColor(getPropertyPrefix() + ".selectionForeground");
        this.inactiveBackground = UIManager.getColor(getPropertyPrefix() + ".inactiveBackground");
        this.inactiveForeground = UIManager.getColor(getPropertyPrefix() + ".inactiveForeground");
        this.disabledBackground = UIManager.getColor(getPropertyPrefix() + ".disabledBackground");
        this.disabledForeground = UIManager.getColor(getPropertyPrefix() + ".disabledForeground");
        textComponent.setDisabledTextColor(disabledForeground);
        colorLineInactive = UIManager.getColor(getPropertyPrefix() + "[Line].inactiveColor");
        colorLineActive = UIManager.getColor(getPropertyPrefix() + "[Line].activeColor");
        textComponent.setFont(UIManager.getFont(getPropertyPrefix() + ".font"));
        colorLine = getComponent().hasFocus() && getComponent().isEditable() ? colorLineActive : colorLineInactive;
        textComponent.setSelectionColor(getComponent().hasFocus() && getComponent().isEnabled() ? activeBackground : inactiveBackground);
        textComponent.setSelectedTextColor(getComponent().hasFocus() && getComponent().isEnabled() ? activeForeground : inactiveForeground);
        textComponent.setForeground(getComponent().hasFocus() && getComponent().isEnabled() ? activeForeground : inactiveForeground);
        textComponent.setBorder(UIManager.getBorder(getPropertyPrefix() + ".border"));
    }
    
    @Override
    protected void installDefaults() {
        super.installDefaults();
    }

    @Override
    public void uninstallUI(JComponent c) {

        JFormattedTextField formattedTextField = (JFormattedTextField) c;
        formattedTextField.setSelectionColor(null);
        formattedTextField.setSelectedTextColor(null);
        formattedTextField.setBackground(null);
        formattedTextField.setForeground(null);
        formattedTextField.setFont(null);
        formattedTextField.setBorder(null);

        super.uninstallUI(c);
    }
    
    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        getComponent().setBorder(null);
    }
    
    @Override
    protected void installListeners() {
        super.installListeners();
        getComponent().addFocusListener(focusListenerColorLine);
        getComponent().addPropertyChangeListener(propertyChangeListener);
        propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }
    
    @Override
    protected void uninstallListeners() {
        getComponent().removeFocusListener(focusListenerColorLine);
        getComponent().removePropertyChangeListener(propertyChangeListener);
        propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
        super.uninstallListeners();
    }

    protected void parentChanged(Component parent) {
        if(parent instanceof JTable) {
            MaterialConstants.TextComponent lineStyle = (MaterialConstants.TextComponent)UIManager.get("Table[TextField].lineStyleType");
            if(lineStyle == null)
                lineStyle = MaterialConstants.TextComponent.TEXT_FIELD_STYLE_NONE;
            
            this.textFieldStyle = lineStyle;
        }
    }
    
    protected void logicForPropertyChange(Color newColor, boolean isForeground) {
        if (newColor == null) {
            return;
        }
        if (isForeground) {
            if (!newColor.equals(this.activeForeground)) {
                this.inactiveForeground = newColor;
            } else {
                this.activeForeground = newColor;
            }
            getComponent().repaint();
        }
        if (!isForeground) {
            if (!newColor.equals(this.activeBackground)) {
                this.inactiveBackground = newColor;
            } else {
                this.activeBackground = newColor;
            }
            getComponent().repaint();
        }
    }
    
    protected void changeColorOnFocus(Graphics g) {
        boolean hasFocus = focused;
        JTextComponent c = getComponent();
        if (c == null) {
            return;
        }
        if (hasFocus && (activeBackground != null) && (activeForeground != null)) {
            logicForChangeColorOnFocus(c, activeBackground, activeForeground);
            //TODO create a new changePropriety
            paintLine(g);
        }

        if (!hasFocus && (inactiveBackground != null) && (inactiveForeground != null)) {
            logicForChangeColorOnFocus(c, inactiveBackground, inactiveForeground);
            paintLine(g);
        }
    }
    
    protected synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if ((propertyName == null || propertyName.isEmpty()) || oldValue == null || newValue == null) {
            throw new IllegalArgumentException("Some property null");
        }

        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    protected void paintBackground(Graphics g) {
        super.paintBackground(MaterialDrawingUtils.getAliasedGraphics(g));
    }

    @Override
    public void update(Graphics g, JComponent c) {
        super.update(MaterialDrawingUtils.getAliasedGraphics(g), c);
    }

    @Override
    public void paintSafely(Graphics g) {
        super.paintSafely(g);
        paintLine(g);
        changeColorOnFocus(g);
    }
    
    protected void paintLine(Graphics graphics) {
        if (graphics == null) {
            return;
        }
        JTextComponent c = getComponent();
        if (getTextFieldStyle() == MaterialConstants.TextComponent.TEXT_FIELD_STYLE_LINE) {
            int x = c.getInsets().left;
            int y = c.getHeight();
            if(c.getInsets().bottom > 0)
                y -= (c.getInsets().bottom + 1);
            else
                y --;
            
            int w = c.getWidth() - c.getInsets().left - c.getInsets().right;
            if(textComponent.isEnabled()){
                graphics.setColor(colorLine);
            }else{
                graphics.setColor(disabledBackground);
            }
            graphics.fillRect(x, y, w, 1);
        }
    }
    
    protected class FocusListenerColorLine implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            firePropertyChange(PROPERTY_LINE_COLOR, colorLineInactive, colorLineActive);
            firePropertyChange(PROPERTY_SELECTION_COLOR, inactiveBackground, activeBackground);
            firePropertyChange(PROPERTY_SELECTION_TEXT_COLOR, inactiveForeground, activeForeground);
            focused = true;
        }

        @Override
        public void focusLost(FocusEvent e) {
            firePropertyChange(PROPERTY_LINE_COLOR, colorLineActive, colorLineInactive);
            firePropertyChange(PROPERTY_SELECTION_COLOR, activeBackground, inactiveBackground);
            firePropertyChange(PROPERTY_SELECTION_TEXT_COLOR, activeForeground, inactiveForeground);
            focused = false;
        }
    }

    protected class MaterialPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (getComponent() == null) {
                return;
            }

            if (pce.getPropertyName().equals(PROPERTY_SELECTION_COLOR)) {
                Color newColor = (Color) pce.getNewValue();
                logicForPropertyChange(newColor, false);
            } else if (pce.getPropertyName().equals(PROPERTY_SELECTION_TEXT_COLOR)) {
                Color newColor = (Color) pce.getNewValue();
                logicForPropertyChange(newColor, true);
            } else if (pce.getPropertyName().equals(PROPERTY_LINE_COLOR)) {
                Color newColor = (Color) pce.getNewValue();
                colorLine = newColor;
                getComponent().repaint();
            } else if (pce.getPropertyName().equals("background")) {
                getComponent().repaint();
            } else if (pce.getPropertyName().equals(PROPERTY_ENABLED_COMPONENT)){
                boolean newValue = (boolean) pce.getNewValue();
                if(!newValue){
                    getComponent().setSelectionStart(0);
                    getComponent().setSelectionEnd(0);
                }
                getComponent().repaint();
            }
            else if (pce.getPropertyName().equals(PROPERTY_ANCESTOR)) {
                if(pce.getNewValue() != null) {
                    parentChanged((Component)pce.getNewValue());
                }
            }
        }
    }
    
    @Override
    public MaterialConstants.TextComponent getTextFieldStyle() {
       return textFieldStyle;
    }
    
    @Override
    public Color getDisabledBackground() {
       return disabledBackground;
    }
    
    @Override
    public Color getColorLine() {
       return colorLine;
    }
    
    public Color colorLineActive() {
       return colorLineActive;
    }
}
