package ec.falpema;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.log4j.Logger;





public class TareaCrearReducida implements Runnable{
	
	private String archivoProcesar;
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	static Logger log = Logger.getLogger(TareaCrearReducida.class);
    private Propiedades propiedades;
	private UtilImagenes utilImagenes;
	
	
	
	public TareaCrearReducida(String archivoProcesar) {
		this.archivoProcesar = archivoProcesar;
		this.propiedades= new Propiedades();
		this.utilImagenes= new UtilImagenes();
	}





    @Override
    public void run() {
        //abrir archivo
        log.info("Procesar archivo " + archivoProcesar);
        Path path = Paths.get(archivoProcesar);
        boolean correcto = false;
        try {

            //process each line in some way
            String pathRelativo = archivoProcesar;
            if (!pathRelativo.trim().isEmpty()) {
                /*   String pathBase = propiedades.getPropiedadReductor("pathBase");
                String pathDestinoBase = propiedades.getPropiedadReductor("pathDestino");
                String archivoDestino = pathDestinoBase.concat(pathRelativo);
                String archivoOrigen = pathBase.concat(pathRelativo);*/
                String archivoDestino = pathRelativo;
                String archivoOrigen = pathRelativo;
                log.info("archivoDestino " + archivoDestino);
                log.info("archivoOrigen " + archivoOrigen);
                utilImagenes.crearImagenReducida(archivoOrigen.trim(), archivoDestino, 0);
            }

            correcto = true;
        } catch (Exception e) {
            log.error("Error al crear imagen reducida ", e);
            correcto = false;
        }


    }

	
	
	

}
