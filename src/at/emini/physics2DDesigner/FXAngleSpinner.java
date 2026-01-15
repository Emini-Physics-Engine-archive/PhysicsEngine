package at.emini.physics2DDesigner;

import at.emini.physics2D.util.FXUtil;

public class FXAngleSpinner extends FXSpinner 
{

    private static final long serialVersionUID = -7696723586493843963L;
    
    private double radToDeg = 180.0 / (Math.PI * (1 << FXUtil.DECIMAL)); 
    
    public FXAngleSpinner()
    {
        super(0, 0.0, 360.0, 5, FXUtil.DECIMAL);
    }
    
    public int getValueFX()
    {
        return (int) (super.getValueFX() / radToDeg);   //#FX2F return (int) (super.getValueFX() / radToDeg);
    }
    
    public void setValueFX(int valueFX)
    {
        super.setValueFX( (int) ((double) valueFX * radToDeg)); //#FX2F super.setValueFX( (int) ((double) valueFX * radToDeg));
    }
    
}
