package de.muenchen.oss.digiwf.dms.integration.adapter.out.fabasoft;

import com.fabasoft.schemas.websvc.lhmbai_15_1700_giwsd.*;
import de.muenchen.oss.digiwf.dms.integration.application.port.out.*;
import de.muenchen.oss.digiwf.dms.integration.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class FabasoftAdapter implements
        CreateFilePort,
        CreateProcedurePort,
        CreateDocumentPort,
        UpdateDocumentPort,
        DepositObjectPort,
        CancelObjectPort,
        ReadContentPort,
        SearchFilePort,
        SearchSubjectAreaPort,
        ReadMetadataPort {

    private final FabasoftProperties properties;
    private final LHMBAI151700GIWSDSoap wsClient;

    private final DMSErrorHandler dmsErrorHandler = new DMSErrorHandler();

    @Override
    public String createFile(File file, String user) {
        //logging for dms team
        log.info("calling CreateFileGI"
                + " Userlogin: " + user
                + " Apentry: " + file.getApentryCOO()
                + " Filesubj: " + file.getTitle()
                + " Shortname: " + file.getTitle()
                + " Apentrysearch: true"
        );

        final CreateFileGI request = new CreateFileGI();
        request.setUserlogin(user);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setApentry(file.getApentryCOO());
        request.setShortname(file.getTitle());
        request.setApentrysearch(true); // looks for free parent entry

        final CreateFileGIResponse response = this.wsClient.createFileGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    @Override
    public Procedure createProcedure(Procedure procedure, String user) {
        log.info("calling CreateProcedureGI: " + procedure.toString());

        final CreateProcedureGI request = new CreateProcedureGI();
        request.setUserlogin(user);
        request.setReferrednumber(procedure.getFileCOO());
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setShortname(procedure.getTitle());
        if (StringUtils.isNotBlank(procedure.getFileSubj())) {
            request.setFilesubj(procedure.getFileSubj());
        }
        request.setFiletype("Elektronisch");

        final CreateProcedureGIResponse response = this.wsClient.createProcedureGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return new Procedure(response.getObjid(), procedure.getFileCOO(), procedure.getFileSubj(), procedure.getTitle());
    }

    @Override
    public String createDocument(final Document document, final String user) {
        switch (document.getType()) {
            case EINGEHEND:
                return this.createIncomingDocument(document, user);
            case AUSGEHEND:
                return this.createOutgoingDocument(document, user);
            case INTERN:
                return this.createInternalDocument(document, user);
            default:
                throw new AssertionError("must not happen");
        }
    }

    private String createIncomingDocument(final Document document, final String user) {
        //logging for dms team
        log.info("calling CreateIncomingGI"
                + " Userlogin: " + user
                + " Referrednumber: " + document.getProcedureCOO()
                + " Shortname: " + document.getTitle()
                + " Filesubj: " + document.getTitle()
        );

        final CreateIncomingGI request = new CreateIncomingGI();
        request.setUserlogin(user);
        request.setReferrednumber(document.getProcedureCOO());
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setShortname(document.getTitle());
        if (document.getDate() != null) {
            request.setDelivery(this.convertDate(document.getDate()));
        }

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : document.getContents()) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final CreateIncomingGIResponse response = this.wsClient.createIncomingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    private String createOutgoingDocument(final Document document, final String user) {
        //logging for dms team
        log.info("calling CreateOutgoingGI"
                + " Userlogin: " + user
                + " Referrednumber: " + document.getProcedureCOO()
                + " Shortname: " + document.getTitle()
                + " Filesubj: " + document.getTitle()
        );

        final CreateOutgoingGI request = new CreateOutgoingGI();
        request.setUserlogin(user);
        request.setReferrednumber(document.getProcedureCOO());
        request.setBusinessapp(this.properties.getBusinessapp());

        request.setShortname(document.getTitle());
        if (document.getDate() != null) {
            request.setOutgoingdate(this.convertDate(document.getDate()));
        }

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : document.getContents()) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final CreateOutgoingGIResponse response = this.wsClient.createOutgoingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    private String createInternalDocument(final Document document, final String user) {
        //logging for dms team
        log.info("calling CreateInternalGI"
                + " Userlogin: " + user
                + " Referrednumber: " + document.getProcedureCOO()
                + " Shortname: " + document.getTitle()
                + " Filesubj: " + document.getTitle()
        );

        final CreateInternalGI request = new CreateInternalGI();
        request.setUserlogin(user);
        request.setReferrednumber(document.getProcedureCOO());
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setShortname(document.getTitle());
        if (document.getDate() != null) {
            request.setDeliverydate(this.convertDate(document.getDate()));
        }

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : document.getContents()) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final CreateInternalGIResponse response = this.wsClient.createInternalGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return response.getObjid();
    }

    @Override
    public void updateDocument(final String documentCOO, final DocumentType type, final List<Content> contents, final String user) {
        switch (type) {
            case EINGEHEND:
                this.updateIncomingDocument(documentCOO, contents, user);
                return;
            case AUSGEHEND:
                this.updateOutgoingDocument(documentCOO, contents, user);
                return;
            case INTERN:
                this.updateInternalDocument(documentCOO, contents, user);
                return;
            default:
                throw new AssertionError("must not happen");
        }
    }

    private void updateIncomingDocument(final String documentCOO, final List<Content> contents, final String user) {
        log.info("calling UpdateIncomingGI: " + documentCOO);

        final UpdateIncomingGI request = new UpdateIncomingGI();
        request.setUserlogin(user);
        request.setObjaddress(documentCOO);
        request.setBusinessapp(this.properties.getBusinessapp());

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : contents) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final UpdateIncomingGIResponse response = this.wsClient.updateIncomingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    private void updateOutgoingDocument(final String documentCOO, final List<Content> contents, final String user) {
        log.info("calling UpdateOutgoingGI: " + documentCOO);

        final UpdateOutgoingGI request = new UpdateOutgoingGI();
        request.setUserlogin(user);
        request.setObjaddress(documentCOO);
        request.setBusinessapp(this.properties.getBusinessapp());

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : contents) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final UpdateOutgoingGIResponse response = this.wsClient.updateOutgoingGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    private void updateInternalDocument(final String documentCOO, final List<Content> contents, final String user) {
        log.info("calling UpdateInternalGI: " + documentCOO);

        final UpdateInternalGI request = new UpdateInternalGI();
        request.setUserlogin(user);
        request.setObjaddress(documentCOO);
        request.setBusinessapp(this.properties.getBusinessapp());

        final ArrayOfLHMBAI151700GIAttachmentType attachmentType = new ArrayOfLHMBAI151700GIAttachmentType();
        final List<LHMBAI151700GIAttachmentType> files = attachmentType.getLHMBAI151700GIAttachmentType();

        for (final Content content : contents) {
            files.add(this.parseContent(content));
        }

        request.setGiattachmenttype(attachmentType);

        final UpdateInternalGIResponse response = this.wsClient.updateInternalGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }


    @Override
    public void depositObject(String objectCoo, String user) {
        log.info("calling DepositObject: " + objectCoo);

        final DepositObjectGI request = new DepositObjectGI();
        request.setUserlogin(user);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setObjaddress(objectCoo);

        final DepositObjectGIResponse response = this.wsClient.depositObjectGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    private LHMBAI151700GIAttachmentType parseContent(final Content content) {
        final LHMBAI151700GIAttachmentType attachment = new LHMBAI151700GIAttachmentType();
        attachment.setLHMBAI151700Filecontent(content.getContent());
        attachment.setLHMBAI151700Fileextension(content.getExtension());
        attachment.setLHMBAI151700Filename(content.getName());
        return attachment;
    }

    @Override
    public void cancelObject(final String objectCoo, final String user) {
        log.info("calling CancelObjectGI"
                + " Userlogin: " + user
                + " Objaddress: " + objectCoo
        );

        final CancelObjectGI cancelObjectGI = new CancelObjectGI();
        cancelObjectGI.setObjaddress(objectCoo);
        cancelObjectGI.setUserlogin(user);
        cancelObjectGI.setBusinessapp(this.properties.getBusinessapp());

        final CancelObjectGIResponse response = this.wsClient.cancelObjectGI(cancelObjectGI);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
    }

    @Override
    public List<Content> readContent(final List<String> coos, final String user) {

        final List<Content> files = new ArrayList<>();

        for (val coo : coos) {
            val request = new ReadContentObjectGI();
            request.setUserlogin(user);
            request.setBusinessapp(this.properties.getBusinessapp());
            request.setObjaddress(coo);
            val response = this.wsClient.readContentObjectGI(request);
            dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());
            files.add(this.map(response));
        }

        return files;
    }

    private Content map(ReadContentObjectGIResponse response) {
        return new Content(
                response.getGiattachmenttype().getLHMBAI151700Fileextension(),
                response.getGiattachmenttype().getLHMBAI151700Filename(),
                response.getGiattachmenttype().getLHMBAI151700Filecontent()
        );
    }

    @Override
    public List<String> searchFile(String searchString, String user, String reference, String value) {
        return this.searchObject(searchString, DMSObjectClass.Sachakte, user, reference, value).stream()
                .map(LHMBAI151700GIObjectType::getLHMBAI151700Objaddress)
                .toList();
    }

    @Override
    public List<String> searchSubjectArea(final String searchString, final String user) {
        return this.searchObject(searchString, DMSObjectClass.Aktenplaneintrag, user).stream()
                .map(LHMBAI151700GIObjectType::getLHMBAI151700Objaddress)
                .toList();
    }

    @Override
    public Metadata readMetadata(final String coo, final String username) {
        log.info("calling ReadMetadataObjectGI"
                + " Userlogin: " + username
                + " COO: " + coo
        );

        val request = new ReadMetadataObjectGI();
        request.setObjaddress(coo);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setUserlogin(username);
        val response = this.wsClient.readMetadataObjectGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return new Metadata(
                response.getObjname(),
                response.getObjclass(),
                String.format(this.properties.getUiurl(), coo)
        );
    }

    @Override
    public Metadata readContentMetadata(final String coo, final String username) {
        log.info("calling ReadContentObjectMetaDataGI"
                + " Userlogin: " + username
                + " COO: " + coo
        );

        val request = new ReadContentObjectMetaDataGI();
        request.setObjaddress(coo);
        request.setBusinessapp(this.properties.getBusinessapp());
        request.setUserlogin(username);
        val response = this.wsClient.readContentObjectMetaDataGI(request);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        return new Metadata(
                response.getGimetadatatype().getLHMBAI151700Filename(),
                response.getGimetadatatype().getLHMBAI151700Objclass(),
                String.format(this.properties.getUiurl(), coo)
        );
    }

    //------------------------------------- HELPER METHODS -------------------------------------------

    /**
     * Searches for an object.
     *
     * @param searchString   string to search for
     * @param dmsObjectClass object class for a soap request
     * @param username       account name
     * @return List of discovered objects
     */
    private List<LHMBAI151700GIObjectType> searchObject(final String searchString, final DMSObjectClass dmsObjectClass, final String username) {
        return searchObject(searchString, dmsObjectClass, username, null, null);
    }

    /**
     * Searches for an object.
     *
     * @param searchString   string to search for
     * @param dmsObjectClass object class for a soap request
     * @param username       account name
     * @param reference      (optional) 'Fachdatum'/business case to refine a search
     * @param value          (optional) value of 'Fachdatum'/business case
     * @return List of discovered objects
     */
    private List<LHMBAI151700GIObjectType> searchObject(final String searchString, final DMSObjectClass dmsObjectClass, final String username, final String reference, final String value) {
        //logging for dms team
        log.info("calling SearchObjNameGI"
                + " Userlogin: " + username
                + " SearchString: " + searchString
                + " Objclass: " + dmsObjectClass.getName()
                + " Reference: " + reference
                + " Value: " + value
        );

        final SearchObjNameGI params = new SearchObjNameGI();
        params.setUserlogin(username);
        params.setBusinessapp(this.properties.getBusinessapp());
        params.setObjclass(dmsObjectClass.getName());
        params.setSearchstring(searchString);
        if (Objects.nonNull(reference)) params.setReference(reference);
        if (Objects.nonNull(value)) params.setValue(value);

        final SearchObjNameGIResponse response = this.wsClient.searchObjNameGI(params);

        dmsErrorHandler.handleError(response.getStatus(), response.getErrormessage());

        if (response.getGiobjecttype() == null || response.getGiobjecttype().getLHMBAI151700GIObjectType() == null) {
            log.debug("No search results found");
            return Collections.emptyList();
        }
        return response.getGiobjecttype().getLHMBAI151700GIObjectType();
    }

    private XMLGregorianCalendar convertDate(final LocalDate date) {
        return DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(
                GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()))
        );
    }
}
