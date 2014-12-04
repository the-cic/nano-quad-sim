package com.cic.quadsim.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

/**
 * Simplified wrap of jinput specifically for game pads.
 *
 * @author cic
 */
public class GamePad {

    private static final Logger log = Logger.getLogger(GamePad.class.getName());

    class Channel {
        private long nanos = 0;
        private float value = 0;
        private boolean analog = true;
        private boolean raised = false;
        private boolean lowered = false;

        public Channel(Component component){
            analog = component.isAnalog();
        }

        public void setValue(float value, long nanos){
            // somehow the nanos are wrong
            //if (nanos > this.nanos) {
                if (value > this.value) {
                    raised = true;
                }
                if (value < this.value) {
                    lowered = true;
                }
                this.value = value;
                this.nanos = nanos;
            //}
        }

        public boolean isRaised(){
            if (raised) {
                raised = false;
                return true;
            }
            return false;
        }

        public boolean isLowered(){
            if (lowered) {
                lowered = false;
                return true;
            }
            return false;
        }

        public float getValue() {
            return this.value;
        }

        public String toString(){
            return value + " at " + nanos;
        }
    }

    private static GamePad[] gamePads = null;

    private Controller joystickController = null;
    private HashMap<Component.Identifier, Channel> channels = null;

    /**
     * Called from get(). No reason to call it unless you are refreshing inputs.
     */
    public static void collect(){
        JinputDriverExtractor.extract();

        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        gamePads = new GamePad[]{};

        if (controllers.length == 0) {
            log.warning("No controllers found, check library.");
            return;
        }

        ArrayList<GamePad> list = new ArrayList<GamePad>();
        for (Controller controller : controllers) {
            if (controller.getType() == Controller.Type.GAMEPAD) {
                list.add(new GamePad(controller));
                log.info("Found game pad: "+controller.getName());
            }
        }

        if (list.isEmpty()) {
            log.warning("No game pads found.");
        }

        gamePads = list.toArray(gamePads);
    }

    /**
     * A call is made to JinputDriverExtractor.extract(), to extract native drivers needed by jinput.
     *
     * However, I would recommend calling JinputDriverExtractor.extract() sooner than the first GamePad.get(),
     * for more controlled flow and logging in case of errors.
     *
     * @return all the available game pads.
     */
    public static GamePad[] get(){
        if (gamePads == null) {
            collect();
        }
        return gamePads;
    }

    /**
     * Constructor.
     * Use get() to get all the available game pads.
     * Use this to manually create one if you really need to.
     *
     * @param controller - wrapped jinput controller
     */
    public GamePad(Controller controller){
        this.joystickController = controller;

        channels = new HashMap<Component.Identifier, Channel>();
        for (Component component : joystickController.getComponents()) {
            Channel channel = new Channel(component);
            channels.put(component.getIdentifier(), channel);
        }
    }

    /**
     * Try not to use this
     */
    public HashMap<Component.Identifier, Channel> getAllChannels(){
        return channels;
    }

    /**
    * Poll the controller and refresh the channels.
    */
    public void poll(){
        if (joystickController.poll()) {
            EventQueue event_queue = joystickController.getEventQueue();
            Event event = new Event();
            while (event_queue.getNextEvent(event)) {
                Channel channel = channels.get(event.getComponent().getIdentifier());
                if (channel != null) {
                    channel.setValue(event.getValue(), event.getNanos());
                }
            }
        }
    }

    public float getChannelValue(Component.Identifier key){
        Channel channel = channels.get(key);
        if (channel != null) {
            return channel.getValue();
        }
        return 0;
    }

    /**
     * Did channel's value go up?
     * @param key
     * @return
     */
    public boolean isChannelRaised(Component.Identifier key){
        Channel channel = channels.get(key);
        if (channel != null) {
            return channel.isRaised();
        }
        return false;
    }

    /**
     * Did channel's value go down?
     * @param key
     * @return
     */
    public boolean isChannelLowered(Component.Identifier key){
        Channel channel = channels.get(key);
        if (channel != null) {
            return channel.isLowered();
        }
        return false;
    }

    /**
     * Is channel's value up?
     * @param key
     * @return
     */
    public boolean isChannelUp(Component.Identifier key){
        Channel channel = channels.get(key);
        if (channel != null) {
            return channel.getValue() > 0.5;
        }
        return false;
    }

