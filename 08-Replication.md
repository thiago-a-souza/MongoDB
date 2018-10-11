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

A *replica set* can have up to 50 members, and each one perform heartbeats to each other every 2 seconds. If a member does not respond the heartbeat in 10 seconds, the node is marked as inaccessible. If the primary is inaccessible, an election is invoked to determine the new primary. While the election is in progress, *replica set* members can respond to reads but writes are not allowed until the election completes successfully. In addition to that, other events can also trigger an election, such as initiating a *replica set*, adding a new node, or calling maintenance methods (e.g. *rs.stepDown()*, *rs.reconfig()*). 


When an election is called, eligible members can vote to elect a new primary. Although a lower priority member can become primary for a short interval, elections are invoked until the primary represents the highest priority member. Reaching a majority in an election is tricky because it depends on the number of nodes that go down. To avoid having additional replicas, an arbiter can be configured. Arbiters cannot become a primary because they don't store any data, but they participate in elections breaking ties.

In addition to arbiters, there are some circumstances that it's desirable to vote and hold the data but not allowing the member to become primary. 

- **Priority 0:** by setting the member priority to 0, it cannot become primary or invoke an election
- **Hidden:** this option makes the member invisible to client requests, which makes it appropriate for high latency servers. When *hidden* is enabled, the *priority* must be set to 0.  
- **Delayed:** stores delayed data for historical purposes. When this option is used, the priority must be set to 0, and  *hidden* should be enabled. 
