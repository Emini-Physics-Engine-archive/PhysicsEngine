package at.emini.physics2D;

import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

/**
 * The Contact class represents a contact between two bodies. <br>
 * The contacts are created during the collision detection step.  
 * A contact can be a single or double contact of a collision between two bodies. 
 * A single contact happens if the contact type is edge-face. 
 * In case of two perpendicular bodies the contact type can be face-face.
 * This situation is a single contact object representing the double contact. 
 * The extreme points of the face-face intersection line are contacts of the same bodies. 
 * To achieve better stacking results, these are treated together 
 * (Block solving).
 * 
 * @author Alexander Adensamer
 *
 */
public class Contact
{
    /**
     * Body 1 of the contact. 
     */
    Body mBody1;
    /**
     * additional index of shape/segment in body1
     */
    int mB1Index;
    
    /**
     * Body 2 of the contact. 
     */
    Body mBody2;
    /**
     * additional index of shape/segment in body1
     */
    int mB2Index;
        
    /**
     * Normal direction of the touching faces. 
     */
    FXVector mNormalDirection = new FXVector();
    /**
     * Tangent direction. 
     * Convenience to save computation time. 
     */
    FXVector mTangentDirection = new FXVector();

    /**
     * Contact position 1.
     * In absolute coordinates.
     */
    FXVector mContactPosition1 = new FXVector();
    /**
     * Contact position 2. 
     * In absolute coordinates.
     */
    FXVector mContactPosition2 = new FXVector();
    
    /**
     * Penetration depth of the bodies.
     * For contact 1. 
     */
    int mDepth1FX = 0;
    /**
     * Penetration depth of the bodies.
     * For contact 2 (if this is a double contact).  
     */
    int mDepth2FX = 0;
            
    //contact internals
    private FXVector mAccumulatedLambdaVec = new FXVector();
    private FXVector mAccumulatedTangentLambdaVec = new FXVector();
    private FXVector mRestitutionVec = new FXVector();
    private FXVector mRestitutionVecStore = new FXVector();  //stores the correct restitution from before bodies collide 
    private FXVector mCorrectVec = new FXVector();     //Node. vector is reused after iteration as the complete the correcting impulse!
    private FXVector mAccumulatedVirtualLambdaVec = new FXVector();
    
    /**
     * impulse vector for the apply momentum method
     * @fx
     */
    private static FXVector M_jvFX = new FXVector();    
    private static FXVector M_contactTmp = new FXVector();
    
    /**
     * Flag to indicate that the contact was just created. <br>
     * It is required to avoid double calculation of contacts and allows correct contact matching. 
     */
    boolean mIsNew = true;                     

    /**
     * Flag indicating the contact multiplicity: single or double.
     * A single contact has a single intersection point of the involved bodies. 
     * A double contact has two intersections. 
     * The shapes are strictly convex (and we assume small movements) so two relevant intersections
     * are the maximum.  
     */
    boolean mSingle = true;
    
 	/**
     * Is used when a contact in spe is skipped due to the geometry
     */
    boolean mSkipContact = false;
   
    //variables for precalculation
    FXVector mB11c = new FXVector();
    FXVector mB21c = new FXVector();
    FXVector mB12c = new FXVector();
    FXVector mB22c = new FXVector();

    FXMatrix mKMatrix = new FXMatrix(FXUtil.DECIMAL2);
    FXMatrix mNormalMassMatrix = new FXMatrix(FXUtil.DECIMAL2);
    FXVector mTangentMassVec2FX = new FXVector();
    
    /**
     * Pre-computed combined friction of the contact
     */
    int mFrictionFX = 0;
    
    //temporary vectors
    private static FXVector M_addImpulse = new FXVector(); 
    private static FXVector M_relativeVelocity1 = new FXVector();
    private static FXVector M_relativeVelocity2 = new FXVector();
    private static FXVector M_relativeVirtualVelocity1 = new FXVector();
    private static FXVector M_relativeVirtualVelocity2 = new FXVector();
    private static FXVector M_tempv1 = new FXVector();
    private static FXVector M_tempv2 = new FXVector();
    private static FXVector M_tempv3 = new FXVector();
    
    private static FXVector M_oldAccumLambdaVec = new FXVector();
    private static FXVector M_lambdaVec = new FXVector();
    private static FXVector M_resubstituteLambdaVec = new FXVector();
    private static FXVector M_oldAccumVirtualLambdaVec = new FXVector();
    private static FXVector M_virtualLambdaVec = new FXVector();

    private static FXVector M_tangentLambdaVec = new FXVector();
    
    //used for calculation whether contacts are active
    private static FXVector[] M_lineVertices = new FXVector[2];
    
    
    /**
     * Constructor. 
     * This sets up the contact, but contact positions are not initialized. 
     * @param normal - collision normal
     * @param body1 Body 1
     * @param body2 Body 2
     */
    protected Contact(FXVector normal, Body body1, int index1, Body body2, int index2 ) 
    {
        this.mBody1 = body1;
        this.mBody2 = body2;
        
        this.mB1Index = index1;
        this.mB2Index = index2;
        
        
        this.mNormalDirection.assign(normal);
      
        this.mTangentDirection.assign(mNormalDirection);
        mTangentDirection.turnRight(); 
        
        //friction
        mFrictionFX = (body1.mShape.mFrictionFX * body2.mShape.mFrictionFX) >> FXUtil.DECIMAL;
    }    
    
