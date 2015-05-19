
import java.util.ArrayList;
import java.util.List;
//import java.io.*;
//import java.nio.file.FileVisitResult;
import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author SachilistOfLists
 */
public class ProcessQuery1 {
    private final List<String> tokens = new ArrayList<String>();
    private final NaiveLogicalMerge nm = new NaiveLogicalMerge();
//    private final NaivePositionalIndex1 mIndex = new NaivePositionalIndex1();    

    
    /**
     * Splits the query literal at quotes, returns a list of String and uses Porter Stemmer.
     * @param str
     * @return
     */
    public List<String> getTokens(String str) {
        if(str.startsWith(" ")){
            str = str.substring(1);
        }
        if(str.endsWith(" ")) {
            str = str.substring(0, str.length() - 2);
        }
        String[] split = str.split("(\\s)+");
        
        tokens.addAll(Arrays.asList(split));
        List<String> asList = Arrays.asList(split);
//        for(String str1: split)
//            System.out.println(str1 + " getTokens");
//        return Arrays.asList(split);
        
        // stem the phrase in users query.
        for(int i = 0; i < asList.size(); i++) {
            System.out.print(asList.get(i) + " " + i);
            if(asList.get(i).endsWith("\"")) {
                System.out.println(PorterStemmer.processToken(asList.get(i).replaceAll("\"", "")) + "getToken last term in quotes");
                asList.set(i, PorterStemmer.processToken(asList.get(i).replaceAll("\"", "")) + "\"");
                
            }
            else {
                asList.set(i, PorterStemmer.processToken(asList.get(i)));
                System.out.println(asList.get(i));
            }
            
        }
        return asList;
    }
    
    /**
     * Returns a list of tokens with phrase query as a single token.
     * Does not use PorterStemmer.
     * converts the string to lowercase.
     * returns the phrase and near queries as a single token.
     * @param str string to be tokenized.
     * @return List of tokens.
     */
    public List<String> getTokens1(String str) {
        List<String> tokens = new ArrayList<String>();
        StringBuffer sb = new StringBuffer(str.toLowerCase());
        StringBuffer phrase = new StringBuffer("");
        int quote = 0;
        int space = 0;
        System.out.println("getTokens1 str: " + str);
        while (sb.length() > 0) {
            System.out.println("sb: " + sb);
            if(sb.toString().contains("near/")) {
                tokens.add(sb.toString());
                return tokens;
            }
            if (sb.toString().startsWith("\"")) {
                quote = sb.indexOf("\"", 1);
                phrase = phrase.append(sb.substring(0, quote + 1));
                tokens.add(phrase.toString());
                sb = sb.delete(0, quote + 2); // deletes the phrase with quote and the space after it
            } else {
                if(sb.charAt(0) == ' ') {
                    sb.deleteCharAt(0);
                }
                space = sb.indexOf(" ");
                if (space > -1) {
                    String token = sb.substring(0, space);
                    System.out.println("token added: " + token);
                    tokens.add(token);
                    sb = sb.delete(0, space + 1);
                } else {
                    tokens.add(sb.toString());
                    sb = sb.delete(0, sb.length());
                }
                
            }
        }
        System.out.println("getTokens1 end. tokens size: " + tokens.size());
        for(String token: tokens) {
            System.out.println(token);
        }
        return tokens;
    }
    
    public List<String> splitAtPlus(String str) {
        return (Arrays.asList(str.split("(\\+)+")));
    }
    
    
    /**
     * Process the query and used in the application.
     * @param query query entered by the user.
     * @param index NaivePositionalIndex class reference.
     * @return List of PositionalPostings for the query.
     */
    public List<QueryResult> process1(String query, DiskPositionalIndex index) {
//        ProcessQuery pq = new ProcessQuery();
        return processQuery(query, index);
    }
    
