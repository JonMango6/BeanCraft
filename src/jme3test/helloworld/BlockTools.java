package jme3test.helloworld;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
/**
 *
 * @author jonma
 */

public class BlockTools{
    
    //Used for building. Gets the world location of the block in the crosshairs.
    public static Vector3f getBlockLocation(RigidBodyControl blockTerrain, Vector3f collisionContactPoint, boolean getNeighborLocation){
        Vector3f collisionLocation = collisionContactPoint;
        Vector3f blockLocation = new Vector3f(
                (int) (collisionLocation.getX() / 1),
                (int) (collisionLocation.getY() / 1),
                (int) (collisionLocation.getZ() / 1)); //location of block divided by size of block.
        if((blockLocation != null) == getNeighborLocation){
            if((collisionLocation.getX()) == 0) {
                blockLocation.subtractLocal(1, 0, 0);
            }
            else if((collisionLocation.getY()) == 0){
                blockLocation.subtractLocal(0, 1, 0);
            }
            else if((collisionLocation.getZ()) == 0){
                blockLocation.subtractLocal(0, 0, 1);
            }
         
        }
        return blockLocation;
    }
    
    
}
