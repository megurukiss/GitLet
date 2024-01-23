package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;
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
        commit.setHash();
        commit.setPath();
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

    public static void commitMerge(String message, String parent1, String parent2){
        // read head commit
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        // Initialize commit object
        Commit commit = new Commit(message, parent1,parent2);
        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        // read removed stage file
        LinkedList<String> removedStage = Utils.readObject(REMOVED_STAGE_FILE, LinkedList.class);
        // update blobsMap in commit object
        commit.updateBlobsMap(stage);
        // save blobs in stage file
        for (String key : stage.keySet()) {
            Blob blob = new Blob(key);
            blob.save();
        }

        // remove blobs in removed stage file
        commit.deleteBlobsMap(removedStage);
        commit.setHash();
        commit.setPath();
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
        LinkedList<String> commits =new LinkedList<>(Utils.plainFilenamesIn(Commit.commitDir));
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
        ArrayList<String> branches =new ArrayList<>(Utils.plainFilenamesIn(Branch.branchDir));
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
        // read head commit
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        Commit headCommitObj= Commit.loadCommit(head.get("head"));
        //check if file exists in head commit
        if(!headCommitObj.getBlobsMap().containsKey(fileName)){
            // if not, print error File does not exist in that commit.
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        // if yes, overwrite file in working directory
        String blobHash = headCommitObj.getBlobsMap().get(fileName);
        //read file and write to original file
        File file = join(CWD, fileName);
        File blobFile = join(Blob.blobDir, blobHash);
        String content = Utils.readContentsAsString(blobFile);
        Utils.writeContents(file, content);
        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        // check if file is in stage file
        if(stage.containsKey(fileName)){
            // if yes, delete file from stage file
            stage.remove(fileName);
            Utils.writeObject(Stage_FILE, stage);
        }
    }

    /** If a working file is untracked in the current branch and would be overwritten
     * by the checkout, print There is an untracked file in the way;
     * delete it, or add and commit it first. and exit
     * Support abbreviated version of commit hash */
    public static void checkoutSingleFile(String commitHash, String fileName){
        //read commit
        Commit commitObj = Commit.loadCommit(commitHash);
        //check if file exists in commit
        if(!commitObj.getBlobsMap().containsKey(fileName)){
            // if not, print error File does not exist in that commit.
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        // read file and write to original file
        File file = join(CWD, fileName);
        File blobFile = join(Blob.blobDir, commitObj.getBlobsMap().get(fileName));
        String content = Utils.readContentsAsString(blobFile);
        Utils.writeContents(file, content);
        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        // check if file is in stage file
        if(stage.containsKey(fileName)){
            // if yes, delete file from stage file
            stage.remove(fileName);
            Utils.writeObject(Stage_FILE, stage);
        }
    }

    public static void checkoutBranch(String branchName){
        // read branch file
        String headCommitHash = Branch.readHead(branchName);
        // read head file
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        // check if branch is current branch
        if(head.get("branch").equals(branchName)){
            // if yes, print error No need to check out the current branch.
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        // read branch head commit
        Commit branchCommitObj = Commit.loadCommit(headCommitHash);
        // read commit blob map
        HashMap<String, String> blobsMap = branchCommitObj.getBlobsMap();

        // create queue
        Queue<File> queue = new LinkedList<>();
        // push CWD to queue
        queue.add(CWD);
        // iterate through queue
        while(!queue.isEmpty()){
            // pop file from stack
            File folder = queue.poll();
            File[] listFiles = folder.listFiles();
            for (File f : listFiles){
                // check if a folder
                if(f.isDirectory()){
                    if(f.getName().equals(".gitlet")){
                        continue;
                    }
                    // push folder to queue
                    queue.add(f);
                }
                else{
                    String relativePath=CWD.toPath().relativize(f.toPath()).toString();
                    if(!blobsMap.containsKey(relativePath)){
                        try {
                            Files.delete(f.toPath());
                        }catch (IOException ex){
                            ex.printStackTrace();
                        }
                    }else{
                        File F = join(CWD, relativePath);
                        File blobFile = join(Blob.blobDir, blobsMap.get(relativePath));
                        String content = Utils.readContentsAsString(blobFile);
                        Utils.writeContents(F, content);
                    }
                }
            }
            // check if the current folder is empty
            if(folder.listFiles().length == 0){
                try {
                    Files.delete(folder.toPath());
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }

        /**
        // walk files in CWD, check if file is in commit blob map. if not, delete file. if yes, overwrite file
        try{
            Stream<Path> stream = Files.walk(CWD.toPath());
            stream.forEach(path -> {
                //String fileName = path.getFileName().toString();
                String relativePath=CWD.toPath().relativize(path).toString();
                if(!blobsMap.containsKey(relativePath)){
                    try {
                        Files.delete(path);
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }else{
                    File file = join(CWD, relativePath);
                    File blobFile = join(Blob.blobDir, blobsMap.get(relativePath));
                    String content = Utils.readContentsAsString(blobFile);
                    Utils.writeContents(file, content);
                }
            });
        }catch (IOException ex){
            ex.printStackTrace();
        }*/
        // clear stage file
        Utils.writeObject(Stage_FILE, new HashMap<String,String>());
        // clear removed stage file
        Utils.writeObject(REMOVED_STAGE_FILE, new LinkedList<String>());
        // update head file
        head.put("branch", branchName);
        head.put("head", headCommitHash);
        Utils.writeObject(HEAD_FILE, head);
    }

    public static void branch(String branchName){
        // check if branch exists
        File branchFile = join(Branch.branchDir, branchName);
        if(branchFile.exists()){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        // read head file
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        // create branch file
        Branch branch = new Branch(branchName, head.get("head"));
        branch.save();
    }

    public static void removeBranch(String branchName){
        // check if branch exists
        File branchFile = join(Branch.branchDir, branchName);
        if(!branchFile.exists()){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        // read head file
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        // check if branch is current branch
        if(head.get("branch").equals(branchName)){
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        // delete branch file
        branchFile.delete();
    }

    public static void reset(String commitHash){
        // read commit
        Commit commitObj = Commit.loadCommit(commitHash);
        // read head
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        // read current branch
        String branch = head.get("branch");
        // save commitHash as current branch and head commit hash
        Branch.updateHead(branch, commitHash);
        head.put("head", commitHash);
        Utils.writeObject(HEAD_FILE, head);
        // read commit blob map
        HashMap<String, String> blobsMap = commitObj.getBlobsMap();
        // walk files in CWD, check if file is in commit blob map. if not, delete file. if yes, overwrite file
        // use queue to replace forEach
        // create queue
        Queue<File> queue = new LinkedList<>();
        // push CWD to queue
        queue.add(CWD);
        // iterate through queue
        while(!queue.isEmpty()){
            // pop file from stack
            File folder = queue.poll();
            File[] listFiles = folder.listFiles();
            for (File f : listFiles){
                // check if a folder
                if(f.isDirectory()){
                    if(f.getName().equals(".gitlet")){
                        continue;
                    }
                    // push folder to queue
                    queue.add(f);
                }
                else{
                    String relativePath=CWD.toPath().relativize(f.toPath()).toString();
                    if(!blobsMap.containsKey(relativePath)){
                        try {
                            Files.delete(f.toPath());
                        }catch (IOException ex){
                            ex.printStackTrace();
                        }
                    }else{
                        File F = join(CWD, relativePath);
                        File blobFile = join(Blob.blobDir, blobsMap.get(relativePath));
                        String content = Utils.readContentsAsString(blobFile);
                        Utils.writeContents(F, content);
                        blobsMap.remove(relativePath);
                    }
                }
            }
            // check if the current folder is empty
            if(folder.listFiles().length == 0){
                try {
                    Files.delete(folder.toPath());
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
        // iterate through commit blob map
        for (String key : blobsMap.keySet()) {
            String BlobHash = blobsMap.get(key);
            // read file and write to original file
            File file = join(CWD, key);
            File blobFile = join(Blob.blobDir, BlobHash);
            String content = Utils.readContentsAsString(blobFile);
            //create file if not exists
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    System.out.println("Error creating file");
                }
            }
            Utils.writeContents(file, content);
        }
        /**
        try{
            Stream<Path> stream = Files.walk(CWD.toPath());
            stream.forEach(path -> {
                // skip folders and files in .gitlet folder
                if(path.toString().contains("/.") || path.toString().){
                    return;
                }
                //String fileName = path.getFileName().toString();
                String relativePath=CWD.toPath().relativize(path).toString();
                if(!blobsMap.containsKey(relativePath)){
                    try {
                        Files.delete(path);
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }else{
                    File file = join(CWD, relativePath);
                    File blobFile = join(Blob.blobDir, blobsMap.get(relativePath));
                    String content = Utils.readContentsAsString(blobFile);
                    Utils.writeContents(file, content);
                }
            });
        }catch (IOException ex){
            ex.printStackTrace();
        }
         */
        Utils.writeObject(Stage_FILE, new HashMap<String,String>());
        // clear removed stage file
        Utils.writeObject(REMOVED_STAGE_FILE, new LinkedList<String>());
    }

    public static void merge(String branchName) {
        Boolean conflict = false;
        // read branch head commit
        String branchHeadCommitHash = Branch.readHead(branchName);
        // read head commit
        HashMap<String, String> head = Utils.readObject(HEAD_FILE, HashMap.class);
        String headCommitHash = head.get("head");
        String curBranch = head.get("branch");
        // get split point
        String splitPointHash = Commit.searchSplit(headCommitHash, branchHeadCommitHash);
        // check if split point is current head or branch head
        if (splitPointHash.equals(branchHeadCommitHash)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPointHash.equals(headCommitHash)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        // read split point commit
        Commit splitPointCommitObj = Commit.loadCommit(splitPointHash);
        HashMap<String, String> splitPointBlobsMap = splitPointCommitObj.getBlobsMap();
        // read branch commit
        Commit branchCommitObj = Commit.loadCommit(branchHeadCommitHash);
        HashMap<String, String> branchBlobsMap = branchCommitObj.getBlobsMap();
        // read head commit
        Commit headCommitObj = Commit.loadCommit(headCommitHash);
        HashMap<String, String> headBlobsMap = headCommitObj.getBlobsMap();

        // read stage file
        HashMap<String, String> stage = Utils.readObject(Stage_FILE, HashMap.class);
        LinkedList<String> removedStage = Utils.readObject(REMOVED_STAGE_FILE, LinkedList.class);

        // iterate through split point commit blobs map
        for (String key : splitPointBlobsMap.keySet()) {
            String BlobHash = splitPointBlobsMap.get(key);
            // check if file is modified in the given branch and also in the current branch
            if (branchBlobsMap.containsKey(key) && headBlobsMap.containsKey(key)) {
                // check if file is modified in the given branch but not in the current branch
                if (!branchBlobsMap.get(key).equals(BlobHash) && headBlobsMap.get(key).equals(BlobHash)) {
                    File file = join(CWD, key);
                    File blobFile = join(Blob.blobDir, branchBlobsMap.get(key));
                    String content = Utils.readContentsAsString(blobFile);
                    Utils.writeContents(file, content);
                    stage.put(key, branchBlobsMap.get(key));
                }
                // file modification conflicts
                else if (!branchBlobsMap.get(key).equals(BlobHash) && !headBlobsMap.get(key).equals(BlobHash) && !branchBlobsMap.get(key).equals(headBlobsMap.get(key))) {
                    conflict = true;
                    File file = join(CWD, key);
                    File blobFile1 = join(Blob.blobDir, branchBlobsMap.get(key));
                    File blobFile2 = join(Blob.blobDir, headBlobsMap.get(key));
                    String content1 = Utils.readContentsAsString(blobFile1);
                    String content2 = Utils.readContentsAsString(blobFile2);
                    Utils.writeContents(file, "<<<<<<< HEAD\n" + content2 + "=======\n" + content1 + ">>>>>>>\n");
                    stage.put(key, branchBlobsMap.get(key));
                }
            }
            //Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed (and untracked).
            else if (!branchBlobsMap.containsKey(key) && headBlobsMap.containsKey(key) && headBlobsMap.get(key).equals(BlobHash)) {
                File file = join(CWD, key);
                file.delete();
                removedStage.add(key);
            }
            // remove from branch blobs map and head blobs map
            branchBlobsMap.remove(key);
//            headBlobsMap.remove(key);
        }

        // iterate through branch commit blobs map
        for (String key : branchBlobsMap.keySet()) {
            String BlobHash = branchBlobsMap.get(key);
            // Any files that were not present at the split point and are present only in the given branch should be checked out and staged.
            if(!headBlobsMap.containsKey(key)){
                File file = join(CWD, key);
                File blobFile = join(Blob.blobDir, BlobHash);
                String content = Utils.readContentsAsString(blobFile);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                        System.out.println("Error creating file");
                    }
                }
                Utils.writeContents(file, content);
                stage.put(key, BlobHash);
            }
            else if(!headBlobsMap.get(key).equals(BlobHash)){
                conflict = true;
                File file = join(CWD, key);
                File blobFile1 = join(Blob.blobDir, branchBlobsMap.get(key));
                File blobFile2 = join(Blob.blobDir, headBlobsMap.get(key));
                String content1 = Utils.readContentsAsString(blobFile1);
                String content2 = Utils.readContentsAsString(blobFile2);
                Utils.writeContents(file, "<<<<<<< HEAD\n" + content2 + "=======\n" + content1 + ">>>>>>>\n");
                stage.put(key, branchBlobsMap.get(key));
            }
        }
        // save stage file
        Utils.writeObject(Stage_FILE, stage);
        // save removed stage file
        Utils.writeObject(REMOVED_STAGE_FILE, removedStage);

        //commit
        if(conflict){
            System.out.println("Encountered a merge conflict.");
        }
        else{
            commitMerge("Merged " + branchName + " into " + curBranch + ".", headCommitHash, branchHeadCommitHash);
        }
    }
}
