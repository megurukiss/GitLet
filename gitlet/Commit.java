package gitlet;

// TODO: any imports you need here

import java.util.*;
import java.io.File;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date date;
    private String hashValue;
    private String parentHashValue;
//    private String[] blobs;
    // hashmap, key: filepath, value: blob hash value
    private HashMap<String, String> blobsMap;
    private final File commitPath;
    public static final File commitDir = Utils.join(Repository.GITLET_DIR, "commits");

    /* TODO: fill in the rest of this class. */

    // constructor for normal commit
    public Commit(String message,String parent) {
        // normal commit
        this.message = message;
        this.date = new Date();
        this.parentHashValue = parent;
        // copy blobs from parents commit
        this.blobsMap = new HashMap<>(loadCommit(parent).getBlobsMap());
        this.setHash();
        this.commitPath = Utils.join(commitDir, this.hashValue);
    }

    // constructor for initial commit
    public Commit(){
        // initial commit
        this.message = "initial commit";
        this.date = new Date(0);
        this.parentHashValue = "null";
        this.blobsMap = new HashMap<>();
        this.setHash();
        this.commitPath = Utils.join(commitDir, this.hashValue);
    }

    // create commit folder for initial commit
    public static void createCommitFolder(){
        if(!commitDir.exists()){
            commitDir.mkdir();
        }
    }

    // calculate hash value
    public void setHash(){
        // set hash value
        List<Object> inputList=new ArrayList<Object>();
        inputList.add(this.message);
        inputList.add(this.date.toString());
        inputList.add(this.parentHashValue);

        for (String key : this.blobsMap.keySet()) {
            inputList.add(key);
            inputList.add(this.blobsMap.get(key));
        }

        this.hashValue = Utils.sha1((List<Object>) inputList);
    }

    public String getHash() {
        return hashValue;
    }

    public String getParentHashValue() {
        return parentHashValue;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, String> getBlobsMap() {
        return blobsMap;
    }

    // load commit from commit folder given hash value
    public static Commit loadCommit(String hash){
        File commitFile = Utils.join(commitDir, hash);
        if(!commitFile.exists()){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return Utils.readObject(commitFile, Commit.class);
    }

    // update blobsMap given hashmap from stage file
    public void updateBlobsMap(HashMap<String, String> stage){
        // update blobsMap
        for (String key : stage.keySet()) {
            blobsMap.put(key, stage.get(key));
        }
    }

    // delete blobs given hashmap from removed stage file
    public void deleteBlobsMap(LinkedList<String> remv){
        // delete blobs
        for (String key : remv) {
            blobsMap.remove(key);
        }
    }

    // save commit to commit folder
    public void save(){
        // save commit to commit folder
        if(!commitPath.exists()){
            try{
                commitPath.createNewFile();
            }catch (Exception e){
                System.out.println("Error creating commit file");
            }
        }
        Utils.writeObject(commitPath, this);
    }
}