    /**
     * Processes the query and returns list of PositionalPostings objects. Uses PorterStemmer when getting the postings.
     * @param query String entered by the user
     * @param index reference to the NaivePositionalIndex1 class.
     * @return
     */
    private List<QueryResult> processQuery(String query, DiskPositionalIndex index) {
        ProcessQuery1 pq = new ProcessQuery1();
        List<String> splitPlus = pq.splitAtPlus(query);
//        System.out.println("split at plus: " + splitPlus.size());
        List<QueryResult> resFinal = new ArrayList<QueryResult>();
        for(String s: splitPlus) {
//            System.out.println("for loop processQuery.");
            List<QueryResult> res = new ArrayList<QueryResult>();
            if(s.contains("near/")) {
                return nearQuery(query, index);

            }
            List<String> tokens = pq.getTokens1(s);
//            System.out.println("tokens size: " + tokens.size());
            for(String t: tokens) {
                System.out.println("For token: " + t);
                if(t.startsWith("-")) {
                    if(res.size() == 0) {
                        System.out.println("Incorrect NOT query. Please check your query.");
                        return null;
                    }
                    else {
                        List<QueryResult> postings = index.GetPostings(PorterStemmer.processToken(t.substring(1)));
                        res = nm.not(res, postings);
                    }
                }
                else if(t.startsWith("\"")) {
                    t = t.replaceAll("\"", "");
                    List<QueryResult> phraseRes = pq.phraseQuery1(t, index);
                    res = nm.and(res, phraseRes);
                }
                else {
                    List<QueryResult> postings = index.GetPostings(PorterStemmer.processToken(t));
//                    System.out.println(t + " posting size: " + postings.size() + " res size:" + res.size());
                    if(res.size() < 1) {
                        res = postings;
                    }
                    res = nm.and(res, postings);
                }
            }
            resFinal = nm.or(resFinal, res);
        }
//        System.out.println("resFinal size: " + resFinal.size());
        return resFinal;
    }
   
    
    public void test(String query) {
        List<String> str = new ArrayList<String>();
        str.addAll(Arrays.asList(query.split("\"")));
        System.out.println("test :" + str.size());
        
        for(String s: str) {
            System.out.println(s);
        }
    }

    /**
     * Not used!!
     * @param query
     * @param index
     * @return
     */
//    public List<QueryResult> phraseQuery(String query, DiskPositionalIndex index, int b) {
//        System.out.println("phrasequery called");
//        List<List<QueryResult>> listOfLists = new ArrayList<List<QueryResult>>();
//        List<QueryResult> resultPostings = new ArrayList<QueryResult>();
//        List<QueryResult> temp = new ArrayList<QueryResult>();
//        List<Integer> resultIds = new ArrayList<Integer>();
//        
//        String[] split = query.split("\\s");
//        // get posting lists for all terms and store in a alist
//        
//        for(String s: split) {
//            List<QueryResult> copy1 = new ArrayList<QueryResult>();
//            System.out.println("split in pq (processquery > phrasequery):" + s + s.length());
////            temp = index.getPostings(PorterStemmer.processToken(s));
//            temp = index.GetPostings(s);
////            for(PositionalPosting p: temp) {
////                System.out.println(p.getDocId() + " " + p.getPosList());
////            }
////            System.out.println("temp size " + temp.size());
//            copyList(temp, copy1);
//            System.out.println("copy1 size: " + copy1.size() + "temp size: " + temp.size());
//            listOfLists.add(copy1);
//        }
//        System.out.println("n size" + listOfLists.size());
//        System.out.println("before filtering (processquery 296)");
//        
//        List<QueryResult> first = listOfLists.get(0);
//        for(int i = 1; i < listOfLists.size(); i++) {
//            System.out.println("filtering lists (processquery 306)" + i + (i + 1));
//
//            filterLists(listOfLists.get(i), first);
//        }
//        
//        System.out.println("after filtering");
//        
//        // compare the lists and add the matches to result list
//        for(int i = 0; i <listOfLists.size() - 1; i++) {
//            System.out.println("first for phrase query");
//            List<QueryResult> pList1 = listOfLists.get(i);
//            List<QueryResult> pList2 = listOfLists.get(i + 1);
//            System.out.println("pList1 pList2 size (processQuery phraseQuery) " + pList1.size() + " " + pList2.size());
//            for(int j = 0; j < pList1.size(); j++) {
//                System.out.println("second for phrase query");
//                QueryResult pp1 = pList1.get(j);
//                QueryResult pp2 = pList2.get(j); //gives an error
//                // here 10-10-2014
//                System.out.println("posLists size pp1 and pp2: " + pp1.getPostings().size() + " " + pp2.getPostings().size());
//                boolean bool = compare(pp1.getPostings(), pp2.getPostings(), 1);
////                System.out.println("bool " + bool);
//                if(bool) {
////                    resultIds.add(pp1.getDocId());
////                    resultPostings.add(pp2);
//                    
//                    
//                    if(resultIds.size() == 0) {
//                        resultPostings.add(pp2);
//                        resultIds.add(pp1.getDocId());
//                    }
//                    else {
//                        if(resultIds.contains(pp1.getDocId())) {
//                            resultPostings.add(pp2);
//                            System.out.println("added " + pp1.getDocId());
//                        }
//                    }
//                }
//            }
//        }
//        
//        System.out.println("phrase query result1:");
////        for(QueryResult pp: resultPostings) {
////            System.out.println(pp.getDocId());
////        }
//        System.out.println("pq ending");
//        return resultPostings;
//    }

