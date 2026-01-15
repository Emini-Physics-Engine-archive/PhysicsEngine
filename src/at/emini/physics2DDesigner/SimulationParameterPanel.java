package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.emini.physics2D.World;

public class SimulationParameterPanel extends JPanel implements ChangeListener
{
  private static final long serialVersionUID = -3509518653767380334L;
  
  JSpinner mConstraintIt;
  JSpinner mPosConstraintIt;
  
  
  public SimulationParameterPanel()
  {
      initComponents();
  }

  private void initComponents()
  {
      setLayout(new BorderLayout());
      JPanel hold = new JPanel();
      hold.setLayout(new GridLayout(2,2));
            
      mConstraintIt = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
      mPosConstraintIt = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));

      mConstraintIt.addChangeListener(this);
      mPosConstraintIt.addChangeListener(this);
      
      hold.add(new JLabel("Constraint iteration"));
      hold.add(mConstraintIt);
      hold.add(new JLabel("Pos. Constraint iteration"));
      hold.add(mPosConstraintIt);
      
      add(hold, BorderLayout.NORTH);
  }  

    private void updateParameter()
    {
        //World.setConstraintIterations(((SpinnerNumberModel) mConstraintIt.getModel()).getNumber().intValue() ); 
        //World.setPositionConstraintIterations(((SpinnerNumberModel) mPosConstraintIt.getModel()).getNumber().intValue() ); 
    }

    @Override
    public void stateChanged(ChangeEvent evt)
    {
        updateParameter();
    }
}