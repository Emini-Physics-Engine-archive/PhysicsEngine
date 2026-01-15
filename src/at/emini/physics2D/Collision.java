package at.emini.physics2D;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

/**
 * Detects collisions of two bodies. <br>
 * For both bodies (or body and landscape) each relevant projection direction is tested for overlap
 * (see also {@link Shape#mUniqueAxesIndices}).
 * If one direction can be determined, where a gap is between both objects,
 * there is no intersection.<br>
 * Otherwise the projection info can be used to find the collision point(s). <br>
 * Only convex polygons are considered, so it is ensured that at most two relevant
 * collision points exist.
 *
 * @author Alexander Adensamer
 */
public class Collision
{
    //temp variables
    private static FXVector M_b1b2 = new FXVector();
    private static FXVector M_tmp1 = new FXVector();
    private static FXVector M_tmp2 = new FXVector();
    private static FXVector M_tmp3 = new FXVector();
    private static FXVector M_distance = new FXVector();
    private static FXVector M_axis = new FXVector();
    private static FXVector M_separationAxis = new FXVector();
    private static FXVector M_separationAxis2 = new FXVector();
    private static FXVector M_circleAxis = new FXVector();

    private static FXVector M_relativeVelocity = new FXVector();


    private static boolean M_projectionAxisIsFromFirstPoly = false;
    private static int M_separationDistanceFX = 0;
    private static int M_absoluteDMinFX = 0;

    private static FXVector[] M_supportVertices1 = new FXVector[2];
    private static FXVector[] M_supportVertices2 = new FXVector[2];
    private static int[] M_depthDiff1FX = new int[2];                 //#FX2F private static float[] M_depthDiff1FX = new float[2];
    private static int[] M_depthDiff2FX = new int[2];                 //#FX2F private static float[] M_depthDiff2FX = new float[2];
    private static long M_dFX[] = new long[World.M_SHAPE_MAX_VERTICES]; //#FX2F private static float M_dFX[] = new float[World.M_SHAPE_MAX_VERTICES];

    private static FXVector[] M_lineVertices = new FXVector[2];
    private static FXVector[] M_lineVertexEstimates = new FXVector[2];
    static
    {
        M_lineVertexEstimates[0] = new FXVector();
        M_lineVertexEstimates[1] = new FXVector();
    }

    private static FXVector[] M_vertexPositionEstimates1;
    private static FXVector[] M_vertexPositionEstimates2;

    /**
     * Detects contacts between two bodies.
     * No more than two contact points are assumed between two (convex) shapes.
     * If the two bodies are not touching or intersecting, null is returned.
     * If a single Contact point is detected, it is returned.
     * If two contact points exist, a double contact is returned. <br>
     * The method takes any body and calls a method depending on the body shapes (circle, polygon)
     * @param b1 Body 1 of the collision
     * @param b2 Body 2 of the collision
     * @return the found contact, null if none found
     */
    public static Contact detectCollision(Body b1, Body b2)
    {
      //look for previously calculated contact
        Contact c = b1.getContact(b2);
        if (c != null && c.mIsNew)
        {
            return null;    //we already have this contact
        }

        if (b1.mShape.mVertices.length > 1 )
        {
            if (b2.mShape.mVertices.length > 1 )
            {
                return detectCollisionPolyPoly(b1, 0, b2, 0, c);
            }
            else
            {
                return detectCollisionPolyCircle(b1, 0, b2, 0, c);
            }
        }
        else
        {
            if (b2.mShape.mVertices.length > 1 )
            {
                return detectCollisionPolyCircle(b2, 0, b1, 0, c);
            }
            else
            {
                return detectCollisionCircleCircle(b1, 0, b2, 0, c);
            }
        }
    }

    public static Contact detectCollision(Body b1, int index1, Body b2, int index2)
    {
        //look for previously calculated contact
        Contact c = b1.getContact(index1, b2, index2);
        if (c != null && c.mIsNew)
        {
            return null;    //we already have this contact
        }

        boolean isPoly1 = false;
        boolean isPoly2 = false;

        if ( b1.mShape instanceof MultiShape)
        {
            isPoly1 = ((MultiShape) b1.mShape).mShapes[index1].mVertices.length > 1;
        }
        else
        {
            isPoly1 = b1.mShape.mVertices.length > 1;
        }

        if ( b2.mShape instanceof MultiShape)
        {
            isPoly2 = ((MultiShape) b2.mShape).mShapes[index2].mVertices.length > 1;
        }
        else
        {
            isPoly2 = b2.mShape.mVertices.length > 1;
        }

        if ( isPoly1 )
        {
            if (isPoly2 )
            {
                return detectCollisionPolyPoly(b1, index1, b2, index2, c);
            }
            else
            {
                return detectCollisionPolyCircle(b1, index1, b2, index2, c);
            }
        }
        else
        {
            if (isPoly1 )
            {
                return detectCollisionPolyCircle(b2, index2, b1, index1, c);
            }
            else
            {
                return detectCollisionCircleCircle(b1, index1, b2, index2, c);
            }

        }
    }

    /**
     * Detects contacts between a body and a landscape segment.
     * No more than two contact points are assumed between the convex shape and the line.
     * If the body and line are not touching or intersecting, null is returned.
     * If a single Contact point is detected, it is returned.
     * If two contact points exist, a double contact is returned. <br>
     * The method takes any body and calls a method depending on the body shape (circle, polygon)
     * @param b1 Body 1 of the collision
     * @param landscape the landscape object
     * @param index the index of the line in the landscape
     * @return the found contact, null if none found
     */
    //#NoEco /*
    protected static Contact detectCollision(Body b1, Landscape landscape, int index)
    {
      //look for previously calculated contact
        Contact c = landscape.getContact(b1, 0, index);
        if (c != null && c.mIsNew)
        {
            return null;    //we already have this contact
        }

        Contact contact;
        if (b1.mShape.mVertices.length > 1 )
        {
            contact = detectCollisionPolyLine(b1, 0,
                    landscape.getBody(), index,
                    landscape.mStartpoints[index],
                    landscape.mEndpoints[index],
                    landscape.mFaces[index], c);
        }
        else
        {
            contact = detectCollisionCircleLine(b1, 0,
                    landscape.getBody(), index,
                    landscape.mStartpoints[index],
                    landscape.mEndpoints[index],
                    landscape.mFaces[index], c);
        }

        if (contact != c)
        {
            landscape.addContact(contact);
        }
        if (contact == null && c != null)
        {
            c.mIsNew = false;    //flag for deletion
        }

        return contact;
    }
    //#NoEco */

