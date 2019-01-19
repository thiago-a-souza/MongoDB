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

All pipeline stages are limited to 100 Mb, and MongoDB throws an error when that limit is exceeded. To overcome this limitation, the *allowDiskUse* option should be enabled. In addition to that, regardless if the aggregation is returning the data or storing the results in a collection, the documents have a maximum size of 16Mb.

Unlike the *explain* in CRUD operations, the aggregate function allows passing *explain* as an option.

**Syntax:**

```
db.collection.aggregate([ { stage1 }, { stage2 }, ..., { stageN } ])

// without array
db.collection.aggregate({ stage1 }, { stage2 }, ..., { stageN } )

// array syntax allows additional options
db.collection.aggregate([ { stage1 }, { stage2 }, ..., { stageN } ], { allowDiskUse : true })

db.collection.aggregate([ { stage1 }, { stage2 }, ..., { stageN } ], { explain : true })
```


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

- **$match**: filters matching documents to limit the documents passing from one stage into another; it's recommended to place  *$match* at the beginning to take advantage of indexes and reduce the number of documents in the pipeline.

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

- **$sort**: returns sorted documents to the next pipeline stage. This stage has a 100 Mb limit, and throws an error when this limit is exceeded.

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

- **$skip and $limit**: useful for paginating results, but unlike the similar functions available in *find*, the order of *$skip* and *$limit* make a difference in the aggregation pipeline.

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

- **$group**: allows grouping documents by a single or compound keys defined by the *_id* field. As a result, the *_id* field is mandatory. All other fields must be accumulators. This stage also has a 100 Mb limit, and throws an error when it is exceeded. Because one stage can be used by the next stage, it's possible to create a double grouping.

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

// compound grouping
> db.movies.aggregate([
...     { $match : { year : { $gt : 2014} } },
...     { $group : {
...             "_id": { year : "$year", rated : "$rated" },
...             "count": { $sum : 1 }
...         }
...     },
...     { $sort : { "_id": -1 } },
... ])
{ "_id" : { "year" : 2018, "rated" : null }, "count" : 1 }
{ "_id" : { "year" : 2016, "rated" : null }, "count" : 8 }
{ "_id" : { "year" : 2015, "rated" : "UNRATED" }, "count" : 1 }
{ "_id" : { "year" : 2015, "rated" : "TV-14" }, "count" : 2 }
{ "_id" : { "year" : 2015, "rated" : "R" }, "count" : 4 }
{ "_id" : { "year" : 2015, "rated" : "PG-13" }, "count" : 3 }
{ "_id" : { "year" : 2015, "rated" : "NOT RATED" }, "count" : 2 }
{ "_id" : { "year" : 2015, "rated" : null }, "count" : 61 }

// double grouping
> db.movies.aggregate([
...     { $match : { year : { $gt : 2014} } },
...     { $group : {
...             "_id": { year : "$year", rated : "$rated" },
...             "count": { $sum : 1 }
...       }
...     },
...     { $group : {
...         "_id" : "$_id.year",
...         "count" : { $sum : "$count" }
...       }
...     },
...     { $sort : { "_id": -1 } }
... ])
{ "_id" : 2018, "count" : 1 }
{ "_id" : 2016, "count" : 8 }
{ "_id" : 2015, "count" : 73 }
```

- **$unwind**: flattens an array and outputs a document for each element in the array so it can be grouped. Obviously, it's possible to unwind a second array, but it should used carefully because the number of documents may grow dramatically.

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

// double unwind
> db.movies.aggregate([
...     { $match : { countries : "USA"} },
...     { $unwind :  "$actors" },
...     { $unwind :  "$writers" },
...     { $group : {
...             _id : {actor : "$actors", writer : "$writers" },
...             count : { $sum : 1 }
...       }
...     },
...     { $sort : { count : -1} }
... ])
{ "_id" : { "actor" : "George Kennedy", "writer" : "David Zucker" }, "count" : 6 }
{ "_id" : { "actor" : "Priscilla Presley", "writer" : "David Zucker" }, "count" : 6 }
{ "_id" : { "actor" : "Tom Hanks", "writer" : "Andrew Stanton" }, "count" : 6 }
{ "_id" : { "actor" : "Louis C.K.", "writer" : "Louis C.K." }, "count" : 6 }

```

