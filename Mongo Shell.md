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
