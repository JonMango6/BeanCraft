package jme3test.helloworld;

import com.jme3.app.SimpleApplication;
import java.util.*;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import static com.jme3.bullet.PhysicsSpace.getPhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import java.util.ArrayList;

/**
 * B E A N C R A F T
 * This game is very similar to minecraft but more bean themed.
 * @author jonma
 */
public class BeancraftMain extends SimpleApplication implements ActionListener{

  private Spatial sceneModel; //sceneModel is the spatial that makes up the sky.
  private Geometry cube; //Geometry used for every block in the game.
  private Picture compass;
  private Node compassNode;
  private BulletAppState bulletAppState;
  private RigidBodyControl landscape; // Both RigidBodyControl variables control the collision of the landscape and blocks.
  private RigidBodyControl blockscape;
  private RigidBodyControl blank;
  private CharacterControl player;
  private Vector3f walkDirection = new Vector3f(); // walkDirection is used in our update method for walking.
  private Vector3f lookDirection = new Vector3f();
  private ArrayList<Spatial> blockArr = new ArrayList<Spatial>(); // this arrayList stores every block created.
  private int blockNum; // Used for switching between blocks.
  private String blockInHand = ("Textures/grass6.png");
  private int scrollNum; // Used to see how many times the block has been changed.
  private boolean left = false, right = false, up = false, down = false, place = false; //Boolean variables for key bindings.
  private Vector3f camDir = new Vector3f(); //Temporary vectors used on each frame.
  private Vector3f camLeft = new Vector3f(); //They here to avoid instanciating new vectors on each frame
  private Vector2f ex = new Vector2f(2,0);
  private AudioNode audio_placeGrass;
  private AudioNode audio_nature;
  private CameraNode camera = new CameraNode();

  public static void main(String[] args) { // Main method that runs the code and starts the application.
    BeancraftMain app = new BeancraftMain();
    app.start();
  }

