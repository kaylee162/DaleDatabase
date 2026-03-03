import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import refactor.StaticTreeMap;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TreeMapStudentTests {
    private static final int TIMEOUT = 200;
    private StaticTreeMap<Integer, String> map;

    @Before
    public void setup() {
        map = Main.getTreeMapInstance();
    }

    @Test(timeout = TIMEOUT)
    public void testAddRightRotation() {
        /*
                    5,A                   4,B
                    /                    /   \
                  4,B         ->       3,C   5,A
                  /
                3,C
         */

        assertNull(map.put(5, "A"));
        assertNull(map.put(4, "B"));
        assertNull(map.put(3, "C"));

        assertEquals(3, map.size());
        assertEquals(1, map.height());
    }

    @Test(timeout = TIMEOUT)
    public void testAddRightLeftRotationRoot() {
        /*
                3,A               4,C
                  \              /   \
                  5,B     ->    3,A  5,B
                  /
                4,C
         */

        assertNull(map.put(3, "A"));
        assertNull(map.put(5, "B"));
        assertNull(map.put(4, "C"));
        assertEquals("C", map.put(4, "A"));

        assertEquals(3, map.size());
        assertEquals(1, map.height());

    }

    @Test(timeout = TIMEOUT)
    public void testRemove() {
        /*
                   646,A                     646,A
                  /     \                   /     \
                477,B  856,C      ->      386,D  856,C
               /     \                      \
            386,D   526,E                  526,E
         */
        String toBeRemoved = "B";
        map.put(646, "A");
        map.put(477, toBeRemoved);
        map.put(856, "C");
        map.put(386, "D");
        map.put(526, "E");

        assertSame(toBeRemoved, map.remove(477));
        assertEquals(4, map.size());
        assertEquals(2, map.height());
    }

    @Test(timeout = TIMEOUT)
    public void testGet() {
       /*
                 477,A
                /     \
             386,C   526,B
                         \
                        646,D
        */
        String maximum = "D";
        map.put(477, "A");
        map.put(526, "B");
        map.put(386, "C");
        map.put(646,  maximum);

        assertSame(maximum, map.get(646));
    }

    @Test(timeout = TIMEOUT)
    public void testGetRange() {
       /*
                 477,A
                /     \
             386,C   526,B
                         \
                        646,D
        */
        map.put(477, "A");
        map.put(526, "B");
        map.put(386, "C");
        map.put(646,  "D");

        assertEquals(List.of("C", "A", "B"), map.getRange(Integer.MIN_VALUE, 526));
    }

    @Test(timeout = TIMEOUT)
    public void testContainsKey() {
       /*
                 477,A
                /     \
             386,C   526,B
                         \
                        646,D
        */
        map.put(477, "A");
        map.put(526, "B");
        map.put(386, "C");
        map.put(646, "D");

        assertTrue(map.containsKey(477));
        assertTrue(map.containsKey(386));
        assertTrue(map.containsKey(646));
        assertFalse(map.containsKey(387));
        assertFalse(map.containsKey(700));
        assertFalse(map.containsKey(500));
    }

    @Test(timeout = TIMEOUT)
    public void testKeySet() {
       /*
                 477,A
                /     \
             386,C   526,B
                         \
                        646,D
        */
        map.put(477, "A");
        map.put(526, "B");
        map.put(386, "C");
        map.put(646, "D");

        assertEquals(Set.of(386, 477, 526, 646), map.keySet());
    }

    @Test(timeout = TIMEOUT)
    public void testValues() {
       /*
                 477,A
                /     \
             386,C   526,B
                         \
                        646,D
        */
        map.put(477, "A");
        map.put(526, "B");
        map.put(386, "C");
        map.put(646, "D");

        assertEquals(List.of("C", "A", "B", "D"), map.values());
    }

    @Test(timeout = TIMEOUT)
    public void testClear() {
        map.clear();
        assertEquals(0, map.size());
        assertEquals(-1, map.height());
    }

    @Test(timeout = TIMEOUT)
    public void testIterator() {
       /*
                 477,A
                /     \
             386,C   526,B
                         \
                        646,D
        */
        map.put(477, "A");
        map.put(526, "B");
        map.put(386, "C");
        map.put(646, "D");
        Iterator<String> iterator = map.iterator();

        assertEquals("C", iterator.next());
        assertEquals("A", iterator.next());
        assertEquals("B", iterator.next());
        assertEquals("D", iterator.next());
        assertFalse(iterator.hasNext());
    }
}
