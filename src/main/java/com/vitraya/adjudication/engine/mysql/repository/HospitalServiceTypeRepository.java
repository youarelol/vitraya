package com.vitraya.adjudication.engine.mysql.repository;


import com.vitraya.adjudication.engine.mysql.entity.HospitalServiceType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalServiceTypeRepository extends CrudRepository<HospitalServiceType, Long> {

    @Query("select * from hospital_service_type where hospital_id = :hospitalId and private_ac = :privateAc and enabled = 1")
    HospitalServiceType findByHospitalIdAndPrivateAc(@Param("hospitalId") long hospitalId, @Param("privateAc") boolean privateAc);

    @Query("select * from hospital_service_type where hospital_room_name = :hospitalRoomName and hospital_id = :hospitalId and enabled = 1")
    HospitalServiceType getByServiceTypeAndHospitalId(@Param("hospitalRoomName") String roomType, @Param("hospitalId") long hospitalId);

    @Query("select * from hospital_service_type where hospital_id = :hospitalId and enabled = 1")
    List<HospitalServiceType> findByHospitalId(@Param("hospitalId") long hospitalId);
}
