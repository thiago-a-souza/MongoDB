package com.thiago;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.bson.types.ObjectId;

public class LoadGridFS {
    public static void main(String args[]) throws FileNotFoundException {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase db = mongoClient.getDatabase("mydb");

        GridFSBucket gridFSBucket = GridFSBuckets.create(db);

        InputStream streamToUploadFrom = new FileInputStream(new File("/path/to/file"));

        ObjectId id = gridFSBucket.uploadFromStream("my-large-file", streamToUploadFrom);

        System.out.println("_id : " + id);

    }
}

