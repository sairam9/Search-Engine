import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
Writes an inverted indexing of a directory to disk.
*/
public class IndexWriter {

   private String mFolderPath;
   private static HashMap<String, Integer> tftdMap = new HashMap<String, Integer>();
   private int docCount = 0;
   private static Double[] wdt;
   public static List<Float> weightAndAvg = new ArrayList<Float>();

   public IndexWriter() {
       
   }
   /**
   Constructs an IndexWriter object which is prepared to index the given folder.
   */
   public IndexWriter(String folderPath) {
      mFolderPath = folderPath;
   }

   /**
   Builds and writes an inverted index to disk. Creates three files: 
   vocab.bin, containing the vocabulary of the corpus; 
   postings.bin, containing the postings list of document IDs;
   vocabTable.bin, containing a table that maps vocab terms to postings locations
   */
   public void buildIndex() {
      buildIndexForDirectory(mFolderPath);
   }

   /**
   Builds the normal PositionalInvertedIndex for the folder.
   */
   private static void buildIndexForDirectory(String folder) {
      PositionalInvertedIndex index = new PositionalInvertedIndex();
      IndexWriter iw = new IndexWriter();

      // Index the directory using a positional index
      indexFiles(folder, index);

			// at this point, "index" contains the in-memory inverted index 
      // now we save the index to disk, building three files: the postings index,
      // the vocabulary list, and the vocabulary table.

      // the array of terms
//      String[] dictionary = index.getDictionary();
      String[] dictionary = index.getVocabulary();
      // an array of positions in the vocabulary file
      long[] vocabPositions = new long[dictionary.length];

      buildVocabFile(folder, dictionary, vocabPositions);
      buildPostingsFile(folder, index, dictionary, vocabPositions);
      buildDocWeightsFile(folder, iw.weightAndAvg);
   }

   /**
   Builds the postings.bin file for the indexed directory, using the given
   PositionalInvertedIndex of that directory.
   */
   private static void buildPostingsFile(String folder, PositionalInvertedIndex index,
    String[] dictionary, long[] vocabPositions) {
      FileOutputStream postingsFile = null;
      try {
         postingsFile = new FileOutputStream(
          new File(folder, "postings.bin")
         );

         // simultaneously build the vocabulary table on disk, mapping a term index to a
         // file location in the postings file.
         FileOutputStream vocabTable = new FileOutputStream(
          new File(folder, "vocabTable.bin")
         );

         // the first thing we must write to the vocabTable file is the number of vocab terms.
         byte[] tSize = ByteBuffer.allocate(4)
          .putInt(dictionary.length).array();
         vocabTable.write(tSize, 0, tSize.length);
         int vocabI = 0;
         for (String s : dictionary) {
            // for each String in dictionary, retrieve its postings.
//            PositionalInvertedIndex.PositionalPosting post = null;
            List<PositionalInvertedIndex.PositionalPosting> posPostings = index.getPosList(s);
//            List<Integer> postings = index.getPositions(s);
//            int postingSize = index.getTermCount();

            // write the vocab table entry for this term: the byte location of the term in the vocab list file,
            // and the byte location of the postings for the term in the postings file.
            byte[] vPositionBytes = ByteBuffer.allocate(8)
             .putLong(vocabPositions[vocabI]).array();
            vocabTable.write(vPositionBytes, 0, vPositionBytes.length);

            byte[] pPositionBytes = ByteBuffer.allocate(8)
             .putLong(postingsFile.getChannel().position()).array();
            vocabTable.write(pPositionBytes, 0, pPositionBytes.length);

            // write the postings file for this term. first, the document frequency for the term, then
            // the document IDs, encoded as gaps.
            byte[] docFreqBytes = ByteBuffer.allocate(4)
             .putInt(posPostings.size()).array();
            postingsFile.write(docFreqBytes, 0, docFreqBytes.length);

            
            // assignment 6 part
//            for(int i = 0; i < postings.size() - 1; i++) {
//                byte[] docIdBytes = ByteBuffer.allocate(4)
//                .putInt(postings.get(i + 1) - postings.get(i)).array();
//            }
            
            
            int lastDocId = 0;
            int docId = 0;
            for(PositionalInvertedIndex.PositionalPosting pp: posPostings) {
//            for (int docId : positions) {
               List<Integer> posList = pp.getPositions();
               docId = pp.getDocumentID();
               byte[] docIdBytes = ByteBuffer.allocate(4)
                .putInt(docId - lastDocId).array(); // encode a gap, not a doc ID
               postingsFile.write(docIdBytes, 0, docIdBytes.length);
//               if(s.equalsIgnoreCase("the"))
//                   System.out.println("docId, gap: " + docId + ", " + (docId - lastDocId) +" " + lastDocId);
               lastDocId = docId;
               
               // writing position frequency to the file
               byte[] posFreqBytes = ByteBuffer.allocate(4)
                       .putInt(posList.size()).array();
               postingsFile.write(posFreqBytes, 0, posFreqBytes.length);
//               if(s.equalsIgnoreCase("the"))
//                   System.out.println("posFreq: " +posList.size());
               
               // writing each position to the file
               int prev = 0;

//               for(int pos = 0; pos < (posList.size() - 1); pos++) {
//                   next = posList.get(pos + 1);
//                   System.out.println("current and next:" + current + " " + next);
//                   byte[] posByte = ByteBuffer.allocate(4)
//                           .putInt(next - current).array();
//                   postingsFile.write(posByte, 0, posByte.length);
//               }
               for(int i = 0; i < posList.size(); i++) {
                   byte[] posBytes = ByteBuffer.allocate(4)
                   .putInt(posList.get(i) - prev).array();
                   postingsFile.write(posBytes, 0, posBytes.length);
                   prev = posList.get(i);
               }
            }

            vocabI++;
         }
         vocabTable.close();
         postingsFile.close();
      }
      catch (FileNotFoundException ex) {
      }
      catch (IOException ex) {
      }
      finally {
         try {
            postingsFile.close();
         }
         catch (IOException ex) {
         }
      }
   }

