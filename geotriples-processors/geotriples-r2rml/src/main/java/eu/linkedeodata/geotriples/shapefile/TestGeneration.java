package eu.linkedeodata.geotriples.shapefile;

import eu.linkedeodata.geotriples.KeyGenerator;
import eu.linkedeodata.geotriples.KeyGenerator.Use;

public class TestGeneration {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(KeyGenerator.Generate());
		System.out.println(KeyGenerator.Generate(Use.NEW_ONE));

		System.out.println(KeyGenerator.Generate());

		System.out.println(KeyGenerator.Generate("dimis",Use.USE_PREV));

		System.out.println(KeyGenerator.Generate(Use.USE_PREV));
		System.out.println(KeyGenerator.Generate("dimis",Use.NEW_ONE));
		System.out.println(KeyGenerator.Generate("dimis"));
		System.out.println(KeyGenerator.Generate());
		
		KeyGenerator kg=new KeyGenerator();
		System.out.println(kg.GenerateFromMap("dimis"));
		System.out.println(kg.GenerateFromMap("fff"));
		System.out.println(kg.GenerateFromMap("dimis"));
		System.out.println(kg.GenerateFromMap("dimis"));
		System.out.println(kg.GenerateFromMap("fff"));
		System.out.println(kg.GenerateFromMap("ppp"));




	}

}
