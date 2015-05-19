
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiskPositionalIndex {

   private String mPath;
   private RandomAccessFile mVocabList;
   private RandomAccessFile mPostings;
   private long[] mVocabTable;
   private List<String> mFileNames;
   private RandomAccessFile mDocWeight;
   private static int termFreqC;
   
//   public DiskPositionalIndex() {
//       
//   }

   public DiskPositionalIndex(String path) {
      try {
         mPath = path;
         mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
         mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
         mVocabTable = readVocabTable(path);
         mFileNames = readFileNames(path);
         mDocWeight = new RandomAccessFile(new File(path, "docWeights.bin"), "r");
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
   }

   private static List<QueryResult> readPostingsFromFile(RandomAccessFile postings, 
    long postingsPosition) {
      try {
          QueryResult qRes;
          List<QueryResult> resList = new ArrayList<QueryResult>();

         // seek to the position in the file where the postings start.
         postings.seek(postingsPosition);
         
         // read the 4 bytes for the document frequency
         byte[] buffer = new byte[4];
         postings.read(buffer, 0, buffer.length);

         // use ByteBuffer to convert the 4 bytes into an int.
         int documentFrequency = ByteBuffer.wrap(buffer).getInt();
         QueryResult[] result = new QueryResult[documentFrequency];
         

         // initialize the array that will hold the postings. 
         // write the following code:
         // read 4 bytes at a time from the file, until you have read as many
         //    postings as the document frequency promised.
         //    
         // after each read, convert the bytes to an int posting. this value
         //    is the GAP since the last posting. decode the document ID from
         //    the gap and put it in the array.
         // repeat until all postings are read.

         int prevId = 0;
         int gap = 0;
         int posFreq = 0;
         System.out.println("diskpositionalindex");
         System.out.println("docFrequency: " + documentFrequency);
         
         for(int i = 0; i < documentFrequency; i++) {
//             postingsPosition += 4;
//             postings.seek(postingsPosition);
             qRes = new QueryResult();

             // reading document id
             postings.read(buffer, 0, buffer.length);
             gap = ByteBuffer.wrap(buffer).getInt();
             prevId += gap;
             qRes.setDocId(prevId);
//             System.out.print("docID, gap: " + prevId + " " + gap + " ");
             
             // assignment 6 addition
             postings.read(buffer, 0, buffer.length);
             posFreq = ByteBuffer.wrap(buffer).getInt();
             qRes.settftd(posFreq);
//             System.out.println("posFreq: " + posFreq);
             
             // read positional data
             List<Integer> posting = new ArrayList<Integer>();
             int prevPos = 0;
             int postGap = 0;
             for(int j = 0; j < posFreq; j++) {
                 postings.read(buffer, 0, buffer.length);
                 postGap = ByteBuffer.wrap(buffer).getInt();
                 posting.add(prevPos + postGap);
                 prevPos = prevPos+ postGap;
             }
             qRes.setPostings(posting);
             result[i] = qRes;
             resList.add(qRes);
             
         }
         return resList;
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }
   
   private static Float readDocWeight (RandomAccessFile weight, int docId) {
       Float docWeight = 0.0F;
       try {
           weight.seek(docId * 4 * 2);
           byte[] buffer = new byte[4];
           weight.read(buffer, 0, buffer.length);
           
           docWeight = ByteBuffer.wrap(buffer).getFloat();
       } catch(Exception e) {
           
       }
       return docWeight;
   }
   
   public float getAvgDocWeight() {
       try {
           mDocWeight.seek(4 * IndexWriter.getDocCount() * 2);
           byte[] buffer = new byte[4];
           mDocWeight.read(buffer, 0, buffer.length);
           return ByteBuffer.wrap(buffer).getFloat();
       } catch (IOException ex) {
           Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
       }
       return 0f;
   }
   
   public float getAvgTFTD(int docId) {
       try {
           RandomAccessFile weight = new RandomAccessFile(new File(mPath, "docWeights.bin"), "r");
           weight.seek(docId * 4 * 2 + 4);
           byte[] buffer = new byte[4];
           weight.read(buffer, 0, buffer.length);
           return ByteBuffer.wrap(buffer).getFloat();
       } catch (FileNotFoundException ex) {
           Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
       } catch (IOException ex) {
           Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
       }
       return 0;
   }
   
   public Float getDocWeight(int docId) {
       return readDocWeight(mDocWeight, docId);
   }

   public List<QueryResult> GetPostings(String term) {
//      long postingsPosition = BinarySearchVocabulary(PorterStemmer.processToken(term).trim().toLowerCase());
      long postingsPosition = BinarySearchVocabulary(term.trim().toLowerCase());
      System.out.println("binary search vocabulary: " + term + " " + term.length() + " " + postingsPosition);
      if (postingsPosition >= 0) {
         return readPostingsFromFile(mPostings, postingsPosition);
      }
      return null;
   }

   private long BinarySearchVocabulary(String term) {
      // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
      int i = 0, j = mVocabTable.length / 2 - 1;
      while (i <= j) {
         try {
            int m = (i + j) / 2;
            long vListPosition = mVocabTable[m * 2];
            int termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
            mVocabList.seek(vListPosition);

            byte[] buffer = new byte[termLength];
            mVocabList.read(buffer, 0, termLength);
            String fileTerm = new String(buffer, "ASCII");

            int compareValue = term.compareTo(fileTerm);
            if (compareValue == 0) {
               // found it!
               return mVocabTable[m * 2 + 1];
            }
            else if (compareValue < 0) {
               j = m - 1;
            }
            else {
               i = m + 1;
            }
         }
         catch (IOException ex) {
            System.out.println(ex.toString());
         }
      }
      return -1;
   }


   private static List<String> readFileNames(String indexName) {
      try {
         final List<String> names = new ArrayList<String>();
         final Path currentWorkingPath = Paths.get(indexName).toAbsolutePath();

         Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
            int mDocumentID = 0;

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
                  names.add(file.toFile().getName());
               }
               return FileVisitResult.CONTINUE;
            }

            // don't throw exceptions if files are locked/other errors occur
            public FileVisitResult visitFileFailed(Path file,
             IOException e) {

               return FileVisitResult.CONTINUE;
            }

         });
         return names;
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }

   private static long[] readVocabTable(String indexName) {
      try {
         long[] vocabTable;
         
         RandomAccessFile tableFile = new RandomAccessFile(
          new File(indexName, "vocabTable.bin"),
          "r");
         
         byte[] byteBuffer = new byte[4];
         tableFile.read(byteBuffer, 0, byteBuffer.length);
        
         int tableIndex = 0;
         vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
         byteBuffer = new byte[8];
         
         while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 4 bytes
            vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
            tableIndex++;
         }
         tableFile.close();
         return vocabTable;
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }

   public List<String> getFileNames() {
      return mFileNames;
   }
   
   public int getTermCount() {
      return mVocabTable.length / 2;
   }
   
   public int getTermFreq(String term) {
       term = PorterStemmer.processToken(term);
       long postingsPosition = BinarySearchVocabulary(term.trim());
       System.out.println("getTermFreq diskpositionalindex: term : " + term);
       try {
           RandomAccessFile posting = new RandomAccessFile(new File(mPath, "postings.bin"), "r");
           posting.seek(postingsPosition);
           
           byte[] buffer = new byte[4];
           posting.read(buffer, 0, buffer.length);
           
           int termFreq = ByteBuffer.wrap(buffer).getInt();
           termFreqC = termFreq;
           return termFreq;
//           return getTermFreqS();
           
       } catch (FileNotFoundException ex) {
           Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
       } catch (IOException ex) {
           Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
       }
       return 0;
   }
   
   public static int getTermFreqS() {
       return termFreqC;
   }
   
   public List<QueryResult> rankedRetrieval(String query, int n) {
       HashMap<Integer, Float> accumulator = new HashMap<Integer, Float>();
       IndexWriter iw = new IndexWriter();
       float avgDocWeight = getAvgDocWeight();
       int docCount = iw.getDocCount(); // N = docCount = number of documents.
       for(int i = 0; i < docCount; i++) {
           accumulator.put(i, 0f);
       }
       int dft = 0; // df-t = freq of a term.
        
       String[] splitAtSpace = query.split(" ");
       
       float wd = 0;
       PriorityQueue<BinaryHeapPQ> ranked = new PriorityQueue<BinaryHeapPQ>();
       switch(n) {
           case 0:
               DefaultWeighting dw;
//               BinaryHeap bh = new BinaryHeap();
               for(String str: splitAtSpace) {
                   dft = getTermFreq(str);
                   List<QueryResult> postings = GetPostings(PorterStemmer.processToken(str));
                   System.out.println("switch case 0, posting size:" + postings.size());
                   for(int i = 0; i < postings.size(); i++) {
                       float tftd = postings.get(i).gettftd();
                       dw = new DefaultWeighting(n, dft, tftd);

                       float ad;
                       if(accumulator.get(postings.get(i).getDocId()) == null) {
                           ad = 0f;
                       }
                       else{
                           ad = accumulator.get(postings.get(i).getDocId());
                       }
                       ad = ad + dw.calculateWeights();
                       accumulator.put(i, ad);
                   }
               }
               
               for(int id: accumulator.keySet()) {
                   if(accumulator.get(id) > 0) {
                       Float ad = accumulator.get(id);
                       ad = ad / getDocWeight(id); // need to have weights Wd according to different schemes
                       ranked.add(new BinaryHeapPQ(id, accumulator.get(id)));
                   }
               }
               
               List<QueryResult> res = new ArrayList<QueryResult>();
               int a = 0;
               for(BinaryHeapPQ d: ranked) {
                   QueryResult q = new QueryResult();
                   q.setDocId(d.docId);
                   q.settftd(d.tftd);
                   res.add(q);
                   if(a > 9) {
                   break;
                   }
                   a++;
               }
               System.out.println("printing result:");
               for(int i = 0; i < 10; i++) {
                   BinaryHeapPQ poll = ranked.poll();
                   System.out.println(getFileNames().get(poll.docId) + " " + poll.tftd);
                   
               }
//               Collections.sort(res);
               return res;
               
               
//           break;
               
           case 1:
               TraditionalWeighting tw;
//               PriorityQueue11<Default> queue = new PriorityQueue11<Default>();
               for(String str: splitAtSpace) {
                   dft = getTermFreq(str);
                   List<QueryResult> postings = GetPostings(str);
                   for(int i = 0; i < postings.size(); i++) {
                       float tftd = postings.get(i).gettftd();
                       float ad = accumulator.get(postings.get(i).getDocId());
                       tw = new TraditionalWeighting(n, dft, tftd);
                       ad = ad + tw.calculateWeights();
                       accumulator.put(i, ad);
                   }
               }
               for(int id: accumulator.keySet()) {
                   if(accumulator.get(id) > 0) {
                       Float ad = accumulator.get(id);
                       ad = ad / getDocWeight(id); // need to have weights Wd according to different schemes
                   }
               }
               int an = 0;
               for(BinaryHeapPQ d: ranked) {
                   QueryResult q = new QueryResult();
                   q.setDocId(d.docId);
                   q.settftd(d.tftd);
//                   res.add(q);
                   if(an > 9) {
                   break;
                   }
                   an++;
               }
               System.out.println("printing result:");
               for(int i = 0; i < 10; i++) {
                   BinaryHeapPQ poll = ranked.poll();
                   System.out.println(getFileNames().get(poll.docId) + " " + poll.tftd);
                   
               }
           break;
           
           case 2:
               OkapiWeighting ow;
               for(String str: splitAtSpace) {
                   dft = getTermFreq(str);
                   List<QueryResult> postings = GetPostings(str);
                   for(int i = 0; i < postings.size(); i++) {
                       float tftd = postings.get(i).gettftd();
                       float ad = accumulator.get(postings.get(i).getDocId());
                       ow = new OkapiWeighting(n, dft, tftd, getDocWeight(postings.get(i).getDocId()), avgDocWeight);
                       ad = ad + ow.calculateWeights();
                       accumulator.put(i, ad);
                   }
               }
               int am = 0;
               for(int id: accumulator.keySet()) {
                   if(accumulator.get(id) > 0) {
                       Float ad = accumulator.get(id);
                       ad = ad / 1; // need to have weights Wd according to different schemes
                   }
                   for(BinaryHeapPQ d: ranked) {
                   QueryResult q = new QueryResult();
                   q.setDocId(d.docId);
                   q.settftd(d.tftd);
//                   res.add(q);
                   if(am > 9) {
                   break;
                   }
                   am++;
               }
               System.out.println("printing result:");
               for(int i = 0; i < 10; i++) {
                   BinaryHeapPQ poll = ranked.poll();
                   System.out.println(getFileNames().get(poll.docId) + " " + poll.tftd);
                   
               }
               }
           break;
           
           case 3:
               WackyWeighting ww;
               for(String str: splitAtSpace) {
                   dft = getTermFreq(str);
                   List<QueryResult> postings = GetPostings(str);
                   for(int i = 0; i < postings.size(); i++) {
                       float tftd = postings.get(i).gettftd();
                       float ad = accumulator.get(postings.get(i).getDocId());
                       ww = new WackyWeighting(n, dft, tftd, getDocWeight(postings.get(i).getDocId()),
                               avgDocWeight, getAvgTFTD(postings.get(i).getDocId()));
                       ad = ad + ww.calculateWeights();
                       accumulator.put(i, ad);
                   }
               }
           for(int id: accumulator.keySet()) {
           if(accumulator.get(id) > 0) {
               Float ad = accumulator.get(id);
               ad = ad / getDocWeight(id); // need to have weights Wd according to different schemes
           }
           int al = 0;
                   for(BinaryHeapPQ d: ranked) {
                   QueryResult q = new QueryResult();
                   q.setDocId(d.docId);
                   q.settftd(d.tftd);
//                   res.add(q);
                   if(al > 9) {
                   break;
                   }
                   al++;
               }
               System.out.println("printing result:");
               for(int i = 0; i < 10; i++) {
                   BinaryHeapPQ poll = ranked.poll();
                   System.out.println(getFileNames().get(poll.docId) + " " + poll.tftd);
                   
               }
       }
               
       }

//       for(int id: accumulator.keySet()) {
//           if(accumulator.get(id) > 0) {
//               Float ad = accumulator.get(id);
//               ad = ad / getDocWeight(id); // need to have weights Wd according to different schemes
//           }
//       }
       return null;
    }
   
   static class BinaryHeapPQ implements Comparable {
       
       int docId;
       float tftd;
       
       public BinaryHeapPQ(int id, float t) {
           docId = id;
           tftd = t;
       }

        @Override
        public int compareTo(Object o) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            
            if(tftd == ((BinaryHeapPQ)o).gettftd()){
                return 0;
            }
            else if(tftd < ((BinaryHeapPQ)o).gettftd()) {
                return -1;
            }
            else {
                return 1;
            }
        }
        
        public int getId() {
            return docId;
        }
        
        public float gettftd() {
            return tftd;
        }
       
   }
}