    /**
     * Constructor using two absolute points.
     * The actual contact position, normal and penetration depth are calculated.       
     * @param contactPosition1 contact position on body 1 
     * @param contactPosition2 contact position on body 2
     * @param body1 Body 1
     * @param body2 Body 2
     */
    protected Contact(FXVector contactPosition1, FXVector contactPosition2,Body body1, Body body2)
    {
        this.mBody1 = body1;
        this.mBody2 = body2;
        
        this.mNormalDirection.assign(contactPosition1);
        mNormalDirection.subtract(contactPosition2);
        
        this.mDepth1FX = mNormalDirection.lengthFX(); //#ContactPrecision this.mDepth1FX = mNormalDirection.preciseLengthFX();
        mNormalDirection.divideByFX(mDepth1FX);    

        setContactPosition1(contactPosition1, mDepth1FX, true);
        //this.contactPosition2 = new FXVector();	//done by variable initialization		
        
        this.mTangentDirection.assign(mNormalDirection);
        mTangentDirection.turnRight();
        

        //friction
        mFrictionFX = (body1.mShape.mFrictionFX * body2.mShape.mFrictionFX) >> FXUtil.DECIMAL;
    }
    
    /**
     * Resets normal and bodies.
     * Initializes the contact. 
     * This is required for contact reusal. 
     * @param normal the contact normal
     * @param b1 Body 1
     * @param b2 Body 2
     */
    public final void setNormal(FXVector normal, Body b1, int index1, Body b2, int index2)
    {        
        this.mNormalDirection.assign(normal);
               
        this.mTangentDirection.assign(mNormalDirection);
        mTangentDirection.turnRight();
        
        mBody1 = b1;
        mBody2 = b2;
        
        mB1Index = index1;
        mB2Index = index2;
        
        mFrictionFX = (mBody1.mShape.mFrictionFX * mBody2.mShape.mFrictionFX) >> FXUtil.DECIMAL;
    }
    
    /**
     * Sets up the contact for reuse (same bodies). 
     * It is required in order to avoid unnecessary Object instantiations 
     * instead of creating a new object.  
     */
    public final void clear()
    {
        mIsNew = true; //reuse this contact as new
        mSingle = true;
        mSkipContact = false;        
    }
    
    /**
     * Sets up the contact for reuse (different bodies). 
     * This is required in order to avoid unnecessary Object instantiations 
     */
    public final void clearAll()
    {
        clear();
        
        mAccumulatedLambdaVec.assignFX(0,0);
        mAccumulatedTangentLambdaVec.assignFX(0,0);
        mRestitutionVec.assignFX(0,0);
        mRestitutionVecStore.assignFX(0,0);
        mCorrectVec.assignFX(0,0);
        mAccumulatedVirtualLambdaVec.assignFX(0,0);
    }
    
    /**
     * Sets the first contact position. 
     * Calculates the correct relative position depending on the body positions and orientation
     * @fx
     * @param pos the absolute contact position
     * @param depthFX the penetration depth for this contact
     * @param posAtBody1 flag indicating whether the position lies on body 1 
     */
    public final void setContactPosition1(FXVector pos, int depthFX, boolean posAtBody1)
    {        
        mDepth1FX = depthFX;
        if (! posAtBody1)
        {
            mContactPosition1.assign(pos);
            mB11c.assignDiff(mContactPosition1, mBody1.mPositionFX);
            M_contactTmp.assign(mContactPosition1);
            M_contactTmp.add( mNormalDirection, -mDepth1FX);
            mB21c.assignDiff(M_contactTmp, mBody2.mPositionFX);
        }
        else
        {
            M_contactTmp.assign(pos);
            M_contactTmp.add( mNormalDirection, mDepth1FX);
            mContactPosition1.assign(M_contactTmp);
            mB11c.assignDiff(M_contactTmp, mBody1.mPositionFX);
            mB21c.assignDiff(pos, mBody2.mPositionFX);
        }
    }
    
    /**
     * Sets the second contact position.
     * Calculates the correct relative position depending on the body positions and orientation
     * @fx
     * @param pos the absolute contact position
     * @param depthFX the penetration depth for this contact
     * @param posAtBody1 flag indicating whether the position lies on body 1 
     */
    public final void setContactPosition2(FXVector pos, int depthFX, boolean posAtBody1)
    {
        mDepth2FX = depthFX;
        if (! posAtBody1)
        {
            mContactPosition2.assign(pos);
            mB12c.assignDiff(mContactPosition2, mBody1.mPositionFX);
            M_contactTmp.assign(mContactPosition2);
            M_contactTmp.add( mNormalDirection, -mDepth2FX);
            mB22c.assignDiff(M_contactTmp, mBody2.mPositionFX);
        }
        else
        {
            M_contactTmp.assign(pos);
            M_contactTmp.add( mNormalDirection, mDepth2FX);
            mContactPosition2.assign(M_contactTmp);
            mB12c.assignDiff(M_contactTmp, mBody1.mPositionFX);
            mB22c.assignDiff(pos, mBody2.mPositionFX);
        }
        mSingle = false;
    }
    
