# DaleDB – Pond Activity Database

DaleDB is a Java-based database designed to store and analyze time-ordered activity records from ponds. The system supports efficient record insertion, deletion, range queries, pond eviction using an LRU policy, and analytical queries about pond activity.

The project was built to practice implementing and using core data structures such as hash maps and tree maps while handling structured time-series data.

---

## Features

- Store records for multiple ponds
- Insert, retrieve, and delete records efficiently
- Range queries on timestamps
- Least Recently Used (LRU) pond eviction
- Merge fish reports for each duck
- Analyze pond activity including:
  - Peak concurrent duck occupancy
  - Most frequent pond visitors
- Built using custom data structures rather than Java's standard collections

---

## Project Structure

```

apply/
└─ DaleDB.java            # Main database implementation

implement/
└─ HashMap.java           # Custom hash map implementation

refactor/
├─ TreeMap.java           # Tree-based map implementation
└─ StaticTreeMap.java     # TreeMap interface

records/
└─ DaleRecord.java        # Record definitions (BoundaryEvent, FishReport)

tests/
└─ DaleDBStudentTests.java

````

---

## Record Types

### BoundaryEvent
Represents animals entering or exiting a pond.

Each event stores:
- Timestamp
- Animal names involved
- Number of cows detected
- Transition type (ENTRANCE or EXIT)

From this data the system can determine the number of ducks present.

---

### FishReport
Represents a fishing event recorded for a duck.

Each report stores:
- Pond name
- Timestamp
- Duck identifier
- List of fish weights caught

Multiple reports for a duck can be merged into a single consolidated record.

---

## Core Operations

### Insert Record
```java
daleDB.putRecord(record);
````

Adds or updates a record at a specific timestamp.

---

### Retrieve Record

```java
daleDB.getRecord("PondA", 42L);
```

Returns the record stored at the given timestamp.

---

### Delete Record

```java
daleDB.deleteRecord("PondA", 42L);
```

Removes the record from the pond.

---

### Range Query

```java
daleDB.getRecordRange("PondA", 10L, 50L);
```

Returns all records within a timestamp interval.

---

### Pond Retrieval

```java
daleDB.getPond("PondA");
```

Returns all records in a pond sorted by timestamp.

---

## Eviction

The database supports removing the **k least recently accessed ponds**.

```java
daleDB.evict(k);
```

Eviction order is determined by an internal logical clock tracking pond access.

---

## Analytics

### Peak Duck Occupancy

```java
daleDB.getPeakConcurrentOccupancy("PondA");
```

Computes the maximum number of ducks simultaneously present in a pond using boundary events.

---

### Most Frequent Visitor

```java
daleDB.getMostFrequentVisitor("PondA");
```

Returns the animal name that appears most often in boundary events.

---

## Fish Report Merging

```java
daleDB.mergeReports("PondA");
```

For each duck:

* All fish reports are merged into the newest timestamp
* Older reports are removed
* Fish weight lists are concatenated in timestamp order

The method returns a map of:

```
latestTimestamp -> list of merged timestamps
```

---

## Complexity

| Operation     | Time Complexity |
| ------------- | --------------- |
| Insert Record | O(log r)        |
| Delete Record | O(log r)        |
| Get Record    | O(log r)        |
| Range Query   | O(log r + k)    |
| Get Pond      | O(r)            |
| Evict         | O(kp)           |
| Merge Reports | O(r)            |

Where:

* `r` = records in a pond
* `p` = number of ponds
* `k` = eviction count

---

## Technologies

* Java
* Custom HashMap implementation
* Custom TreeMap implementation
* JUnit testing

---

## Purpose

This project focuses on applying data structures to a realistic database-style system, combining:

* time-series data management
* tree-based indexing
* hash-based lookup
* memory management via eviction
* data aggregation and analysis

---

## Author

Kaylee Henry  
Georgia Institute of Technology  
Computer Science  
