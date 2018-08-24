package mdlaf.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class MaterialResurceManager {

    private ResourceBundle resourceBundle;

    public void inizializza(){
        //resourceBundle = ResourceBundle.getBundle("fontsProperties.fonts", new Locale("ar", "MA")); test
        resourceBundle = ResourceBundle.getBundle("fontsProperties.fonts", Locale.getDefault());
    }

    public String getStringResource(String key) {
        return this.resourceBundle.getString(key);
    }
}
