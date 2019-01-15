import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class InvertedIndex {
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		System.out.println("How many words do you want to enter?");
		int noWords = scanner.nextInt();
		String[] wordsArr = new String[noWords];
		HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<>();
		HashMap<Integer, String> docIdNameMap = new HashMap<>();

		String directoryPath = "D:\\SEM 6\\Information Retrieval\\Lab\\Lab 1\\Lab 1 PDFs";
		File searchDirectory = new File(directoryPath);

		if (noWords > 0) {
			System.out.println("\nNow enter " + noWords + " words: ");

			// Input all the words
			for (int i = 0; i < noWords; i++) {
				wordsArr[i] = scanner.next();

				// Create a posting list for the entered word and add it to the hash map
				LinkedList<Integer> postingList = new LinkedList<>();
				invertedIndex.put(wordsArr[i], postingList);
			}

			// Create the inverted index for each word in the array
			PDFTextStripper pdfTextStripper = new PDFTextStripper();
			int docId = 1;
			for (File document : searchDirectory.listFiles()) {
				PDDocument pdDocument = PDDocument.load(document);
				String documentText = pdfTextStripper.getText(pdDocument);
				StringTokenizer docTokenizer = new StringTokenizer(documentText, " ,.?");
				while (docTokenizer.hasMoreTokens()) {
					String docToken = docTokenizer.nextToken();
					for (int i = 0; i < noWords; i++) {
						if (docToken.equalsIgnoreCase(wordsArr[i])) {
							if (!invertedIndex.get(wordsArr[i]).contains(docId))
								invertedIndex.get(wordsArr[i]).add(docId);
						}
					}
				}
				docIdNameMap.put(docId, document.getName());
				docId++;
				pdDocument.close();
			}

			System.out.println("\nThe Inverted Index is: \n");
			for (int i = 0; i < noWords; i++) {
				System.out.print(wordsArr[i] + "");
				for (int doc : invertedIndex.get(wordsArr[i])) {
					System.out.print("-> " + doc);
				}
				System.out.println("");
			}

			System.out.println("\nDocument ID - Name Mapping:");
			for (int i = 0; i < docIdNameMap.size(); i++) {
				System.out.println(i + 1 + " -> " + docIdNameMap.get(i + 1));
			}
		}

		String queryString = "";
		while (true) {
			System.out.println();

			System.out.println(
					"Enter your search query with AND, OR and NOT operations represented as below or enter 0 to exit:");
			System.out.println("AND - 'x'");
			System.out.println("OR - '+'");
			System.out.println("NOT - '!'");
			System.out.println(
					"Represent the words in the query string as numbers according to the below mentioned mapping: ");
			// Search Queries input
			for (int i = 0; i < wordsArr.length; i++) {
				System.out.println(wordsArr[i] + " - '" + (i + 1) + "'");
			}
			System.out.println();

			queryString = scanner.next();
			if (queryString.equals("0")) {
				System.out.println("Pichha Lite teesko! Denge poi!");
				break;
			}

			LinkedList<Integer> resultPostingList = new LinkedList<>();
			LinkedList<Integer> currentTemp = null;
			for (int i = 0; i < queryString.length() - 1; i++) {
				switch (queryString.charAt(i)) {
				case '+':
					if (queryString.charAt(i + 1) == '!') {
						LinkedList<Integer> notResult = computeNot(
								invertedIndex
										.get(wordsArr[Integer.parseInt(String.valueOf(queryString.charAt(i + 2))) - 1]),
								searchDirectory.listFiles().length);
						LinkedList<Integer> orResult = computeOr(notResult, currentTemp);
						currentTemp = orResult;
						i++;
					} else {
						LinkedList<Integer> orResult = computeOr(
								invertedIndex
										.get(wordsArr[Integer.parseInt(String.valueOf(queryString.charAt(i + 1))) - 1]),
								currentTemp);
						currentTemp = orResult;
					}
					break;

				case 'x':
					if (queryString.charAt(i + 1) == '!') {
						LinkedList<Integer> notResult = computeNot(
								invertedIndex
										.get(wordsArr[Integer.parseInt(String.valueOf(queryString.charAt(i + 2))) - 1]),
								searchDirectory.listFiles().length);
						LinkedList<Integer> andResult = computeAnd(notResult, currentTemp);
						currentTemp = andResult;
						i++;
					} else {
						LinkedList<Integer> andResult = computeAnd(
								invertedIndex
										.get(wordsArr[Integer.parseInt(String.valueOf(queryString.charAt(i + 1))) - 1]),
								currentTemp);
						currentTemp = andResult;
					}
					break;

				case '!':
					LinkedList<Integer> notResult = computeNot(
							invertedIndex
									.get(wordsArr[Integer.parseInt(String.valueOf(queryString.charAt(i + 1))) - 1]),
							searchDirectory.listFiles().length);
					currentTemp = notResult;
					break;

				default:
					if (currentTemp == null)
						currentTemp = invertedIndex
								.get(wordsArr[Integer.parseInt(String.valueOf(queryString.charAt(i))) - 1]);
					break;
				}
			}
			resultPostingList = currentTemp;

			System.out.println("\n\n" + resultPostingList);
		}
	}

	private static LinkedList<Integer> computeOr(LinkedList<Integer> firstList, LinkedList<Integer> secondList) {
		LinkedList<Integer> newList = new LinkedList<>();
		newList.addAll(firstList);
		for (int i : secondList) {
			if (!newList.contains(i))
				newList.add(i);
		}
		Collections.sort(newList);

		return newList;
	}

	private static LinkedList<Integer> computeAnd(LinkedList<Integer> firstList, LinkedList<Integer> secondList) {
		LinkedList<Integer> newList = new LinkedList<>();
		Integer temp1;
		Integer temp2;
		int cnt1 = 0;
		int cnt2 = 0;

		while (cnt1 != firstList.size() && cnt2 != secondList.size()) {
			temp1 = firstList.get(cnt1);
			temp2 = secondList.get(cnt2);

			if (temp1.equals(temp2)) {
				newList.add(temp1);
				cnt1++;
				cnt2++;
			} else if (temp1 < temp2) {
				cnt1++;
			} else {
				cnt2++;
			}
		}

		return newList;
	}

	private static LinkedList<Integer> computeNot(LinkedList<Integer> currentPostingList, int totalDocuments) {
		LinkedList<Integer> newList = new LinkedList<>();
		for (int i = 1; i <= totalDocuments; i++) {
			if (!currentPostingList.contains(i))
				newList.add(i);
		}

		return newList;
	}
}
