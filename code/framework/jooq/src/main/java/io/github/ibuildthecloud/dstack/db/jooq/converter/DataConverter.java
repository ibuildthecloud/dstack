package io.github.ibuildthecloud.dstack.db.jooq.converter;

import io.github.ibuildthecloud.dstack.json.JacksonJsonMapper;
import io.github.ibuildthecloud.dstack.json.JsonMapper;
import io.github.ibuildthecloud.dstack.util.exception.ExceptionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jooq.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConverter implements Converter<String, Map<String,Object>> {

    private static final Logger log = LoggerFactory.getLogger(DataConverter.class);

    private static final long serialVersionUID = 8496703546902403178L;

    JsonMapper mapper = new JacksonJsonMapper();

    @Override
    public Map<String, Object> from(String databaseObject) {
        if ( databaseObject == null ) {
            return null;
        }

        try {
            return mapper.readValue(databaseObject);
        } catch (IOException e) {
            log.error("Failed to unmarshall [{}]", databaseObject, e);
            Map<String,Object> result = new HashMap<String, Object>();
            result.put("_string", databaseObject);
            result.put("_exception", ExceptionUtils.toString(e));
            return result;
        }
    }

    @Override
    public String to(Map<String, Object> userObject) {
        if ( userObject == null ) {
            return null;
        }
        try {
            return mapper.writeValueAsString(userObject);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to marshall [" + userObject + "]", e);
        }
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Map<String,Object>> toType() {
        return (Class<Map<String,Object>>)(Class<?>)Map.class;
    }

}