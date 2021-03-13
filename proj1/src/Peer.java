public class Peer{
  private String version;
  private int id;
  private String access_point;

  public static void main(String[] args) {
    if(args.length != 9){
      usage();
      System.exit(1);
    }

    Peer peer_instance = new Peer(args);
  }

  public Peer(String[] args){
    this.version = args[0];
    try {
      this.id = Integer.parseInt(args[1]);
    }catch (NumberFormatException e){
      System.out.println("Exception: " + e.getMessage());
      System.exit(1);
    }
    this.access_point = args[2];

    //MC

  }

  private static void usage(){
    System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
  }


}
