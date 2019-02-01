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
  * **[Configuring a Replica Set](#configuring-a-replica-set)**
  * **[Oplog](#oplog)**
  * **[Read Preference](#read-preference)**
  * **[Write Concern](#write-concern)**  
- [Sharding](./09-Sharding.md)
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

# Replication

MongoDB provides a replication mechanism called *replica sets*, which maintains the same data across different nodes for redundancy purposes. Each *replica set* has only one primary, which receives reads and writes, and multiple read-only secondaries. When a write is performed on the primary, the data gets replicated asynchronously to secondaries, ensuring that all nodes are in sync with the primary. Having multiple copies of the same data on different machines allows the high availability needed in case nodes go down, and it can route requests appropriately. In addition to that, because the  data is available on multiple servers, reads can be load-balanced across different servers using the read preference option. Although replication improves data safety, it's important to highlight that they should not replace backup policies.

*Replica set* members perform heartbeats to each other every 2 seconds. If a member does not respond a heartbeat in 10 seconds, the node is marked as inaccessible. If there is a consensus that the primary is inaccessible, in other words, it's not a network partition, an election is invoked to determine the new primary. While the election is in progress, *replica set* members can respond to reads but writes are not allowed until the election completes successfully. In addition to that, other events, such as initiating a *replica set*, adding a new node, or calling maintenance methods (e.g. *rs.stepDown()*, *rs.reconfig()*) can also trigger an election. 

When an election is called, eligible members can vote to choose a new primary. Only members that can become primary can invoke an election, but they can vote depending on their *votes* and *state*. Although a lower priority member can become primary for a short interval, elections are invoked until the primary represents the highest priority member (default priority is equals 1). Reaching a majority in an election is tricky because it depends on total number of members in the set, not only the members that are accessible. A majority essentially means more than half of all members, so if a replica set has 5 members and 3 went down (including the primary), the other 2 nodes cannot elect a new primary because they won't reach a majority. To avoid having additional replicas, an arbiter can be configured. Arbiters cannot become a primary because they don't store any data, but they help breaking ties in elections, protecting results from network partitions.

In addition to arbiters, there are some circumstances that it's desirable to vote and hold the data but not allowing the member to become primary: 

- **Priority 0:** by setting the member priority to 0, it cannot become primary or invoke elections, but it can vote. By default, arbiters have priority 0 and primary/secondary have priority 1, but it can set to 0-1000.
- **Hidden:** this option makes the member invisible to client requests, and it's appropriate, for example, for high latency servers. When *hidden* is enabled, the ***priority* must be declared and set to 0**.  
- **Delayed:** stores delayed data (in seconds) for historical purposes. When this option is used, **the priority must be declared and set to 0**, and  *hidden* should be enabled. 

Not all *replica set* members can vote. Actually, up to 7 members can vote out of the 50 members allowed in a *replica set* - it throws an error if you try to create or modify a *replica set* with more than 7 voting members. To be eligible, it must have set the *votes* equals 1 and the *state* must be *PRIMARY, SECONDARY, STARTUP2, RECOVERING, ARBITER,* or *ROLLBACK*. Non-voting members must have the priority and votes set to zero. Similarly, it's not allowed to have priorities greater than zero without votes. Remark: *votes* allows 0 or 1.

If the primary becomes unavailable during a write operation that was not replicated to secondaries, the former primary must rollback that data when it rejoins the *replica set* to preserve the data consistency. Obviously, a rollback does not take place if the data gets replicated to another node that remains available. To prevent rollbacks, write operations can enable journaling and specify a majority write concern, so the request is only acknowledged when the data gets replicated to most nodes.

## Configuring a Replica Set

Configuring a *replica set* requires running the MongoDB daemon, *mongod*, on replication hosts. That should include the replica set name, the path where the instance stores the data, and the port it listens to requests.

After starting the daemons, the *replica sets* should be initiated. If all members have no data, the *rs.initiate* command can be executed on any node. Otherwise it must be executed on the instance that already has an initial data and the other members must be empty. After initiating the *replica set*, their members can be verified using the *rs.status* command, and it displays information about each instance from the perspective of the current member. It's also possible to verify if the current member is master using the command *db.isMaster()* or *rs.isMaster()*. Once the *replica set* is configured, it's also possible to add or remove members using the commands *rs.add* and *rs.remove*. In addition to that, it's possible to overwrite all configurations using the command *rs.reconfig*. Finally, to display the current configuration, the function *rs.config()* should be used.

Creating a *replica set* with 5 members:

```
$ mongod --replSet moonlight --dbpath /node1   --port 28001
$ mongod --replSet moonlight --dbpath /node2   --port 28002
$ mongod --replSet moonlight --dbpath /arbiter --port 28003
$ mongod --replSet moonlight --dbpath /hidden  --port 28004
$ mongod --replSet moonlight --dbpath /delayed --port 28005

$ mongo --port 28001
> cfg = {
    _id : "moonlight",
    members : [
        { _id : 1, host : "localhost:28001" },
        { _id : 2, host : "localhost:28002", priority : 2 },
        { _id : 3, host : "localhost:28003", arbiterOnly : true },
        { _id : 4, host : "localhost:28004", priority: 0, hidden : true },
        { _id : 5, host : "localhost:28005", priority: 0, hidden : true, slaveDelay: 3600 }
    ]
}

> rs.initiate(cfg)

// node 2 should become primary because it has a higher priority
$ mongo --port 28002

moonlight:PRIMARY> rs.status()
{
	...
	"members" : [
	        ...
		{
			"_id" : 2,
			"name" : "localhost:28002",
			"health" : 1,
			"state" : 1,
			"stateStr" : "PRIMARY",
			"uptime" : 246,
			"optime" : {
				"ts" : Timestamp(1539632702, 1),
				"t" : NumberLong(2)
			},
			"optimeDate" : ISODate("2018-10-15T19:45:02Z"),
			"electionTime" : Timestamp(1539632551, 1),
			"electionDate" : ISODate("2018-10-15T19:42:31Z"),
			"configVersion" : 1,
			"self" : true
		},
		{
			"_id" : 3,
			"name" : "localhost:28003",
			"health" : 1,
			"state" : 7,
			"stateStr" : "ARBITER",
			"uptime" : 175,
			"lastHeartbeat" : ISODate("2018-10-15T19:45:05.533Z"),
			"lastHeartbeatRecv" : ISODate("2018-10-15T19:45:05.347Z"),
			"pingMs" : NumberLong(0),
			"configVersion" : 1
		},
		...

// displaying the current configuration
moonlight:PRIMARY> rs.config()
{
	"_id" : "moonlight",
	"version" : 1,
	"protocolVersion" : NumberLong(1),
	"members" : [
		{
			"_id" : 1,
			"host" : "localhost:28001",
			"arbiterOnly" : false,
			"buildIndexes" : true,
			"hidden" : false,
			"priority" : 1,
			"tags" : {
				
			},
			"slaveDelay" : NumberLong(0),
			"votes" : 1
		},
                ...		
```

Adding, removing, and reconfiguring an existing *replica set*:

```
$ mongod --replSet moonlight --dbpath /node6   --port 28006

// adds a new replica set member - must be executed on the primary
moonlight:PRIMARY> rs.add( { _id : 6, host : "localhost:28006" } )

// removes a replica set member - must be executed on the primary
moonlight:PRIMARY> rs.remove("localhost:28006")

// overwrites the existing replica set configuration - must be executed on the primary
moonlight:PRIMARY> rs.reconfig( { _id : "moonlight",
                                  members : [
                                     { _id : 1, host : "localhost:28001" },
                                     { _id : 2, host : "localhost:28002", priority : 2 },
                                     { _id : 3, host : "localhost:28003", arbiterOnly : true }
                                  ] 
				} )
```

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

## Read Preference

By default, read operations are routed to the primary member of the *replica set*. However, MongoDB allows the application to specify more appropriate read preferences. This is particularly interesting to move expensive operations (e.g. analytics tasks) off the primary, so the primary can deliver faster business requests. It's important to highlight that moving reads off the primary is subject to the eventual consistency principle, in other words, reads from secondaries may not return the most recent data but eventually it will become consistent. There are several read preferences available, regardless of the option, they don't load balance requests across nodes, and *sharding* should be used to distribute the workload uniformly.



- **primary:** default option, reads are performed on the primary, if the primary is unavailable it throws an error
- **primaryPreferred:** if the primary is not available, it reads from a secondary
- **secondary:** reads from a secondary, if no secondaries are unavailable it throws an error
- **secondaryPreferred:** if secondaries are not available, it reads from the primary
- **nearest:** reads from the nearest member in terms of network latency - useful if the client is not on the same data center and can tolerate eventual consistency.

```
// From Mongo Shell, to read from a secondary it's required to run the function *rs.slaveOk()*, otherwise it throws an error
> rs.slaveOk()

// fine-grained readPref method
> db.people.find().readPref("secondary")

> db.people.find().readPref("primaryPreferred")
```


## Write Concern

Rollbacks occur when the primary becomes unavailable before a write operation propagates to any secondary node. To prevent rollbacks and ensure that the operation is cluster-wide durable, MongoDB allows specifying the acknowledgment level for the write operation. There are several alternatives to get acknowledgements, including a specified number of members, the majority of the members, or instances with a given tag. The write concern also enables confirming that the operation has been written to the journal on the instances specified, and a timeout to complete the operation.

Before version 2.6, the write concern was specified using the  *db.getLastError()* function. After that, the write concern was integrated as an option into write operations.

- **w option:** 
	- **w:0** requests no acknowledgment, but if *j:true* it ignores that and requests acknowledgment from the primary
        - **w:1** default, requests acknowledgment from primary member
	- **w:N** for N > 0, it requests acknowledgment from the primary and other members, including secondaries, hidden, delayed, and priority 0 members
	- **w:"majority"** requests acknowledgment from them majority - it implies in *j:true*

- **j option:** when *j:true*, it returns after the number the specified in *w*, including the primary, have written to the journal.

- **wtimeout:** applicable when *w* is greater than 1, and sets a time limit (in ms) for the write concern.


```
myApp:PRIMARY> db.example.drop()

// gettting an ack from the majority of the members
myApp:PRIMARY> db.example.insertOne( { _id : 1, "name" : "john", "age" : 20 }, 
...                                  { writeConcern : { w : "majority", j : true, wtimeout : 1000 } } )
{ "acknowledged" : true, "insertedId" : 1 }

// gettting an ack from 2 members
myApp:PRIMARY> db.example.insertOne( { _id : 2, "name" : "peter", "age" : 25 },
...                                  { writeConcern : { w : 2, j : true } } )
{ "acknowledged" : true, "insertedId" : 2 }

// gettting an ack from 3 members
myApp:PRIMARY> db.example.insertOne( { _id : 3, "name" : "alex", "age" : 36},
...                                  { writeConcern : { w : 3, j : true } } )
{ "acknowledged" : true, "insertedId" : 3 }

// fire and forget: w = 0 and j = false does not get any acknowledgment that the change was applied
myApp:PRIMARY> db.example.updateOne( { _id : 1 }, { $inc : { age : 1 } }, 
...                                  { writeConcern : { w : 0, j : false} } )
{ "acknowledged" : false }

myApp:PRIMARY> db.example.deleteMany( {_id : { $gte : 2 } }, 
...                                   { writeConcern : { w : "majority", j : true } } )
{ "acknowledged" : true, "deletedCount" : 2 }
```

				  
				  



