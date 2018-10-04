# Author

Thiago Alexandre Domingues de Souza

# Table of Contents

- [NoSQL](./1-NoSQL.md)
- **[MongoDB](#mongodb)**
- [Mongo Shell](./3-Mongo%20Shell.md)
- [CRUD](./4-CRUD.md)
- [Indexes](./5-Indexes.md)     
- [Data Modeling](./6-Data%20Modeling.md)    


# MongoDB

In 2007, a startup called 10gen started working on a PaaS to host web applications. They realized that the existing database platforms were not appropriate for the web in terms of scalability, availability, partitioning, and flexibility to handle complex data structures. And with that, they started building their own NoSQL database: MongoDB. To accomplish these goals, they had to compromise several aspects from the traditional relational model (e.g. transactions, joins, normalization, etc) to create their document database.

Document-oriented databases store and process semi-structured datasets in XML or JSON formats, which are not constrained by a predefined schema. JavaScript Object Notation (JSON) has been widely adopted because it can describe complex data structures, using a key-value pair syntax, in a human readable layout. Because JSON is language-independent, it's commonly used to interact with heterogeneous environments. Also, JSON's schemaless nature allows documents to have different fields, making them easier to evolve over time. These factors explain why MongoDB uses a JSON-based format called BSON - data is encoded in a binary format for performance and storage purposes.  

MongoDB organizes the data into **databases, collections, documents, and fields**. In the relational model, documents are equivalent to rows, fields correspond to columns, collections (a group of documents) are similar to tables, and databases (a group of collections) are similar to Oracle schemas. Unlike relational databases, which require objects created explicitly before using them,  MongoDB creates databases and collections implicitly when the document is first loaded.


In order to deliver high availability and redundancy, MongoDB provides the *replica set* mechanism. A *replica set* consists of *mongod* processes running on different nodes with identical copies of the data. Each *replica set* maintains exactly one primary node and multiple secondary nodes. The primary node can respond to read and write requests, whereas secondaries support read-only operations. When a write operation is requested, the primary applies the change and it gets replicated to secondaries. By default, read operations are also routed to the primary, but read preferences can be modified using the *readPref* function. In case the primary goes down, the secondaries will elect a new primary. As a consequence, an odd number of replicas is recommended to increase the chances of a majority. 

MongoDB is continuously  evolving to accommodate new features. Initially, transactions were supported only at the document level, in other words, transactions were guaranteed only within fields of the same document. Starting in version 4.0, multi-document transactions are also supported (at a low performance cost). In terms of durability, before version 2.0 journaling was not enabled, so it could not ensure by default that changes were permanent after a server crash, and the application would have to  deal with it. In fact, MongoDB allows the user to choose the trade-off between speed and durability, denoted as *write concern* [(1)](#references), by setting the number of replicas that should acknowledge write operations. More consistent durability levels reduce speed and vice versa. Starting in version 3.4, the default configuration requests that at least the primary acknowledges write operations to the journal.

Replica set and sharding are completely different processes. A replica set contains the very same data across different nodes for availability and redundancy purposes. On the other hand, sharding or partitioning splits the data across multiple nodes, so every node has its own subset of the entire dataset. As a result, the database can scale out when a single node cannot fulfill the performance demand. MongoDB supports auto-sharding, distributing and load-balancing the data across different nodes automatically. Consequently, it provides high throughputs even on very large datasets.