    //#NoEco /*
    protected static Contact detectCollision(Body b1, int index1, Landscape landscape, int index)
    {
      //look for previously calculated contact
        Contact c = landscape.getContact(b1, index1, index);
        if (c != null && c.mIsNew)
        {
            return null;    //we already have this contact
        }

        boolean isPoly1 = false;

        if ( b1.mShape instanceof MultiShape)
        {
            isPoly1 = ((MultiShape) b1.mShape).mShapes[index1].mVertices.length > 1;
        }
        else
        {
            isPoly1 = b1.mShape.mVertices.length > 1;
        }

        Contact contact;
        if (isPoly1 )
        {
            contact = detectCollisionPolyLine(b1, index1,
                    landscape.getBody(), index,
                    landscape.mStartpoints[index],
                    landscape.mEndpoints[index],
                    landscape.mFaces[index], c);
        }
        else
        {
            contact = detectCollisionCircleLine(b1, index1,
                    landscape.getBody(), index,
                    landscape.mStartpoints[index],
                    landscape.mEndpoints[index],
                    landscape.mFaces[index], c);
        }

        if (contact != c)
        {
            landscape.addContact(contact);
        }
        if (contact == null && c != null)
        {
            c.mIsNew = false;    //flag for deletion
        }

        return contact;
    }
    //#NoEco */

    /**
     * Detects collision of a landscape segment and a particle.
     * @param landscape
     * @param index
     * @param xFX
     * @param yFX
     * @param xOldFX
     * @param yOldFX
     * @return collision vector at the distance that the point has to move to exactly touch
     */
    //#NoEco /*
    protected static FXVector detectCollision(Landscape landscape, int index, int xFX, int yFX, int xOldFX, int yOldFX)
    {
        M_lineVertices[0] = landscape.mStartpoints[index];
        M_lineVertices[1] = landscape.mEndpoints[index];
        M_axis.assign(M_lineVertices[1]);
        M_axis.subtract(M_lineVertices[0]);
        M_axis.normalize();
        M_axis.turnRight();

        //All the separation axes
        //FXVector separationAxis = new FXVector();
        //FXVector face = new FXVector();
        M_tmp1.assignFX(xFX, yFX);
        M_tmp2.assignFX(xOldFX, yOldFX);

        int deltaFX = 32;

        M_separationDistanceFX = Integer.MIN_VALUE;
        long depthFX = 0;
        // test separation axes of A
        long anchor1FX = M_axis.crossFX(M_lineVertices[0]);
        long anchor2FX = M_axis.crossFX(M_lineVertices[1]);
        long pointFX = M_axis.crossFX(M_tmp1);
        long distance1FX = anchor1FX - pointFX;
        long distance2FX = anchor2FX - pointFX;
        if ((distance1FX > 0 && distance2FX < -0)
                || (distance1FX < -0 && distance2FX > 0) )
        {
            long anchorFX = M_axis.dotFX(M_lineVertices[0]);
            long point1FX = M_axis.dotFX(M_tmp1);
            long point2FX = M_axis.dotFX(M_tmp2);
            distance1FX = point1FX - anchorFX;
            distance2FX = point2FX - anchorFX;
            if ((distance1FX > 0 && distance2FX < -deltaFX)
                    || (distance1FX < 0 && distance2FX > deltaFX) )
            {
                M_axis.multFX(distance1FX);
                return M_axis;
            }
        }
        return null;
    }
    //#NoEco */

    /**
     * Detects contacts between a body and a particle.
     * @param b1
     * @param xFX
     * @param yFX
     * @return the scaled contact normal.
     */
    protected static FXVector detectCollision(Body b1, int xFX, int yFX)
    {
       //look for previously calculated contact
        FXVector normal;
        if (b1.mShape.mVertices.length > 1 )
        {
            normal = detectCollisionPolyPoint(b1, xFX, yFX);
        }
        else
        {
            normal = detectCollisionCirclePoint(b1, xFX, yFX);
        }

        return normal;
    }


    /**
     * Detects collision of a circle and a particle.
     * @param b1
     * @param xFX
     * @param yFX
     * @return the scaled contact normal.
     */
    private static FXVector detectCollisionCirclePoint(Body b1, int xFX, int yFX)
    {
        M_distance.assign(b1.mPositionFX);
        M_distance.xFX -= xFX;
        M_distance.yFX -= yFX;       //length

        int distFX = M_distance.lengthFX();
        int depthFX = distFX - b1.mShape.mBoundingRadiusFX;
        if ( depthFX > 0 || distFX == 0)
        {
            return null;
        }

        return M_distance;
    }

    /**
     * Detects collision of a polygon and a particle.
     * @param b1
     * @param xFX
     * @param yFX
     * @return the scaled contact normal.
     */
    private static FXVector detectCollisionPolyPoint(Body b1, int xFX, int yFX)
    {
        FXVector[] vertices1 = b1.getVertices();
        FXVector[] axes1 = b1.getAxes();

        M_vertexPositionEstimates1 = b1.mVertexPositionEstimates;
        M_vertexPositionEstimates2 = M_lineVertexEstimates;

        M_tmp1.assignFX(xFX, yFX);

        M_separationDistanceFX = Integer.MIN_VALUE;
        long depthFX = 0;
        // test separation axes of A
        for(int i = 0; i < axes1.length; i++)
        {
            depthFX = intervalIntersectFX( vertices1, 0, vertices1.length, M_tmp1, 0, 0, axes1[i]);

            if (depthFX < 0)
            {
                return null;
            }

            if (depthFX < M_separationDistanceFX || M_separationDistanceFX == Integer.MIN_VALUE)
            {
                M_separationAxis.assign(axes1[i]);
                M_separationDistanceFX = (int) depthFX;
            }
        }

        M_tmp2.assign(b1.getAbsoluePoint(b1.mShape.mCcentroid));     //center of mass body 2
        M_b1b2.assignDiff(M_tmp1, M_tmp2);
        if (M_separationAxis.dotFX(M_b1b2) > 0)
        {
            M_separationAxis.mult(-1);
        }

        M_separationAxis.multFX(M_separationDistanceFX);

        return M_separationAxis;
    }

