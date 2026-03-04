package apply;

import java.util.NoSuchElementException;

public class DaleDB {
    /**
     * @author Kaylee Henry
     * @version 1.0
     * @userid khenry61
     * @GTID 904065531
     */
    /** Our world is simple: there are Ponds, Ducks, Fish, and Cows.
     *  - Ducks fly around, and sometimes enter and exit ponds. These movements are tracked in the
     *  database.
     * 
     *  - When at a pond, a duck can go fishing. Every so often, a duck will report its caught fish to the
     *  database.
     * 
     *  - Sometimes, cows wander into or out of a pond area. Their name is recorded alongside the duck
     *  names, but it is unknown which names are cows and which are ducks.
     * 
     *  - To save space in the database, we prioritize recording information for active ponds. If a pond has
     *  little or no activity, its records may be destroyed (see evict).
     
    
     * BoundaryEvent: marks entrances and exits at pond boundaries, listing the animals involved and
     * the number of cows detected among those listed animals.
     * 
     * FishReport: stores a duck’s list of fish weights caught during a fishing event 
     */


    /**
     * Constructs a new instance of the DaleDB class.
     */
    public DaleDB() {
        // Constructor implementation

    }

    /**
     * Constructs a DaleDB from a dale record????.
     */
    // PUT HERE

    /**
     * This method accepts any type of record and inserts it into the database, or modifies it if it already exists,
     * both in O(log(r)) time. 
     * 
     * Return the old DaleRecord if it was modified, otherwise return null
     * 
     * @param key
     * @param value
     * @return the old DaleRecord if it was modified, otherwise return null
     * @throws IllegalArgumentException if the pond name or timestamp is null
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
     * This method accepts a pond name and timestamp, and deletes the record with that timestamp from 
     * that pond in O(log(r)) time. 
     * 
     * Return the deleted DaleRecord. If the pond name is null, throw an IllegalArgumentException. If the 
     * record does not exist, throw a NoSuchElementException. 
     * @param key
     * @return the deleted DaleRecord
     * @throws IllegalArgumentException if the pond name is null
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
     * This method accepts a pond name and timestamp, and returns the record with that timestamp from
     * that pond in O(log(r)) time.
     * 
     * If the pond name is null, throw an IllegalArgumentException. If the record does not exist, throw
     * a NoSuchElementException
     * 
     * @param key
     * @return the DaleRecord with the specified key
     * @throws IllegalArgumentException if the pond name is null
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
     * This method accepts a pond name and returns a list of all records in that pond in O(r) time. The list
     * must be sorted in ascending order by timestamp.
     * 
     * If the pond name is null, throw an IllegalArgumentException. Note that an existing pond should
     * never be empty, as empty ponds are removed from the database. If the pond does not exist, you should
     * 
     * return an empty list as there are no records associated with this pond name.
     * 
     * @param pondName
     * @return a list of all records in the specified pond, sorted in ascending order by
     * @throws IllegalArgumentException if the pond name is null
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
     * his method simply returns the number of ponds in the database in O(1) time. In the example below
     * on this page, there are two ponds, Pond X and Pond Y, so the method call would return 2.
     */
    public static int getNumPonds() {
        // Implementation for retrieving the number of ponds in the database
        return 0;
    }

    /**
     * This method accepts a pond name, start timestamp, and end timestamp, and returns a list of all records
     * in that pond in the inclusive range [start,end]. The returned list must be sorted in ascending order by
     * timestamp.
     * 
     * If the pond name is null, throw an IllegalArgumentException. Note that an existing pond should
     * never be empty, as empty ponds are removed from the database. If the pond does not exist, throw a
     * NoSuchElementException.
     * 
     * If there are no values within the range (or the range is impossible), return an empty list. The bounds
     * are not necessarily timestamps that actually correspond to a record.
     * 
     * Your code must have an O(r) time complexity and O(log(r)) auxiliary space complexity (which ex-
     * cludes the output list).
     * 
     * @param pondName
     * @param start
     * @param end
     * @return a list of all records in the specified pond within the inclusive range [start,end]
     * @throws IllegalArgumentException if the pond name is null
     * @throws IllegalArgumentException if the start or end timestamp is null
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
