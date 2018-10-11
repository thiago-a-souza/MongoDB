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
- **[Replication](#replication)**
- [Sharding](./09-Sharding.md)
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

# Replication

MongoDB provides a replication mechanism called *replica sets*, which maintains the same data across different nodes for redundancy purposes. Each *replica set* has only one primary, that receives reads and writes, and multiple read-only secondaries. When a write is performed on the primary, the data gets replicated asynchronously to secondaries, ensuring that all nodes are in sync with the primary. Having multiple copies of the same on different machines allows the high availability needed in case one node goes down, and it can route requests to another server automatically. In addition to that, because the  data is available on multiple nodes, reads can be load-balanced across different servers using the read preference option. 

