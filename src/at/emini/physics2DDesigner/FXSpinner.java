package at.emini.physics2DDesigner;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import at.emini.physics2D.util.FXUtil;

public class FXSpinner extends JSpinner
{

    private static final long serialVersionUID = -7696723586493843962L;

    private int fxDecimals = FXUtil.DECIMAL;

    public FXSpinner(int initialValueFX, int decimals)
    {
        super(new SpinnerNumberModel(initialValueFX / (double) (1 << decimals), -10000.0, 10000.0, 1.0));
        fxDecimals = decimals;
    }

    public FXSpinner(int initialValueFX, double min, double max, int decimals)
    {
        super(new SpinnerNumberModel(initialValueFX / (double) (1 << decimals), min, max, 1.0));
        fxDecimals = decimals;
    }

    public FXSpinner(int initialValueFX, double min, double max, double step, int decimals)
    {
        super(new SpinnerNumberModel(initialValueFX / (double) (1 << decimals), min, max, step));
        fxDecimals = decimals;
    }

    public void setDecimals(int decimals)
    {
        fxDecimals = decimals;
    }

    public int getValueFX()
    {
        return (int) ( ((SpinnerNumberModel)getModel()).getNumber().doubleValue() * (1 << fxDecimals));
    }

    public void setValueFX(int valueFX)
    {
        getModel().setValue(new Double( valueFX / (double) (1 << fxDecimals)) );
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(40,20);
    }
}
