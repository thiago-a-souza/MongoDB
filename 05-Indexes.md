# Author

Thiago Alexandre Domingues de Souza

# Table of Contents

- [NoSQL](./01-NoSQL.md)
- [MongoDB](./02-MongoDB.md)
- [Mongo Shell](./03-Mongo%20Shell.md)
- [CRUD](./04-CRUD.md)
- **[Indexes](#indexes)**
  * **[Creating Indexes in Background](#creating-indexes-in-background)**
  * **[Explain](#explain)**
  * **[Covered Query](#covered-query)**
  * **[Index Types](#index-types)**
     * **[Single Field Indexes](#single-field-indexes)**
     * **[Compound Indexes](#compound-indexes)**
     * **[Multikey Indexes](#multikey-indexes)**
     * **[Text Indexes](#text-indexes)**
     * **[Geo Indexes](#geo-indexes)**
  * **[Index Options](#index-options)**
    * **[Unique](#unique)**
    * **[Sparse](#sparse)**
    * **[TTL](#ttl)**
- [Data Modeling](./06-Data%20Modeling.md)
- [Aggregation](./07-Aggregation.md)
- [Replication](./08-Replication.md)
- [Sharding](./09-Sharding.md)
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

    
# Indexes

Similar to traditional databases, MongoDB also provides indexes for a faster query execution, avoiding full collection scans. In general, indexes speed up reads and slow down writes. For inserts, indexes slow down writes because it has to add new nodes to the B-trees. For deletes, it depends if the operation is deleting everything or just some documents. Removing everything will cause all B-tree nodes to be removed, increasing the overhead. On the other hand, if just some documents are  deleted, the index can help finding the document, which improves the performance. For updates, indexes can also help finding documents but if the field modified is an index, there will be an overhead to modify B-tree nodes to apply this change. Updates may also increase the document size beyond the allocated space. In that case, the document must be moved to a new disk area, and all indexes should be updated to point to the new location. Despite all the overhead in writes, the cost usually compensates read operations.

When a query is executed, MongoDB looks at the query shape to identify index candidates that can satisfy the query, and then it creates different query plans to verify which one returns fastest results. The winning plan is stored in cache, so future queries with the same shape can use the same plan. Several circumstances can clear this cache, for example, rebuilding/adding/removing indexes, restarting the server, or when it exceeds a threshold of writes.

Indexes can be manipulated using the methods *createIndex*, *dropIndex*, and *getIndexes*. The syntax to create or drop an index is similar to *sort* because index values are ordered, allowing more efficient sort operations. Obviously, the query  influences if an index will be used or not. To verify the actual execution plan, the *explain* method should be used. To override the index identified, the method *hint* forces an alternative index to be executed.


## Creating Indexes in Background

By default, indexes are created in foreground, and the collection is blocked for read/write operations until the process completes. For long collections, indexes can be created in background, allowing reads and writes on the primary while the index is being created - secondaries will run in foreground, so queries will be blocked on secondaries.  Despite the term background, creating such index does not return to the shell until it completes, so another session should be opened to run other commands. In terms of performance, creating indexes in background take longer than in foreground.

```
> db.example.createIndex({"name" : 1}, {background : true})
```

## Explain

The explain methods display details about the query execution, such as the winning plan, including if it's running a collection scan or an index scan, the index used and their directions, the sorting strategy used (index or in-memory), etc. There are two explain methods available: *cursor.explain* and *collection.explain*. The first method works with *find* and *sort*, while the other supports  *find*, *update*, *remove*, *aggregate*, *count*, *distinct*, and *group*. Running the explain methods against *update* or *remove* does not modify the data. **Notice that *insert* is not supported**.

By default, the explain methods displays the information using the *queryPlanner* mode, without actually running the query. The *executionStats* displays the *queryPlanner* stats and also executes the winning plan, showing stats like the number of documents returned, keys examined, execution time, etc. Finally, the *allPlansExecution* executes all plans, including rejected plans.



```
> db.example.drop()
> for(i=1; i<=1000; i++){
    db.example.insertOne({"_id" : i, "title" : "test-"+(i%10) })
  }
  
  
> db.example.find().explain()
> db.example.explain().find()
> db.example.find().sort({"title" : 1}).explain()
> db.example.explain().find().sort({"title" : 1})

// nothing is removed
> db.example.explain().remove({})
// nothing is updated
> db.example.explain().update({"title" : "test-1"}, { $set : {"title" : "test-100"}})

> db.example.explain().count()
> db.example.explain().distinct("title")

// explainable object
> var obj = db.example.explain()
> obj.find()
> obj.remove({})
> obj.count()
> obj.distinct("title")


// explain methods
> db.example.find().explain() // default: queryPlanner
> db.example.find().explain("executionStats") 
> db.example.find().explain("allPlansExecution")
```

## Covered Query

MongoDB allows running queries without examining any documents with covered queries. This situation happens when it uses an index scan and all fields returned are in the same index. It's important to highlight that multikey indexes do not support covered queries.

```     
> db.example.drop()
> for(i=1; i<=1000; i++) {
    db.example.insertOne({_id : i, a : (i*10), b : (i*10 + 1) , c : (i*100 + 1)})
}

> db.example.createIndex({ a : 1, b : 1 })
> db.example.createIndex({ c : 1, d : 1 })

// index scan and covered query: no documents are examined because indexes a and b are part of the query
> db.example.find({a: { $gt : 300}, b : { $gt : 400} }, {_id : 0, a : 1, b : 1 }).explain("executionStats")
   ...
   "executionStats" : {
      "executionSuccess" : true,
      "nReturned" : 961,
      "executionTimeMillis" : 2,
      "totalKeysExamined" : 970,
      "totalDocsExamined" : 0,
      ...

// index scan and non-covered query: documents are examined because indexes c and d are not part of the query
> db.example.find({a: { $gt : 300}, b : { $gt : 400} }, {_id : 0, c : 1, d : 1 }).explain("executionStats")
   ...
   "executionStats" : {
      "executionSuccess" : true,
      "nReturned" : 961,
      "executionTimeMillis" : 2,
      "totalKeysExamined" : 970,
      "totalDocsExamined" : 961,
      ...

// index scan and non-covered query: although a and b are part of the query, the _id field is displayed and it's a different index
> db.example.find({a: { $gt : 300}, b : { $gt : 400} }, { a : 1, b : 1 }).explain("executionStats")
   "executionStats" : {
      "executionSuccess" : true,
      "nReturned" : 961,
      "executionTimeMillis" : 2,
      "totalKeysExamined" : 970,
      "totalDocsExamined" : 961,
```


## Index Types

Several index types are supported for a wide range of purposes. 

### Single Field Indexes

This is the most basic index type. It allows creating indexes on a single field, including embedded fields, in ascending or descending order. If a non-index column is used to sort the documents, it will perform an in-memory sorting.
Sort operations that use indexes have a better performance because they take advantage of index data structures to sort the results, rather than moving the data to memory and sorting. In addition to that, in-memory sorting fails if the operation exceeds 32mb.

```
> db.people.drop()
> db.people.insertMany([ 
  {"_id" : 1, "name" : "john",  "age" : 25, "addr" : { "city" : "new york", "state" : "ny"}},
  {"_id" : 2, "name" : "peter", "age" : 36, "addr" : { "city" : "los angeles", "state" : "ca"}},
  {"_id" : 3, "name" : "alex",  "age" : 36, "addr" : { "city" : "miami", "state" : "fl"}}
 ])
		       
// only the _id unique index is present
> db.people.getIndexes()
[
  {
    "v" : 2,
    "key" : {
      "_id" : 1
    },
    "name" : "_id_",
    "ns" : "mydb.people"
  }
]

// collection scan: there are no indexes on the field
> db.people.find({"name" : "john" }).explain()
   ...
   "winningPlan" : {
     "stage" : "COLLSCAN",
     "filter" : {
        "name" : {
           "$eq" : "john"
        }
     },
     "direction" : "forward"
   },
   ...

// creating an index on name in ascending order
> db.people.createIndex({"name" : 1})

// index scan: field has an index now
> db.people.find({"name" : "john" }).explain()
   ...
   "winningPlan" : {
      "stage" : "FETCH",
      "inputStage" : {
         "stage" : "IXSCAN",
         ...

// creating an index on an embedded field
> db.people.createIndex({"addr.state" : -1})

// index scan: both branches have indexes
> db.people.find({$or : [{"name" : "john" }, { "addr.state" : "ca"}] })

// collection scan: only one branch has an index
> db.people.find({$or : [{"name" : "john" }, { "age" : "36"}] })

// index scan: there's only one branch an it contains an index
> db.people.find({"name" : "john", "age" : 25 })

// collection scan: no index used
> db.people.find()

// all documents are scanned and uses index sorting
> db.people.find().sort({ "name" : 1 })

// index scan and in-memory sorting: query is using a filter but the sort is not (stage is sort)
> db.people.find({name : "john"}).sort({ "age" : 1 }).explain()
   ...
   "winningPlan" : {
      "stage" : "SORT",
      "sortPattern" : {
         "age" : 1 
      },
      "inputStage" : {
         "stage" : "SORT_KEY_GENERATOR",
         "inputStage" : {
             "stage" : "FETCH",
             "inputStage" : {
                "stage" : "IXSCAN",
                "keyPattern" : {
                   "name" : 1
                },
                ...				
				
// indexes are different, the winning plan below chose to scan all documents and perform an index sorting rather than 
// using an index scan and then an in-memory sort
> db.people.find({name : "john"}).sort({ "addr.state" : 1 }).explain()
   ...
   "winningPlan" : {
      "stage" : "FETCH",
      "filter" : {
         "name" : {
            "$eq" : "john"
         }
      },
      "inputStage" : {
         "stage" : "IXSCAN",
         "keyPattern" : {
            "addr.state" : -1
         },
         ...	        
```

### Compound Indexes

Having a compound index does not mean that any combination of index fields can be used to execute an index scan. Only queries using index prefixes can benefit from index scans. For example, consider the compound index (A, B, C), an index scan will be used for queries using (A), (A,B) or (A,B,C), and a collection scan will used for (B), or (B,C).
 
For single field indexes, the sorting direction using an index cannot make a query to perform a collection scan because it can follow either directions. On the other hand, queries using compound keys must use the same or the opposite sort order specified in the index, otherwise it will an in-memory sorting. In addition to that, it's also possible to benefit from index sorting without a prefix index in the sorting clause, as long as the query has the preceding prefix and they use an equality operator.


```
> db.people.drop()
> db.people.insertMany([ 
  {"_id" : 1, "name" : "john",  "age" : 25, "country" : "usa"},
  {"_id" : 2, "name" : "peter", "age" : 36, "country" : "canada"},
  {"_id" : 3, "name" : "lisa",  "age" : 19, "country" : "australia"},
  {"_id" : 4, "name" : "alex",  "age" : 36, "country" : "uk"},
  {"_id" : 5, "name" : "susan",  "age" : 28, "country" : "usa"}
 ])
 
> db.people.createIndex({ "name" : 1, "age" : -1, "country" : 1 })

// index scan: name is a prefix 
> db.people.find({"name" : "john"})

// index scan: prefix fields
> db.people.find({"name" : "john", "age" : 25})

// collection scan: age alone is not a prefix
> db.people.find({"age" : 36})

// all documents are scanned and index sorting because the prefix has the same sort order
> db.people.find().sort({"name" : 1, "age" : -1 })

// all documents are scanned and index sorting because the prefix has the opposite sort order
> db.people.find().sort({"name" : -1, "age" : 1 })

// collection scan and in-memory sorting because the sort order is not the same/opposite
> db.people.find().sort({"name" : 1, "age" : 1 })

// collection scan and in-memory sorting: order is not the same/opposite
> db.people.find().sort({"name" : -1, "age" : -1 })

// index scan and in-memory sorting: query is a prefix but the sorting order is not the same/opposite
> db.people.find({"name" : "john"}).sort({"name" : -1, "age" : -1 })

// index scan and index sort: because name/age use an equality operator and preceed country it uses an index sort
> db.people.find({"name" : "john", "age" : 25}).sort({"country" : 1})

// index scan and index sort: because name/age use an equality operator and preceed country it uses an index sort
> db.people.find({"name" : "john", "age" : 25}).sort({"country" : -1})

// index scan and in-memory sort: name/age are prefix but age is not using an equality, that's why it's an in-memory sort
> db.people.find({"name" : "john", "age" : {$gt : 20 } }).sort({"country" : 1})
```

### Multikey Indexes

MongoDB allows indexing arrays of scalars or embedded documents with multikey indexes. When an indexed field has an array, the index is marked as multikey. Once that happens, it cannot be unflagged unless the index is recreated after removing the array  field from all documents.

Multikey indexes should be used carefully because the size of the index can grow very fast depending on the number of documents and the size of the array. Compound multikey indexes is also possible, but at most one field of each document can be an array. For example, given a collection with a compound key (A,B), a document can have an array on A, another on B but no document can have an array on A and B simultaneously. If a compound index already exists in a collection, it will not allow violating this rule with an insert/update. Notice that it's possible to have more than one multikey indexes in the same document, but they cannot be in the same index (compound).

```
> db.collection.drop()
> db.collection.insertOne({ "_id" : 1, "a" : 10, "b" : [10, 15, 17, 19]})

// creating multikey index is similar to any other index
> db.collection.createIndex({"a": 1, "b" : 1})

// index scan: isMultiKey indicates a multikey index
> db.collection.find({"a" : 10, "b" : 17}).explain()
   "stage" : "IXSCAN",
   ...
   "indexName" : "a_1_b_1",
   "isMultiKey" : true,
   ...

// correct: only one field is an array
> db.collection.insertOne({ "_id" : 2, "a" : [20, 21], "b" : 23})

// correct: only one field is an array
> db.collection.insertOne({ "_id" : 3, "a" : 47, "b" : [31, 85]})

// error: compound multikey index allow at most one array
> db.collection.insertOne({ "_id" : 4, "a" : [20, 21], "b" : [23, 25, 29]})


// it's allowed to have more than one single field multikey index
> db.test.drop()
> db.test.insert({_id : 0, x : [1,1,2], data : [3,5,8]})
> db.test.createIndex({x : 1})
> db.test.createIndex({y : 1})
> db.test.find()
{ "_id" : 0, "x" : [ 1, 1, 2 ], "data" : [ 3, 5, 8 ] }

```

### Text Indexes

Text indexes create tokens from strings or arrays, so it can be searched more efficiently with indexes. This search is not case sensitive and also matches plural and singular words. A collection can have only one text index, but multiple fields are allowed. In addition to that, text indexes provide a matching score, so it can be displayed and sorted using the *$meta* operator.

Unlike regular indexes, which can be dropped using a syntax similar to the way they were created, dropping a text index requires the index name that can be found using the function *getIndexes()*.


```
> db.example.drop()

> db.example.insertMany([
{ _id : 1, fruits : "mango Apple pear ORANGES avocado " },
{ _id : 2, fruits : "banana strawberry GRape lemon APPLES" },
{ _id : 3, fruits : "orange" }
])

// creating a text index
> db.example.createIndex({ "fruits" : "text" })

> db.example.find({$text : { $search : "orange" } })
{ "_id" : 3, "fruits" : "orange" }
{ "_id" : 1, "fruits" : "mango Apple pear ORANGES avocado " }

> db.example.find({$text : { $search : "banana apple" } })
{ "_id" : 2, "fruits" : "banana strawberry GRape lemon APPLES" }
{ "_id" : 1, "fruits" : "mango Apple pear ORANGES avocado " }

> db.example.find( { $text : {$search : "banana apple"} }, { "my-text-score" : {$meta : "textScore" }  })
{ "_id" : 1, "fruits" : "mango Apple pear ORANGES avocado ", "my-text-score" : 0.6 }
{ "_id" : 2, "fruits" : "banana strawberry GRape lemon APPLES", "my-text-score" : 1.2 }

> db.example.find( { $text : {$search : "banana apple"} }, { "my-text-score" : {$meta : "textScore" }  }).sort({ "my-text-score" : {$meta : "textScore" }  })
{ "_id" : 2, "fruits" : "banana strawberry GRape lemon APPLES", "my-text-score" : 1.2 }
{ "_id" : 1, "fruits" : "mango Apple pear ORANGES avocado ", "my-text-score" : 0.6 }


// finding the index name
> db.example.getIndexes()
    ...
    "name" : "fruits_text",
    ...    

// dropping text index    
> db.example.dropIndex("fruits_text")    
```


### Geo Indexes

MongoDB supports *2d* indexes to find locations on a flat space and *2dsphere* for spherical geometries.

```
> db.museums.drop()
> db.museums.insertMany([
  {_id : 1 , name : "American Museum of Natural History", loc : [40.781397, -73.974010] },
  {_id : 2 , name : "Louvre", loc : [48.860622, 2.337716] },
  {_id : 3 , name : "National Gallery", loc : [51.508935, -0.128258] }
  ])

> db.museums.createIndex({ "loc" : "2dsphere" })

// locations sorted by nearest pair of coordinates
> db.museums.find( { "loc" : { $near : { $geometry : { type : "Point" , coordinates : [ 25.777519, -80.131284] } } } } )
  { "_id" : 1, "name" : "American Museum of Natural History", "loc" : [ 40.781397, -73.97401 ] }
  { "_id" : 3, "name" : "National Gallery", "loc" : [ 51.508935, -0.128258 ] }
  { "_id" : 2, "name" : "Louvre", "loc" : [ 48.860622, 2.337716 ] }

// find location within a circle - requires location and radius
> db.museums.find( { "loc" : { $geoWithin : { $centerSphere : [ [ 48.0, 2.0 ] , 0.05 ] } } } )
  { "_id" : 2, "name" : "Louvre", "loc" : [ 48.860622, 2.337716 ] }

// find location within a polygon - last location must close the loop
> db.museums.find( { "loc" : { $geoWithin : { $geometry : { type : "Polygon" , coordinates : [ [
                               [51.544892, -0.132816],
                               [51.471908, -0.165075],
                               [51.472758, -0.065038],
                               [51.544892, -0.132816]
                               ] ]
                      } } } } )
  { "_id" : 3, "name" : "National Gallery", "loc" : [ 51.508935, -0.128258 ] }

```

## Index Options
### Unique

This index option enforces unique values, including nulls, for single or compound fields. It cannot store duplicate values or create a unique key on a collection that already has duplicate values on the specified key(s). Also, it allows creating a unique key on a missing field. 

```
> db.example.drop()
> db.example.insertMany([{ a : 1, b : 10 }, 
                         { a : 2, b : 20 }])
> db.example.createIndex({ a : 1 }, { unique : true })

// identifying unique indexes
> db.example.getIndexes()
[
	{
		"v" : 2,
		"key" : {
			"_id" : 1
		},
		"name" : "_id_",
		"ns" : "mydb.example"
	},
	{
		"v" : 2,
		"unique" : true,
		"key" : {
			"a" : 1
		},
		"name" : "a_1",
		"ns" : "mydb.example"
	}
]

// error: unique index violation
> db.example.insertOne({ a : 1, b : 1000})
   

> db.example.drop()
> db.example.insertMany([{ a : 1, b : 10 }, 
                         { a : 2, b : 20 }])
// compound unique key
> db.example.createIndex({ a : 1, b : 1 }, { unique : true })

// correct: single field alone does not violate the unique index
> db.example.insertOne({ a : 1, b : 1000})

// error: unique compound index violation
> db.example.insertOne({ a : 2, b : 20})


> db.example.drop()
> db.example.createIndex({ x : 1 }, { unique : true })

// correct: x will be stored as null
> db.example.insertOne({ a : 1 })

// error: duplicate null for x
> db.example.insertOne({ a : 2 })
```

### Sparse

Sparse indexes store in the B-tree only documents that contain the indexed field and it's not null. For that reason, sparse indexes are smaller than regular indexes. The sparse option combined with unique prevents loading duplicates but allows omitting the field - if only the unique option is used, it doesn't allow multiple documents missing the indexed field. Because the sparse index does not contain all documents, sort operations using a sparse index run a collection scan. The *hint* method can force a sparse index to be used but it may return incorrect results.


```
> db.example.drop()
> db.example.insertMany([ 
  {"_id" : 1, "name" : "john",  "ssn" : "111-22-3333"},
  {"_id" : 2, "name" : "peter", "ssn" : "123-45-6789"},
  {"_id" : 3, "name" : "lisa",  "ssn" : "321-54-9876"},
  {"_id" : 4, "name" : "alex"},
  {"_id" : 5, "name" : "susan"}
 ])
 
> db.example.createIndex({ ssn : 1 }, { unique : true, sparse : true })
> db.example.createIndex({ name : 1 })

// index scan
> db.example.find({ ssn : "321-54-9876"})

// index scan and in-memory sort
> db.example.find({ ssn : "321-54-9876"}).sort({ name : 1 })

// index scan and index sorting
> db.example.find({ ssn : { $gte : "1"} }).sort({ ssn : 1})

// index scan and in-memory sorting
> db.example.find({ name : "john" } ).sort({ ssn : 1})

// collection scan and in-memory sorting
> db.example.find().sort({ ssn : 1 })

// forces scanning all documents that have the ssn field and perform an index scan,
// it will not return documents that don't have a ssn
> db.example.find().sort({ ssn : 1 }).hint({ ssn : 1 })

// all documents are scanned and perform an index sorting
> db.example.find().sort({ name : 1 })
```

### TTL

This option removes documents after a specified time limit (in seconds) on a field whose value is either a date or an array of dates. In case of an array of dates, the minimum date is used to calculate the expiration limit. If the field does not exist or it's not a date, the document is not removed.

Restrictions: 

- compound indexes do not support TTL indexes and ignore the expireAfterSeconds option
- the *_id* field does not support TTL indexes
- capped collections do not support capped collections because their documents cannot be removed

```
> db.example.drop()
> db.example.insertOne({ _id : 1, created : new Date()});

> db.example.createIndex({ created : 1 }, { expireAfterSeconds : 120 });

// identifying ttl indexes
> db.example.getIndexes()
[
	{
		"v" : 2,
		"key" : {
			"_id" : 1
		},
		"name" : "_id_",
		"ns" : "mydb.example"
	},
	{
		"v" : 2,
		"key" : {
			"created" : 1
		},
		"name" : "created_1",
		"ns" : "mydb.example",
		"expireAfterSeconds" : 120
	}
]

// will not return data after 120 seconds
> db.example.find();

// document will not be removed because TTL field does not exist
> db.example.insertOne({ _id : 2, title : "test" }
```
