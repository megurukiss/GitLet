package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class Test {
    public static void main(String[] args) {
        HashMap<String, String> map = Utils.readObject(Utils.join(Repository.CWD,".gitlet/stage"), HashMap.class);
        System.out.println(map);
    }
}
