package at.emini.physics2DDesigner;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Acts as Float duplicate of FXSpinner -> is used for automatic float conversion
 *
 * @author Alexander Adensamer
 */
public class FloatSpinner extends JSpinner {

    private static final long serialVersionUID = -7696723586493843963L;

    public FloatSpinner(float initialValueFX, int decimals)
    {
        super(new SpinnerNumberModel(initialValueFX, -10000.0, 10000.0, 1.0));
    }

    public FloatSpinner(float initialValueFX, double min, double max, int decimals)
    {
        super(new SpinnerNumberModel(initialValueFX, min, max, 1.0));
    }

    public FloatSpinner(float initialValueFX, double min, double max, double step, int decimals)
    {
        super(new SpinnerNumberModel(initialValueFX, min, max, step));
    }

    public void setDecimals(int decimals)
    {
    }

    public float getValueFX()
    {
        return (float) ((SpinnerNumberModel)getModel()).getNumber().doubleValue();
    }

    public void setValueFX(float valueFX)
    {
        getModel().setValue(new Double( valueFX) );
    }
}
