package org.alopex.Impostor;

import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Filter {
	
	public static int linksDeep = 0;

	public static String getText(String query) {
		StringBuilder sb = new StringBuilder();
		try {
			Document doc = Jsoup.connect(query).get();
			System.out.println("Connected to " + query);
			Elements paragraphs = doc.select(".mw-content-ltr p");
			
			crawlParagraphs(paragraphs, sb);
		    
			if(linksDeep < 5) {
		    	Elements links = paragraphs.select("a");
		    	for(int i=0; i < links.size() / 2; i++) {
			    	String url = (links.remove(new Random().nextInt(links.size())).attr("abs:href"));
			    	System.out.println("\tDeep crawling " + url + " | " + linksDeep);
			    	getText(url);
		    	}
		    }
		} catch (Exception ex) {
			System.out.println("\tDead end: " + query);
		}
		return sb.toString();
	}
	
	private static void crawlParagraphs(Elements paragraphs, StringBuilder sb) {
		linksDeep++;
	    for(int i=0; i < paragraphs.size() / 2; i++) {
	    	String processed = paragraphs.get(i).text().replaceAll("\\(.*?\\) ?", "").replaceAll("\\[.*?\\] ?", "").replaceAll("[,.!?;:]", "$0 ").replaceAll("\\s+", " ");
	    	sb.append(processed);
	    }
	}
}