- **$text**: the aggregation pipeline can benefit from text indexes as long as *$match* with *$text* is the first pipeline stage, otherwise it throws an error.

```
> db.movies.createIndex({ "actors" : "text" })

> db.movies.aggregate([
...  { $match : { $text : { $search : "Portman Johansson Depardieu" } } },
...  { $sort : { score : { $meta : "textScore" } } },
...  { $limit : 3 },
...  { $project : { _id : 0, "title" : 1, "actors" : 1  } }
... ])
{ "title" : "Tous les matins du monde", "actors" : [ "Jean-Pierre Marielle", "Gérard Depardieu", "Anne Brochet", "Guillaume Depardieu" ] }
{ "title" : "Jean de Florette", "actors" : [ "Yves Montand", "Gérard Depardieu", "Daniel Auteuil", "Elisabeth Depardieu" ] }
{ "title" : "The Other Boleyn Girl", "actors" : [ "Natalie Portman", "Scarlett Johansson", "Eric Bana", "Jim Sturgess" ] }

// error: match should be the first stage
> db.movies.aggregate([
   { $project : { _id : 0, "title": 1, "actors": 1 } },
   { $match : { $text : { $search : "Portman Johansson" } } }
 ])

```

- **$out**: writes to the provided collection the documents returned by the aggregation pipeline. If the collection already exists it overwrites the collection if the operation succeeds (documents are actually inserted into a temporary collection and then renamed to the collection provided). Because it writes documents into a collection, the *_id* field must be unique. This is particularly important when using *unwind* because it will generate duplicate ids.

```
// when _id is excluded in $project, an _id for each document is generated when writing to the collection
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


- **$count**: returns a document with the number of documents from the previous stage. It's the same as using *$group* with *$sum*

```
> db.movies.aggregate([
...     { $match : { "awards.wins" : {$gt : 0 } } },
...     { $count : "awarded movies"}
... ])
{ "awarded movies" : 652 }


> db.movies.aggregate([
...     { $match : { "awards.wins" : {$gt : 0 } } },
...     { $group : { _id : null, "awarded-movies" : { $sum : 1 } } }    
... ])
{ "_id" : null, "awarded-movies" : 652 }
```

- **$replaceRoot**: replaces the current document root with a new document - can also be done manually in the *$project* stage. The newRoot expression requires a document (object) otherwise it throws an error.

```
> db.movies.aggregate([
...     {$replaceRoot : { newRoot : "$imdb"}},
...     {$limit : 4}
... ])
{ "id" : "tt0105226", "rating" : 7, "votes" : 15007 }
{ "id" : "tt0052077", "rating" : 4, "votes" : 29171 }
{ "id" : "tt0117731", "rating" : 7.6, "votes" : 94153 }
{ "id" : "tt0314331", "rating" : 7.7, "votes" : 306036 }
```

- **$addFields**: adds fields to documents, similar to adding fields using the *$project* stage

```
> db.movies.aggregate([
...     { $addFields : { 
...         total : { $sum : [ "$imdb.votes", "$tomato.userReviews" ] }
...       }
...     },
...     { $project : { "_id": 0, "title": 1, "total": 1, "imdb.votes": 1, "tomato.userReviews": 1 } },
...     { $limit : 5 }
... ])
{ "title" : "Red Rock West", "imdb" : { "votes" : 15007 }, "total" : 15007 }
{ "title" : "Plan 9 from Outer Space", "imdb" : { "votes" : 29171 }, "total" : 29171 }
{ "title" : "Star Trek: First Contact", "imdb" : { "votes" : 94153 }, "tomato" : { "userReviews" : 99646 }, "total" : 193799 }
{ "title" : "Love Actually", "imdb" : { "votes" : 306036 }, "tomato" : { "userReviews" : 31625241 }, "total" : 31931277 }
{ "title" : "Shakespeare in Love", "imdb" : { "votes" : 167371 }, "tomato" : { "userReviews" : 225871 }, "total" : 393242 }
```

## Aggregation Pipeline Operators

Each stage supports several operations. Some of them are present in both *$project* and *$group* stages like *$sum*,  *$avg*, *$min*, and *$max*, while others are only available in the *$group* stage such as *$push*, *$addToSet*, *$first*, and *$last*.

- **$sum and $avg**: both can be used in the *$project* and *$group* stages. In the *$project* stage it allows an expression or an array of expressions. For the *$group* stage, it allows only an expression. Regardless the stage used, it ignores non-numeric values, and treat them as zero.

```
// using $sum in the $project stage
> db.movies.aggregate([
...     { $project : {
...         _id : 0,
...         title : 1,
...         myScore : { $sum : ["$tomato.meter", "$metacritic"] } }
        },
...     { $sort : { myScore : -1 }},
...     { $limit : 5}
... ])
{ "title" : "Au Hasard Balthazar", "myScore" : 200 }
{ "title" : "The Wizard of Oz", "myScore" : 199 }
{ "title" : "The Adventures of Robin Hood", "myScore" : 197 }
{ "title" : "The Night of the Hunter", "myScore" : 197 }
{ "title" : "Dr. Strangelove or: How I Learned to Stop Worrying and Love the Bomb", "myScore" : 195 }

