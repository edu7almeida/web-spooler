
package org.dcm4che3.net.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;

/**
 * @author Eduardo Almeida <eduardo.almeida@ua.pt>
 *  
 */

public class BasicPrinterSCP extends AbstractDicomService{
    
    private final PrinterSCP printerSCP;
    
    
    public BasicPrinterSCP(PrinterSCP prtSCP) {
        super(UID.BasicGrayscalePrintManagementMetaSOPClass, 
                UID.BasicFilmSessionSOPClass,
                    UID.PresentationLUTSOPClass, 
                        UID.PrinterSOPClass, 
                            UID.BasicFilmBoxSOPClass,
                                UID.BasicGrayscaleImageBoxSOPClass);
        this.printerSCP = prtSCP;
    }
    
    /**
     * Handles the DIMSE request messages from a SCU.
     * @param as
     * @param pc
     * @param dimse
     * @param rq
     * @param rqAtrbts
     * @throws java.io.IOException
     */
    
    @Override
    protected void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes rq, Attributes rqAtrbts) throws IOException {
        switch (dimse){
        
        case N_CREATE_RQ:
            onCreateRQ(as, pc, rq, rqAtrbts);
            break;
        case N_GET_RQ:
            onNGetRQ(as, pc, rq, rqAtrbts);
            break;
        case N_SET_RQ:
            onSetRQ(as, pc, rq, rqAtrbts);
            break;
        case N_ACTION_RQ:
            onActionRQ(as, pc, rq, rqAtrbts);
            break;
        case N_DELETE_RQ:
            onDeleteRQ(as, pc, rq, rqAtrbts);
            break;
        
        default:
            throw new AssertionError(dimse.name());
        }
    }

    /**
     * Handles the reception of N-GET messages from a SCU.
     * 
     * 
     */
    
    private void onNGetRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbs) throws DicomServiceException {

        Attributes rsp = Commands.mkNGetRSP(rq, Status.Success);
        rsp.remove(Tag.AffectedSOPClassUID);
        rsp.remove(Tag.AffectedSOPInstanceUID);
      
        Attributes rspAttrs = new Attributes();
        rspAttrs.setValue(Tag.PrinterStatus, VR.CS, (String) "NORMAL");
        rspAttrs.setValue(Tag.PrinterStatusInfo, VR.CS, (String) "NORMAL");
 
        //as.tryWriteDimseRSP(pc, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }
    
    /**
     * Handles the reception of N-CREATE messages from the SCU.
     * 
     * 
     */
    
    private void onCreateRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbs) throws DicomServiceException, UnsupportedEncodingException {
        System.out.println();
        System.out.println();
        System.out.println("ONCREATERQ: "+ rq.toString());//
        Attributes rsp = Commands.mkNCreateRSP(rq, Status.Success);
        Attributes rspAttrs = this.printerSCP.create(as, rq, rqAtrbs, rsp);
        
        System.out.println("RSP: " + rsp.toString());
        System.out.println("RSPATR: " + rspAttrs);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
        System.out.println("FIM CREATE");
    }
 

    private void onSetRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbs) throws DicomServiceException, IOException {
        System.out.println("ONSETRQ: "+ rq.toString());//
        Attributes rsp = Commands.mkNSetRSP(rq, Status.Success);
        Attributes rspAttrs = this.printerSCP.set(as, rq, rqAtrbs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    private void onActionRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbs) throws DicomServiceException {
        Attributes rsp = Commands.mkNActionRSP(rqAtrbs, Status.Success);
        Attributes rspAttrs = this.printerSCP.action(as, rq, rqAtrbs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    private void onDeleteRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}