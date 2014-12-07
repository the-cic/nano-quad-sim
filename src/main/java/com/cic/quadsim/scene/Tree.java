
package com.cic.quadsim.scene;

import com.cic.quadsim.MaterialLibrary;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.Ray;
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
    
    public Tree(Material material, float x, float z, float height) {
        super("Tree-" + treeCount++);
        Node midNode = new Node("midnode");
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

            midNode.attachChild(inNode);
        }

        midNode.scale(height);
        attachChild(midNode);
        setLocalTranslation(x, 0, z);

        rBControl = new RigidBodyControl(0.0f);
        addControl(rBControl);
        rBControl.setCollisionShape(new BoxCollisionShape(new Vector3f(0.02f, 1f, 0.02f).mult(height)));
        
        //bulletAppState.getPhysicsSpace().add(rBControl);
    }
    
    public RigidBodyControl getRBControl(){
        return rBControl;
    }
    
    private static Tree groundTree(Tree tree, Node groundNode){
        Vector3f location = tree.getLocalTranslation();
        Ray ray = new Ray(new Vector3f(location.x, 1000, location.z), new Vector3f(0, -1, 0));
        CollisionResults results = new CollisionResults();
        groundNode.collideWith(ray, results);
        
        float y = location.y;
        if (results.size() > 0) {
            float distance = results.getClosestCollision().getDistance();
            location.y = 1000 - distance;
        }
        
        tree.getRBControl().setPhysicsLocation(location);
        
        return tree;
    }

    public static Node makeTrees(Node ground, MaterialLibrary matLibrary) {
        Node node = new Node("Trees");

        node.attachChild(groundTree(new Tree(matLibrary.tree, 54, -325, 50), ground));
        node.attachChild(groundTree(new Tree(matLibrary.tree, 149, -254, 100), ground));
        node.attachChild(groundTree(new Tree(matLibrary.tree, 76, -123, 70), ground));
        node.attachChild(groundTree(new Tree(matLibrary.tree, -55, -321, 80), ground));
        node.attachChild(groundTree(new Tree(matLibrary.tree, 134, -483, 90), ground));

        return node;
    }    
}