    /**
     * Detects collision of two circles
     * @param b1 body 1 (circle)
     * @param b2 body 2 (circle)
     * @param c the contact to be filled.
     * @return the contact if the bodies intersect, null otherwise.
     */
    private static Contact detectCollisionCircleCircle(Body b1, int index1, Body b2, int index2, Contact c)
    {
        M_distance.assign(b1.mPositionFX);
        M_distance.subtract(b2.mPositionFX);        //length

        Shape shape1, shape2;
        int startIdx1, startIdx2;

        if (b1.mShape instanceof MultiShape)
        {
            shape1 = ((MultiShape) b1.mShape).mShapes[index1];
            startIdx1 = ((MultiShape) b1.mShape).mVertexStartIndices[index1];
        }
        else
        {
            shape1 = b1.mShape;
            startIdx1 = 0;
        }

        if (b2.mShape instanceof MultiShape)
        {
            shape2 = ((MultiShape) b2.mShape).mShapes[index1];
            startIdx2 = ((MultiShape) b2.mShape).mVertexStartIndices[index1];
        }
        else
        {
            shape2 = b2.mShape;
            startIdx2 = 0;
        }

        long slack1FX = b1.mVertexPositionEstimates[startIdx1].dotFX(M_distance);
        long slack2FX = b2.mVertexPositionEstimates[startIdx2].dotFX(M_distance);


        //#ContactPrecision /*
        int distNormFX = M_distance.lengthFX();
        int distFX = distNormFX;                            //#FX2F float distFX = distNormFX;
        //#ContactPrecision */
        /* //#ContactPrecision
        M_distance.multFX(64);                              //#FX2F
        int distNormFX = M_distance.preciseLengthFX();
        int distFX = distNormFX >> 6;                       //#FX2F float distFX = distNormFX;
        */ //#ContactPrecision

        if (distFX == 0 )
        {
            return null;
        }

        long slackFX = Math.max(((- slack1FX + slack2FX) << FXUtil.DECIMAL) / distFX, 0);

        int depthFX = distFX - shape1.mBoundingRadiusFX - shape2.mBoundingRadiusFX;
        if ( depthFX > slackFX)
        {
            return null;
        }

        M_distance.divideByFX(distNormFX);
        M_tmp1.assign(b1.mPositionFX);      //point 1
        M_tmp1.add(M_distance, -shape1.mBoundingRadiusFX);

        c = initContact(c, M_distance, b1, index1, b2, index2);
        c.setContactPosition1(M_tmp1, -depthFX, true);

        return c;
    }

    /**
     * Detects collision of a polygon and a circle.
     * @param b1 body 1 (polygon)
     * @param b2 body 2 (circle)
     * @param c the contact to be filled.
     * @return the contact if the bodies intersect, null otherwise.
     */
    private static Contact detectCollisionPolyCircle(Body b1, int index1, Body b2, int index2, Contact c)
    {
        FXVector[] vertices1 = b1.getVertices();
        FXVector[] axes1 = b1.getAxes();

        M_vertexPositionEstimates1 = b1.mVertexPositionEstimates;
        M_vertexPositionEstimates2 = b2.mVertexPositionEstimates;

        Shape shape1, shape2;
        int startIdx1, startIdx2, endIdx1;
        int startAxes1, endAxes1;

        if (b1.mShape instanceof MultiShape)
        {
            MultiShape multiShape = ((MultiShape) b1.mShape);
            shape1 = multiShape.mShapes[index1];
            startIdx1  = multiShape.mVertexStartIndices[index1];
            endIdx1    = multiShape.mVertexStartIndices[index1 + 1];
            startAxes1 = multiShape.mAxesStartIndices[index1];
            endAxes1   = multiShape.mAxesStartIndices[index1 + 1];
        }
        else
        {
            shape1 = b1.mShape;
            startIdx1  = 0;
            endIdx1    = shape1.mVertices.length;
            startAxes1 = 0;
            endAxes1   = axes1.length;
        }

        if (b2.mShape instanceof MultiShape)
        {
            MultiShape multiShape = ((MultiShape) b2.mShape);
            shape2 = multiShape.mShapes[index1];
            startIdx2  = multiShape.mVertexStartIndices[index1];

        }
        else
        {
            shape2 = b2.mShape;
            startIdx2  = 0;
        }

        M_separationDistanceFX = Integer.MIN_VALUE;
        long depthFX = 0;
        // test separation axes of A
        for(int i = startAxes1; i < endAxes1; i++)
        {
            depthFX = intervalIntersectFX( vertices1, startIdx1, endIdx1, b2.mPositionFX, shape2.mBoundingRadiusFX, startIdx2, axes1[i]);

            if (depthFX == Integer.MIN_VALUE)
            {
                return null;
            }

            if (depthFX < M_separationDistanceFX || M_separationDistanceFX == Integer.MIN_VALUE)
            {
                M_separationAxis.assign(axes1[i]);
                M_separationDistanceFX = (int) depthFX;
            }
        }

        int mindistFX = b2.mShape.mMaxSizeFX + b1.mShape.mMaxSizeFX;
        int currdistFX = 0;
        boolean circleAxisfound = false;

        //find closest point
        for( int i = startIdx1; i < endIdx1; i++)
        {
            M_distance.assignDiff(vertices1[i], b2.mPositionFX);
            currdistFX = M_distance.lengthFX();
            if (currdistFX < mindistFX)
            {
                mindistFX = currdistFX;
                M_circleAxis.assign(M_distance);
                circleAxisfound = true;
            }
        }

        if (circleAxisfound)
        {
            M_circleAxis.normalize();
            depthFX = intervalIntersectFX( vertices1, startIdx1, endIdx1, b2.mPositionFX, shape2.mBoundingRadiusFX, startIdx2, M_circleAxis);
            if (depthFX  == Integer.MIN_VALUE)
            {
                return null;
            }
            if (depthFX < M_separationDistanceFX + World.M_CONTACT_touchEpsilonFX || M_separationDistanceFX == Integer.MIN_VALUE)
            {
                M_separationAxis.assign(M_circleAxis);
                M_separationDistanceFX = (int) depthFX;
            }
        }


        //determine contacts based on the (turned) separation axis (= face normal)
        M_tmp1.assign(b1.getAbsoluePoint(shape1.mCcentroid));     //center of mass body 1
        M_tmp2.assign(b2.getAbsoluePoint(shape2.mCcentroid));     //center of mass body 2
        M_b1b2.assignDiff(M_tmp2, M_tmp1);
        if (M_separationAxis.dotFX(M_b1b2) > 0)
        {
            M_separationAxis.mult(-1);
        }

        M_separationAxis2.assign(M_separationAxis);
        c = initContact(c, M_separationAxis2, b1, index1, b2, index2);

        int sCount1 = findSupportVertices(vertices1, startIdx1, endIdx1, M_separationAxis, M_supportVertices1, M_depthDiff1FX);

        return checkSupportVertexCasesCircle(c, b2, shape2, M_separationAxis, sCount1);
    }


