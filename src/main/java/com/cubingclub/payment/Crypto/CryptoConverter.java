package com.cubingclub.payment.Crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter(autoApply = false)
@Component
public class CryptoConverter implements AttributeConverter<String, String> {

    private static AesCryptoUtil cryptoUtil;

    public CryptoConverter(AesCryptoUtil util) {
        CryptoConverter.cryptoUtil = util;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : cryptoUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : cryptoUtil.decrypt(dbData);
    }
}