    /**
     * Checks if the contact is a single point.
     * @return true if it represents a single contact point. 
     */
    public boolean isSingle()
    {
        return mSingle;
    }
    
    /**
     * Gets contact position of the first contact point.
     * @return the contact position 1
     */
    public FXVector getContactPosition1()
    {
        return mContactPosition1;
    }   
    
    /**
     * Gets contact position of the second contact point.
     * @return the contact position 2
     */
    public FXVector getContactPosition2()
    {
        return mContactPosition2;
    } 
    
    /**
     * Gets the contact normal. 
     * @return the contact normal
     */
    public FXVector getNormal()
    {
        return mNormalDirection;
    }
    
    /**
     * Gets the first body.
     * @return the first body
     */
    public Body body1()
    {
        return mBody1;
    }
    
    /**
     * Gets the segment index of the first body if it is the landscape or shape index in case of a multi shape.
     * @return the segment/shape index of the first body
     */
    public int segment1()
    {
        return mB1Index;
    }
    
    /**
     * Gets the second body.
     * @return the second body
     */
    public Body body2()
    {
        return mBody2;
    }
    
    /**
     * Gets the segment index of the second body if it is the landscape or shape index in case of a multi shape.
     * @return the segment/shape index of the second body
     */
    public int segment2()
    {
        return mB2Index;
    }
    
    /**
     * Gets the penetration depth first contact point. 
     * @fx
     * @return the penetration depth of the first contact.
     */
    public int getDepth1FX()
    {
        return mDepth1FX;
    }
    
    /**
     * Gets the penetration depth second contact point. 
     * @fx
     * @return the penetration depth of the second contact (if any).
     */
    public int getDepth2FX()
    {
        return mDepth2FX;
    }
    
    /**
     * Applies the accumulated impulses.
     * Performs the warmstarting of the contact constraint.  
     */
    protected final void applyAccumImpulses()
    {
        applyImpulses(mAccumulatedLambdaVec, mNormalDirection);
        applyImpulses(mAccumulatedTangentLambdaVec, mTangentDirection);
    }
   
