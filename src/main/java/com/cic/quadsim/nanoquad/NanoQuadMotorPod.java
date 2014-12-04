package com.cic.quadsim.nanoquad;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

/**
 *
 * @author cic
 */
public class NanoQuadMotorPod extends Node {

    public NanoQuadMotorPod(String name, float ledXDirection, float ledZDirection, Material topMaterial, Material bottomMaterial, Material ledMaterial) {
        super(name);

        Cylinder cy = new Cylinder(4, 6, 0.2f, 0.25f, 0.3f, true, false);
        Geometry geom = new Geometry(name + " up", cy);
        geom.setMaterial(topMaterial);
        geom.rotate(-(float) Math.PI / 2, 0, 0);
        geom.move(0, 0.16f, 0.0f);
        this.attachChild(geom);

        cy = new Cylinder(4, 6, 0.25f, 0.2f, 0.45f, true, false);
        geom = new Geometry(name + " dn", cy);
        geom.setMaterial(bottomMaterial);
        geom.rotate(-(float) Math.PI / 2, 0, 0);
        geom.move(0, -0.22f, 0.0f);
        this.attachChild(geom);

        cy = new Cylinder(4, 6, 0.15f, 0.15f, 0.2f, true, false);
        geom = new Geometry(name + " LED", cy);
        geom.setMaterial(ledMaterial);
        geom.rotate(-(float) Math.PI / 2, 0, 0);
        geom.move(
                Math.signum(ledXDirection) * 0.1f,
                0.0f,
                Math.signum(ledZDirection) * 0.1f);
        this.attachChild(geom);
    }
}
