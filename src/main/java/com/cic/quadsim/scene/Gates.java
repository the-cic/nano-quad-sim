package com.cic.quadsim.scene;

import com.cic.quadsim.MaterialLibrary;
import com.cic.quadsim.nanoquad.NanoQuad;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Gates that can be used for races, that need to be crossed from one side to
 * the other.
 *
 * @author cic
 */
public class Gates extends Node {

    private MaterialLibrary materials;

    private Node visibleShapes;
    private Node collisionShapes;
    
    private ArrayList<String> gates = new ArrayList<String>();

    public Gates(MaterialLibrary materials) {
        super("Gates");
        this.materials = materials;
        visibleShapes = new Node("visible shapes");
        collisionShapes = new Node("collision shapes");
        attachChild(visibleShapes);
        attachChild(collisionShapes);
        collisionShapes.setCullHint(cullHint.Always);
        reset();
    }

    private void reset() {
    }

    public void addGate(String name, float x, float y, float z, float width, float height, float rotY) {
        Node plane = makePlane(name, x, y, z, width, height, rotY);
        plane.setMaterial(materials.blueLED);

        collisionShapes.attachChild(plane);

        plane = makePlane(name + "-front", x, y, z, width, height, rotY);
        plane.setMaterial(materials.gateFront);
        plane.setQueueBucket(RenderQueue.Bucket.Transparent);

        visibleShapes.attachChild(plane);

        plane = makePlane(name + "-back", x, y, z, width, height, rotY + (float) Math.PI);
        plane.setMaterial(materials.gateBack);
        plane.setQueueBucket(RenderQueue.Bucket.Transparent);

        visibleShapes.attachChild(plane);
        gates.add(name);
    }
    
    public void setActive(String activeGateName){
        for (String gateName : gates) {
            if (gateName.equals(activeGateName)) {
                visibleShapes.getChild(gateName + "-front").setMaterial(materials.gateFront);
            } else {
                visibleShapes.getChild(gateName + "-front").setMaterial(materials.gateBack);
            }
        }
    }

    private Node makePlane(String name, float x, float y, float z, float width, float height, float rotY) {
        Quad quad = new Quad(width, height);
        Geometry geom = new Geometry(name, quad);
        Node node = new Node(name);

        geom.move(-width / 2, 0, 0);
        node.attachChild(geom);
        node.rotate(0, rotY, 0);
        node.setLocalTranslation(x, y, z);

        return node;
    }

    public String test(RigidBodyControl rbControl, float tpf) {
        try {
            Vector3f position = rbControl.getPhysicsLocation();
            Vector3f velocity = rbControl.getLinearVelocity();
            Vector3f previousPosition = position.subtract(velocity.mult(tpf * 5));

            float speed = velocity.length();
            Vector3f direction = velocity.normalize();
            Vector3f reverseDirection = direction.negate();

            Ray ray1 = new Ray(previousPosition, direction);
            Ray ray2 = new Ray(position, reverseDirection);
            ray1.setLimit(speed);
            ray2.setLimit(speed);

            CollisionResults results = new CollisionResults();
            collisionShapes.collideWith(ray1, results);
            if (results.size() > 0) {
                CollisionResult collision1 = results.getClosestCollision();
                //String msg = "c1: "+collision1.getDistance();
                results = new CollisionResults();
                collisionShapes.collideWith(ray2, results);
                if (results.size() > 0) {
                    CollisionResult collision2 = results.getClosestCollision();
                    //msg += ", c2: "+collision2.getDistance();
                    //System.out.println(collision1.getGeometry() == collision2.getGeometry());
                    if (collision1.getGeometry() == collision2.getGeometry()) {
                        //msg += ", BINGO!";
                        return collision1.getGeometry().getName();
                    }
                }
                //System.out.println(msg);
                //panel.setMessage("gate", msg);
            }
        } catch (Exception e) {
            /*if (panel != null) {
             panel.setMessage("exception", e.toString());
             }*/
        }
        return null;
    }
}
