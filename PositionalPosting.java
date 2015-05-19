import java.util.*;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sachin
 */
public class PositionalPosting {
    private int docId;
    private List<Integer> posList;
//    private Integer[][] posIndex;
    private HashMap<Integer, List<Integer>> posMap;
    
    PositionalPosting () {
        posList = new ArrayList<Integer>();
        posMap = new HashMap<Integer, List<Integer>>();
        posList = new ArrayList<Integer>();
    }
    
    PositionalPosting (int id, int pos) {
        posList = new ArrayList<Integer>();
        docId = id;
        posList.add(pos);
        
    }
    
    PositionalPosting(int id, List<Integer> pos) {
        docId = id;
        posList = pos;
    }
    
    HashMap<Integer, List<Integer>> getMap() {
        return posMap;
    }
    
    void addPos1 (int pos) {
        if (!posList.contains(pos)) {
            posList.add(pos);
        }
    }
    
    void addPos (int documentId, int pos) {
        /*
        Adds term position to the index.
        If there is no entry for the document, it creates a list and adds the position to the list.
        */
        if (!posMap.containsKey(documentId)) {
            List<Integer> temp = new ArrayList<Integer>();
            temp.add(pos);
            posMap.put(documentId, temp);
//            System.out.println("List size " + documentId);
        }
        else {
//            List<Integer> posList = posMap.get(documentId); //.add(pos);
            posMap.get(documentId).add(pos);
//            posList.add(pos);
            System.out.println("List size " + documentId + " " + posList.size());
//            return posMap.values();
        }
    }
    
    void addPos0 (PositionalPosting p, int documentId, int pos) {
        p.posList.add(pos);
        p.posMap.put(documentId, p.posList);
        
    }
    
    public List<Integer> getPositions(int documentId) {
        /*
        retrieve position list
        */
        return posMap.get(documentId);
    }
    
    public Set<Integer> getDictionary () {
        return posMap.keySet();
//        return 
    }
    
    /**
     * returns the id of the document.
     * @return
     */
    public int getDocId() {
        return docId;
    }
    
    public List<Integer> getPosList() {
        return posList;
    }
}
