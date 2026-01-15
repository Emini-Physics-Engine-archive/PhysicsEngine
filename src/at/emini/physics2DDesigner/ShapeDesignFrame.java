package at.emini.physics2DDesigner;

import javax.swing.JFrame;

public class ShapeDesignFrame extends JFrame {

    private static final long serialVersionUID = -4589803568208155474L;

    private ShapeDesigner designer;
    
    public ShapeDesignFrame(ShapeDesigner designer)
    {
        this.designer = designer;
        
        setTitle( "Design shape: " + designer.getShape().getName());
        
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        getContentPane().add(designer);
        pack();
    }
    
    public DesignShape getPhyShape()
    {
        return designer.getShape();
    }
    
}
