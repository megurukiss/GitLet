package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if(args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        
        String firstArg = args[0];
        if(firstArg.equals("init")) {
            Repository.init();
            System.exit(0);
        }

        // check initial folder exists
        if(!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                assert args.length == 2;
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                assert args.length == 2;
                // check if message is empty
                if(args[1].trim().isEmpty()){
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;

            case "rm":
                assert args.length == 2;
                Repository.remove(args[1]);
                break;

            case "log":
                assert args.length == 1;
                Repository.log();
                break;

            case "global-log":
                assert args.length == 1;
                Repository.globalLog();
                break;

            case  "find":
                assert args.length == 2;
                Repository.find(args[1]);
                break;

            case "status":
                assert args.length == 1;
                Repository.status();
                break;

            case "checkout":
                break;

            case "branch":
                break;

            case "rm-branch":
                break;

            case "reset":
                break;

            case "merge":
                break;

            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