    /* Main analog inputs */

    public float getLeftX(){
        return getChannelValue(net.java.games.input.Component.Identifier.Axis.X);
    }

    public float getLeftY(){
        return getChannelValue(net.java.games.input.Component.Identifier.Axis.Y);
    }

    public float getLeftZ(){
        return getChannelValue(net.java.games.input.Component.Identifier.Axis.Z);
    }

    public float getRightX(){
        return getChannelValue(net.java.games.input.Component.Identifier.Axis.RX);
    }

    public float getRightY(){
        return getChannelValue(net.java.games.input.Component.Identifier.Axis.RY);
    }

    public float getRightZ(){
        return getChannelValue(net.java.games.input.Component.Identifier.Axis.RZ);
    }

    /* POV hat */

    public float getPovX(){
        float pow = getChannelValue(net.java.games.input.Component.Identifier.Axis.POV);
        if (pow == Component.POV.LEFT){
            return -1;
        }
        if (pow == Component.POV.UP_LEFT || pow == Component.POV.DOWN_LEFT){
            return -0.70710678118f;
        }
        if (pow == Component.POV.RIGHT){
            return 1;
        }
        if (pow == Component.POV.UP_RIGHT || pow == Component.POV.DOWN_RIGHT){
            return 0.70710678118f;
        }
        return 0;
    }

    public float getPovY(){
        float pow = getChannelValue(net.java.games.input.Component.Identifier.Axis.POV);
        if (pow == Component.POV.UP){
            return -1;
        }
        if (pow == Component.POV.UP_LEFT || pow == Component.POV.UP_RIGHT){
            return -0.70710678118f;
        }
        if (pow == Component.POV.DOWN){
            return 1;
        }
        if (pow == Component.POV.DOWN_LEFT || pow == Component.POV.DOWN_RIGHT){
            return 0.70710678118f;
        }
        return 0;
    }

    /* General buttons */

    public boolean isButtonPressed(Component.Identifier identifier){
        return isChannelRaised(identifier);
    }

    public boolean isButtonReleased(Component.Identifier identifier){
        return isChannelLowered(identifier);
    }

    public boolean isButtonHeld(Component.Identifier identifier){
        return isChannelUp(identifier);
    }

    /* L and R buttons */

    public boolean isPressedL(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._4);
    }

    public boolean isReleasedL(){
        return isButtonReleased(net.java.games.input.Component.Identifier.Button._4);
    }

    public boolean isHeldL(){
        return isButtonHeld(net.java.games.input.Component.Identifier.Button._4);
    }

    public boolean isPressedR(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._5);
    }

    public boolean isReleasedR(){
        return isButtonReleased(net.java.games.input.Component.Identifier.Button._5);
    }

    public boolean isHeldR(){
        return isButtonHeld(net.java.games.input.Component.Identifier.Button._5);
    }

    /* Action buttons */

    public boolean isPressedA(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._0);
    }

    public boolean isPressedB(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._1);
    }

    public boolean isPressedX(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._2);
    }

    public boolean isPressedY(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._3);
    }

    public boolean isReleasedA(){
        return isButtonReleased(net.java.games.input.Component.Identifier.Button._0);
    }

    public boolean isReleasedB(){
        return isButtonReleased(net.java.games.input.Component.Identifier.Button._1);
    }

    public boolean isReleasedX(){
        return isButtonReleased(net.java.games.input.Component.Identifier.Button._2);
    }

    public boolean isReleasedY(){
        return isButtonReleased(net.java.games.input.Component.Identifier.Button._3);
    }

    public boolean isHeldA(){
        return isButtonHeld(net.java.games.input.Component.Identifier.Button._0);
    }

    public boolean isHeldB(){
        return isButtonHeld(net.java.games.input.Component.Identifier.Button._1);
    }

    public boolean isHeldX(){
        return isButtonHeld(net.java.games.input.Component.Identifier.Button._2);
    }

    public boolean isHeldY(){
        return isButtonHeld(net.java.games.input.Component.Identifier.Button._3);
    }

    /* Navigation buttons */

    public boolean isPressedBack(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._6);
    }

    public boolean isPressedStart(){
        return isButtonPressed(net.java.games.input.Component.Identifier.Button._7);
    }
}
