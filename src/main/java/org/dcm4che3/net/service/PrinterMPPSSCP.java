
package org.dcm4che3.net.service;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.pdu.PresentationContext;

/**
 * @author Eduardo Almeida <eduardo.almeida@ua.pt>
 *  
 */

public class PrinterMPPSSCP extends AbstractDicomService{

    public PrinterMPPSSCP() {
        super(UID.PrintJobSOPClass);
    }
    
    @Override
    protected void onDimseRQ(Association asctn, PresentationContext pc, Dimse dimse, Attributes atrbts, Attributes rqAtrbts) throws IOException {
        switch (dimse){
        case N_GET_RQ:
            onNGetRQ(asctn, pc, atrbts, rqAtrbts);
            break;
        case N_CREATE_RQ:
            onCreateRQ(asctn, pc, atrbts, rqAtrbts);
            break;
        case N_SET_RQ:
            onSetRQ(asctn, pc, atrbts, rqAtrbts);
            break;
        case N_ACTION_RQ:
            onActionRQ(asctn, pc, atrbts, rqAtrbts);
            break;
        case N_DELETE_RQ:
            onDeleteRQ(asctn, pc, atrbts, rqAtrbts);
            break;
        default:
            throw new AssertionError(dimse.name());
        }
    }

    private void onNGetRQ(Association asctn, PresentationContext pc, Attributes atrbts, Attributes rqAtrbts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void onCreateRQ(Association asctn, PresentationContext pc, Attributes atrbts, Attributes rqAtrbts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void onSetRQ(Association asctn, PresentationContext pc, Attributes atrbts, Attributes rqAtrbts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void onActionRQ(Association asctn, PresentationContext pc, Attributes atrbts, Attributes rqAtrbts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void onDeleteRQ(Association asctn, PresentationContext pc, Attributes atrbts, Attributes rqAtrbts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
    
}