    /**
     * Precalculates values for the constraint iteration.
     * The following values are computed:
     * <ul>
     * <li>mass matrices for the contact </li>
     * <li>elasticity </li>
     * <li>friction </li>
     * </ul>
     * @param invTimestepFX the inverse timestep of the simulation
     */
    protected final void precalculate(long invTimestepFX)
    {        
        long b1InvMass2FX = mBody1.getInvMass2FX();
        long b2InvMass2FX = mBody2.getInvMass2FX();
        long b1InvInertia2FX = mBody1.getInvInertia2FX();
        long b2InvInertia2FX = mBody2.getInvInertia2FX();
        
        //contact
        int massSumMatFX = (int) (b1InvMass2FX + b2InvMass2FX);
        
        long b11cxnFX = mB11c.crossFX(mNormalDirection);
        long b21cxnFX = mB21c.crossFX(mNormalDirection);
        
        mKMatrix.mCol1xFX = massSumMatFX +  (((((b11cxnFX * b11cxnFX) >> FXUtil.DECIMAL) * b1InvInertia2FX) 
                                                   +    (((b21cxnFX * b21cxnFX) >> FXUtil.DECIMAL) * b2InvInertia2FX)) >> (FXUtil.DECIMAL)); 
        
        //friction
        mTangentMassVec2FX.xFX = (int)(b1InvMass2FX + b2InvMass2FX);
        mTangentMassVec2FX.xFX += (int)( ((b1InvInertia2FX * (mB11c.dotFX(mB11c) - ((b11cxnFX * b11cxnFX ) >> FXUtil.DECIMAL)) ) >> (FXUtil.DECIMAL)) 
                                      + ((b2InvInertia2FX * (mB21c.dotFX(mB21c) - ((b21cxnFX * b21cxnFX ) >> FXUtil.DECIMAL)) ) >> (FXUtil.DECIMAL)) );
                
        if (!mSingle)
        {
            long b12cxnFX = mB12c.crossFX(mNormalDirection);
            long b22cxnFX = mB22c.crossFX(mNormalDirection);
            
            mKMatrix.mCol1yFX = massSumMatFX + (((((b11cxnFX * b12cxnFX) >> FXUtil.DECIMAL) * b1InvInertia2FX) 
                                                       +   (((b21cxnFX * b22cxnFX) >> FXUtil.DECIMAL) * b2InvInertia2FX)) >> FXUtil.DECIMAL ); 

            mKMatrix.mCol2xFX = mKMatrix.mCol1yFX;

            mKMatrix.mCol2yFX = massSumMatFX + (((((b12cxnFX * b12cxnFX) >> FXUtil.DECIMAL) * b1InvInertia2FX) 
                                                       +   (((b22cxnFX * b22cxnFX) >> FXUtil.DECIMAL) * b2InvInertia2FX)) >> FXUtil.DECIMAL); 

            long k11squareFX = ((long) mKMatrix.mCol1xFX * (long) mKMatrix.mCol1xFX) >> FXUtil.DECIMAL2; 
            long k22k11FX = ((long) mKMatrix.mCol1xFX * (long) mKMatrix.mCol2yFX) >> FXUtil.DECIMAL2; 
            long k12k12FX = ((long) mKMatrix.mCol2xFX * (long) mKMatrix.mCol2xFX) >> FXUtil.DECIMAL2; 
                        
            if ( k11squareFX > World.M_CONTACT_MaxConditionNumber * (k22k11FX - k12k12FX) )
            {
                mSingle = true;
            }
            else
            {
                mNormalMassMatrix.assign(mKMatrix);
                mNormalMassMatrix.invert();
                                
            }
                        
            //friction
            mTangentMassVec2FX.yFX = (int)(b1InvMass2FX + b2InvMass2FX);
            mTangentMassVec2FX.yFX += (int)( ((b1InvInertia2FX * (mB12c.dotFX(mB12c) - ((b12cxnFX * b12cxnFX ) >> FXUtil.DECIMAL)) ) >> (FXUtil.DECIMAL)) 
                                          + ((b2InvInertia2FX * (mB22c.dotFX(mB22c) - ((b22cxnFX * b22cxnFX ) >> FXUtil.DECIMAL)) ) >> (FXUtil.DECIMAL)) );
            
        }
        
        //bounce     
        
        int elasticityFX = (mBody1.mShape.mElasticityFX * mBody2.mShape.mElasticityFX) >> FXUtil.DECIMAL;
        /*if ( elasticityFX >= FXUtil.ONE_FX - 10 && mDepth1FX < 0)
        {
            mSkipContact = true;
        }*/
        
        mBody1.getVelocity(mB11c, M_tempv1);
        mBody2.getVelocity(mB21c, M_tempv2);
        M_relativeVelocity1.assignDiff(M_tempv1, M_tempv2);
        long relVdotn1FX = - M_relativeVelocity1.dotFX(mNormalDirection);
        long relVdotn2FX = 0;

        if (mRestitutionVecStore.xFX != 0)
        {
            mRestitutionVec.xFX = mRestitutionVecStore.xFX;
        }
        else
        {
            mRestitutionVec.xFX = (int) (((long) elasticityFX * relVdotn1FX) >> FXUtil.DECIMAL);            
        }
        
        mRestitutionVecStore.xFX = 0;
        if (mDepth1FX < 0)
        {
            mRestitutionVecStore.xFX = mRestitutionVec.xFX;    
        }
        if (mRestitutionVec.xFX < ((long)World.M_CONTACT_touchEpsilonFX * invTimestepFX) >> FXUtil.DECIMAL
                || mDepth1FX < 0)
        {             
            mRestitutionVec.xFX = 0;
        }
        
        if (!mSingle)
        {
            mBody1.getVelocity(mB12c, M_tempv1);
            mBody2.getVelocity(mB22c, M_tempv2);
            M_relativeVelocity2.assignDiff(M_tempv1, M_tempv2);
            relVdotn2FX = - M_relativeVelocity2.dotFX(mNormalDirection);
            if (mRestitutionVecStore.yFX != 0)
            {
                mRestitutionVec.yFX = mRestitutionVecStore.yFX;
            }
            else
            {
                mRestitutionVec.yFX = (int) (((long) elasticityFX * relVdotn2FX) >> FXUtil.DECIMAL);
            }
            
            mRestitutionVecStore.yFX = 0;
            if (mDepth2FX < 0)
            {
                mRestitutionVecStore.yFX = mRestitutionVec.yFX;    
            }
            if (mRestitutionVec.yFX < ((long)World.M_CONTACT_touchEpsilonFX * invTimestepFX) >> FXUtil.DECIMAL
                    || mDepth2FX < 0)
            {
                mRestitutionVec.yFX = 0;            
            }
        }                 

        long actualDepthFX = 0;
        //correct for nice impact
        if (mDepth1FX < 0)
        {            
            actualDepthFX = mDepth1FX - World.M_CONTACT_touchEpsilonFX;
            mCorrectVec.xFX = (int) ((actualDepthFX * invTimestepFX) >> FXUtil.DECIMAL);
        }
        else
        {
            mCorrectVec.xFX = 0;
        }
                
        if (! mSingle)
        {
            if (mDepth2FX < 0)
            {            
                actualDepthFX = mDepth2FX - World.M_CONTACT_touchEpsilonFX;
                mCorrectVec.yFX = (int) ((actualDepthFX * invTimestepFX) >> FXUtil.DECIMAL);
            }
            else
            {
                mCorrectVec.yFX = 0;
            }
        }
    }
    
