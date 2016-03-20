package edu.csuchico.ecst.ahorgan.neighbor.p2p;

/**
 * Created by annika on 3/20/16.
 */
public class World {
    private static World ourInstance = new World();

    public static World getInstance() {
        return ourInstance;
    }

    private World() {
    }
}
