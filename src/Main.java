import apply.StaticDaleDB;
import refactor.StaticTreeMap;

/**
 * Entry point for accessing your project 2 files.
 *
 * @author YOUR NAME HERE
 * @version 1.0
 * @userid YOUR USER ID HERE (i.e. gburdell3)
 * @GTID YOUR GT ID HERE (i.e. 900000000)
 * <br>
 * <p>
 * Collaborators: LIST ALL COLLABORATORS YOU WORKED WITH HERE
 * <p>
 * Resources: LIST ALL NON-COURSE RESOURCES YOU CONSULTED HERE
 * <p>
 * <br>
 * By typing 'I agree' below, you are agreeing that this is your
 * own work and that you are responsible for the contents of all
 * submitted files. If this is left blank, this project will lose
 * points.
 *<p>
 *<br>
 * Agree Here: REPLACE THIS TEXT
 */
public class Main {


    /**
     * Creates and returns a new instance of your class implementing
     * {@link StaticTreeMap}.
     *
     * @param <K> the type of keys; must implement {@link Comparable}
     * @param <V> the type of values
     * @return a new {@link StaticTreeMap} instance
     * @apiNote This method must be implemented for unit tests to run.
     */
    public static <K extends Comparable<? super K>, V> StaticTreeMap<K, V> getTreeMapInstance() {
        throw new UnsupportedOperationException("Instantiate your class here!"); // Replace this line
    }

    /**
     * Creates and returns a new instance of your class implementing
     * {@link StaticDaleDB}.
     *
     * @return a new {@link StaticDaleDB} instance
     * @apiNote This method must be implemented for unit tests to run.
     */
    public static StaticDaleDB getDaleDBInstance() {
        throw new UnsupportedOperationException("Instantiate your class here!"); // Replace this line
    }
}