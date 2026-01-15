package at.emini.physics2DDesigner;

import java.awt.Color;
import java.io.File;
import java.util.Vector;

import at.emini.physics2D.UserData;

public interface DesignShape
{

    public abstract void setName(String name);

    public abstract String getName();

    public abstract Color getColor();

    public abstract void setColor(Color c);

    public abstract Color getOpaqueColor();

    public abstract void draw(GraphicsWrapper g, boolean edit);

    public abstract void saveToFile(File file);

    public abstract void saveToFile(MyFileWriter fileWriter, Vector worldshapes);

    public abstract int getBoundingRadiusSquare();

    public abstract void setMassFX(int massFX);

    public abstract void setElasticityFX(int elasticityFX);

    public abstract void setFrictionFX(int frictionFX);

    public abstract UserData getUserData();

    public abstract int getId();

    public abstract int getMassFX();

    public abstract int getElasticityFX();

    public abstract int getFrictionFX();

    public abstract void correctCentroid();

}