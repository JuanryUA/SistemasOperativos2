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
    private int track; // Track/cylinder number for disk scheduling
    private long tiempoInicio;
    
    //LUEGO ANADIR DIRECTORIO

    public Petition(FileData filedata) {
        this.fileName=filedata.getFileName();
        this.fileSize=filedata.getFileSize();
        this.filedata =filedata;
        // Generate track number based on filename hash (0-199 tracks)
        this.track = Math.abs(fileName.hashCode()) % 200;
        this.tiempoInicio = System.currentTimeMillis();
    }
    
    public Petition(FileData filedata, int track) {
        this.fileName=filedata.getFileName();
        this.fileSize=filedata.getFileSize();
        this.filedata =filedata;
        this.track = track;
        this.tiempoInicio = System.currentTimeMillis();
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
