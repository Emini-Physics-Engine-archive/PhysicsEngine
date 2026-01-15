package at.emini.physics2DDesigner;

import java.util.LinkedList;

public class UndoManager
{
    private int historySize = 10;

    private LinkedList undoHistory;
    private int position = -1;

    public UndoManager(int historySize)
    {
        this.historySize = historySize;
        undoHistory = new LinkedList();
    }

    public void addUndoElement(DesignWorld world)
    {
        if (position + 1 == historySize && undoHistory.size() == historySize)
        {
            undoHistory.remove(0);
        }
        else if (undoHistory.size() == position + 1)
        {
            position++;
        }
        else    //we have a redo history in front of the current position
                //we have to delete it
        {
            for( int i = undoHistory.size() - 1; i > position; i--)
            {
                undoHistory.removeLast();
            }
            position++;
        }

        undoHistory.add(world);
    }

    public DesignWorld getUndoElement()
    {
        if (position == 0)
        {
            return null;
        }
        position--;
        return (DesignWorld) undoHistory.get(position);
    }

    public DesignWorld getRedoElement()
    {
        if (position + 1 == undoHistory.size())
        {
            return null;
        }
        position++;
        return (DesignWorld) undoHistory.get(position);
    }

    public boolean hasUndoElement()
    {
        return position > 0;
    }

    public boolean hasRedoElement()
    {
        return position + 1 < undoHistory.size();
    }

    public void reset(DesignWorld world)
    {
        position = 0;
        undoHistory.clear();
        undoHistory.add(world);
    }
}
