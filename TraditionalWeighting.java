
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
public class TraditionalWeighting implements WeightingSchemes {
    int num;
    float dft;
    float tftd;
    float docW;
    float avgDocW;
    
    public TraditionalWeighting(int n, float dft, float tftd) {
        num = n;
        this.dft = dft;
        this.tftd = tftd;
    }
    @Override
    public float calculateWeights() {
        float wqt = (float)Math.log(num / dft);
        float wdt = tftd * wqt;
        
        return wqt * wdt;
    }
}
