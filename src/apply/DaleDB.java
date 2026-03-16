package apply;

import implement.HashMap;
import refactor.TreeMap;
import refactor.StaticTreeMap;

import java.util.ArrayList;
import java.util.List;
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
 * @author Kaylee Henry
 * @version 1.0
 * @userid khenry61
 * @GTID 904065531
 * <br>
 * <p>
 * Collaborators: NONE
 * <p>
 * Resources: NONE
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
public class DaleDB implements StaticDaleDB {

    /**
     * Stores all records for one pond, keyed by timestamp.
     * Also tracks when that pond was last accessed so eviction
     * can remove the least recently used pond.
     */
    private static class PondData {
        private StaticTreeMap<Long, DaleRecord> records;
        private long lastAccessTime;

        PondData() {
            records = new TreeMap<>();
            lastAccessTime = -1L;
        }
    }

    /**
     * Helper object used during mergeReports to gather all fish reports
     * belonging to the same duck.
     */
    private static class MergeBucket {
        private long latestTimestamp;
        private List<Long> oldTimestamps;
        private List<Double> mergedWeights;

        MergeBucket(long latestTimestamp, List<Double> initialWeights) {
            this.latestTimestamp = latestTimestamp;
            this.oldTimestamps = new ArrayList<>();
            this.mergedWeights = new ArrayList<>();

            // Copy weights so we dont alias the original list.
            for (Double weight : initialWeights) {
                this.mergedWeights.add(weight);
            }
        }
    }

    // Top-level map: pond name -> all data for that pond
    private HashMap<String, PondData> ponds;

    // Number of ponds currently stored
    private int pondCount;

    // Monotonically increasing logical clock used for LRU tracking
    private long accessClock;

    /**
     * Constructs a new instance of the {@code DaleDB} class
     */
    public DaleDB() {
        // Constructor implementation
        ponds = new HashMap<>();
        pondCount = 0;
        accessClock = 0L;
    }

    /**
     * Touches a pond so it becomes the most recently accessed pond
     *
     * @param pondData the pond to mark as accessed
     */
    private void touchPond(PondData pondData) {
        accessClock++;
        pondData.lastAccessTime = accessClock;
    }

    /**
     * Returns the pond data for the given pond, throwing if it does not exist
     *
     * @param pond the pond name
     * @return the pond data object
     * @throws IllegalArgumentException if pond is null
     * @throws NoSuchElementException if pond does not exist
     */
    private PondData requirePond(String pond) {
        if (pond == null) {
            throw new IllegalArgumentException("Pond name cannot be null");
        }
        if (!ponds.containsKey(pond)) {
            throw new NoSuchElementException("Pond does not exist");
        }
        return ponds.get(pond);
    }

    /**
     * Accepts any type of record and inserts it into the database, or modifies it if it already exists,
     * both in {@code O(log(r))} time
     * <p>
     * Returns the old {@link DaleRecord} if it was modified; otherwise returns {@code null}
     *
     * @param daleRecord the {@link DaleRecord} to insert or update
     * @return the old {@link DaleRecord} if it was modified; otherwise {@code null}
     * @throws IllegalArgumentException if the key or value is {@code null}
     */
    @Override
    public DaleRecord putRecord(DaleRecord daleRecord) {
        // Implementation for putting a record into the database

        //error handling for null pond name or timestamp
        if (daleRecord == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }

        String pond = daleRecord.pond();
        Long timestamp = daleRecord.timestamp();

        if (pond == null || timestamp == null) {
            throw new IllegalArgumentException("Pond name and timestamp cannot be null");
        }

        PondData pondData;

        // Create the pond lazily the first time we see it
        if (!ponds.containsKey(pond)) {
            pondData = new PondData();
            ponds.put(pond, pondData);
            pondCount++;
        } else {
            pondData = ponds.get(pond);
        }

        // Any successful access should refresh LRU state
        touchPond(pondData);

        // TreeMap.put returns the old record if one existed at this timestamp
        return pondData.records.put(timestamp, daleRecord);
    }

    /**
     * Accepts a pond name and timestamp, and deletes the record with that timestamp from
     * that pond in {@code O(log(r))} time.
     * <p>
     * Returns the deleted {@link DaleRecord}.
     *
     * @param pond the pond key to delete from
     * @param timestamp the timestamp key to remove
     * @return the deleted {@link DaleRecord}
     * @throws IllegalArgumentException if the key is {@code null}
     * @throws NoSuchElementException if the record does not exist
     */
    @Override
    public DaleRecord deleteRecord(String pond, long timestamp) {
        // Implementation for deleting a record from the database

        // error handling for null pond name
        PondData pondData = requirePond(pond);

        if (!pondData.records.containsKey(timestamp)) {
            throw new NoSuchElementException("Record does not exist");
        }

        // Mark the pond as recently used
        touchPond(pondData);

        DaleRecord removed = pondData.records.remove(timestamp);

        // If this pond becomes empty, remove the pond itself from the top-level map
        if (pondData.records.size() == 0) {
            ponds.remove(pond);
            pondCount--;
        }

        return removed;
    }

