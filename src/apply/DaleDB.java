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

    // Top-level map: pond name -> all data for that pond
    // A HashMap is the right choice here because pond lookup should be fast on average,
    // and we do not need pond names kept in sorted order.
    private HashMap<String, PondData> ponds;

    // Number of ponds currently stored
    // This is tracked explicitly so getPondCount() can run in O(1) time instead of
    // recomputing the number of ponds by scanning the map.
    private int pondCount;

    // Monotonically increasing logical clock used for LRU tracking
    // Using a logical clock is simpler and safer than relying on system time, and it gives
    // a deterministic access ordering for eviction decisions.
    private long accessClock;

    /**
     * Constructs a new instance of the {@code DaleDB} class
     */
    public DaleDB() {
        // Initialize the top-level storage for ponds.
        ponds = new HashMap<>();
        // Start with no ponds stored.
        pondCount = 0;
        // Start the logical access clock at 0 so future accesses can increment from here.
        accessClock = 0L;
    }

    /**
     * Touches a pond so it becomes the most recently accessed pond
     *
     * @param pondData the pond to mark as accessed
     */
    private void touchPond(PondData pondData) {
        // Increment the global logical clock first so every access gets a unique,
        // increasing timestamp.
        accessClock++;
        // Store the newest access time directly in the pond so eviction can later
        // compare ponds and remove the least recently used one.
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
        // Null pond names are invalid input, so reject them immediately.
        if (pond == null) {
            throw new IllegalArgumentException("Pond name cannot be null");
        }
        // This helper centralizes "pond must exist" validation so the same check
        // does not have to be rewritten in every method.
        if (!ponds.containsKey(pond)) {
            throw new NoSuchElementException("Pond does not exist");
        }
        // Return the already-validated pond data.
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
        // Rejecting null records early avoids null dereferences later and keeps
        // the database from storing invalid entries.
        if (daleRecord == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }

        // Pull the key fields out once so they can be validated and reused.
        String pond = daleRecord.pond();
        Long timestamp = daleRecord.timestamp();

        // Both pond and timestamp are essential parts of record identity, so neither
        // can be null.
        if (pond == null || timestamp == null) {
            throw new IllegalArgumentException("Pond name and timestamp cannot be null");
        }

        PondData pondData;

        // Create the pond lazily the first time we see it.
        // This avoids pre-allocating pond objects that may never be used.
        if (!ponds.containsKey(pond)) {
            pondData = new PondData();
            ponds.put(pond, pondData);
            // Keep pondCount synchronized with structural changes to the top-level map.
            pondCount++;
        } else {
            // Reuse the existing pond if it is already present.
            pondData = ponds.get(pond);
        }

        // Any successful insert or update counts as accessing the pond, so we refresh
        // its recency for future LRU eviction.
        touchPond(pondData);

        // The per-pond TreeMap is keyed by timestamp so records stay sorted naturally.
        // put() is a good choice because it inserts when absent and replaces when present,
        // and it returns the old record if one existed, which matches the required behavior.
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
        // Reuse the shared validation helper so null or missing ponds are handled consistently.
        PondData pondData = requirePond(pond);

        // We check existence first so we can throw the correct exception instead of silently
        // returning null from remove().
        if (!pondData.records.containsKey(timestamp)) {
            throw new NoSuchElementException("Record does not exist");
        }

        // Deleting is still an interaction with the pond, so we mark it as recently used.
        touchPond(pondData);

        // Remove the specific timestamped record from the pond's TreeMap.
        DaleRecord removed = pondData.records.remove(timestamp);

        // If this pond becomes empty, remove the pond itself from the top-level map.
        // This keeps the structure compact and ensures pondCount stays accurate.
        if (pondData.records.size() == 0) {
            ponds.remove(pond);
            pondCount--;
        }

        // Return the deleted record as required.
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
        // Validate that the pond exists before searching inside it.
        PondData pondData = requirePond(pond);

        // Check that the exact timestamp exists so the method throws the expected exception
        // rather than returning null.
        if (!pondData.records.containsKey(timestamp)) {
            throw new NoSuchElementException("Record does not exist");
        }

        // Reads count as access for LRU because the pond is still actively being used.
        touchPond(pondData);

        // TreeMap lookup by timestamp is efficient and preserves the intended O(log r) behavior.
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
        // Reject null pond names up front to keep input handling consistent.
        if (pondName == null) {
            throw new IllegalArgumentException("Pond name cannot be null");
        }

        // Returning an empty list for a missing pond matches the spec and avoids forcing
        // the caller to handle an exception for this particular method.
        if (!ponds.containsKey(pondName)) {
            return new ArrayList<>();
        }

        PondData pondData = ponds.get(pondName);

        // Even a full-pond read should refresh access time, because the pond was just used.
        touchPond(pondData);

        // The records are stored in a TreeMap by timestamp, so values() already comes out
        // in ascending timestamp order without any extra sorting work.
        return pondData.records.values();
    }

    /**
     * Returns the number of ponds in the database in {@code O(1)} time
     *
     * @return the number of ponds currently stored
     */
    @Override
    public int getPondCount() {
        // Return the cached count directly for constant-time performance.
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
        // Require the pond to exist since this method's contract says a missing pond is an error.
        PondData pondData = requirePond(pondName);

        // If the range is impossible, return an empty list instead of throwing, which is
        // a clean and predictable interpretation of "no values in range."
        if (start > end) {
            // Even this unsuccessful range attempt still interacted with the pond,
            // so recency is updated.
            touchPond(pondData);
            return new ArrayList<>();
        }

        // Range query counts as access for the same LRU reasons as other reads.
        touchPond(pondData);

        // Delegate to the tree's range query since the timestamps are already organized
        // in sorted order, making TreeMap the appropriate structure for this operation.
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
        // Evicting zero or a negative number of ponds is not meaningful.
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        // The method requires that at least k ponds exist, so enforce that before doing work.
        if (pondCount < k) {
            throw new NoSuchElementException("Not enough ponds to evict");
        }

        // This collects every record removed across all evicted ponds in the exact order required.
        List<DaleRecord> removedRecords = new ArrayList<>();

        // Evict one pond at a time so the returned list is ordered first by eviction order,
        // then by ascending timestamp within each pond.
        for (int i = 0; i < k; i++) {
            String lruPond = null;
            long oldestAccess = Long.MAX_VALUE;

            // Scan all ponds to find the least recently accessed one.
            // This is straightforward and works well because each pond already stores its last access time.
            // It also preserves the correct eviction ordering without needing an extra priority structure.
            for (String pond : ponds) {
                PondData pondData = ponds.get(pond);
                if (pondData.lastAccessTime < oldestAccess) {
                    oldestAccess = pondData.lastAccessTime;
                    lruPond = pond;
                }
            }

            // This defensive check should never trigger if the earlier validation passed,
            // but it guards against inconsistent internal state.
            if (lruPond == null) {
                throw new NoSuchElementException("No pond available for eviction");
            }

            // Remove the selected least-recently-used pond from the top-level structure.
            PondData evicted = ponds.remove(lruPond);
            pondCount--;

            // The pond's records are already stored by ascending timestamp because of TreeMap,
            // so values() gives them in the exact within-pond order the spec wants.
            List<DaleRecord> pondRecords = evicted.records.values();
            for (DaleRecord record : pondRecords) {
                removedRecords.add(record);
            }
        }

        // Return the flattened list of all removed records.
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
        // Require a valid existing pond before performing the analysis.
        PondData pondData = requirePond(pond);

        // This analytical read affects recency because it still uses the pond's data.
        touchPond(pondData);

        // currentDucks tracks the running number of ducks currently inside the pond.
        int currentDucks = 0;
        // peakDucks stores the maximum value currentDucks ever reaches.
        int peakDucks = 0;

        // Iterate in ascending timestamp order so entrances and exits are processed
        // in the same order they happened.
        for (DaleRecord record : pondData.records) {
            if (record instanceof DaleRecord.BoundaryEvent event) {
                // Each boundary event stores all animals involved, but only some are ducks.
                // Subtracting the known cow count gives the duck count for that event.
                int ducksAtEvent = event.names().size() - event.numCows();

                if (event.type() == DaleRecord.BoundaryEvent.TransitionType.ENTRANCE) {
                    // Entrances increase the current number of ducks in the pond.
                    currentDucks += ducksAtEvent;
                    // Update the peak whenever a new maximum is reached.
                    if (currentDucks > peakDucks) {
                        peakDucks = currentDucks;
                    }
                } else {
                    // Exits decrease the running count.
                    currentDucks -= ducksAtEvent;
                }
            }
        }

        // Return the highest occupancy observed during the scan.
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
        // Ensure the pond exists before analyzing its records.
        PondData pondData = requirePond(pond);

        // This is another pond access, so it should affect LRU recency.
        touchPond(pondData);

        // Count how many times each animal name appears across boundary events.
        // A HashMap is ideal here because we only need fast counting by name,
        // not sorted traversal while building the counts.
        HashMap<String, Integer> visitCounts = new HashMap<>();

        // Iterate through all records in chronological order.
        // The order does not actually matter for counting frequency, but the same tree traversal
        // keeps the method simple and consistent with the underlying structure.
        for (DaleRecord record : pondData.records) {
            if (record instanceof DaleRecord.BoundaryEvent event) {
                for (String name : event.names()) {
                    // getOrDefault avoids a separate containsKey check and cleanly supports
                    // the "first time seen" case.
                    int oldCount = visitCounts.getOrDefault(name, 0);
                    visitCounts.put(name, oldCount + 1);
                }
            }
        }

        // If there were no boundary events, there is no meaningful frequent visitor.
        if (visitCounts.size() == 0) {
            return null;
        }

        String bestName = null;
        int bestCount = -1;

        // Find the name with the highest count.
        // On a tie, choose the lexicographically smaller name so the result is deterministic
        // and does not depend on hash table iteration quirks.
        for (String name : visitCounts) {
            int count = visitCounts.get(name);

            if (bestName == null
                    || count > bestCount
                    || (count == bestCount && name.compareTo(bestName) < 0)) {
                bestName = name;
                bestCount = count;
            }
        }

        // Return the best candidate found.
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
        // Require that the pond already exists since this method mutates its records.
        PondData pondData = requirePond(pond);

        // Merging changes the pond and therefore definitely counts as an access.
        touchPond(pondData);

        // Buckets group all reports by duck name so each duck can be merged independently.
        HashMap<String, MergeBucket> buckets = new HashMap<>();
        // This stores the required return value: final timestamp -> timestamps merged into it.
        HashMap<Long, List<Long>> mergedInto = new HashMap<>();

        // Walk records in ascending timestamp order so merged weights are appended
        // chronologically, which matches the spec.
        for (DaleRecord record : pondData.records) {
            if (record instanceof DaleRecord.FishReport report) {
                String duck = report.duck();

                if (!buckets.containsKey(duck)) {
                    // First report for this duck, so initialize its bucket using this report
                    // as the current latest report.
                    buckets.put(duck, new MergeBucket(report.timestamp(), report.weights()));
                } else {
                    MergeBucket bucket = buckets.get(duck);

                    // The previously latest timestamp becomes an old timestamp now that
                    // a newer report for the same duck has been found.
                    bucket.oldTimestamps.add(bucket.latestTimestamp);

                    // Append the current report's weights to preserve chronological ordering
                    // across all reports for this duck.
                    for (Double weight : report.weights()) {
                        bucket.mergedWeights.add(weight);
                    }

                    // Update the bucket so it now points at the newest report seen so far.
                    bucket.latestTimestamp = report.timestamp();
                }
            }
        }

        // Apply the merged results back into the pond's tree structure.
        for (String duck : buckets) {
            MergeBucket bucket = buckets.get(duck);

            // Remove all older report timestamps for this duck so only the merged latest
            // report remains in the database.
            for (Long oldTimestamp : bucket.oldTimestamps) {
                pondData.records.remove(oldTimestamp);
            }

            // Rebuild the final fish report at the newest timestamp using the merged weights.
            // Writing the merged record back this way guarantees the pond ends in the required state.
            DaleRecord.FishReport mergedReport =
                    new DaleRecord.FishReport(pond, bucket.latestTimestamp, duck, bucket.mergedWeights);
            pondData.records.put(bucket.latestTimestamp, mergedReport);

            // Always include an entry in the returned map, even if there were no older reports.
            // That makes the output complete and consistent for every duck encountered.
            mergedInto.put(bucket.latestTimestamp, bucket.oldTimestamps);
        }

        // Return the mapping from latest timestamps to the timestamps that were absorbed into them.
        return mergedInto;
    }

    /**
     * Stores all records for one pond, keyed by timestamp.
     * Also tracks when that pond was last accessed so eviction
     * can remove the least recently used pond.
     */
    private static class PondData {
        // TreeMap is the best fit here because per-pond records need to stay sorted by timestamp,
        // support logarithmic insert/search/delete, and allow efficient range queries.
        private StaticTreeMap<Long, DaleRecord> records;
        // This field stores the last logical access time for LRU eviction decisions.
        private long lastAccessTime;

        /**
         * Constructs an empty {@code PondData} object.
         */
        PondData() {
            // Each pond gets its own timestamp-sorted tree of records.
            records = new TreeMap<>();
            // Initialize to a sentinel value before the pond has been touched.
            lastAccessTime = -1L;
        }
    }

    /**
     * Helper object used during mergeReports to gather all fish reports
     * belonging to the same duck.
     */
    private static class MergeBucket {
        // The newest timestamp seen so far for this duck.
        private long latestTimestamp;
        // All older timestamps that should be removed after merging.
        private List<Long> oldTimestamps;
        // The combined fish weights from all reports for this duck.
        private List<Double> mergedWeights;

        /**
         * Constructs a merge bucket for a duck's fish reports.
         *
         * @param latestTimestamp the timestamp of the most recent report seen so far
         * @param initialWeights the initial fish weights to copy into this bucket
         */
        MergeBucket(long latestTimestamp, List<Double> initialWeights) {
            // Start the bucket with the first report's timestamp as the current latest.
            this.latestTimestamp = latestTimestamp;
            // No old timestamps exist yet because this is the first report seen.
            this.oldTimestamps = new ArrayList<>();
            // Build a separate list for merged weights rather than aliasing the original report list.
            this.mergedWeights = new ArrayList<>();

            // Copy weights so changes inside the bucket do not accidentally modify
            // the original report's list through shared references.
            for (Double weight : initialWeights) {
                this.mergedWeights.add(weight);
            }
        }
    }
}