    /**
     * Detects collision of two convex polygons.
     * @param b1 body 1 (polygon)
     * @param b2 body 2 (polygon)
     * @param c the contact to be filled.
     * @return the contact if the bodies intersect, null otherwise.
     */
    private static Contact detectCollisionPolyPoly(Body b1, int index1, Body b2, int index2, Contact c)
    {
        FXVector[] vertices1 = b1.getVertices();
        FXVector[] vertices2 = b2.getVertices();
        FXVector[] axes1 = b1.getAxes();
        FXVector[] axes2 = b2.getAxes();

        M_vertexPositionEstimates1 = b1.mVertexPositionEstimates;
        M_vertexPositionEstimates2 = b2.mVertexPositionEstimates;

        Shape shape1, shape2;
        int startIdx1, startIdx2, endIdx1, endIdx2;
        int startAxes1, startAxes2, endAxes1, endAxes2;

        if (b1.mShape instanceof MultiShape)
        {
            MultiShape multiShape = ((MultiShape) b1.mShape);
            shape1 = multiShape.mShapes[index1];
            startIdx1  = multiShape.mVertexStartIndices[index1];
            endIdx1    = multiShape.mVertexStartIndices[index1 + 1];
            startAxes1 = multiShape.mAxesStartIndices[index1];
            endAxes1   = multiShape.mAxesStartIndices[index1 + 1];
        }
        else
        {
            shape1 = b1.mShape;
            startIdx1  = 0;
            endIdx1    = shape1.mVertices.length;
            startAxes1 = 0;
            endAxes1   = axes1.length;
        }

        if (b2.mShape instanceof MultiShape)
        {
            MultiShape multiShape = ((MultiShape) b2.mShape);
            shape2 = multiShape.mShapes[index2];
            startIdx2  = multiShape.mVertexStartIndices[index2];
            endIdx2    = multiShape.mVertexStartIndices[index2 + 1];
            startAxes2 = multiShape.mAxesStartIndices[index2];
            endAxes2   = multiShape.mAxesStartIndices[index2 + 1];
        }
        else
        {
            shape2 = b2.mShape;
            startIdx2  = 0;
            endIdx2    = shape2.mVertices.length;
            startAxes2 = 0;
            endAxes2   = axes2.length;
        }

        M_separationDistanceFX = Integer.MIN_VALUE;
        long depthFX = 0;
        long secondaryCriteriumFX = 0;      //secondary criterium in case of not complete penetration
        long testCriteriumFX = 0;
        // test separation axes of A
        M_relativeVelocity.assignDiff(b1.mVelocityFX, b2.mVelocityFX);
        for(int i = startAxes1; i < endAxes1; i++)
        {
            depthFX = intervalIntersectFX( vertices1, startIdx1, endIdx1, vertices2, startIdx2, endIdx2, axes1[i]);
             if (depthFX  == Integer.MIN_VALUE)
            {
                return null;
            }

            if (depthFX < M_separationDistanceFX || M_separationDistanceFX == Integer.MIN_VALUE)
            {
                if (depthFX < World.M_CONTACT_touchEpsilonCollisionSlackFX)
                {
                    testCriteriumFX = Math.abs(axes1[i].crossFX(M_relativeVelocity));
                    if (testCriteriumFX >= secondaryCriteriumFX)
                    {
                        M_separationAxis.assign(axes1[i]);
                        M_separationDistanceFX = (int) depthFX;
                        M_projectionAxisIsFromFirstPoly = true;

                        secondaryCriteriumFX = testCriteriumFX;
                    }
                }
                else
                {
                    M_separationAxis.assign(axes1[i]);
                    M_separationDistanceFX = (int) depthFX;
                    M_projectionAxisIsFromFirstPoly = true;
                }

            }
            else if (depthFX < World.M_CONTACT_touchEpsilonCollisionSlackFX)
            {
                //check secondary criterium
                testCriteriumFX = Math.abs(axes1[i].crossFX(M_relativeVelocity));
                if (testCriteriumFX >= secondaryCriteriumFX)
                {
                    M_separationAxis.assign(axes1[i]);
                    M_separationDistanceFX = (int) depthFX;
                    M_projectionAxisIsFromFirstPoly = true;

                    secondaryCriteriumFX = testCriteriumFX;
                }
            }
        }


        for(int i = startAxes2; i < endAxes2; i++)
        {
            depthFX = intervalIntersectFX( vertices1, startIdx1, endIdx1, vertices2, startIdx2, endIdx2, axes2[i]);

            if (depthFX  == Integer.MIN_VALUE)
            {
                return null;
            }

            if (depthFX < M_separationDistanceFX)
            {
                if (depthFX < World.M_CONTACT_touchEpsilonCollisionSlackFX)
                {
                    testCriteriumFX = Math.abs(axes2[i].crossFX(M_relativeVelocity));
                    if (testCriteriumFX >= secondaryCriteriumFX)
                    {
                        M_separationAxis.assign(axes2[i]);
                        M_separationDistanceFX = (int) depthFX;
                        M_projectionAxisIsFromFirstPoly = false;

                        secondaryCriteriumFX = testCriteriumFX;
                    }
                }
                else
                {
                    M_separationAxis.assign(axes2[i]);
                    M_separationDistanceFX = (int) depthFX;
                    M_projectionAxisIsFromFirstPoly = false;
                }
            }
            else if (depthFX < World.M_CONTACT_touchEpsilonCollisionSlackFX)
            {
                //check secondary criterium
                testCriteriumFX = Math.abs(axes2[i].crossFX(M_relativeVelocity));
                if (testCriteriumFX >= secondaryCriteriumFX)
                {
                    M_separationAxis.assign(axes2[i]);
                    M_separationDistanceFX = (int) depthFX;
                    M_projectionAxisIsFromFirstPoly = false;

                    secondaryCriteriumFX = testCriteriumFX;
                }
            }
        }

        //determine contacts based on the (turned) separation axis (= face normal)
        M_tmp1.assign(b1.getAbsoluePoint(shape1.mCcentroid));     //center of mass body 1
        M_tmp2.assign(b2.getAbsoluePoint(shape2.mCcentroid));     //center of mass body 2
        M_b1b2.assignDiff(M_tmp2, M_tmp1);
        if (M_separationAxis.dotFX(M_b1b2) > 0)
        {
            M_separationAxis.mult(-1);
        }

        int minDiffFX = 0;
        int sCount1 = 0, sCount2 = 0;
        sCount1 = findSupportVertices(vertices1, startIdx1, endIdx1, M_separationAxis, M_supportVertices1, M_depthDiff1FX);
        M_separationAxis.mult(-1);
        sCount2 = findSupportVertices(vertices2, startIdx2, endIdx2, M_separationAxis, M_supportVertices2, M_depthDiff2FX);

        //make contacts
        //three (four) cases:
        // 1-1          vert-vert
        if (sCount1 == 1 && sCount2 == 1)
        {
            //center of intersecting vertices is used
            //could be improved, but is works fine and is fast...
            Contact c2 = new Contact(M_supportVertices1[0], M_supportVertices2[0], b1, b2);
            return c2;
        }


        M_separationAxis2.assign(M_separationAxis);
        M_separationAxis2.mult(-1);

        c = initContact(c, M_separationAxis2, b1, index1, b2, index2);

        return checkSupportVertexCasesPolygon(c, M_separationAxis, sCount1, sCount2, vertices1, startIdx1, endIdx1, vertices2, startIdx2, endIdx2);
    }

