package ec.falpema;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;



public class UtilArchivos {
	static Logger log = Logger.getLogger(UtilArchivos.class);
	private String separadorSftp="/";
	
	private static String separador;
	
	private static final String SO=System.getProperty("os.name");
	

	public UtilArchivos() {
	
		if (!SO.toLowerCase().contains("windows")) {
            separador = "/";
        }else{
            separador = "\\";
        }
	}

	public static String getSeparador() {
		return separador;
	}

	public void setSeparador(String separador) {
		this.separador = separador;
	}

	public String getSeparadorSftp() {
		return separadorSftp;
	}

	public void setSeparadorSftp(String separadorSftp) {
		this.separadorSftp = separadorSftp;
	}
	
	
	
    public static Boolean copiarFichero(String origen, String destino) {
        String comando = "";      
        log.info("sistema "+UtilArchivos.getSO().toLowerCase());
        if (UtilArchivos.getSO().toLowerCase().contains("windows")) {          
            comando = "cmd /c copy /Y \"" + origen + "\"" + " \"" + destino + "\"";
            log.info("windows "+comando);
        } else {
            comando = "cp -f " + origen + " " + destino;
            log.info("linux "+comando);
        }
        UtilArchivos.log.debug("Copiar fichero "+origen +" a "+destino);
        int resp = llamarSO(comando);
        return resp == 0;
    }
    
    
    public static String getSO() {
        return SO;
    } 
    
    public static int llamarSO(String comando) {
        String s = null;
        try {
            // Ejecutamos el comando
            Process p = Runtime.getRuntime().exec(comando);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));

            // Leemos la salida del comando
            UtilArchivos.log.debug("Ésta es la salida standard del comando:");
            s = stdInput.readLine();
            int linea=0;
            while ( s!= null) {
                 UtilArchivos.log.debug(s);
                 linea ++;
                 if(linea>=10){
                     UtilArchivos.log.debug("Se corta salida de comando por tener mas de 10 lineas");
                     break;
                 }
            }

