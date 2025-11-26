package de.hsh.jaxbutil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

/**
 * This class can write or read an instance of a JAXB annotated class.
 * It handles masking of illegal characters in an XML file and formatting issues.
 */
public class JaxbUtil {
    
	
	/**
	 * Write object {@code o} to {@code output} while using the {@code classesToBebound} as
	 * the class context.
	 * @param o the instance to be written
	 * @param output output stream
	 * @param classesToBeBound these classes define the class context
	 * @throws JAXBException  if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while writing the object.
	 * @throws IOException if an I/O specific error was encountered.
	 */
	public static void write(Object o, OutputStream output, Class<?> ... classesToBeBound) throws JAXBException, IOException {
		ArrayList<Class<?>> cl= new ArrayList<Class<?>>();
		for (Class<?> c : classesToBeBound) cl.add(c);
		write(o, output, cl);
	}

	
	/**
	 * Write object {@code o} to output file {@code outFileName} while using the {@code classesToBebound} as
	 * the class context.
	 * @param o the instance to be written
	 * @param outFileName output file name
	 * @param classesToBeBound these classes define the class context
	 * @throws JAXBException  if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while writing the object.
	 * @throws IOException if an I/O specific error was encountered.
	 */
	public static void write(Object o, String outFileName, Class<?> ... classesToBeBound) throws JAXBException, IOException {
		ArrayList<Class<?>> cl= new ArrayList<Class<?>>();
		for (Class<?> c : classesToBeBound) cl.add(c);
		write(o, outFileName, cl);
	}

	/**
	 * Write object {@code o} to output file {@code outFileName} while using the {@code classesToBebound} as
	 * the class context.
	 * @param o the instance to be written
	 * @param outFileName output file name
	 * @param classesToBeBound these classes define the class context
	 * @throws JAXBException  if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while writing the object.
	 * @throws IOException if an I/O specific error was encountered.
	 */
	public static void write(Object o, String outFileName, Collection<Class<?>> classesToBeBound) throws JAXBException, IOException {
		FileOutputStream outFile= null;
		BufferedOutputStream bos= null;
		try {
	        outFile= new FileOutputStream(outFileName);
	        bos= new BufferedOutputStream(outFile);
	        write(o, bos, classesToBeBound);
		} finally {
            if (bos != null) bos.close();       
            if (outFile != null) outFile.close();       
		}
	}

	

	
	/**
	 * Write object {@code o} to {@code output} while using the {@code classesToBebound} as
	 * the class context.
	 * @param o the instance to be written
	 * @param output output stream
	 * @param classesToBeBound these classes define the class context
	 * @throws JAXBException  if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while writing the object.
	 * @throws IOException if an I/O specific error was encountered.
	 */
	public static void write(Object o, OutputStream output, Collection<Class<?>> classesToBeBound) throws JAXBException, IOException {
	    Objects.requireNonNull(output, "output stream must not be null!");
	    BufferedOutputStream bos= null;
	    if (output.getClass() != BufferedOutputStream.class) {
	        bos= new BufferedOutputStream(output);
            output= bos;
	    }
        Class<?>[] c= new Class<?>[classesToBeBound.size()+1];
        int i=0;
        for (Class<?> clazz : classesToBeBound) {
        	c[i+1]= clazz;
        	i++;
        }
        c[0]= o.getClass();
		JAXBContext context = JAXBContext.newInstance(c);       
        Marshaller m = context.createMarshaller();
        
        // Dies hier verwendet kein CDATA, was aber hilfreich wäre für das description-Element in der task.xml:
        //m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        //m.marshal(o, outFile);
        
        // Hier ist die Lösung. Quellen:
        //  - http://stackoverflow.com/questions/3136375/how-to-generate-cdata-block-using-jaxb)
        //  - http://stackoverflow.com/questions/4616383/xmlstreamwriter-indentation
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter= null;
        try {
            streamWriter = xof.createXMLStreamWriter(output, "UTF-8" );
            IndentingXMLStreamWriter indWriter= new IndentingXMLStreamWriter(streamWriter);
            CDataXMLStreamWriter cdataStreamWriter = new CDataXMLStreamWriter( indWriter );
            streamWriter= cdataStreamWriter;
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            m.marshal( o, streamWriter );
        } catch (XMLStreamException e) {
            throw new IOException(e);
        } finally {
            if (streamWriter != null)
                try {
                    streamWriter.flush();
                } catch (XMLStreamException e) {
                    throw new IOException(e);
                }
            if (bos != null) bos.flush();
        }
	}

	
	/**
	 * Instantiate an object of a JAXB annotated type {@code clazz} by reading 
	 * it from input file {@code inFileName} while using the {@code contextClasses} as
	 * the class context.
	 * @param inFileName  the source file
	 * @param clazz  the type of the new object
	 * @param contextClasses these classes define the class context
	 * @return the new instance
	 * @throws JAXBException if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while reading the object.
	 * @throws IOException if an I/O specific error was encountered.
	 */
	public static <T> T read(String inFileName, Class<T> clazz, Class<?> ... contextClasses) throws JAXBException, IOException {
		FileInputStream inFile= null;
		BufferedInputStream bin= null;
		try {
			inFile= new FileInputStream(inFileName);
			bin= new BufferedInputStream(inFile);

	        return read(bin, clazz, contextClasses);
		} finally {
            if (bin != null) bin.close();
            if (inFile != null) inFile.close();
		}
	}

	
	