    /**
     * Detects collision of a line and a circle.
     * @param b1 the body (circle)
     * @param start the start point of the line
     * @param end the end point of the line
     * @param face the face of the landscape element see {@link Landscape#mFaces}
     * @param c the contact to be filled.
     * @return the contact if the bodies intersect, null otherwise.
     */
    //#NoEco /*
    private static Contact detectCollisionCircleLine(Body b1, int index1, Body landscapeBody, int index2, FXVector start, FXVector end, short face, Contact c)
    {
        M_lineVertices[0] = start;
        M_lineVertices[1] = end;
        M_axis.assign(end);
        M_axis.subtract(start);

                                //#ContactPrecision M_axis.mult(64);
        M_axis.normalize();     //#ContactPrecision M_axis.normalizePrecise();
        M_axis.turnRight();

        M_vertexPositionEstimates1 = M_lineVertexEstimates;
        M_vertexPositionEstimates2 = b1.mVertexPositionEstimates;

        if ( (face == Landscape.FACE_NONE && ! b1.mPositionFX.leftOf(start, end))
           || face == Landscape.FACE_LEFT )
        {
            M_axis.mult(-1);
        }

        Shape shape1;
        int startIdx1;

        if (b1.mShape instanceof MultiShape)
        {
            MultiShape multiShape = ((MultiShape) b1.mShape);
            shape1 = multiShape.mShapes[index1];
            startIdx1  = multiShape.mVertexStartIndices[index1];

        }
        else
        {
            shape1 = b1.mShape;
            startIdx1  = 0;
        }


        M_separationDistanceFX = Integer.MIN_VALUE;
        long depthFX = 0;
        // test separation axis
        //the (incorrect) length of 1 is passed in order to avoid double calculation
        //In the projection direction of the axis, start and end are equal
        depthFX = intervalIntersectFX( M_lineVertices, 0, 1, b1.mPositionFX, shape1.mBoundingRadiusFX, startIdx1, M_axis);

        if (depthFX  == Integer.MIN_VALUE)
        {
            return null;
        }

        M_separationAxis.assign(M_axis);
        M_separationDistanceFX  = (int) depthFX;

        //check from the optimal circle direction
        //FXVector circleAxis = new FXVector();
        int mindistFX = M_axis.fastLengthFX() + shape1.mMaxSizeFX;
        int currdistFX = 0;
        boolean circleAxisfound = false;

        //find closest point
        for( int i = 0; i < M_lineVertices.length; i++)
        {
            M_distance.assignDiff(M_lineVertices[i], b1.mPositionFX);
            currdistFX = M_distance.lengthFX();
            if (currdistFX < mindistFX)
            {
                mindistFX = currdistFX;
                M_circleAxis.assign(M_distance);
                circleAxisfound = true;
            }
        }

        if (circleAxisfound)
        {
            M_circleAxis.normalize();
            depthFX = intervalIntersectFX( M_lineVertices, 0, 2, b1.mPositionFX, shape1.mBoundingRadiusFX, startIdx1, M_circleAxis);
            if (depthFX  == Integer.MIN_VALUE)
            {
                return null;
            }

            if (depthFX < M_separationDistanceFX + World.M_CONTACT_touchEpsilonFX || M_separationDistanceFX == Integer.MIN_VALUE)
            {
                M_separationAxis.assign(M_circleAxis);
                M_separationDistanceFX = (int) depthFX;
            }
        }

        //determine contacts based on the (turned) separation axis (= face normal)

        M_separationAxis2.assign(M_separationAxis);
        c = initContact(c, M_separationAxis2, landscapeBody, index2, b1, index1 );

        int sCount1 = findSupportVertices(M_lineVertices, 0, 2, M_separationAxis, M_supportVertices1, M_depthDiff1FX);

        return checkSupportVertexCasesCircle(c, b1, shape1, M_separationAxis, sCount1);
    }
    //#NoEco */

