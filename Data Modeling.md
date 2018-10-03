# Author
Thiago Alexandre Domingues de Souza

# Table of Contents

- [Data Modeling](#data-modeling)    
  * [Relationships](#relationships)
    * [One-to-One](#one-to-one)
    * [One-to-Many](#one-to-many)
    * [Many-to-Many](#many-to-many)  
  * [Tree Structures](#tree-structures)
  * [GridFS](#tree-structures)
  * [Views](#views)
  * [Collations](#collations)  
  * [NumberDecimal](#numberdecimal)  


# Data Modeling

The normalization process used by relational databases guarantee that the data is consistent across different tables. Among other rules, it does not allow multivalued fields nor nested relations, so the data must be stored in separate tables. This approach has a significant impact on performance because typical applications have to join several tables to pull all information needed. NoSQL databases, such as MongoDB, do not follow these rules and can benefit from embedded documents/arrays to avoid pulling data from other sources. To enforce this approach, MongoDB does not support foreign keys or joins. As a result, a document referencing another document (constraints in the SQL world), must be handled at the application level, in other words, the database will not ensure that the reference exists and does not join documents.

Initially, atomic operations were supported at the document level, and it could not ensure that multi-document changes were either committed or rolled back. In fact, transactions on a document level can cover most demands for data integrity, since embedded documents and arrays keep in the same document the data that would be stored in different sources. However, some circumstances still require ACID transactions, and developers had to handle them at the application level. With the release 4.0, MongoDB introduced multi-document transactions to solve this problem.

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
## Views
## Collations
## NumberDecimal