    /**
     * Processes the phrase query and returns the list of PositionalPostings containing all the terms in the same order one after another.
     * @param query phrase query.
     * @param index NaivePositionalIndex1 reference.
     * @return
     */
    public List<QueryResult> phraseQuery1(String query, DiskPositionalIndex index) {
          List<String> tokens = getTokens1(query);
          List<List<QueryResult>> postLists = new ArrayList<List<QueryResult>>();
          List<QueryResult> postings = new ArrayList<QueryResult>();
          for(String t: tokens) {
              List<QueryResult> copy = new ArrayList<QueryResult>();
              postings = index.GetPostings(PorterStemmer.processToken(t));
              System.out.println("<phraseQuery1> lists size for " + t + ":" + postings.size());
              copyList(postings, copy); // using copies of posting lists so that modifications don't affect the database
              System.out.println("<phraseQuery1> copy size: " + copy.size());
              postLists.add(copy);
//              copy.clear();
          }
          System.out.println("<phraseQuery1> postLists size phraseQuery1: " + postLists.size());
          // a list of booleans to keep track of documents to be added to the result.
          Boolean[] resBool = new Boolean[postLists.get(0).size()];
          for(int i = 0; i < postLists.get(0).size(); i++) {
              resBool[i] = (Boolean.TRUE);
          }
          System.out.println("resBool size after initialization: " + resBool.length);
          for(int i = 0; i < postLists.size() - 1; i++) {
              System.out.println("<phraseQuery1> lists sizes: " + postLists.get(i).size() +
                      " " + postLists.get(i + 1).size());
              filter(postLists.get(i), postLists.get(i + 1)); // filter out the uncommon documents. Makes the lists equal in length.
              System.out.println("<phraseQuery1> lists sizes: " + postLists.get(i).size() + 
                      " " + postLists.get(i + 1).size());
              int m = 0;
              int n = 0;
              for(int j = 0; j < postLists.get(i).size(); j++) {
                  System.out.println("lists " + postLists.get(i).get(j).getDocId() + " " + postLists.get(i + 1).get(j).getDocId());
                  boolean present = compare(postLists.get(i).get(j).getPostings(),
                          postLists.get(i + 1).get(j).getPostings(), 1);
                  resBool[j] = present & resBool[j];
                  System.out.println("added at " + j + " " + "present(compare result) = "
                          + present + "resBool at "+ j + ": " + resBool[j]);
              }
          }
          List<QueryResult> result = new ArrayList<QueryResult>();
          System.out.println("resBool size: " + resBool.length);
          for(int i = 0; i < postLists.get(0).size(); i++) {
              if(resBool[i]) {
                  System.out.println("adding to resBool" + resBool.length + " " + postLists.size());
                  result.add(postLists.get(0).get(i));
                  System.out.println("added to resBool");
              }
          }
          return result;
      }
      
      public List<QueryResult> nearQuery(String str, DiskPositionalIndex index) {
//          String word = "near/";
//          int dist = str.charAt(str.indexOf(word) + word.length());
//          str = str.replace("near/" + dist + " ", "");
          List<QueryResult> result = new ArrayList<QueryResult>();
          List<String> tokens = getTokens1(str);
          if(str.contains("near/")) {
                int near = str.indexOf("near/");
                int dist = Integer.parseInt(Character.toString(str.charAt(near + 5)));
                System.out.println("Distance in near: " + dist);
                String token1 = str.substring(0, near - 1);
                String token2 = str.substring(str.lastIndexOf(" "));
                List<QueryResult> posting1 = index.GetPostings(PorterStemmer.processToken(token1));
                List<QueryResult> posting2 = index.GetPostings(PorterStemmer.processToken(token2));
                List<QueryResult> copy1 = new ArrayList<QueryResult>();
                List<QueryResult> copy2 = new ArrayList<QueryResult>();
                copyList(posting1, copy1);
                copyList(posting2, copy2);
                filter(copy1, copy2);
//                System.out.println("copy1 and copy2 lists:");
//                for(int j = 0; j < copy1.size(); j++) {
//                    System.out.print(copy1.get(j).getDocId() + " ");
//                    System.out.println(copy2.get(j).getDocId());
//                }
                for(int i = 0; i < copy1.size(); i++) {
                    List<Integer> positions1 = copy1.get(i).getPostings();
                    List<Integer> positions2 = copy2.get(i).getPostings();
//                    System.out.println("checking file " + index.getFileNames().get(copy1.get(i).getDocId()));
                    if(compare(positions1, positions2, dist)) {
                        result.add(copy1.get(i));
                        System.out.println("added file: " + index.getFileNames().get(posting1.get(i).getDocId()));
                    }
                }
                
          }
          return result;
      }
          
