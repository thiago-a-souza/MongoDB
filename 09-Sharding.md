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
  * **[Shard Keys and Chunks](#shard-key-and-chunks)**
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


## Shard Keys and Chunks

Because the shard key determines the chunk that the data will be stored, a bad decision makes a difference between achieving a high performance or not. For example, writing only ascending shard keys will produce unbalanced chunks, overloading the last chunk and making MongoDB to split it into smaller chunks and then migrating them to other shards. Also, if the shard key is coarsed grained, it will not be possible to split the chunk and spread them across different shards because each shard key is stored in a single chunk. Finally, if the shard key is not in the query, MongoDB will request all shards to perform the operation without benefiting from a targeted query.




## Configuring a Sharded Cluster
## Queries in a Sharded Cluster
