package gitlet;

import java.io.File;
public class Blob{
    public static File blobDir = Utils.join(Repository.GITLET_DIR, "blobs");
    private String hashValue;
    private final File filePath;
    private final File blobFile;
    public Blob(String fileName) {
        filePath = Utils.join(Repository.CWD, fileName);
        setHashValue();
        blobFile = Utils.join(blobDir, hashValue);
    }

    public static void createBlobFolder(){
        if(!blobDir.exists()){
            blobDir.mkdir();
        }
    }

    public String getHashValue() {
        return hashValue;
    }

    private void setHashValue() {
        this.hashValue = Utils.sha1(Utils.readContentsAsString(filePath));
    }

    public void save(){
        if(!blobFile.exists()){
            try{
                blobFile.createNewFile();
            }catch (Exception e){
                System.out.println("Error creating blob file");
            }
        }
        Utils.writeContents(blobFile, Utils.readContentsAsString(filePath));
    }

}