    /**
     * Removes ulistOfListsique objects. The resultilistOfListsg lists will be of same lelistOfListsgth
     * @param list1 List of PositiolistOfListsalPostilistOfListsgs
     * @param list2 List of PositiolistOfListsalPostilistOfListsgs
     */
    public void filterLists(List<QueryResult> list1, List<QueryResult> list2) {
        System.out.println("filterLists called");
        int index1 = 0;
        int index2 = 0;
        int temp1, temp2;
        List<QueryResult> new1 = new ArrayList<QueryResult>();
        List<QueryResult> new2 = new ArrayList<QueryResult>();
        
        while(list1 != null && list2 != null) {
            temp1 = list1.get(index1).getDocId();
            temp2 = list2.get(index2).getDocId();
            if(temp1 == temp2) {
//                result.add(list1.get(index1));
                index1++;
                index2++;
            }
            else if(temp1 > temp2) {
//                index2++;
                list2.remove(list2.get(index2));
            }
            else {
//                temp1 > temp2 ? index2++ : index1++;
//                index1++;
                list1.remove(list1.get(index1));
            }
            if(index1 >= list1.size()) {
                while(index2 < list2.size()) {
                    list2.remove(index2);
//                    index2++;
                }
                    
                break;
            }
            if(index2 >= list2.size()){
                while(index1 < list1.size()) {
                    list1.remove(index1);
//                    index1++;
                    // incorrect filtering. Lists are not of equal size after filtering.
                }
                break;
            }
        }
//        System.out.println("filter result 401 index1 " + index1 + " index2 " + index2);
    }
    
    public void filter(List<QueryResult> list1, List<QueryResult> list2) {
        int i = 0;
        int j = 0;
        
        while(list1.size() > 0 && list2.size() > 0) {
            if(list1.get(i).getDocId() == list2.get(j).getDocId()) {
                i++;
                j++;
            }
            else if(list1.get(i).getDocId() > list2.get(j).getDocId()) {
                list2.remove(j);
            }
            else if(list1.get(i).getDocId() < list2.get(j).getDocId()) {
                list1.remove(i);
            }
            if(i >= list1.size()) {
                while(j < list2.size()) {
                    list2.remove(j);
                }
                break;
            }
            if(j >= list2.size()) {
                while(i < list1.size()) {
                    list1.remove(i);
                }
                break;
            }
        }
    }
    
    public boolean compare(List<Integer> list1, List<Integer> list2, int dist) {
//        System.out.println("comparing");
        int i = 0;
        int j = 0;
        int first = 0;
        int second = 0;
        boolean result = false;
        while(i < list1.size() && j < list2.size()) {
//        while(true) {
//            System.out.println(list1.get(i) + " " + list2.get(j) + " list1 and list2");
            first = list1.get(i);
            second = list2.get(j);
            int diff = second - first;
            if ((diff) <= dist && diff > 0) {
                result = true;
                break;
//                return result;
            }
//            else if(first == second) {
//                i++;
//                j++;   
//            }
            else if(first < second) {
                i++;
            }
            else {
                j++;
            }
        }
        System.out.println("result after compare ends: " + result);
        return result;
    }
    
    /**
     * Makes a copy of the list
     * @param original - the list to be copied
     * @param copy - the copy of the list
     */
    public void copyList(List<QueryResult> original, List<QueryResult> copy) {
        copy.clear();
        System.out.println("copyList");
//        System.out.println("copyList (processQuery 468). original size: " + original.size());
        for(QueryResult p: original) {
            
            copy.add(p);
        }
    }
    
}
