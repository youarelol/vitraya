package com.vitraya.adjudication.engine.email.service.factory;

import com.vitraya.adjudication.engine.email.service.EmailInterface;
import com.vitraya.adjudication.engine.email.service.NivaMailingServiceImmpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceFactory {


    @Autowired
    private NivaMailingServiceImmpl nivaMailingServiceImmpl;


    public EmailInterface getImplementationOfClient(long insurerId){

        if (insurerId==5){
            return nivaMailingServiceImmpl;
        }
        return null;
    }
}
