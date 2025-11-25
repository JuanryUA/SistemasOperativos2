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
    public enum OperationType {
        CREATE, READ, UPDATE, DELETE
    }
    
    private String fileName;
    private String newFileName; // For UPDATE operation
    private int fileSize; //tamano en bloques del futuro archivo
    private String ruta; // Directory path like "root/x/y/z"
    private boolean isProcessed;
    private String processName; // Nombre del proceso que creó este archivo
    private OperationType operationType;
    private String errorMessage; // Mensaje de error si la operación falla
    private String tipoArchivo; // "publico" o "privado"
    private String modoUsuario; // "Administrador" o "Usuario" - modo del usuario que hace la petición

    public FileData(String fileName, int fileSize) {
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.ruta = "root";
        this.isProcessed = false;
        this.processName = null;
        this.operationType = OperationType.CREATE;
        this.tipoArchivo = "publico"; // Default
        this.modoUsuario = "Usuario"; // Default
    }
    
    public FileData(String fileName, int fileSize, String processName) {
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.ruta = "root";
        this.isProcessed = false;
        this.processName = processName;
        this.operationType = OperationType.CREATE;
        this.tipoArchivo = "publico"; // Default
        this.modoUsuario = "Usuario"; // Default
    }
    
    public FileData(String fileName, int fileSize, String ruta, String processName) {
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.ruta = ruta != null ? ruta : "root";
        this.isProcessed = false;
        this.processName = processName;
        this.operationType = OperationType.CREATE;
        this.tipoArchivo = "publico"; // Default
        this.modoUsuario = "Usuario"; // Default
    }
    
    public FileData(String fileName, int fileSize, String ruta, String processName, String tipoArchivo) {
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.ruta = ruta != null ? ruta : "root";
        this.isProcessed = false;
        this.processName = processName;
        this.operationType = OperationType.CREATE;
        this.tipoArchivo = tipoArchivo != null ? tipoArchivo : "publico";
        this.modoUsuario = "Usuario"; // Default
    }
    
    public FileData(String fileName, OperationType operationType, String processName) {
        this.fileName = fileName;
        this.fileSize = 0;
        this.ruta = "root";
        this.isProcessed = false;
        this.processName = processName;
        this.operationType = operationType;
        this.tipoArchivo = "publico"; // Default
        this.modoUsuario = "Usuario"; // Default
    }
    
    public FileData(String fileName, String ruta, OperationType operationType, String processName) {
        this.fileName = fileName;
        this.fileSize = 0;
        this.ruta = ruta != null ? ruta : "root";
        this.isProcessed = false;
        this.processName = processName;
        this.operationType = operationType;
        this.tipoArchivo = "publico"; // Default
        this.modoUsuario = "Usuario"; // Default
    }
    
//    public FileData(String fileName, String newFileName, OperationType operationType, String processName) {
//        this.fileName = fileName;
//        this.newFileName = newFileName;
//        this.fileSize = 0;
//        this.ruta = "root";
//        this.isProcessed = false;
//        this.processName = processName;
//        this.operationType = operationType;
//    }
    
    public FileData(String fileName, String newFileName, String ruta, OperationType operationType, String processName) {
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.fileSize = 0;
        this.ruta = ruta != null ? ruta : "root";
        this.isProcessed = false;
        this.processName = processName;
        this.operationType = operationType;
        this.tipoArchivo = "publico"; // Default
        this.modoUsuario = "Usuario"; // Default
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
    
    public OperationType getOperationType() {
        return operationType;
    }
    
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    public String getNewFileName() {
        return newFileName;
    }
    
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
    
    public String getRuta() {
        return ruta;
    }
    
    public void setRuta(String ruta) {
        this.ruta = ruta != null ? ruta : "root";
    }
    
    public String getTipoArchivo() {
        return tipoArchivo;
    }
    
    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo != null ? tipoArchivo : "publico";
    }
    
    public String getModoUsuario() {
        return modoUsuario;
    }
    
    public void setModoUsuario(String modoUsuario) {
        this.modoUsuario = modoUsuario != null ? modoUsuario : "Usuario";
    }
    
}
