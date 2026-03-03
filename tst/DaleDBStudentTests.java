import apply.DaleRecord;
import apply.DaleRecord.BoundaryEvent.TransitionType;
import apply.StaticDaleDB;
import implement.HashMap;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DaleDBStudentTests {
    
    private static final int TIMEOUT = 200;
    private StaticDaleDB daleDB;
    private DaleRecord[] records;

    @Before
    public void setup() {
        daleDB = Main.getDaleDBInstance();
        records = new DaleRecord[]{
            new DaleRecord.BoundaryEvent("B", 0L, TransitionType.ENTRANCE, List.of("Andy"), 0),
            new DaleRecord.BoundaryEvent("A", 1L, TransitionType.ENTRANCE, List.of("Andy", "Mike", "Ham"), 0),
            new DaleRecord.BoundaryEvent("A", 2L, TransitionType.EXIT, List.of("Andy"), 0),
            new DaleRecord.BoundaryEvent("A", 3L, TransitionType.ENTRANCE, List.of("Jack", "Andy", "Lee"), 0),
            new DaleRecord.BoundaryEvent("A", 4L, TransitionType.EXIT, List.of("Andy"), 0),
            new DaleRecord.BoundaryEvent("A", 5L, TransitionType.EXIT, List.of("Mike"), 0),
            new DaleRecord.BoundaryEvent("A", 6L, TransitionType.EXIT, List.of("Lee"), 0),
            new DaleRecord.BoundaryEvent("A", 7L, TransitionType.ENTRANCE, List.of("Luke", "Can"), 0),
            new DaleRecord.BoundaryEvent("C", 2L, TransitionType.ENTRANCE, List.of("Luke", "Can"), 0),
            new DaleRecord.FishReport("C", 5L, "Luke", List.of(0.4, 0.5, 0.6)),
            new DaleRecord.FishReport("C", 4L, "Luke", List.of(0.1, 0.2, 0.3)),
            new DaleRecord.FishReport("C", 3L, "Can", List.of(0.1, 0.2, 0.3)),
            new DaleRecord.BoundaryEvent("A", 6L, TransitionType.EXIT, List.of("Can"), 0),
        };
    }

    public void basicSetup() {
        for (DaleRecord record : records) daleDB.putRecord(record);
    }

    @Test(timeout = TIMEOUT)
    public void testStandardOperations1() {
        DaleRecord r = records[0];
        assertNull(daleDB.putRecord(r));
        assertEquals(r, daleDB.getRecord(r.pond(), r.timestamp()));
        assertEquals(List.of(r), daleDB.getPond(r.pond()));
        assertEquals(r, daleDB.deleteRecord(r.pond(), r.timestamp()));
        assertEquals(List.of(), daleDB.getPond(r.pond()));
    }

    @Test(timeout = TIMEOUT)
    public void testStandardOperations2() {
        assertNull(daleDB.putRecord(records[2]));
        assertNull(daleDB.putRecord(records[1]));
        assertEquals(List.of(records[1], records[2]), daleDB.getPond(records[1].pond()));
        assertEquals(List.of(records[1]), daleDB.getRecordRange(records[1].pond(), 0L, 1L));
        assertEquals(List.of(records[2]), daleDB.getRecordRange(records[1].pond(), 2L, 3L));
        assertEquals(List.of(records[1], records[2]), daleDB.getRecordRange(records[1].pond(), 0L, 3L));
    }

    @Test(timeout = TIMEOUT)
    public void testEvict() {
        basicSetup();
        assertEquals(List.of(records[0]), daleDB.evict(1));
    }

    @Test(timeout = TIMEOUT)
    public void testGetPeakConcurrentOccupancy() {
        basicSetup();
        assertEquals(5, daleDB.getPeakConcurrentOccupancy("A"));
    }

    @Test(timeout = TIMEOUT)
    public void testGetMostFrequentVisitor() {
        basicSetup();
        assertEquals("Andy", daleDB.getMostFrequentVisitor("A"));
    }

    @Test(timeout = TIMEOUT)
    public void testMergeReports() {
        basicSetup();
        HashMap<Long, List<Long>> temp = daleDB.mergeReports("C");
        assertEquals(2, temp.size());
        assertEquals(Set.of(5L, 3L), temp.keySet());
        assertEquals(List.of(4L), temp.get(5L));
        assertEquals(List.of(), temp.get(3L));

        assertEquals(3, daleDB.getPond("C").size());
        assertThrows(NoSuchElementException.class, () -> daleDB.getRecord("C", 4L));
        assertEquals(List.of(0.1, 0.2, 0.3, 0.4, 0.5, 0.6),
                ((DaleRecord.FishReport) daleDB.getRecord("C", 5L)).weights());
    }

}
