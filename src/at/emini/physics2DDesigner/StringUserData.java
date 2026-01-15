package at.emini.physics2DDesigner;

import java.io.IOException;

import at.emini.physics2D.UserData;

public class StringUserData implements UserData
{
    private String data = "";

    public StringUserData()
    {
    }

    public StringUserData(StringUserData userData)
    {
        data = userData.data;
    }

    public StringUserData copy()
    {
        return new StringUserData(this);
    }

    public StringUserData createNewUserData(String data, int type)
    {
        StringUserData userData = new StringUserData();
        userData.data = data;
        return userData;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public static void writeToStream(MyFileWriter writer, StringUserData userData) throws IOException
    {
        if (userData == null)
        {
            writer.writeInt(0);
        }
        else
        {
            byte[] bytes = userData.data.getBytes("UTF-8");
            writer.writeInt(bytes.length);
            writer.write(bytes, 0, bytes.length);
        }
    }

}
