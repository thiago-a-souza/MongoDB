# Author

Thiago Alexandre Domingues de Souza

# Table of Contents

- [NoSQL](./1-NoSQL.md)
- [MongoDB](./2-MongoDB.md)
- [Mongo Shell](./3-Mongo%20Shell.md)
- [CRUD](#crud)
  * [Create](#create)
     * [insertOne](#insertone)
     * [insertMany](#insertmany)
     * [save](#save)  
     * [findAndModify](#findandmodify)
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
     * [remove](#remove)
     * [deleteOne and deleteMany](#deleteone-and-deletemany)  
- [Indexes](./5-Indexes.md)     
- [Data Modeling](./6-Data%20Modeling.md)    


# CRUD

In contrast to relational databases, MongoDB does not support SQL to perform CRUD operations. Instead, it provides APIs to popular programming languages that can access the database and execute queries. These functions take documents as parameters, including the data, filters, and other options. Depending on the programming language used, additional boilerplate code is required to perform these operations. The commands described in this document refers to Javascript syntax used by  Mongo shell. For examples in other programming languages, visit the documentation at [(2)](#references).

Older versions does not support ACID transactions to insert/update/delete multiple documents. As a result, concurrent requests might see different results while the documents are being modified. However, each individual insert/update/delete operation is atomic, so users will not see a half-modified document.

## Create

Prior to version 3.2, inserting one or multiple documents into a collection was performed using the same *insert* function. This is still allowed for backward compatibility, but more appropriate functions were introduced, according to the number of documents loaded: *insertOne* and *insertMany*.

If the document loaded does not specify the unique *_id* field, MongoDB creates automatically an *ObjectId*. This field represents the document's primary key, in other words, there are no duplicate documents with the same *_id* in the collection.

### insertOne

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

### findAndModify

Modifies and returns one document. It can also insert a new document if the *upsert* option is enabled. By default, it returns the document before the modification takes place, but it can return the modified document if the *new* option is enabled.

**Syntax:**

```
db.collection.findAndModify({
    query: <document>,
    sort: <document>,
    remove: <boolean>,
    update: <document>,
    new: <boolean>,
    fields: <document>,
    upsert: <boolean>,
    bypassDocumentValidation: <boolean>,
    writeConcern: <document>,
    collation: <document>,
    arrayFilters: [ <filterdocument1>, ... ]
});
```

**Examples:**

```
> db.people.drop()
> db.people.insertMany([ {"_id" : 1, "name" : "john", "age" : 20 },
                         {"_id" : 2, "name" : "peter", "age" : 25 },
                         {"_id" : 3, "name" : "john", "age" : 38 },
                         {"_id" : 4, "name" : "peter", "age" : 40 },
                        ])

> db.people.findAndModify({ "query" : { "name" : "john" }, 
                            "sort" : { "age" : 1}, 
                            "update" : { $inc : { "age" : 1 } }})                        
{ "_id" : 1, "name" : "john", "age" : 20 } 
> db.people.find()                          
{ "_id" : 1, "name" : "john", "age" : 21 }
{ "_id" : 2, "name" : "peter", "age" : 25 }
{ "_id" : 3, "name" : "john", "age" : 38 }
{ "_id" : 4, "name" : "peter", "age" : 40 }

> db.people.findAndModify({ "query" : { "name" : "alex" }, 
                            "update" : { $set : { "age" : 22 } },
                            "upsert" : true
                           })                        
null 
> db.people.find() 
{ "_id" : 1, "name" : "john", "age" : 21 }
{ "_id" : 2, "name" : "peter", "age" : 25 }
{ "_id" : 3, "name" : "john", "age" : 38 }
{ "_id" : 4, "name" : "peter", "age" : 40 }
{ "_id" : ObjectId("5ba42be856dfe3533fdab900"), "name" : "alex", "age" : 22 }

> db.people.findAndModify({ "query" : { "name" : "peter" }, 
                            "sort" : { "age" : -1}, 
                            "remove" : true })           
{ "_id" : 4, "name" : "peter", "age" : 40 }                                     
> db.people.find()                          
{ "_id" : 1, "name" : "john", "age" : 21 }
{ "_id" : 2, "name" : "peter", "age" : 25 }
{ "_id" : 3, "name" : "john", "age" : 38 }
{ "_id" : ObjectId("5ba42be856dfe3533fdab900"), "name" : "alex", "age" : 22 }
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
- ***$elemMatch*** it should be used queries using arrays with multiple criterias because it forces each condition to be evaluated.

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


// another collection to demonstrate $elemMatch
> db.weather.drop()
> db.weather.insertMany([
 {"_id" : 1, temperatures : [{"station" : "a", temp : 32 }, {"station" : "b", temp : 25 } ]},
 {"_id" : 2, temperatures : [{"station" : "a", temp : 18 }, {"station" : "c", temp : 30 } ]},
 {"_id" : 3, temperatures : [{"station" : "a", temp : 30 }]}
])

// incorrectly finds document id 2 because it matches one condition
> db.weather.find({"temperatures.station" : "a", "temperatures.temp" : 30})
{ "_id" : 2, "temperatures" : [ { "station" : "a", "temp" : 18 }, { "station" : "c", "temp" : 30 } ] }
{ "_id" : 3, "temperatures" : [ { "station" : "a", "temp" : 30 } ] }

// $elemMatch forces both conditions to be evaluated
> db.weather.find({"temperatures" : { $elemMatch : { "station" : "a", "temp" : 30 }}})
{ "_id" : 3, "temperatures" : [ { "station" : "a", "temp" : 30 } ] }
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

As expected, the update functions modifies only matching documents, not changing anything if there are no matches. However,  enabling the *upsert* option allows inserting the data as a new document if no matches are found.



### update

```
// syntax
// db.collection.update(query, update, options)

> db.people.drop()
> db.people.insertMany([ {"_id" : 1, "name" : "john", "age" : 25 },
                         {"_id" : 2, "name" : "peter", "age" : 36 },
                         {"_id" : 3, "name" : "alex", "age" : 36 }
                       ])
> db.people.find()
{ "_id" : 1, "name" : "john", "age" : 25 }
{ "_id" : 2, "name" : "peter", "age" : 36 }
{ "_id" : 3, "name" : "alex", "age" : 36 }

// replacing all fields, except the _id
> db.people.update({"name" : "john"}, {"gender" : "male"})
> db.people.find()
{ "_id" : 1, "gender" : "male" }
{ "_id" : 2, "name" : "peter", "age" : 36 }
{ "_id" : 3, "name" : "alex", "age" : 36 }

// it cannot replace multiple documents
> db.people.update({"age" : "36"}, {"gender" : "male"})
WriteResult({ "nMatched" : 0, "nUpserted" : 0, "nModified" : 0 })

// modifying only specific fields with $set
> db.people.update({"_id" : 1}, { $set : {"name" : "john", "age" : 25}})
> db.people.find({"_id" : 1})
{ "_id" : 1, "gender" : "male", "age" : 25, "name" : "john" }

// upsert enabled
> db.people.update({"name" : "bob"}, { $set : {"age" : 45}}, { "upsert" : true })
> db.people.find({"name" : "bob"})
{ "_id" : ObjectId("5ba3d75056dfe3533fda9c50"), "name" : "bob", "age" : 45 }

// _id cannot be updated
> db.people.update({"name" : "bob"}, { $set : {"_id" : 4}})
WriteResult({
	"nMatched" : 0,
	"nUpserted" : 0,
	"nModified" : 0,
	"writeError" : {
		"code" : 66,
		"errmsg" : "Performing an update on the path '_id' would modify the immutable field '_id'"
	}
})

// increment all ages by 1; multiple updates require the multi option
> db.people.update({}, { $inc : { age : 1 } }, {"multi" : true})
> db.people.find()
{ "_id" : 1, "gender" : "male", "age" : 27, "name" : "john" }
{ "_id" : 2, "name" : "peter", "age" : 37, "group" : "thirties" }
{ "_id" : 3, "name" : "alex", "age" : 37, "group" : "thirties" }
{ "_id" : ObjectId("5ba3d75056dfe3533fda9c50"), "name" : "bob", "age" : 46 }
```

### updateOne

```
// syntax
// db.collection.updateOne(filter, update, options)

// document before update
> db.people.find({"name" : "john"})
{ "_id" : 1, "gender" : "male", "age" : 27, "name" : "john" }

// incrementing the age by 1
> db.people.updateOne({"name" : "john"}, {$inc : { "age" : 1 }})

// document after update
> db.people.find({"name" : "john"})
{ "_id" : 1, "gender" : "male", "age" : 28, "name" : "john" }
```

### updateMany

```
// syntax
// db.collection.updateMany(filter, update, options)

> db.people.updateMany({"age" : { $gte : 30 , $lt : 40 }}, { $set : { "group" : "thirties"}})
> db.people.find()
{ "_id" : 1, "gender" : "male", "age" : 28, "name" : "john" }
{ "_id" : 2, "name" : "peter", "age" : 37, "group" : "thirties" }
{ "_id" : 3, "name" : "alex", "age" : 37, "group" : "thirties" }
{ "_id" : ObjectId("5ba3d75056dfe3533fda9c50"), "name" : "bob", "age" : 46 }
```

### Update Operators


- ***$set*** modifies a field 
- ***$unset*** removes a field
- ***$rename*** renames a field
- ***$inc*** increments a field by a specified number
- ***$mul*** multiplies a field by a specified number
- ***$min*** **and** ***$max*** updates a field only if the field is less/greater than min/max
- ***$setOnInsert*** when the upsert option is enabled, it sets values in case of inserts

```
> db.users.drop()
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

- ***$addToSet*** adds value to array unless it already exists
- ***$pop*** removes the first (-1) or the last (1) item from the array
- ***$pull*** removes items that match a criteria
- ***$pullAll*** removes items that match values from an array
- ***$push*** appends an item to an array
  - ***$each*** allows appending array items into the document's array 
  - ***$slice*** limits the resulting array with the initial/final items using a positive/negative argument
  - ***$sort*** sorts the array
  - ***$position*** location where the data should be appended - requires $each
- **Positional $ operator** identifies an element in the array without specifying the index; the array field name must appear in the query


```
> db.example.drop()
> db.example.insertOne({"_id": 1, "data" : [1, 2, 3, 4]})

// adding an item if it does not exist
> db.example.update({"_id" : 1}, { $addToSet : { "data" : 5 }})
> db.example.find()
{ "_id" : 1, "data" : [ 1, 2, 3, 4, 5 ] }

// modifying an item from an array position
> db.example.update({"_id" : 1}, { $set : { "data.2" : 300 }})
> db.example.find()
{ "_id" : 1, "data" : [ 1, 2, 300, 4, 5 ] }

// adding an array of items with $addToSet requires the $each operator
> db.example.update({"_id" : 1}, { $addToSet : { "data" : { $each : [10,20,30]} }})
> db.example.find()
{ "_id" : 1, "data" : [ 1, 2, 300, 4, 5, 10, 20, 30 ] }

// removing the last item
> db.example.updateOne({"_id" : 1 }, {$pop : { data : 1 }})
> db.example.find()
{ "_id" : 1, "data" : [ 1, 2, 300, 4, 5, 10, 20 ] }

// removing the first item
> db.example.updateOne({"_id" : 1 }, {$pop : { data : -1 }})
> db.example.find()
{ "_id" : 1, "data" : [ 2, 300, 4, 5, 10, 20 ] }

// removing items >= 2 and <= 4
> db.example.updateOne({"_id" : 1 }, {$pull : { data : {$gte : 2, $lte : 4} }})
> db.example.find()
{ "_id" : 1, "data" : [ 300, 5, 10, 20 ] }

// removing items listed in the array
> db.example.updateOne({"_id" : 1 }, {$pullAll : { data : [2, 10] }})
> db.example.find()
{ "_id" : 1, "data" : [ 300, 5, 20 ] }

// appending an item
> db.example.updateOne({"_id" : 1 }, {$push : { data : 8 }})
> db.example.find()
{ "_id" : 1, "data" : [ 300, 5, 20, 8 ] }

// appending an array of items with $each
> db.example.updateOne({"_id" : 1 }, {$push : { data : { $each : [ 17, 12, 31, 5] } }})
> db.example.find()
{ "_id" : 1, "data" : [ 300, 5, 20, 8, 17, 12, 31, 5 ] }

// appends two elements and  keeps only the last five elements in the resulting array
> db.example.updateOne({"_id" : 1 }, {$push : { data : { $each : [100, 200], $slice : -5  } }})
> db.example.find()
{ "_id" : 1, "data" : [ 12, 31, 5, 100, 200 ] }

// appends an item and sort the resulting array in ascending order
> db.example.updateOne({"_id" : 1 }, {$push : { data : { $each : [25], $sort : 1  } }})
> db.example.find()
{ "_id" : 1, "data" : [ 5, 12, 25, 31, 100, 200 ] }

// appends an item, sort the array and keep only the last 4 elements
> db.example.updateOne({"_id" : 1 }, {$push : { data : { $each : [73], $sort : 1, $slice : -4  } }})
> db.example.find()
{ "_id" : 1, "data" : [ 31, 73, 100, 200 ] }

// inserting an item into the index three
> db.example.updateOne({"_id" : 1 }, {$push : { data : {$each : [19], $position : 3  }}})
> db.example.find()
{ "_id" : 1, "data" : [ 31, 73, 100, 19, 200 ] }

// positional $ operator - finding and changing a value from an array
> db.example.updateOne({"_id" : 1, "data" : 73 }, {$set : { "data.$" : 85}})
> db.example.find()
{ "_id" : 1, "data" : [ 31, 85, 100, 19, 200 ] }
```

## Delete

Before release 3.0, deleting documents was performed with the *remove* method, and it deletes everything that matches a criteria. Although *remove* is still supported, it's recommended to use either *deleteOne* or *deleteMany* according to the number of documents that should be deleted. All three methods have a syntax similar to *find*, but they require a query, otherwise they throw an error. In addition to the delete methods, all documents can be removed using the *drop* function, which is preferred to remove all documents from a large collection.


### remove

```
> db.example.drop()
> for(i=1; i<=1000; i++){
    db.example.insertOne({"_id" : i })
  }
 
// deleting rows where the id is greater than 500 
> db.example.remove({"_id" : {$gt : 500}})  
> db.example.count()
500

// deleting all documents
> db.example.remove({})
> db.example.count()
0  
```

### deleteOne and deleteMany

```
> db.example.drop()
> for(i=1; i<=1000; i++){
    db.example.insertOne({"_id" : i })
  }

// deletes only the first matching occurrence
> db.example.deleteOne({"_id" : {$gt : 500}})  
> db.example.count()
999

// deletes all matching documents
> db.example.deleteMany({"_id" : {$gt : 500}})  
> db.example.count()
500

// delete everything
> db.example.deleteMany({})
> db.example.count()
0
```