  public void simpleInitApp() { // This method is where most pieces of code are declared. This is the method that is run in the main method.
   
    /** Set up Physics */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    //bulletAppState.setDebugEnabled(true); (for debugging)

    viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f)); //Sky color
    flyCam.setMoveSpeed(100);
    setUpKeys();  //Key Bindings
    setUpLight(); //Sets up light
    CrossHairs();  //Creates a crosshair for the user
    makeScene(25,25); // Creates a 25 x 3 x 25 area of blocks
    Music();
    //HUD();


    // loads the scene from the zip file and adjust its size.
    assetManager.registerLocator("town.zip", ZipLocator.class);
    sceneModel = assetManager.loadModel("Scenes/newScene.j3o");
    sceneModel.setLocalScale(1f);
    

    // sets up collision detection for the scene by creating a
    // compound collision shape and a static RigidBodyControl with mass zero.
    CollisionShape sceneShape =
            CollisionShapeFactory.createMeshShape(sceneModel);
    landscape = new RigidBodyControl(sceneShape, 0);
    sceneModel.addControl(landscape);

    // Sets up collision detection for the player by creating
    // a capsule collision shape and a CharacterControl.
    // The player is also put in their starting position.
    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.5f, 2f, 1);
    player = new CharacterControl(capsuleShape, 0.05f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);

    player.setGravity(new Vector3f(0,-80f,0)); // Sets the gravity for the player.
    player.setPhysicsLocation(new Vector3f(10, 10, 10)); // Spawn location of the player.

    // Attaches the scene and the player to the rootnode and the physics space,
    // to make them appear in the game world.
    rootNode.attachChild(sceneModel);
    bulletAppState.getPhysicsSpace().add(landscape);
    bulletAppState.getPhysicsSpace().add(player);
    
    
  }

  private void setUpLight() { // Method that adds light so we see the scene
    AmbientLight al = new AmbientLight(); //Creates a light object
    al.setColor(ColorRGBA.White.mult(1.3f)); //Sets brightness
    rootNode.addLight(al); //Adds light to the rootNode

    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White);
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal()); //Points the light at that Vector
    rootNode.addLight(dl);
  }
  
  protected void CrossHairs() { // Gives the player crosshairs
    setDisplayStatView(false);
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt"); // Font of crosshair
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2); // Size of the crosshair
    ch.setText("+"); // crosshairs
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - ch.getLineWidth()/2,
      settings.getHeight() / 2 + ch.getLineHeight()/2, 0); // Dimensions
    guiNode.attachChild(ch); // Attaches to rootNode
  }
  
  private void HUD(){ // W.I.P. (trying to make a compass and HUD bar)
    compass = new Picture("compass");
        compass.setHeight(64.0f);
        compass.setWidth(64.0f);
        compass.setImage(getAssetManager(), "Models/Compass.jpg", true);
        compass.center();
        compassNode = new Node();
        compassNode.attachChild(compass);
        compassNode.setLocalTranslation(getGuiViewPort().getCamera().getWidth() - 100, getGuiViewPort().getCamera().getHeight() - 100, 0);
        updateCompass();
        getGuiNode().attachChild(compassNode);
  }
  
  private void updateCompass() {
        float[] angles = camera.getLocalRotation().toAngles(null);
        compassNode.setLocalRotation(new Quaternion(new float[]{0.0f, 0.0f, angles[1]}));
    }
  
  private void setUpKeys() { //Sets up the keys that will be used in the game.
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addMapping("Place", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
    inputManager.addMapping("Break", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addMapping("Scroll", new KeyTrigger(KeyInput.KEY_LSHIFT));
    inputManager.addListener(this, "Left"); // Adds listener to the key at runtime.
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Up");
    inputManager.addListener(this, "Down");
    inputManager.addListener(this, "Jump");
    inputManager.addListener(this, "Place");
    inputManager.addListener(this, "Break");
    inputManager.addListener(this, "Scroll");
   
  }

  public void onAction(String binding, boolean isPressed, float tpf) { //These are the custom key actions.
    if (binding.equals("Left")) { //Move left
      left = isPressed;
    } else if (binding.equals("Right")) { //Move right
      right= isPressed;
    } else if (binding.equals("Up")) { //Move forward
      up = isPressed;
    } else if (binding.equals("Down")) { //Move backward
      down = isPressed;
    } else if (binding.equals("Jump")) { //Jump

      if (isPressed) { player.jump(new Vector3f(0,20f,0));}
    }
    if(binding.equals("Place")){ //Places block if right click is pressed.
        if(isPressed){
            Vector3f blockLocation = getCurrentBlockLocation(true);
            //(The block location is null, if the user looks in the sky or out of the map)
            if(blockLocation != null){
            //places a block
            makeCube(blockInHand, (float) (blockLocation.x + 0.5), blockLocation.y + 1, (float) (blockLocation.z + 0.5));
            }
        }
  }
   if(binding.equals("Break")){ //Deletes a block if left click is placed
        if(isPressed){
            Vector3f blockLocation = getCurrentBlockLocation(true);
            CollisionResults results = getCastingResults(rootNode);
            //(The block location is null, if the user looks in the sky or out of the map)
            if(blockLocation != null){
            //deletes the block
            delet(results);        
            }
        }    
  }
   if(binding.equals("Scroll")){ //Changes block if left shift is pressed
       if(isPressed){
           scrollNum++; 
           
           if(scrollNum == 0){
              blockInHand = ("Textures/grass6.png");
           }
           if(scrollNum == 1){
              blockInHand = ("Textures/cobblestone2.jpg");
           }
           if(scrollNum == 2){
               blockInHand = ("Textures/LimaBeanOre.png");
         }
           if(scrollNum == 3){
               blockInHand = ("Textures/wood3.png");
           }
           if(scrollNum == 4){
               blockInHand = ("Textures/fatSal.jpg");
           }
           if(scrollNum == 5){
               blockInHand = ("Textures/grass6.png");
               scrollNum = 0;
           }
       }
   }
}
  
  private CollisionResults getCastingResults(Node node){ //Method used for building/breaking. Finds nearest casting results.
    Vector3f origin = cam.getWorldCoordinates(new Vector2f((settings.getWidth() / 2), (settings.getHeight() / 2)), 0.0f);
    Vector3f direction = cam.getWorldCoordinates(new Vector2f((settings.getWidth() / 2), (settings.getHeight() / 2)), 0.3f);
    direction.subtractLocal(origin).normalizeLocal();
    Ray ray = new Ray(origin, direction);
    CollisionResults results = new CollisionResults();
    node.collideWith(ray, results);
    return results;
}
  
  private Vector3f getCurrentBlockLocation(boolean getNeighborLocation){ //Finds the world location of a block.
    CollisionResults results = getCastingResults(rootNode);
    if(results.size() > 0){
        Vector3f collisionContactPoint = results.getClosestCollision().getContactPoint();
        //return collisionContactPoint;
        return BlockTools.getBlockLocation(landscape, collisionContactPoint, getNeighborLocation);
    }
    return null;
}
  
  public void delet(CollisionResults results){ //Method for deleting a block.
          Geometry block = results.getClosestCollision().getGeometry();
          getPhysicsSpace().remove(block);
          results.getClosestCollision().getGeometry().removeFromParent();
          
    }  

  @Override
    public void simpleUpdate(float tpf) { //This method updates the move direction based on the key input and updates the direction.
        camDir.set(cam.getDirection()).multLocal(0.1f);
        camLeft.set(cam.getLeft()).multLocal(0.1f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection); //Sets the new walk direction.
        cam.setLocation(player.getPhysicsLocation()); //Changes location of camera with walk direction.
    }
     
    //Used to create blocks. Mostly used only to create blocks placed by the player.
    Geometry makeCube(String name, float x, float y, float z) {
    Box box = new Box(0.5f, 0.5f, 0.5f); 
    cube = new Geometry(name, box); //Creates block geometry
    cube.setLocalTranslation(x, y, z);
    Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat1.setTexture("ColorMap", assetManager.loadTexture(blockInHand)); //Texture
    cube.setMaterial(mat1);
    CollisionShape cubeShape =
            CollisionShapeFactory.createMeshShape(cube); //collision shape 
      blockscape = new RigidBodyControl(cubeShape, 0);
    cube.addControl(blockscape);
    bulletAppState.getPhysicsSpace().add(blockscape);
    rootNode.attachChild(cube); //attaches cube to root node
    blockArr.add(cube);
    return cube;
  }
    
    //Used to create blocks. Used for making the default scene of blocks.
    Geometry makeSceneCubes(String name, float x, float y, float z) {
    Box box = new Box(0.5f, 0.5f, 0.5f);
    cube = new Geometry(name, box);
    cube.setLocalTranslation(x, y, z);
    Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat1.setTexture("ColorMap", assetManager.loadTexture(name));
    cube.setMaterial(mat1);
    CollisionShape cubeShape =
            CollisionShapeFactory.createMeshShape(cube);
      blockscape = new RigidBodyControl(cubeShape, 0);
    cube.addControl(blockscape);
    bulletAppState.getPhysicsSpace().add(blockscape);
    rootNode.attachChild(cube);
    blockArr.add(cube);
    return cube;
  }
    
    public void makeScene(float x, float y){
        for(float e = 0; e < x; e++){
            for(float r = 0; r < y; r++){
                makeSceneCubes("Textures/grass6.png", (float) (e + 0.5),0, (float) (r + 0.5));
                makeSceneCubes("Textures/grass6.png",(float) (e + 0.5),-1, (float) (r + 0.5));
                makeSceneCubes("Textures/cobblestone2.jpg",(float) (e + 0.5),-2, (float) (r + 0.5));
            }
        }
    }
    
     private void Music() {
    //Music from Minecraft that plays on a loop
    audio_nature = new AudioNode(assetManager, "Sounds/calm2.ogg", DataType.Stream);
    audio_nature.setLooping(true);  // activate continuous playing
    audio_nature.setPositional(false);
    audio_nature.setVolume(3);
    rootNode.attachChild(audio_nature);
    audio_nature.play(); // play continuously
  }

}
