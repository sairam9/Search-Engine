import java.util.*;

public class PositionalInvertedIndex {

   public class PositionalPosting {
      private int mDocumentId;
      private List<Integer> mPositions;

      public PositionalPosting(int docID) {
         mDocumentId = docID;
         mPositions = new ArrayList<Integer>();
      }

      public int getDocumentID() {
         return mDocumentId;
      }

      public List<Integer> getPositions() {
         return mPositions;
      }
   }

   private HashMap<String, List<PositionalPosting>> mIndex = 
    new HashMap<String, List<PositionalPosting>>();

   public void addTerm(String term, int documentID, int position) {
//      List<PositionalPosting> postings = null;
//      PositionalPosting post = null;
//
//      if (!mIndex.containsKey(term)) {
//         postings = new ArrayList<PositionalPosting>();
//         mIndex.put(term, postings);
//      }
//      else {
//         postings = mIndex.get(term);
//      }
//
//      if (postings.isEmpty() || postings.get(postings.size() - 1)
//       .getDocumentID() != documentID) {
//         post = new PositionalPosting(documentID);
//         postings.add(post);
//      }
//      else {
//         post = postings.get(postings.size() - 1);
//      }
//
//      if (post.getPositions().isEmpty() || post.getPositions()
//       .get(post.getPositions().size() - 1) != position) {
//         post.getPositions().add(position);
//      }
       
//       System.out.println("addTerm starts " + term);
        if(!mIndex.containsKey(term)) {
//            System.out.println("addTerm fisrt if");
            PositionalPosting pPost = new PositionalPosting(documentID);
            List<PositionalPosting> pPostList = new ArrayList<PositionalPosting>();
            pPostList.add(pPost);
            pPost.getPositions().add(position);
            mIndex.put(term, pPostList);
        }
        else { // if the term is present in the hash table
            List<PositionalPosting> pPostList = mIndex.get(term);
            int len = pPostList.size();
            if(pPostList.get(len - 1).getDocumentID() == documentID) { // if there is an object present for the current document
                pPostList.get(len - 1).getPositions().add(position);
//                mIndex.put(term, pPostList);
            }
            else { // if there is no object present for the current docId, create a new one.
//                List<Integer> posList = new ArrayList<Integer>();
//                posList.add(position);
                PositionalPosting p = new PositionalPosting(documentID);
                p.getPositions().add(position);
                pPostList.add(p);
            }
        }
   }
   
   public int getTermCount() {
      return mIndex.size();
   }
   
   public String[] getVocabulary() {
      String[] terms = mIndex.keySet().toArray(new String[mIndex.size()]);
      Arrays.sort(terms);
      return terms;
   }

   public List<PositionalPosting> getPosList(String s) {
       return mIndex.get(s);
   }
}