    /**
     * @param timestepFX the timestep of the simulation
     * @param invTimestepFX the inverse timestep of the simulation
     */
    protected final void precalculatePositionCorrection(int timestepFX, long invTimestepFX)
    {
        //estimate new depths

        
        FXVector tempv1 = Contact.M_tempv1;
        FXVector tempv2 = Contact.M_tempv2;
        FXVector tempv3 = Contact.M_tempv3;
        
        mBody1.getVelocity(mB11c, tempv1);
        mBody2.getVelocity(mB21c, tempv2);
        FXVector velocityDiff = tempv3;
        velocityDiff.assignDiff(tempv1, tempv2);
        velocityDiff.multFX(timestepFX);
                
        int depth1FX = this.mDepth1FX - (int) velocityDiff.dotFX(mNormalDirection); 
        
        long actualDepthFX = 0;
        //overlap
        if (depth1FX > World.M_CONTACT_touchEpsilonFX)
        {            
            actualDepthFX = depth1FX - World.M_CONTACT_touchEpsilonFX;
            mCorrectVec.xFX = (int) (((( actualDepthFX * World.M_CONTACT_betaFX) >> FXUtil.DECIMAL) * (long)invTimestepFX) >> FXUtil.DECIMAL);
        }
        else
        {
            mCorrectVec.xFX = 0;
        }
    
        
        if (!mSingle)
        {
            mBody1.getVelocity(mB12c, tempv1);
            mBody2.getVelocity(mB22c, tempv2);
            FXVector velocityDiff2 = tempv3;
            velocityDiff2.assignDiff(tempv1, tempv2);
            velocityDiff2.multFX(timestepFX);
                    
            int depth2FX = this.mDepth2FX - (int) velocityDiff2.dotFX(mNormalDirection);
           
            if (depth2FX > World.M_CONTACT_touchEpsilonFX)
            {            
                actualDepthFX = depth2FX - World.M_CONTACT_touchEpsilonFX;
                mCorrectVec.yFX = (int) (((( actualDepthFX * World.M_CONTACT_betaFX) >> FXUtil.DECIMAL) * (long)invTimestepFX) >> FXUtil.DECIMAL);
            }
            else
            {
                mCorrectVec.yFX = 0;
            }
        }
        
        mAccumulatedVirtualLambdaVec.assignFX(0,0);
    }

