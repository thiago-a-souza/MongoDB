# Author

Thiago Alexandre Domingues de Souza

# Table of Contents

- [NoSQL](./01-NoSQL.md)
- [MongoDB](./02-MongoDB.md)
- [Mongo Shell](./03-Mongo%20Shell.md)
- [CRUD](./04-CRUD.md)
- [Indexes](./05-Indexes.md)     
- [Data Modeling](./06-Data%20Modeling.md)
- **[Aggregation](#aggregation)**
  * **[Aggregation Pipeline Stages](#aggregation-pipeline-stages)**
  * **[Aggregation Pipeline Operators](#aggregation-pipeline-operators)**  
- [Replication](./08-Replication.md)
- [Sharding](./09-Sharding.md)
- [Server Tools](./10-Server%20Tools.md)
- [Storage Engines](./11-Storage%20Engines.md)
- [References](./README.md#references)

# Aggregation

The aggregation framework provided by MongoDB is similar to the concept of Unix pipes, in which each stage produces an output that is used as input to the next stage. As a result, the same type of stage can be used multiple times in the same pipeline.

## Aggregation Pipeline Stages

- **$project**: allows including/excluding fields, modifying values or the document structure.

```
// excluding _id, changing title to uppercase, renaming year, and reshaping the document 
> db.movies.aggregate([
... { $project: {
...         "_id": 0,
...         "title": { $toUpper : "$title" },
...         "yr": "$year",
...         "cast": {
...             "writers": "$writers",
...             "actors": "$actors"
...         }
...   }
... }
... ]).pretty()
{
	"title" : "RED ROCK WEST",
	"yr" : 1993,
	"cast" : {
		"writers" : [
			"John Dahl",
			"Rick Dahl"
		],
		"actors" : [
			"Nicolas Cage",
			"Dennis Hopper",
			"Lara Flynn Boyle",
			"J.T. Walsh"
		]
	}
}
```

- **$match**:

```
> db.movies.aggregate([
...     { $match : { "year": { $gt : 2015 } } },
...     { $project: {
...             "_id": 0,
...             "title": 1,
...             "year": 1
...       }
...     }
... ])
{ "title" : "Ég Man Þig", "year" : 2016 }
{ "title" : "Untitled J.J. Abrams/Chris Alender Sci-Fi Project", "year" : 2016 }
{ "title" : "The Duke: Based on the Memoir 'I'm the Duke' by J.P. Duke", "year" : 2016 }
{ "title" : "Elf ll", "year" : 2016 }
{ "title" : "MF", "year" : 2016 }
{ "title" : "Que Pena Tu Vida Mx", "year" : 2016 }
{ "title" : "2 GB Ki Life", "year" : 2016 }
{ "title" : "QQ Speed", "year" : 2018 }
{ "title" : "JL Ranch", "year" : 2016 }
```

- **$sort**:

```
> db.movies.aggregate([
...     { $match : { "year": { $gt : 2015 } } },
...     { $sort : { "year": -1, "title": 1 } },
...     { $project: {
...             "_id": 0,
...             "title": 1,
...             "year": 1
...       }
...     }
... ])
{ "title" : "QQ Speed", "year" : 2018 }
{ "title" : "2 GB Ki Life", "year" : 2016 }
{ "title" : "Elf ll", "year" : 2016 }
{ "title" : "JL Ranch", "year" : 2016 }
{ "title" : "MF", "year" : 2016 }
{ "title" : "Que Pena Tu Vida Mx", "year" : 2016 }
{ "title" : "The Duke: Based on the Memoir 'I'm the Duke' by J.P. Duke", "year" : 2016 }
{ "title" : "Untitled J.J. Abrams/Chris Alender Sci-Fi Project", "year" : 2016 }
{ "title" : "Ég Man Þig", "year" : 2016 }
```

- **$skip and $limit**:

```
> db.movies.aggregate([
...     { $match : { "year": { $gt : 2015 } } },
...     { $sort : { "year": -1, "title": 1 } },
...     { $skip : 2 },
...     { $limit : 3 },
...     { $project: {
...             "_id": 0,
...             "title": 1,
...             "year": 1
...       }
...     }
... ])
{ "title" : "Elf ll", "year" : 2016 }
{ "title" : "JL Ranch", "year" : 2016 }
{ "title" : "MF", "year" : 2016 }

// inverting the order of $skip and $limit
> db.movies.aggregate([
...     { $match : { "year": { $gt : 2015 } } },
...     { $sort : { "year": -1, "title": 1 } },
...     { $limit : 3 },
...     { $skip : 2 },
...     { $project: {
...             "_id": 0,
...             "title": 1,
...             "year": 1
...       }
...     }
... ])
{ "title" : "Elf ll", "year" : 2016 }
```

- **$group**:

```
> db.movies.aggregate([
...     { $group : { "_id": "$year", "count": { $sum : 1 } } },
...     { $sort : { "_id": -1 } },
...     { $limit : 4 },
...     { $project: {
...             "_id" : 0,
...             "year": "$_id",
...             "count": 1
...       }
...     }
... ])
{ "count" : 1, "year" : 2018 }
{ "count" : 8, "year" : 2016 }
{ "count" : 73, "year" : 2015 }
{ "count" : 100, "year" : 2014 }
```

- **$unwind**:

```
> db.movies.aggregate([
... { $unwind : "$actors" },
... { $group : { "_id" : "$actors", "sum" : { $sum : 1 } } },
... { $sort : { "sum" : -1, "_id" : 1} },
... { $limit : 5 },
... { $project: {
...             "_id" : 0,
...             "actors": "$_id",
...             "sum": 1
...         }
...   }
... ])
{ "sum" : 8, "actors" : "Louis C.K." }
{ "sum" : 8, "actors" : "Natalie Portman" }
{ "sum" : 8, "actors" : "Tom Hanks" }
{ "sum" : 7, "actors" : "B.B. King" }
{ "sum" : 7, "actors" : "Ewan McGregor" }
```

- **$text**:

```
> db.movies.createIndex({ "actors" : "text" })

> db.movies.aggregate([
...     { $match : { $text : { $search : "Portman Johansson Depardieu" } } },
...     { $sort : { title : -1 } },
...     { $limit : 3 },
...     { $project : { _id : 0, "title" : 1, "actors" : 1  } }
... ])
{ "title" : "Where the Heart Is", "actors" : [ "Natalie Portman", "Ashley Judd", "Stockard Channing", "Joan Cusack" ] }
{ "title" : "Thor: The Dark World", "actors" : [ "Chris Hemsworth", "Natalie Portman", "Tom Hiddleston", "Anthony Hopkins" ] }
{ "title" : "The Other Woman", "actors" : [ "Natalie Portman", "Scott Cohen", "Lisa Kudrow", "Charlie Tahan" ] }
```

- **$out**:

```
> db.movies.aggregate([
...     { $unwind : "$actors" },
...     { $group : { "_id" : "$actors", "sum" : { $sum : 1 } } },
...     { $project: {
...                 "_id" : 0,
...                 "actors": "$_id",
...                 "sum": 1
...             }
...     },
...     { $out :  "actors_freq"}
... ])

> db.actors_freq.find( { }, {_id : 0}).sort({sum : -1 }).limit(5)
{ "sum" : 8, "actors" : "Louis C.K." }
{ "sum" : 8, "actors" : "Natalie Portman" }
{ "sum" : 8, "actors" : "Tom Hanks" }
{ "sum" : 7, "actors" : "B.B. King" }
{ "sum" : 7, "actors" : "Ewan McGregor" }
```

## Aggregation Pipeline Operators

- **$sum**:
- **$avg**:
- **$addToSet**:
- **$max and $min**:
