# Author
Thiago Alexandre Domingues de Souza

# NoSQL

The relational database model, introduced by E.F. Codd in 1970, is considered one of the greatest breakthroughs in computer history. This model has a strong mathematical foundation based on the set theory to describe the relationship between tables. To certify that these relations are properly designed, the relational model provides the normalization process, which ensures the data consistency and eliminates redundancy. As a result, the data can be retrieved from a single table or joined with multiple tables using the well-known standard query language (SQL). 

Database systems based on the relational model support transactions. One or more database operations, such as reading, deleting, updating, or inserting data, are combined into a single unit called transaction. Combining statements into a single transaction allows running multiple changes, and then at the end of the transaction, either apply (i.e. commit) or revert (i.e. rollback) all of them. Relational database management systems (RDBMS) follow ACID transactions:

- **Atomic:** basically means "all or nothing"; a transaction is completely applied, or nothing is performed.

- **Consistent:** ensures that when a transaction completes, successfully or not, the database will be in a valid state. In other words, all data written to the database must valid according to the rules defined, including constraints, column types, triggers, etc.

- **Isolated:** implies that a transaction does not interfere with one another. Database locks, such as row or table locks, 
is an example of isolation. For instance, if two transactions try to modify the same row(s), one will run and the other will wait or fail - depending how long the first transaction takes to complete.

- **Durable:** once a change is committed, they are permanent and will not be lost.

These properties guarantee the reliability needed for traditional applications, and explains, among other factors, why relational databases have dominated the market. However, over the past years, a colossal number of devices have been connected to the internet, emerging new applications that store non-conventional data (e.g. photos, videos, posts, etc). The relational model was neither appropriate nor fast enough to solve these problems. Typical applications using relational databases require a high computing power because they involve complex joins to pull data from different sources. When the system has to respond to millions of users running operations on huge databases, using the relational model becomes a bottleneck. To solve this problem, several non-relational databases were created, and the term NoSQL was coined to refer to them.

NoSQL databases take a completely opposite direction compared to traditional databases in favor of performance. Instead of having the data in a single place using the normalization process, NoSQL databases denormalize them, so they are available in different places wherever needed. This approach may reduce the data consistency because values will be stored in different places and also requires more disk space, but eliminates the need for computationally expensive joins. Also, in contrast to ACID principles from relational databases, NoSQL databases follow a different approach, defined by the BASE acronym:

- **Basically Available:** implies that the system continues to respond, successfully or not, even in case of failures. 
- **Eventually Consistent:** unlike the consistency defined in ACID transactions, which ensures that the database will be in a valid state (without any invalid data), the consistency in BASE systems mean that the most recent data may not be available for a period of time but eventually it will be replicated in a consistent state.
- **Soft State:** indicates that the state of the system may change even without any input for eventual consistency.

At a first glance, it may seem odd that NoSQL databases are *eventually consistent*. However, for some applications having the most recent data is not critical. For example, it's not really important that a post on a social network is not displayed immediately to all followers; it's ok that eventually they will be consistent. In fact, some NoSQL databases provide levels of consistency and availability, which can be configured according to the requirements. 

In 2000, Eric Brewer outlined the CAP theorem, describing three desirable properties in distributed systems: consistency (the same data is be available to all requests), availability (all read/write requests are responded with low latency), and partition tolerance (the system can be split into different nodes and work in case of network failures). The theorem states that it's not possible to guarantee all three properties at the same time. Later, it was demonstrated that only two of these properties can be satisfied simultaneously. This is why NoSQL databases have to choose availability and partition tolerance over to consistency. On the other hand, consistency and availability are not negotiable in relational databases. The primary goal of some popular databases in terms of the CAP theorem is illustrated in Figure 1.

<p align="center">
<img src="https://github.com/thiago-a-souza/tmp/blob/master/fig/cap.png"  height="30%" width="30%"> <br>
  <b>Figure 1:</b> CAP theorem 
</p> 


There are many NoSQL databases to solve a wide range of problems. They are often divided into four major categories: 


| TYPE | DESCRIPTION | EXAMPLE |
|:--------------------|:------------|:--------|
|  Key-value stores <img width=300/>    |  It's the simplest NoSQL database model. Data is stored in key-value pairs, similar to a hash map.  It's useful for caching purposes.| Redis |
|  Document store | One of the most popular NoSQL database types. Store the data as a collection of documents, and it's usually specified in a JSON format.  | MongoDB, CouchDB |
|  Column wide store  |  It uses tables, rows, and columns, but their operation is not similar to relational databases, for example, the set of columns can vary from a row to another. It's very popular in data-intensive applications like video-streaming, analytics, etc. | Cassandra, HBase |
|  Graph database    | Designed to support vertexes and edges to represent graphs. It can be used to model the relationship of friends in a social network. | Neo4J |


Contrary to relational databases, which provide standard features across different vendors, NoSQL databases were created to solve specific problems that were not appropriate for the relational model. In consequence, NoSQL databases are different from each other in several aspects, including how the data is stored and accessed. However, the following  characteristics are frequently present in NoSQL databases.


- **Horizontal Scalability:** there are two types of scalability: horizontal (i.e. scale out) and vertical (i.e. scale up). Distributed systems that are horizontally scalable expand their performance by adding more computing nodes. On the other hand, vertical scalable systems require replacing the existing hardware to increase their performance. Unlike RDBMS, NoSQL databases were designed to scale out using commodity hardware. 

- **Highly distributable:** scaling out allows NoSQL databases to store the data on multiple nodes, rather than keeping huge datasets in a single location.

- **Schema-free:** relational databases require data types to be defined up-front, and all rows of the same column must have the same type. Conversely, NoSQL databases don't require schemas, so the data structure doesn't need to be defined ahead of time and can evolve over time, giving more control to developers to change what will be stored.

- **Denormalization:** the normalization process enhances the data consistency by not duplicating the same data on multiple tables, but it comes at a cost. Joining tables requires high processing power, which impacts performance.

- **No Joins:** NoSQL databases don't support joins as it severely impacts performance. In consequence, the data must be denormalized, making queries faster and easier to implement.

- **No Foreign Keys:** because there are no joins and the data should be denormalized, foreign keys are not available in NoSQL databases.

- **Query-based design:** designing a database application using the relational model involves describing entities, relationships, constraints, etc. Once created, queries can be designed based on the data model implemented. By contrast, modeling a NoSQL application takes the opposite direction. Queries should be designed first and then the data around them. As a result, the underlying structure will store the data required for targeted queries.




