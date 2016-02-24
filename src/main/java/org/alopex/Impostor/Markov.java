package org.alopex.Impostor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

public class Markov {
	
	public static final boolean UNIQUE_MODE = false;        //Option for unique chaining

	private static HashMap<String, ArrayList<String>> data; //Mapping / grab-bag model for chaining
	private static ArrayList<String> input;                 //Container for word-basis plain text

	/**
	 * Main point of entry in program; prompts user for number of chains to generate
	 */
	public static void main(String[] args) {
		System.out.println("==== Markov Chain Generator v0.1 ====");
		
		//Prompt user to input chain count
		Scanner countScan = new Scanner(System.in);
		System.out.print("Enter the number of chains to generate from input: ");
		int iterations = -1;
		if(countScan.hasNextInt()) {
			iterations = countScan.nextInt();
		} else {
			System.out.println("Invalid input.");
			System.out.println();
			main(args);
		}
		
		File inputFile = new File("input.txt");
		String inputStr = "";
		boolean fileFound = false;
		
		if(inputFile.exists()) {
			System.out.println("Loading corpus from input file...");
			System.out.println();
			fileFound = true;
		} else {
			System.out.println("No input file found; falling back to web mode");
			
			if(countScan.hasNext()) {
				String webQuery = countScan.next();
				System.out.println("Locking query to " + webQuery);
				inputStr = Filter.getText("http://en.wikipedia.org/wiki/" + webQuery);
				countScan.close();
			}
		}
		
		if(inputFile != null || inputStr != null) {
			//Initialize and begin chain generation
			if(iterations != -1) {
				if(fileFound) {
					initialize(inputFile);
				} else {
					initialize(inputStr);
				}
				for(int i=0; i < iterations; i++) {
					generateChain(i);
				}
			}
		}
	}
	
	/**
	 * Initializes the input ArrayList container prior to looped chain generation
	 */
	private static void initialize(File inputFile) {
		//Initialize local variable for storing words
		input = new ArrayList<String> ();

		//Read words from a plain text file, word by word
		try {
			Scanner scanOne = new Scanner(inputFile);
			while(scanOne.hasNextLine()) {
				Scanner scanTwo = new Scanner(scanOne.nextLine());
				while(scanTwo.hasNext()) {
					String proposedInput = scanTwo.next();
					if(!proposedInput.startsWith("//")) {
						input.add(proposedInput);
					}
				}
				scanTwo.close();
			}
			scanOne.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Initializes the input ArrayList container prior to looped chain generation
	 */
	private static void initialize(String inputString) {
		//Initialize local variable for storing words
		input = new ArrayList<String> ();

		//Read words from a plain text file, word by word
		try {
			Scanner scanOne = new Scanner(inputString);
			while(scanOne.hasNextLine()) {
				Scanner scanTwo = new Scanner(scanOne.nextLine());
				while(scanTwo.hasNext()) {
					String proposedInput = scanTwo.next();
					if(!proposedInput.startsWith("//")) {
						input.add(proposedInput);
					}
				}
				scanTwo.close();
			}
			scanOne.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Generates a chain and outputs it with its chainNumber
	 * @param chainNumber the specific iteration for this generation
	 */
	private static void generateChain(int chainNumber) {
		//Initialize instance variable for mapping the input text
		if(data == null) {
			data = new HashMap<String, ArrayList<String>> ();
		} else {
			data.clear();
		}
		
		//Initialize storage for the output
		ArrayList<String> output = new ArrayList<String> ();
		
		//For each word, bin each word at a depth of 1
		for(int i=0; i < input.size() - 2; i++) {
			String thisWord = input.get(i).trim();
			String nextWord = input.get(i + 1).trim();
			
			ArrayList<String> newEntry = null;
			
			//If there is no key for this word then create an ArrayList of nexts for it
			if(!data.containsKey(thisWord)) {
				newEntry = new ArrayList<String> (); 
			} else {
				//If there is a key then fetch it and set the pointer of newEntry to it
				newEntry = data.get(thisWord);
			}
			newEntry.add(nextWord);
			
			//Set this word's updated index (whether it existed or not)
			data.put(input.get(i), newEntry);
		}
		
		//Pull all the keys from the map
		ArrayList<String> keys = processKeys();
		
		//Now, we construct the chain by choosing any bin at random
		Iterator<String> firstWordIter = keys.iterator();
		
		while(output.size() < 1 && firstWordIter.hasNext()) {
			//Get a bin as an entry
			String firstWord = firstWordIter.next();
			
			//Search for a capitalized iteration
			while(firstWord.toLowerCase().equals(firstWord) || firstWord.startsWith("/")) {
				//Move to the next word
				firstWord = firstWordIter.next();
			}
			
			//Concatenate the bin's key as the working word
			output.add(firstWord);
		}
		
		//Set the suggested key from the concatenation
		String suggestedKey = output.get(0);

		//Loop through chaining until there is enough data
		int counter = 0;
		while(counter <= 50) {
			ArrayList<String> potentialNexts = data.get(suggestedKey);
			
			//If there are potential next values, choose a random string from the mapping
			if(potentialNexts != null && potentialNexts.size() > 0) {
				Collections.shuffle(potentialNexts);
				String chosenPotential = UNIQUE_MODE ? 
										 potentialNexts.remove(new Random().nextInt(potentialNexts.size())) 
										  : potentialNexts.get(new Random().nextInt(potentialNexts.size()));
				if(chosenPotential.equals(" ")) {
					counter--;
				} else {
					output.add(chosenPotential);
				}
				
				//Set next suggestedKey as the chosen string
				suggestedKey = chosenPotential;
			} else {
				//If no mappings are left (in uniqueness mode), break
				break;
			}
			counter++;
		}
		
		//Concatenate the chain with StringBuilder
		StringBuilder sb = new StringBuilder();
		for(String str : output) {
			sb.append(str + " ");
		}
		String finalizedOutput = sb.toString();
		
		//If the sentence generated goes past its last period, remove the excess
		int periodIndex = finalizedOutput.lastIndexOf('.');
		if(periodIndex != -1) {
			finalizedOutput = finalizedOutput.substring(0, periodIndex) + ".";
		} else {
			//Otherwise, place an ellipsis at the end of the phrase
			finalizedOutput += "...";
		}
		
		//Print out line break and StringBuilder's output
		System.out.println();
		System.out.println((chainNumber + 1) + ") " + finalizedOutput);
	}
	
	/**
	 * Pulls keys from the data map for further processing
	 * @return a shuffled ArrayList of keys
	 */
	private static ArrayList<String> processKeys() {
		ArrayList<String> keys = new ArrayList<String> ();
		Iterator<Entry<String, ArrayList<String>>> primIter = data.entrySet().iterator();
		while(primIter.hasNext()) {
			keys.add(primIter.next().getKey());
		}
		
		//Shuffle the keys, such that the first chosen key will be different each iteration
		Collections.shuffle(keys);
		return keys;
	}
}
