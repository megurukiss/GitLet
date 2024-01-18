package gitlet;

import java.io.File;
public class Branch {
    private String name;
    private String headCommitHash;
    private final File branchPath;
    public static File branchDir = Utils.join(Repository.REF_DIR, "branches");

    public Branch(String name, String headCommitHash) {
        this.name = name;
        this.headCommitHash = headCommitHash;
        this.branchPath = Utils.join(branchDir, name);
    }

    public static void createBranchFolder(){
        if(!branchDir.exists()){
            branchDir.mkdir();
        }
    }

    public void save(){
        if(!branchPath.exists()){
            try{
                branchPath.createNewFile();
            }catch (Exception e){
                System.out.println("Error creating branch file");
            }
        }
        Utils.writeContents(branchPath, headCommitHash);
    }

    public static void updateHead(String branchName, String commitHash){
        File branchPath = Utils.join(branchDir, branchName);
        Utils.writeContents(branchPath, commitHash);
    }

    public static String readHead(String branchName){
        File branchPath = Utils.join(branchDir, branchName);
        // check if branch exists
        if(!branchPath.exists()){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        return Utils.readContentsAsString(branchPath);
    }
}
