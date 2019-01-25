# Author

Thiago Alexandre Domingues de Souza

# Table of Contents

- [NoSQL](./01-NoSQL.md)
- [MongoDB](./02-MongoDB.md)
- [Mongo Shell](./03-Mongo%20Shell.md)
- [CRUD](./04-CRUD.md)
- [Indexes](./05-Indexes.md)     
- [Data Modeling](./06-Data%20Modeling.md)
- [Aggregation](./07-Aggregation.md)
- [Replication](./08-Replication.md)
- [Sharding](./09-Sharding.md)
- [Server Tools](./10-Server%20Tools.md)
- **[Storage Engines](#storage-engines)**
- [References](./README.md#references)

# Storage Engines

The storage engine manages how the data is read/written to disk. Because the storage engine is a layer between the hardware and the database, it does not affect the syntax of the queries. However, it does influence their performance because each storage engine has its own data structures, which are suitable for different purposes. As a result, MongoDB takes advantage of pluggable storage engines to allow the user to decide what's the appropriate solution. Starting in version 3.2, WiredTiger is the default storage engine. Before that, MongoDB used MMAPv1, which is deprecated as of version 4.0.

## WiredTiger

- **B-trees:** data and indexes are stored in B-trees.
- **Document Level Concurrency:** multiple users can modify different documents of the same collection at the same time
- **Snapshots and Checkpoints:** to allow multiple readers while a write is in progress, MongoDB implements MultiVersion Concurrency Control (MVCC). As a result, each user sees a different snapshot of the data from a particular point in time. WiredTiger performs writes all the data in a snapshot to disk in a consistent way, that represents a new checkpoint, which can be used for recovery purposes. By default, snapshots are written to disk to create a new snapshot every 60s. WiredTiger can recover from failures that happened up to the last checkpoint even if journaling is not enabled, but if the failure happened after the last checkpoint journaling should be active.
- **Journal:** combining checkpoints with journaling ensures data durability in the event of failures. WiredTiger journal is compressed using snappy.
- **Compression:** by default, WiredTiger compresses collections, indexes, and the journal using the snappy library to reduce the space used. The default compression library can be configured as well as individual settings for collection and indexes.
- **Memory:** WiredTiger uses an internal cache and the filesystem cache. During a checkpoint, the data is sent from the internal cache to the filesystem cache and then it's written to disk. The data in the filesystem cache is the same as the on-disk format, including the compression. Indexes and the collection data in the WiredTiger internal cache have a different representation to the on-disk format, and indexes are compressed, while the collection data is not.


```
// explicitly defining WiredTiger the storage engine
mongod --port 2801 --storageEngine wiredTiger --dbpath /data/wt1

// wiredTiger is the default storage engine as of version 3.2
mongod --port 2802 --dbpath /data/wt2

// defining MMAPv1 storage engine
mongod --port 2803 --storageEngine mmapv1 --dbpath /data/mmapv1

// error: directory already contains WiredTiger data files
mongod --port 2804 --storageEngine mmapv1 --dbpath /data/wt1

// error: directory already contains MMAPv1 data files 
mongod --port 2805 --storageEngine wiredTiger --dbpath /data/mmapv1

// error: directory already contains MMAPv1 data files 
mongod --port 2806 --dbpath /data/mmapv1
```


## MMAPv1

- **Mapping:** maps data files into virtual memory, making the operating system responsible for most of the work of the storage engine.
- **Collection Level Concurrency:** as of version 3.0, concurrent users can modify different collections of the same database, so it's not possible multiple writers modifying different documents of the same collection. Before that version, it  MMAPv1 used a database level locking.
- **Journal:** journaling is enabled by default to ensure that changes are durable, all modifications are written to an on-disk journal every 100ms and then flushed from the journal to the data files every 60s.
- **Record Allocation:** all documents are stored in a contiguous memory region, meaning that when they exceed their allocated size, they are moved to another region and their indexes are updated to reflect the new address. To minimize movements and fragmentation, MongoDB uses a power of 2 bytes strategy (e.g. 32, 64, 128, 256, 512, ..., documents larger than 2 MB are rounded up to the nearest multiple of 2 MB) with padding to allow the document to grow. For collections whose document sizes  don't grow, padding can be disabled to reduce the data files.
- **Memory:** it uses all free memory as its cache, but it can release memory to other processes.
- **Data Files:** data files double their size until it reaches 2Gb, from there it allocates 2Gb for new data files.


