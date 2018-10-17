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
- **[Sharding](#sharding)**
  * **[Chunks](#chunks)**
  * **[Shard Keys](#shard-keys)**
  * **[Configuring a Sharded Cluster](#configuring-a-sharded-cluster)**
  * **[Queries in a Sharded Cluster](#queries-in-a-sharded-cluster)**  
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

# Sharding

Sharding is the method of partitioning the data into chunks based on a *shard key*. Because each chunk has a lower and upper ranges based on the *shard key*, different chunks cannot have the same data. This process enables using commodity hardware to scale out the application rather than increasing the capacity of a single server (a.k.a. vertical scaling). Building a *sharded cluster* requires careful planning and should be used with very large data sets that could not be managed without partitioning. 

A *sharded cluster*, illustrated in Figure 3, comprises three components:


- **Shard:** starting in version 3.6, shards must be configured as *mongod* replica set to store partitions.
- **mongos:** *mongos* caches config servers' metadata and routes reads/writes to the appropriate shard. As a result, it represents the front door to client requests. Because *mongos* processes are lightweight, they can be deployed together with the application server to deliver a higher throughput.
- **Config servers:** they are *mongod* processes that store the cluster metadata, including databases/collections locations, the list of chunks on every shard and their ranges, etc. Because config servers must have the same data, it uses a two-phase commit to ensure the consistency needed.


<p align="center">
<img src="./fig/sharded-cluster.png"  height="60%" width="60%"> <br>
<b> Figure 3: </b> Sharded Cluster Components  <a href="./README.md#references">(4)</a> </p>

## Chunks

A chunk contains a subset of the data based on the shard key. When an insert or update makes the chunk exceed the default  size of 64 Mb, it triggers a split process to divide it into smaller chunks, and the balancer migrates them across shards. It's possible to modify the chunk size, but the frequency of migrations and the impact on queries executed should be monitored.

## Shard Keys

Because the shard key determines the chunk that the data will be stored, a bad decision makes a difference between achieving a high performance or not. For example, writing only ascending shard keys will produce unbalanced chunks, overloading the last chunk and making MongoDB to split it into smaller chunks and then migrating them to other shards. Alternatively, if the shard key is coarsed grained, it will not be possible to split the chunk and spread them across different shards because each shard key must be stored in a single chunk. In addition to that, if the shard key is not in the query, MongoDB will request all shards to perform the operation without benefiting from a targeted query. 

Choosing a good shard key can be complicated because it has to ensure that reads target appropriate shards, writes are balanced across shards, and splits/migrations can occur appropriately. To evaluate a shard key, investigate how the  cardinality, frequency, and rate of change can influence its efficiency. Cardinality refers to the number of distinct shard keys, meaning that a high cardinality shard key does not guarantee an even distribution of data across shards, but allows scaling out. Frequency describes how often a shard key is present, and high frequency shard keys should be avoided because they cannot be partitioned. Finally, the rate of change defines if the shard key increases or decreases monotonically, not distributing the data evenly.

All sharded collections must have an index on the shard key or a compound index with the shard key as prefix. If the collection is empty, the function *sh.shardCollection()*  creates the index on the shard key if it does not exist, otherwise, if the collection is loaded, the index must be created before running the *sh.shardCollection()*. It's important to note that sharded collections only allow unique indexes on the *_id* and on the shard key, because allowing on other fields would require an inter-shard communication.




## Configuring a Sharded Cluster
## Queries in a Sharded Cluster
