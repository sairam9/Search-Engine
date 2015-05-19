
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class DiskEngine {
    private String mPath;

   public static void main(String[] args) {
       DiskEngine de = new DiskEngine();
       ProcessQuery1 pq = new ProcessQuery1();
       QueryResult[] qr = new QueryResult[1];
      Scanner scan = new Scanner(System.in);

      System.out.println("Menu:");
      System.out.println("1) Build index");
      System.out.println("2) Read and query index");
      System.out.println("Choose a selection:");
      int menuChoice = scan.nextInt();
      scan.nextLine();

      switch (menuChoice) {
         case 1:
            System.out.println("Enter the name of a directory to index: ");
            String folder = scan.nextLine();

            IndexWriter writer = new IndexWriter(folder);
            writer.buildIndex();
            break;

         case 2:
            System.out.println("Enter the name of an index to read:");
            String indexName = scan.nextLine();
            de.mPath = indexName;
            int retrievalType = 1;

            DiskPositionalIndex index = new DiskPositionalIndex(indexName);

            QueryResult[] postings;
            while (true) {
               List<QueryResult> result = new ArrayList<QueryResult>();

               System.out.println("Enter one or more search terms, separated " +
               "by spaces:");
               String input = scan.nextLine();
               if (input.equals("EXIT")) {
                  break;
               }
               System.out.println("Select retrieval type:");
               System.out.println("1: Boolean retrieval.");
               System.out.println("2: Ranked retrieval.");
               retrievalType = Integer.parseInt(scan.nextLine());
               switch(retrievalType) {
                   case 1: // Boolean retrieval.
                       result = pq.process1(input, index);
                       postings = new QueryResult[result.size()];
                       postings = result.toArray(qr);
                       break;
                   
                   case 2: // ranked retrieval.
//               int[] rankedRetrieval = index.rankedRetrieval(input, 0);
                       System.out.println("Select a formula:");
                       System.out.println("1. Default");
                       System.out.println("2. Traditional");
                       System.out.println("3. Okapi");
                       System.out.println("4. Whacky");
               int variant = Integer.parseInt(scan.nextLine());
                       for(QueryResult a: index.rankedRetrieval(input, variant - 1)) {
                           System.out.print(a.getDocId());
                           System.out.println(" weight: " + a.gettftd());
                       }
                       break;
               }
               
               
               boolean isEmpty = true;
//               for(QueryResult q: postings)

               if (result == null) {
                  System.out.println("Term not found");
               }
               else {
                  System.out.print("Docs: ");
                  for (QueryResult post : result) {
//                      System.out.println("post " + post);
                     System.out.println(index.getFileNames().get(post.getDocId()) + " ");
                  }
                  System.out.println("Total docs: " + result.size());
                  System.out.println();
                  System.out.println();
               }

            }

            break;
      }
   }
   
   public String getPath() {
       return mPath;
   }
}
