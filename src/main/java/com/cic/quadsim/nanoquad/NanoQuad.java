package com.cic.quadsim.nanoquad;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * What looks a bit like and behaves somewhat like a Cheerson CX-10 nano
 * quadcopter.
 *
 * @author cic
 */
public class NanoQuad extends Node {

    private NanoQuadProp propBR, propBL, propFR, propFL;
    private Battery battery;
    private RigidBodyControl rbControl;
    private float throttle, rudder, elevator, alieron;
    private SphereCollisionShape collisionShape;
    private float collisionShapeSize = 0.55f;
    private float scale = 0.32f / 2;

    public NanoQuad(String name,
            Material topMaterial, Material bottomMaterial,
            Material frontPropsMaterial, Material rearPropsMaterial,
            Material frontLEDMaterial, Material rearLEDMaterial) {
        super(name);

        Node aircraftShapeNode = new Node("aircraftShape");

        // Scale to 32 mm between props, which are at coord 1 -> distance between props is 2

        aircraftShapeNode.attachChild(new NanoQuadBody("aircraft body", topMaterial, bottomMaterial).scale(scale));

        propBR = (NanoQuadProp) new NanoQuadProp("Prop1", rearPropsMaterial)
                .move(1f * scale, 0.4f * scale, 1f * scale)
                .scale(scale);
        propBL = (NanoQuadProp) new NanoQuadProp("Prop2", rearPropsMaterial)
                .move(-1f * scale, 0.4f * scale, 1f * scale)
                .scale(scale);
        propFR = (NanoQuadProp) new NanoQuadProp("Prop3", frontPropsMaterial)
                .move(1f * scale, 0.4f * scale, -1f * scale)
                .scale(scale);
        propFL = (NanoQuadProp) new NanoQuadProp("Prop4", frontPropsMaterial)
                .move(-1f * scale, 0.4f * scale, -1f * scale)
                .scale(scale);

        propFL.setSpinDirection(-1);
        propBR.setSpinDirection(-1);

        aircraftShapeNode.attachChild(propBR);
        aircraftShapeNode.attachChild(propBL);
        aircraftShapeNode.attachChild(propFR);
        aircraftShapeNode.attachChild(propFL);

        aircraftShapeNode.attachChild(new NanoQuadMotorPod("Pod1", -1, -1, topMaterial, bottomMaterial, frontLEDMaterial)
                .move(-1f * scale, 0.0f * scale, -1f * scale)
                .scale(scale));
        aircraftShapeNode.attachChild(new NanoQuadMotorPod("Pod2", 1, -1, topMaterial, bottomMaterial, frontLEDMaterial)
                .move(1f * scale, 0.0f * scale, -1f * scale)
                .scale(scale));
        aircraftShapeNode.attachChild(new NanoQuadMotorPod("Pod3", -1, 1, topMaterial, bottomMaterial, rearLEDMaterial)
                .move(-1f * scale, 0.0f * scale, 1f * scale)
                .scale(scale));
        aircraftShapeNode.attachChild(new NanoQuadMotorPod("Pod4", 1, 1, topMaterial, bottomMaterial, rearLEDMaterial)
                .move(1f * scale, 0.0f * scale, 1f * scale)
                .scale(scale));

        /* If we don't do this ridiculous thing, the physics goes mad  */
        Node emptyNode = new Node("empty");
        Box b = new Box(1f, 1f, 1f);
        Geometry geom = new Geometry("Empty", b);
        geom.setMaterial(bottomMaterial);
        emptyNode.attachChild(geom);

        aircraftShapeNode.attachChild(emptyNode);

        this.attachChild(aircraftShapeNode);
        this.setShadowMode(RenderQueue.ShadowMode.Cast);

        rbControl = new RigidBodyControl(.3f);
        this.addControl(rbControl);
        
        CompoundCollisionShape cShape = new CompoundCollisionShape();
        collisionShape = new SphereCollisionShape(collisionShapeSize * scale);
        cShape.addChildShape(collisionShape, new Vector3f(-1 * scale, 0.1f * scale, -1 * scale));
        cShape.addChildShape(collisionShape, new Vector3f( 1 * scale, 0.1f * scale, -1 * scale));
        cShape.addChildShape(collisionShape, new Vector3f(-1 * scale, 0.1f * scale,  1 * scale));
        cShape.addChildShape(collisionShape, new Vector3f( 1 * scale, 0.1f * scale,  1 * scale));
        rbControl.setCollisionShape((CollisionShape)cShape);
        
        rbControl.setRestitution(0.09f);
        rbControl.setDamping(0.1f, 0.0f);

        /* This is part of the ridiculous thing */
        this.getChild("Empty").removeFromParent();
        
        /* Have 100 mAh battery */
        this.battery = new Battery(100);
    }
    
