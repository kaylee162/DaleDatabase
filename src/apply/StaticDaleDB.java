package apply;

import implement.HashMap;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Interface for your custom {@code DaleDB} implementation.
 * <p>
 * This API will function as a database performing basic operations,
 * as well as some more complex analytical methods in the context
 * of the provided scenario. More details are provided in the PDF.
 *
 * @apiNote DO NOT MODIFY THIS FILE!
 * @implSpec You must use your own map structures to solve all methods.
 * Any Java-provided implementation of the {@link Map} interface is banned.
 * @version 1.0
 * @author CS 1332 TAs
 */
public interface StaticDaleDB {
    /**
     * Inserts or updates a record in the database.
     *
     * @param daleRecord the {@link DaleRecord} to insert or update
     * @return the old record if replaced; {@code null} otherwise
     * @throws IllegalArgumentException if {@code record} is {@code null}
     * @implSpec {@code O(log r)} runtime
     */
    DaleRecord putRecord(DaleRecord daleRecord);

    /**
     * Deletes a record from the database, given its pond and timestamp,
     * if it exists.
     *
     * @param pond the pond key (pond ID)
     * @param timestamp the sort key (event time)
     * @return the associated record
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if a matching record doesn't exist
     * @implSpec {@code O(log r)} runtime
     */
    DaleRecord deleteRecord(String pond, long timestamp);

    /**
     * Retrieves a record from the database, given its pond and timestamp.
     *
     * @param pond the pond key (pond ID)
     * @param timestamp the sort key (event time)
     * @return the associated record
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if a matching record doesn't exist
     * @implSpec {@code O(log r)} runtime
     */
    DaleRecord getRecord(String pond, long timestamp);

    /**
     * Retrieves all records belonging to a pond.
     * <p>
     * The returned list must be sorted in ascending order of timestamp.
     *
     * @param pond the pond key
     * @return a sorted list of all records belonging to {@code pond}
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @implSpec {@code O(r)} runtime
     */
    List<DaleRecord> getPond(String pond);

    /**
     * Retrieves the count of how many ponds are in the database.
     *
     * @return the number of ponds in DaleDB
     * @implSpec {@code O(1)} runtime
     */
    int getPondCount();

    /**
     * Retrieves all records within a timestamp range (inclusive) belonging
     * to a pond.
     * <p>
     * The returned list must be sorted in ascending order of timestamp.
     *
     * @param pond the pond key
     * @param start the start timestamp (inclusive)
     * @param end the end timestamp (inclusive)
     * @return a sorted list of records in the {@code pond}
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if the {@code pond} doesn't exist
     * @implSpec
     * <p>{@code O(r)} runtime
     * <p>{@code O(log r)} auxiliary space
     */
    List<DaleRecord> getRecordRange(String pond, long start, long end);

    /**
     * Evicts the {@code k} least recently accessed ponds from the database.
     * <p>
     * The removed records should be returned as a list, sorted first by
     * ascending order of eviction, and second by ascending order of
     * timestamp.
     *
     * @param k the number of ponds to evict
     * @return a list of all records removed as a result of eviction
     * @throws IllegalArgumentException if {@code k} is nonpositive
     * @throws NoSuchElementException if there are less than {@code k} ponds
     * @implSpec {@code O(kr)} runtime
     */
    List<DaleRecord> evict(int k);

    /**
     * Evicts the least recently accessed pond from the database.
     * <p>
     * The removed records should be returned as a list, sorted by
     * ascending order of timestamp.
     *
     * @return a list of all records removed as a result of eviction
     * @throws NoSuchElementException if the database is empty
     * @implSpec {@code O(r)} runtime
     */
    default List<DaleRecord> evict() {
        return evict(1);
    }

    /**
     * Computes the maximum concurrent ducks in a pond.
     * <p>
     * This method should examine {@link DaleRecord.BoundaryEvent}
     * records over time, taking the number of ducks and cows at each
     * event occurrence into account.
     *
     * @param pond the pond key
     * @return the maximum number ducks in a pond at one time
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if {@code pond} not in database
     * @implSpec {@code O(r)} runtime
     * <p> {@code O(log r)} auxiliary space
     */
    int getPeakConcurrentOccupancy(String pond);

    /**
     * Retrieves the most frequent visitor to the specified pond.
     * <p>
     * This method should examine {@link DaleRecord.BoundaryEvent}
     * records over time, taking into account who is visiting the
     * pond the most times.
     *
     * @param pond the pond key
     * @return the name of the most frequent visitor; {@code null} otherwise
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if {@code pond} not in database
     * @implSpec
     * <p> {@code O(r)} runtime

     */
    String getMostFrequentVisitor(String pond);

    /**
     * Merges fishing reports for each duck within the specified pond.
     * <p>
     * For each duck, this consolidates all readings into a single record
     * at the latest timestamp, pruning older records. The latest timestamp
     * for each duck's fish report will have all lists concatenated to one
     * another in ascending order of timestamp. The result maps each latest
     * timestamp to the list of pruned timestamps for that sensor, also
     * sorted in ascending order.
     *
     * @param pond the pond key
     * @return a map where each key is the latest timestamp and each value
     *         is the list of timestamps that were merged into it, in
     *         ascending order
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if {@code pond} not in database
     * @implSpec {@code O(r + k log r)} runtime, where {@code k << r}
     */
    HashMap<Long, List<Long>> mergeReports(String pond);
}