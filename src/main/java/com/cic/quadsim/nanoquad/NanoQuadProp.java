package com.cic.quadsim.nanoquad;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author cic
 */
public class NanoQuadProp extends Node {

    private Material propMaterial;
    private Material propBlurMaterial;
    private float spinDirection = 1;
    private ColorRGBA propBlurMultColor = new ColorRGBA(1, 1, 1, 1);
    private ColorRGBA propMultColor = new ColorRGBA(1, 1, 1, 1);
    private Geometry propBlur;
    private boolean propBlurVisible = true;

    public NanoQuadProp(String name, Material material) {
        super(name);

        Node propCenterNode = new Node("center");
        Node propBladesNode = new Node("blades");
        Node propBluredBladesNode = new Node("blur");

        Dome propCenter = new Dome(Vector3f.ZERO, 4, 4, 1f, false);
        Geometry geom = new Geometry(name + " center", propCenter);
        geom.setMaterial(material);
        geom.move(0, -0.1f, 0);
        geom.scale(0.1f, 0.5f, 0.1f);
        propCenterNode.attachChild(geom);

        propMaterial = material.clone();
        propMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Sphere sphere = new Sphere(4, 4, 1f);
        geom = new Geometry(name + " prop 1", sphere);
        geom.setMaterial(propMaterial);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.scale(0.5f, 0.05f, 0.15f);
        geom.move(0.5f, 0.15f, 0);
        propBladesNode.attachChild(geom);

        sphere = new Sphere(4, 4, 1f);
        geom = new Geometry(name + " prop 2", sphere);
        geom.setMaterial(propMaterial);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.scale(0.5f, 0.05f, 0.15f);
        geom.move(-0.5f, 0.15f, 0);
        propBladesNode.attachChild(geom);

        propBlurMaterial = material.clone();
        propBlurMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        sphere = new Sphere(6, 4, 1f);
        geom = new Geometry(name + " blur shape", sphere);
        geom.setMaterial(propBlurMaterial);
        geom.move(0, 0.15f, 0);
        geom.scale(1f, 0.1f, 1f);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setShadowMode(RenderQueue.ShadowMode.Off);
        propBluredBladesNode.attachChild(geom);
        
        propBlur = geom;
        
        setPropBlurVisible(false);

        this.attachChild(propCenterNode);
        this.attachChild(propBladesNode);
        this.attachChild(propBluredBladesNode);
    }

    public void setSpinDirection(float v) {
        spinDirection = v;
    }

    public void animate(float tpf, float throttle) {
        float rotorSpeed = tpf * 200 * throttle;
        if (rotorSpeed > tpf * 60) {
            rotorSpeed = tpf * 60;
        }

        this.rotate(0, rotorSpeed * spinDirection, 0);

        propMultColor.a = throttle < 0.2 ? 1 : 1 - (throttle - 0.2f) * 1.1f;
        propBlurMultColor.a = throttle > 0.1 ? (throttle - 0.1f) * 0.2f : 0;

        propMaterial.setColor("Diffuse", ((ColorRGBA) propMaterial.getParam("Ambient").getValue()).mult(propMultColor));
        propBlurMaterial.setColor("Diffuse", ((ColorRGBA) propBlurMaterial.getParam("Ambient").getValue()).mult(propBlurMultColor));
        setPropBlurVisible(propBlurMultColor.a > 0);
    }
    
    private void setPropBlurVisible(boolean v){
        if (propBlurVisible && !v) {
            propBlurVisible = false;
            propBlur.setCullHint(CullHint.Always);
        } else if (!propBlurVisible && v) {
            propBlurVisible = true;
            propBlur.setCullHint(CullHint.Dynamic);
        }
    }
}
