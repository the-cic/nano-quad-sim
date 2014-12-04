
package com.cic.quadsim.scene;

import com.cic.quadsim.MaterialLibrary;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author cic
 */
public class Tree extends Node {
    
    private static int treeCount = 0;
    
    private RigidBodyControl rBControl;
    
    public Tree(Material material, float x, float y, float height) {
        super("Tree-" + treeCount++);
        Node inNode;
        Mesh mesh;
        Geometry geom;

        for (int i = 0; i < 4; i++) {
            mesh = new Quad(1f, 1f);
            geom = new Geometry("sideA", mesh);
            geom.setMaterial(material);
            geom.setShadowMode(RenderQueue.ShadowMode.Off);
            geom.move(-0.5f, 0, 0);
            geom.setQueueBucket(RenderQueue.Bucket.Transparent);

            inNode = new Node("nodeA");
            inNode.attachChild(geom);
            inNode.rotate(0, (i / 4f) * (float) Math.PI * 2, 0);

            attachChild(inNode);
        }

        scale(height);
        setLocalTranslation(x, 1, y);

        rBControl = new RigidBodyControl(0.0f);
        addControl(rBControl);
        rBControl.setCollisionShape(new BoxCollisionShape(new Vector3f(0.02f, 1f, 0.02f).mult(height)));
        
        //bulletAppState.getPhysicsSpace().add(rBControl);
    }
    
    public RigidBodyControl getRBControl(){
        return rBControl;
    }

    public static Node makeTrees(MaterialLibrary matLibrary) {
        Node node = new Node("Trees");

        node.attachChild(new Tree(matLibrary.tree, 54, -325, 50));
        node.attachChild(new Tree(matLibrary.tree, 149, -254, 100));
        node.attachChild(new Tree(matLibrary.tree, 76, -123, 70));
        node.attachChild(new Tree(matLibrary.tree, -55, -321, 80));
        node.attachChild(new Tree(matLibrary.tree, 134, -483, 90));

        return node;
    }    
}