    /**
     * Detects collision of a polygon and a line.
     * @param b1 the body (polygon)
     * @param start the start point of the line
     * @param end the end point of the line
     * @param face the face of the landscape element see {@link Landscape#mFaces}
     * @param c the contact to be filled.
     * @return the contact if the bodies intersect, null otherwise.
     */
    //#NoEco /*
    private static Contact detectCollisionPolyLine(Body b1, int index1, Body landscapeBody, int index2, FXVector start, FXVector end, short face, Contact c)
    {
        FXVector[] vertices1 = b1.getVertices();
        M_lineVertices[0] = start;
        M_lineVertices[1] = end;

        FXVector[] axes1 = b1.getAxes();
        M_axis.assign(end);
        M_axis.subtract(start);
                             //#ContactPrecision M_axis.mult(64);
        M_axis.normalize();  //#ContactPrecision M_axis.normalizePrecise();
        M_axis.turnRight();

        M_vertexPositionEstimates1 = b1.mVertexPositionEstimates;
        M_vertexPositionEstimates2 = M_lineVertexEstimates;

        if ( (face == Landscape.FACE_NONE && ! b1.mPositionFX.leftOf(start, end))
                || face == Landscape.FACE_RIGHT )
        {
             M_axis.mult(-1);
        }

        Shape shape1;
        int startIdx1, endIdx1;
        int startAxes1, endAxes1;

        if (b1.mShape instanceof MultiShape)
        {
            MultiShape multiShape = ((MultiShape) b1.mShape);
            shape1 = multiShape.mShapes[index1];
            startIdx1  = multiShape.mVertexStartIndices[index1];
            endIdx1    = multiShape.mVertexStartIndices[index1 + 1];
            startAxes1 = multiShape.mAxesStartIndices[index1];
            endAxes1   = multiShape.mAxesStartIndices[index1 + 1];
        }
        else
        {
            shape1 = b1.mShape;
            startIdx1  = 0;
            endIdx1    = shape1.mVertices.length;
            startAxes1 = 0;
            endAxes1   = axes1.length;
        }

        M_separationDistanceFX = Integer.MIN_VALUE;
        long depthFX = 0;

        // test separation axes of A
        for(int i = startAxes1; i < endAxes1; i++)
        {
            depthFX = intervalIntersectFX( vertices1, startIdx1, endIdx1, M_lineVertices, 0, 2, axes1[i]);

            if (depthFX == Integer.MIN_VALUE)
            {
                return null;
            }

            if (depthFX < M_separationDistanceFX || M_separationDistanceFX == Integer.MIN_VALUE)
            {
                M_separationAxis.assign(axes1[i]);
                M_separationDistanceFX = (int) depthFX;
            }
        }

        depthFX = intervalIntersectFX( vertices1, startIdx1, endIdx1, M_lineVertices, 0, 1, M_axis);
        if (depthFX  == Integer.MIN_VALUE)
        {
            return null;
        }

        if (depthFX < M_separationDistanceFX)
        {
            M_separationAxis.assign(M_axis);
            M_separationDistanceFX = (int) depthFX;
        }
        //check condition for switching
        switch(face)
        {
        case Landscape.FACE_NONE :
            {
                M_tmp1.assign(start);     //center
                M_tmp1.add(end);
                M_tmp1.divideBy(2);
                M_tmp2.assign(b1.getAbsoluePoint(shape1.mCcentroid));     //center of mass body 1
                M_b1b2.assignDiff(M_tmp1, M_tmp2);
                if (M_separationAxis.dotFX(M_b1b2) > 0)
                {
                    M_separationAxis.mult(-1);
                }
            }
            break;
        case Landscape.FACE_RIGHT :
        case Landscape.FACE_LEFT :
            {
                if (M_separationAxis.dotFX(M_axis) <= 0)
                {
                    M_separationAxis.mult(-1);
                }
            }
            break;
        }

        //determine contacts based on the (turned) separation axis (= face normal)

        int sCount1 = 0, sCount2 = 0;
        sCount1 = findSupportVertices(vertices1, startIdx1, endIdx1, M_separationAxis, M_supportVertices1, M_depthDiff1FX);
        M_separationAxis.mult(-1);
        sCount2 = findSupportVertices(M_lineVertices, 0, 2, M_separationAxis, M_supportVertices2, M_depthDiff2FX);

        //make contacts
        //three (four) cases:
        // 1-1          vert-vert
        if (sCount1 == 1 && sCount2 == 1)
        {
            //center of intersecting vertices is used
            //could be improved, but is works fine and is fast...
            Contact c2 = new Contact(M_supportVertices1[0], M_supportVertices2[0], b1, landscapeBody);
            return c2;
        }

        M_separationAxis2.assign(M_separationAxis);
        M_separationAxis2.mult(-1);
        c = initContact(c, M_separationAxis2, b1, index1, landscapeBody, index2 );

        return checkSupportVertexCasesPolygon(c, M_separationAxis, sCount1, sCount2, vertices1, startIdx1, endIdx1, M_lineVertices, 0, 2);
    }
    //#NoEco */