   private static void buildVocabFile(String folder, String[] dictionary,
    long[] vocabPositions) {
      OutputStreamWriter vocabList = null;
      try {
         // first build the vocabulary list: a file of each vocab word concatenated together.
         // also build an array associating each term with its byte location in this file.
         int vocabI = 0;
         vocabList = new OutputStreamWriter(
          new FileOutputStream(new File(folder, "vocab.bin")), "ASCII"
         );
         
         int vocabPos = 0;
         for (String vocabWord : dictionary) {
            // for each String in dictionary, save the byte position where that term will start in the vocab file.
            vocabPositions[vocabI] = vocabPos;
            vocabList.write(vocabWord); // then write the String
            vocabI++;
            vocabPos += vocabWord.length();
         }
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
      catch (UnsupportedEncodingException ex) {
         System.out.println(ex.toString());
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      finally {
         try {
            vocabList.close();
         }
         catch (IOException ex) {
            System.out.println(ex.toString());
         }
      }
   }
   
   private static void buildDocWeightsFile(String folder, List<Float> wnAvg) {
       FileOutputStream weightList;
       float avgWeight = 0f;
       try {
           weightList = new FileOutputStream(new File(folder, "docWeights.bin"));
           float f = 0f;
           System.out.println("building docWeight file. wnAvg size: " + wnAvg.size());
           for(int i = 0; i < wnAvg.size(); i++) {
               f = wnAvg.get(i);
               byte[] docWeight = ByteBuffer.allocate(4)
                       .putFloat(f).array();
               weightList.write(docWeight, 0, docWeight.length);
               avgWeight = avgWeight + f;
               f = wnAvg.get(i);
               docWeight = ByteBuffer.allocate(4)
                       .putFloat(f).array();
               weightList.write(docWeight, 0, docWeight.length);
           }
           avgWeight = avgWeight / (wnAvg.size() / 2);
           byte[] avg = ByteBuffer.allocate(4)
                   .putFloat(avgWeight).array();
           weightList.write(avg, 0, avg.length);
           
       } catch(Exception e) {
           
       }
       
   }

   private static void indexFiles(String folder, final PositionalInvertedIndex index) {
      final Path currentWorkingPath = Paths.get(folder).toAbsolutePath();
      final IndexWriter iw = new IndexWriter();
      
      try {
         Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
            
            int mDocumentID  = 0;
            public FileVisitResult preVisitDirectory(Path dir,
             BasicFileAttributes attrs) {
               // make sure we only process the current working directory
               if (currentWorkingPath.equals(dir)) {
                  return FileVisitResult.CONTINUE;
               }
               return FileVisitResult.SKIP_SUBTREE;
            }
            
            public FileVisitResult visitFile(Path file,
             BasicFileAttributes attrs) {
                
               // only process .txt files
               if (file.toString().endsWith(".txt")) {
                  // we have found a .txt file; add its name to the fileName list,
                  // then index the file and increase the document ID counter.
                  // System.out.println("Indexing file " + file.getFileName());
                  
                  System.out.println(iw.tftdMap.size() + " indexfiles before");
                  indexFile(file.toFile(), index, mDocumentID);
                  Float w = iw.normalize(); // W after normalization.
                  int totalTFTD = 0;
                  for(String term: iw.tftdMap.keySet()) {
                      totalTFTD += iw.tftdMap.get(term);
                  }
                  System.out.println(iw.tftdMap.size() + " indexfiles" + totalTFTD);
                  float avg = totalTFTD / iw.tftdMap.size();
                  iw.weightAndAvg.add(w);
                  iw.weightAndAvg.add(avg);
                  iw.setDocCount(mDocumentID); // is counting right?
                  mDocumentID++;
               }
               tftdMap.clear();
               return FileVisitResult.CONTINUE;
            }
            
            // don't throw exceptions if files are locked/other errors occur
            public FileVisitResult visitFileFailed(Path file,
             IOException e) {
               
               return FileVisitResult.CONTINUE;
            }
            
         });
         
      }
      catch (IOException ex) {
         Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private static void indexFile(File fileName, PositionalInvertedIndex index,
    int documentID) {
       IndexWriter iW = new IndexWriter();
       
      try {
         SimpleTokenStream stream = new SimpleTokenStream(fileName);
         int pos = 0;
         while (stream.hasNextToken()) {
            String term = stream.nextToken();
            String stemmed = PorterStemmer.processToken(term);

            if (stemmed != null && stemmed.length() > 0) {
               index.addTerm(stemmed, documentID,pos);
               iW.updateTFTD(stemmed);
               pos++;
            }
         }
      }
      catch (Exception ex) {
         System.out.println(ex.toString());
      }
   }
   
   private void updateTFTD(String term) {
       if(!tftdMap.containsKey(term)) {
           tftdMap.put(term, 1);
       }
       else {
           tftdMap.put(term, tftdMap.get(term) + 1);
       }
       System.out.println(tftdMap.size());
   }
   
   private Float normalize() {
       
       wdt = new Double[tftdMap.size()];
//       String[] terms = tftdMap.keySet().toArray(new String[0]);
       int i = 0;
       for(String s: tftdMap.keySet()) {
//           terms[i] = s;
//           System.out.println(tftdMap.get(s) + "tftdMap for " + s);
           wdt[i] = 1 + Math.log1p(tftdMap.get(s));
           i++;
       }
       System.out.println("normalizing. wdt size: " + wdt.length + "tftdMap size " + tftdMap.size());
       Double wd = 0.0;
       for(Double d: wdt) {
           System.out.println("d: " + d);
           wd = wd + d * d;
           System.out.println("wd = " + wd);
       }
       wd = Math.sqrt(wd);
       System.out.println("wd final = " + wd);
       return wd.floatValue();
   }
   
   private void setDocCount(int n) {
       docCount = n;
   }
   
   public static int getDocCount(){
       IndexWriter iw = new IndexWriter();
       return iw.docCount;
   }
}