    /*
     * The method is called iteratively by the simulation step. 
     * Eventually the applied effect on the bodies will converge towards zero.
     */
    /**
     * Applies the momentum of the collision. 
     * Uses the pre-calculated mass matrices and the current velocities of the bodies
     * to calculate and apply the impulse to satisfy the contact constraint. 
     */
    protected boolean applyMomentum()
    {           
        if (mSkipContact)
        {
            return true;
        }
        
        FXVector relativeVelocity1 = Contact.M_relativeVelocity1; 
        FXVector relativeVelocity2 = Contact.M_relativeVelocity2;
        Body body1 = this.mBody1;
        Body body2 = this.mBody2;
        FXVector normalDirection = this.mNormalDirection; 
        FXVector accumulatedLambdaVec = this.mAccumulatedLambdaVec;

        FXVector oldAccumLambdaVec = Contact.M_oldAccumLambdaVec;
        FXVector lambdaVec = Contact.M_lambdaVec;
        FXVector resubstituteLambdaVec = Contact.M_resubstituteLambdaVec;
        
        FXVector tempv1 = Contact.M_tempv1;
        FXVector tempv2 = Contact.M_tempv2;
                
        body1.getVelocity(mB11c, tempv1);
        body2.getVelocity(mB21c, tempv2);
        relativeVelocity1.assignDiff(tempv1, tempv2);
        if (!mSingle)
        {
            body1.getVelocity(mB12c, tempv1);
            body2.getVelocity(mB22c, tempv2);
            relativeVelocity2.assignDiff(tempv1, tempv2);            
        }        

        if (mSingle)
        {
            oldAccumLambdaVec.xFX = accumulatedLambdaVec.xFX;
            accumulatedLambdaVec.xFX += (int) ((((long) (-relativeVelocity1.dotFX(normalDirection) + mRestitutionVec.xFX + mCorrectVec.xFX)) << (FXUtil.DECIMAL2)) / mKMatrix.mCol1xFX);
                        
            accumulatedLambdaVec.xFX = Math.max(accumulatedLambdaVec.xFX, 0); 
            lambdaVec.xFX = accumulatedLambdaVec.xFX - oldAccumLambdaVec.xFX;
            
            applyImpulses(lambdaVec, normalDirection);
        }        
        else
        {
            FXVector jvFX = Contact.M_jvFX;
                      
            jvFX.xFX = - (int) relativeVelocity1.dotFX(normalDirection); 
            jvFX.yFX = - (int) relativeVelocity2.dotFX(normalDirection);
            jvFX.add(mRestitutionVec);        //restitution
            jvFX.add(mCorrectVec);            //overlap
            
            //this is due to calculation of block constraint on incremental impulse
            mKMatrix.mult(accumulatedLambdaVec, tempv1); 
            jvFX.add( tempv1 );
                        
            mNormalMassMatrix.mult(jvFX, lambdaVec);
            
            for(;;)
            {   
                //case 1: both contacts active, 
                if (lambdaVec.xFX >= 0 && lambdaVec.yFX >= 0)
                { 
                    break;
                }
                
                //case 2: contact 1 active
                lambdaVec.xFX = (int) (((long)jvFX.xFX << FXUtil.DECIMAL2) / mKMatrix.mCol1xFX);
                lambdaVec.yFX = 0;
                int relativeVelocityDotNyFX = (int) (((long)lambdaVec.xFX * mKMatrix.mCol1yFX) >> FXUtil.DECIMAL2) - jvFX.yFX; 
                
                if (lambdaVec.xFX >= 0 && relativeVelocityDotNyFX >= 0)
                {
                    break;
                }
                
                //case 3: contact 2 active
                lambdaVec.xFX = 0; 
                lambdaVec.yFX = (int) (((long)jvFX.yFX << FXUtil.DECIMAL2) / mKMatrix.mCol2yFX);
                int relativeVelocityDotNxFX = (int) (((long)lambdaVec.yFX * mKMatrix.mCol2xFX) >> FXUtil.DECIMAL2) - jvFX.xFX;
                
                if (lambdaVec.yFX >= 0 && relativeVelocityDotNxFX >= 0)
                {                    
                    break;
                }
                
                //case 4: both inactive
                lambdaVec.xFX = 0; 
                lambdaVec.yFX = 0;                
                break;
            }
            
            resubstituteLambdaVec.assignDiff(lambdaVec, accumulatedLambdaVec);
            
            applyImpulses(resubstituteLambdaVec, normalDirection);
            accumulatedLambdaVec.assign(lambdaVec);
        }
        
        //friction
        
        if ( ((World.M_iteration & 3) == 2) &&
            ! ( mFrictionFX == 0 || mTangentMassVec2FX.xFX == 0 ) )
        {
            
            FXVector tangentDirection = this.mTangentDirection; 
            FXVector tangentLambdaVec = Contact.M_tangentLambdaVec;
            FXVector accumulatedTangentLambdaVec = this.mAccumulatedTangentLambdaVec; 
                                
            FXVector maxTangentImpulseVec = M_tempv3;
            maxTangentImpulseVec.assignScaledFX(accumulatedLambdaVec, mFrictionFX);
            oldAccumLambdaVec.assign( accumulatedTangentLambdaVec );
            
            if (mDepth1FX >= 0)
            {
                body1.getVelocity(mB11c, tempv1);
                body2.getVelocity(mB21c, tempv2);
                relativeVelocity1.assignDiff(tempv1, tempv2);
                accumulatedTangentLambdaVec.xFX = accumulatedTangentLambdaVec.xFX - (int) (((long) relativeVelocity1.crossFX(normalDirection) << FXUtil.DECIMAL2) / mTangentMassVec2FX.xFX );
                
                if (accumulatedTangentLambdaVec.xFX < -maxTangentImpulseVec.xFX) accumulatedTangentLambdaVec.xFX = -maxTangentImpulseVec.xFX;
                if (accumulatedTangentLambdaVec.xFX > maxTangentImpulseVec.xFX) accumulatedTangentLambdaVec.xFX = maxTangentImpulseVec.xFX;
                
                tangentLambdaVec.xFX = accumulatedTangentLambdaVec.xFX - oldAccumLambdaVec.xFX;
            }
            else
            {
                tangentLambdaVec.xFX = 0;
                accumulatedTangentLambdaVec.xFX = 0;
            }
            
            if (!mSingle )
            {
                if(mDepth2FX >= 0)
                {
                    body1.getVelocity(mB12c, tempv1);
                    body2.getVelocity(mB22c, tempv2);
                    relativeVelocity2.assignDiff(tempv1, tempv2);
                    accumulatedTangentLambdaVec.yFX = accumulatedTangentLambdaVec.yFX - (int) (((long) relativeVelocity2.crossFX(normalDirection) << FXUtil.DECIMAL2) / mTangentMassVec2FX.yFX );
                    if (accumulatedTangentLambdaVec.yFX < -maxTangentImpulseVec.yFX) accumulatedTangentLambdaVec.yFX = -maxTangentImpulseVec.yFX;
                    if (accumulatedTangentLambdaVec.yFX > maxTangentImpulseVec.yFX) accumulatedTangentLambdaVec.yFX = maxTangentImpulseVec.yFX;
                    
                    tangentLambdaVec.yFX = accumulatedTangentLambdaVec.yFX - oldAccumLambdaVec.yFX;
                }
                else
                {
                    tangentLambdaVec.yFX = 0;
                    accumulatedTangentLambdaVec.yFX = 0;
                }
            }     
            applyImpulses(tangentLambdaVec, tangentDirection);             
        }
        
        //check if contact is sufficiently converged
        return Math.abs(resubstituteLambdaVec.xFX) < World.M_CONTACT_IterationConvergenceFX &&
               Math.abs(resubstituteLambdaVec.yFX) < World.M_CONTACT_IterationConvergenceFX;
        
    }
    
