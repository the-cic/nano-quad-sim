
package com.cic.quadsim.scene;

import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author cic
 */
public class Silhouette extends Node {
    
    public Silhouette(String name, Material material, float x, float y, float z, float rot) {
        super(name);

        Mesh box = new Quad(10f, 22.5f);
        Geometry boxGeom = new Geometry("cutout-front", box);
        boxGeom.setMaterial(material);
        boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
        boxGeom.move(-10f / 2, 0, 0);
        boxGeom.setQueueBucket(RenderQueue.Bucket.Transparent);

        Node inNode = new Node("node-front");
        inNode.attachChild(boxGeom);
        inNode.rotate(0, rot, 0);

        attachChild(inNode);

        box = new Quad(10f, 22.5f);
        boxGeom = new Geometry("cutout-back", box);
        boxGeom.setMaterial(material);
        boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
        boxGeom.move(-10f / 2, 0, 0);
        boxGeom.setQueueBucket(RenderQueue.Bucket.Transparent);

        inNode = new Node("node-back");
        inNode.attachChild(boxGeom);
        inNode.rotate(0, rot + (float) Math.PI, 0);

        attachChild(inNode);
        scale(17.3f / 22.5f);
        setLocalTranslation(x, y, z);
    }    
}
