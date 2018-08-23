# Author
Thiago Alexandre Domingues de Souza

# NoSQL

The relational database model, introduced by E.F. Codd in 1970, is considered one of the greatest breakthroughs in computer history. This model has a strong mathematical foundation based on the set theory to describe the relationship between tables. To certify that these relations are properly designed, the relational model introduced a concept known as normalization, which ensures the data consistency and eliminates redundancy. As a result, the data can be retrieved from a single table or joined with multiple tables using the well-known standard query language (SQL). 

Database systems based on the relational model support transactions. One or more database operations, such as reading, deleting, updating, or inserting data, are combined into a single unit called transaction. Combining statements into a single transaction allows running multiple changes, and then at the end of the transaction, either apply (i.e. commit) or revert (i.e. rollback) all of them. Relational database management systems (RDBMS) follow ACID transactions:

- **A**tomic: basically means "all or nothing"; a transaction is completely applied or nothing is performed.

- **C**onsistent: ensures that when a transaction completes, successfully or not, the database will be in a valid state. In other words, all data written to the database must valid according to the rules defined, including constraints, column types, triggers, etc

- **I**solated: implies that a transaction does not interfere with another one. Database locks, such as row or table locks, 
is an example of isolation. For instance, if two transactions try to modify the same row(s), one will run and the other will wait or fail - depending how long the first transaction takes to complete.

- **D**urable: once a change is committed, they are permanent and will not be lost.

These properties guarantee the reliability needed for traditional applications, and explains, among other factors, why relational databases have dominated the market. However, over the past years, a tremendous amount of devices have been connected to the internet, emerging new applications that store non-conventional data (e.g. photos, videos, posts, etc). The relational model was neither appropriate nor fast to solve these problems. Typical applications using relational databases require a high computing power because they involve complex joins to pull data from different tables. When the system has respond to millions of users running operations on huge databases, using the relational model becomes a problem.



