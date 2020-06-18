
package com.webank.ai.fate.serving.common.flow;



import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class FlowCounter {

    private double qpsAllowed;

    private final LeapArray<LongAdder> data;

    public FlowCounter(double qpsAllowed) {
        this(new UnaryLeapArray(10, 1000), qpsAllowed);
    }

    FlowCounter(LeapArray<LongAdder> data, double qpsAllowed) {
        this.data = data;
        this.qpsAllowed = qpsAllowed;
    }

    public void increment() {
        data.currentWindow().value().increment();
    }

    public void add(int x) {
        data.currentWindow().value().add(x);
    }

    public  class QpsData{
        long   current;
        long   sum;

        public QpsData(long   current,long   sum){

            this.current =  current;
            this.sum =  sum;

        }
    }
    public QpsData  getQpsData(){


        long success = 0;
        WindowWrap windowWrap = data.currentWindow();
        List<LongAdder> list = data.values();
        for (LongAdder window : list) {
            success += window.sum();
        }
        double qps=   success/data.getIntervalInSecond();

       return  new  QpsData(windowWrap.windowStart(),getSum());

    }


    public long getSum() {
        data.currentWindow();
        long success = 0;

        List<LongAdder> list = data.values();
        for (LongAdder window : list) {
            success += window.sum();
        }
        return success;
    }

    public double getQps() {
        return getSum() / data.getIntervalInSecond();
    }


    public double getQpsAllowed() {
        return qpsAllowed;
    }

    public boolean canPass() {
        return getQps() + 1 <= qpsAllowed;
    }

    public FlowCounter setQpsAllowed(double qpsAllowed) {
        this.qpsAllowed = qpsAllowed;
        return this;
    }

    public boolean tryPass() {
        if (canPass()) {
            add(1);
            return true;
        }
        return false;
    }
}
