package com.DTO;

public class MYLOC {
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    Double  LAT;
    Double  LNT;
    String WORK_DTTM;

    public Double getLAT() {
        return LAT;
    }

    public void setLAT(Double LAT) {
        this.LAT = LAT;
    }

    public Double getLNT() {
        return LNT;
    }

    public void setLNT(Double LNT) {
        this.LNT = LNT;
    }

    public String getWORK_DTTM() {
        return WORK_DTTM;
    }

    public void setWORK_DTTM(String WORK_DTTM) {
        this.WORK_DTTM = WORK_DTTM;
    }
}
