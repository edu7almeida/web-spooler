
package org.dcm4che3.net.service;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
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

    
    private void onNGetRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbs) throws DicomServiceException {
        System.out.println("ONNGETRQ:rq: "+rq.toString());//
        Attributes rsp = Commands.mkNGetRSP(rq, Status.Success);
        System.out.println("RSP:" + rsp.toString());//
        Attributes rspAttrs = this.printerSCP.get(as, rq, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }
    
    private void onCreateRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbs) throws DicomServiceException {
        System.out.println("ONCREATERQ: "+ rq.toString());//
        Attributes rsp = Commands.mkNCreateRSP(rq, Status.Success);
        Attributes rspAttrs = this.printerSCP.create(as, rq, rqAtrbs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
        System.out.println("FIM CREATE");
    }
 

    private void onSetRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAtrbs) throws DicomServiceException {
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