    /**
     * Instantiate an object of a JAXB annotated type {@code clazz} by reading 
     * it from {@code input} while using the {@code contextClasses} as
     * the class context.
     * @param input  the source stream
     * @param clazz  the type of the new object
     * @param schema
     * @param contextClasses these classes define the class context
     * @return the new instance
     * @throws JAXBException if an error was encountered while preparing the Marshaller object or the JAXBContext
     * or while reading the object.
     * @throws IOException if an I/O specific error was encountered.
     */
    public static <T> T read(InputStream input, Class<T> clazz, Schema schema, Class<?> ... contextClasses) throws JAXBException, IOException {
        Objects.requireNonNull(input, "input must not be null!");
        
        if (input.getClass() != BufferedInputStream.class) {
            input= new BufferedInputStream(input);
        }
        
        Class<?>[] allContextClasses= new Class<?>[1+contextClasses.length];
        allContextClasses[0]= clazz;
        for (int i=1; i<allContextClasses.length; i++) {
            allContextClasses[i]= contextClasses[i-1];
        }
        JAXBContext context = JAXBContext.newInstance(allContextClasses);       
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        XMLInputFactory xif= XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader;
        try {
            xmlStreamReader = xif.createXMLStreamReader(input);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        
        // this implementation is a part of the API and convenient for trouble-shooting,
        // as it prints out errors to System.out
        //unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
        
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            @Override public boolean handleEvent(ValidationEvent event) {
                switch (event.getSeverity()) {
                case ValidationEvent.ERROR:
                case ValidationEvent.FATAL_ERROR:
                    throw new RuntimeException(event.toString(), event.getLinkedException());
                }
                return true; // warning
            }
        });

        if (schema != null) {
            unmarshaller.setSchema(schema);
        }

        try {
            //Object obj= unmarshaller.unmarshal(input);
            Object obj= unmarshaller.unmarshal(xmlStreamReader);
            return clazz.cast(obj);
        } catch (JAXBException t) {
            //t.printStackTrace(System.err);
            throw t;
        } catch (RuntimeException r) {
            //r.printStackTrace(System.err);
            throw r;
        }

    }
    
    
	/**
	 * Instantiate an object of a JAXB annotated type {@code clazz} by reading 
	 * it from {@code input} while using the {@code contextClasses} as
	 * the class context.
	 * @param input  the source stream
	 * @param clazz  the type of the new object
	 * @param contextClasses these classes define the class context
	 * @return the new instance
	 * @throws JAXBException if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while reading the object.
	 * @throws IOException if an I/O specific error was encountered.
	 */
	public static <T> T read(InputStream input, Class<T> clazz, Class<?> ... contextClasses) throws JAXBException, IOException {
	    return read(input, clazz, null, contextClasses);
	}

	
	private static boolean answeredYes(Scanner console, String prompt, String defaultAnswer) {
		String answer;
		do {
			System.out.print(prompt+" ["+defaultAnswer+"] ");
			answer= console.nextLine().toUpperCase();
			if (answer.trim().length()==0) answer= defaultAnswer;
		} while (!(answer.equals("Y") || answer.equals("N")));
		return (answer.equals("Y"));
	}
	
	private static void abort() {
		System.out.println("Aborted.");
		System.exit(1);
	}

	/**
	 * Generates schema files for a given classes context
	 * @param console is used to get user input about filenames
	 * @param domain these classes define the class context.
	 * @param destDir destination directory. If null, the files are created in the current directory. 
	 * The destination directory must be created by the caller, if it does not exist. 
	 */
	public static void queryXsd(Scanner console, Class<?>[] domain, File destDir) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(domain);
			SchemaOutputResolver sor = new MySchemaOutputResolver(console, destDir);
			jaxbContext.generateSchema(sor);
		    
		} catch (JAXBException | IOException e) {
			System.err.println("Error: "+e.getMessage());
			e.printStackTrace(System.err);
			abort();
		}
		
        System.out.println("Done.");
		System.exit(0);
	}
	
	
	private static class MySchemaOutputResolver extends SchemaOutputResolver {

		private Scanner console;
		private File destDir;
		public MySchemaOutputResolver(Scanner console, File destDir) {
			this.console= console;
			this.destDir= destDir;
		}
	    public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
	    	String fileName;
	    	if (namespaceURI.startsWith("urn:")) {
	    		fileName= namespaceURI.substring(4).replace(':', '_') + ".xsd";
	    	} else {
	    		fileName= suggestedFileName;
	    	}
	    	System.out.println(namespaceURI);
	    	System.out.println(fileName);
	    	File file;
	    	if (destDir != null) {
	    		file= new File(destDir.getPath() + File.separator + fileName);
	    	} else {
	    		file= new File(fileName);
	    	}
	        file = file.getAbsoluteFile();
	        if (file.exists()) {
	        	if (!answeredYes(console, "File '"+file+"' exists. Overwrite (Y/N)?", "Y")) {
	        		abort();
	        	}
	        } else {
		        System.out.println("Writing file '"+file+"'.");
	        }
	        StreamResult result= new StreamResult(new FileOutputStream(file));
	        result.setSystemId(file.toURI().toURL().toString());
	        return result;
	    }

	}
}

