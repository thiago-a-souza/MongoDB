# Author
Thiago Alexandre Domingues de Souza

# Table of Contents


- [NoSQL](#nosql)
- [MongoDB](#mongodb)
- [Mongo Shell](#mongo-shell)
- [CRUD](#crud)
  * [Create](#create)
     * [insertOne](#insertone)
     * [insertMany](#insertmany)
     * [save](#save)     
  * [Read](#read)
     * [Count](#count)
     * [Comparison operators](#comparison-operators)
     * [Logical operators](#logical-operators)
     * [Element operators](#element-operators)     
     * [Array operators](#array-operators)     
     * [Cursors](#cursors)          
  * [Update](#update)
     * [updateOne](#updateone)  
     * [updateMany](#updatemany)  
     * [Update Operators](#update-operators)  
  * [Delete](#delete)  

# NoSQL

The relational database model, introduced by E.F. Codd in 1970, is considered one of the greatest breakthroughs in computer history. This model has a strong mathematical foundation based on the set theory to describe the relationship between tables. To certify that these relations are properly designed, the relational model provides the normalization process, which ensures the data consistency and eliminates redundancy. As a result, the data can be retrieved from a single table or joined with multiple tables using the well-known standard query language (SQL). 

Database systems based on the relational model support transactions. One or more database operations, such as reading, deleting, updating, or inserting data, are combined into a single unit called transaction. Combining statements into a single transaction allows running multiple changes, and then at the end of the transaction, either apply (i.e. commit) or revert (i.e. rollback) all of them. Relational database management systems (RDBMS) must support these properties, defined as ACID transactions:

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
|  Column wide store  |  It uses tables, rows, and columns, but their operation is not similar to relational databases, for example, the set of columns can vary from a row to another. It's very popular in data-intensive applications like video-streaming. | Cassandra, HBase |
|  Graph database    | Designed to support vertexes and edges to represent graphs. It can be used to model the relationship of friends in a social network. | Neo4J |


Contrary to relational databases, which provide standard features across different vendors, NoSQL databases were created to solve specific problems that were not appropriate for the relational model. In consequence, NoSQL databases are different from each other in several aspects, including how the data is stored and accessed. However, the following  characteristics are frequently present in NoSQL databases.


- **Horizontal Scalability:** there are two types of scalability: horizontal (i.e. scale out) and vertical (i.e. scale up). Distributed systems that are horizontally scalable expand their performance by adding more computing nodes. On the other hand, vertical scalable systems require replacing the existing hardware to increase their performance. Unlike RDBMS, NoSQL databases were designed to scale out using commodity hardware. 

- **Highly distributable:** scaling out allows NoSQL databases to store the data on multiple nodes, rather than keeping huge datasets in a single location.

- **Schema-free:** relational databases require data types to be defined up-front, and all rows of the same column must have the same type. Conversely, NoSQL databases don't require schemas, so the data structure doesn't need to be predefined and can evolve over time, giving more control to developers to change what will be stored.

- **Denormalization:** the normalization process enhances the data consistency by not duplicating the same data on multiple tables, but it comes at a cost. Joining tables requires high processing power, which impacts performance. For this reason, the data is denormalized in NoSQL databases.

- **No Joins:** NoSQL databases don't support joins as it severely impacts performance. In consequence, the data must be denormalized, making queries faster and easier to implement.

- **No Foreign Keys:** because there are no joins and the data should be denormalized, foreign keys are not available in NoSQL databases.

- **Query-based design:** designing a database application using the relational model involves describing entities, relationships, constraints, etc. Once created, queries can be designed based on the data model implemented. By contrast, modeling a NoSQL application takes the opposite direction. Queries should be designed first and then the data around them. As a result, the underlying structure will store the data required for targeted queries.

Despite the recent popularity of NoSQL databases, they are not replacing traditional relational databases. In fact, some applications that could benefit from the NoSQL model would still choose RDBMS because they have been used for decades, and people are more familiar with this solution. However, they have different purposes and each approach has pros and cons. RDBMS are best suited for applications that demand ACID transactions, non-negotiable data consistency, and strict schemas. On the other hand, NoSQL databases are recommended for applications handling huge volumes of data that require high performance, real-time responses, and non critical levels of data consistency.


# MongoDB

In 2007, a startup called 10gen started working on a PaaS to host web applications. They realized that the existing database platforms were not appropriate for the web in terms of scalability, availability, partitioning, and flexibility to handle complex data structures. And with that, they started building their own NoSQL database: MongoDB. To accomplish these goals, they had to compromise several aspects from the traditional relational model (e.g. transactions, joins, normalization, etc) to create their document database.

Document-oriented databases store and process semi-structured datasets in XML or JSON formats, which are not constrained by a predefined schema. JavaScript Object Notation (JSON) has been widely adopted because it can describe complex data structures, using a key-value pair syntax, in a human readable layout. Because JSON is language-independent, it's commonly used to interact with heterogeneous environments. Also, JSON's schemaless nature allows documents to have different fields, making them easier to evolve over time. These factors explain why MongoDB uses a JSON-based format called BSON - data is encoded in a binary format for performance and storage purposes.  

MongoDB organizes the data into **databases, collections, documents, and fields**. In the relational model, documents are equivalent to rows, fields correspond to columns, collections (a group of documents) are similar to tables, and databases (a group of collections) are similar to Oracle schemas. Unlike relational databases, which require objects created explicitly before using them,  MongoDB creates databases and collections implicitly when the document is first loaded.


In order to deliver high availability and redundancy, MongoDB provides the *replica set* mechanism. A *replica set* consists of *mongod* processes running on different nodes with identical copies of the data. Each *replica set* maintains exactly one primary node and multiple secondary nodes. The primary node can respond to read and write requests, whereas secondaries support read-only operations. When a write operation is requested, the primary applies the change and it gets replicated to secondaries. By default, read operations are also routed to the primary, but read preferences can be modified using the *readPref* function. In case the primary goes down, the secondaries will elect a new primary. As a consequence, an odd number of replicas is recommended to increase the chances of a majority. 

MongoDB is continuously  evolving to accommodate new features. Initially, transactions were supported only at the document level, in other words, transactions were guaranteed only within fields of the same document. Starting in version 4.0, multi-document transactions are also supported (at a low performance cost). In terms of durability, before version 2.0 journaling was not enabled, so it could not ensure by default that changes were permanent after a server crash, and the application would have to  deal with it. In fact, MongoDB allows the user to choose the trade-off between speed and durability, denoted as *write concern* [(1)](#references), by setting the number of replicas that should acknowledge write operations. More consistent durability levels reduce speed and vice versa. Starting in version 3.4, the default configuration requests that at least the primary acknowledges write operations to the journal.

Replica set and sharding are completely different processes. A replica set contains the very same data across different nodes for availability and redundancy purposes. On the other hand, sharding or partitioning splits the data across multiple nodes, so every node has its own subset of the entire dataset. As a result, the database can scale out when a single node cannot fulfill the performance demand. MongoDB supports auto-sharding, distributing and load-balancing the data across different nodes automatically. Consequently, it provides high throughputs even on very large datasets.

# Mongo Shell

MongoDB comes with mongo shell, a CLI for managing and interacting with the data. Because mongo shell is a Javascript-based tool, it also allows running any Javascript code. The shell is a process called *mongo* that connects to a *mongod* instance, representing the MongoDB server, and assigns the current database to the variable *db* - this variable is updated whenever the database is switched.


```
// display all databases
> show databases
admin        0.000GB
local        0.947GB

// switch to a database - creates the database if it does not exist
> use mydb
switched to db mydb

// show the current database
> db
mydb

// insert a document into a collection - creates the collection if it does not exist
> db.books.insertOne({"title" : "Robinson Crusoe", "author" : "Daniel Defoe" })
{
	"acknowledged" : true,
	"insertedId" : ObjectId("5b9191312c544c23552a793d")
}

// display all collections 
> show collections
books

// javascript operations like loops are supported
> for(i=1; i<=1000000; i++){
... db.testing.insertOne({"name" : "test-" + i })
... }

// display shell related commands
> help
	show dbs                     show database names
	show collections             show collections in current database
	show users                   show users in current database
	show profile                 show most recent system.profile entries with time >= 1ms
	show logs                    show the accessible logger names
	...

// typing the function without parentheses displays the function code 
> db.testing.drop
function () {
    if (arguments.length > 0)
        throw Error("drop takes no argument");
    var ret = this._db.runCommand({drop: this.getName()});
    if (!ret.ok) {
        if (ret.errmsg == "ns not found")
            return false;
        throw _getErrorWithCode(ret, "drop failed: " + tojson(ret));
    }
    return true;
}
```

# CRUD

In contrast to relational databases, MongoDB does not support SQL to perform CRUD operations. Instead, it provides APIs to popular programming languages that can access the database and execute queries. These functions take documents as parameters, including the data, filters, and other options. Depending on the programming language used, additional boilerplate code is required to perform these operations. The commands described in this document refers to Javascript syntax used by  Mongo shell. For examples in other programming languages, visit the documentation at [(2)](#references).

## Create

Prior to version 3.2, inserting one or multiple documents into a collection was performed using the same *insert* function. This is still allowed for backward compatibility, but more appropriate functions were introduced, according to the number of documents loaded: *insertOne* and *insertMany*.

If the document loaded does not specify the unique *_id* field, MongoDB creates automatically an *ObjectId*. This field represents the document's primary key, in other words, there are no duplicate documents with the same *_id* in the collection.

### insertOne()

For obvious reasons, this function does not allow inserting multiple documents.


**Syntax:**

```
db.collection.insertOne(<document>, { writeConcern: <document> })
```

**Examples:**

```
> db.books.insertOne({"title" : "1984", "author" : "George Orwell" } )

> doc = { "title" : "Fahrenheit 451" , "author" : "Ray Bradburry" }
> db.books.insertOne(doc)
```

### insertMany

Function loads an array of documents. It allows inserting a single document, but it must be in an array. By default,  documents are inserted in the sequence provided. Setting the *ordered* parameter to *false* allows MongoDB reorder inserts for an enhanced load. In case *insertMany* produces an error, it stops where the failure occurred if *ordered* is set to true (default), otherwise it continues trying to load all documents if it's set to false.

**Syntax:**

```
db.collection.insertMany( [ <document 1> , ... ], { writeConcern: <document>, ordered: <boolean> } )
```

**Examples:**

```
> db.books.insertMany([
    {"title" : "The Old Man and The Sea", "author" : "Ernest Hemingway"},
    {"title" : "Great Expectations", "author" : "Charles Dickens"} ])
    
> arr = [{"title" : "The Great Gatsby", "author" : "F. Scott Fitzgerald"}, 
         {"title" : "A Study in Scarlet", "author" : "Arthur Conan Doyle"}]
> db.books.insertMany(arr)

// ordered is set to true by default; it stops where it fails
> db.test.insertMany([{ "_id" : 1 }, { "_id" : 1 }, { "_id" : 2 } ])
> db.test.find()
{ "_id" : 1 }

// ordered set to false; try to load remaining documents
> db.test.insertMany([{ "_id" : 10 }, { "_id" : 10 }, { "_id" : 20 } ], { ordered : false } )
> db.test.find()
{ "_id" : 1 }
{ "_id" : 10 }
{ "_id" : 20 }
```

### save

Inserts one or more documents if the *_id* is not provided, otherwise it replaces existing documents by running an *update* with the *upsert* parameter set to true.

**Syntax:**

```
db.collection.save(<document>, { writeConcern: <document> })
```

**Examples:**

```
> db.books.save({"title" : "Leonardo da Vinci"})

> db.books.find({"title" : "Leonardo da Vinci"})
{ "_id" : ObjectId("5b9c11f6d44947754a8d4370"), "title" : "Leonardo da Vinci" }

// existing document is replaced
> db.books.find({ "author" : "Walter Isaacson"})
{ "_id" : ObjectId("5b9c11f6d44947754a8d4370"), "author" : "Walter Isaacson" }
```


## Read

There are two popular alternatives to access the data: *find* and *findOne*. Both have the same arguments, which are optional, but *find* returns a cursor object, whereas *findOne* returns a single document. 

**Syntax:**

```
db.collection.find(query, projection)
```

**Loading sample dataset:**

```
> load("/path/to/laureates.js")
```

**Examples:**

- **Returning all documents:** use *find* without any arguments

```
// display all documents in a pretty format
> db.laureates.find().pretty()
{
	"_id" : ObjectId("5b9fb4c353cfac900ac29129"),
	"firstname" : "Wilhelm Conrad",
	"surname" : "Röntgen",
	"born" : "1845-03-27",
	"died" : "1923-02-10",
	"bornCountry" : "Prussia (now Germany)",
	"bornCountryCode" : "DE",
	"bornCity" : "Lennep (now Remscheid)",
	"diedCountry" : "Germany",
	"diedCountryCode" : "DE",
	"diedCity" : "Munich",
	"gender" : "male",
	"prizes" : [
		{
			"year" : 1901,
			"category" : "physics",
			"share" : 1,
			"motivation" : "in recognition of the extraordinary services he has rendered by the discovery of the remarkable rays subsequently named after him",
			"affiliations" : [
				{
					"name" : "Munich University",
					"city" : "Munich",
					"country" : "Germany"
				}
			]
		}
	]
}
... 
```

- **Returning a single document:** use either *findOne* or *find* restricting the number of documents with *limit*

```
// display only one document
> db.laureates.findOne()
> db.laureates.find().limit(1)
```

- **Showing only specific fields:** the projection argument defines fields that should be displayed by providing the field name as the key and 1 as the value. By default, the *_id* is displayed, and to hide the value should be set to 0.

```
// display the first name and the id for all documents (first argument should be empty or null)
> db.laureates.find({}, {"firstname" : 1})
{ "_id" : ObjectId("5b9fb4c353cfac900ac29129"), "firstname" : "Wilhelm Conrad" }
{ "_id" : ObjectId("5b9fb4c353cfac900ac2912a"), "firstname" : "Hendrik Antoon" }
...

// display only the first name for all documents (first argument should be empty or null)
> db.laureates.find(null, {"firstname" : 1, "_id" : 0 })
{ "firstname" : "Wilhelm Conrad" }
{ "firstname" : "Hendrik Antoon" }
...
```

- **Documents that match a criteria:** the query parameter specifies comma separated conditions that perform an implicit AND operation. In case a given field has to be specified multiple times, the *$and* operator should be used instead.

```
// display laureates that were born in France
> db.laureates.find({"bornCountry" : "France"})

// display laureates that were born in France AND died in USA
> db.laureates.find({"bornCountry" : "France", "diedCountry" : "USA"} )
```


### Count

There are two *count* functions: *collection.count* and *cursor.count*. The first one has a syntax similar to *find* and also can execute without arguments, while the second doesn't take a query. Executing the function without arguments returns approximate results based on the collection's metadata.

```
// syntaxes
// db.collection.count(query, options)
// cursor.count()

// operations are equivalent
> db.laureates.find().count()
> db.laureates.count()

// syntax accepts a query
db.laureates.count({ "bornCountry" : "Canada" })
```

### Distinct

Returns *distinct* values for a field passed as a string parameter and returns an array.

```
// syntax - field is a string
// db.collection.distinct(field, query, options)

// distinct values by bornCountry
> db.laureates.distinct("bornCountry")

// distinct categories awarded to women
> db.laureates.distinct("prizes.category", { "gender" : "female"})
```



### Comparison operators

By default, queries match documents using the equality operator, but it also supports explicit comparison operators such as  ***$eq, $gt, $gte, $lt, $lte, $ne, $in,*** and  ***$nin***.

**Remarks:**  *$in* and *$nin* must be in an array; *$nin* matches values that are not in the array, even if the field does not exist.

```
// list prizes awarded after year 2000
> db.laureates.find({"prizes.year" : { $gt : 2000 } })

// comparison operators also applies to strings
> db.laureates.find({"firstname" : { $lt : "B" }})

// list prizes awarded between years 2000 and 2005
> db.laureates.find({"prizes.year" : { $gt : 2000, $lt : 2005 } })

// laureates that were born in Italy or Belgium; $in requires an array
> db.laureates.find({"bornCountry" : { $in : ["Italy", "Belgium"] } })

// matches all documents because the field does not exist and it was used the $nin operator
> db.laureates.find({"fieldDoesNotExist" : { $nin : ["whatever"] } })
```

### Logical operators

The logical operators ***$and, $not, $nor,*** and ***$or*** are performed on an array of expressions. The *$and* operator allows specifying multiple constraints on the same field.

```
// laureates who were born in Egypt or died in Australia
> db.laureates.find({ $or : [ { "bornCountry" : "Egypt"}, { "diedCountry" : "Australia"}] })

// laureates who were awared in physics AND chemistry
> db.laureates.find({ $and : [ { "prizes.category" : "physics"}, { "prizes.category" : "chemistry"}] })

// Multiple constraints on the same field with an implicit AND evaluates only the last one
> db.laureates.find({ "prizes.category" : "physics", "prizes.category" : "chemistry"}).count()
177
// Query above is equivalent to selecting only chemistry winners
> db.laureates.find({ "prizes.category" : "chemistry"}).count()
177
```



### Element operators

- ***$exists*** returns documents that contains (or not) a specific field
- ***$type*** selects documents that have a field with a given data type

```
// laureates that don't have the field born
db.laureates.find({born : {$exists : false }})

// documents where the year is an int
db.laureates.find({ "prizes.year" : { $type : "int" }})

// documents where the prizes field is an array
db.laureates.find({ "prizes" : { $type : "array" }})
```

### Array operators

- ***$all*** returns documents where all values match values stored in the array provided, regardless the order.
- ***$size*** returns documents where the field is an array with a given size. Multiple criterias on arrays are evaluated separately, and it can return incorrect documents. To prevent this side effect, 
- ***$elemMatch*** forces multiple criterias to be evaluated together and return documents that has at least one item that match them.

```
db.example.insertMany([{"fruits" : ["orange", "apple"]},
                       {"fruits" : ["orange", "apple", "lemon"]},
                       {"fruits" : ["apple", "orange"]},
                       {"fruits" : ["strawberry", "apple", "avocado", "orange"]},
                       {"fruits" : ["grape", "pear", "apple"]}
                      ]);
// because it's a scalar comparison, it will match documents that have an orange
> db.example.find({"fruits" : "orange"}).count()
4

// because it's an array comparison, it will match documents with the very same array
> db.example.find({"fruits" : ["orange", "apple"]}).count()
1

// match documents that have both orange and apple, regardless the order
> db.example.find({"fruits" : {$all : ["orange", "apple"]}}).count()
4

// find documents where the array fruits has two elements
> db.example.find({"fruits" : { $size : 2 }}).count()
2

// collection to demonstrate $elemMatch
db.test.insertMany([
{"years" : [2000, 2005, 2020]},
{"years" : [1990, 2006]},
])

// returns the document with years 2000, 2005, 2020 because each criteria is evaluated separately
> db.test.find({ "years" : { $gt : 2005, $lt : 2020}})
{ "_id" : ObjectId("5ba0026453cfac900ac294d0"), "years" : [ 2000, 2005, 2020 ] }
{ "_id" : ObjectId("5ba0026453cfac900ac294d1"), "years" : [ 1990, 2006 ] }

// only the document with the year 2006 match both criterias
> db.test.find({ "years" : { $elemMatch : { $gt : 2005, $lt : 2020}}})
{ "_id" : ObjectId("5ba0026453cfac900ac294d1"), "years" : [ 1990, 2006 ] }
```

### Cursors

The *find* command returns a cursor of objects, and it's up to the API to handle the results. Mongo shell automatically displays the first 20 documents, and the ***it*** command displays the next 20 results. However, if the command is not executed in mongo shell, the items should be iterated manually to display them.

To iterate and manipulate the data, the cursor object provides several methods. They are efficient because the *find* command takes into account cursor methods before sending the request to the database, rather than submitting the request and then running cursor commands. Cursor methods that return another cursor (e.g. skip, limit, sort) can be chained together, regardless the order the outcome will be the same.

```
// looping through the cursor result
> var cur = db.laureates.find({ "prizes.category" : "physics" }, {"_id" : 0, "firstname" : 1 });
> while(cur.hasNext()){
    printjson(cur.next())
  }
```

- ***cursor.count()*** returns the number of documents referenced by the cursor. Unlike the *collection.count*, the *cursor.count* doesn't accept a query parameter.

```
> db.laureates.find().count()
```

- ***cursor.limit()*** set a max limit on the number of documents returned.

```
> db.laureates.find().limit(3)
```

- ***cursor.skip()*** skip a specified number of results; useful for pagination results.

```
// skip 10 results
> db.laureates.find().skip(10)

// skip 10 results and displays only 10 results
> db.laureates.find().skip(10).limit(10)
// chaining cursor functions return the same result
> db.laureates.find().limit(10).skip(10)
```

- ***cursor.sort()*** take as argument a key/value pair representing the field and the direction, respectively, in ascending (1) or descending (-1) order.

```
// sort laureates by their firstname in ascending order
> db.laureates.find().sort({"firstname" : 1})

// sort results by year in descending order and category in ascending order
> db.laureates.find().sort({"prizes.year" : -1, "prizes.category" : 1})

// chaining skip, limit, and sort returns the same result regardless the order
> db.laureates.find().limit(5).skip(5).sort({"firstname" : 1})
> db.laureates.find().skip(5).sort({"firstname" : 1}).limit(5)
> db.laureates.find().sort({"firstname" : 1}).limit(5).skip(5)
```


## Update

There are three update functions: *update*, *updateOne*, and *updateMany*. The first method, *update*, can replace the entire document using a key/value pair syntax as the update parameter or modify specific fields through update operators (e.g. *$set*, *$inc*, *$rename*, etc). By default, *update* affects a single document, but it allows updating multiple documents if update operators  are used. The other update functions, *updateOne* and *updateMany*, have a similar syntax, and allows only update operators, throwing an error if the key/value pair syntax is used.

### update

```
// syntax
// db.collection.update(query, update, options)

> db.people.insertMany([ {"name" : "john", "age" : 25 },
                         {"name" : "peter", "age" : 36 },
                         {"name" : "alex", "age" : 36 }
                       ])
> db.people.find()
{ "_id" : ObjectId("5ba286f753cfac900ac294ee"), "name" : "john", "age" : 25 }
{ "_id" : ObjectId("5ba286f753cfac900ac294ef"), "name" : "peter", "age" : 36 }
{ "_id" : ObjectId("5ba286f753cfac900ac294f0"), "name" : "alex", "age" : 36 }

// replaces all fields, except the _id
> db.people.update({"name" : "john"}, {"gender" : "male"})
> db.people.find()
{ "_id" : ObjectId("5ba286f753cfac900ac294ee"), "gender" : "male" }
{ "_id" : ObjectId("5ba286f753cfac900ac294ef"), "name" : "peter", "age" : 36 }
{ "_id" : ObjectId("5ba286f753cfac900ac294f0"), "name" : "alex", "age" : 36 }

// it cannot replace multiple documents
> db.people.update({"age" : "36"}, {"gender" : "male"})
WriteResult({ "nMatched" : 0, "nUpserted" : 0, "nModified" : 0 })

// modifying only specific fields with $set
> db.people.update({"_id" : ObjectId("5ba286f753cfac900ac294ee")}, { $set : {"name" : "john", "age" : 25}})
> db.people.find({"_id" : ObjectId("5ba286f753cfac900ac294ee")})
{ "_id" : ObjectId("5ba286f753cfac900ac294ee"), "gender" : "male", "name" : "john", "age" : 25 }
```

### updateOne

```
// syntax
// db.collection.updateOne(filter, update, options)

// document before update
> db.people.find({"name" : "john"})
{ "_id" : ObjectId("5ba286f753cfac900ac294ee"), "gender" : "male", "name" : "john", "age" : 25 }

// incrementing the age by 1
> db.people.updateOne({"name" : "john"}, {$inc : { "age" : 1 }})

// document after update
> db.people.find({"name" : "john"})
{ "_id" : ObjectId("5ba286f753cfac900ac294ee"), "gender" : "male", "name" : "john", "age" : 26 }
```

### updateMany

```
// syntax
// db.collection.updateMany(filter, update, options)

> db.people.updateMany({"age" : { $gte : 30 , $lt : 40 }}, { $set : { "group" : "thirties"}})

> db.people.find()
{ "_id" : ObjectId("5ba286f753cfac900ac294ee"), "gender" : "male", "name" : "john", "age" : 26 }
{ "_id" : ObjectId("5ba286f753cfac900ac294ef"), "name" : "peter", "age" : 36, "group" : "thirties" }
{ "_id" : ObjectId("5ba286f753cfac900ac294f0"), "name" : "alex", "age" : 36, "group" : "thirties" }
```

### Update Operators


- ***$set*** modifies a field 
- ***$unset*** removes a field
- ***$rename*** renames a field
- ***$inc*** increments by a specified number
- ***$mul*** multiplies by a specified number
- ***$min*** **and** ***$max*** updates only if the field is less/greater than min/max
- ***$setOnInsert*** when the upsert option is enabled, it sets values in case of inserts

```
> db.users.insertOne(
  {
  "_id" : 1,
  "name" : "bob",
  "address" : { "street" : "1000 5th Ave", "city" : "New York", "state" : "ny" },      
  "age" : 30,
  "limit" : 1000.00,
  "score" : 50
  }
 )

// modifying and adding field
> db.users.updateOne({"_id" : 1}, {$set : {"name" : "Bob", "favcolor" : "green"}})
> db.users.find({"_id": 1}, {"_id" : 0, "name" : 1, "favcolor" : 1})
{ "name" : "Bob", "favcolor" : "green" }

// removing field
> db.users.updateOne({"_id" : 1}, {$unset : {"favcolor" : ""}})

// renaming field
> db.users.updateOne({"_id" : 1}, {$rename : {"address" : "addr"}})
> db.users.find({"_id": 1}, {"_id" : 0, "name" : 1, "addr" : 1})
{ "name" : "Bob", "addr" : { "street" : "1000 5th Ave", "city" : "New York", "state" : "ny" } }

// updating embedded document
> db.users.find({"_id": 1}, {"_id" : 0, "name" : 1, "addr" : 1})
{ "name" : "Bob", "addr" : { "street" : "1000 5th Ave", "city" : "New York", "state" : "NY" } }

// incrementing age by 1 and multiplying limit by 3
> db.users.updateOne({"_id" : 1}, {$inc : {"age" : 1}, $mul : {"limit" : 3}})
> db.users.find({"_id": 1}, {"_id" : 0, "name" : 1, "age" : 1, "limit" : 1})
{ "name" : "Bob", "age" : 31, "limit" : 3000 }

// updates only if the current score is less than 80
> db.users.updateOne({"_id" : 1}, {$inc : {"age" : 1}, $max : {"score" : 80}})
> db.users.find({"_id": 1}, {"_id" : 0, "name" : 1, "score" : 1})
{ "name" : "Bob", "score" : 80 }
```

- **Positional operator** 
- ***$addToSet*** adds value to array unless it already exists
- ***$pop*** removes the first (-1) or the last (1) item from the array
- ***$pull*** removes items that match a criteria
- ***$pullAll*** removes items that match values from an array
- ***$push*** appends an item to an array
  - ***$each***
  - ***$slice***
  - ***$sort***
  - ***$position***

```
db.example.drop()
db.example.insertOne({"_id": 1, "data" : [1, 2, 3, 4]})

> db.example.update({"_id" : 1}, { $addToSet : { "data" : 5 }})
> db.example.find()
{ "_id" : 1, "data" : [ 1, 2, 3, 4, 5 ] }

// adding an array of items with $addToSet requires the $each operator
> db.example.update({"_id" : 1}, { $addToSet : { "data" : { $each : [10,20,30]} }})
> db.example.find()
{ "_id" : 1, "data" : [ 1, 2, 3, 4, 5, 10, 20, 30 ] }



```

## Delete




# References 

(1) MongoDB Documentation - Write Concern - https://docs.mongodb.com/manual/reference/write-concern/

(2) MongoDB CRUD Operations - https://docs.mongodb.com/manual/crud/

