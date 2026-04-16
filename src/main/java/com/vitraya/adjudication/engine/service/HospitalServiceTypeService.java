package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.HospitalServiceType;
import com.vitraya.adjudication.engine.mysql.impl.HospitalDataRepositoryImpl;
import com.vitraya.adjudication.engine.mysql.repository.HospitalServiceTypeRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HospitalServiceTypeService {

    private final HospitalServiceTypeRepository hospitalServiceTypeRepository;
    private final HospitalDataRepositoryImpl hospitalDataRepositoryImpl;

    public HospitalServiceTypeService(HospitalServiceTypeRepository hospitalServiceTypeRepository, HospitalDataRepositoryImpl hospitalDataRepositoryImpl) {
        this.hospitalServiceTypeRepository = hospitalServiceTypeRepository;
        this.hospitalDataRepositoryImpl = hospitalDataRepositoryImpl;
    }

    public HospitalServiceType getHospitalPrivateRoom(long hospitalId) {
        return hospitalServiceTypeRepository.findByHospitalIdAndPrivateAc(hospitalId, true);
    }

    public HospitalServiceType getHospitalRoomType(ClaimData claimData) {
        return hospitalServiceTypeRepository.getByServiceTypeAndHospitalId(claimData.getRoomType(), claimData.getHospitalId());
    }

    public Map<String, Object> getAllHospitals() {
        return hospitalDataRepositoryImpl.getAllHospitals().orElseThrow(() -> new VitrayaException(VitrayaErrorCodes.NO_HOSPITAL_FOUND));
    }

    public Map<String, Object> getHospitalRoomTypes(String hospitalCd) {
        return hospitalDataRepositoryImpl.getHospitalRoomTypes(hospitalCd).orElseThrow(() -> new VitrayaException(VitrayaErrorCodes.HOSPITAL_HAS_NO_ROOM));
    }

}
