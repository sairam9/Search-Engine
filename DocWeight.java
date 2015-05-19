/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sachin
 */
public class DocWeight implements Comparable {
    public int docId;
    public float score;
    
    public DocWeight(int id, float score) {
        docId = id;
        this.score = score;
    }
    
    public int compareTo(Object o) {
        DocWeight dw = (DocWeight)o;
        if(score < dw.score) {
            return 1;
        }
        if(score > dw.score) {
            return -1;
        }
        return 0;
    }
}
