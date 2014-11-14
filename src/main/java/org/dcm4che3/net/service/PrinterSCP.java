
package org.dcm4che3.net.service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eduardo Almeida <eduardo.almeida@ua.pt>
 *  
 */

public class PrinterSCP {
   
    private static final ResourceBundle rb =
                            ResourceBundle.getBundle("mppsscp.messages");
    private static final Logger LOG = LoggerFactory.getLogger(PrinterSCP.class);
    private final Device device = new Device("printerscp");
    private final ApplicationEntity ae = new ApplicationEntity("PRINTER");
    private final Connection conn = new Connection();
    private File storageDir;
    private IOD printerNCreateIOD;
    private IOD printerNSetIOD;
    private IOD printerNGetIOD;
    private IOD printerNActionIOD;
    private IOD printerNDeleteIOD;
   
    private final BasicPrinterSCP basicPrtrSCP;
   
   
    public PrinterSCP(){
       basicPrtrSCP = new BasicPrinterSCP(this);
       device.addConnection(conn);
       device.addApplicationEntity(ae);
      
       ae.setAssociationAcceptor(true);
       ae.addConnection(conn);
       DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
       serviceRegistry.addDicomService(new BasicCEchoSCP());
       serviceRegistry.addDicomService(basicPrtrSCP);
       ae.setDimseRQHandler(serviceRegistry);
    }
    
    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addBindServerOption(opts);
        CLIUtils.addAEOptions(opts);
        CLIUtils.addCommonOptions(opts);
        addStorageDirectoryOptions(opts);
        addTransferCapabilityOptions(opts);
        addIODOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, PrinterSCP.class);
    }
    
    @SuppressWarnings("static-access")
    private static void addTransferCapabilityOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("sop-classes"))
                .withLongOpt("sop-classes")
                .create(null));
    }
    
    @SuppressWarnings("static-access")
    private static void addStorageDirectoryOptions(Options opts) {
        opts.addOption(null, "ignore", false,
                rb.getString("ignore"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("path")
                .withDescription(rb.getString("directory"))
                .withLongOpt("directory")
                .create(null));
    }
    
    private static void configureIODs(PrinterSCP printer, CommandLine cl)
            throws IOException {
        if (!cl.hasOption("no-validate")) {
            printer.setPrinterNCreateIOD(IOD.load(
                    cl.getOptionValue("mpps-ncreate-iod", 
                            "resource:mpps-ncreate-iod.xml")));
            printer.setPrinterNSetIOD(IOD.load(
                    cl.getOptionValue("mpps-nset-iod", 
                            "resource:mpps-nset-iod.xml")));
            printer.setPrinterNGetIOD(IOD.load(
                    cl.getOptionValue("mpps-nget-iod", 
                            "resource:mpps-nget-iod.xml")));
            printer.setPrinterNDeleteIOD(IOD.load(
                    cl.getOptionValue("mpps-ndelete-iod", 
                            "resource:mpps-ndelete-iod.xml")));
            printer.setPrinterNActionIOD(IOD.load(
                    cl.getOptionValue("mpps-naction-iod", 
                            "resource:mpps-naction-iod.xml")));
        }
            
        
    }
    
    private static void configureTransferCapability(ApplicationEntity ae,
        CommandLine cl) throws IOException {
        Properties p = CLIUtils.loadProperties(
                cl.getOptionValue("sop-classes", "resource:sop-classes.properties"),
                null);
        
        for (String cuid : p.stringPropertyNames()) {
            System.out.println("CUID: "+cuid);
            String ts = p.getProperty(cuid);
            System.out.println("TS: "+ts);
            ae.addTransferCapability(
                    new TransferCapability(null,
                            CLIUtils.toUID(cuid),
                            TransferCapability.Role.SCP,
                            CLIUtils.toUIDs(ts)));
        }
    }
    
    private static void configureStorageDirectory(PrinterSCP printer, CommandLine cl) {
        if (!cl.hasOption("ignore")) {
            printer.setStorageDirectory(
                    new File(cl.getOptionValue("directory", ".")));
        }
    }
    
    private static void addIODOptions(Options opts) {
        opts.addOption(null, "no-validate", false,
                rb.getString("no-validate"));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("ncreate-iod"))
                .withLongOpt("ncreate-iod")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("nset-iod"))
                .withLongOpt("nset-iod")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("nget-iod"))
                .withLongOpt("nget-iod")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("naction-iod"))
                .withLongOpt("naction-iod")
                .create(null));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("file|url")
                .withDescription(rb.getString("ndelete-iod"))
                .withLongOpt("ndelete-iod")
                .create(null));
    }
    
    public void setStorageDirectory(File storageDir) {
       if (storageDir != null)
           storageDir.mkdirs();
       this.storageDir = storageDir;
    }
    
    private void setPrinterNCreateIOD(IOD printerNCreateIOD) {
       this.printerNCreateIOD = printerNCreateIOD;
    }

    private void setPrinterNSetIOD(IOD printerNSetIOD) {
       this.printerNSetIOD = printerNSetIOD;
    }
    
    private void setPrinterNGetIOD(IOD printerNGetIOD) {
       this.printerNSetIOD = printerNGetIOD;
    }
    
    private void setPrinterNActionIOD(IOD printerNActionIOD) {
       this.printerNActionIOD = printerNActionIOD;
    }
    
    private void setPrinterNDeleteIOD(IOD printerNDeleteIOD) {
       this.printerNDeleteIOD = printerNDeleteIOD;
    }
    
    public static void main2(String[] args) {
        CommandLine cl = null;
        try {
            for (String s : args){
                System.out.println(s);
            }
            cl = parseComandLine(args);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(PrinterSCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        PrinterSCP main = new PrinterSCP();
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = 
        Executors.newSingleThreadScheduledExecutor();
        
        try {
            CLIUtils.configureBindServer(main.conn, main.ae, cl);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(PrinterSCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            CLIUtils.configure(main.conn, cl);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(PrinterSCP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PrinterSCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            configureTransferCapability(main.ae, cl);
            //configureStorageDirectory(main, cl);
            configureIODs(main, cl);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PrinterSCP.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        main.device.setScheduledExecutor(scheduledExecutorService);
        main.device.setExecutor(executorService);
        try {
            main.device.bindConnections();
            
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PrinterSCP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GeneralSecurityException ex) {
            java.util.logging.Logger.getLogger(PrinterSCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Service is waiting for clients...");
    }
    
    /**
     *  Main Function.
     * 
     *  @param  args initialization arguments
     */
  
    public static void main(String[] args) {
        try {
          
            CommandLine cl = parseComandLine(args);
            PrinterSCP printer = new PrinterSCP();
            CLIUtils.configureBindServer(printer.conn, printer.ae, cl);
            CLIUtils.configure(printer.conn, cl);
            configureTransferCapability(printer.ae, cl);
            configureStorageDirectory(printer, cl);
            configureIODs(printer, cl);
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = 
            Executors.newSingleThreadScheduledExecutor();
            printer.device.setScheduledExecutor(scheduledExecutorService);
            printer.device.setExecutor(executorService);
            printer.device.bindConnections();
        } catch (ParseException e) {
            System.err.println("mppsscp: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("mppsscp: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
        System.out.println("Service is waiting for clients...");
    }
    
    
    
    /**
     * Process of the N-GET-RQ message.
     * Gets the instance of the Printer SOP Class.
     * 
     * @param as association for witch the instance will be retrieved
     * @param rq requested meta-data atributes
     * @param rsp response data atributes
     * @return response of the printer with the information requested
     * @throws org.dcm4che3.net.service.DicomServiceException 
     */
    
    public Attributes get(Association as, Attributes rq, Attributes rsp)
            throws DicomServiceException {
        
        System.out.println("Entrei no GET");//
        if (this.printerNGetIOD != null) {
            ValidationResult result = rq.validate(this.printerNGetIOD);
            System.out.println("É diferente de NULL");//
            if (!result.isValid())
                throw DicomServiceException.valueOf(result, rq);
        }
        if (storageDir == null)
            return null;
        
        String cuid = rq.getString(Tag.RequestedSOPClassUID);
        String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
        
        System.out.println("CUID: " + cuid);
        System.out.println("IUID: " + iuid);
        
        File file;
        file = new File(storageDir, iuid);
        if (new File(storageDir, iuid).exists()){
            if (file.delete()){
                System.out.println("The file "+file.getName()+" is deleted.");
                file = new File(storageDir, iuid);
            }else{
                System.out.println("Deleted operation of "+file.getName()+" file is failed.");
            throw new DicomServiceException(Status.NoSuchObjectInstance).
                setUID(Tag.RequestedSOPClassUID, iuid);
            }
        }
        LOG.info("{}: M-WRITE {}", as, file);

        /* write printer sop class instance to a dicom stream */
        DicomOutputStream out = null;
        try {
            out = new DicomOutputStream(file);            
            out.writeDataset( Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian), rq);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to create the stream dicom:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(out);
        }
        return rq;
    }
    
    
    /**
     * Process of the N-CREATE-RQ messages. Is used to create instances for the
     * SOP classes Basic Film Box SOP Class, Basic Film Session SOP Class e
     * Presentation Lut SOP Class.
     * 
     * @param as association for witch the instance will be retrieved
     * @param rq requested meta-data atributes
     * @param rqAttrs requeste data atributes
     * @param rsp response data atributes
     * @return the requested meta data-atributes
     * @throws DicomServiceException 
     */
    
    public Attributes create(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
           throws DicomServiceException {
       System.out.println("create AS: " + as.toString());
       System.out.println("create RQ: " + rq.toString());
       //System.out.println("create rqATTRS: " + rqAttrs.toString());
       System.out.println("create RSP: " + rsp.toString());
        
       if (printerNCreateIOD != null) {
           ValidationResult result = rq.validate(printerNCreateIOD);
           if (!result.isValid())
               throw DicomServiceException.valueOf(result, rq);
       }
       if (storageDir == null)
           return null;
       String cuid = rq.getString(Tag.AffectedSOPClassUID);
       String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
       
       System.out.println("CREATE");
       System.out.println(cuid);
       System.out.println(iuid);
       
       File file = new File(storageDir, iuid);
       if (file.exists())
           throw new DicomServiceException(Status.DuplicateSOPinstance).
               setUID(Tag.AffectedSOPInstanceUID, iuid);
       
       DicomOutputStream out = null;
       LOG.info("{}: M-WRITE {}", as, file);
       try {
           out = new DicomOutputStream(file);
           out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian), rq);
       
       } catch (IOException e) {
           LOG.warn(as + ": Failed to store MPPS:", e);
           throw new DicomServiceException(Status.ProcessingFailure, e);
       } finally {
           SafeClose.close(out);
       }
       System.out.println("end create rqATTRS: " + rq.toString());
       return rq;
    }
    
    
    /**
     * Process of the N-SET-RQ messages. 
     * 
     * @param as
     * @param rq
     * @param rqAttrs
     * @param rsp
     * @return
     * @throws DicomServiceException 
     */
    
    public Attributes set(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
            throws DicomServiceException    {
        if (this.printerNSetIOD != null) {
            ValidationResult result = rqAttrs.validate(printerNSetIOD);
            if (!result.isValid())
                throw DicomServiceException.valueOf(result, rqAttrs);
        }
        if (storageDir == null)
            return null;
        String cuid = rq.getString(Tag.RequestedSOPClassUID);
        String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (!file.exists())
            throw new DicomServiceException(Status.NoSuchObjectInstance).
                setUID(Tag.AffectedSOPInstanceUID, iuid);
        LOG.info("{}: M-UPDATE {}", as, file);
        Attributes data;
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(file);
            data = in.readDataset(-1, -1);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to read MPPS:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(in);
        }

        data.addAll(rqAttrs);
        DicomOutputStream out = null;
        try {
            out = new DicomOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian),
                    data);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to update MPPS:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(out);
        }
        return rq;
    }
    
    
     public Attributes action(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp)
            throws DicomServiceException    {
        if (this.printerNSetIOD != null) {
            ValidationResult result = rqAttrs.validate(printerNSetIOD);
            if (!result.isValid())
                throw DicomServiceException.valueOf(result, rqAttrs);
        }
        if (storageDir == null)
            return null;
        String cuid = rq.getString(Tag.RequestedSOPClassUID);
        String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (!file.exists())
            throw new DicomServiceException(Status.NoSuchObjectInstance).
                setUID(Tag.AffectedSOPInstanceUID, iuid);
        LOG.info("{}: M-UPDATE {}", as, file);
        Attributes data;
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(file);
            data = in.readDataset(-1, -1);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to read MPPS:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(in);
        }
        if (!"IN PROGRESS".equals(data.getString(Tag.PerformedProcedureStepStatus)))
            BasicMPPSSCP.mayNoLongerBeUpdated();

        data.addAll(rqAttrs);
        DicomOutputStream out = null;
        try {
            out = new DicomOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid, UID.ExplicitVRLittleEndian),
                    data);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to update MPPS:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(out);
        }
        return null;
    }
}