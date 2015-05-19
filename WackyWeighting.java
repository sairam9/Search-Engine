
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
public class WackyWeighting implements WeightingSchemes {
    int num;
    float dFT;
    float tFTD;
    float docW;
    float avgDocW;
    float avgtftd;
    
    public WackyWeighting (int n, float dft, float tftd, float docWeight, float avgDocWeight, float avgTFTD) {
        num = n;
        dFT = dft;
        tFTD = tftd;
        docW = docWeight;
        avgDocW = avgDocWeight;
        avgtftd = avgTFTD;
    }
    
    public float calculateWeights() {
        float wqt = (float)Math.max(0, Math.log((num - dFT) / dFT));
        float wdt = (float)(1 + Math.log(tFTD) / 1 + Math.log(avgtftd));
        return wqt * wdt;
    }
}