    /**
     * Applies the position correction to the involved bodies.
     * The concept of virtual velocities is used to correct the positions directly.  
     * @fx
     * @return the amount of correction
     */
    protected int applyMomentumPositionCorrectionFX()
    {
        //correct penetration
        if (mCorrectVec.xFX != 0 || (!mSingle && mCorrectVec.yFX != 0))
        {        
            FXVector oldAccumVirtualLambdaVec = Contact.M_oldAccumVirtualLambdaVec;
            FXVector virtualLambdaVec = Contact.M_virtualLambdaVec;
            FXVector relativeVirtualVelocity1 = Contact.M_relativeVirtualVelocity1; 
            FXVector relativeVirtualVelocity2 = Contact.M_relativeVirtualVelocity2;
            FXVector accumulatedVirtualLambdaVec = this.mAccumulatedVirtualLambdaVec;
            
            mBody1.getVirtualVelocity(mB11c, M_tempv1);
            mBody2.getVirtualVelocity(mB21c, M_tempv2);
            relativeVirtualVelocity1.assignDiff(M_tempv1, M_tempv2);
            if (!mSingle)
            {
                mBody1.getVirtualVelocity(mB12c, M_tempv1);
                mBody2.getVirtualVelocity(mB22c, M_tempv2);
                relativeVirtualVelocity2.assignDiff(M_tempv1, M_tempv2);            
            }            
        
            oldAccumVirtualLambdaVec.xFX = accumulatedVirtualLambdaVec.xFX;
            accumulatedVirtualLambdaVec.xFX += (int) ((((long) (-relativeVirtualVelocity1.dotFX(mNormalDirection) + mCorrectVec.xFX)) << (FXUtil.DECIMAL2)) / mKMatrix.mCol1xFX);
            accumulatedVirtualLambdaVec.xFX = Math.max(accumulatedVirtualLambdaVec.xFX, 0); 
            virtualLambdaVec.xFX = accumulatedVirtualLambdaVec.xFX - oldAccumVirtualLambdaVec.xFX;
            
            if (!mSingle)
            {
                oldAccumVirtualLambdaVec.yFX = accumulatedVirtualLambdaVec.yFX;
                accumulatedVirtualLambdaVec.yFX += (int) ((((long) (-relativeVirtualVelocity2.dotFX(mNormalDirection) + mCorrectVec.yFX)) << (FXUtil.DECIMAL2)) / mKMatrix.mCol2yFX);
                
                accumulatedVirtualLambdaVec.yFX = Math.max(accumulatedVirtualLambdaVec.yFX, 0);
                virtualLambdaVec.yFX = accumulatedVirtualLambdaVec.yFX - oldAccumVirtualLambdaVec.yFX;
            }
            
            applyVirtualImpulses(virtualLambdaVec, mNormalDirection);
            
            return virtualLambdaVec.fastLengthFX();
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Applies the the impulse to the bodies. 
     * Utility method. 
     * @param lambdaVec the impulse length vector (x is position 1, y is position 2)
     * @param direction the direction in which the force is applied (normal or perpendicular (e.g. for friction))
     */
    private final void applyImpulses(FXVector lambdaVec, FXVector direction)
    {
        FXVector addImpulse = Contact.M_addImpulse;
        addImpulse.assignScaledFX(direction, lambdaVec.xFX);
                    
        if (mBody1.mDynamic)
            mBody1.applyMomentumAt(addImpulse, mB11c);
        
        if (mBody2.mDynamic)
            mBody2.applyMomentumReverseAt(addImpulse, mB21c);
        
        if (!mSingle)
        {
            addImpulse.assignScaledFX(direction, lambdaVec.yFX);
                            
            if (mBody1.mDynamic)
                mBody1.applyMomentumAt(addImpulse, mB12c);
            
            if (mBody2.mDynamic)
                mBody2.applyMomentumReverseAt(addImpulse, mB22c);                        
        }
    }
    
    /**
     * Applies the the virtual impulse to the bodies. 
     * @param lambdaVec the impulse length vector (x is position 1, y is position 2)
     * @param direction the direction in which the force is applied
     */
    private final void applyVirtualImpulses(FXVector lambdaVec, FXVector direction)
    {
        FXVector addImpulse = Contact.M_addImpulse;
    
        addImpulse.assignScaledFX(direction, lambdaVec.xFX);
                    
        if (mBody1.mDynamic)
            mBody1.applyVirtualMomentumAt(addImpulse, mB11c);
        
        if (mBody2.mDynamic)
            mBody2.applyVirtualMomentumReverseAt(addImpulse, mB21c);
        
        if (!mSingle)
        {
            addImpulse.assignScaledFX(direction, lambdaVec.yFX);
                            
            if (mBody1.mDynamic)
                mBody1.applyVirtualMomentumAt(addImpulse, mB12c);
            
            if (mBody2.mDynamic)
                mBody2.applyVirtualMomentumReverseAt(addImpulse, mB22c);
        }
    }

    /**
     * Checks is the contact applies to a body. 
     * @param body the body for which to check
     * @return true if this constraint applies to the body.  
     */
    public boolean concernsBody(Body body) 
    {
        return mBody1 == body || mBody2 == body;
    }
 
    /**
     * Gets the last acting impulse of the contact (position 1). 
     * @fx
     * @return the last acting impulse on the contact at position 1
     */
    public int getImpulseContact1FX()
    {
        return mAccumulatedLambdaVec.xFX;
    }
    
    /**
     * Gets the last acting impulse of the contact (position 2). 
     * This is only available if the contact is a double contact. 
     * If the contact is a single contact, 0 is returned.
     * @fx
     * @return the last acting impulse on the contact at position 2
     */
    public int getImpulseContact2FX()
    {
        return mSingle ? 0 : mAccumulatedLambdaVec.yFX;
    }
    
    //apply accumulated impulse
    static void applyAccumImpulses(Contact[] contacts, int contactCount)
    {
        for( int i = 0; i < contactCount; i++ )
        {
            Contact c = contacts[i];           
            
            c.applyImpulses(c.mAccumulatedLambdaVec, c.mNormalDirection);
            c.applyImpulses(c.mAccumulatedTangentLambdaVec, c.mTangentDirection);
        }
    }
    
    //check after accumimpulses application -> better relative velocity estimates
    //check for validity of contacts in spe
    //Ball rolling along broken line might get wrong contact otherwise. 
    //#NoEco /*
    static void checkAllContacts(Contact[] contacts, int contactCount, Landscape landscape)
    {
        boolean checkLandscapes = landscape != null;
        Body landscapeBody = landscape.getBody();
        
        for( int i = 0; i < contactCount; i++ )
        {
            Contact c = contacts[i];
            
            if (c.mDepth1FX < 0) //check whether to use the future contact
            {
                Body body1 = c.mBody1;
                Body body2 = c.mBody2;
                //determine relative velocity in contact pos1
                                                              
                if (body1.mShape.mVertices.length == 1)
                {
                    M_tempv1.assign(body1.mVelocityFX);    //we take the center velocity of the circle
                }
                else
                {
                    body1.getVelocity(c.mB11c, M_tempv1);
                }
                
                if (body2.mShape.mVertices.length == 1)
                {
                    M_tempv2.assign(body2.mVelocityFX);    //we take the center velocity of the circle
                }
                else
                {
                    body2.getVelocity(c.mB21c, M_tempv2);
                }                
                
                M_tempv3.assignDiff(M_tempv1, M_tempv2);
                M_tempv3.normalize();
                
                long intersectionDepthFX = Integer.MAX_VALUE;                
                if (body1.mShape.mVertices.length == 1)
                {
                    if (body2.mShape.mVertices.length == 1)
                    {
                        //do nothing for circle-circle collisions, contact is always ok
                        continue;
                    }
                    else
                    {
                        FXVector[] vertices = body2.getVertices();
                        if (body2 == landscapeBody)
                        {
                            vertices = M_lineVertices;
                            landscape.fillVertices(vertices, c, c.mB2Index);
                        }
                        intersectionDepthFX = intervalIntersectFX(vertices, vertices.length, body1.mPositionFX, body1.mShape.mBoundingRadiusFX, M_tempv3);
                    }
                }
                else
                {
                    if (body2.mShape.mVertices.length == 1)
                    {
                        FXVector[] vertices = body1.getVertices();
                        if (body1 == landscapeBody)
                        {
                            vertices = M_lineVertices;
                            landscape.fillVertices(vertices, c, c.mB1Index);
                        }
                        intersectionDepthFX = intervalIntersectFX(vertices, vertices.length, body2.mPositionFX, body2.mShape.mBoundingRadiusFX, M_tempv3);
                    }
                    else
                    {
                        FXVector[] vertices1 = body1.getVertices();
                        FXVector[] vertices2 = body2.getVertices();
                        if (body1 == landscapeBody)
                        {
                            vertices1 = M_lineVertices;
                            landscape.fillVertices(vertices1, c, c.mB1Index);
                        }
                        else if (body2 == landscapeBody)
                        {
                            vertices2 = M_lineVertices;
                            landscape.fillVertices(vertices2, c, c.mB2Index);
                        }               
                        intersectionDepthFX = intervalIntersectFX(vertices1, vertices1.length, vertices2, vertices2.length, M_tempv3);                        
                    }
                }
                
                if (intersectionDepthFX < World.M_CONTACT_touchEpsilonSlack2FX)
                {
                    c.mSkipContact = true;
                }
            }
        } 
    }
    
    private static long intervalIntersectFX(FXVector vertices1[], int vertices1Count, FXVector vertices2[], int vertices2Count, FXVector axis)
    {
        if (vertices1Count == 0 || vertices2Count == 0)
            return -1;
            
        long dFX = 0;
        long min1FX = vertices1[0].crossFX(axis); 
        long max1FX = min1FX;
        for(int i = 1; i < vertices1Count; i ++)
        {
            dFX = vertices1[i].crossFX(axis);
            if (dFX < min1FX) 
                min1FX = dFX; 
            else if (dFX > max1FX) 
                max1FX = dFX;
        }
        
        long min2FX = vertices2[0].crossFX(axis);
        long max2FX = min2FX;
        for(int i = 1; i < vertices2Count; i ++)
        {
            dFX = vertices2[i].crossFX(axis);
            if (dFX < min2FX) 
                min2FX = dFX; 
            else if (dFX > max2FX) 
                max2FX = dFX;
        }

        long d1FX = min1FX - max2FX; 
        long d2FX = min2FX - max1FX;
        if ( d1FX > 0 || d2FX > 0) 
        {
            return -1;
        }
            
        return d1FX > d2FX ? -d1FX : -d2FX;
    }
    
    private static long intervalIntersectFX(FXVector vertices1[], int vertices1Count, FXVector center, int radiusFX, FXVector axis)
    {
        if (vertices1Count == 0 )
            return -1;
        
        
        long dFX = 0;
        long min1FX = vertices1[0].crossFX(axis);
        long max1FX = min1FX;
        for(int i = 1; i < vertices1Count; i ++)
        {
            dFX = vertices1[i].crossFX(axis);
            if (dFX < min1FX) 
                min1FX = dFX; 
            else if (dFX > max1FX) 
                max1FX = dFX;
        }
        
        long min2FX = center.crossFX(axis) - radiusFX;
        long max2FX = center.crossFX(axis) + radiusFX;
        
        long d1FX = min1FX - max2FX; 
        long d2FX = min2FX - max1FX;
        if ( d1FX > 0 || d2FX > 0) 
        {
            return -1;
        }
            
        return d1FX > d2FX ? -d1FX : -d2FX;
    }
    //#NoEco */
}
