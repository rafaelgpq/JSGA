import java.io.*;

public class RetrieveObjects implements Serializable {

    static final long serialVersionUID = -8309108707030711260L;
    private String objectID;
    private FileInputStream fileIn;
    private ObjectInputStream read;

    /**
     * @param objectID
     */
    public RetrieveObjects(String objectID) {
        try {
            this.objectID = objectID;
            this.fileIn = new FileInputStream(objectID);
            this.read = new ObjectInputStream(fileIn);
        } catch (FileNotFoundException fnfx) {
            fnfx.printStackTrace();
        } catch (IOException iox) {
            iox.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public Object retrieveObject() {
        Object obj = new Object();
        try {
            obj = this.read.readObject();
        } catch(IOException iox) {
            iox.printStackTrace();
        } catch(Exception x) {
            x.printStackTrace();
        }
        return obj;
    }

    public void close() {
        try {
            this.read.close();
        } catch(IOException iox) {
            iox.printStackTrace();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    public static void main(String[] args)
        throws FileNotFoundException, IOException, Exception {

        Population p = null;
        RetrieveObjects ro = new RetrieveObjects("p1");
        p = (Population) ro.retrieveObject();
        p.showPop();
        ro.close();
        System.exit(0);
    }


}
