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
public class FileData {
    private String fileName;
    private int fileSize; //tamano en bloques del futuro archivo
    //LUEGO ANADIR DIRECTORIO
    private boolean isProcessed;
    private String processName; // Nombre del proceso que creó este archivo

    public FileData(String fileName, int fileSize) {
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.isProcessed = false;
        this.processName = null;
    }
    
    public FileData(String fileName, int fileSize, String processName) {
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.isProcessed = false;
        this.processName = processName;
    }

    public String getFileName() {
        return fileName;
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

    public boolean isIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    
}
