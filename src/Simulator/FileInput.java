package Simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileInput {
	
	public void readFile(Scanner in) {
		String fileName;
		File file = null;
		
		
		//boolean valid = false;
		//do {
			System.out.print("\nPlease enter a file path:");
			fileName = in.nextLine();
			/*
			file = new File(fileName);
			if(file.exists()) {
				valid = true;
			} else {
				System.out.println("File does not exist");
			}
			*/
			
		//}while(!valid);
				
		try (BufferedReader br = new BufferedReader(new FileReader(fileName));){
			
			String line;
			
			while ((line = br.readLine()) != null) {
				//do something with line
				System.out.println(line);
			}
		} catch (IOException ex) {
			// Oh No!
			System.out.println("error occured in retrieving file");
		}
		
	}
	
}
