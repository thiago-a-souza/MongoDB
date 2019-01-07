# Author
Thiago Alexandre Domingues de Souza

# Table of Contents

- [NoSQL](./01-NoSQL.md)
- [MongoDB](./02-MongoDB.md)
- [Mongo Shell](./03-Mongo%20Shell.md)
- [CRUD](./04-CRUD.md)
- [Indexes](./05-Indexes.md)
- **[Data Modeling](#data-modeling)**
  * **[Relationships](#relationships)**
    * **[One-to-One](#one-to-one)**
    * **[One-to-Many](#one-to-many)**
    * **[Many-to-Many](#many-to-many)**
  * **[Tree Structures](#tree-structures)**
  * **[GridFS](#tree-structures)**
  * **[Views](#views)**
  * **[Capped Collections](#capped-collections)**  
  * **[Collations](#collations)**
  * **[Data Types](#data-types)**
- [Aggregation](./07-Aggregation.md)
- [Replication](./08-Replication.md)
- [Sharding](./09-Sharding.md)
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)  
- [References](./README.md#references)

# Data Modeling

The normalization process used by relational databases guarantee that the data is consistent across different tables. Among other rules, it does not allow multivalued fields nor nested relations, so the data must be stored in separate tables. This approach has a significant impact on performance because typical applications have to join several tables to pull all information needed. NoSQL databases, such as MongoDB, do not follow these rules and can benefit from embedded documents/arrays to avoid pulling data from other sources. To enforce this approach, MongoDB does not support foreign keys or joins. As a result, a document referencing another document (constraints in the SQL world), must be handled at the application level, in other words, the database will not ensure that the reference exists and does not join documents.

Initially, atomic operations were supported at the document level, and it could not ensure that multi-document changes were either committed or rolled back. In fact, transactions on a document level can cover most demands for data integrity, since embedded documents and arrays keep in the same document the data that would be stored in different sources. However, some circumstances still require ACID transactions, and developers had to handle them at the application level. With the release 4.0, MongoDB introduced multi-document transactions to solve these problems.

## Relationships

Unlike modeling the data for relational databases, designing documents must focus on the data access pattern, so the document should represent a typical usage in terms of read/write operations. As a result, the queries used should be discussed before creating the data model. The data model in MongoDB extends the traditional relationships (i.e. one-to-one, one-to-many and many-to-many) to take advantage of embedded documents and arrays. However, embedding should be used with caution to prevent spreading data inconsistencies across multiple collections. In addition to that, the final document size must be at most 16Mb, including embedded documents and references.


### One-to-One

Embedding a document into another makes it easier to access all the information with a single query. It's important to highlight that  embedding is recommended only if the application is frequently accessing that information. If the embedded document is large enough to overload the memory and it's rarely used, a separate document should be created. 

```
> db.person.findOne()
{ "_id" : 100, "name" : "joe", "age" : 30, "gender" : "male" }

> db.address.findOne()
{
	"_id" : ObjectId("5bb3d4081685d0df75e6aea7"),
	"person_id" : 100,
	"street" : "1071 5th Ave",
	"city" : "New York",
	"state" : "NY"
}

> db.person_addr.findOne()
{
	"_id" : 100,
	"name" : "joe",
	"age" : 30,
	"gender" : "male",
	"address" : {
		"street" : "1071 5th Ave",
		"city" : "New York",
		"state" : "NY"
	}
}
```

### One-to-Many

There's no single solution to address this relationship, so they can be divided into three categories: one-to-few, one-to-many, and one-to-millions.


- **One-to-Few:** if the relationship has only a few documents or items, it's fine embedding into an array, and it would allow the application to retrieve all data at once.

```
> db.blog_posts.findOne()
{
	"_id" : 1,
	"title" : "Post Title",
	"url" : "http://www.example.com",
	"comments" : [
		{
			"title" : "First comment",
			"comment" : "asdfg"
		},
		{
			"title" : "Second comment",
			"comment" : "asdfg"
		},
		{
			"title" : "Third comment",
			"comment" : "asdfg"
		}
	]
}
```

- **One-to-Many:**  if the relationship has hundreds but less than thousands of items, embedding all documents into an array would exceed the maximum document size. As a result, it should be created an array of references on the one side. This would require the application to join the data to get the referenced documents.

```
> db.companies.find({ _id : 1 }).pretty()
{
	"_id" : 1,
	"name" : "Apple",
	"products" : [
		100,
		157,
		192,
		...
	]
}

> p = db.companies.findOne({ _id : 1 });

> db.products.find({ _id : { $in : p.products } })
  { "_id" : 100, "name" : "iPad Air" }
  { "_id" : 157, "name" : "MacBook Pro 13" }
  { "_id" : 192, "name" : "Apple Watch" }
  ...
```

- **One-to-Squillions:** if the relationship has a very large number of items, the parent should reference the relationship, so the one side does not reach the maximum document size.

```
> db.users.findOne()
  { "_id" : 100, "name" : "john", "age" : 20 }

> db.products_viewed.find( { userid : 100 })
  { "_id" : 1, "productid" : "281519", "userid" : 100 }
  { "_id" : 3, "productid" : "281519", "userid" : 100 }
  ...
```


### Many-to-Many

Again, there are several alternatives to model this relationship, and the access pattern should drive the final solution. For example, if one side is frequently accessing the references but the other side is not, only that side should keep them. Otherwise, a reference can be stored on both sides. Alternatively, one side can embed the other if there are, but this should be used carefully to avoid spreading data inconsistencies accross different collections.

- **Reference on a single side:**

```
> db.movies.findOne({_id : 20983})
{
	"_id" : "20983",
	"title" : "Gladiator",
	"year" : 2000,
	"starring" : [
		115,
		132,
		...
	]
}

> c = db.movies.findOne({ _id : 20983 });

> db.cast.find({ _id : { $in : c.starring }}).pretty()
{
	"_id" : 115,
	"name" : "Russell Crowe",
	"born" : "April 7, 1964",
	"country" : "New Zealand"
}
...
```

- **Reference on both sides:**

```
> db.movies.findOne({ _id : 23516 })
{
	"_id" : 23516,
	"title" : "Forrest Gump",
	"year" : 1994,
	"starring" : [
		223,
		351,
		...
	]
}

> db.cast.findOne({_id : 223 })
{
	"_id" : 223,
	"name" : "Tom Hanks",
	"born" : "July 9, 1956",
	"country" : "USA",
	"movieIDs" : [
		23516,
		18920,
		...
	]
}
```

- **Embedding:**

```
> db.movies.findOne({_id : 78146})
{
	"_id" : 78146,
	"title" : "Back to the Future",
	"year" : 1985,
	"starring" : [
		{
			"name" : "Michael J. Fox",
			"born" : "June 9, 1961",
			"country" : "Canada"
		},
		{
			"name" : "Christopher Lloyd",
			"born" : "October 22, 1938",
			"country" : "USA"
		}
		...
	]
}
```

## Tree Structures

Because documents enable rich data structures, there are several alternatives to design trees, rather than referencing the parent node or left/right child nodes. Consider the tree illustrated in Figure 2 for the following data models.


<p align="center">
<img src="https://github.com/thiago-a-souza/tmp/blob/master/fig/model-tree.png"  height="50%" width="50%"> <br>
  <b>Figure 2:</b> Sample Tree<a href="https://github.com/thiago-a-souza/tmp/blob/master/README.md#references"> (3)</a>
</p> 

- **Parent Reference:**
```
> db.categories.find()
  { "_id" : "MongoDB", "parent" : "Databases" }
  { "_id" : "dbm", "parent" : "Databases" }
  { "_id" : "Databases", "parent" : "Programming" }
  { "_id" : "Languages", "parent" : "Programming" }
  { "_id" : "Programming", "parent" : "Books" }
  { "_id" : "Books", "parent" : null }
```

- **Child References:**

```
> db.categories.find()
  { "_id" : "MongoDB", "children" : [ ] }
  { "_id" : "dbm", "children" : [ ] }
  { "_id" : "Databases", "children" : [ "MongoDB", "dbm" ] }
  { "_id" : "Languages", "children" : [ ] }
  { "_id" : "Programming", "children" : [ "Databases", "Languages" ] }
  { "_id" : "Books", "children" : [ "Programming" ] }
```

- **Ancestors References:**

```
> db.categories.find()
  { "_id" : "MongoDB", "ancestors" : [ "Books", "Programming", "Databases" ], "parent" : "Databases" }
  { "_id" : "dbm", "ancestors" : [ "Books", "Programming", "Databases" ], "parent" : "Databases" }
  { "_id" : "Databases", "ancestors" : [ "Books", "Programming" ], "parent" : "Programming" }
  { "_id" : "Languages", "ancestors" : [ "Books", "Programming" ], "parent" : "Programming" }
  { "_id" : "Programming", "ancestors" : [ "Books" ], "parent" : "Books" }
  { "_id" : "Books", "ancestors" : [ ], "parent" : null }
```

- **Path Reference:**

```
> db.categories.find()
{ "_id" : "Books", "path" : null }
{ "_id" : "Programming", "path" : ",Books," }
{ "_id" : "Databases", "path" : ",Books,Programming," }
{ "_id" : "Languages", "path" : ",Books,Programming," }
{ "_id" : "MongoDB", "path" : ",Books,Programming,Databases," }
{ "_id" : "dbm", "path" : ",Books,Programming,Databases," }

// finding descendants
> db.categories.find( { path: /,Programming,/ } )
{ "_id" : "Databases", "path" : ",Books,Programming," }
{ "_id" : "Languages", "path" : ",Books,Programming," }
{ "_id" : "MongoDB", "path" : ",Books,Programming,Databases," }
{ "_id" : "dbm", "path" : ",Books,Programming,Databases," }
```


## GridFS

Because the maximum document size is 16Mb, MongoDB provides GridFS to store large files. By default, it splits the original file into chunks of 255Kb, allowing the driver to return the complete file or specific parts. GridFS stores chunks in the *fs.chunks* collection and the metatada for each file in *fs.files*


Sample code written in Java - complete code found [here](./MongoClient):

```java
MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
MongoDatabase db = mongoClient.getDatabase("mydb");

GridFSBucket gridFSBucket = GridFSBuckets.create(db);

InputStream streamToUploadFrom = new FileInputStream(new File("/path/to/file"));

ObjectId id = gridFSBucket.uploadFromStream("my-large-file", streamToUploadFrom);
System.out.println("_id : " + id);
```

Collections created:

```
> show collections
fs.chunks
fs.files

> db.fs.files.findOne()
{
	"_id" : ObjectId("5bb60be4765ca69a6f6e8998"),
	"filename" : "my-large-file",
	"length" : NumberLong(46255756),
	"chunkSize" : 261120,
	"uploadDate" : ISODate("2017-10-04T12:47:33.618Z"),
	"md5" : "ef5df36bfac1c84be209e237ce1bb924"
}
```

## Views

The concept of views in relational databases is similar in MongoDB. It can transform complex queries into an easier format and also shows only required fields, hiding sensitive information. View's data cannot be modified, but changes to the underlying collection are propagated to the view. In addition to that, removing the source collection does not remove the corresponding view, rather it displays an empty collection, and the view reflects the data when the collection is populated. In terms of performance, views can take advantage of the source collection indexes to query and sort the data. Because views are essentially queries, they don't occupy the space of the documents displayed. Actually, a view can use another view as a source rather than a collection. Finally, when a view is created, a collection representing the view is produced as if it was a regular collection, and it's definition is stored in the *system.views* collection.


```
> db.person.find()
{ "_id" : 1, "name" : "john", "ssn" : "111-22-3333" }
{ "_id" : 2, "name" : "peter", "ssn" : "123-45-6789" }
{ "_id" : 3, "name" : "lisa", "ssn" : "321-54-9876" }

> db.createView("personView", "person", [{ $project : {_id : 1, name : 1} }])

> show collections
person
personView
system.views

> db.personView.find()
{ "_id" : 1, "name" : "john" }
{ "_id" : 2, "name" : "peter" }
{ "_id" : 3, "name" : "lisa" }

> db.personView.find({ _id : { $gte : 2 }}).sort({ "name" : 1 })
{ "_id" : 3, "name" : "lisa" }
{ "_id" : 2, "name" : "peter" }

> db.system.views.find()
{ "_id" : "testing.personView", "viewOn" : "person", "pipeline" : [ { "$project" : { "_id" : 1, "name" : 1 } } ] }

// dropping a view 
> db.personView.drop()
```

## Capped Collections

Unlike regular collections, which can grow dynamically, capped collections have a fixed size, specified in advance, and when there's no space to add a new document, the oldest one is replaced, operating like a circular list. This behavior is particularly useful to store log files or caching data.

To preserve these properties, some operations are not allowed on capped collections. Updates that cause an increase in the document size throw an error. Also, deletes are not allowed to maintain the insertion order. As a result, a document can be removed when it becomes the oldest document or the entire collection must be dropped. Finally, sharding is not available for capped collections.

Capped collections are created explicitly, and it requires the *size* option specifying the limit in bytes. Optionally, it's possible to specify the *max* number of documents allowed. Whichever option reached first becomes the limit of the capped collection.

```
> db.createCollection("my_log_capped1", {"capped" : true, "size" : 8192});
> db.createCollection("my_log_capped2", {"capped" : true, "size" : 8192, "max" : 100});
```




## Collations

Collations are used to compare strings based on language rules. Unless specified, results are sorted using a binary comparison. Collations can be defined at several levels (e.g. collections, indexes, and CRUD), but collection's collation is used, except when an alternative configuration is provided. The method *db.getCollectionInfos()* can be used to identify the default collation for each collection. For CRUD operations, only functions that queries data (e.g. *find*, *remove*, *update*, etc) support collations. Because *insert* does not query data, it does not allow collations. In general, the collation is declared as an argument in the method, but for *find* and *sort* the function *cursor.collation* is used.

Although several fields are available, only *locale* is mandatory. Another key field is *strength*, allowing five levels of comparisons.

- **Level 1:** only base characters are considered, and ignores diacritics/case (a < b)
- **Level 2:** base characters and diacritics are evaluated, and ignores case (as < às < at)
- **Level 3:** default option; examines base characters, diacritics, and case (ao < Ao < aó)

Because levels 1 and 2 are not case sensitive, they can be used to match not exact strings, for example, level 1 can treat words with accents or not as if they were equal. This enables creating case-insensitive collections or indexes.

```
> db.createCollection("strength1", { collation : { locale: "pt", strength : 1}})
> db.strength1.insertMany([ {_id : 1, name : "açaí" }, {_id : 2, name : "Açaí" },
                            {_id : 3, name : "Acaí" }, {_id : 4, name : "acai" } ])

// level 1 ignores diacritics/case 			    
> db.strength1.find({"name" : "açaí" })
  { "_id" : 1, "name" : "açaí" }
  { "_id" : 2, "name" : "Açaí" }
  { "_id" : 3, "name" : "Acaí" }
  { "_id" : 4, "name" : "acai" }

> db.createCollection("strength3", { collation : { locale: "pt", strength : 3}}) 
> db.strength3.insertMany([ {_id : 1, name : "açaí" }, {_id : 2, name : "Açaí" },
                            {_id : 3, name : "Acaí" }, {_id : 4, name : "acai" } ])
// level 3 will match only words with the same diacritics/case
> db.strength3.find({"name" : "açaí" })
  { "_id" : 1, "name" : "açaí" }
```

Unless an index specifies a collation, indexes created inherit the collation from the collection. These different settings influence how CRUD operations are performed. For an index scan, *find()* or *sort()* should use indexes with the same configuration, otherwise the collation must be explicitly declared. To avoid an in-memory sort, *sort()* should use indexes with the same collation of the source collection or a different collation must be declared. Tipically, duplicate indexes are not allowed, but if they have different collations they can be created using a provided name. Finally, a collection scan is performed if there's no matching index collation.


```
> db.createCollection("employees", { collation : { locale: "en"}})
// index will inherit the collection collation
> db.employees.createIndex( { role : 1 } )
// index with a different collation
> db.employees.createIndex( { city : 1 }, { collation : { locale : "pt" }})
// index on the same field, but different collation
> db.employees.createIndex( { city : 1 }, { name : "city-ru", collation : { locale : "ru" }})


// listing index collations
> db.employees.getIndexes()
   "name" : "_id_",
   "collation" : {
      "locale" : "en",
        ...
   "name" : "role_1",
   "collation" : {
      "locale" : "en",
        ...
   "name" : "city_1",
   "collation" : {
      "locale" : "pt",
        ...
   "name" : "city-ru",
   "collation" : {
      "locale" : "ru",
        ...

// index scan: role has the same collation
> db.employees.find({role : ""})

// collection scan: city has a different collation
> db.employees.find({city : ""})

// index scan: explicitly declaring the index collation
> db.employees.find({city : ""}).collation({ locale : "ru" })

// collection scan: no matching collation
> db.employees.find({city : ""}).collation({ locale : "ja" })



// all documents are scanned and index sort: role has the same collation
> db.employees.find().sort({ role : 1 })

// collection scan and in-memory sort: city has a different collation
> db.employees.find().sort({ city : 1 })

// all documents are scanned and index sort: explicitly declaring the index collation
> db.employees.find().sort({ city : 1 }).collation({ locale : "pt" })



// all documents are scanned and index sort: role has the same collation
> db.employees.find({city : ""}).sort({ role : 1 })

// index scan and in-memory sort: city has a different collation
> db.employees.find({role : ""}).sort({ city : 1 })

// all documents are scanned and index sort: explicitly declaring the index collation
> db.employees.find({role : ""}).collation({ locale : "pt" }).sort({ city : 1 })



> exp = db.employees.explain()

// collection scan: city does not match the default collation
> exp.update({ city : "" }, { $set : { address : "" }} )

// index scan: declaring a collation that matches an index
> exp.update({ city : "" }, { $set : { address : "" }}, { collation : { locale : "pt" }} )
```



## Data Types

BSON supports several data types not available in the JSON specification.

- ***Date()*** *Date()* returns the current date as a string, while *new Date()* returns an *ISODate* object
- ***ISODate()*** *ISODate()* returns an *ISODate* object
- ***ObjectId()*** wrapper class to store document IDs
- ***NumberLong()*** by default, all numbers in mongo shell have a 64-bit double representation; this wrapper stores 64-bit integers.
- ***NumberInt()*** wrapper stores 32-bit integers
- ***NumberDecimal()*** wrapper stores 128-bit floating-point values; although it accepts a numeric representation, the value should be passed as a string to avoid losing precision.

```
> db.example.drop()
> db.example.insertMany([
{_id : 1, a : Date(), b : new Date(), c : ISODate()},
{_id : 2, x : NumberDecimal("12345.6789"), y : NumberDecimal(12345.6789)}
])

> db.example.find().pretty()
{
	"_id" : 1,
	"a" : "Fri Oct 05 2018 18:07:38 GMT+0000 (UTC)",
	"b" : ISODate("2018-10-05T18:07:38.319Z"),
	"c" : ISODate("2018-10-05T18:07:38.319Z")
}
{
	"_id" : 2,
	"x" : NumberDecimal("12345.6789"),
	"y" : NumberDecimal("12345.6789000000")
}


```