// using $sum in the $group stage
> db.movies.aggregate([
...    { $group : {
...            _id : "$year",
...            totalUserReviews : { $sum : "$tomato.userReviews" },
...            totalMovies : { $sum : 1 }
...      }
...    },
...    { $sort : {totalUserReviews : -1 } },
...    { $limit : 5 },
...    { $project : {
...        _id : 0,
...        year : "$_id",
...        totalUserReviews : 1,
...        totalMovies : 1
...        }
...    }
... ])
{ "totalUserReviews" : 101679324, "totalMovies" : 73, "year" : 2005 }
{ "totalUserReviews" : 37552454, "totalMovies" : 65, "year" : 2004 }
{ "totalUserReviews" : 34551101, "totalMovies" : 61, "year" : 2003 }
{ "totalUserReviews" : 32806773, "totalMovies" : 24, "year" : 1990 }
{ "totalUserReviews" : 32446019, "totalMovies" : 15, "year" : 1982 }

> db.movies.aggregate([
...    { $match : { "tomato.reviews" : { $ne : null } } },
...    { $group : {
...        _id : "$year",
...        avgReviews : { $avg : "$tomato.reviews" }
...      } 
...     },
...     { $sort : { _id : -1 } },
...     { $limit : 5},
...     { $project : {
...             _id : 0,
...             year : "$_id",
...             avgReviews : 1
...       }
...     }
...  ])
{ "avgReviews" : 189.28571428571428, "year" : 2015 }
{ "avgReviews" : 110.5, "year" : 2014 }
{ "avgReviews" : 137.52380952380952, "year" : 2013 }
{ "avgReviews" : 129.71428571428572, "year" : 2012 }
{ "avgReviews" : 133.27272727272728, "year" : 2011 }
```


- **$push**: only available in the *$group* stage, it adds items into an array.

```
> db.movies.aggregate([
...     { $match : { year : { $gte : 2013}, director : {$ne : null } } },
...     { $group : {
...          _id : "$director",
...          movies : { $push : "$title"  } 
...       }
...     }
... ])
{ "_id" : "Jay Sin", "movies" : [ "TS Playground 4", "TS Playground 5", "TS Playground 7: Ebony Edition", "TS Playground 6" ] }
{ "_id" : "G.M. Whiting", "movies" : [ "G.M. Whiting's: Notes", "G.M. Whiting's Enemy" ] }
{ "_id" : "Cris D'Amato", "movies" : [ "S.O.S.: Mulheres ao Mar 2", "S.O.S.: Mulheres ao Mar" ] }
```

- **$addToSet**: similar to $push, but only unique items are added.

```
> db.movies.aggregate([
...     { $match : { year : { $gte : 2000}, director : {$ne : null } } },
...     { $group : {
...          _id : "$director",
...          years : { $addToSet : "$year"  } ,
...         }
...     }, 
...     { $project : {
...             _id : 0,
...             director : "$_id",
...             years : 1,
...             length : { $size : "$years"}
...       }
...     },
...     { $sort : { length : -1 } },
...     { $limit : 5 }
... ])
{ "years" : [ 2011, 2009, 2013, 2015, 2010, 2007 ], "director" : "Louis C.K.", "length" : 6 }
{ "years" : [ 2013, 2007, 2002, 2004 ], "director" : "Sam Raimi", "length" : 4 }
{ "years" : [ 2011, 2008, 2013, 2010 ], "director" : "Syamsul Yusof", "length" : 4 }
{ "years" : [ 2003, 2007, 2002, 2005 ], "director" : "Robert Rodriguez", "length" : 4 }
{ "years" : [ 2006, 2008, 2012 ], "director" : "Jack Neo", "length" : 3 }
```



- **$max and $min**: both can be used in the *$project* and *$group* stages. In the *$project* stage it allows an expression or an array of expressions. For the *$group* stage, it allows only an expression. Regardless the stage used, it ignores non-numeric values, and treat them as zero.

```
> db.movies.aggregate([
...     { $match : { "awards" : { $exists : true }  }},
...     { $group : {
...         _id : "$year",
...         max_wins : { $max : "$awards.wins"}
...       }
...     },
...     { $sort : { "_id" : -1 } },
...     { $limit : 5 }
... ])
{ "_id" : 2018, "max_wins" : 0 }
{ "_id" : 2016, "max_wins" : 0 }
{ "_id" : 2015, "max_wins" : 26 }
{ "_id" : 2014, "max_wins" : 59 }
{ "_id" : 2013, "max_wins" : 29 }