            // Leemos los errores si los hubiera
            UtilArchivos.log.debug("SALIDA DE ERROR, EN CASO DE HABERLO");
            s = stdError.readLine();   
            linea=0;
            while (s != null) {
                UtilArchivos.log.debug(s);
                linea ++;
                if(linea>=20){
                     UtilArchivos.log.debug("Se corta salida de comando por tener mas de 20 lineas");
                     break;
                 }
            }
            return 0;
        } catch (Exception err) {
            UtilArchivos.log.error("Error al llamar funcion de sistema operativo ",err);
            return -1;
        }
    }

   
    public static void crearDirectorio(String dir) {
        if (dir.isEmpty()) {
            return;
        }
        File directorio = new File(dir);
        directorio.mkdirs();
        log.info("se crea "+dir);
    }
    
    public static List<String> getListaArchivos(String path){
        List<String> archivos=new ArrayList<>();
        String sDirectorio = path;
        File f = new File(sDirectorio);
        if (f.exists()) { // Directorio existe 
            File[] ficheros = f.listFiles();
            for (int x=0;x<ficheros.length;x++){
                archivos.add(ficheros[x].getName());              
            }
        } else { //Directorio no existe 

        }
        return archivos;
    }
	
    /**
     * Metodo para borrar el directorio si es que existe
     * @autor: Ing. Fabian Peñaloza M
     * @param path
     * @return 
     */
    public static boolean borrarDirectorio(String path) {
        log.debug("borrando "+path);
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (!file.isDirectory()) {
            return file.delete();
        }
        return UtilArchivos.borrarDirHijo(file) && file.delete();
    }

    public static boolean borrarFichero(String nombre) {
        File fich = new File(nombre);
        return fich.delete();
    }
    
    private static  boolean borrarDirHijo(File dir) {
        File[] children = dir.listFiles();
        boolean childrenDeleted = true;
        for (int i = 0; children != null && i < children.length; i++) {
            File child = children[i];
            if (child.isDirectory()) {
                childrenDeleted = UtilArchivos.borrarDirHijo(child) && childrenDeleted;
            }
            if (child.exists()) {
                childrenDeleted = child.delete() && childrenDeleted;
            }
        }
        return childrenDeleted;
    }
    
    @Deprecated
    public static byte[] filetoBytes(String filePath ){
    	RandomAccessFile f=null;
    	byte[] b=null;
		try {
			f = new RandomAccessFile(filePath, "r");
			b = new byte[(int)f.length()];
	    	f.readFully(b);
		} catch (Exception e) {
			log.error("Error al leer el archivo "+filePath,e);
		}finally{
			try {
				f.close();
			} catch (IOException e) {
				log.error("Error al cerrar RandomAccessFile del archivo "+filePath,e);
			}
		}
    	return b;
    }
    
    
    
    
    /**
     * Metodo para realizar un filtrado por lo rangos
     * para dar la lista de imagenes que cumplan las condiciones de estar entre el limite inferior y limite superior
     * en la cantidad de imagenes.
     * @author fabian
     * @param archivos
     * @param limitSupe 
     * @param limitInfe 
     * @return devuelve la lista desde la cantidad limitInfe hasta la de limitSupe
     */
	public static List<String> aplicarFiltradoRangolista(List<String> archivos, Integer limitInfe, Integer limitSupe) {
		List<String> lsResp = new ArrayList<String>();
		for (int i=0;i<archivos.size();i++) {
			if(i>=limitInfe && i<limitSupe){
				lsResp.add(archivos.get(i));
			}
			
		}
		
		return lsResp;
	}
	
	
	  /**
     * Metodo recursivo para validar si existe directorio destino carpeta por
     * carpeta si no existe deja creando dicha carpeta
     *
     * @author fabian
     * @param sftp
     * @param finalPath
     */
    public  void validarExistaDirectorioDestino(String finalPath) {
    	UtilArchivos ua = new UtilArchivos();
		if (finalPath.contains("\\")) {
			finalPath = finalPath.replace("\\", ua.getSeparador());
		}
        log.info(finalPath + " " + UtilArchivos.getSeparador());
        if (SO.toLowerCase().contains("windows")) {
            UtilArchivos.crearDirectorio(finalPath);
            log.info("creo directorio");
        } else {

            String[] ipath = finalPath.split(UtilArchivos.getSeparador().trim());
            String fsum = UtilArchivos.getSeparador();
            for (String part : ipath) {

                try {
                    if (((fsum).compareTo(UtilArchivos.getSeparador()) != 0)
                            && existeFichero(fsum + part) == false) {
                        log.debug(fsum);
                        crearDirectorio(fsum + UtilArchivos.getSeparador() + part);
                    }
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();

                }
                if (!part.isEmpty()) {
                    fsum = new StringBuilder().append(fsum).append(part).append(UtilArchivos.getSeparador()).toString();
                }
            }

        }
    }
    
    public  Boolean existeFichero(String nombrefichero) {
        File fichero = new File(nombrefichero);
        return fichero.exists();
    }
    
    
    
    public  byte[] getBytesObject(Object objeto) throws IOException {
        ByteArrayOutputStream bs= new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream (bs);
        os.writeObject(objeto);  // this es de tipo DatoUdp
        os.close();
        byte[] bytes =  bs.toByteArray(); // devuelve byte[]
        return bytes;
    }
    
    
    public  Object getObjectFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bs= new ByteArrayInputStream(bytes); // bytes es el byte[]
        ObjectInputStream is = new ObjectInputStream(bs);
        Object unObjetoSerializable = is.readObject();
        is.close();
        return unObjetoSerializable;
    }
    
    
    public  void guardarImagenDesdeBytes(String archivo, byte[] imagen) throws Exception {
        File directorio = new File(archivo).getParentFile();
        if (directorio.mkdirs()) {
            log.debug("Directorio Creado: " + archivo);
        }
        FileOutputStream stream = new FileOutputStream(archivo);
        try {
            stream.write(imagen);
        } finally {
            stream.close();
        }
    }
    
   


}
