
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sachin
 */
public class DefaultWeighting implements WeightingSchemes {
    int num;
    float dft;
    float tftd;

    public DefaultWeighting(int n, float dft, float tftd) {
        num = n;
        this.dft = dft;
        this.tftd = tftd;
    }
    @Override
    public float calculateWeights() {
        double wqt = Math.log1p(1 + (num / dft));
        double wdt = 1 + Math.log1p(tftd);
        return (float)wqt * (float)wdt;
            
    }
}
