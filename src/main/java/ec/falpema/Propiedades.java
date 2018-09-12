package ec.falpema;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;




public class Propiedades implements Serializable {

	private static final long serialVersionUID = 1L;

	private  Properties properties = new Properties();;

	
	
	public Propiedades() {
		this.properties = new Properties();
	}

	public void init() {
		
	}
	
	public String getPropiedadReductor(String data) throws IOException {
		InputStream archivo = Propiedades.class.getClassLoader().getResourceAsStream("reductor.properties");
		properties.load(archivo);
		return properties.getProperty(data);
	}
	
	
	
}
