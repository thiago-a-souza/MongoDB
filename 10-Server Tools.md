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

Imports data from JSON, CSV, or TSV into MongoDB. Although it helps importing the data, it shouldn't be used to restore backups because these formats don't preserve BSON data types. 

```
--db or -d: source database
--collection -c: source collection
--type: file type to import 
--file: input file
--fields or -f: comma separated list of field names 
--fieldFile: file storing field names
--headerline: first line represents field names
```

Importing a JSON file stored in contacts.json into the contacts collection in the users database:

```
$ mongoimport --db users --collection contacts --file contacts.json
```


Importing a CSV file requires one of the parameters *--headerline*, *--fields*, or *--fieldFile* to determine the field names.

```
// using headerline instructs MongoDB to use the first line as field names
$ mongoimport --db users --collection contacts --type csv --headerline --file contacts.csv

// if headerline is not used, the field names must be provided using --fields or --fieldFile
$ mongoimport --db mydb --collection movies --type csv --fields title,director,year --file movies.csv 
```

## mongoexport

Exports the data stored in MongoDB into a JSON or CSV file format.

```
--out or -o: file to export the data
```

Exporting the movies collection from the mydb database into the movies.json file:

```
$ mongoexport --db mydb --collection movies --out /movies.json
```

For CSV files, the field names must be specified using the option *--fields* or *--fieldFile*

```
$ mongoexport --db mydb --collection users --type csv --fields name,age --out /users.csv
```

## mongostat

Provides statistics (e.g. number of queries/inserts/updates/deletes per second) about a running *mongos* or *mongod* instance.

- **insert, query, update, delete:** number of operations per second
- **getmore:** number of get more operations (i.e. cursor batch) per second
- **command:** number of commands (e.g. create index, get indexes, etc) per second
- **qr, qw:** length of the queue of clients waiting to read/write data
- **ar, aw:** number of active clients performing reads/writes
- **res:** resident memory in MB used 


**WiredTiger only:**
- **dirty:** percentage of cache with dirty bytes
- **used:** percentage of cache in use

**MMAPv1 only:**
- **mapped:** amount of data mapped in MB
- **non-mapped:** total amount of virtual memory excluding the mapped memory
- **faults:** number of page faults per second
- **idx miss:** percentage of index accesses that required a page fault to load a btree node


```
$ mongostat 5
insert query update delete getmore command dirty  used flushes vsize  res qrw arw net_in net_out conn                time
  2129    *0     *0     *0       0     0|0  2.4% 29.7%       0 1.10G 224M 0|0 1|0   315k    108k    3 Dec 26 20:32:49.382
```

## mongotop

*mongotop* displays reading/writing statistics at a collection level. By default, stats are refreshed every second.
Mongotop cannot run against mongos.

Displaying stats every 5 seconds while loading the employees collection:

```
$ mongotop 5
            ns    total    read    write    
mydb.employees    194ms     0ms    194ms    
...
```
