
import java.io.RandomAccessFile;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sachin
 */
public class OkapiWeighting implements WeightingSchemes {
    int num;
    float dft;
    float tftd;
    float docWeight;
    float avgDocWeight;
    
    public OkapiWeighting(int n, float dft, float tftd, float docWeight, float avgDocWeight) {
        num = n;
        this.dft = dft;
        this.tftd = tftd;
        this.docWeight = docWeight;
        this.avgDocWeight = avgDocWeight;
    }
    public float calculateWeights() {
        float wqt = (float)Math.log((num - dft + 0.5) / dft + 0.5);
        float kd = (float)((float) 1.2 * (0.25 + (float)0.75 * docWeight / avgDocWeight));
        float wdt = (float)(2.2 * tftd) / kd + tftd;
        return wqt * wdt;
        
    }
}
