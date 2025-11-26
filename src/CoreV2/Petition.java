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
    private static int contadorGlobal = 0;
    private int id;
    private String fileName;
    private int fileSize;
    private FileData filedata;
    private int track; 
    private long tiempoInicio;
    

    public Petition(FileData filedata) {
        this.id = ++contadorGlobal;
        this.fileName=filedata.getFileName();
        this.fileSize=filedata.getFileSize();
        this.filedata =filedata;
    
        this.track = Math.abs(fileName.hashCode()) % 200;
        this.tiempoInicio = System.currentTimeMillis();
    }
    
    public Petition(FileData filedata, int track) {
        this.id = ++contadorGlobal;
        this.fileName=filedata.getFileName();
        this.fileSize=filedata.getFileSize();
        this.filedata =filedata;
        this.track = track;
        this.tiempoInicio = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
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
    
    public int getTrack() {
        return track;
    }
    
    public void setTrack(int track) {
        this.track = track;
    }
    
    public long getTiempoInicio() {
        return tiempoInicio;
    }
    
}
