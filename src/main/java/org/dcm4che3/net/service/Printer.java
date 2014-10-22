
package org.dcm4che3.net.service;

import java.io.File;
import java.util.ResourceBundle;
import org.dcm4che3.data.IOD;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.MppsSCP;

/**
 * @author Eduardo Almeida <eduardo.almeida@ua.pt>
 *  
 */

public class Printer {
    private static final ResourceBundle rb =
                            ResourceBundle.getBundle("mppsscp.messages");

   private static final Logger LOG = LoggerFactory.getLogger(MppsSCP.class);

   private final Device device = new Device("mppsscp");
   private final ApplicationEntity ae = new ApplicationEntity("*");
   private final Connection conn = new Connection();
   private File storageDir;
   private IOD mppsNCreateIOD;
   private IOD mppsNSetIOD;
   private IOD mppsNGetIOD;
   private IOD mppsNAction;
   private IOD mppsNDelete;
   
   private final PrinterMPPSSCP printerSCP = new PrinterMPPSSCP();
   
   
   public Printer(){
       device.addConnection(conn);
       device.addApplicationEntity(ae);
       ae.setAssociationAcceptor(true);
       ae.addConnection(conn);
       DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
       serviceRegistry.addDicomService(printerSCP);
       ae.setDimseRQHandler(serviceRegistry);    
   }
   
   
}
