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
- **[Server Tools](#server-tools)**
  * **[mongoimport](#mongoimport)**
  * **[mongoexport](#mongoexport)**  
  * **[mongostat](#mongostat)**
  * **[mongotop](#mongotop)**  
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

# Server Tools

## mongoimport

## mongoexport

## mongostat

Provides statistics (e.g. number of queries/inserts/updates/deletes per second) about a running *mongos* or *mongod* instance.

```
$ mongostat 5
insert query update delete getmore command dirty  used flushes vsize  res qrw arw net_in net_out conn                time
  2129    *0     *0     *0       0     0|0  2.4% 29.7%       0 1.10G 224M 0|0 1|0   315k    108k    3 Dec 26 20:32:49.382
```

## mongotop

*mongotop* displays reading/writing statistics at a collection level. By default, stats are refreshed every second.

Displaying stats every 5 seconds while loading the employees collection:

```
$ mongotop 5
            ns    total    read    write    
mydb.employees    194ms     0ms    194ms    
...
```
