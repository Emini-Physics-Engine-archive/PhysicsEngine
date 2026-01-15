package at.emini.physics2DVisualTest;

public class TrigFXCalculator
{

    public int calcPi(int decimals)
    {
        return (int) (Math.PI * (1 << decimals));
    }

    public int[] calcSin(int granularityDecimals, int decimals)
    {
        int count = (int) (Math.PI * 2.0 * (1 << granularityDecimals));
        int[] sinVals = new int[count];

        for( int i = 0; i < count; i++)
        {
            double angle = (Math.PI * 2.0 * i / count);
            sinVals[i] = (int) (Math.sin(angle) * (1 << decimals));
        }

        return sinVals;
    }

    public static void main(String[] args)
    {
        TrigFXCalculator calculator = new TrigFXCalculator();

        //System.out.println("Pi: " + calculator.calcPi(24));
        //System.out.println("Pi: " + ((double) calculator.calcPi(24) / (1 << 24)));

        int[] sinVals = calculator.calcSin(8, 15);

        for( int i = 0; i < sinVals.length; i++)
        {
            System.out.print(sinVals[i] + ",");
            if ( i % (16) == 0)
            {
                System.out.print("\n");
            }
        }
    }
}
