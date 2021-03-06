
package neironweb;

import java.util.Arrays;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Web {

    Layer[] layermas;
    double[] inputdata;
    MatrixWeight[] matrixweightArray;
    XYSeries xyser;
    XYDataset xy;
    JFreeChart jf;
    JFrame jframe;
    ChartPanel chp;
    int cntiter = 0;
    Logic log = new Logic();
    int layerCount = 0;

    Web(int[] neironLayers) {

        layerCount = neironLayers.length;
        layermas = new Layer[layerCount];

        createMatrixW(neironLayers);

        createGUI();

        for (int i = 0; i < layerCount; i++) {
            Neiron[] temp = new Neiron[neironLayers[i]];

            for (int j = 0; j < neironLayers[i]; j++) {
                temp[j] = new Neiron();
            }
            Layer ltemp = new Layer(temp);
            layermas[i] = ltemp;
        }
    }

    public void start(double[] inputdata) {
        this.inputdata = inputdata;
        log.start(layermas, inputdata, matrixweightArray);
    }

    void createGUI() {
        xyser = new XYSeries("adsf");
        xy = new XYSeriesCollection(xyser);
        jf = ChartFactory.createXYLineChart("grafic", "X", "Y", xy);
        jframe = new JFrame();
        jframe.setVisible(true);
        jframe.setSize(500, 500);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chp = new ChartPanel(jf);
        jframe.setContentPane(chp);
    }

    void createMatrixW(int[] neironLayers) {
        matrixweightArray = new MatrixWeight[neironLayers.length];
        for (int i = 0; i < neironLayers.length - 1; i++) {
            matrixweightArray[i] = new MatrixWeight(neironLayers[i], neironLayers[i + 1]);
        }

        for (int i = 1; i < neironLayers.length; i++) {
            matrixweightArray[i].readMatrix("matrixweight" + Integer.toString(i));
        }
    }

    public void education(double data[], double waitingResult[]) throws InterruptedException {

        double[] haveResult;
        boolean flag = true;
        double SPEED = 0.2;

        while (flag) {
            cntiter++;
            flag = false;

            haveResult = log.start(layermas, data, matrixweightArray);

            for (int i = 0; i < haveResult.length; i++) {
                if (Math.abs(waitingResult[i] - haveResult[i]) > 0.1) {
                    flag = true;
                }
            }

            //GUI
            xyser.add(cntiter, haveResult[0]);
            Thread.sleep(500);

            if (flag) {

                Layer layerTwo = layermas[1];

                layerTwo.Error[0] = haveResult[0] * (1 - haveResult[0]) * (waitingResult[0] - haveResult[0]);
                layerTwo.Error[1] = haveResult[1] * (1 - haveResult[1]) * (waitingResult[1] - haveResult[1]);

                Layer layerOne = layermas[0];

                double sum = 0;
                for (int i = 0; i < 2; i++) {
                    sum += matrixweightArray[1].weight[0][i] * layerTwo.Error[i];
                }
                layerOne.Error[0] = sum * layerOne.layeroutput[0] * (1 - layerOne.layeroutput[0]);

                for (int i = 0; i < 2; i++) {
                    sum += matrixweightArray[1].weight[1][i] * layerTwo.Error[i];
                }
                layerOne.Error[1] = sum * layerOne.layeroutput[1] * (1 - layerOne.layeroutput[1]);

                for (int i = 0; i < 2; i++) {
                    sum += matrixweightArray[1].weight[2][i] * layerTwo.Error[i];
                }
                layerOne.Error[2] = sum * layerOne.layeroutput[2] * (1 - layerOne.layeroutput[2]);

                //for second layer change weigth 
                double[] deltaWeighti0 = new double[3];
                for (int i = 0; i < 3; i++) {
                    deltaWeighti0[i] = layerOne.layeroutput[i] * layerTwo.Error[0] * SPEED;
                }

                for (int i = 0; i < 3; i++) {
                    matrixweightArray[1].weight[i][0] += deltaWeighti0[i];
                }

                double[] deltaWeighti1 = new double[3];
                for (int i = 0; i < 3; i++) {
                    deltaWeighti1[i] = layerOne.layeroutput[i] * layerTwo.Error[1] * SPEED;
                }

                for (int i = 0; i < 3; i++) {
                    matrixweightArray[1].weight[i][1] += deltaWeighti1[i];
                }

              //for first layer change weigth                
                // for first neiron 
                double[] L1_deltaWeighti0 = new double[4];
                for (int i = 0; i < 4; i++) {
                    L1_deltaWeighti0[i] = data[i] * layerOne.Error[0] * SPEED;
                }

                for (int i = 0; i < 4; i++) {
                    matrixweightArray[0].weight[i][0] += L1_deltaWeighti0[i];
                }

                // for second neiron 
                double[] L1_deltaWeighti1 = new double[4];
                for (int i = 0; i < 4; i++) {
                    L1_deltaWeighti1[i] = data[i] * layerOne.Error[1] * SPEED;
                }

                for (int i = 0; i < 4; i++) {
                    matrixweightArray[0].weight[i][1] += L1_deltaWeighti1[i];
                }

                // for third neiron 
                double[] L1_deltaWeighti2 = new double[4];
                for (int i = 0; i < 4; i++) {
                    L1_deltaWeighti2[i] = data[i] * layerOne.Error[2] * SPEED;
                }

                for (int i = 0; i < 4; i++) {
                    matrixweightArray[0].weight[i][1] += L1_deltaWeighti2[i];
                }

                flag = true;   //флаг, что нужно еще раз запустить сеть

            }

            System.out.println("we have result : " + haveResult[0] + " " + haveResult[1]);

        }

        System.out.println("OK. I'm educated now");

    }

//  public void savematrix(MatrixWeight matrixweight) {
//    log.savematrix(matrixweight);
//}
//  public void savematrix (MatrixWeight matrix1, MatrixWeight matrix2) {
//     log.savematrix(matrix1,matrix2); 
//  }
}
