package ec.fin.coopjep.indexacion.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.imgscalr.Scalr;

import com.icafe4j.image.ImageIO;
import com.icafe4j.image.ImageParam;
import com.icafe4j.image.ImageType;
import com.icafe4j.image.options.TIFFOptions;
import com.icafe4j.image.tiff.TiffFieldEnum.Compression;
import com.icafe4j.image.writer.ImageWriter;

public class UtilImagenes {
	static Logger log = Logger.getLogger(UtilImagenes.class);
	UtilArchivos ua= new UtilArchivos();
	private Propiedades properties;
	
	
	
	public UtilImagenes() {
		super();
		this.properties = new Propiedades();
		properties.init();
	}
	   public boolean crearImagenReducida(String pathArchivo, String pathTemporal, int numeroImagen) {
        boolean resultado = false;
        try {
            UtilArchivos ua = new UtilArchivos();
            System.out.println("path real " + pathArchivo + " pathTemporal" + pathTemporal);
            File archivoTemporal = new File(pathTemporal);
            if (!archivoTemporal.exists()) {
                log.debug("Archivo no  existe " + pathTemporal);
            } else {
                java.awt.image.BufferedImage pimagen = (java.awt.image.BufferedImage) loadImagenTIFF(pathArchivo, numeroImagen);
                java.awt.image.BufferedImage imagenmin = (java.awt.image.BufferedImage) pimagen;
                int anchoOriginal = pimagen.getWidth();
                int altoOriginal = pimagen.getHeight();
                int anchoNuevo = 0;
                int altoNuevo = 0;
                int anchoDefault = Integer.valueOf(properties.getPropiedadReductor("ancho"));
                int altoDefault = Integer.valueOf(properties.getPropiedadReductor("alto"));
                if (anchoOriginal > altoOriginal) { //hoja horizontal
                    altoNuevo = altoOriginal < altoDefault ? altoOriginal : altoDefault;
                    double relacion = ((double) altoOriginal / ((double) altoNuevo));
                    log.info("Relacion de imagen " + relacion);
                    double anchotmp = anchoOriginal / relacion;
                    anchoNuevo = (int) anchotmp;
                } else {//vertical
                    anchoNuevo = anchoOriginal < anchoDefault ? anchoOriginal : anchoDefault;
                    double relacion = ((double) anchoOriginal / (double) anchoNuevo);
                    double altotmp = altoOriginal / relacion;
                    log.info("Relacion de imagen " + relacion);
                    altoNuevo = (int) altotmp;

                }

                imagenmin = org.imgscalr.Scalr.resize(imagenmin, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, anchoNuevo, altoNuevo);
                log.info("Tamnio de imagen nueva" + anchoNuevo + "-" + altoNuevo);
                UtilArchivos.borrarFichero(pathArchivo); log.info("borro "+pathArchivo);
                guardarTiff(pathTemporal, imagenmin); log.info("crea imagen reducido  "+pathTemporal);
                resultado = true;
            }

        } catch (Exception e) {
            log.error("Error al obtener imagen reducida error", e);
            resultado = false;
        }
        return resultado;
    }
	/**
     * Devuelve la imagen correspondiente a un numero de pagina especifico de
     * una imagen tiff multipagina
     *
     * @param path Ruta completa de la imagen
     * @param numpagina numero de pagina a extaer
     * @return Image, imagen correspondiente a el numero de pagina de paramtro
     * @throws Exception
     */
    public  BufferedImage loadImagenTIFF(String path, int numpagina) throws Exception {
        BufferedImage bufferedImage;
        try {
            FileInputStream in = new FileInputStream(path);
            ImageInputStream iis = javax.imageio.ImageIO.createImageInputStream(in);
            ImageReader reader = getTiffImageReader();
            reader.setInput(iis);
            bufferedImage = reader.read(numpagina);
        } catch (Exception e) {
            log.error("No se pudo leer imagen", e);
            bufferedImage = ImageIO.read(UtilImagenes.class.getResourceAsStream("/image-not-found.jpg"));
        }
        return bufferedImage;
    }
    private static ImageReader getTiffImageReader() {
        Iterator<ImageReader> imageReaders = javax.imageio.ImageIO.getImageReadersByFormatName("TIFF");
        if (!imageReaders.hasNext()) {
            throw new UnsupportedOperationException("No TIFF Reader found!");
        }
        return imageReaders.next();
    }
    
    public  byte[] imageToByteArray(BufferedImage imagen, String formato) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(imagen, formato, baos);
        baos.flush();
        
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        log.debug("devuelve en crearImagen: " + imageInByte + " size " + imageInByte.length);
        return imageInByte;
    }
    
    
    public  void guardarTiff(String fichero, java.awt.image.BufferedImage imagen) throws Exception {
        try {
        	File carpeta=new File(fichero).getParentFile();
        	if(carpeta.mkdirs()){
        		log.info("Creando directorio "+carpeta.getAbsolutePath());
        	}
        	boolean tamanioCorrecto=false;
        	long tamanioMaximo=Long.valueOf(properties.getPropiedadReductor("tamanioMaximo"));
        	int calidadJPG=Integer.valueOf(properties.getPropiedadReductor("calidad"));
        	while (!tamanioCorrecto){
        		try (FileOutputStream out = new FileOutputStream(fichero)) {
                    ImageParam.ImageParamBuilder builder = ImageParam.getBuilder();
                    TIFFOptions tiffOptions = new TIFFOptions();
                    tiffOptions.setTiffCompression(Compression.JPG);
                    tiffOptions.setJPEGQuality(calidadJPG);
                    builder.imageOptions(tiffOptions);
                    ImageType imageType = ImageType.TIFF;
                    ImageWriter writer = ImageIO.getWriter(imageType);
                    writer.setImageParam(builder.build());
                    writer.write(imagen, out);
                    // Need to close the underlying stream explicitly!!!
                    log.info("Guardado Imagen " + fichero);
                }
        		File archivoDestino= new File(fichero);
        		if(archivoDestino.length() < (tamanioMaximo*1024)){
        			tamanioCorrecto=true;
        			
        		}else{
        			log.debug("imagen de tamaño "+archivoDestino+" bytes a calidad " +calidadJPG);
        		}
        		calidadJPG= calidadJPG-10;
        	}
            
        } catch (Exception ex) {
            log.error("Error al guardar el fichero TIFF - " + ex.getMessage(), ex);
            throw ex;
        }
    }

}