> db.example.drop()
> db.example.insertMany([
  { _id : 1, a : [19, 4, 52, 183, 89, 3]},
  { _id : 2, a : [5, 100, 29]},
  { _id : 3, a : [6, 5, 4]},
])
> db.example.aggregate([
...     { $project : {
...            _id : 1,
...            max_a : { $max : "$a" }
...       }
...     }
... ])
{ "_id" : 1, "max_a" : 183 }
{ "_id" : 2, "max_a" : 100 }
{ "_id" : 3, "max_a" : 6 }
```


- **$first and $last**: only available in the *$group* stage, and returns the first/last document in the group. The *$group* stage should be declared after the *sort* stage, so the data makes sense.

```
> db.movies.find({ "director" : "Steven Spielberg" }, { "_id" : 0, "director" : 1, "year" : 1 }).sort({ "year" : 1 })
{ "year" : 1981, "director" : "Steven Spielberg" }
{ "year" : 1982, "director" : "Steven Spielberg" }
{ "year" : 1997, "director" : "Steven Spielberg" }
{ "year" : 2001, "director" : "Steven Spielberg" }
{ "year" : 2002, "director" : "Steven Spielberg" }
{ "year" : 2011, "director" : "Steven Spielberg" }

// sorted in ascending order
> db.movies.aggregate([
...   { $match : { director : "Steven Spielberg" } },
...   { $sort :{ year : 1 } },
...   { $group : {
...       _id : {director : "$director"},
...       firstyear : {$first : "$year"},
...       lastyear : {$last : "$year"}
...     }
...   }
... ])
{ "_id" : { "director" : "Steven Spielberg" }, "firstyear" : 1981, "lastyear" : 2011 }

// sorted in descending order
> db.movies.aggregate([
...   { $match : { director : "Steven Spielberg" } },
...   { $sort :{ year : -1 } },
...   { $group : {
...       _id : {director : "$director"},
...       firstyear : {$first : "$year"},
...       lastyear : {$last : "$year"}
...     }
...   }
... ])
{ "_id" : { "director" : "Steven Spielberg" }, "firstyear" : 2011, "lastyear" : 1981 }
```
