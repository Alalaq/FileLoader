import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class FileLoader implements Runnable {
    private final URL FILE_URL;
    private final File CURR_FILE;
    private String fileName;
    private long fileSize;
    private String status;
    private boolean isPaused;


    public FileLoader(String dir, URL url, String fileName) throws IOException {
        FILE_URL = url;
        this.fileName = fileName;
        this.CURR_FILE = new File(dir + "/" + fileName);
        CURR_FILE.createNewFile();
        this.fileSize = getFileSize(FILE_URL);
    }

    public StringBuffer getInfo() {
        setStatus();
        StringBuffer sb = new StringBuffer(System.lineSeparator());
        sb.append(String.format("%-10s", "Name: " + getFileName() + "\n")).
                append(String.format("%-10s", " Status: " + status + "\n")).
                append(String.format("%-10s", " Bytes downloaded at the moment: " + getBytesCount() + "\n")).
                append(String.format("%-10s", " Percentage of file download: " + getBytesCountProcent() + "\n"));

        return sb;
    }

    public static long getFileSize(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            return conn.getContentLengthLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private long getBytesCount() {
        return CURR_FILE.length();
    }

    private int getBytesCountProcent() {
        return (int) (100 * getBytesCount() / getFileSize(FILE_URL));
    }

    private void setStatus(){
        if (getBytesCount() == fileSize){
            this.status = "downloaded";
        }
        else if (isPaused){
            this.status = "paused";
        }
        else {
            this.status = "downloading";
        }
    }

    public void setPause(){
        this.isPaused = true;
    }

    public void resume(){
        setStatus();
        if (!status.equals("downloaded")) {
            this.isPaused = false;
        }
    }

    public boolean isPaused(){
        return isPaused;
    }

    public String getFileName(){
        return fileName;
    }

    @Override
    public void run() {
        while (!isPaused) {
            try (BufferedInputStream in = new BufferedInputStream(FILE_URL.openStream());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(CURR_FILE))) {
                int a;
                byte[] buffer = new byte[1024];
                while ((a = in.read(buffer, 0, 1024)) != -1) {
                    out.write(buffer, 0, a);
                }
                out.flush();
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileLoader that = (FileLoader) o;
        return fileSize == that.fileSize && Objects.equals(FILE_URL, that.FILE_URL) && Objects.equals(CURR_FILE, that.CURR_FILE) && Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(FILE_URL, CURR_FILE, fileName, fileSize, status, isPaused);
    }
}