    /**
     * Accepts a pond name and timestamp, and returns the record with that timestamp from
     * that pond in {@code O(log(r))} time
     *
     * @param pond the pond key to search in
     * @param timestamp the timestamp key to search for
     * @return the {@link DaleRecord} associated with the given key
     * @throws IllegalArgumentException if the key is {@code null}
     * @throws NoSuchElementException if the record does not exist
     */
    @Override
    public DaleRecord getRecord(String pond, long timestamp) {
        // Implementation for retrieving a record from the database

        // error handling for null pond name
        PondData pondData = requirePond(pond);

        if (!pondData.records.containsKey(timestamp)) {
            throw new NoSuchElementException("Record does not exist");
        }

        // Reads also count as access for LRU.
        touchPond(pondData);

        return pondData.records.get(timestamp);
    }

    /**
     * Accepts a pond name and returns a list of all records in that pond in {@code O(r)} time
     * The list must be sorted in ascending order by timestamp
     * <p>
     * If the pond does not exist, returns an empty list
     *
     * @param pondName the name of the pond to retrieve records for
     * @return a list of all records in the specified pond, sorted in ascending timestamp order
     * @throws IllegalArgumentException if {@code pondName} is {@code null}
     */
    @Override
    public List<DaleRecord> getPond(String pondName) {
        // Implementation for retrieving a pond record from the database

        // error handling for null pond name
        if (pondName == null) {
            throw new IllegalArgumentException("Pond name cannot be null");
        }

        // Missing pond returns an empty list
        if (!ponds.containsKey(pondName)) {
            return new ArrayList<>();
        }

        PondData pondData = ponds.get(pondName);

        // Accessing the pond updates its recency
        touchPond(pondData);

        // TreeMap.values() is already in ascending key order
        return pondData.records.values();
    }

    /**
     * Returns the number of ponds in the database in {@code O(1)} time
     *
     * @return the number of ponds currently stored
     */
    @Override
    public int getPondCount() {
        // Implementation for retrieving the number of ponds in the database
        return pondCount;
    }

    /**
     * Accepts a pond name, start timestamp, and end timestamp, and returns a list of all records
     * in that pond in the inclusive range {@code [start, end]}
     * The returned list must be sorted in ascending order by timestamp
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
    @Override
    public List<DaleRecord> getRecordRange(String pondName, long start, long end) {
        // Implementation for retrieving a range of records from a pond in the database

        // error handling for null pond name
        PondData pondData = requirePond(pondName);

        // Impossible range should just return an empty list
        if (start > end) {
            touchPond(pondData);
            return new ArrayList<>();
        }

        // Range query still counts as access
        touchPond(pondData);

        return pondData.records.getRange(start, end);
    }

    /**
     * Evicts the {@code k} least recently accessed ponds from the database
     * <p>
     * The removed records should be returned as a list, sorted first by
     * ascending order of eviction, and second by ascending order of
     * timestamp.
     *
     * @param k the number of ponds to evict
     * @return a list of all records removed as a result of eviction
     * @throws IllegalArgumentException if {@code k} is nonpositive
     * @throws NoSuchElementException if there are less than {@code k} ponds
     */
    @Override
    public List<DaleRecord> evict(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        if (pondCount < k) {
            throw new NoSuchElementException("Not enough ponds to evict");
        }

        List<DaleRecord> removedRecords = new ArrayList<>();

        // Evict one pond at a time so the returned list is ordered by
        // eviction order, then by ascending timestamp within each pond
        for (int i = 0; i < k; i++) {
            String lruPond = null;
            long oldestAccess = Long.MAX_VALUE;

            // Scan all ponds to find the least recently accessed one
            // This fits the allowed O(kr) style complexity for eviction
            for (String pond : ponds) {
                PondData pondData = ponds.get(pond);
                if (pondData.lastAccessTime < oldestAccess) {
                    oldestAccess = pondData.lastAccessTime;
                    lruPond = pond;
                }
            }

            // Safety check, though logically this should never be null here
            if (lruPond == null) {
                throw new NoSuchElementException("No pond available for eviction");
            }

            PondData evicted = ponds.remove(lruPond);
            pondCount--;

            // Records are already in ascending timestamp order.
            List<DaleRecord> pondRecords = evicted.records.values();
            for (DaleRecord record : pondRecords) {
                removedRecords.add(record);
            }
        }

        return removedRecords;
    }

