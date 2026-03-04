package apply;

import java.util.NoSuchElementException;

/**
 * A simple database for storing and querying {@link DaleRecord} objects by pond and timestamp.
 * <p>
 * World model:
 * <ul>
 *   <li>Ponds contain activity records.</li>
 *   <li>Ducks may enter/exit ponds (boundary events) and report caught fish (fish reports).</li>
 *   <li>Cow names may appear alongside duck names; which names are cows is unknown.</li>
 *   <li>Low-activity ponds may be evicted to save space.</li>
 * </ul>
 *
 * <p>Record types include:
 * <ul>
 *   <li><b>BoundaryEvent</b>: entrances/exits at pond boundaries, listing animals involved and the
 *       number of cows detected among them.</li>
 *   <li><b>FishReport</b>: stores a duck’s list of fish weights caught during a fishing event.</li>
 * </ul>
 *
 * @author Kaylee Henry the author of this class
 * @version 1.0 the version of this implementation
 * @userid khenry61 the Georgia Tech user id
 * @GTID 904065531 the Georgia Tech ID number
 */
public class DaleDB {

    /**
     * Constructs a new instance of the {@code DaleDB} class.
     */
    public DaleDB() {
        // Constructor implementation

    }

    /**
     * Constructs a {@code DaleDB} from a serialized dale record source.
     * <p>
     * Note: Placeholder documentation until the constructor signature is defined.
     */
    // PUT HERE

    /**
     * Accepts any type of record and inserts it into the database, or modifies it if it already exists,
     * both in {@code O(log(r))} time.
     * <p>
     * Returns the old {@link DaleRecord} if it was modified; otherwise returns {@code null}.
     *
     * @param key the record key (e.g., pond name and timestamp identifier)
     * @param value the record payload to store for the given key
     * @return the old {@link DaleRecord} if it was modified; otherwise {@code null}
     * @throws IllegalArgumentException if the key or value is {@code null}
     */
    public static DaleRecord putRecord(String key, String value) {
        // Implementation for putting a record into the database

        //error handling for null pond name or timestamp
        if (key == null || value == null) {
            throw new IllegalArgumentException("Pond name and timestamp cannot be null.");
        }

        return null;
    }

    /**
     * Accepts a pond name and timestamp, and deletes the record with that timestamp from
     * that pond in {@code O(log(r))} time.
     * <p>
     * Returns the deleted {@link DaleRecord}.
     *
     * @param key the record key to delete (e.g., pond name and timestamp identifier)
     * @return the deleted {@link DaleRecord}
     * @throws IllegalArgumentException if the key is {@code null}
     * @throws NoSuchElementException if the record does not exist
     */
    public static DaleRecord deleteRecord(String key) {
        // Implementation for deleting a record from the database

        // error handling for null pond name
        if (key == null) {
            throw new IllegalArgumentException("Pond name cannot be null.");
        }

        return null;
    }

    /**
     * Accepts a pond name and timestamp, and returns the record with that timestamp from
     * that pond in {@code O(log(r))} time.
     *
     * @param key the record key to search for (e.g., pond name and timestamp identifier)
     * @return the {@link DaleRecord} associated with the given key
     * @throws IllegalArgumentException if the key is {@code null}
     * @throws NoSuchElementException if the record does not exist
     */
    public static DaleRecord getRecord(String key) {
        // Implementation for retrieving a record from the database

        // error handling for null pond name
        if (key == null) {
            throw new IllegalArgumentException("Pond name cannot be null.");
        }

        return null;
    }

    /**
     * Accepts a pond name and returns a list of all records in that pond in {@code O(r)} time.
     * The list must be sorted in ascending order by timestamp.
     * <p>
     * If the pond does not exist, returns an empty list.
     *
     * @param pondName the name of the pond to retrieve records for
     * @return a list of all records in the specified pond, sorted in ascending timestamp order
     * @throws IllegalArgumentException if {@code pondName} is {@code null}
     */
    public static DaleRecord getPond(String pondName) {
        // Implementation for retrieving a pond record from the database

        // error handling for null pond name
        if (pondName == null) {
            throw new IllegalArgumentException("Pond name cannot be null.");
        }

        return null;
    }

    /**
     * Returns the number of ponds in the database in {@code O(1)} time.
     *
     * @return the number of ponds currently stored
     */
    public static int getNumPonds() {
        // Implementation for retrieving the number of ponds in the database
        return 0;
    }

    /**
     * Accepts a pond name, start timestamp, and end timestamp, and returns a list of all records
     * in that pond in the inclusive range {@code [start, end]}.
     * The returned list must be sorted in ascending order by timestamp.
     * <p>
     * If there are no values within the range (or the range is impossible), returns an empty list.
     *
     * @param pondName the name of the pond to query
     * @param start the start timestamp (inclusive)
     * @param end the end timestamp (inclusive)
     * @return a list of all records in the specified pond within the inclusive range {@code [start,end]}
     * @throws IllegalArgumentException if {@code pondName} is {@code null}
     * @throws IllegalArgumentException if {@code start} or {@code end} is {@code null}
     * @throws NoSuchElementException if the pond does not exist
     */
    public static DaleRecord getPondRange(String pondName, String start, String end) {
        // Implementation for retrieving a range of records from a pond in the database

        // error handling for null pond name
        if (pondName == null) {
            throw new IllegalArgumentException("Pond name cannot be null.");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end timestamps cannot be null.");
        }

        return null;
    }
}