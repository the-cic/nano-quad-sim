package com.cic.quadsim.nanoquad;

import com.cic.quadsim.shapes.StarDome;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * @author cic
 */
public class NanoQuadBody extends Node {

    public NanoQuadBody(String name, Material topMaterial, Material bottomMaterial) {
        super(name);

        Mesh dm = new StarDome(Vector3f.ZERO, 6, 32, 1f, false);
        Geometry geom = new Geometry("BodyTop", dm);
        geom.setMaterial(topMaterial);
        geom.rotate(0, (float) Math.PI / 4, 0);
        geom.scale(1.4f, 0.24f, 1.4f);
        geom.move(0, 0.0f, 0);
        this.attachChild(geom);

        dm = new StarDome(Vector3f.ZERO, 6, 32, 1f, false);
        geom = new Geometry("BodyBottom", dm);
        geom.setMaterial(bottomMaterial);
        geom.rotate(0, (float) Math.PI / 4, 0);
        geom.rotate((float) Math.PI, 0, 0);
        geom.scale(1.4f, 0.24f, 1.4f);
        geom.move(0, 0.0f, 0);
        this.attachChild(geom);

        Box b = new Box(0.6f, 0.2f, 0.6f);
        geom = new Geometry("Battery", b);
        geom.setMaterial(bottomMaterial);
        geom.move(0f, -0.25f, 0f);
        this.attachChild(geom);

        b = new Box(0.15f, 0.1f, 0.2f);
        geom = new Geometry("ChargePort", b);
        geom.setMaterial(topMaterial);
        geom.move(0f, 0.1f, 0.5f);
        this.attachChild(geom);
    }
}