    /**
     * Computes the maximum concurrent ducks in a pond
     * <p>
     * This method should examine {@link DaleRecord.BoundaryEvent}
     * records over time, taking the number of ducks and cows at each
     * event occurrence into account
     *
     * @param pond the pond key
     * @return the maximum number ducks in a pond at one time
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if {@code pond} not in database
     */
    @Override
    public int getPeakConcurrentOccupancy(String pond) {
        PondData pondData = requirePond(pond);

        // This analytical read should also affect recency
        touchPond(pondData);

        int currentDucks = 0;
        int peakDucks = 0;

        // Records are visited in ascending timestamp order
        for (DaleRecord record : pondData.records) {
            if (record instanceof DaleRecord.BoundaryEvent event) {
                // Number of ducks is total names minus the known number of cows
                int ducksAtEvent = event.names().size() - event.numCows();

                if (event.type() == DaleRecord.BoundaryEvent.TransitionType.ENTRANCE) {
                    currentDucks += ducksAtEvent;
                    if (currentDucks > peakDucks) {
                        peakDucks = currentDucks;
                    }
                } else {
                    currentDucks -= ducksAtEvent;
                }
            }
        }

        return peakDucks;
    }

    /**
     * Retrieves the most frequent visitor to the specified pond.
     * <p>
     * This method should examine {@link DaleRecord.BoundaryEvent}
     * records over time, taking into account who is visiting the
     * pond the most times
     *
     * @param pond the pond key
     * @return the name of the most frequent visitor; {@code null} otherwise
     * @throws IllegalArgumentException if {@code pond} is {@code null}
     * @throws NoSuchElementException if {@code pond} not in database
     */
    @Override
    public String getMostFrequentVisitor(String pond) {
        PondData pondData = requirePond(pond);

        // Accessing this pond updates its recency
        touchPond(pondData);

        // Count how many times each animal appears in boundary events
        HashMap<String, Integer> visitCounts = new HashMap<>();

        for (DaleRecord record : pondData.records) {
            if (record instanceof DaleRecord.BoundaryEvent event) {
                for (String name : event.names()) {
                    int oldCount = visitCounts.getOrDefault(name, 0);
                    visitCounts.put(name, oldCount + 1);
                }
            }
        }

        // If there were no boundary events, there is no frequent visitor
        if (visitCounts.size() == 0) {
            return null;
        }

        String bestName = null;
        int bestCount = -1;

        // Choose the highest count. On ties, use lexicographically smaller
        // name for deterministic behavior
        for (String name : visitCounts) {
            int count = visitCounts.get(name);

            if (bestName == null
                    || count > bestCount
                    || (count == bestCount && name.compareTo(bestName) < 0)) {
                bestName = name;
                bestCount = count;
            }
        }

        return bestName;
    }

    /**
     * Merges fishing reports for each duck within the specified pond
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
     */
    @Override
    public HashMap<Long, List<Long>> mergeReports(String pond) {
        PondData pondData = requirePond(pond);

        // Merging mutates the pond, so it definitely counts as an access
        touchPond(pondData);

        HashMap<String, MergeBucket> buckets = new HashMap<>();
        HashMap<Long, List<Long>> mergedInto = new HashMap<>();

        // Walk the records in ascending timestamp order so concatenated weights
        // are naturally assembled in chronological order.
        for (DaleRecord record : pondData.records) {
            if (record instanceof DaleRecord.FishReport report) {
                String duck = report.duck();

                if (!buckets.containsKey(duck)) {
                    // First report for this duck.
                    buckets.put(duck, new MergeBucket(report.timestamp(), report.weights()));
                } else {
                    MergeBucket bucket = buckets.get(duck);

                    // The previous "latest" timestamp now becomes an old timestamp,
                    // because we found a newer report for the same duck
                    bucket.oldTimestamps.add(bucket.latestTimestamp);

                    // Append this report's weights in ascending timestamp order
                    for (Double weight : report.weights()) {
                        bucket.mergedWeights.add(weight);
                    }

                    // Update latest timestamp to the current report's timestamp
                    bucket.latestTimestamp = report.timestamp();
                }
            }
        }

        // Apply the merges back into the pond tree
        for (String duck : buckets) {
            MergeBucket bucket = buckets.get(duck);

            // Remove the older reports, if any.
            for (Long oldTimestamp : bucket.oldTimestamps) {
                pondData.records.remove(oldTimestamp);
            }

            // Always write back the final merged report, even if this duck only had one report.
            DaleRecord.FishReport mergedReport =
                    new DaleRecord.FishReport(pond, bucket.latestTimestamp, duck, bucket.mergedWeights);
            pondData.records.put(bucket.latestTimestamp, mergedReport);

            // Always include an entry in the returned map.
            // If nothing was merged, this will just be an empty list.
            mergedInto.put(bucket.latestTimestamp, bucket.oldTimestamps);
        }

        return mergedInto;
    }
}