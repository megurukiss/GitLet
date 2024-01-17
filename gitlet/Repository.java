package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.text.SimpleDateFormat;
import java.util.*;
// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    // reference directory
    public static final File REF_DIR = join(GITLET_DIR, "refs");
    // head file
    public static final File HEAD_FILE = join(REF_DIR, "HEAD");
    public static final File Stage_FILE = join(GITLET_DIR, "stage");
    public static final File REMOVED_STAGE_FILE = join(GITLET_DIR, "removed_stage");

    /* TODO: fill in the rest of this class. */

    public static void init(){
        // create folders and files
        if(GITLET_DIR.exists()){
            System.out.println("A gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        REF_DIR.mkdir();
        Blob.createBlobFolder();
        Commit.createCommitFolder();
        try{
            Stage_FILE.createNewFile();
            REMOVED_STAGE_FILE.createNewFile();
        }catch (Exception e){
            System.out.println("Error creating stage file");
        }

        // create initial commit
        Commit initialCommit = new Commit();
        initialCommit.save();
        // create head file
        try{
            HEAD_FILE.createNewFile();
        }catch (Exception e){
            System.out.println("Error creating head file");
        }
        // write initial commit hash value to head file
        HashMap<String, String> head = new HashMap<>();
        head.put("branch", "master");
        head.put("head", initialCommit.getHash());
        Utils.writeObject(HEAD_FILE, head);
        // create master branch and save initial commit hash value
        Branch.createBranchFolder();
        Branch master = new Branch("master", initialCommit.getHash());
        master.save();
        // create empty hashmap and save to stage file
        HashMap<String, String> stage = new HashMap<>();
        Utils.writeObject(Stage_FILE, stage);
        // create empty Queque and save to removed stage file
        LinkedList<String> removedStage = new LinkedList<>();
        Utils.writeObject(REMOVED_STAGE_FILE, removedStage);
    }

    public static void add(String path){
        File file = join(CWD, path);
        // check if file exists
        if(!file.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        // create blob for file
        Blob blob=new Blob(path);
        String blobHash = blob.getHashValue();
        // read head commit
        String headCommit = (String) Utils.readObject(HEAD_FILE, HashMap.class).get("head");
//        File headCommitFile = join(Commit.commitDir, headCommit);
//        Commit headCommitObj = Utils.readObject(headCommitFile, Commit.class);
        Commit headCommitObj= Commit.loadCommit(headCommit);
        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        // check if file is same as head commit by blob hash value
        if(headCommitObj.getBlobsMap().containsKey(path) && headCommitObj.getBlobsMap().get(path).equals(blobHash)) {
            // if same, delete from stage file if exists in stage file
            if (stage.containsKey(path)) {
                stage.remove(path);
                Utils.writeObject(Stage_FILE, stage);
            }else{
                System.out.println("File has not been modified since the last commit.");
                System.exit(0);
            }
        }else{
            // if not same, add to stage file, overwrite if exists in stage file
            stage.put(path, blobHash);
            Utils.writeObject(Stage_FILE, stage);
//            blob.save();
        }
    }

    public static void commit(String message){
        // read head commit
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        // Initialize commit object
        Commit commit = new Commit(message, head.get("head"));
        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        // read removed stage file
        LinkedList<String> removedStage = Utils.readObject(REMOVED_STAGE_FILE, LinkedList.class);
        // check if stage file is empty and removed stage file is empty
        if(stage.isEmpty() && removedStage.isEmpty()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // update blobsMap in commit object
        commit.updateBlobsMap(stage);
        // save blobs in stage file
        for (String key : stage.keySet()) {
            Blob blob = new Blob(key);
            blob.save();
        }

        // remove blobs in removed stage file
        commit.deleteBlobsMap(removedStage);
        //clear stage file
        Utils.writeObject(Stage_FILE, new HashMap<String,String>());
        // clear removed stage file
        Utils.writeObject(REMOVED_STAGE_FILE, new LinkedList<String>());
        // update head commit hash value in head file
        head.put("head", commit.getHash());
        Utils.writeObject(HEAD_FILE, head);
        // update branch head commit hash value in branch file
        Branch.updateHead(head.get("branch"), commit.getHash());
        // save commit object
        commit.save();
    }

    public static void remove(String path){
        // read head commit
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        Commit headCommitObj= Commit.loadCommit(head.get("head"));
        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        // read removed stage file
        LinkedList<String> removedStage = Utils.readObject(REMOVED_STAGE_FILE, LinkedList.class);
        // check if file is in stage file
        if(stage.containsKey(path)){
            // remove file from stage file
            stage.remove(path);
            Utils.writeObject(Stage_FILE, stage);
            //exit
            System.exit(0);
        }

        // check if file is in head commit
        else if(headCommitObj.getBlobsMap().containsKey(path)){
            // add file to removed stage file
            removedStage.add(path);
            // delete file from working directory
            File file = join(CWD, path);
            file.delete();
        }
        // else, print error message
        else{
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /** modify for merge command */
    public static void log(){
        // read head commit
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        Commit headCommitObj= Commit.loadCommit(head.get("head"));

        // trace back to parents
        while(true){
            // print commit information
            System.out.println("===");
            System.out.println("commit " + headCommitObj.getHash());
            // generate date string
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            sdf.setTimeZone(TimeZone.getTimeZone("PST"));
            String formattedDate = sdf.format(headCommitObj.getDate());

            System.out.println("Date: " + formattedDate);
            System.out.println(headCommitObj.getMessage());
            System.out.println();

            // check if parent commit exists
            if(headCommitObj.getParentHashValue().equals("null")){
                break;
            }
            // read parent commit
            headCommitObj = Commit.loadCommit(headCommitObj.getParentHashValue());
        }
    }

    public static void globalLog(){
        LinkedList<String> commits =(LinkedList<String>) Utils.plainFilenamesIn(Commit.commitDir);
        for(String commit: commits){
            Commit commitObj = Commit.loadCommit(commit);
            // print commit information
            System.out.println("===");
            System.out.println("commit " + commitObj.getHash());
            // generate date string
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            sdf.setTimeZone(TimeZone.getTimeZone("PST"));
            String formattedDate = sdf.format(commitObj.getDate());

            System.out.println("Date: " + formattedDate);
            System.out.println(commitObj.getMessage());
            System.out.println();
        }
    }

    public static void find(String message){
        LinkedList<String> commits =(LinkedList<String>) Utils.plainFilenamesIn(Commit.commitDir);
        boolean found = false;
        for(String commit: commits){
            Commit commitObj = Commit.loadCommit(commit);
            if(commitObj.getMessage().equals(message)){
                System.out.println(commitObj.getHash());
                found = true;
            }
        }
        if(!found){
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }
    /** section Modifications Not Staged For Commit, Untracked Files  */
    public static void status(){
        // read head commit
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        Commit headCommitObj= Commit.loadCommit(head.get("head"));
        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        // read removed stage file
        LinkedList<String> removedStage = Utils.readObject(REMOVED_STAGE_FILE, LinkedList.class);
        // read branch file
        String branch = head.get("branch");
//        String headCommitHash = Branch.readHead(branch);
//        Commit branchCommitObj = Commit.loadCommit(headCommitHash);
        // read all commits
//        LinkedList<String> commits =(LinkedList<String>) Utils.plainFilenamesIn(Commit.commitDir);
        // read all branches
        LinkedList<String> branches =(LinkedList<String>) Utils.plainFilenamesIn(Branch.branchDir);
        // read all blobs
//        LinkedList<String> blobs =(LinkedList<String>) Utils.plainFilenamesIn(Blob.blobDir);

        // print branch
        System.out.println("=== Branches ===");
        for(String b: branches){
            if(b.equals(branch)){
                System.out.println("*" + b);
            }else{
                System.out.println(b);
            }
        }
        System.out.println();

        // print staged files
        System.out.println("=== Staged Files ===");
        for(String s: stage.keySet()){
            System.out.println(s);
        }
        System.out.println();

        // print removed files
        System.out.println("=== Removed Files ===");
        for(String r: removedStage){
            System.out.println(r);
        }
        System.out.println();

        // print modifications not staged for commit
//        System.out.println("=== Modifications Not Staged For Commit ===");
//        // check if file is modified
//        for(String b: blobs){
//            // check if file is in head commit
//            if(headCommitObj.getBlobsMap().containsKey(b)){
//                // check if file is in stage file
//                if(stage.containsKey(b)){
//                    // check if file is modified
//                    if(!headCommitObj.getBlobsMap().get(b).equals(stage.get(b))){
//                        System.out.println(b + " (modified)");
//                    }
//                }else{
//                    // check if file is modified
//                    if(!headCommitObj.getBlobsMap().get(b).equals(Blob.loadBlob(b).getHashValue())){
//                        System.out.println(b + " (modified)");
//                    }
    }

    public static void checkoutSingleFile(String fileName){

    }

    public static void checkoutCommit(String commitHash, String fileName){

    }

    public static void checkoutBranch(String branchName){

    }

}
