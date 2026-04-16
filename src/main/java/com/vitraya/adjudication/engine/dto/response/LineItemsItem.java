package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class LineItemsItem {
    private LineItemMetadata metadata;
    private LineItemData data;

    public LineItemsItem(LineItemData data) {
        this.data = data;
    }

    public static LineItemsItem from(LineItemsItemWithStageDto source) {
        LineItemsItem item = new LineItemsItem();
        item.setMetadata(source.getMetadata());
        item.setData(source.getData());
        return item;
    }

    public BigDecimal getPerUnitAmount() {
        BigDecimal perUnitAmount = BigDecimal.ZERO;
        if (this.data != null && this.data.getTariff() != null) {
            if (this.data.getTariff().getInsurer_unit_amount() != null) {
                perUnitAmount = this.data.getTariff().getInsurer_unit_amount();
            } else {
                perUnitAmount = this.data.getRate().getValue() != null ? new BigDecimal(this.data.getRate().getValue())
                        : BigDecimal.ZERO;
            }
        }

        return perUnitAmount;
    }

    public BigDecimal getPerUnitTariffRate() {
        BigDecimal perUnitTariffRate = BigDecimal.ZERO;
        if (this.data != null && this.data.getTariff() != null) {
            if (this.data.getTariff().getInsurer_tariff_rate() != null) {
                perUnitTariffRate = this.data.getTariff().getInsurer_tariff_rate();
            } else {
                perUnitTariffRate = this.data.getTariff().getTariff_per_unit_amount() != null
                        ? this.data.getTariff().getTariff_per_unit_amount()
                        : BigDecimal.ZERO;
            }
        }

        return perUnitTariffRate;
    }
}