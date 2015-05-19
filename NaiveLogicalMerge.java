
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sachin
 */
public class NaiveLogicalMerge {
    
    public List<QueryResult> and(List<QueryResult> list1, List<QueryResult> list2) {
//        List<PositionalPosting> posting1 = index.getPostings(first);
//        List<PositionalPosting> posting2 = index.getPostings(second);
        List<QueryResult> result = new ArrayList<QueryResult>();
        if(list1.size() == 0 || list1 == null) {
            System.out.println("and: list1 is null.");
            return list2;
        }
        if(list2.size() == 0 || list2 == null) {
            System.out.println("and: list2 is null.");
            return list1;   
        }
        
        int index1 = 0;
        int index2 = 0;
        int temp1, temp2;
        
        while(list1.size() > 0 && list2.size() > 0) {
            temp1 = list1.get(index1).getDocId();
            temp2 = list2.get(index2).getDocId();
            if(temp1 == temp2) {
                result.add(list1.get(index1));
                index1++;
                index2++;
            }
            else if(temp1 > temp2) {
                index2++;
                
            }
            else {
//                temp1 > temp2 ? index2++ : index1++;
                index1++;
            }
            if(index1 >= list1.size() || index2 >= list2.size()) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Evaluates the result of list1 OR list2
     * @param list1
     * @param list2
     * @return 
     */
    public List<QueryResult> or(List<QueryResult> list1, List<QueryResult> list2) {
        int index1 = 0;
        int index2 = 0;
        if(list1.size() == 0) {
                System.out.println("list1 is null.");
                return list2;
            }
            else if(list2.size() == 0) {
                System.out.println("list2 is null.");
                return list1;   
            }
        
        List<QueryResult> result = new ArrayList<QueryResult>();
        if(list1.size() > 0 && list2.size() > 0) {
        while(index1 < list1.size() && index2 < list2.size()) {
            int temp1 = list1.get(index1).getDocId();
            int temp2 = list2.get(index2).getDocId();
            
            if(temp1 < temp2) {
                result.add(list1.get(index1));
                System.out.println("ORed if: ");
                index1++;
            }
            else if(temp2 < temp1) {
                result.add(list2.get(index2));
                System.out.println("ORed else if: ");
                index2++;
            }
            else if(temp1 == temp2) {
                result.add(list2.get(index2));
                System.out.println("ORed else: ");
                index1++;
                index2++;
            }
            
            if(index1 >= list1.size() && index2 < list2.size()) {
//                list2 = list2.subList(index2, list2.size());
//                for(PositionalPosting pp: list2) {
//                    result.add(pp.getDocId());
//                }
//                result = addRemaining(result, list2, index2);
                System.out.println("ORed second if: ");
                result.addAll(list2.subList(index2, list2.size()));
                break;
            }
            else if(index2 >= list2.size() && index1 < list1.size()) {
                result = addRemaining(result, list1, index1);
                System.out.println("ORed second else if: ");
                break;
            }
//            else {
//                System.out.println("ORed 2nd else: ");
//                break;
//            }
        }
        }
        else {
            
        }
        return result;
    }
    
    private List<QueryResult> addRemaining(List<QueryResult> result, List<QueryResult> pList, int index) {
        pList = pList.subList(index, pList.size());
        result.addAll(pList);
        System.out.println("add remaining: ");
//        for(PositionalPosting pp: pList) {
//            result.add(pp);
//        }
        return result;
    }
    
    /**
     * Returns list1 without the postings in common with list2
     * @param index
     * @param list1
     * @param list2
     * @return
     */
    public List<QueryResult> not(List<QueryResult> list1, List<QueryResult> list2) {
        int index1 = 0;
        int index2 = 0;
        int temp1, temp2;
        List<QueryResult> result1 = new ArrayList<QueryResult>();

        while(true){
            temp1 = list1.get(index1).getDocId();
            temp2 = list2.get(index2).getDocId();
            
            
            if(temp1 == temp2) {
                index1++;
                index2++;

            }
            else if(temp1 > temp2) {
                index2++;
            }
            else {
                result1.add(list1.get(index1));
                index1++;
            }
//            if(lastID < list2.get(index2).getDocId())
//                break;
//            else 
                if(index1 >= list1.size()) {
                break;
                }
                if(index2 >= list2.size()) {
                    while(index1 < list1.size()) {
                        result1.add(list1.get(index1));
                        index1++;
                    }
                    break;
                }
        }
        return result1;
    }
    
    public void copyList(List<PositionalPosting> original, List<PositionalPosting> copy) {
        for(PositionalPosting p: original) {
            copy.add(p);
        }
    }
}
