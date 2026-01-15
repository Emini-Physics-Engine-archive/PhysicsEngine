package at.emini.physics2DDesigner.tools;

import java.io.File;

import at.emini.physics2D.Body;
import at.emini.physics2DDesigner.DesignWorld;
import at.emini.physics2DDesigner.StringUserData;

public class WorldConvert
{
    static final String arg_help = "-help";
    static final String arg_file = "-file";
    static final String arg_out = "-out";
    
    public static void main(String[] args)
    {
        boolean helpFound = false;
        String filename = "";
        String newfilename = "";
        for( int i = 0; i< args.length; i++)
        {
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
        if (helpFound || filename.length() == 0)
        {
            displayHelp();
            System.exit(0);
        }
        
        //start converting
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
        
        performConversion(world);        
                
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
        "Emini WorldConvert\n"+
        "Usage:\n"+
        "      "+arg_help+" : display this help\n"+
        "      "+arg_file+" <filename> : the filename containing the world to convert\n"+
        " opt: "+arg_out+"  <filename> : the target filename of the scaled world\n"+
        "\n"
        );
    }
    
    private static void performConversion(DesignWorld world)
    {
        int bodyCount = world.getBodyCount();
        Body[] bodies = world.getBodies();
        for( int i = bodyCount - 1; i >= 0 ; i--)
        {
            String shapeString = ((StringUserData) bodies[i].shape().getUserData()).getData();
            if (! bodies[i].isDynamic() && 
                ! bodies[i].isInteracting() && 
                ! shapeString.equals("item"))
            {
                String addData = ",";
                addData += shapeString;
                addData += "," + bodies[i].positionFX().xFX;
                addData += "," + bodies[i].positionFX().yFX;
                world.removeBody(bodies[i]);
                
                StringUserData worldData = ((StringUserData) world.getUserData());
                String orig = worldData.getData();
                worldData.setData(orig + addData);
            }      
        }
    }
}
