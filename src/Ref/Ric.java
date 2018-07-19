package Ref;

import java.io.Serializable;

public class Ric implements Serializable {
    public String ric;

    /**
     *
     * @param ric
     */
    public Ric(String ric) {
        this.ric = ric;
    }

    /**
     *
     * @return
     */
    public String getEx() {
        return ric.split(".")[1];
    }

    /**
     *
     * @return
     */
    public String getCompany() {
        return ric.split(".")[0];
    }
}