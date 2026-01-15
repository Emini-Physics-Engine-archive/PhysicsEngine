package at.emini.physics2D;

/**
 * Interface for additional data for bodies.
 * Implement this interface if bodies need custom data. 
 * 
 * @author Alexander Adensamer
 */
public interface UserData
{
    
    public static final int TYPE_BODY       = 1;
    public static final int TYPE_SHAPE      = 2;
    public static final int TYPE_EVENT      = 3;
    public static final int TYPE_CONSTRAINT = 4;
    public static final int TYPE_PARTICLE   = 5;
    public static final int TYPE_WORLD      = 6;
    
    /**
     * Fills the userdata object
     * The string is read from the world file and passed to this method to create
     * the user data object.
     * Can return null.  
     * @param data the string to parse  
     * @param type the type of item that this used data belongs to
     * @return new userdata object 
     */
    public UserData createNewUserData(String data, int type);
    
    /**
     * Copy method for the user data
     * @return a deep copy of the user data
     */
    public UserData copy();
}
