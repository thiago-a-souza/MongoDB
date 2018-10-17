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
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

# Sharding

Sharding is the method of partitioning the data into chunks based on a *shard key*. Because each chunk has a lower and upper ranges based on the *shard key*, different chunks cannot have the same data. This process enables using commodity hardware to scale out the application rather than increasing the capacity of a single server (a.k.a. vertical scaling). Building a *sharded cluster* requires careful planning and should be used with very large data sets that could not be managed without partitioning.

