# Gitlet Design Document

**Name**: Chenze Fan

## Classes and Data Structures

### Blob Class

#### Variables
1. String sha1-hash, the hash of the file contents 
2. Static File path, the path of the blob folder
3. File filePath, the path of the original file

#### Methods
1. `Blob(String path)`: constructor
2. `String getHash()`: returns the hash of the file contents
3. `Void setHash(String hash)`: sets the hash of the file contents
4. `Void save()`: saves the blob to the blob folder
5. `Blob load(String hash)`: loads the blob from the blob folder
6. `Void delete()`: deletes the blob from the blob folder
7. `Static Void createBlobFolder()`: creates the blob folder


### Repository Class

#### Variables
1. static final File CWD, the current working directory
2. static final File GITLET_FOLDER, the gitlet folder
3. static final File REF_DIR, the reference folder
4. static final File HEAD_FILE, the head file stores head commit hash and branch name
5. static final File STAGE_FILE, the stage file stores the files in the staging area
6. static final File REMOVED_STAGE_FILE, the removed stage file stores the files staged for removal

#### Methods
1. `Static Void init()`: initializes the gitlet folder
2. `Static Void add(String path)`: adds the file to the staging area
3. `Static Void commit(String message)`: commits the files in the staging area
4. `Static Void remove(String path)`: removes the file from the staging area, or stage for removal and delete the original file
5. `Static Void log()`: prints the commit history
6. `Static Void globalLog()`: prints the commit history of all commits
7. `Static Void find(String message)`: prints the commit hash of the commit with the given message
8. `Static Void status()`: prints the status of the repository
9. `Static Void checkoutSingleFile(String fileName)`: checks out the file in head commit
10. `Static Void checkoutSingleFile(String commitHash, String fileName)`: checks out the file in the given commit
11. `Static Void checkoutBranch(String branchName)`: checks out the branch


### Commit Class

#### Variables
1. String sha1-hash, the hash of the commit
2. String message, the commit message
3. String timestamp, the commit timestamp
4. String parent, the parent commit hash
5. HashMap<String,String> blobsMap, keys are the file paths, values are the blob hashes
6. Final File commitPath, the path of the commit file
7. Static final File commitDir, the path of the commit folder

#### Methods
1. `Commit(String message, String parent)`: constructor
2. `Commit()`: constructor for initial commit
3. `String getHash()`: returns the hash of the commit
4. `Void setHash()`: sets the hash of the commit
5. `String getMessage()`: returns the commit message
6. `Void save()`: saves the commit to the commit folder
7. `Static Void createCommitFolder()`: creates the commit folder
8. `Static Commit loadCommit(String hash)`: loads the commit from the commit folder
9. `Void updateBlobsMap(HashMap<String, String> stage)`: updates the blobs map given the stage HashMap
10. `Void deleteBlobsMap(HashMap<String, String> remv)`: deletes the blobs from the blobs map given the remove HashMap


### Branch Class

#### Variables
1. String name, the name of the branch
2. String headCommitHash, the head commit hash of the branch
3. Static final File branchDir, the path of the branch folder
4. Final File branchPath, the path of the branch file

#### Methods
1. `Branch(String name, String head)`: constructor
2. `Static Void createBranchFolder()`: creates the branch folder
3. `Void save()`: saves the branch to the branch folder
4. `Static Void updateHead(String branchName, String commitHash)`: updates the head of the branch
5. `Static String readHead(String branchName)`: reads the head of the branch


## .gitlet Folder Structure

```
.gitlet
├── blobs, stores all the blobs, original file hash is the file name, copy of original file
|   ├── the hash of the file contents
├── commits, stores all the commits, commit hash is the file name, commit object is the content
|   ├── the hash of the commit object
|── refs, the folder that stores all the branches and heads
|   ├── branches, store branches, branch name is the file name, head commit hash is the content
|   ├── HEAD, stores the head commit hash and branch name
|── stage, a HashMap stores the file paths and blob hashes of the files in the staging area
```

