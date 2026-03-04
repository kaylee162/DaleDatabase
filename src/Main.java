import apply.StaticDaleDB;
import refactor.StaticTreeMap;
import refactor.TreeMap;

/**
 * Entry point for accessing your project 2 files.
 *
 * @author Kaylee Henry
 * @version 1.0
 * @userid khenry61
 * @GTID 904065531
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
 * Agree Here: I agree
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
        return new TreeMap<>(); // Fixed: instantiate your TreeMap implementation
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