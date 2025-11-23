/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CoreV2;

/**
 *
 * @author verol
 */
public class Petition {
    private String fileName;
    private int fileSize; //tamano en bloques del futuro archivo
    private FileData filedata;
    //LUEGO ANADIR DIRECTORIO

    public Petition(FileData filedata) {
        this.fileName=filedata.getFileName();
        this.fileSize=filedata.getFileSize();
        this.filedata =filedata;
    }

    public String getFileName() {
        return fileName;
    }
    
    public FileData getFileData() {
        return this.filedata;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
    
    
    
}
