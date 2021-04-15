package com.axis.revmaxinterface.Audit;


//this class represents an invoice and is used to get and set invoice property values
public class Invoice {


    //=na is meant to guard againstl null values when generating xml in uitilities class
    private String ID="";
    private String DeviceBPN="na";
    private String CODE="na";
    private String MACNUM="na";
    private String DECSTARTDATE="na";
    private String DECENDDATE="na";
    private String DETSTARTDATE="na";
    private String DETENDDATE="na";
    private String CPY="na";
    private String IND="na";
    private String ITYPE="na";
    private String ICODE="na";
    private String VAT="na";
    private String BRANCH="na";
    private String INUM="na";
    private String IBPN="na";
    private String INAME="na";
    private String IADDRESS="na";
    private String ICONTACT="na";
    private String ISHORTNAME="na";
    private String IPAYER="na";
    private String IPVAT="na";
    private String IPADDRESS="na";
    private String IPTEL="na";
    private String IPBPN="na";
    private String ICURRENCY="na";
    private String IAMT="na";
    private String ITAX="na";
    private String ISTATUS="na";
    private String IISSUER="na";
    private String IDATE="na";
    private String ITAXCTRL="na";
    private String IOCODE="na";
    private String IONUM="na";
    private String IREMARK="na";
    private String ITEMSXML="na";
    private String CurrenciesReceivedXML="na";

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDeviceBPN() {
        return DeviceBPN;
    }

    public void setDeviceBPN(String deviceBPN) {
        DeviceBPN = deviceBPN;
    }

    public String getCODE() {
        return CODE;
    }

    public void setCODE(String CODE) {
        this.CODE = CODE;
    }

    public String getMACNUM() {
        return MACNUM;
    }

    public void setMACNUM(String MACNUM) {
        this.MACNUM = MACNUM;
    }

    public String getDECSTARTDATE() {
        return DECSTARTDATE;
    }

    public void setDECSTARTDATE(String DECSTARTDATE) {
        this.DECSTARTDATE = DECSTARTDATE;
    }

    public String getDECENDDATE() {
        return DECENDDATE;
    }

    public void setDECENDDATE(String DECENDDATE) {
        this.DECENDDATE = DECENDDATE;
    }

    public String getDETSTARTDATE() {
        return DETSTARTDATE;
    }

    public void setDETSTARTDATE(String DETSTARTDATE) {
        this.DETSTARTDATE = DETSTARTDATE;
    }

    public String getDETENDDATE() {
        return DETENDDATE;
    }

    public void setDETENDDATE(String DETENDDATE) {
        this.DETENDDATE = DETENDDATE;
    }

    public String getCPY() {
        return CPY;
    }

    public void setCPY(String CPY) {
        this.CPY = CPY;
    }

    public String getIND() {
        return IND;
    }

    public void setIND(String IND) {
        this.IND = IND;
    }

    public String getITYPE() {
        return ITYPE;
    }

    public void setITYPE(String ITYPE) {
        this.ITYPE = ITYPE;
    }

    public String getICODE() {
        return ICODE;
    }

    public void setICODE(String ICODE) {
        this.ICODE = ICODE;
    }

    public String getBRANCH() {
        return BRANCH;
    }

    public void setBRANCH(String BRANCH) {
        this.BRANCH = BRANCH;
    }

    public String getINUM() {
        return INUM;
    }

    public void setINUM(String INUM) {
        this.INUM = INUM;
    }

    public String getIBPN() {
        return IBPN;
    }

    public void setIBPN(String IBPN) {
        this.IBPN = IBPN;
    }

    public String getINAME() {
        return INAME;
    }

    public void setINAME(String INAME) {
        this.INAME = INAME;
    }

    public String getIADDRESS() {
        return IADDRESS;
    }

    public void setIADDRESS(String IADDRESS) {
        this.IADDRESS = IADDRESS;
    }

    public String getICONTACT() {
        return ICONTACT;
    }

    public void setICONTACT(String ICONTACT) {
        this.ICONTACT = ICONTACT;
    }

    public String getISHORTNAME() {
        return ISHORTNAME;
    }

    public void setISHORTNAME(String ISHORTNAME) {
        this.ISHORTNAME = ISHORTNAME;
    }

    public String getIPAYER() {
        return IPAYER;
    }

    public void setIPAYER(String IPAYER) {
        this.IPAYER = IPAYER;
    }

    public String getIPVAT() {
        return IPVAT;
    }

    public void setIPVAT(String IPVAT) {
        this.IPVAT = IPVAT;
    }

    public String getIPADDRESS() {
        return IPADDRESS;
    }

    public void setIPADDRESS(String IPADDRESS) {
        this.IPADDRESS = IPADDRESS;
    }

    public String getIPTEL() {
        return IPTEL;
    }

    public void setIPTEL(String IPTEL) {
        this.IPTEL = IPTEL;
    }

    public String getIPBPN() {
        return IPBPN;
    }

    public void setIPBPN(String IPBPN) {
        this.IPBPN = IPBPN;
    }

    public String getICURRENCY() {
        return ICURRENCY;
    }

    public void setICURRENCY(String ICURRENCY) {
        this.ICURRENCY = ICURRENCY;
    }

    public String getIAMT() {
        return IAMT;
    }

    public void setIAMT(String IAMT) {
        this.IAMT = IAMT;
    }

    public String getITAX() {
        return ITAX;
    }

    public void setITAX(String ITAX) {
        this.ITAX = ITAX;
    }

    public String getISTATUS() {
        return ISTATUS;
    }

    public void setISTATUS(String ISTATUS) {
        this.ISTATUS = ISTATUS;
    }

    public String getIISSUER() {
        return IISSUER;
    }

    public void setIISSUER(String IISSUER) {
        this.IISSUER = IISSUER;
    }

    public String getIDATE() {
        return IDATE;
    }

    public void setIDATE(String IDATE) {
        this.IDATE = IDATE;
    }

    public String getITAXCTRL() {
        return ITAXCTRL;
    }

    public void setITAXCTRL(String ITAXCTRL) {
        this.ITAXCTRL = ITAXCTRL;
    }

    public String getIOCODE() {
        return IOCODE;
    }

    public void setIOCODE(String IOCODE) {
        this.IOCODE = IOCODE;
    }

    public String getIONUM() {
        return IONUM;
    }

    public void setIONUM(String IONUM) {
        this.IONUM = IONUM;
    }

    public String getIREMARK() {
        return IREMARK;
    }

    public void setIREMARK(String IREMARK) {
        this.IREMARK = IREMARK;
    }

    public String getITEMSXML() {
        return ITEMSXML;
    }

    public void setITEMSXML(String ITEMSXML) {
        this.ITEMSXML = ITEMSXML;
    }

    public String getCurrenciesReceivedXML() {
        return CurrenciesReceivedXML;
    }

    public void setCurrenciesReceivedXML(String currenciesReceivedXML) {
        CurrenciesReceivedXML = currenciesReceivedXML;
    }

    public String getVAT() {
        return VAT;
    }

    public void setVAT(String VAT) {
        this.VAT = VAT;
    }
}
