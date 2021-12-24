import java.io.*;

public class StoreObjects implements Serializable {

    static final long serialVersionUID = -7503199887922442805L;
    private String objectID;
    private FileOutputStream fileOut;
    private ObjectOutputStream write;

    /**
     * @param objectID
     */
    public StoreObjects(String objectID) {
        try {
            this.objectID = objectID;
            this.fileOut = new FileOutputStream(objectID);
            this.write = new ObjectOutputStream(fileOut);
        } catch (FileNotFoundException fnfx) {
            fnfx.printStackTrace();
        } catch (IOException iox) {
            iox.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * @return
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * @param objectID
     */
    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    /**
     * @return
     */
    public FileOutputStream getFileOut() {
        return fileOut;
    }

    /**
     * @param fileOut
     */
    public void setFileOut(FileOutputStream fileOut) {
        this.fileOut = fileOut;
    }

    /**
     * @return
     */
    public ObjectOutputStream getWrite() {
        return write;
    }

    /**
     * @param write
     */
    public void setWrite(ObjectOutputStream write) {
        this.write = write;
    }

    /**
     *
     */
    public void writeObject() {
        try {
            this.write.writeObject(this);
            this.write.flush();
            this.write.close();
        } catch (IOException iox) {
            iox.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }


    public void storeObject(Object objectToBeWritten) {
        try {
            this.write.writeObject(objectToBeWritten);
        } catch (IOException iox) {
            iox.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void close() {
        try {
            this.write.flush();
            this.write.close();
        } catch (IOException iox) {
            iox.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void main(String[] args)
            throws IOException, Exception {

        Population p = new Population(333);
        StoreObjects so = new StoreObjects("p1");
        so.storeObject(p);
        p.showPop();
        so.close();
        System.exit(0);
    }


}
