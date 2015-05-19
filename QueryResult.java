
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sachin
 */
public class QueryResult {
    private int docId;
    private float tftd;
    private List<Integer> postings;
    
    public QueryResult() {
        
    }
    
    public QueryResult(int id, int tftd) {
        docId = id;
        settftd(tftd);
    }
    
//    public QueryResult(int id, float )
    
    public void setDocId(int id) {
        docId = id;
    }
    
    public int getDocId() {
        return docId;
    }
    
    public void settftd(float f) {
        tftd = f;
    }
    
    public float gettftd() {
        return tftd;
    }
    
    public void setPostings(List<Integer> post) {
        postings = post;
    }
    
    public List<Integer> getPostings() {
        return postings;
    }
}
