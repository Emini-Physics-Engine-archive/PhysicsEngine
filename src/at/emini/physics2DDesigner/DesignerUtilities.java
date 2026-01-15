package at.emini.physics2DDesigner;

import java.awt.Color;

public class DesignerUtilities
{

    public static final Color getGrayBlendColor(Color c)
    {
        Color refColor = new Color(200,200,200);
        Color colorNew = new Color(
                (int) (c.getRed() * c.getAlpha() / 255.0 + refColor.getRed() * (255.0 - c.getAlpha()) / 255.0),      //#FX2F (int) (c.getRed() * c.getAlpha() / 255.0 + refColor.getRed() * (255.0 - c.getAlpha()) / 255.0),
                (int) (c.getGreen() * c.getAlpha() / 255.0 + refColor.getGreen() * (255.0 - c.getAlpha()) / 255.0),  //#FX2F (int) (c.getGreen() * c.getAlpha() / 255.0 + refColor.getGreen() * (255.0 - c.getAlpha()) / 255.0),
                (int) (c.getBlue() * c.getAlpha() / 255.0 + refColor.getBlue() * (255.0 - c.getAlpha()) / 255.0));   //#FX2F (int) (c.getBlue() * c.getAlpha() / 255.0 + refColor.getBlue() * (255.0 - c.getAlpha()) / 255.0));
        
        return colorNew;
    }
}
