package com.vitraya.adjudication.engine.mysql.entity.insurerspecific;

import java.io.Serializable;
import java.util.Objects;

public class CompositeKeyUcr implements Serializable {

    private  String procedureCode ;
    private  String network_type;
    private  String provider_region;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeKeyUcr that = (CompositeKeyUcr) o;
        return Objects.equals(procedureCode, that.procedureCode) && Objects.equals(network_type, that.network_type) && Objects.equals(provider_region, that.provider_region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(procedureCode, network_type, provider_region);
    }
}
