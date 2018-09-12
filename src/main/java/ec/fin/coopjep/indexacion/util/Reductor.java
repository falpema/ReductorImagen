package ec.fin.coopjep.indexacion.util;

import static ec.fin.coopjep.indexacion.util.TareaCrearReducida.log;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class Reductor {

    private static Logger log = Logger.getLogger(Reductor.class);
    private ExecutorService exService;
    private Propiedades properties;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    
    public Reductor() {
        int numeroHilos = 6;
        try {
            properties = new Propiedades();
            numeroHilos = Integer.valueOf(properties.getPropiedadReductor("hilos"));
            exService = Executors.newFixedThreadPool(numeroHilos);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void procesar() throws IOException {
        //ver ruta de archivos de texto
        String rutaProcesar = properties.getPropiedadReductor("rutaRevisar");
        log.info("Inicia ejecucion");
        log.info("Ruta Revisar " + rutaProcesar);
        File carpetaProcesar = new File(rutaProcesar);
        if (carpetaProcesar.exists()) {
            
            Path path = Paths.get(rutaProcesar + properties.getPropiedadReductor("nombreArchivo"));
            log.info("Procesar archivo " + path);
            boolean correcto = false;
            try (Scanner scanner = new Scanner(path, ENCODING.name())) {
                while (scanner.hasNextLine()) {
                    
                    TareaCrearReducida tarea = new TareaCrearReducida(scanner.nextLine());
                    exService.submit(tarea);
                    
                }
                exService.shutdown();
                while (!exService.isTerminated()) {
                    
                }
                exService.shutdownNow();
                log.info("TERMINO PROCESO OPTIMIZACION DE IMAGENES");
            } catch (Exception e) {
                log.error("Error al crear imagen reducida ", e);
                correcto = false;
            }
            
        } else {
            log.warn("No existe la carpeta " + rutaProcesar);
        }
        log.info("TERMINA EJECUCION ");
        
    }
    
    public static void main(String[] args) {
        Reductor reductor = new Reductor();
        try {
            reductor.procesar();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error("Error en proceso de reducción de imagenes ", e);
            System.exit(1);
        }
        log.info("Proceso terminado correctamente ");
        System.exit(0);
    }
    
}
