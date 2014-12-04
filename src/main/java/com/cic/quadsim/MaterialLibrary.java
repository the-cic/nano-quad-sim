/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cic.quadsim;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

/**
 *
 * @author mirko
 */
public class MaterialLibrary {
    public Material whitePlastic;
    public Material orangePlastic;
    public Material redPlastic;
    public Material redLED;
    public Material blueLED;
    
    public Material grass;
    public Material grassB;
    public Material grassSolid;
    public Material grassTerrain;
    public Material silhouette;
    public Material tree;
    
    public Texture grassTexture;
    public Texture grassNormalTexture;
    public Texture dirtTexture;
    public Texture concreteTexture;
    public Texture silhouetteTexture;
    public Texture treeTexture;
    public Texture treeAlphaTexture;
    
    public Material concrete;
    
    public ColorRGBA greenColor = new ColorRGBA(108f / 255f, 117f / 255f, 67f / 255f, 1);
    public ColorRGBA redOrangeColor = new ColorRGBA(255f / 255f, 99f / 255f, 63f / 255f, 1f);
    public ColorRGBA darkRedColor = new ColorRGBA(208f / 255f, 8f / 255f, 8f / 255f, 1f);

    public MaterialLibrary(AssetManager assetManager){
        whitePlastic = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        whitePlastic.setBoolean("UseMaterialColors", true);
        whitePlastic.setColor("Diffuse", ColorRGBA.White);
        whitePlastic.setColor("Specular", ColorRGBA.White);
        whitePlastic.setColor("Ambient", ColorRGBA.White);
        whitePlastic.setFloat("Shininess", 64f);  // [0,128]

        orangePlastic = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        orangePlastic.setBoolean("UseMaterialColors", true);
        orangePlastic.setColor("Diffuse", redOrangeColor);
        orangePlastic.setColor("Specular", redOrangeColor);
        orangePlastic.setColor("Ambient", redOrangeColor);
        orangePlastic.setFloat("Shininess", 64f);  // [0,128]
        
        redPlastic = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        redPlastic.setBoolean("UseMaterialColors", true);
        redPlastic.setColor("Diffuse", darkRedColor);
        redPlastic.setColor("Specular", darkRedColor);
        redPlastic.setColor("Ambient", darkRedColor);
        redPlastic.setFloat("Shininess", 64f);  // [0,128]
        
        redLED = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        redLED.setColor("Color",ColorRGBA.Red.mult(2));
        redLED.setColor("GlowColor",ColorRGBA.Red.mult(5));
        
        blueLED = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blueLED.setColor("Color",ColorRGBA.Cyan.mult(2));
        blueLED.setColor("GlowColor",ColorRGBA.Cyan.mult(5));
        
        grassTexture = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grassNormalTexture = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
        dirtTexture = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        concreteTexture = assetManager.loadTexture("Textures/concrete.jpg");
        silhouetteTexture = assetManager.loadTexture("Textures/silhouette.jpg");
        treeTexture = assetManager.loadTexture("Textures/tree.jpg");
        treeAlphaTexture = assetManager.loadTexture("Textures/tree-alpha.jpg");
        
        grassTexture.setWrap(Texture.WrapMode.Repeat);
        grassNormalTexture.setWrap(Texture.WrapMode.Repeat);
        dirtTexture.setWrap(Texture.WrapMode.Repeat);
        concreteTexture.setWrap(Texture.WrapMode.Repeat);
        
        grass = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
        grass.setTexture("DiffuseMap", grassTexture);
        grass.setTexture("NormalMap", grassNormalTexture);
        grass.setBoolean("UseMaterialColors",true);
        grass.setColor("Diffuse", greenColor);
        grass.setColor("Specular", ColorRGBA.White.mult(0.1f));
        grass.setColor("Ambient", ColorRGBA.White);
        grass.setFloat("Shininess", 64f);  // [0,128]
        
        grassB = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
        grassB.setTexture("DiffuseMap", grassTexture);
        grassB.setTexture("NormalMap", grassNormalTexture);
        grassB.setBoolean("UseMaterialColors",true);
        grassB.setColor("Diffuse", greenColor);
        grassB.setColor("Specular", ColorRGBA.White.mult(0.1f));
        grassB.setColor("Ambient", ColorRGBA.White);
        grassB.setFloat("Shininess", 64f);  // [0,128]

        grassSolid = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
        grassSolid.setBoolean("UseMaterialColors",true);
        grassSolid.setColor("Diffuse", greenColor);
        grassSolid.setColor("Specular", ColorRGBA.White.mult(0.1f));
        grassSolid.setColor("Ambient", greenColor);
        grassSolid.setFloat("Shininess", 64f);  // [0,128]

        concrete = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        concrete.setTexture("DiffuseMap", concreteTexture);
        //concrete.setTexture("NormalMap", concreteTexture);
        concrete.setBoolean("UseMaterialColors", true);
        concrete.setColor("Diffuse", ColorRGBA.Gray);
        concrete.setColor("Specular", ColorRGBA.Gray);
        concrete.setColor("Ambient", ColorRGBA.Gray);
        concrete.setFloat("Shininess", 64f);  // [0,128]
        
        grassTerrain = new Material(assetManager, 
            "Common/MatDefs/Terrain/TerrainLighting.j3md");
        grassTerrain.setFloat("Shininess", 0.5f);
        grassTerrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"));
        //grassTerrain.setTexture("Tex1", grassTexture);
        //grassTerrain.setFloat("Tex1Scale", 4f);
        grassTerrain.setTexture("DiffuseMap", grassTexture);
        grassTerrain.setFloat("DiffuseMap_0_scale", 256f);
        grassTerrain.setTexture("DiffuseMap_1", dirtTexture);
        grassTerrain.setFloat("DiffuseMap_1_scale", 16f);
        //grassTerrain.getAdditionalRenderState().setWireframe(true);
        
        //grassTerrain.setBoolean("useTriPlanarMapping", true);
        //grassTerrain.setFloat("DiffuseMap_0_scale", 1f / (float) (512f / 1024f));
        
        /*grassTerrain.setTexture("DiffuseMap", grassTexture);
        grassTerrain.setTexture("NormalMap", grassNormalTexture);
        grassTerrain.setBoolean("UseMaterialColors",true);
        grassTerrain.setColor("Diffuse", greenColor);
        grassTerrain.setColor("Specular", ColorRGBA.White.mult(0.1f));
        grassTerrain.setColor("Ambient", ColorRGBA.White);
        grassTerrain.setFloat("Shininess", 64f);  // [0,128]*/
        
        silhouette = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        silhouette.setTexture("DiffuseMap", silhouetteTexture);
        silhouette.setTexture("AlphaMap", silhouetteTexture);
        silhouette.setBoolean("UseMaterialColors", true);
        silhouette.setColor("Diffuse", ColorRGBA.Gray);
        silhouette.setColor("Specular", ColorRGBA.Gray);
        silhouette.setColor("Ambient", ColorRGBA.Gray);
        silhouette.setFloat("Shininess", 64f);  // [0,128]
        silhouette.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        tree = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        tree.setTexture("DiffuseMap", treeTexture);
        tree.setTexture("AlphaMap", treeAlphaTexture);
        tree.setBoolean("UseMaterialColors", true);
        tree.setColor("Diffuse", ColorRGBA.White);
        tree.setColor("Specular", ColorRGBA.White);
        tree.setColor("Ambient", ColorRGBA.White);
        tree.setFloat("Shininess", 64f);  // [0,128]
        tree.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
    }
    
    public void enableTextures(boolean val){
        if (val) {
            grass.setTexture("DiffuseMap", grassTexture);
            grass.setTexture("NormalMap", grassNormalTexture);
            grassB.setTexture("DiffuseMap", grassTexture);
            grassB.setTexture("NormalMap", grassNormalTexture);
            grassB.setColor("Ambient", ColorRGBA.White);
            grass.setColor("Ambient", ColorRGBA.White);
        } else {
            grass.setTexture("DiffuseMap", null);
            grass.setTexture("NormalMap", null);
            grassB.setTexture("DiffuseMap", null);
            grassB.setTexture("NormalMap", null);
            grass.setColor("Ambient", greenColor);
            grassB.setColor("Ambient", greenColor.mult(0.5f));
        }
    }
}