    public Battery getBattery(){
        return battery;
    }
    
    public float getThrottleValue(){
        return throttle;
    }

    public RigidBodyControl getRBControl() {
        return rbControl;
    }

    public void animate(float tpf, float throttle) {
        propBR.animate(tpf, throttle * (1 - elevator * 0.3f + alieron * 0.3f + rudder * 0.3f));
        propBL.animate(tpf, throttle * (1 - elevator * 0.3f - alieron * 0.3f - rudder * 0.3f));
        propFR.animate(tpf, throttle * (1 + elevator * 0.3f + alieron * 0.3f - rudder * 0.3f));
        propFL.animate(tpf, throttle * (1 + elevator * 0.3f - alieron * 0.3f + rudder * 0.3f));
    }
    
    public void applyControls(float tpf, float throttleV, float rudderV, float elevatorV, float alieronV){
        // For some reason the simulation is drifting, these trims are to compensate that
        final float rudderTrim = 0.004f;
        final float elevatorTrim = 0.004f;
        final float alieronTrim = 0.004f;

        this.throttle = throttleV;
        this.rudder = rudderV + rudderTrim;
        this.elevator = elevatorV + elevatorTrim;
        this.alieron = alieronV + alieronTrim;
        
        // Throttle under 0.4 is useless, so start at that.
        float throttleStart = 0.4f;

        // To more approximate an rc controller's throttle on the gamepad
        // from   0 .. 1
        // to   0.4 .. 1
        throttle = throttle < 0.01
                ? 0
                : throttleStart + (FastMath.pow(throttle, 1.5f) * (1 - throttleStart));

        throttle = battery.getPower(tpf, throttle);
        
        Quaternion globalRotation = this.getWorldRotation();

        float targetPitch = elevator / 1.7f;
        float targetRoll = alieron / 1.7f;

        float[] eulerAngles = globalRotation.toAngles(null);
        float pitch = eulerAngles[0];
        float roll = eulerAngles[2];

        Vector3f linearVelocity = rbControl.getLinearVelocity();
        float velocityMagnitude = linearVelocity.length();
        
        Vector3f angVel = rbControl.getAngularVelocity(); // global angular velocity
        angVel = globalRotation.inverse().mult(angVel); // local
        
        final float angVelCompensation = 0.3f;
        final float yawAngVelCompensation = 0.8f;
        final float pitchRollForceFactor = 10;
        final float yawForceFactor = 5;
        
        float pitchForce = (targetPitch - pitch - angVel.x * angVelCompensation) * pitchRollForceFactor;
        float rollForce  = (targetRoll  - roll  - angVel.z * angVelCompensation) * pitchRollForceFactor;
        float yawForce   = (rudder              - angVel.y * yawAngVelCompensation) * yawForceFactor;

        Vector3f localUpVector = globalRotation.mult(new Vector3f(0, 1, 0));

        float engineDistance = 1 * 0.32f;
        Vector3f engineLeftPosition  = globalRotation.mult(new Vector3f(-engineDistance, 0,               0));
        Vector3f engineRightPosition = globalRotation.mult(new Vector3f( engineDistance, 0,               0));
        Vector3f engineFrontPosition = globalRotation.mult(new Vector3f(              0, 0, -engineDistance));
        Vector3f engineBackPosition  = globalRotation.mult(new Vector3f(              0, 0,  engineDistance));

        final float hoverForce = 2;
        final float forceFactor = 0.7f;
        final float torqueFactor = 0.9f;
        
        rbControl.applyForce(localUpVector.mult((hoverForce - rollForce)  * throttle * forceFactor), engineLeftPosition);
        rbControl.applyForce(localUpVector.mult((hoverForce + rollForce)  * throttle * forceFactor), engineRightPosition);
        rbControl.applyForce(localUpVector.mult((hoverForce + pitchForce) * throttle * forceFactor), engineFrontPosition);
        rbControl.applyForce(localUpVector.mult((hoverForce - pitchForce) * throttle * forceFactor), engineBackPosition);

        rbControl.applyTorque(localUpVector.mult((yawForce) * throttle * torqueFactor));
        
        // Dynamic air resistance
        float dynamicFriction = FastMath.pow(velocityMagnitude / 90, 2);
        rbControl.applyForce(linearVelocity.negate().mult(dynamicFriction), Vector3f.ZERO);
        
        // Compensate for small size of rigid body when velocity is high
        float csScale = velocityMagnitude / 5;
        csScale = csScale < 1 
                ? 1 
                : ( csScale > 5 
                    ? 5 
                    : csScale);
        
        collisionShape.setScale(new Vector3f(csScale, csScale, csScale));
        
        animate(tpf, throttle);
    }
}