    /**
     * Checks the type of a polygon collision situation.
     * @param c the contact
     * @param separationAxis the axis along which the intersection occurs.
     * @param sCount1 support vertex count for body 1
     * @param sCount2 support vertex count for body 2
     * @return the correctly filled contact
     */
    private static Contact checkSupportVertexCasesPolygon(Contact c, FXVector separationAxis, int sCount1, int sCount2, FXVector[] vertices1, int startIdx1, int endIdx1, FXVector[] vertices2, int startIdx2, int endIdx2 )
    {
        //remaining two (three) cases: 2-1, 1-2, 2-2
        // 2-1(1-2)     edge-vert
        if (sCount1 == 1 && sCount2 == 2)
        {
            c.setContactPosition1(M_supportVertices1[0], M_separationDistanceFX, true);
            return c;
        }
        if (sCount1 == 2 && sCount2 == 1)
        {
            c.setContactPosition1(M_supportVertices2[0], M_separationDistanceFX, false);
            return c;
        }
        // 2-2          edge-edge
        if (sCount1 == 2 && sCount2 == 2)
        {
            long tmpFX = 0;
            int tmpMinIndex1 = 0;
            int tmpMinIndex2 = 0;

            long min1FX = M_supportVertices1[0].crossFX(separationAxis);
            long max1FX = min1FX;
            tmpFX = M_supportVertices1[1].crossFX(separationAxis);
            if (min1FX > tmpFX )
            {
                min1FX = tmpFX;
                tmpMinIndex1 = 1;
            }
            else if (max1FX < tmpFX )
            {
                max1FX = tmpFX;
            }

            long min2FX = M_supportVertices2[0].crossFX(separationAxis);
            long max2FX = min2FX;
            tmpFX = M_supportVertices2[1].crossFX(separationAxis);
            if (min2FX > tmpFX )
            {
                min2FX = tmpFX;
                tmpMinIndex2 = 1;
            }
            else if (max2FX < tmpFX )
            {
                max2FX = tmpFX;
            }


            if (max1FX < max2FX)
            {
                int separationDistFX = calcSeparationDistanceFX(M_supportVertices1[1 - tmpMinIndex1], true, vertices2, startIdx2, endIdx2);
                c.setContactPosition1( M_supportVertices1[1 - tmpMinIndex1], separationDistFX - M_depthDiff1FX[1 - tmpMinIndex1], true);
            }
            else
            {
                int separationDistFX = calcSeparationDistanceFX(M_supportVertices2[1 - tmpMinIndex2], false, vertices1, startIdx1, endIdx1);
                c.setContactPosition1( M_supportVertices2[1 - tmpMinIndex2], separationDistFX - M_depthDiff2FX[1 - tmpMinIndex2], false);
            }

            if (min1FX < min2FX)
            {
                int separationDistFX = calcSeparationDistanceFX(M_supportVertices2[tmpMinIndex2], false, vertices1, startIdx1, endIdx1);
                c.setContactPosition2( M_supportVertices2[tmpMinIndex2], separationDistFX - M_depthDiff2FX[tmpMinIndex2], false);
            }
            else
            {
                int separationDistFX = calcSeparationDistanceFX(M_supportVertices1[tmpMinIndex1], true, vertices2, startIdx2, endIdx2);
                c.setContactPosition2( M_supportVertices1[tmpMinIndex1], separationDistFX - M_depthDiff1FX[tmpMinIndex1], true);
            }

            return c;
        }

        //this should never happen!! -> means that some sCount is not 1 or 2 (maybe 0)
        return null;
    }

    private final static int calcSeparationDistanceFX(FXVector referenceVertex, boolean isVertexFromFirst, FXVector[] vertices, int startIdx, int endIdx)
    {
        if (M_projectionAxisIsFromFirstPoly == isVertexFromFirst)
        {
            FXVector tmp1 = Collision.M_tmp1;
            tmp1.assign(referenceVertex);

            FXVector separationAxis = isVertexFromFirst ?  M_separationAxis2 : Collision.M_separationAxis;
            tmp1.add(separationAxis, M_separationDistanceFX * 1000);

            FXVector tmp2 = Collision.M_tmp2;
            FXVector tmp3 = Collision.M_tmp3;
            int minDistFX = -1, actualMinDistFX = 0;
            for( int j = endIdx - 1, i = startIdx; i < endIdx; j = i, i++)
            {
                boolean intersection = FXVector.intersect(vertices[i], vertices[j], referenceVertex, tmp1, tmp2);
                if (!intersection)
                {
                    continue;
                }
                tmp3.assign(referenceVertex);
                tmp3.subtract(tmp2);

                int distanceFX = tmp3.lengthFX();

                if (minDistFX == -1 || minDistFX > distanceFX)
                {
                    minDistFX = distanceFX;
                    actualMinDistFX = minDistFX;
                    //length looses the information about direction
                    //the separationdistance is the deepest penetration
                    //if something is beyond that, we have negative penetration (almost touching)
                    if (separationAxis.dotFX(tmp3) > 0)
                    {
                        actualMinDistFX *= -1;
                    }

                }
            }
            if (minDistFX == -1)
            {
                return 0;
            }

            return actualMinDistFX;
        }
        else
        {
            return M_separationDistanceFX;
        }
    }

    /**
     * Checks the type of a circle collision situation.
     * @param c the contact
     * @param circle the body representing the circle
     * @param separationAxis the axis along which the intersection occurs.
     * @param sCount1 support vertex count for body 1
     * @return the correctly filled contact
     */
    private static Contact checkSupportVertexCasesCircle(Contact c, Body circle, Shape circleShape, FXVector separationAxis, int sCount1)
    {
      //make contacts
        //two cases:
        // 1 contact          vert
        if (sCount1 == 1 )
        {
            c.setContactPosition1(M_supportVertices1[0], M_separationDistanceFX, true);
            return c;
        }

        // 2 contacts     edge
        if (sCount1 == 2)
        {
            separationAxis.mult(-1);
            M_tmp1.assign(circle.mPositionFX);
            M_tmp1.add(separationAxis, -circleShape.mBoundingRadiusFX);

            c.setContactPosition1(M_tmp1, M_separationDistanceFX, false);
            return c;
        }

        //this should never happen!! -> means that some sCount is not 1 or 2 (maybe 0)
        return null;
    }

