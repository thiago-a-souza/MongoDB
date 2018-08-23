# Author
Thiago Alexandre Domingues de Souza

# NoSQL

The relational database model, introduced by E.F. Codd in 1970, is considered one of the greatest breakthroughs in computer history. This model has a strong mathematical foundation based on the set theory to describe the relationship between tables. To certify that these relations are properly designed, the relational model provides the normalization process, which ensures the data consistency and eliminates redundancy. As a result, the data can be retrieved from a single table or joined with multiple tables using the well-known standard query language (SQL). 

Database systems based on the relational model support transactions. One or more database operations, such as reading, deleting, updating, or inserting data, are combined into a single unit called transaction. Combining statements into a single transaction allows running multiple changes, and then at the end of the transaction, either apply (i.e. commit) or revert (i.e. rollback) all of them. Relational database management systems (RDBMS) follow ACID transactions:

- **Atomic:** basically means "all or nothing"; a transaction is completely applied, or nothing is performed.

- **Consistent:** ensures that when a transaction completes, successfully or not, the database will be in a valid state. In other words, all data written to the database must valid according to the rules defined, including constraints, column types, triggers, etc.

- **Isolated:** implies that a transaction does not interfere with another one. Database locks, such as row or table locks, 
is an example of isolation. For instance, if two transactions try to modify the same row(s), one will run and the other will wait or fail - depending how long the first transaction takes to complete.

- **Durable:** once a change is committed, they are permanent and will not be lost.

These properties guarantee the reliability needed for traditional applications, and explains, among other factors, why relational databases have dominated the market. However, over the past years, a colossal number of devices have been connected to the internet, emerging new applications that store non-conventional data (e.g. photos, videos, posts, etc). The relational model was neither appropriate nor fast enough to solve these problems. Typical applications using relational databases require a high computing power because they involve complex joins to pull data from different sources. When the system has to respond to millions of users running operations on huge databases, using the relational model becomes a bottleneck. To solve this problem, several non-relational databases were created, and the term NoSQL was coined to refer to them.

NoSQL databases take a completely opposite direction compared to traditional databases in favor of performance. Instead of having the data in a single place using the normalization process, NoSQL databases denormalize them, so they are available in different places wherever needed. This approach may reduce the data consistency because values will be stored in different places and also requires more disk space, but eliminates the need for computationally expensive joins. Also, in contrast to ACID principles from relational databases, NoSQL databases follow a different approach, defined by the BASE acronym:

- **Basically Available:** implies that the system continues to respond, successfully or not, even in case of failures. 
- **Eventually Consistent:** unlike the consistency defined in ACID transactions, which ensures that the database will be in a valid state (without any invalid data), the consistency in BASE systems means that the most recent data may not be available for a period of time but eventually it will be replicated in a consistent state.
- **Soft State:** indicates that the state of the system may change even without any input for eventual consistency.

At a first glance, it may seem odd that NoSQL databases are *eventually consistent*. However, for some applications having the most recent data is not critical. For example, it's not really important that a post on a social network is not displayed immediately to all followers; it's ok that eventually they will be consistent. In fact, some NoSQL databases, like Cassandra, provide a *tuneable consistency*, which means that the level of consistency required can be configured.
