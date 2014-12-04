
package com.cic.quadsim;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;

/**
 *
 * @author cic
 */
public class NotificationsPanel extends Node {
    private int messageCount = 0;
    private ArrayList<String> messages = new ArrayList<String>();
    
    private Geometry darken;
    private BitmapText text;
    
    public NotificationsPanel(AssetManager assetManager) {
        super("NotificationsPanel");
        
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setText("Hello World\nHohoho");
        //helloText.setLocalTranslation(300, helloText.getLineHeight()*2, 0);
        this.attachChild(text);        
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0,0,0,0.5f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        darken = new Geometry("StatsDarken", new Quad(1, 1));
        darken.setMaterial(mat);
        darken.setLocalTranslation(0, 0, -1);
        darken.rotate(0, 0, -(float)Math.PI/2);
        //darken.setCullHint(showFps && darkenBehind ? CullHint.Never : CullHint.Always);
        this.attachChild(darken);
    }
    
    public int registerMessage(){
        messages.add(null);
        return messageCount++;
    }
    
    public void setMessage(int id, String s){
        if (id >= messageCount) {
            return;
        }
        messages.set(id, s);
    }
    
    public void update(){
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String s : messages) {
            if (s != null && !s.isEmpty()) {
                sb.append(s).append("\n");
                count++;
            }
        }
        text.setText(sb.toString());
        darken.setLocalScale(count * text.getLineHeight(), text.getLineWidth(), 1);
    }
}