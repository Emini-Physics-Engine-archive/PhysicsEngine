package at.emini.physics2DDesigner;


public interface WorldChangeListener
{
    public void worldChanged(DesignWorld w);
    public void updateRequired();
}
