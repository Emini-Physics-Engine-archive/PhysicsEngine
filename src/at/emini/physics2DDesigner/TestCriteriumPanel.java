package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;

/**
 * Class is required to create a test from a designed world
 * @author Alexander Adensamer
 *
 */
public class TestCriteriumPanel extends JPanel
{

    private static final long serialVersionUID = -7550467336358780619L;

    public JComboBox event;
    public JCheckBox mustOccur;
    public JSpinner rangeStart;
    public JSpinner rangeEnd;

    public TestCriteriumPanel(String[] eventList)
    {
        setLayout(new GridLayout(2,3));
        setBorder(new LineBorder(Color.BLACK));

        initComponents(eventList);
    }

    private void initComponents(String[] eventList)
    {
        event = new JComboBox(eventList);
        mustOccur = new JCheckBox("Must occur");

        rangeStart = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 20));
        rangeEnd = new JSpinner(new SpinnerNumberModel(20, 0, 10000, 20));

        add(new JLabel("Event"));
        add(event);
        add(mustOccur);

        add(new JLabel("in range"));
        add(rangeStart);
        add(rangeEnd);
    }

    public Dimension getMaximumSize()
    {
        return new Dimension(800, 50);
    }

    public String getCodeLine()
    {
        String code = "        addTestCriterium(";
        code += event.getSelectedItem().toString();
        code += ", ";
        code += mustOccur.isSelected() ?
                "TestEventListener.MUST_OCCUR, " :
                "TestEventListener.MUST_NOT_OCCUR, " ;
        code += rangeStart.getModel().getValue().toString();
        code += ", ";
        code += rangeEnd.getModel().getValue().toString();
        code += ");\n";

        return code;
    }


}
