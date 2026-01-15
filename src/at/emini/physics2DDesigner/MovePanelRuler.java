package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

public class MovePanelRuler extends JComponent {
    
    private static final long serialVersionUID = -2383747305945993497L;
    
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SIZE = 40;

    public int orientation;
   
    AffineTransform transform;
    
    public MovePanelRuler(int o) {
        orientation = o;        
    }
    
    public void setTransform(AffineTransform transform) 
    {
        this.transform = transform;
    }
   
    public Dimension getPreferredSize()
    {
        if (orientation == HORIZONTAL)
        {
            return new Dimension(100, SIZE);
        }
        else
        {
            return new Dimension(SIZE, 100);
        }
    }

    public void paint(Graphics graphics) 
    {
        if (graphics == null)
        {
            return;
        }
        GraphicsWrapper g = new GraphicsWrapper((Graphics2D) graphics);
        
        //calculate increment from transform
        double increment = (1.0/ (orientation == HORIZONTAL ? transform.getScaleX() : transform.getScaleY()) );
        float scale = (float) Math.log10(increment);
        float effectiveScale = scale - ((int) scale);
        int labelScale = (int) scale;   //#FX2F int labelScale = (int) scale;
        if (effectiveScale > 0) 
        {
            effectiveScale -= 1;
            labelScale += 1;
        }
        
        double step = Math.pow(10, - effectiveScale + 1);
        
        int zeroLine = orientation == HORIZONTAL ? (int) (transform.getTranslateX() + SIZE) : (int) (transform.getTranslateY() ); //#FX2F int zeroLine = orientation == HORIZONTAL ? (int) (transform.getTranslateX() + SIZE) : (int) (transform.getTranslateY() );
        
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);
        
        int cnt = 0;
        double pos = 0.0;
        int max = orientation == HORIZONTAL ? getWidth() : getHeight();
        int cntStart = - (int) ((zeroLine/step) + 0.5); //#FX2F int cntStart = - (int) ((zeroLine/step) + 0.5);
        double start = zeroLine + cntStart * step ;
        for(pos = start, cnt = cntStart; pos < max; pos+= step, cnt++)
        {
            if (cnt == 0)
                g.setColor(Color.red);
            else
                g.setColor(Color.black);
            drawLine(g, (int) pos, cnt, labelScale); //#FX2F drawLine(g, (int) pos, cnt, labelScale);            
        }
    }
    
    private void drawLine(GraphicsWrapper g, int pos, int cnt, int scale)
    {
        int size = SIZE / 4;
        if (cnt % 5 == 0) size = SIZE / 2;
        if (cnt % 10 == 0) size = SIZE;
        
        if (orientation == HORIZONTAL)
        {
            g.drawLine(pos, 0, pos, size);
        }
        else
        {
            g.drawLine(0, pos, size, pos);
        }  
        
        if (cnt % 5 == 0)
        {
            String label = String.valueOf(Math.pow(10, scale + 1) * cnt);
            if (orientation == HORIZONTAL)
            {
                g.drawString(label, pos + 5, SIZE);
            }
            else
            {
                g.drawString(label, SIZE/2 - 10, pos - 5);
            }
        }
    }
}

