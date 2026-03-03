package apply;

import java.util.List;

/**
 * Represents a record stored in the DaleDB database.
 * <p>
 * Each record is associated with a pond and a timestamp.
 * DaleRecord is a sealed interface, implemented only by the record types
 * needed for the system: {@link BoundaryEvent} and {@link FishReport}.
 *
 * @apiNote DO NOT MODIFY THIS FILE!!
 * @version 1.0
 * @author CS 1332 TAs
 */
public sealed interface DaleRecord permits DaleRecord.BoundaryEvent, DaleRecord.FishReport {

    /**
     * Returns the pond ID associated with this record.
     * <p>
     * The pond key determines the logical grouping of records.
     *
     * @return the pond
     */
    String pond();

    /**
     * Returns the timestamp associated with this record.
     * <p>
     * The timestamp serves as the sort key for all records within a pond,
     * marking the chronological order of events.
     *
     * @return the timestamp
     * @apiNote events have millisecond latency and are unique, given that
     * they are located within the same pond.
     */
    Long timestamp();

    /**
     * Represents an arrival or departure event in DaleDB.
     * <p>
     * The record contains information of either an entrance or exit
     * transition occurring at the pond boundary.
     * <p>
     * These records are relevant for the following challenge problems:
     * <ul>
     *   <li>{@link StaticDaleDB#getPeakConcurrentOccupancy(String)}</li>
     *   <li>{@link StaticDaleDB#getMostFrequentVisitor(String)}</li>
     * </ul>
     *
     * @param pond the pond key (pond ID)
     * @param timestamp the sort key (event time)
     * @param type boundary transition of enum type {@link TransitionType TransitionType}
     * @param names the list of animal names that crossed the boundaries
     * @param numCows the number of cows involved
     */
    record BoundaryEvent(String pond, Long timestamp, TransitionType type, List<String> names, Integer numCows)
            implements DaleRecord {
        public enum TransitionType {
            ENTRANCE,
            EXIT
        }
    }

    /**
     * Represents a fishing record in DaleDB.
     * <p>
     * The record contains information regarding the results of a duck's
     * fishing event in the form of a report of fish weights it has caught.
     * <p>
     * These records are relevant for the following challenge problems:
     * <ul>
     *   <li>{@link StaticDaleDB#mergeReports(String)}</li>
     * </ul>
     * @param pond the pond key (pond ID)
     * @param timestamp the sort key (event time)
     * @param duck the name of the duck involved
     * @param weights a list of fish weights caught by this duck
     */
    record FishReport(String pond, Long timestamp, String duck, List<Double> weights)
            implements DaleRecord { }

}
