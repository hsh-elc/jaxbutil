package de.hsh.jaxbutil;


import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * This is a XML object marshaller.
 * This class relies on JAXB annotations in the serialized class.
 */
public class XmlMarshaller {
    /**
     * Read an object from {@code inFileName} and instantiate it as an object of class {@code clazz}.
     * @param inFileName input file
     * @param clazz type of the new instance
     * @return the new instance
     * @throws JAXBException if an error was encountered while preparing the Marshaller object or the JAXBContext
     * or while reading the object.
     * @throws IOException if an I/O specific error was encountered.
     */
    public <T> T read(String inFileName, Class<T> clazz) throws JAXBException, IOException {
        FileInputStream inFile= null;
        try {
            inFile= new FileInputStream(inFileName);
            return read(inFile, clazz);
        } finally {
            if (inFile != null) inFile.close();
        }
    }
    
    /**
     * Write an object {code o} to file {@code outFileName}.
     * @param o the object to be written
     * @param outFileName the output file
     * @throws JAXBException if an error was encountered while preparing the Marshaller object or the JAXBContext
     * or while writing the object.
     * @throws IOException if an I/O specific error was encountered.
     */
    public void write(Object o, String outFileName) throws JAXBException, IOException {
        FileOutputStream outFile= null;
        BufferedOutputStream bos= null;
        try {
            outFile= new FileOutputStream(outFileName);
            bos= new BufferedOutputStream(outFile);
            write(o, bos);
        } finally {
            if (bos != null) bos.close();       
            if (outFile != null) outFile.close();       
        }

    }
    

    
    /**
	 * Read an object from {@code input} and instantiate it as an object of class {@code clazz}.
	 * @param input input stream
	 * @param clazz type of the new instance
	 * @return the new instance
	 * @throws JAXBException if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while reading the object.
	 * @throws IOException if an I/O specific error was encountered.
	 * @throws XMLStreamException 
	 */
	public <T> T read(InputStream input, Class<T> clazz) throws JAXBException, IOException {
		return JaxbUtil.read(input, clazz);
	}

	/**
	 * Write an object {code o} to {@code output}.
	 * @param o the object to be written
	 * @param output the output stream
	 * @throws JAXBException if an error was encountered while preparing the Marshaller object or the JAXBContext
	 * or while writing the object.
	 * @throws IOException if an I/O specific error was encountered.
	 * @throws XMLStreamException  if an error was encountered while writing XML data
	 */
	public void write(Object o, OutputStream output) throws JAXBException, IOException {
		JaxbUtil.write(o, output);
	}
    
}
