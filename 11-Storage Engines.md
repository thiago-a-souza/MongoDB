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


- **Document Level Concurrency:** multiple users can modify different documents at the same time
- **Snapshots and Checkpoints:** to allow multiple readers while a write is in progress, MongoDB implements MultiVersion Concurrency Control (MVCC). As a result, each user sees a different snapshot of the data from a particular point in time. WiredTiger performs writes all the data in a snapshot to disk in a consistent way, that represents a new checkpoint, which can be used for recovery purposes. By default, snapshots are written to disk to create a new snapshot every 60s. 



## MMAPv1
