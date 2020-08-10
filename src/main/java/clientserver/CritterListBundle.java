package clientserver;

import model.Critter;
import model.Location;

import java.util.ArrayList;

/**
 *this bundle is used to contain the location of the critters
 */
public class CritterListBundle {
    ArrayList<Location> locationList;

    public CritterListBundle(ArrayList<Location> ll){
        locationList = ll;
    }

}
