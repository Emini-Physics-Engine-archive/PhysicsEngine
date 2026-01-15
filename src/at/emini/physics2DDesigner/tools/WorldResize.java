package at.emini.physics2DDesigner.tools;

import java.io.File;


import at.emini.physics2D.util.PhysicsFileReader;
import at.emini.physics2DDesigner.DesignWorld;
import at.emini.physics2DDesigner.StringUserData;

public class WorldResize
{
    static final String arg_help = "-help";
    static final String arg_scale = "-scale";
    static final String arg_file = "-file";
    static final String arg_out = "-out";

    public static void main(String[] args)
    {
        boolean helpFound = false;
        String filename = "";
        String newfilename = "";
        String scaleStr = "";
        for( int i = 0; i< args.length; i++)
        {
            if (args[i].equals(arg_scale))
            {
                i++;
                if (i >= args.length) break;
                scaleStr = args[i];
                continue;
            }
            if (args[i].equals(arg_file))
            {
                i++;
                if (i >= args.length) break;
                filename = args[i];
                continue;
            }
            if (args[i].equals(arg_out))
            {
                i++;
                if (i >= args.length) break;
                newfilename = args[i];
                continue;
            }
            if (args[i].equals(arg_help)) helpFound = true;
        }

        //check parameters
        if (helpFound || !(scaleStr.length() != 0 && filename.length() != 0))
        {
            displayHelp();
            System.exit(0);
        }

        //start rescaling
        DesignWorld world = null;
        File file = null;
        try
        {
            file = new File(filename);
            world = DesignWorld.loadFromFile(file);
        }
        catch(Exception e)
        {
            System.out.println("The file could not be loaded!\n");
            System.exit(1);
        }

        double scale = 0.0;
        try
        {
            scale = Double.parseDouble(scaleStr);
        }
        catch(Exception e)
        {
            System.out.println("Invalid scalefactor!\n");
            System.exit(1);
        }

        if (world == null)
        {
            System.out.println("An unexpected Error has occurred!\n");
            System.exit(1);
        }

        world.scale((float) scale);

        //create new filename
        if (newfilename.length() == 0)
        {
            newfilename = filename.substring(0, filename.lastIndexOf("."));
            newfilename+= "_new.phy";
        }

        File newfile = null;
        try {
            newfile = new File(newfilename);
            newfile.createNewFile();
            world.saveToFile(newfile);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Scaled File saved as: " + newfile + "\n");
    }

    private static void displayHelp()
    {
        System.out.println(
        "Emini WorldResize\n"+
        "Usage:\n"+
        "      "+arg_help+" : display this help\n"+
        "      "+arg_file+" <filename> : the filename containing the world to rescale\n"+
        "      "+arg_scale+" <scalefactor> : the factor for scaling\n"+
        " opt: "+arg_out+"  <filename> : the target filename of the scaled world\n"+
        " Both scale and file must be supplied.\n"+
        "\n"
        );
    }


}