    /**
     * Intersects the projection of two polygons.
     * The projection occurs along the given axis.
     * @param vertices1 Vertices of polygon 1
     * @param vertices1Count vertices count of polygon 1
     * @param vertices2 Vertices of polygon 2
     * @param vertices2Count vertices count of polygon 2
     * @param axis projection axis
     * @return the amount of overlap if intersection, negative if no intersection
     */
    private static long intervalIntersectFX(FXVector vertices1[], int startIdx1, int endIdx1, FXVector vertices2[], int startIdx2, int endIdx2, FXVector axis)
    {
        long dFX = 0, estimateFX = 0;
        long minSlack1FX = 0, minSlack2FX = 0;
        long maxSlack1FX = 0, maxSlack2FX = 0;
        long  min1FX = vertices1[startIdx1].dotFX(axis);
        long max1FX = min1FX;
        for(int i = startIdx1; i < endIdx1; i ++)
        {
            dFX = vertices1[i].dotFX(axis);
            estimateFX = M_vertexPositionEstimates1[i].dotFX(axis);
            if (dFX <= min1FX)
            {
                min1FX = dFX;
                if ( estimateFX < 0 ) minSlack1FX = estimateFX;
            }
            else if (dFX >= max1FX)
            {
                max1FX = dFX;
                if ( estimateFX > 0 ) maxSlack1FX = estimateFX;
            }

        }

        long min2FX = vertices2[startIdx2].dotFX(axis);
        long max2FX = min2FX;
        for(int i = startIdx2; i < endIdx2; i ++)
        {
            dFX = vertices2[i].dotFX(axis);
            estimateFX = M_vertexPositionEstimates2[i].dotFX(axis);
            if (dFX <= min2FX)
            {
                min2FX = dFX;
                if ( estimateFX < 0 ) minSlack2FX = estimateFX;
            }
            else if (dFX >= max2FX)
            {
                max2FX = dFX;
                if ( estimateFX > 0 ) maxSlack2FX = estimateFX;
            }
        }

        long slack1FX = Math.min(minSlack1FX, -maxSlack2FX);
        long slack2FX = Math.min(minSlack2FX, -maxSlack1FX);
        long d1FX = min1FX - max2FX;
        long d2FX = min2FX - max1FX;
        if ( d1FX > -slack1FX || d2FX > -slack2FX)
        {
            return Integer.MIN_VALUE;
        }

        return d1FX > d2FX ? -d1FX : -d2FX;
    }

    /**
     * Intersects the projection of a polygon and a circle.
     * The projection occurs along the given axis.
     * @param vertices1 Vertices of polygon 1
     * @param vertices1Count vertices count of polygon 1
     * @param center center of the circle
     * @param radiusFX the radius of the circle(FX)
     * @param axis projection axis
     * @return the amount of overlap if intersection, negative if no intersection
     */
    private static long intervalIntersectFX(FXVector vertices1[], int startIndex1, int endIndex1, FXVector center, int radiusFX, int startIndex2, FXVector axis)
    {
        long dFX = 0, estimateFX = 0;
        long minSlack1FX = 0, minSlack2FX = 0;
        long maxSlack1FX = 0, maxSlack2FX = 0;
        long min1FX = vertices1[startIndex1].dotFX(axis);
        long max1FX = min1FX;
        for(int i = startIndex1; i < endIndex1; i ++)
        {
            dFX = vertices1[i].dotFX(axis);
            estimateFX = M_vertexPositionEstimates1[i].dotFX(axis);
            if (dFX <= min1FX)
            {
                min1FX = dFX;
                if ( estimateFX < 0 ) minSlack1FX = estimateFX;
            }
            else if (dFX >= max1FX)
            {
                max1FX = dFX;
                if ( estimateFX > 0 ) maxSlack1FX = estimateFX;
            }
        }

        long min2FX = center.dotFX(axis) - radiusFX;
        long max2FX = center.dotFX(axis) + radiusFX;

        estimateFX = M_vertexPositionEstimates2[startIndex2].dotFX(axis);
        if (estimateFX < 0)
            minSlack2FX = estimateFX;
        else
            maxSlack2FX = estimateFX;

        long slack1FX = Math.min(minSlack1FX, -maxSlack2FX);
        long slack2FX = Math.min(minSlack2FX, -maxSlack1FX);
        long d1FX = min1FX - max2FX;
        long d2FX = min2FX - max1FX;
        if ( d1FX > -slack1FX || d2FX > -slack2FX)
        {
            return Integer.MIN_VALUE;
        }

        return d1FX > d2FX ? -d1FX : -d2FX;
    }

    /**
     * Determines the support vertices for the contact.
     * Support vertices are the vertices that penetrate the other body the most.
     * @param vertices the vertices to check
     * @param verticesCount the count of elements in the vector
     * @param normal the projection normal
     * @param supportVertices vector to store the found vertices in
     * @param depthFX vector to store the found depths in
     * @return the number of found vertices
     */
    private static int findSupportVertices(FXVector[] vertices, int startIdx, int endIdx, FXVector normal, FXVector[] supportVertices, int[] depthFX)
    {
        long dMinFX = vertices[startIdx+0].dotFX(normal);
        long dMin2FX = vertices[startIdx+1].dotFX(normal);
        int index1 = startIdx + 0;
        int index2 = startIdx + 1;
        long tmpFX = 0;

        //switch the initial state if the order is reversed
        if (dMin2FX < dMinFX)
        {
            tmpFX = dMinFX;
            dMinFX = dMin2FX;
            dMin2FX = tmpFX;
            index1 = startIdx + 1;
            index2 = startIdx + 0;
        }

        for( int i = startIdx + 2; i < endIdx; i++)
        {
            tmpFX = vertices[i].dotFX(normal);
            if (tmpFX < dMinFX)
            {
                index2 = index1;
                dMin2FX = dMinFX;
                index1 = i;
                dMinFX = tmpFX;
            }
            else if (tmpFX < dMin2FX)
            {
                index2 = i;
                dMin2FX = tmpFX;
            }
        }


        //add first body
        depthFX[0] = 0;
        supportVertices[0] = vertices[index1];

        //the threshold for having a second support point
        tmpFX = (int) dMinFX + Math.max(World.M_COLLISION_collinearityDeltaFX,  M_separationDistanceFX);
        if (dMin2FX < tmpFX)
        {
            depthFX[1] = (int) (dMin2FX - dMinFX);
            supportVertices[1] = vertices[index2];
            return 2;
        }
        return 1;

    }

    /**
     * Initializes a contact for two bodies.
     * If no contact is passed a new is created,
     * unless the Contact storage holds an unused one (to avoid object creation).
     * @param c the contact
     * @param separationAxis the separation axis
     * @param b1 the body 1
     * @param b2 the body 2
     * @return the contact
     */
    private static Contact initContact(Contact c, FXVector separationAxis,
            Body b1, int index1, Body b2, int index2)
    {
        if ( c == null)
        {
            if (World.mContactStorageCount > 0)
            {
                c = World.mContactStorage[--World.mContactStorageCount];
                World.mContactStorage[World.mContactStorageCount] = null;
                c.clearAll();
                c.setNormal(separationAxis, b1, index1, b2, index2);
            }
            else
            {
                c = new Contact(separationAxis, b1, index1, b2, index2);
            }
        }
        else
        {
            c.clear();
            c.setNormal(separationAxis, b1, index1, b2, index2);
        }
        return c;
    }

}


