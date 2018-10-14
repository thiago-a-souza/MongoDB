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
  * **[Oplog](#oplog)**
- [Sharding](./09-Sharding.md)
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

# Replication

MongoDB provides a replication mechanism called *replica sets*, which maintains the same data across different nodes for redundancy purposes. Each *replica set* has only one primary, which receives reads and writes, and multiple read-only secondaries. When a write is performed on the primary, the data gets replicated asynchronously to secondaries, ensuring that all nodes are in sync with the primary. Having multiple copies of the same data on different machines allows the high availability needed in case nodes go down, and it can route requests appropriately. In addition to that, because the  data is available on multiple servers, reads can be load-balanced across different servers using the read preference option. Although replication improves data safety, it's important to highlight that they should not replace backup policies.

*Replica set* members perform heartbeats to each other every 2 seconds. If a member does not respond a heartbeat in 10 seconds, the node is marked as inaccessible. If there is a consensus that the primary is inaccessible, in other words, it's not a network partition, an election is invoked to determine the new primary. While the election is in progress, *replica set* members can respond to reads but writes are not allowed until the election completes successfully. In addition to that, other events, such as initiating a *replica set*, adding a new node, or calling maintenance methods (e.g. *rs.stepDown()*, *rs.reconfig()*) can also trigger an election. 

When an election is called, eligible members can vote to choose a new primary. Although a lower priority member can become primary for a short interval, elections are invoked until the primary represents the highest priority member. Reaching a majority in an election is tricky because it depends on the number of nodes that go down. To avoid having additional replicas, an arbiter can be configured. Arbiters cannot become a primary because they don't store any data, but they help breaking ties    in elections, protecting results from network partitions.

In addition to arbiters, there are some circumstances that it's desirable to vote and hold the data but not allowing the member to become primary: 

- **Priority 0:** by setting the member priority to 0, it cannot become primary or invoke elections, but it can vote
- **Hidden:** this option makes the member invisible to client requests, and it's appropriate, for example, for high latency servers. When *hidden* is enabled, the *priority* must be set to 0.  
- **Delayed:** stores delayed data for historical purposes. When this option is used, the priority must be set to 0, and  *hidden* should be enabled. 

Not all *replica set* members can vote. Actually, up to 7 members can vote out of the 50 members allowed in a *replica set*. To be eligible, it must have set the *votes* greater than 0 and the *state* must be *PRIMARY, SECONDARY, STARTUP2, RECOVERING, ARBITER,* or *ROLLBACK*. Non-voting members must have the priority and votes set to zero. Similarly, it's not allowed to have priorities greater than zero without votes.

If the primary becomes unavailable during a write operation that was not replicated to secondaries, the former primary must rollback that data when it rejoins the *replica set* to preserve the data consistency. Obviously, a rollback does not take place if the data gets replicated to another node that remains available. To prevent rollbacks, write operations can enable journaling and specify a majority write concern, so the request is only acknowledged when the data gets replicated to most nodes.

## Oplog

After applying a write operation to the primary node, MongoDB records that information in a capped collection 
known as *oplog* (operations log), which is stored in the *local* database as *oplog.rs*. And then, secondaries query primary's *oplog* for records newer than their latest *oplog* records. After that, they apply these modifications and  write them to their own *oplogs*. Each write operation produces a new *oplog* entry, including operations that modify multiple documents generate multiple *oplog* entries. To ensure that secondaries end up with the same data as the primary, *oplog* records must be idempotent, in other words, no matter how many times that particular operation is applied it will produce the same result.

The following example illustrates how *oplog* works. First, two documents are inserted into the *example* collection, and then the *age* field is incremented. We can find the corresponding *oplog* entries in the *oplog.rs* collection. Even though two commands were executed, they affected four documents, so four *oplog* entries were produced. Also, notice that the *$inc* operation was replaced with *$set* with expected value to preserve the idempotency.

```
myApp:PRIMARY> use mydb
switched to db mydb

myApp:PRIMARY> db.example.drop()
false
myApp:PRIMARY> db.example.insertMany([ {"_id" : 1, "name" : "john", "age" : 20 },
                                       {"_id" : 2, "name" : "peter", "age" : 25 }
                                     ])

myApp:PRIMARY> db.example.updateMany({}, {$inc : { age : 1 }})

myApp:PRIMARY> use local
switched to db local

myApp:PRIMARY> db.oplog.rs.find({ns:/mydb.example/}).sort({wall:1}).pretty()                
{
	"ts" : Timestamp(1539553750, 2),
	"t" : NumberLong(9),
	"h" : NumberLong("-8362812390414468398"),
	"v" : 2,
	"op" : "i",
	"ns" : "mydb.example",
	"ui" : UUID("2d5364c7-b17f-458b-914a-6c29b8babe55"),
	"wall" : ISODate("2018-10-14T21:49:10.754Z"),
	"o" : {
		"_id" : 1,
		"name" : "john",
		"age" : 20
	}
}
{
	"ts" : Timestamp(1539553750, 3),
	"t" : NumberLong(9),
	"h" : NumberLong("4953900032754086238"),
	"v" : 2,
	"op" : "i",
	"ns" : "mydb.example",
	"ui" : UUID("2d5364c7-b17f-458b-914a-6c29b8babe55"),
	"wall" : ISODate("2018-10-14T21:49:10.754Z"),
	"o" : {
		"_id" : 2,
		"name" : "peter",
		"age" : 25
	}
}
{
	"ts" : Timestamp(1539553806, 2),
	"t" : NumberLong(9),
	"h" : NumberLong("-7218810790864236062"),
	"v" : 2,
	"op" : "u",
	"ns" : "mydb.example",
	"ui" : UUID("2d5364c7-b17f-458b-914a-6c29b8babe55"),
	"o2" : {
		"_id" : 1
	},
	"wall" : ISODate("2018-10-14T21:50:06.914Z"),
	"o" : {
		"$v" : 1,
		"$set" : {
			"age" : 21
		}
	}
}
{
	"ts" : Timestamp(1539553806, 3),
	"t" : NumberLong(9),
	"h" : NumberLong("-4662535812237866725"),
	"v" : 2,
	"op" : "u",
	"ns" : "mydb.example",
	"ui" : UUID("2d5364c7-b17f-458b-914a-6c29b8babe55"),
	"o2" : {
		"_id" : 2
	},
	"wall" : ISODate("2018-10-14T21:50:06.914Z"),
	"o" : {
		"$v" : 1,
		"$set" : {
			"age" : 26
		}
	}
}
```


