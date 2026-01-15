package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class TestCreationDialog extends JDialog
{
    private static final long serialVersionUID = 5685062958123365462L;

    public static String resPath = "/res/tests/";
    public static String testPath = "/src/physics2DSimulationTests/";


    private DesignWorld world;

    private JButton cancelButton;
    private JButton createButton;
    private JButton addCriteriumButton;

    private JTextField testName;
    private JTextArea testDescription;
    private JSpinner simulationTime;

    private JPanel criteria;

    private String[] eventIds;

    public TestCreationDialog(DesignWorld world)
    {
        this.world = world;
        eventIds = new String[world.eventCount()];
        for( int i = 0; i < eventIds.length; i++)
        {
            eventIds[i] = String.valueOf( world.getEvent(i).getIdentifier() );
        }

        setTitle("Create Physics Simulation Test");

        addWindowListener (new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                dispose();
            }
        });

        initComponents();
    }

    private void initComponents()
    {
        //Buttons
        JPanel hold = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 5) );

        addCriteriumButton = new JButton("Add Criterium");
        addCriteriumButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                addCriterium();
            }
        });
        hold.add(addCriteriumButton);
        hold.add(new JLabel());

        createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                createPressed();
            }
        });
        hold.add(createButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });
        hold.add(cancelButton);

        add(hold, BorderLayout.SOUTH);


        //Test name, description and sim time
        JPanel box = new JPanel(new BorderLayout(5, 5));
        JPanel box2 = new JPanel(new GridLayout( 2, 2, 5, 5));

        testName = new JTextField("Simulation Test");
        box2.add(new JLabel("Test Name:"));
        box2.add(testName);

        simulationTime = new JSpinner(new SpinnerNumberModel(20, 0, 10000, 20));
        box2.add(new JLabel("Simulation Time:"));
        box2.add(simulationTime);

        JPanel box3 = new JPanel(new GridLayout( 1, 2, 5, 5));
        testDescription = new JTextArea("Description");
        testDescription.setRows(5);
        box3.add(new JLabel("Test Description:"));
        box3.add(new JScrollPane(testDescription));

        box.add(box2, BorderLayout.NORTH);
        box.add(box3, BorderLayout.SOUTH);

        add(box, BorderLayout.NORTH);

        //Test Criteria
        criteria = new JPanel();
        criteria.setLayout(new BoxLayout(criteria, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(criteria);

        add(scrollPane, BorderLayout.CENTER);

        addCriterium();

        pack();
    }

    private void cancelPressed()
    {
        setVisible(false);
        dispose();
    }

    private void createPressed()
    {
        //create Code
        String testName = this.testName.getText();
        testName = testName.replaceAll(" ", "");    //TODO: improved regexp for classname
        String filename = testName + ".world";
        String simTime = simulationTime.getModel().getValue().toString();

        String code = createCode(testName, filename, simTime);

        //save world and test (call save dialog)
        JFileChooser chooser = new JFileChooser("Choose Physics Engine Main directory");
        chooser.setCurrentDirectory( Designer.testStdDir );
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = chooser.showSaveDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File baseDirectory = chooser.getSelectedFile();
            //save world in res
            String worldFileName = baseDirectory.getAbsolutePath() + resPath +  filename;
            File worldFile = new File(worldFileName);
            world.saveToFile(worldFile);

            String testFileName =  baseDirectory.getAbsolutePath() + testPath +  testName + ".java";
            File testFile = new File(testFileName);
            //save test file
            saveTestFile(testFile, code);
        }

        setVisible(false);
        dispose();
    }

    private void saveTestFile(File file, String text)
    {
        try {
           FileWriter outFile = new FileWriter(file);
           PrintWriter out = new PrintWriter(outFile);

           // Write text to file
           out.print(text);
           out.close();
       } catch (IOException e){
           e.printStackTrace();
       }
    }

    private void addCriterium()
    {
        criteria.add(new TestCriteriumPanel(eventIds));
        validate();
    }

    public static final String codeTemplate01  = "package physics2DSimulationTests;\n";
    public static final String codeTemplate02  = "/**\n";
    public static final String codeTemplate03  = "*\n";
    public static final String codeTemplate04  = "*/\n";
    public static final String codeTemplate05  = "public class ";            //testname
    public static final String codeTemplate05b = " extends SimulationTest {\n";
    public static final String codeTemplate06  = "    public ";              //testname
    public static final String codeTemplate06b = " (String name) {\n";
    public static final String codeTemplate07  = "        super(name);\n";
    public static final String codeTemplate08  = "        filename = \"";      //world filename
    public static final String codeTemplate08b = "\";\n";
    public static final String codeTemplate09  = "        simTime = ";       //simulation time
    public static final String codeTemplate09b = ";\n";
    public static final String codeTemplate10  = "    }\n";
    public static final String codeTemplate11  = "\n";
    public static final String codeTemplate12  = "    public void testSimulation()\n";
    public static final String codeTemplate13  = "    {\n";
    public static final String codeTemplate14  = "        performSimTest();\n";
    public static final String codeTemplate15  = "    }\n";
    public static final String codeTemplate16  = "    public static void main(String[] args)\n";
    public static final String codeTemplate17  = "    {\n";
    public static final String codeTemplate18  = "        ";
    public static final String codeTemplate18b = " test = new ";
    public static final String codeTemplate18c = "(\"\");\n";
    public static final String codeTemplate19  = "        test.startVisualTest();\n";
    public static final String codeTemplate20  = "    }\n";
    public static final String codeTemplate21  = "}\n";


    private String createCode(String testName, String filename, String simTime)
    {
        String code = "";

        code += codeTemplate01;
        code += codeTemplate02;
        code += codeTemplate03;
        code += testDescription.getText();
        code += codeTemplate04;
        code += codeTemplate05 + testName + codeTemplate05b;
        code += codeTemplate06 + testName + codeTemplate06b;
        code += codeTemplate07;
        code += codeTemplate08 + filename + codeTemplate08b;
        code += codeTemplate09 + simTime + codeTemplate09b;
        code += codeTemplate10;
        code += codeTemplate11;
        code += codeTemplate12;
        code += codeTemplate13;
        //add test criteria
        for( int i = 0; i < criteria.getComponentCount(); i++)
        {
            if (criteria.getComponent(i) instanceof TestCriteriumPanel)
            {
                code += ((TestCriteriumPanel) criteria.getComponent(i)).getCodeLine();
            }
        }
        code += codeTemplate14;
        code += codeTemplate15;
        code += codeTemplate16;
        code += codeTemplate17;
        code += codeTemplate18 + testName + codeTemplate18b + testName + codeTemplate18c;
        code += codeTemplate19;
        code += codeTemplate20;
        code += codeTemplate21;

        return code;
    }
}
