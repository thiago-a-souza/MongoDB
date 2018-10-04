# Compiling

From the project root directory, where *pom.xml* was created, generate the jar using the command:

```
mvn package
```

# Running

After the build completes successfully, go to the *target* directory created and run the class desired:

```
java -cp MongoClient.jar FULLY-QUALIFIED-CLASS-NAME
```
