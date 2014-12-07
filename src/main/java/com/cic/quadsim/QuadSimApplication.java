package com.cic.quadsim;

import com.cic.quadsim.input.GamePad;
import com.cic.quadsim.input.JinputDriverExtractor;
import com.cic.quadsim.nanoquad.NanoQuad;
import com.cic.quadsim.scene.Gates;
import com.cic.quadsim.scene.Silhouette;
import com.cic.quadsim.scene.Tree;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class QuadSimApplication extends SimpleApplication implements PhysicsCollisionListener {

    private static final Logger log = Logger.getLogger(QuadSimApplication.class.getName());

    public static void main(String[] args) {
        JinputDriverExtractor.extract();

        QuadSimApplication app = new QuadSimApplication();

        //app.setShowSettings(false);
        AppSettings mySettings = new AppSettings(true);
        mySettings.setVSync(true);
        mySettings.setSamples(2);
        mySettings.setResolution(1024, 768);
        mySettings.setTitle("Nano Quad Simulator");
        mySettings.setSettingsDialogImage("Textures/splash.png");

        app.setSettings(mySettings);

        app.start();
    }

    private GamePad gamePad = null;

    private NanoQuad aircraft;
    //private Node hudNode;

    private MaterialLibrary materials;

    private BulletAppState bulletAppState;

    private Node groundNode;
    
    private CameraNode camNode1;
    private CameraNode camNode2;
    private Node camNode2Node;
    private ChaseCamera chaseCam;
    private final float cameraFieldOfViewDefault = 45;
    private final float cameraFieldOfViewMin = 2;
    private final float cameraFieldOfViewMax = 90;
    private float cameraFieldOfView = 45;
    private float cameraAspectRatio = 1;
    private int cameraMode = 0;

    private Gates gates;
    private String nextGate = null;
    private ArrayList<String> gateSequence;
    private long lapStart = -1;

    private int treeCount = 0;

    private NotificationsPanel notifications;

    private boolean showBatteryStats = false;
    private Picture batteryIcon;

    public QuadSimApplication() {
        super();

        GamePad[] controllers = GamePad.get();
        if (controllers.length == 0) {
            //log.warning("Found no gamepad controllers.");
        } else {
            gamePad = controllers[0];
        }
    }

    @Override
    public void simpleInitApp() {
        /* Setup camera */
        flyCam.setEnabled(false);
        cameraAspectRatio = (cam.getFrustumRight() - cam.getFrustumLeft()) / (cam.getFrustumTop() - cam.getFrustumBottom());

        setDisplayStatView(false);

        notifications = new NotificationsPanel(assetManager);
        notifications.getNode().setLocalTranslation(0, cam.getHeight(), 0);
        guiNode.attachChild(notifications.getNode());

        /* Bind space to reset simulation */
        inputManager.addMapping("ResetPosition", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "ResetPosition");

        //inputManager.addMapping("ToggleTextures", new KeyTrigger(KeyInput.KEY_T));
        //inputManager.addListener(actionListener, "ToggleTextures");
        /* Add sky */
        rootNode.attachChild(SkyFactory.createSky(assetManager,
                "Textures/Sky/Bright/FullskiesBlueClear03.dds", false));

        materials = new MaterialLibrary(assetManager);

        /* Prepare physics */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // fall from 1 m = 0.45 sec
        // fall from 10 m = 1.4 sec
        // fall from 100m = 4.5 sec
        // To fake 10 units as 1 meter, we scale time by 1.4/0.45 = 3.11
        bulletAppState.setSpeed(3.12f);

        bulletAppState.getPhysicsSpace().addCollisionListener(this);

        /* Add objects */
        aircraft = new NanoQuad("aircraft",
                materials.orangePlastic, materials.whitePlastic,
                materials.whitePlastic, materials.redPlastic,
                materials.blueLED, materials.redLED);

        bulletAppState.getPhysicsSpace().add(aircraft.getRBControl());

        rootNode.attachChild(aircraft);
        rootNode.attachChild(makeGround());
        //rootNode.attachChild(makeHud());
        rootNode.attachChild(makeObstacles());
        
        Node trees = Tree.makeTrees(groundNode, materials);
        rootNode.attachChild(trees);
        for (Spatial tree : trees.getChildren()) {
            if (tree instanceof Tree) {
                bulletAppState.getPhysicsSpace().add(((Tree)tree).getRBControl());
            }
        }

        gates = new Gates(materials);
        rootNode.attachChild(gates);

        gateSequence = new ArrayList<String>();
        gates.addGate("1", 50, 0, -50, 10, 20, 0);
        gates.addGate("2", 1550 - 1280, 0, 1030 - 1280, 10, 5, -(float) Math.PI / 2); // 155 103
        gates.addGate("3", 1400 - 1280, 10, 1720 - 1280, 10, 20, -(float) Math.PI);   // 140 172
        gates.addGate("4", 1320 - 1280, 15, 1510 - 1280, 3, 3, 0);   // 140 172
        gateSequence.add("1");
        gateSequence.add("2");
        gateSequence.add("3");
        gateSequence.add("4");
        
        placeStick(0, -20, 10);
        //placeStick(-10, -50, 10);
        //placeStick(-10, -150, 10);
        
        placeStick(50 - 5, -50, 22);
        placeStick(50 + 5, -50, 22);
        
        placeStick(1550 - 1280, 1030 - 1280 - 5, 10);
        placeStick(1550 - 1280, 1030 - 1280 + 5, 10);
        
        placeStick(1400 - 1280 - 5, 1720 - 1280, 25);
        placeStick(1400 - 1280 + 5, 1720 - 1280, 25);
        
        placeStick(1320 - 1280 - 1.5f, 1510 - 1280, 13);
        placeStick(1320 - 1280 + 1.5f, 1510 - 1280, 13);

        /* Add lights */
        DirectionalLight sunLight = new DirectionalLight();
        sunLight.setDirection(new Vector3f(0, -2, 2).normalizeLocal());
        sunLight.setColor(new ColorRGBA(1f, 1f, 0.9f, 1f));

        DirectionalLight groundReflectedLight = new DirectionalLight();
        groundReflectedLight.setDirection(new Vector3f(0, 1, 0).normalizeLocal());
        groundReflectedLight.setColor(new ColorRGBA(0.8f, 1f, 0.6f, 1f).mult(0.1f));

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(new ColorRGBA(0.8f, 0.9f, 1f, 1f).mult(0.7f));

        rootNode.addLight(sunLight);
        rootNode.addLight(ambientLight);
        rootNode.addLight(groundReflectedLight);

        /* Drop shadows */
        final int SHADOWMAP_SIZE = 1024;
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(sunLight);
        viewPort.addProcessor(dlsr);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
        dlsf.setLight(sunLight);
        dlsf.setEnabled(true);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);

        viewPort.setBackgroundColor(new ColorRGBA(0.3f, 0.5f, 0.8f, 1.0f));

        /* Again, setup camera views */
        camNode1 = new CameraNode("Camera Node 1", cam);
        camNode1.setControlDir(ControlDirection.SpatialToCamera);
        aircraft.attachChild(camNode1);
        camNode1.setLocalTranslation(new Vector3f(0, 0.05f, -0.08f));
        camNode1.lookAt(aircraft.getLocalTranslation().add(new Vector3f(0, -0.3f, -2f)), Vector3f.UNIT_Y);
        camNode1.setEnabled(false);

        camNode2 = new CameraNode("Camera Node 2", cam);
        camNode2.setControlDir(ControlDirection.SpatialToCamera);
        camNode2Node = new Node("Camera node 2 center");
        camNode2Node.attachChild(camNode2);
        aircraft.attachChild(camNode2Node);
        camNode2.setLocalTranslation(new Vector3f(0, 0.5f, 2f));
        camNode2.lookAt(aircraft.getLocalTranslation().add(new Vector3f(0, -0.3f, -2f)), Vector3f.UNIT_Y);
        camNode2.setEnabled(false);

        chaseCam = new ChaseCamera(cam, aircraft, inputManager);
        chaseCam.setSmoothMotion(true);
        chaseCam.setEnabled(false);
        chaseCam.setDragToRotate(false);

        if (gamePad == null) {
            notifications.put("gamepad-missing", "Game pad not found!");
        }

        /*
         throttleMsg = notifications.registerMessage();
         throttleMsg2 = notifications.registerMessage();
         batteryMsg = notifications.registerMessage();
         batteryMsg2 = notifications.registerMessage();
         batteryWarningMsg = notifications.registerMessage();
         altMsg = notifications.registerMessage();
         */
        makeHud();
        
        resetPosition(3);
    }

    private void makeHud() {
        batteryIcon = new Picture("HUD Battery Icon");
        batteryIcon.setImage(assetManager, "Textures/battery-warning.png", true);
        
        batteryIcon.setWidth(6 * 7);
        batteryIcon.setHeight(10 * 7);
        batteryIcon.setPosition(settings.getWidth() - (6 * 7) - 10, 10);
        guiNode.attachChild(batteryIcon);    
    }

    private Node makeGround() {
        groundNode = new Node("Floor");

        AbstractHeightMap heightmap = null;
        try {
            //heightmap = new HillHeightMap(129, 1000, 50, 100, (byte) 3);

            Texture heightMapImage = assetManager.loadTexture(
                    //"Textures/Terrain/splat/fortress512.png");
                    "Textures/Terrain/Trzaska/trzaska-heightmap256.png");
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
            heightmap.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TerrainQuad terrain = new TerrainQuad("terrain", 65, 257, heightmap.getHeightMap());
        terrain.setMaterial(materials.grassTerrain);
        terrain.setLocalScale(10f, .3f, 10f);
        terrain.setLocalTranslation(0, -18.5f, 0);
        ArrayList<Camera> cameras = new ArrayList<Camera>();
        cameras.add(getCamera());
        TerrainLodControl control = new TerrainLodControl(terrain, cameras);
        terrain.addControl(control);
        //control.setLodCalculator(new NonlinearDistanceLodCalculator(65, 0.7f));

        terrain.setShadowMode(RenderQueue.ShadowMode.Receive);

        //return terrain;
        groundNode.attachChild(terrain);

        RigidBodyControl groundRBControl = new RigidBodyControl(0.0f);
        groundNode.addControl(groundRBControl);
        //groundRBControl.setCollisionShape(new BoxCollisionShape(new Vector3f(900.0f, 1.0f, 900.0f)));
        groundRBControl.setFriction(1.9f);
        bulletAppState.getPhysicsSpace().add(groundRBControl);

        return groundNode;
    }

    private void placeStick(float x, float z, float height){
        Mesh box = new Box(0.1f, height / 2, 0.1f);
        Geometry geom = new Geometry("stick", box);
        geom.setMaterial(materials.whitePlastic);
        geom.move(0, height / 2, 0);
        Node node = new Node("Stick");
        node.attachChild(geom);
        node.setShadowMode(RenderQueue.ShadowMode.Cast);
        
        float y = 0;
        Ray ray = new Ray(new Vector3f(x, 1000, z), new Vector3f(0, -1, 0));
        CollisionResults results = new CollisionResults();
        groundNode.collideWith(ray, results);
        
        if (results.size() > 0) {
            float distance = results.getClosestCollision().getDistance();
            y = 1000 - distance;
        }
        
        node.setLocalTranslation(x, y, z);
        rootNode.attachChild(node);
        
        RigidBodyControl landingRBControl = new RigidBodyControl(0.0f);
        node.addControl(landingRBControl);
        //landingRBControl.setCollisionShape(new BoxCollisionShape(new Vector3f(2f, 0.25f, 2f)));
        bulletAppState.getPhysicsSpace().add(landingRBControl);
    }
    
    private Node makeObstacles() {
        Mesh box = new Box(2f, 0.25f, 2f);
        Geometry boxGeom = new Geometry("Landing pad", box);
        boxGeom.setMaterial(materials.concrete);
        boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
        boxGeom.setLocalTranslation(0, 0f, 0);
        Node landingNode = new Node("Landing");
        landingNode.attachChild(boxGeom);

        /*box = new Box(0.5f, 5f, 0.5f);
        boxGeom = new Geometry("1m-box", box);
        boxGeom.setMaterial(materials.concrete);
        boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
        boxGeom.setLocalTranslation(0, 5f-0.8f, -10);
        landingNode.attachChild(boxGeom);*/

        /*box = new Box(0.5f, 5f, 0.5f);
         boxGeom = new Geometry("1m-box-2", box);
         boxGeom.setMaterial(materials.concrete);
         boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
         boxGeom.setLocalTranslation(5, 0.5f, -9);
         boxGeom.rotate(0,0, (float)Math.PI/2);
         landingNode.attachChild(boxGeom);

         box = new Box(1f, 10f, 1f);
         boxGeom = new Geometry("2m-box", box);
         boxGeom.setMaterial(materials.concrete);
         boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
         boxGeom.setLocalTranslation(10, 10, -10);
         //boxGeom.setQueueBucket(Bucket.Transparent);
         landingNode.attachChild(boxGeom);*/
        landingNode.attachChild(new Silhouette("man", materials.silhouette, -10, -1f, -10, 0));
        landingNode.attachChild(new Silhouette("man2", materials.silhouette, 10, -1f, -20, 0.1f));
        landingNode.attachChild(new Silhouette("man3", materials.silhouette, 15, -4.5f, -100, 0.3f));

        /*box = new Box(2f, 25f, 2f);
        boxGeom = new Geometry("5m-box", box);
        boxGeom.setMaterial(materials.concrete);
        boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
        boxGeom.setLocalTranslation(0, 25f-3f, -100);
        landingNode.attachChild(boxGeom);*/

        /*
         box = new Box(2f, 50f, 2f);
         boxGeom = new Geometry("10m-box", box);
         boxGeom.setMaterial(materials.concrete);
         boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
         boxGeom.setLocalTranslation(10, 2f, -50);
         boxGeom.rotate((float)Math.PI/2, 0, 0);
         landingNode.attachChild(boxGeom);
        
         box = new Box(5f, 500f, 2f);
         boxGeom = new Geometry("100m-box", box);
         boxGeom.setMaterial(materials.redPlastic);
         boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
         boxGeom.setLocalTranslation(20, 2f, -500);
         boxGeom.rotate((float)Math.PI/2, 0, 0);
         landingNode.attachChild(boxGeom);
        
         box = new Box(5f, 500f, 2f);
         boxGeom = new Geometry("100m-box2", box);
         boxGeom.setMaterial(materials.redPlastic);
         boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
         boxGeom.setLocalTranslation(40, 2f, -1500);
         boxGeom.rotate((float)Math.PI/2, 0, 0);
         landingNode.attachChild(boxGeom);

         box = new Box(5f, 500f, 2f);
         boxGeom = new Geometry("100m-box3", box);
         boxGeom.setMaterial(materials.redPlastic);
         boxGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
         boxGeom.setLocalTranslation(20, 2f, -2500);
         boxGeom.rotate((float)Math.PI/2, 0, 0);
         landingNode.attachChild(boxGeom);
         */
        /*float sideLength = 50f * 129f;
         box = new Quad(sideLength, sideLength);
         boxGeom = new Geometry("bottomquad", box);
         boxGeom.setMaterial(materials.whitePlastic);
         boxGeom.setLocalTranslation(-sideLength/2, -2f, sideLength/2);
         boxGeom.rotate(-(float)Math.PI/2, 0, 0);
         landingNode.attachChild(boxGeom);*/

        /*box = new Quad(0.32f, 0.32f);
         boxGeom = new Geometry("32mm square", box);
         boxGeom.setMaterial(materials.orangePlastic);
         boxGeom.rotate(-(float)Math.PI/2, 0, 0);
         boxGeom.setLocalTranslation(0, 0.4f, -0.75f);
         landingNode.attachChild(boxGeom);

         box = new Quad(0.32f, 0.32f);
         boxGeom = new Geometry("32mm square2", box);
         boxGeom.setMaterial(materials.whitePlastic);
         boxGeom.rotate(-(float)Math.PI/2, 0, 0);
         boxGeom.setLocalTranslation(0.32f, 0.4f, -0.75f);
         landingNode.attachChild(boxGeom);*/
        landingNode.setLocalTranslation(0, 1.2f, 0);

        RigidBodyControl landingRBControl = new RigidBodyControl(0.0f);
        landingNode.addControl(landingRBControl);
        //landingRBControl.setCollisionShape(new BoxCollisionShape(new Vector3f(2f, 0.25f, 2f)));
        bulletAppState.getPhysicsSpace().add(landingRBControl);

        return landingNode;
    }

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("ResetPosition") && !keyPressed) {
                resetPosition(10);
            }
            /*if (name.equals("ToggleTextures") && !keyPressed) {
             useTextures = !useTextures;
             materials.enableTextures(useTextures);
             }*/
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        if (gamePad != null) {
            gamePad.poll();

            if (gamePad.isPressedStart()) {
                resetPosition(3);
                return;
            }
            
            if (gamePad.isPressedA()) {
                aircraft.getBattery().reset();
                return;
            }

            float throttle = -gamePad.getLeftY();
            float alieron = -gamePad.getRightX();
            float elevator = gamePad.getRightY();
            float rudder = -gamePad.getLeftX();

            notifications.put("input-throttle", showBatteryStats ? "Input Throttle: " + throttle : null);

            aircraft.applyControls(tpf, throttle, rudder, elevator, alieron);

            notifications.put("applied-throttle", showBatteryStats ? "Applied Throttle: " + aircraft.getThrottleValue() : null);

        } else {
            aircraft.applyControls(tpf, 0, 0, 0, 0);
            aircraft.getBattery().getPower(tpf, 0);
        }

        notifications.put("bat-cap", showBatteryStats ? "Capacity: " + aircraft.getBattery().getEffectiveCapacity() : null);
        notifications.put("bat-load", showBatteryStats ? "Load: " + aircraft.getBattery().getLoad() : null);

        notifications.put("bat-warn", aircraft.getBattery().hasWarning() ? "BATTERY WARNING!" : null);
        batteryIcon.setCullHint(aircraft.getBattery().hasWarning() ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        //notifications.put("altitude", "Altitude: " + aircraft.getWorldTranslation().y/10);
        //notifications.put("velocity", "Velocity: " + aircraft.getRBControl().getLinearVelocity().length());

        String gate = gates.test(aircraft.getRBControl(), tpf);
        if (gate != null && gate.equals(nextGate)) {
            int index = gateSequence.indexOf(gate);
            if (index == 0) {
                long newLapStart = System.currentTimeMillis();
                if (lapStart > 0) {
                    notifications.put("lap" + newLapStart, "Lap time: " + ((newLapStart - lapStart) / 1000) + " seconds");
                }
                lapStart = newLapStart;
            }
            int nextIndex = index + 1;
            if (nextIndex >= gateSequence.size()) {
                nextIndex = 0;
            }
            nextGate = gateSequence.get(nextIndex);
            //System.out.println("next: "+nextGate);
            notifications.put("gate", "Passed gate " + gate);
            gates.setActive(nextGate);
            //lastGate = gate;
        }

        updateCamera(tpf);
        //updateHud(tpf, tpf);
        notifications.update();
    }

    private void updateHud(float pitch, float roll) {
        //hudNode.getChild(0).setLocalRotation(new Quaternion(new float[]{0, 0, roll}));
        //hudNode.getChild(1).setLocalRotation(new Quaternion(new float[]{0, 0, pitch}));

        //hudNode.getChild(2).getLocalTranslation();
        //hudNode.getChild(3).setLocalTranslation(hudNode.getChild(2).getLocalTranslation().add(localUpVector.mult(0.25f)));
    }

    private void updateCamera(float tpf) {
        Vector3f location = aircraft.getWorldTranslation();

        float zoomChange = gamePad != null ? gamePad.getLeftZ() : 0;
        boolean changeMode = false;

        if (gamePad != null) {
            if (gamePad.isPressedL()) {
                cameraMode = (cameraMode + 1) % 4;
                changeMode = true;
            }

            if (gamePad.isPressedR()) {
                cameraFieldOfView = cameraFieldOfViewDefault;
                camNode2Node.setLocalRotation(new Quaternion(0.0f, 0f, 0f, 1.0f));
                updateCameraFov();
            }

            if (gamePad.isPressedB()) {
                showBatteryStats = !showBatteryStats;
            }
        }

        if (changeMode) {
            switch (cameraMode) {
                case 0:
                    camNode1.setEnabled(false);
                    chaseCam.setEnabled(false);
                    break;
                case 1: {
                    camNode1.setEnabled(true);
                    chaseCam.setEnabled(false);
                }
                break;
                case 2: {
                    camNode2.setEnabled(true);
                    camNode1.setEnabled(false);
                }
                break;
                case 3: {
                    camNode2.setEnabled(false);
                    chaseCam.setEnabled(true);
                    //chaseCam.setDragToRotate(false);
                }
                break;
            }
        }

        switch (cameraMode) {
            case 0:
                cam.setLocation(new Vector3f(0f, 17f, 20f));
                cam.lookAt(location, Vector3f.UNIT_Y);
                break;
            case 2: {
                camNode2Node.rotate(gamePad.getPovY() * tpf, gamePad.getPovX() * tpf, 0);
            }
            break;
            case 3:
                if (gamePad != null) {
                    if (gamePad.getPovX() == 0) {
                        //chaseCam.setLookAtOffset(new Vector3f(0,0,0));
                    }
                    if (gamePad.getPovX() > 0) {
                        chaseCam.onAnalog(ChaseCamera.ChaseCamMoveRight, 0.1f, tpf);
                    }
                    if (gamePad.getPovX() < 0) {
                        chaseCam.onAnalog(ChaseCamera.ChaseCamMoveLeft, 0.1f, tpf);
                    }
                }
                break;
        }

        if (Math.abs(zoomChange) > 0.01) {

            cameraFieldOfView += zoomChange * tpf * 90 * cameraFieldOfView / 45;

            if (cameraFieldOfView > cameraFieldOfViewMax) {
                cameraFieldOfView = cameraFieldOfViewMax;
            }
            if (cameraFieldOfView < cameraFieldOfViewMin) {
                cameraFieldOfView = cameraFieldOfViewMin;
            }

            updateCameraFov();
        }
    }

    private void updateCameraFov() {
        //cam.setFrustumPerspective(cameraFieldOfView, cameraAspectRatio, cam.getFrustumNear(), cam.getFrustumFar());
        cam.setFrustumPerspective(cameraFieldOfView, cameraAspectRatio, 0.1f, 10000);
        cam.update();
    }

    private void resetPosition(float height) {
        RigidBodyControl aircraftRBControl = aircraft.getRBControl();
        aircraftRBControl.setPhysicsLocation(new Vector3f(0, height, 0));
        aircraftRBControl.setPhysicsRotation(Quaternion.ZERO);
        aircraftRBControl.setAngularVelocity(Vector3f.ZERO);
        aircraftRBControl.setLinearVelocity(Vector3f.ZERO);
        cameraFieldOfView = cameraFieldOfViewDefault;
        updateCameraFov();
        //releaseMillis = System.currentTimeMillis();
        aircraft.getBattery().reset();
        notifications.clear();
        //lastGate = null;
        nextGate = gateSequence.get(0);
        gates.setActive(nextGate);
        lapStart = -1;
    }

    @Override
    public void collision(PhysicsCollisionEvent pce) {
        /*
         if (releaseMillis > 0) {
         System.out.println(pce);
         System.out.println(System.currentTimeMillis() - releaseMillis);
         releaseMillis = -1;
         }
         */
    }
}
