package com.vitraya.adjudication.engine.service.factory;

import com.vitraya.adjudication.engine.service.InsurerSpecificLimitsService;
import com.vitraya.adjudication.engine.service.NivaUcrServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InsurerSpecificLimitsFactory {


    @Autowired
    NivaUcrServiceImpl nivaLimitsService;


    public InsurerSpecificLimitsService getInsurerSpecificLimits(long insurerID) {

        if (insurerID == 5) { // Assuming 1 is the ID for Niva
            return nivaLimitsService;
        }

        return null; // or throw an exception if no service is found for the insurer code
    }

}
